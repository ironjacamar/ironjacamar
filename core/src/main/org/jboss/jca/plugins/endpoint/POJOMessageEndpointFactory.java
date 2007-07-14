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

import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.jboss.aop.AspectManager;
import org.jboss.aop.proxy.container.AOPProxyFactory;
import org.jboss.aop.proxy.container.AOPProxyFactoryParameters;
import org.jboss.aop.proxy.container.GeneratedAOPProxyFactory;

/**
 * A POJO MessageEndpointFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.5 $
 */
public class POJOMessageEndpointFactory implements MessageEndpointFactory
{
   /** The proxy factory */
   private static final AOPProxyFactory proxyFactory = new GeneratedAOPProxyFactory();

   /** The target */
   protected Object target;

   /** The aspect manager */
   protected AspectManager manager;
   
   /** The message listener class */
   protected Class messageListener;

   /**
    * Get the manager.
    * 
    * @return the manager.
    */
   public AspectManager getManager()
   {
      return manager;
   }

   /**
    * Set the manager.
    * 
    * @param manager The manager to set.
    */
   public void setManager(AspectManager manager)
   {
      this.manager = manager;
   }

   /**
    * Get the messageListener.
    * 
    * @return the messageListener.
    */
   public Class getMessageListener()
   {
      return messageListener;
   }

   /**
    * Set the messageListener.
    * 
    * @param messageListener The messageListener to set.
    */
   public void setMessageListener(Class messageListener)
   {
      this.messageListener = messageListener;
   }

   /**
    * Get the target.
    * 
    * @return the target.
    */
   public Object getTarget()
   {
      return target;
   }

   /**
    * Set the target.
    * 
    * @param target The target to set.
    */
   public void setTarget(Object target)
   {
      this.target = target;
   }
   
   public MessageEndpoint createEndpoint(XAResource resource) throws UnavailableException
   {
      if (target == null)
         throw new IllegalStateException("Null target");
      if (manager == null)
         throw new IllegalStateException("Null manager");
      if (messageListener == null)
         throw new IllegalStateException("Null messageListener");
      
      Class[] interfaces = new Class[] { messageListener, MessageEndpoint.class }; 
      
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setInterfaces(interfaces);
      params.setTarget(target);
      
      return (MessageEndpoint) proxyFactory.createAdvisedProxy(params);
   }

   public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI isDeliveryTransacted");
   }
}
