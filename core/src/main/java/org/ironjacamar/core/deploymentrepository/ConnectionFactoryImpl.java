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

import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.ironjacamar.core.api.connectionmanager.ConnectionManager;
import org.ironjacamar.core.api.deploymentrepository.ConfigProperty;
import org.ironjacamar.core.api.deploymentrepository.ConnectionFactory;
import org.ironjacamar.core.api.deploymentrepository.Pool;
import org.ironjacamar.core.api.deploymentrepository.Recovery;
import org.ironjacamar.core.spi.naming.JndiStrategy;
import org.ironjacamar.core.spi.statistics.StatisticsPlugin;

import java.util.Collection;

/**
 * A connection factory implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConnectionFactoryImpl implements ConnectionFactory
{
   /** Activated */
   private boolean activated;

   /** The JNDI name */
   private String jndiName;

   /** The connection factory */
   private Object cf;
   
   /** The config properties */
   private Collection<ConfigProperty> configProperties;

   /** The activation */
   private ConnectionDefinition activation;
   
   /** The connection manager */
   private ConnectionManager connectionManager;
   
   /** The pool */
   private Pool pool;
   
   /** The statistics */
   private StatisticsPlugin statistics;
   
   /** The recovery */
   private Recovery recovery;
   
   /** The JNDI strategy */
   private JndiStrategy jndiStrategy;
   
   /**
    * Constructor
    * @param jndiName The JNDI name
    * @param cf The connection factory
    * @param configProperties The configuration properties
    * @param activation The activation
    * @param connectionManager The connection manager
    * @param pool The pool
    * @param statistics The statistics
    * @param recovery The recovery module
    * @param jndiStrategy The JNDI strategy
    */
   public ConnectionFactoryImpl(String jndiName,
                                Object cf,
                                Collection<ConfigProperty> configProperties,
                                ConnectionDefinition activation,
                                ConnectionManager connectionManager,
                                Pool pool,
                                StatisticsPlugin statistics,
                                Recovery recovery,
                                JndiStrategy jndiStrategy)
   {
      this.activated = false;
      this.jndiName = jndiName;
      this.cf = cf;
      this.configProperties = configProperties;
      this.activation = activation;
      this.connectionManager = connectionManager;
      this.pool = pool;
      this.statistics = statistics;
      this.recovery = recovery;
      this.jndiStrategy = jndiStrategy;
   }
   
   /**
    * {@inheritDoc}
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * {@inheritDoc}
    */
   public Object getConnectionFactory()
   {
      return cf;
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
   public ConnectionDefinition getActivation()
   {
      return activation;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionManager getConnectionManager()
   {
      return connectionManager;
   }

   /**
    * {@inheritDoc}
    */
   public Pool getPool()
   {
      return pool;
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
         jndiStrategy.bind(jndiName, cf);
         ((org.ironjacamar.core.connectionmanager.pool.Pool)pool.getPool()).prefill();

         if (recovery != null)
            recovery.activate();
         
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
         if (recovery != null)
            recovery.deactivate();
         
         jndiStrategy.unbind(jndiName, cf);
         ((org.ironjacamar.core.connectionmanager.ConnectionManager)connectionManager).shutdown();

         activated = false;
         return true;
      }

      return false;
   }
}
