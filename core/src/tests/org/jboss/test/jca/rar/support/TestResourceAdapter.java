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
package org.jboss.test.jca.rar.support;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

/**
 * A TestResourceAdapter.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.4 $
 */
public class TestResourceAdapter implements ResourceAdapter
{
   protected BootstrapContext ctx;

   protected Map<TestActivationSpec, MessageEndpoint> endpoints = new ConcurrentHashMap<TestActivationSpec, MessageEndpoint>();

   public void start(BootstrapContext ctx) throws ResourceAdapterInternalException
   {
      this.ctx = ctx;
   }

   public void stop()
   {
      
      for (Map.Entry<TestActivationSpec, MessageEndpoint> entry : endpoints.entrySet())
      {
         MessageEndpoint endpoint = entry.getValue();
         if (endpoint != null)
            endpoint.release();
         endpoints.clear();
      }
      ctx = null;
   }
   
   public MessageEndpoint getEndpoint(String name) throws Exception
   {
      for (Map.Entry<TestActivationSpec, MessageEndpoint> entry : endpoints.entrySet())
      {
         TestActivationSpec spec = entry.getKey();
         if (name.equals(spec.getName()))
            return entry.getValue();
      }
      throw new Exception("MessageEndpoint not found for name: " + name);      
   }

   public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException
   {
      TestActivationSpec test = TestActivationSpec.class.cast(spec);
      MessageEndpoint endpoint = endpointFactory.createEndpoint(null);
      endpoints.put(test, endpoint);
   }

   public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
   {
      MessageEndpoint endpoint = endpoints.remove(spec);
      if (endpoint != null)
         endpoint.release();
   }

   public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException
   {
      return null;
   }
   
   public Timer createTimerFromBootstrapContext() throws UnavailableException
   {
      if (ctx == null)
         throw new IllegalStateException("No bootstrap context");
      Timer timer = ctx.createTimer();
      if (timer == null)
         throw new IllegalStateException("createTimer() returned null");
      return timer;
   }
   
   public WorkManager getWorkManagerFromBootstrapContext()
   {
      if (ctx == null)
         throw new IllegalStateException("No bootstrap context");
      WorkManager workManager = ctx.getWorkManager();
      if (workManager == null)
         throw new IllegalStateException("Null work manager");
      return workManager;
   }
   
   public XATerminator getXATerminatorFromBootstrapContext()
   {
      if (ctx == null)
         throw new IllegalStateException("No bootstrap context");
      XATerminator xaTerminator = ctx.getXATerminator();
      if (xaTerminator == null)
         throw new IllegalStateException("Null xa terminator");
      return xaTerminator;
   }
}
