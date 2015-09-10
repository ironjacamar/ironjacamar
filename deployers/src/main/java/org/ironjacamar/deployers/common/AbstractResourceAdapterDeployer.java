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
import org.ironjacamar.common.api.metadata.resourceadapter.AdminObject;
import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.core.api.connectionmanager.ConnectionManager;
import org.ironjacamar.core.api.deploymentrepository.Deployment;
import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.api.metadatarepository.Metadata;
import org.ironjacamar.core.api.metadatarepository.MetadataRepository;
import org.ironjacamar.core.connectionmanager.NoTransactionConnectionManager;
import org.ironjacamar.core.connectionmanager.pool.DefaultPool;
import org.ironjacamar.core.deploymentrepository.AdminObjectImpl;
import org.ironjacamar.core.deploymentrepository.ConfigPropertyImpl;
import org.ironjacamar.core.deploymentrepository.ConnectionFactoryImpl;
import org.ironjacamar.core.deploymentrepository.DeploymentBuilder;
import org.ironjacamar.core.deploymentrepository.PoolImpl;
import org.ironjacamar.core.deploymentrepository.ResourceAdapterImpl;
import org.ironjacamar.core.metadatarepository.MetadataImpl;
import org.ironjacamar.core.spi.naming.JndiStrategy;
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
   protected DeploymentRepository deploymentRepository;

   /** The MetadataRepository */
   protected MetadataRepository metadataRepository;

   /** The BootstrapContext */
   protected BootstrapContext bootstrapContext;

   /** The JndiStrategy */
   protected JndiStrategy jndiStrategy;

   /**
    * Constructor
    */
   public AbstractResourceAdapterDeployer()
   {
      this.deploymentRepository = null;
      this.metadataRepository = null;
      this.bootstrapContext = null;
      this.jndiStrategy = null;
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
    * Set the JNDI strategy
    * @param v The value
    */
   public void setJndiStrategy(JndiStrategy v)
   {
      this.jndiStrategy = v;
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
      try
      {
         DeploymentBuilder builder = new DeploymentBuilder();
         TransactionSupportEnum transactionSupport = getTransactionSupport(connector, activation);

         Metadata md = metadataRepository.findByName(activation.getArchive());

         builder.identifier(activation.getId());
         builder.name(md.getName());

         builder.metadata(connector);
         builder.activation(activation);

         builder.archive(md.getArchive());
         builder.classLoader(cl);

         if (connector.getResourceadapter().getResourceadapterClass() != null)
            createResourceAdapter(builder, connector.getResourceadapter().getResourceadapterClass(),
                                  connector.getResourceadapter().getConfigProperties(), transactionSupport);

         if (activation.getConnectionDefinitions() != null)
         {
            for (ConnectionDefinition cd : activation.getConnectionDefinitions())
            {
               createConnectionDefinition(builder, connector, cd, transactionSupport);
            }
         }

         if (activation.getAdminObjects() != null)
         {
            for (AdminObject ao : activation.getAdminObjects())
            {
               createAdminObject(builder, connector, ao);
            }
         }

         Deployment deployment = builder.build();

         deployment.activate();
      
         if (!deploymentRepository.registerDeployment(deployment))
            throw new DeployException("Not registered");
      
         return deployment;
      }
      catch (DeployException de)
      {
         throw de;
      }
      catch (Exception e)
      {
         throw new DeployException(e.getMessage(), e);
      }
   }

   /**
    * Create resource adapter instance
    * @param builder The deployment builder
    * @param raClz The resource adapter class
    * @param configProperties The config properties
    * @param transactionSupport The transaction support level
    * @throws DeployException Thrown if the resource adapter cant be created
    */
   protected void
      createResourceAdapter(DeploymentBuilder builder,
                            String raClz,
                            Collection<org.ironjacamar.common.api.metadata.spec.ConfigProperty> configProperties,
                            TransactionSupportEnum transactionSupport)
      throws DeployException
   {
      try
      {
         Class<?> clz = Class.forName(raClz, true, builder.getClassLoader());
         javax.resource.spi.ResourceAdapter resourceAdapter =
            (javax.resource.spi.ResourceAdapter)clz.newInstance();

         Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty> dcps =
            injectConfigProperties(resourceAdapter, configProperties, builder.getClassLoader());

         org.ironjacamar.core.spi.statistics.StatisticsPlugin statisticsPlugin = null;
         if (resourceAdapter instanceof org.ironjacamar.core.spi.statistics.Statistics)
            statisticsPlugin = ((org.ironjacamar.core.spi.statistics.Statistics)resourceAdapter).getStatistics();
         
         org.ironjacamar.core.api.deploymentrepository.Recovery recovery = null;
         if (isXA(transactionSupport))
         {
            // Do recovery
         }

         builder.resourceAdapter(new ResourceAdapterImpl(resourceAdapter, bootstrapContext, dcps,
                                                         statisticsPlugin, recovery));
      }
      catch (Throwable t)
      {
         throw new DeployException("createResourceAdapter", t);
      }
   }

   /**
    * Create connection definition instance
    * @param builder The deployment builder
    * @param connector The metadata
    * @param cd The connection definition
    * @param transactionSupport The transaction support level
    * @throws DeployException Thrown if the resource adapter cant be created
    */
   protected void
      createConnectionDefinition(DeploymentBuilder builder,
                                 Connector connector,
                                 ConnectionDefinition cd,
                                 TransactionSupportEnum transactionSupport)
      throws DeployException
   {
      try
      {
         String mcfClass = findManagedConnectionFactory(cd.getClassName(), connector);
         Class<?> clz = Class.forName(mcfClass, true, builder.getClassLoader());
         javax.resource.spi.ManagedConnectionFactory mcf =
            (javax.resource.spi.ManagedConnectionFactory)clz.newInstance();

         Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty> dcps =
            injectConfigProperties(mcf, findConfigProperties(mcfClass, connector),
                                   builder.getClassLoader());

         org.ironjacamar.core.connectionmanager.pool.Pool pool = new DefaultPool();
         ConnectionManager cm = new NoTransactionConnectionManager(pool);
         
         org.ironjacamar.core.api.deploymentrepository.Pool dpool = new PoolImpl(pool, null);

         org.ironjacamar.core.spi.statistics.StatisticsPlugin statisticsPlugin = null;
         if (mcf instanceof org.ironjacamar.core.spi.statistics.Statistics)
            statisticsPlugin = ((org.ironjacamar.core.spi.statistics.Statistics)mcf).getStatistics();
         
         org.ironjacamar.core.api.deploymentrepository.Recovery recovery = null;
         if (isXA(transactionSupport))
         {
            // Do recovery
         }

         if (builder.getResourceAdapter() != null)
            associateResourceAdapter(builder.getResourceAdapter().getResourceAdapter(), mcf);

         // Create ConnectionFactory
         Object cf = mcf.createConnectionFactory(cm);
         
         builder.connectionFactory(new ConnectionFactoryImpl(cd.getJndiName(), cf, dcps, cd, cm, dpool,
                                                             statisticsPlugin, recovery, jndiStrategy));
      }
      catch (Throwable t)
      {
         throw new DeployException("createConnectionDefinition", t);
      }
   }

   /**
    * Create admin object instance
    * @param builder The deployment builder
    * @param connector The metadata
    * @param ao The admin object
    * @throws DeployException Thrown if the resource adapter cant be created
    */
   protected void createAdminObject(DeploymentBuilder builder, Connector connector, AdminObject ao)
      throws DeployException
   {
      try
      {
         String aoClass = findAdminObject(ao.getClassName(), connector);
         Class<?> clz = Class.forName(aoClass, true, builder.getClassLoader());

         Object adminObject = clz.newInstance();

         Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty> dcps =
            injectConfigProperties(adminObject, findConfigProperties(aoClass, connector),
                                   builder.getClassLoader());
         
         org.ironjacamar.core.spi.statistics.StatisticsPlugin statisticsPlugin = null;
         if (adminObject instanceof org.ironjacamar.core.spi.statistics.Statistics)
            statisticsPlugin = ((org.ironjacamar.core.spi.statistics.Statistics)adminObject).getStatistics();
         
         if (builder.getResourceAdapter() != null)
            associateResourceAdapter(builder.getResourceAdapter().getResourceAdapter(), adminObject);

         builder.adminObject(new AdminObjectImpl(ao.getJndiName(), adminObject, dcps, ao,
                                                 statisticsPlugin, jndiStrategy));
      }
      catch (Throwable t)
      {
         throw new DeployException("createAdminObject", t);
      }
   }

   /**
    * Find the ManagedConnectionFactory class
    * @param className The initial class name
    * @param connector The metadata
    * @return The ManagedConnectionFactory
    */
   private String findManagedConnectionFactory(String className, Connector connector)
   {
      for (org.ironjacamar.common.api.metadata.spec.ConnectionDefinition cd :
              connector.getResourceadapter().getOutboundResourceadapter().getConnectionDefinitions())
      {
         if (className.equals(cd.getManagedConnectionFactoryClass().getValue()) ||
             className.equals(cd.getConnectionFactoryInterface().getValue()))
            return cd.getManagedConnectionFactoryClass().getValue();
      }
      return className;
   }

   /**
    * Find the AdminObject class
    * @param className The initial class name
    * @param connector The metadata
    * @return The AdminObject
    */
   private String findAdminObject(String className, Connector connector)
   {
      for (org.ironjacamar.common.api.metadata.spec.AdminObject ao :
              connector.getResourceadapter().getAdminObjects())
      {
         if (className.equals(ao.getAdminobjectClass().getValue()) ||
             className.equals(ao.getAdminobjectInterface().getValue()))
            return ao.getAdminobjectClass().getValue();
      }
      return className;
   }

   /**
    * Find the config properties for the class
    * @param className The class name
    * @param connector The metadata
    * @return The config properties
    */
   private Collection<org.ironjacamar.common.api.metadata.spec.ConfigProperty>
      findConfigProperties(String className, Connector connector)
   {
      for (org.ironjacamar.common.api.metadata.spec.ConnectionDefinition cd :
              connector.getResourceadapter().getOutboundResourceadapter().getConnectionDefinitions())
      {
         if (className.equals(cd.getManagedConnectionFactoryClass().getValue()) ||
             className.equals(cd.getConnectionFactoryInterface().getValue()))
            return cd.getConfigProperties();
      }
      return null;
   }

   /**
    * Inject the config properties into the object
    * @param o The object
    * @param configProperties The config properties
    * @param classLoader The class loader
    * @return The deployment data
    * @exception Throwable Thrown if an error occurs
    */
   private Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty>
      injectConfigProperties(Object o,
                             Collection<org.ironjacamar.common.api.metadata.spec.ConfigProperty> configProperties,
                             ClassLoader classLoader)
      throws Throwable
   {
      Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty> dcps = null;
      if (configProperties != null && configProperties.size() > 0)
      {
         Injection injector = new Injection();

         dcps = new ArrayList<org.ironjacamar.core.api.deploymentrepository.ConfigProperty>(configProperties.size());
         for (org.ironjacamar.common.api.metadata.spec.ConfigProperty cp : configProperties)
         {
            String name = cp.getConfigPropertyName().getValue();
            Class<?> type = Class.forName(cp.getConfigPropertyType().getValue(), true, classLoader);
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
                  injector.inject(o,
                                  cp.getConfigPropertyName().getValue(),
                                  cp.getConfigPropertyValue().getValue(),
                                  cp.getConfigPropertyType().getValue());
               }
               catch (Throwable t)
               {
                  type = convertType(type);
                     
                  if (type != null)
                  {
                     injector.inject(o,
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

            dcps.add(new ConfigPropertyImpl(o, name, type,
                                            value, readOnly, confidential,
                                            declared));
         }
      }
      return dcps;
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
    * Get the transaction support level
    * @param connector The spec metadata
    * @param activation The activation
    * @return True if XA, otherwise false
    */
   private TransactionSupportEnum getTransactionSupport(Connector connector, Activation activation)
   {
      if (activation.getTransactionSupport() != null)
         return activation.getTransactionSupport();

      if (connector.getResourceadapter().getOutboundResourceadapter() != null)
         return connector.getResourceadapter().getOutboundResourceadapter().getTransactionSupport();

      // We have to assume XA for pure inbound, overrides is done with activation
      return TransactionSupportEnum.XATransaction;
   }

   /**
    * Is XA deployment
    * @param tse The transaction support level
    * @return True if XA, otherwise false
    */
   private boolean isXA(TransactionSupportEnum tse)
   {
      return TransactionSupportEnum.XATransaction == tse;
   }
}
