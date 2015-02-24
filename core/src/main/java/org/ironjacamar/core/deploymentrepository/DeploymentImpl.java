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

import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.core.api.deploymentrepository.AdminObject;
import org.ironjacamar.core.api.deploymentrepository.ConfigProperty;
import org.ironjacamar.core.api.deploymentrepository.ConnectionFactory;
import org.ironjacamar.core.api.deploymentrepository.Deployment;
import org.ironjacamar.core.spi.statistics.StatisticsPlugin;

import java.io.File;
import java.util.Collection;

/**
 * The deployment
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DeploymentImpl implements Deployment
{
   /** The identifier */
   private String identifier;
   
   /** The name */
   private String name;
   
   /** The archive */
   private File archive;
   
   /** The class loader */
   private ClassLoader classLoader;
   
   /** The config properties */
   private Collection<ConfigProperty> configProperties;
   
   /** The statistics */
   private StatisticsPlugin statistics;
   
   /** The metadata */
   private Connector metadata;
   
   /** The activation */
   private Activation activation;
   
   /** The connection factories */
   private Collection<ConnectionFactory> connectionFactories;
   
   /** The admin objects */
   private Collection<AdminObject> adminObjects;

   /**
    * Constructor
    * @param identifier The identifier
    * @param name The name
    * @param archive The archive
    * @param classLoader The class loader
    * @param configProperties The configuration properties
    * @param statistics The statistics
    * @param metadata The metadata
    * @param activation The activation
    * @param connectionFactories The connection factories
    * @param adminObjects The admin objects
    */
   public DeploymentImpl(String identifier,
                         String name,
                         File archive,
                         ClassLoader classLoader,
                         Collection<ConfigProperty> configProperties,
                         StatisticsPlugin statistics,
                         Connector metadata,
                         Activation activation,
                         Collection<ConnectionFactory> connectionFactories,
                         Collection<AdminObject> adminObjects)
   {
      this.identifier = identifier;
      this.name = name;
      this.archive = archive;
      this.classLoader = classLoader;
      this.configProperties = configProperties;
      this.statistics = statistics;
      this.metadata = metadata;
      this.activation = activation;
      this.connectionFactories = connectionFactories;
      this.adminObjects = adminObjects;
   }
   
   /**
    *{@inheritDoc}
    */
   public String getIdentifier()
   {
      return identifier;
   }

   /**
    *{@inheritDoc}
    */
   public String getName()
   {
      return name;
   }

   /**
    *{@inheritDoc}
    */
   public File getArchive()
   {
      return archive;
   }

   /**
    *{@inheritDoc}
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /**
    *{@inheritDoc}
    */
   public Collection<ConfigProperty> getConfigProperties()
   {
      return configProperties;
   }

   /**
    *{@inheritDoc}
    */
   public StatisticsPlugin getStatistics()
   {
      return statistics;
   }

   /**
    *{@inheritDoc}
    */
   public Connector getMetadata()
   {
      return metadata;
   }

   /**
    *{@inheritDoc}
    */
   public Activation getActivation()
   {
      return activation;
   }

   /**
    *{@inheritDoc}
    */
   public Collection<ConnectionFactory> getConnectionFactories()
   {
      return connectionFactories;
   }

   /**
    *{@inheritDoc}
    */
   public Collection<AdminObject> getAdminObjects()
   {
      return adminObjects;
   }
}
