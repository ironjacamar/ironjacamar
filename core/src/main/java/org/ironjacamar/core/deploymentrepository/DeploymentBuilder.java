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
import org.ironjacamar.core.api.deploymentrepository.ConnectionFactory;
import org.ironjacamar.core.api.deploymentrepository.Deployment;
import org.ironjacamar.core.api.deploymentrepository.ResourceAdapter;
import org.ironjacamar.core.spi.classloading.ClassLoaderPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Deployment builder
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DeploymentBuilder
{
   /** The identifier */
   private String identifier;
   
   /** The name */
   private String name;
   
   /** The archive */
   private File archive;
   
   /** The class loader */
   private ClassLoader classLoader;
   
   /** The metadata */
   private Connector metadata;
   
   /** The activation */
   private Activation activation;
   
   /** The resource adapter */
   private ResourceAdapter resourceAdapter;
   
   /** The connection factories */
   private Collection<ConnectionFactory> connectionFactories;
   
   /** The admin objects */
   private Collection<AdminObject> adminObjects;

   /**The class loader plugin */
   private ClassLoaderPlugin classLoaderPlugin;


   /**
    * Constructor
    */
   public DeploymentBuilder()
   {
      this.identifier = null;
      this.name = null;
      this.archive = null;
      this.classLoader = null;
      this.metadata = null;
      this.activation = null;
      this.resourceAdapter = null;
      this.connectionFactories = null;
      this.adminObjects = null;
      this.classLoaderPlugin = null;
   }
   
   /**
    * Get identifier
    * @return The value
    */
   public String getIdentifier()
   {
      return identifier;
   }

   /**
    * Set identifier
    * @param v The value
    * @return The builder
    */
   public DeploymentBuilder identifier(String v)
   {
      identifier = v;
      return this;
   }

   /**
    * Get name
    * @return The value
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set name
    * @param v The value
    * @return The builder
    */
   public DeploymentBuilder name(String v)
   {
      name = v;
      return this;
   }

   /**
    * Get archive
    * @return The value
    */
   public File getArchive()
   {
      return archive;
   }

   /**
    * Set archive
    * @param v The value
    * @return The builder
    */
   public DeploymentBuilder archive(File v)
   {
      archive = v;
      return this;
   }

   /**
    * Get class loader
    * @return The value
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /**
    * Set class loader
    * @param v The value
    * @return The builder
    */
   public DeploymentBuilder classLoader(ClassLoader v)
   {
      classLoader = v;
      return this;
   }

   /**
    * Set class loader plugin
    * @param v The value
    * @return The builder
    */
   public DeploymentBuilder classLoaderPlugin(ClassLoaderPlugin v)
   {
      classLoaderPlugin = v;
      return this;
   }

   /**
    * Get metadata
    * @return The value
    */
   public Connector getMetadata()
   {
      return metadata;
   }

   /**
    * Set meta data
    * @param v The value
    * @return The builder
    */
   public DeploymentBuilder metadata(Connector v)
   {
      metadata = v;
      return this;
   }

   /**
    * Get activation
    * @return The value
    */
   public Activation getActivation()
   {
      return activation;
   }

   /**
    * Set activation
    * @param v The value
    * @return The builder
    */
   public DeploymentBuilder activation(Activation v)
   {
      activation = v;
      return this;
   }

   /**
    * Get resource adapter
    * @return The value
    */
   public ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
   }

   /**
    * Set resource adapter
    * @param v The value
    * @return The builder
    */
   public DeploymentBuilder resourceAdapter(ResourceAdapter v)
   {
      resourceAdapter = v;
      return this;
   }

   /**
    * Get connection factories
    * @return The value
    */
   public Collection<ConnectionFactory> getConnectionFactories()
   {
      if (connectionFactories == null)
         return Collections.emptyList();
      
      return Collections.unmodifiableCollection(connectionFactories);
   }

   /**
    * Add connection factory
    * @param v The value
    * @return The builder
    */
   public DeploymentBuilder connectionFactory(ConnectionFactory v)
   {
      if (connectionFactories == null)
         connectionFactories = new ArrayList<ConnectionFactory>();
      
      connectionFactories.add(v);
      return this;
   }

   /**
    * Get admin objects
    * @return The value
    */
   public Collection<AdminObject> getAdminObjects()
   {
      if (adminObjects == null)
         return Collections.emptyList();
      
      return Collections.unmodifiableCollection(adminObjects);
   }

   /**
    * Add admin object
    * @param v The value
    * @return The builder
    */
   public DeploymentBuilder adminObject(AdminObject v)
   {
      if (adminObjects == null)
         adminObjects = new ArrayList<AdminObject>();
      
      adminObjects.add(v);
      return this;
   }

   /**
    * Build
    * @return The deployment
    */
   public Deployment build()
   {
      return new DeploymentImpl(identifier, name, archive, classLoader,
                                metadata, activation,
                                resourceAdapter, getConnectionFactories(), getAdminObjects(), classLoaderPlugin);
   }
}
