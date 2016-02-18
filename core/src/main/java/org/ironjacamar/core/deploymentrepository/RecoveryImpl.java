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

import org.ironjacamar.core.api.deploymentrepository.ConfigProperty;
import org.ironjacamar.core.api.deploymentrepository.Recovery;
import org.ironjacamar.core.spi.transaction.recovery.XAResourceRecovery;
import org.ironjacamar.core.spi.transaction.recovery.XAResourceRecoveryRegistry;

import java.util.Collection;
import java.util.Collections;

/**
 * Recovery module implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class RecoveryImpl implements Recovery
{
   /** Activated */
   private boolean activated;

   /** Class name of the plugin used */
   private String pluginClassName;

   /** The config properties */
   private Collection<ConfigProperty> dcps;

   /** The recovery module */
   private XAResourceRecovery recovery;
   
   /** The recovery registry */
   private XAResourceRecoveryRegistry registry;
   
   /**
    * Constructor
    * @param pluginClassName The class name of the plugin
    * @param dcps The used config properties
    * @param recovery The recovery module
    * @param jndiName The JNDI name
    * @param registry The registry
    */
   public RecoveryImpl(String pluginClassName, Collection<ConfigProperty> dcps,
                       XAResourceRecovery recovery, String jndiName, XAResourceRecoveryRegistry registry)
   {
      this.activated = false;
      this.pluginClassName = pluginClassName;
      this.dcps = dcps;
      this.recovery = recovery;
      this.registry = registry;

      if (jndiName != null)
      {
         this.recovery.setJndiName(jndiName);
      }
      else
      {
         this.recovery.setJndiName("ResourceAdapter");
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getPluginClassName()
   {
      return pluginClassName;
   }
   
   /**
    * {@inheritDoc}
    */
   public Collection<ConfigProperty> getConfigProperties()
   {
      return Collections.unmodifiableCollection(dcps);
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
