/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.deploymentrepository;

import org.ironjacamar.core.api.deploymentrepository.InflowRecovery;
import org.ironjacamar.core.spi.transaction.recovery.XAResourceRecovery;
import org.ironjacamar.core.spi.transaction.recovery.XAResourceRecoveryRegistry;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.endpoint.MessageEndpointFactory;

/**
 * Inflow recovery module implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class InflowRecoveryImpl implements InflowRecovery
{
   /** Activated */
   private boolean activated;

   /** The message endpoint factory */
   private MessageEndpointFactory mef;

   /** The activation spec */
   private ActivationSpec as;

   /** The recovery module */
   private XAResourceRecovery recovery;
   
   /** The recovery registry */
   private XAResourceRecoveryRegistry registry;
   
   /**
    * Constructor
    * @param mef The message endpoint factory
    * @param as The activation spec
    * @param recovery The recovery module
    * @param registry The registry
    */
   public InflowRecoveryImpl(MessageEndpointFactory mef, ActivationSpec as,
                             XAResourceRecovery recovery, XAResourceRecoveryRegistry registry)
   {
      this.activated = false;
      this.mef = mef;
      this.as = as;
      this.recovery = recovery;
      this.registry = registry;
   }

   /**
    * {@inheritDoc}
    */
   public MessageEndpointFactory getMessageEndpointFactory()
   {
      return mef;
   }

   /**
    * {@inheritDoc}
    */
   public ActivationSpec getActivationSpec()
   {
      return as;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isActivated()
   {
      return activated;
   }

   /**
    * {@inheritDoc}
    */
   public boolean activate() throws Exception
   {
      if (!activated)
      {
         recovery.initialize();
         registry.addXAResourceRecovery(recovery);
         
         activated = true;
         return true;
      }

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean deactivate() throws Exception
   {
      if (activated)
      {
         recovery.shutdown();
         registry.removeXAResourceRecovery(recovery);
         
         activated = false;
         return true;
      }

      return false;
   }
}
