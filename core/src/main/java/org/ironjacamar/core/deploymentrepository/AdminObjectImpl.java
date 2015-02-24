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

import org.ironjacamar.core.api.deploymentrepository.AdminObject;
import org.ironjacamar.core.api.deploymentrepository.ConfigProperty;
import org.ironjacamar.core.spi.statistics.StatisticsPlugin;

import java.util.Collection;

/**
 * An admin object implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AdminObjectImpl implements AdminObject
{
   /** JNDI name */
   private String jndiName;
   
   /** Config properties */
   private Collection<ConfigProperty> configProperties;

   /** Activation */
   private org.ironjacamar.common.api.metadata.resourceadapter.AdminObject activation;

   /** Statistics */
   private StatisticsPlugin statistics;
   
   /**
    * Constructor
    * @param jndiName The JNDI name
    * @param configProperties The configuration properties
    * @param activation The activation
    * @param statistics The statistics
    */
   public AdminObjectImpl(String jndiName,
                          Collection<ConfigProperty> configProperties,
                          org.ironjacamar.common.api.metadata.resourceadapter.AdminObject activation,
                          StatisticsPlugin statistics)
   {
      this.jndiName = jndiName;
      this.configProperties = configProperties;
      this.activation = activation;
      this.statistics = statistics;
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
   public Collection<ConfigProperty> getConfigProperties()
   {
      return configProperties;
   }

   /**
    * {@inheritDoc}
    */
   public org.ironjacamar.common.api.metadata.resourceadapter.AdminObject getActivation()
   {
      return activation;
   }

   /**
    * {@inheritDoc}
    */
   public StatisticsPlugin getStatistics()
   {
      return statistics;
   }
}
