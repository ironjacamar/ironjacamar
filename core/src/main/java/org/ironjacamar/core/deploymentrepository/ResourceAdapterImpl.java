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
import org.ironjacamar.core.api.deploymentrepository.ResourceAdapter;
import org.ironjacamar.core.spi.statistics.StatisticsPlugin;

import java.util.Collection;

import javax.resource.spi.BootstrapContext;

/**
 * A resource adapter implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapterImpl implements ResourceAdapter
{
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

   /**
    * Constructor
    * @param resourceAdapter The resource adapter
    * @param bc The BootstrapContext
    * @param configProperties The configuration properties
    * @param statistics The statistics
    * @param recovery The recovery module
    */
   public ResourceAdapterImpl(javax.resource.spi.ResourceAdapter resourceAdapter,
                              BootstrapContext bc,
                              Collection<ConfigProperty> configProperties,
                              StatisticsPlugin statistics,
                              Recovery recovery)
   {
      this.resourceAdapter = resourceAdapter;
      this.bc = bc;
      this.configProperties = configProperties;
      this.statistics = statistics;
      this.recovery = recovery;
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
   public void activate() throws Exception
   {
      resourceAdapter.start(bc);
   }

   /**
    * {@inheritDoc}
    */
   public void deactivate() throws Exception
   {
      resourceAdapter.stop();
   }
}
