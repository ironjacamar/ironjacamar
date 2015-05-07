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

package org.ironjacamar.deployers.common;

import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.core.api.deploymentrepository.Deployment;
import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.api.metadatarepository.Metadata;
import org.ironjacamar.core.api.metadatarepository.MetadataRepository;
import org.ironjacamar.core.deploymentrepository.ConfigPropertyImpl;
import org.ironjacamar.core.deploymentrepository.DeploymentBuilder;
import org.ironjacamar.core.deploymentrepository.ResourceAdapterImpl;
import org.ironjacamar.core.metadatarepository.MetadataImpl;
import org.ironjacamar.core.util.Injection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterAssociation;

/**
 * Base class for resource adapter deployers
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractResourceAdapterDeployer
{
   /** The DeploymentRepository */
   private DeploymentRepository deploymentRepository;

   /** The MetadataRepository */
   private MetadataRepository metadataRepository;

   /** The BootstrapContext */
   private BootstrapContext bootstrapContext;

   /**
    * Constructor
    */
   public AbstractResourceAdapterDeployer()
   {
      this.deploymentRepository = null;
      this.metadataRepository = null;
      this.bootstrapContext = null;
   }

   /**
    * Set the deployment repository
    * @param v The value
    */
   public void setDeploymentRepository(DeploymentRepository v)
   {
      this.deploymentRepository = v;
   }
   
   /**
    * Set the metadata repository
    * @param v The value
    */
   public void setMetadataRepository(MetadataRepository v)
   {
      this.metadataRepository = v;
   }
   
   /**
    * Set the bootstrap context
    * @param v The value
    */
   public void setBootstrapContext(BootstrapContext v)
   {
      this.bootstrapContext = v;
   }
   
   /**
    * Register a metadata instance with the repository
    * @param name The name
    * @param c The connector metadata
    * @param archive The archive
    * @return The metadata instance registered
    */
   public Metadata registerMetadata(String name, Connector c, File archive)
   {
      Metadata md = new MetadataImpl(name, c, archive);
      metadataRepository.registerMetadata(md);
      return md;
   }

   /**
    * Activate a resource adapter
    * @param connector The merged metadata
    * @param activation The activation
    * @param cl The class loader
    * @return The deployment
    * @exception DeployException Thrown if a deployment error occurs
    */
   public Deployment activate(Connector connector, Activation activation, ClassLoader cl)
      throws DeployException
   {
      DeploymentBuilder builder = new DeploymentBuilder();
      boolean isXA = isXA(connector, activation);

      Metadata md = metadataRepository.findByName(activation.getArchive());

      builder.identifier(activation.getId());
      builder.name(md.getName());

      builder.metadata(connector);
      builder.activation(activation);

      builder.archive(md.getArchive());
      builder.classLoader(cl);

      if (connector.getResourceadapter().getResourceadapterClass() != null)
         createResourceAdapter(builder, connector.getResourceadapter().getResourceadapterClass(),
                               connector.getResourceadapter().getConfigProperties(), isXA);
      
      Deployment deployment = builder.build();

      // deployment.activate();

      if (deployment.getResourceAdapter() != null)
         startContext(deployment.getResourceAdapter().getResourceAdapter());
      
      if (!deploymentRepository.registerDeployment(deployment))
         throw new DeployException("Not registered");
      
      return deployment;
   }

   /**
    * Create resource adapter instance
    * @param builder The deployment builder
    * @param raClz The resource adapter class
    * @param configProperties The config properties
    * @param isXA Is XA ?
    * @throws DeployException Thrown if the resource adapter cant be created
    */
   protected void
      createResourceAdapter(DeploymentBuilder builder,
                            String raClz,
                            Collection<org.ironjacamar.common.api.metadata.spec.ConfigProperty> configProperties,
                            boolean isXA)
      throws DeployException
   {
      try
      {
         Class<?> clz = Class.forName(raClz, true, builder.getClassLoader());
         javax.resource.spi.ResourceAdapter resourceAdapter =
            (javax.resource.spi.ResourceAdapter)clz.newInstance();

         Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty> dcps = null;
         if (configProperties != null && configProperties.size() > 0)
         {
            Injection injector = new Injection();

            dcps = new ArrayList<org.ironjacamar.core.api.deploymentrepository.ConfigProperty>(configProperties.size());
            for (org.ironjacamar.common.api.metadata.spec.ConfigProperty cp : configProperties)
            {
               String name = cp.getConfigPropertyName().getValue();
               Class<?> type = Class.forName(cp.getConfigPropertyType().getValue(), true, builder.getClassLoader());
               Object value = cp.isValueSet() ? cp.getConfigPropertyValue().getValue() : null;
               boolean readOnly = cp.getConfigPropertySupportsDynamicUpdates() != null ?
                  cp.getConfigPropertySupportsDynamicUpdates().booleanValue() : true;
               boolean confidential = cp.getConfigPropertyConfidential() != null ?
                  cp.getConfigPropertyConfidential().booleanValue() : false;
               boolean declared = true;

               if (cp.isValueSet())
               {
                  try
                  {
                     injector.inject(resourceAdapter,
                                     cp.getConfigPropertyName().getValue(),
                                     cp.getConfigPropertyValue().getValue(),
                                     cp.getConfigPropertyType().getValue());
                  }
                  catch (Throwable t)
                  {
                     type = convertType(type);
                     
                     if (type != null)
                     {
                        injector.inject(resourceAdapter,
                                        cp.getConfigPropertyName().getValue(),
                                        cp.getConfigPropertyValue().getValue(),
                                        type.getName());
                     }
                     else
                     {
                        throw t;
                     }
                  }
               }

               dcps.add(new ConfigPropertyImpl(resourceAdapter, name, type,
                                               value, readOnly, confidential,
                                               declared));
            }
         }

         org.ironjacamar.core.spi.statistics.StatisticsPlugin statisticsPlugin = null;
         if (resourceAdapter instanceof org.ironjacamar.core.spi.statistics.Statistics)
            statisticsPlugin = ((org.ironjacamar.core.spi.statistics.Statistics)resourceAdapter).getStatistics();
         
         org.ironjacamar.core.api.deploymentrepository.Recovery recovery = null;
         if (isXA)
         {
            // Do recovery
         }

         builder.resourceAdapter(new ResourceAdapterImpl(resourceAdapter, dcps, statisticsPlugin, recovery));
      }
      catch (Throwable t)
      {
         throw new DeployException("createResourceAdapter", t);
      }
   }

   /**
    * Convert type if possible
    * @param old The old type
    * @return The new type; otherwise <code>null</code>
    */
   private Class<?> convertType(Class<?> old)
   {
      if (Boolean.class.equals(old))
      {
         return boolean.class;
      }
      else if (boolean.class.equals(old))
      {
         return Boolean.class;
      }
      else if (Byte.class.equals(old))
      {
         return byte.class;
      }
      else if (byte.class.equals(old))
      {
         return Byte.class;
      }
      else if (Short.class.equals(old))
      {
         return short.class;
      }
      else if (short.class.equals(old))
      {
         return Short.class;
      }
      else if (Integer.class.equals(old))
      {
         return int.class;
      }
      else if (int.class.equals(old))
      {
         return Integer.class;
      }
      else if (Long.class.equals(old))
      {
         return long.class;
      }
      else if (long.class.equals(old))
      {
         return Long.class;
      }
      else if (Float.class.equals(old))
      {
         return float.class;
      }
      else if (float.class.equals(old))
      {
         return Float.class;
      }
      else if (Double.class.equals(old))
      {
         return double.class;
      }
      else if (double.class.equals(old))
      {
         return Double.class;
      }
      else if (Character.class.equals(old))
      {
         return char.class;
      }
      else if (char.class.equals(old))
      {
         return Character.class;
      }

      return null;
   }
   
   /**
    * Start the resource adapter
    * @param resourceAdapter The resource adapter
    * @throws DeployException Thrown if the resource adapter cant be started
    */
   @SuppressWarnings("unchecked")
   protected void startContext(javax.resource.spi.ResourceAdapter resourceAdapter)
      throws DeployException
   {
      // This needs to be part of the deployment activation
      try
      {
         resourceAdapter.start(bootstrapContext);
      }
      catch (Throwable t)
      {
         throw new DeployException("startContext", t);
      }
   }

   /**
    * Associate resource adapter with the object if it implements ResourceAdapterAssociation
    * @param resourceAdapter The resource adapter
    * @param object The possible association object
    * @throws DeployException Thrown if the resource adapter cant be associated
    */
   @SuppressWarnings("unchecked")
   protected void associateResourceAdapter(javax.resource.spi.ResourceAdapter resourceAdapter, Object object)
      throws DeployException
   {
      if (resourceAdapter != null && object != null)
      {
         if (object instanceof ResourceAdapterAssociation)
         {
            try
            {
               ResourceAdapterAssociation raa = (ResourceAdapterAssociation)object;
               raa.setResourceAdapter(resourceAdapter);
            }
            catch (Throwable t)
            {
               throw new DeployException("associateResourceAdapter", t);
            }
         }
      }
   }
   
   /**
    * Is XA deployment
    * @param connector The spec metadata
    * @param activation The activation
    * @return True if XA, otherwise false
    */
   private boolean isXA(Connector connector, Activation activation)
   {
      if (activation.getTransactionSupport() != null)
         return TransactionSupportEnum.XATransaction == activation.getTransactionSupport();

      if (connector.getResourceadapter().getOutboundResourceadapter() != null)
         return TransactionSupportEnum.XATransaction ==
            connector.getResourceadapter().getOutboundResourceadapter().getTransactionSupport();

      // We have to assume XA for pure inbound, overrides is done with activation
      return true;
   }
}
