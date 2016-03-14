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
import java.util.Collection;

/**
 * The deployment
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DeploymentImpl implements Deployment
{
   /** Activated */
   private boolean activated;

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
    * @param identifier The identifier
    * @param name The name
    * @param archive The archive
    * @param classLoader The class loader
    * @param metadata The metadata
    * @param activation The activation
    * @param resourceAdapter The resource adapter
    * @param connectionFactories The connection factories
    * @param adminObjects The admin objects
    * @param classLoaderPlugin the class loader plugin
    */
   public DeploymentImpl(String identifier,
                         String name,
                         File archive,
                         ClassLoader classLoader,
                         Connector metadata,
                         Activation activation,
                         ResourceAdapter resourceAdapter,
                         Collection<ConnectionFactory> connectionFactories,
                         Collection<AdminObject> adminObjects,
                         ClassLoaderPlugin classLoaderPlugin)
   {
      this.activated = false;
      this.identifier = identifier;
      this.name = name;
      this.archive = archive;
      this.classLoader = classLoader;
      this.metadata = metadata;
      this.activation = activation;
      this.resourceAdapter = resourceAdapter;
      this.connectionFactories = connectionFactories;
      this.adminObjects = adminObjects;
      this.classLoaderPlugin = classLoaderPlugin;
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
   public ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
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

   /**
    *{@inheritDoc}
    */
   @Override
   public ClassLoaderPlugin getClassLoaderPlugin()
   {
      return classLoaderPlugin;
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
         if (connectionFactories != null)
         {
            for (ConnectionFactory cf : connectionFactories)
            {
               cf.activate();
            }
         }

         if (adminObjects != null)
         {
            for (AdminObject ao : adminObjects)
            {
               ao.activate();
            }
         }

         if (resourceAdapter != null)
         {
            resourceAdapter.activate();
         }

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
         if (connectionFactories != null)
         {
            for (ConnectionFactory cf : connectionFactories)
            {
               cf.deactivate();
            }
         }

         if (adminObjects != null)
         {
            for (AdminObject ao : adminObjects)
            {
               ao.deactivate();
            }
         }

         if (resourceAdapter != null)
         {
            resourceAdapter.deactivate();
         }

         activated = false;
         return true;
      }
      
      return false;
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }

   /**
    *{@inheritDoc}
    */
   @Override
   public boolean equals(Object o)
   {
      if (o == this)
         return true;

      if (o == null || !(o instanceof DeploymentImpl))
         return false;

      return super.equals(o);
   }

   /**
    *{@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("Deployment@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[");
      sb.append("identifier=").append(identifier).append(" ");
      sb.append("name=").append(name).append(" ");
      sb.append("classLoader=").append(classLoader).append(" ");
      sb.append("metadata=").append(metadata).append(" ");
      sb.append("activation=").append(activation).append(" ");
      sb.append("resourceAdapter=").append(resourceAdapter).append(" ");
      sb.append("connectionFactories=").append(connectionFactories).append(" ");
      sb.append("adminObjects=").append(adminObjects);
      sb.append("]");
      
      return sb.toString();
   }
}
