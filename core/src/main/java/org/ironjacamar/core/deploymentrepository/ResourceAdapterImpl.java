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
import org.ironjacamar.core.api.deploymentrepository.MessageListener;
import org.ironjacamar.core.api.deploymentrepository.Recovery;
import org.ironjacamar.core.api.deploymentrepository.ResourceAdapter;
import org.ironjacamar.core.spi.statistics.StatisticsPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.endpoint.MessageEndpointFactory;

/**
 * A resource adapter implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapterImpl implements ResourceAdapter
{
   /** Activated */
   private boolean activated;

   /** The resource adapter */
   private javax.resource.spi.ResourceAdapter resourceAdapter;

   /** The BootstrapContext */
   private BootstrapContext bc;
   
   /** The config properties */
   private Collection<ConfigProperty> configProperties;

   /** The statistics */
   private StatisticsPlugin statistics;
   
   /** The recovery */
   private Recovery recovery;

   /** The message listeners */
   private Map<String, ActivationSpecImpl> messageListeners;
   
   /** Active endpoints */
   private Map<MessageEndpointFactory, Set<ActivationSpec>> activeEndpoints;
   
   /**
    * Constructor
    * @param resourceAdapter The resource adapter
    * @param bc The BootstrapContext
    * @param configProperties The configuration properties
    * @param statistics The statistics
    * @param recovery The recovery module
    * @param messageListeners The message listeners
    */
   public ResourceAdapterImpl(javax.resource.spi.ResourceAdapter resourceAdapter,
                              BootstrapContext bc,
                              Collection<ConfigProperty> configProperties,
                              StatisticsPlugin statistics,
                              Recovery recovery,
                              Map<String, ActivationSpecImpl> messageListeners)
   {
      this.activated = false;
      this.resourceAdapter = resourceAdapter;
      this.bc = bc;
      this.configProperties = configProperties;
      this.statistics = statistics;
      this.recovery = recovery;
      this.messageListeners = messageListeners;
      this.activeEndpoints = new HashMap<>();
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
   public javax.resource.spi.ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
   }

   /**
    * {@inheritDoc}
    */
   public BootstrapContext getBootstrapContext()
   {
      return bc;
   }

   /**
    * {@inheritDoc}
    */
   public Collection<ConfigProperty> getConfigProperties()
   {
      return configProperties;
   }

   /**
    * {@inheritDoc}
    */
   public StatisticsPlugin getStatistics()
   {
      return statistics;
   }

   /**
    * {@inheritDoc}
    */
   public Recovery getRecovery()
   {
      return recovery;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isMessageListenerSupported(String ml)
   {
      if (messageListeners != null && ml != null)
         return messageListeners.containsKey(ml);

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public MessageListener createMessageListener(String ml) throws Exception
   {
      if (isMessageListenerSupported(ml))
      {
         Class<?> mlClz = Class.forName(ml, true, resourceAdapter.getClass().getClassLoader());
         return new MessageListenerImpl(mlClz, messageListeners.get(ml), resourceAdapter.getClass().getClassLoader());
      }

      throw new Exception(ml + " not supported by " + resourceAdapter.getClass().getName());
   }
   
   /**
    * {@inheritDoc}
    */
   public void endpointActivation(MessageEndpointFactory mef, ActivationSpec as) throws Exception
   {
      if (mef == null)
         throw new Exception("MessageEndpointFactory is null");

      if (as == null)
         throw new Exception("ActivationSpec is null");

      // TODO: Bean validation
      
      try
      {
         as.validate();
      }
      catch (UnsupportedOperationException uoe)
      {
         // Ignore
      }

      resourceAdapter.endpointActivation(mef, as);

      // TODO: Recovery

      Set<ActivationSpec> s = activeEndpoints.get(mef);
      if (s == null)
         s = new HashSet<>();
      s.add(as);
      activeEndpoints.put(mef, s);
   }

   /**
    * {@inheritDoc}
    */
   public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec as) throws Exception
   {
      if (mef == null)
         throw new Exception("MessageEndpointFactory is null");

      if (as == null)
         throw new Exception("ActivationSpec is null");

      // TODO: Recovery

      try
      {
         resourceAdapter.endpointDeactivation(mef, as);
      }
      finally
      {
         Set<ActivationSpec> s = activeEndpoints.get(mef);
         if (s != null)
         {
            s.remove(as);
            if (s.size() == 0)
            {
               activeEndpoints.remove(mef);
            }
            else
            {
               activeEndpoints.put(mef, s);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean activate() throws Exception
   {
      if (!activated)
      {
         resourceAdapter.start(bc);

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
         if (activeEndpoints.size() > 0)
            throw new Exception("Still active endpoints");

         resourceAdapter.stop();

         activated = false;
         return true;
      }

      return false;
   }
}
