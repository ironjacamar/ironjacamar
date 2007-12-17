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

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import org.jboss.jca.spi.ResourceExceptionUtil;
import org.jboss.logging.Logger;

/**
 * An Activation.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.3 $
 */
public class Activation
{
   /** The logger */
   private static final Logger log = Logger.getLogger(Activation.class);
   
   /** The endpoint factory */
   protected MessageEndpointFactory messageEndpointFactory;
   
   /** The activation spec */
   protected ActivationSpec activationSpec;

   /** The resource adapter */
   protected ResourceAdapter resourceAdapter;

   /**
    * Get the activationSpec.
    * 
    * @return the activationSpec.
    */
   public ActivationSpec getActivationSpec()
   {
      return activationSpec;
   }

   /**
    * Set the activationSpec.
    * 
    * @param activationSpec The activationSpec to set.
    */
   public void setActivationSpec(ActivationSpec activationSpec)
   {
      this.activationSpec = activationSpec;
   }

   /**
    * Get the endpointFactory.
    * 
    * @return the endpointFactory.
    */
   public MessageEndpointFactory getMessageEndpointFactory()
   {
      return messageEndpointFactory;
   }

   /**
    * Set the endpointFactory.
    * 
    * @param messageEndpointFactory The messageEndpointFactory to set.
    */
   public void setMessageEndpointFactory(MessageEndpointFactory messageEndpointFactory)
   {
      this.messageEndpointFactory = messageEndpointFactory;
   }
   
   /**
    * Get the resourceAdapter.
    * 
    * @return the resourceAdapter.
    */
   public ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
   }

   /**
    * Set the resourceAdapter.
    * 
    * @param resourceAdapter The resourceAdapter to set.
    */
   public void setResourceAdapter(ResourceAdapter resourceAdapter)
   {
      this.resourceAdapter = resourceAdapter;
   }
   
   public void start() throws Exception
   {
      if (resourceAdapter == null)
         throw new IllegalStateException("Null resource adapter");
      if (messageEndpointFactory == null)
         throw new IllegalStateException("Null message endpoint factory");
      if (activationSpec == null)
         throw new IllegalStateException("Null activation spec");
      
      boolean trace = log.isTraceEnabled();
      try
      {
         if (trace)
            log.trace("Trying to validate ActivationSpec " + activationSpec);
         activationSpec.validate();
      }
      catch (UnsupportedOperationException e)
      {
         log.debug("Validation is not supported for ActivationSpec: " + activationSpec.getClass().getName());
      }
      
      if (trace)
         log.trace("Activation rar=" + resourceAdapter + " mef=" + messageEndpointFactory + " spec=" + activationSpec);
      try
      {
         resourceAdapter.endpointActivation(messageEndpointFactory, activationSpec);
      }
      catch (Throwable t)
      {
         throw ResourceExceptionUtil.checkResourceAdapterInternal(t);
      }
   }
   
   public void stop()
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Deactivation rar=" + resourceAdapter + " mef=" + messageEndpointFactory + " spec=" + activationSpec);
      try
      {
         resourceAdapter.endpointDeactivation(messageEndpointFactory, activationSpec);
      }
      catch (Throwable t)
      {
         log.warn("Error during deactivation rar=" + resourceAdapter + " mef=" + messageEndpointFactory + " spec=" + activationSpec, t);
      }
   }
}
