/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.jca.plugins.endpoint;

import java.lang.reflect.Method;

import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.jca.plugins.advice.AbstractJCAInterceptor;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * Implements the message endpoint requirements.
 *
 * @todo transactions and classloaders
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.3 $
 */
public class POJOMessageEndpointAdvice extends AbstractJCAInterceptor
{
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled(); 
   
   /** Cached version of our proxy string */
   private String cachedProxyString = null;
   
   /** Whether this proxy has had before invoked */
   protected SynchronizedBoolean beforeInvoked = new SynchronizedBoolean(false);
   
   /** Whether this proxy has been released */
   protected SynchronizedBoolean released = new SynchronizedBoolean(false);
   
   /** Whether we have delivered a message */
   protected SynchronizedBoolean delivered = new SynchronizedBoolean(false);
   
   /** The in use thread */
   protected Thread inUseThread = null;
   
   public Object invoke(Invocation invocation) throws Throwable
   {
      MethodInvocation mi = (MethodInvocation) invocation;
      check(mi);
      
      Method method = mi.getMethod();
      if (MessageEndpoint.class.equals(method.getDeclaringClass()))
      {
         String name = method.getName();
         if ("beforeDelivery".equals(name))
            beforeDelivery(mi);
         else if ("afterDelivery".equals(name))
            afterDelivery(mi);
         else if ("release".equals(name))
            release(mi);
         return null;
      }
      else
      {
         return deliver(mi);
      }
   }
   
   /**
    * Deliver the message
    * 
    * @param mi the method
    * @return the result of the delivery
    * @throws Throwable for any error
    */
   protected Object deliver(MethodInvocation mi) throws Throwable
   {
      // Have we already delivered a message?
      if (delivered.get())
         throw new IllegalStateException("Multiple message delivery between before and after delivery is not allowed for message endpoint " + getProxyString(mi));

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " delivering");
      
      // Mark delivery if beforeDelivery was invoked
      if (beforeInvoked.get())
         delivered.set(true);

      try
      {
         Object result = mi.invokeNext();
         if (trace)
            log.trace("MessageEndpoint " + getProxyString(mi) + " delivered");
         return result;
      }
      catch (Throwable t)
      {
         if (trace)
            log.trace("MessageEndpoint " + getProxyString(mi) + " delivery error", t);
         throw t;
      }
      finally
      {
         // No before/after delivery, end any transaction and release the lock
         if (beforeInvoked.get() == false)
            releaseThreadLock(mi);
      }
   }
   
   /**
    * Release this message endpoint.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void release(MethodInvocation mi) throws Throwable
   {
      // We are now released
      released.set(true);

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " released");
      
      // Tidyup any outstanding delivery
      if (beforeInvoked.get())
      {
         try
         {
            finish("release", mi, false);
         }
         catch (Throwable t)
         {
            log.warn("Error in release ", t);
         }
      }
   }
   
   /**
    * Before delivery processing.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void beforeDelivery(MethodInvocation mi) throws Throwable
   {
      // Called out of sequence
      if (beforeInvoked.get())
         throw new IllegalStateException("Missing afterDelivery from the previous beforeDelivery for message endpoint " + getProxyString(mi));

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " before");
      beforeInvoked.set(true);
   }
   
   /**
    * After delivery processing.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void afterDelivery(MethodInvocation mi) throws Throwable
   {
      // Called out of sequence
      if (beforeInvoked.get() == false)
         throw new IllegalStateException("afterDelivery without a previous beforeDelivery for message endpoint " + getProxyString(mi));
      
      // Finish this delivery committing if we can
      try
      {
         finish("afterDelivery", mi, true);
      }
      catch (Throwable t)
      {
         throw new ResourceException(t);
      }
      finally
      {
         beforeInvoked.set(false);
      }
   }

   /**
    * Check the state of the method endpoint
    * 
    * @param mi the method invocation
    */
   protected void check(MethodInvocation mi)
   {
      // Are we still useable?
      trace = log.isTraceEnabled();
      if (released.get())
         throw new IllegalStateException("This message endpoint + " + getProxyString(mi) + " has been released");

      // Concurrent invocation?
      Thread currentThread = Thread.currentThread();
      if (inUseThread != null && inUseThread != currentThread)
         throw new IllegalStateException("This message endpoint + " + getProxyString(mi) + " is already in use by another thread " + inUseThread);
      inUseThread = currentThread;
      
      String method = mi.getMethod().getName();
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " in use by " + method + " " + inUseThread);
   }
   
   /**
    * Finish the current delivery
    * 
    * @param context the lifecycle method
    * @param mi the invocation
    * @param commit whether to commit
    * @throws Throwable for any error
    */
   protected void finish(String context, MethodInvocation mi, boolean commit) throws Throwable
   {
      // Reset delivered flag
      delivered.set(false);
      // We no longer hold the lock
      releaseThreadLock(mi);
   }

   /**
    * Release the thread lock
    * 
    * @param mi the invocation
    */
   protected void releaseThreadLock(MethodInvocation mi)
   {
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " no longer in use by " + inUseThread);
      inUseThread = null;
   }
   
   /**
    * Get our proxy's string value.
    * 
    * @param mi the invocation
    * @return the string
    */
   protected String getProxyString(MethodInvocation mi)
   {
      if (cachedProxyString == null)
         cachedProxyString = mi.getTargetObject().toString();
      return cachedProxyString;
   }
}
