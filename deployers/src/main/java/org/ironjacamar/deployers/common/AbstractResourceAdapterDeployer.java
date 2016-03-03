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

import org.ironjacamar.common.api.metadata.Defaults;
import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.resourceadapter.AdminObject;
import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.api.metadata.spec.InboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.XsdString;
import org.ironjacamar.core.api.connectionmanager.ConnectionManagerConfiguration;
import org.ironjacamar.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.ironjacamar.core.api.connectionmanager.pool.PoolConfiguration;
import org.ironjacamar.core.api.deploymentrepository.Deployment;
import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;
import org.ironjacamar.core.api.metadatarepository.Metadata;
import org.ironjacamar.core.api.metadatarepository.MetadataRepository;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.ConnectionManagerFactory;
import org.ironjacamar.core.connectionmanager.pool.PoolFactory;
import org.ironjacamar.core.deploymentrepository.ActivationSpecImpl;
import org.ironjacamar.core.deploymentrepository.AdminObjectImpl;
import org.ironjacamar.core.deploymentrepository.ConfigPropertyImpl;
import org.ironjacamar.core.deploymentrepository.ConnectionFactoryImpl;
import org.ironjacamar.core.deploymentrepository.DeploymentBuilder;
import org.ironjacamar.core.deploymentrepository.PoolImpl;
import org.ironjacamar.core.deploymentrepository.RecoveryImpl;
import org.ironjacamar.core.deploymentrepository.ResourceAdapterImpl;
import org.ironjacamar.core.metadatarepository.MetadataImpl;
import org.ironjacamar.core.recovery.DefaultRecoveryPlugin;
import org.ironjacamar.core.spi.naming.JndiStrategy;
import org.ironjacamar.core.spi.recovery.RecoveryPlugin;
import org.ironjacamar.core.spi.security.SubjectFactory;
import org.ironjacamar.core.spi.transaction.TransactionIntegration;
import org.ironjacamar.core.spi.transaction.recovery.XAResourceRecovery;
import org.ironjacamar.core.util.Injection;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

   /** The TransactionIntegration */
   protected TransactionIntegration transactionIntegration;

   /** The CachedConnectionManager */
   protected CachedConnectionManager cachedConnectionManager;

   /** The Subject Factory */
   protected SubjectFactory subjectFactory;

   /** The default pool type */
   protected String defaultPoolType;
   
   /**
    * Constructor
    */
   public AbstractResourceAdapterDeployer()
   {
      this.deploymentRepository = null;
      this.metadataRepository = null;
      this.bootstrapContext = null;
      this.jndiStrategy = null;
      this.transactionIntegration = null;
      this.cachedConnectionManager = null;
      this.subjectFactory = null;
      this.defaultPoolType = null;
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
    * Set the transaction integration
    * @param v The value
    */
   public void setTransactionIntegration(TransactionIntegration v)
   {
      this.transactionIntegration = v;
   }

   /**
    * Set the subject factory
    * @param subjectFactory The value
    */
   public void setSubjectFactory(SubjectFactory subjectFactory)
   {
      this.subjectFactory = subjectFactory;
   }

   /**
    * Set the cached connection manager
    * @param v The value
    */
   public void setCachedConnectionManager(CachedConnectionManager v)
   {
      this.cachedConnectionManager = v;
   }
   
   /**
    * Set the default pool type
    * @param v The value
    */
   public void setDefaultPoolType(String v)
   {
      this.defaultPoolType = v;
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
                                  connector.getResourceadapter().getConfigProperties(),
                                  activation.getConfigProperties(),
                                  transactionSupport,
                                  getProductName(connector), getProductVersion(connector),
                                  connector.getResourceadapter().getInboundResourceadapter());

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
    * @param overrides The config properties overrides
    * @param transactionSupport The transaction support level
    * @param productName The product name
    * @param productVersion The product version
    * @param ira The inbound resource adapter definition
    * @throws DeployException Thrown if the resource adapter cant be created
    */
   protected void
      createResourceAdapter(DeploymentBuilder builder,
                            String raClz,
                            Collection<org.ironjacamar.common.api.metadata.spec.ConfigProperty> configProperties,
                            Map<String, String> overrides,
                            TransactionSupportEnum transactionSupport,
                            String productName, String productVersion,
                            InboundResourceAdapter ira)
      throws DeployException
   {
      try
      {
         Class<?> clz = Class.forName(raClz, true, builder.getClassLoader());
         javax.resource.spi.ResourceAdapter resourceAdapter =
            (javax.resource.spi.ResourceAdapter)clz.newInstance();

         Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty> dcps =
            injectConfigProperties(resourceAdapter, configProperties, overrides, builder.getClassLoader());

         org.ironjacamar.core.spi.statistics.StatisticsPlugin statisticsPlugin = null;
         if (resourceAdapter instanceof org.ironjacamar.core.spi.statistics.Statistics)
            statisticsPlugin = ((org.ironjacamar.core.spi.statistics.Statistics)resourceAdapter).getStatistics();

         TransactionIntegration ti = null;
         if (isXA(transactionSupport))
         {
            ti = transactionIntegration;
         }

         builder.resourceAdapter(new ResourceAdapterImpl(resourceAdapter, bootstrapContext, dcps,
                                                         statisticsPlugin, productName, productVersion,
                                                         createInboundMapping(ira, builder.getClassLoader()),
                                                         ti));
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
            injectConfigProperties(mcf, findConfigProperties(mcfClass, connector), cd.getConfigProperties(),
                                   builder.getClassLoader());

         ConnectionManagerConfiguration cmc = new ConnectionManagerConfiguration();
         applyConnectionManagerConfiguration(cmc, cd);
         applyConnectionManagerConfiguration(cmc, cd.getSecurity());
         applyConnectionManagerConfiguration(cmc, cd.getTimeout());
         if (isXA(transactionSupport))
            applyConnectionManagerConfiguration(cmc, (org.ironjacamar.common.api.metadata.common.XaPool)cd.getPool());
         
         ConnectionManager cm =
            ConnectionManagerFactory.createConnectionManager(transactionSupport, mcf,
                                                             cd.isUseCcm() ? cachedConnectionManager : null,
                                                             cmc,
                                                             transactionIntegration);
         if (subjectFactory != null)
            cm.setSubjectFactory(subjectFactory);

         String poolType = cd.getPool() != null ? cd.getPool().getType() : null;

         if (poolType == null || poolType.equals(""))
            poolType = defaultPoolType;

         PoolConfiguration pc = new PoolConfiguration();
         applyPoolConfiguration(pc, cd.getPool());
         applyPoolConfiguration(pc, cd.getTimeout());
         applyPoolConfiguration(pc, cd.getValidation());

         org.ironjacamar.core.connectionmanager.pool.Pool pool = PoolFactory.createPool(poolType, cm, pc);
         cm.setPool(pool);

         org.ironjacamar.core.api.deploymentrepository.Pool dpool = new PoolImpl(pool, null);

         org.ironjacamar.core.spi.statistics.StatisticsPlugin statisticsPlugin = null;
         if (mcf instanceof org.ironjacamar.core.spi.statistics.Statistics)
            statisticsPlugin = ((org.ironjacamar.core.spi.statistics.Statistics)mcf).getStatistics();
         
         org.ironjacamar.core.api.deploymentrepository.Recovery recovery = null;
         if (isXA(transactionSupport))
         {
            recovery = createRecovery(mcf, cd);
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
            injectConfigProperties(adminObject, findConfigProperties(aoClass, connector), ao.getConfigProperties(),
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
    * @param overrides The overrides
    * @param classLoader The class loader
    * @return The deployment data
    * @exception Throwable Thrown if an error occurs
    */
   private Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty>
      injectConfigProperties(Object o,
                             Collection<org.ironjacamar.common.api.metadata.spec.ConfigProperty> configProperties,
                             Map<String, String> overrides,
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
            boolean readOnly = cp.getConfigPropertySupportsDynamicUpdates() != null ?
               cp.getConfigPropertySupportsDynamicUpdates().booleanValue() : true;
            boolean confidential = cp.getConfigPropertyConfidential() != null ?
               cp.getConfigPropertyConfidential().booleanValue() : false;
            boolean declared = true;

            Object value = cp.isValueSet() ? cp.getConfigPropertyValue().getValue() : null;
            if (overrides != null && overrides.containsKey(cp.getConfigPropertyName().getValue()))
               value = overrides.get(cp.getConfigPropertyName().getValue());

            if (value != null)
            {
               try
               {
                  injector.inject(o,
                                  cp.getConfigPropertyName().getValue(),
                                  value,
                                  cp.getConfigPropertyType().getValue());
               }
               catch (Throwable t)
               {
                  type = convertType(type);
                     
                  if (type != null)
                  {
                     injector.inject(o,
                                     cp.getConfigPropertyName().getValue(),
                                     value,
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
    * Is a support type
    * @param t The type
    * @return True if supported, otherwise false
    */
   private boolean isSupported(Class<?> t)
   {
      if (Boolean.class.equals(t) || boolean.class.equals(t) ||
          Byte.class.equals(t) || byte.class.equals(t) ||
          Short.class.equals(t) || short.class.equals(t) ||
          Integer.class.equals(t) || int.class.equals(t) ||
          Long.class.equals(t) || long.class.equals(t) ||
          Float.class.equals(t) || float.class.equals(t) ||
          Double.class.equals(t) || double.class.equals(t) ||
          Character.class.equals(t) || char.class.equals(t) ||
          String.class.equals(t))
         return true;

      return false;
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

   /**
    * Apply connection definition to connection manager configuration
    * @param cmc The connection manager configuration
    * @param cd The connection definition definition
    */
   private void applyConnectionManagerConfiguration(ConnectionManagerConfiguration cmc,
      org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition cd)
   {
      if (cd.getJndiName() != null)
         cmc.setJndiName(cd.getJndiName());

      if (cd.isSharable() != null)
         cmc.setSharable(cd.isSharable());

      if (cd.isEnlistment() != null)
         cmc.setEnlistment(cd.isEnlistment());

      if (cd.isConnectable() != null)
         cmc.setConnectable(cd.isConnectable());

      if (cd.isTracking() != null)
         cmc.setTracking(cd.isTracking());
   }

   /**
    * Apply security to connection manager configuration
    * @param cmc The connection manager configuration
    * @param s The security definition
    */
   private void applyConnectionManagerConfiguration(ConnectionManagerConfiguration cmc,
      org.ironjacamar.common.api.metadata.common.Security s)
   {
      if (s != null)
      {
         if (s.getSecurityDomain() != null)
            cmc.setSecurityDomain(s.getSecurityDomain());
      }
   }

   /**
    * Apply xa-pool to connection manager configuration
    * @param cmc The connection manager configuration
    * @param xp The xa-pool definition
    */
   private void applyConnectionManagerConfiguration(ConnectionManagerConfiguration cmc,
      org.ironjacamar.common.api.metadata.common.XaPool xp)
   {
      if (xp != null)
      {
         if (xp.isIsSameRmOverride() != null)
            cmc.setIsSameRMOverride(xp.isIsSameRmOverride());

         if (xp.isPadXid() != null)
            cmc.setPadXid(xp.isPadXid());

         if (xp.isWrapXaResource() != null)
            cmc.setWrapXAResource(xp.isWrapXaResource());
      }
   }

   /**
    * Apply timeout to connection manager configuration
    * @param cmc The connection manager configuration
    * @param t The timeout definition
    */
   private void applyConnectionManagerConfiguration(ConnectionManagerConfiguration cmc,
      org.ironjacamar.common.api.metadata.common.Timeout t)
   {
      if (t != null)
      {
         if (t.getAllocationRetry() != null)
            cmc.setAllocationRetry(t.getAllocationRetry());

         if (t.getAllocationRetryWaitMillis() != null)
            cmc.setAllocationRetryWaitMillis(t.getAllocationRetryWaitMillis());

         if (t.getXaResourceTimeout() != null)
            cmc.setXAResourceTimeout(t.getXaResourceTimeout());
      }
   }

   /**
    * Apply pool to pool configuration
    * @param pc The pool configuration
    * @param p The pool definition
    */
   private void applyPoolConfiguration(PoolConfiguration pc,
                                       org.ironjacamar.common.api.metadata.common.Pool p)
   {
      if (p != null)
      {
         if (p.getMinPoolSize() != null)
            pc.setMinSize(p.getMinPoolSize().intValue());

         if (p.getInitialPoolSize() != null)
            pc.setInitialSize(p.getInitialPoolSize().intValue());

         if (p.getMaxPoolSize() != null)
            pc.setMaxSize(p.getMaxPoolSize().intValue());

         if (p.isPrefill() != null)
            pc.setPrefill(p.isPrefill().booleanValue());
         if (p.getFlushStrategy() != null)
            pc.setFlushStrategy(p.getFlushStrategy());

      }
   }

   /**
    * Apply timeout to pool configuration
    * @param pc The pool configuration
    * @param t The timeout definition
    */
   private void applyPoolConfiguration(PoolConfiguration pc,
                                       org.ironjacamar.common.api.metadata.common.Timeout t)
   {
      if (t != null)
      {
         if (t.getBlockingTimeoutMillis() != null)
            pc.setBlockingTimeout(t.getBlockingTimeoutMillis().longValue());
         
         if (t.getIdleTimeoutMinutes() != null)
            pc.setIdleTimeoutMinutes(t.getIdleTimeoutMinutes().intValue());
      }
   }

   /**
    * Apply validation to pool configuration
    * @param pc The pool configuration
    * @param v The validation definition
    */
   private void applyPoolConfiguration(PoolConfiguration pc,
                                       org.ironjacamar.common.api.metadata.common.Validation v)
   {
      if (v != null)
      {
         if (v.isValidateOnMatch() != null)
            pc.setValidateOnMatch(v.isValidateOnMatch().booleanValue());

         if (v.isBackgroundValidation() != null)
            pc.setBackgroundValidation(v.isBackgroundValidation().booleanValue());

         if (v.getBackgroundValidationMillis() != null)
            pc.setBackgroundValidationMillis(v.getBackgroundValidationMillis().longValue());

         if (v.isUseFastFail() != null)
            pc.setUseFastFail(v.isUseFastFail().booleanValue());
      }
   }

   /**
    * Create a recovery module
    * @param mcf The ManagedConnectionFactory
    * @param cd The connection definition
    * @return The recovery module, or <code>null</code> if no recovery
    * @exception Throwable In case on an error
    */
   private org.ironjacamar.core.api.deploymentrepository.Recovery
      createRecovery(javax.resource.spi.ManagedConnectionFactory mcf, ConnectionDefinition cd) throws Throwable
   {
      Boolean padXid = Defaults.PAD_XID;
      Boolean isSameRMOverride = Defaults.IS_SAME_RM_OVERRIDE;
      Boolean wrapXAResource = Defaults.WRAP_XA_RESOURCE;
      String securityDomain = null;
      RecoveryPlugin plugin = null;
      Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty> dcps = null;
      
      if (transactionIntegration.getRecoveryRegistry() == null)
         return null;

      if (subjectFactory == null)
         return null;

      if (cd.getRecovery() != null && cd.getRecovery().isNoRecovery())
         return null;

      // Check security domain
      if (cd.getRecovery() != null && cd.getRecovery().getCredential() != null)
         securityDomain = cd.getRecovery().getCredential().getSecurityDomain();

      if (securityDomain == null && cd.getSecurity() != null)
         securityDomain = cd.getSecurity().getSecurityDomain();

      if (securityDomain == null)
         return null;

      if (cd.getRecovery() != null && cd.getRecovery().getPlugin() != null)
      {
         Extension extension = cd.getRecovery().getPlugin();
         Collection<org.ironjacamar.common.api.metadata.spec.ConfigProperty> configProperties =
            new ArrayList<org.ironjacamar.common.api.metadata.spec.ConfigProperty>();

         for (Map.Entry<String, String> property : extension.getConfigPropertiesMap().entrySet())
         {
            org.ironjacamar.common.api.metadata.spec.ConfigProperty c =
               new org.ironjacamar.common.metadata.spec.ConfigPropertyImpl(null,
                                                                           new XsdString(property.getKey(), null),
                                                                           XsdString.NULL_XSDSTRING,
                                                                           new XsdString(property.getValue(), null),
                                                                           Boolean.FALSE, Boolean.FALSE,
                                                                           Boolean.FALSE,
                                                                           null, false,
                                                                           null, null, null, null);

            configProperties.add(c);
         }

         Class<?> clz = Class.forName(extension.getClassName(), true, mcf.getClass().getClassLoader());
         plugin = (RecoveryPlugin)clz.newInstance();
         
         dcps = injectConfigProperties(plugin, configProperties, null, plugin.getClass().getClassLoader());
      }

      if (plugin == null)
         plugin = new DefaultRecoveryPlugin();

      if (dcps == null)
         dcps = new ArrayList<>(1);

      if (cd.getPool() != null)
      {
         org.ironjacamar.common.api.metadata.common.XaPool xaPool =
            (org.ironjacamar.common.api.metadata.common.XaPool)cd.getPool();

         if (xaPool.isPadXid() != null)
            padXid = xaPool.isPadXid();

         if (xaPool.isIsSameRmOverride() != null)
            isSameRMOverride = xaPool.isIsSameRmOverride();

         if (xaPool.isWrapXaResource() != null)
            wrapXAResource = xaPool.isWrapXaResource();
      }

      XAResourceRecovery r = transactionIntegration.createXAResourceRecovery(mcf,
                                                                             padXid,
                                                                             isSameRMOverride,
                                                                             wrapXAResource,
                                                                             securityDomain,
                                                                             subjectFactory,
                                                                             plugin,
                                                                             null);

      return new RecoveryImpl(plugin.getClass().getName(), dcps, r, cd.getJndiName(),
                              transactionIntegration.getRecoveryRegistry());
   }

   /**
    * Create an inbound mapping
    * @param ira The inbound resource adapter definition
    * @param cl The class loader
    * @return The mapping
    * @exception Exception Thrown in case of an error
    */
   private Map<String, ActivationSpecImpl> createInboundMapping(InboundResourceAdapter ira, ClassLoader cl)
      throws Exception
   {
      if (ira != null)
      {
         Map<String, ActivationSpecImpl> result = new HashMap<>();

         for (org.ironjacamar.common.api.metadata.spec.MessageListener ml :
                 ira.getMessageadapter().getMessagelisteners())
         {
            String type = ml.getMessagelistenerType().getValue();
            org.ironjacamar.common.api.metadata.spec.Activationspec as = ml.getActivationspec();
            String clzName = as.getActivationspecClass().getValue();
            Class<?> clz = Class.forName(clzName, true, cl);
            Map<String, Class<?>> configProperties = createPropertyMap(clz);
            Set<String> requiredConfigProperties = new HashSet<>();

            if (as.getRequiredConfigProperties() != null)
            {
               for (org.ironjacamar.common.api.metadata.spec.RequiredConfigProperty rcp :
                       as.getRequiredConfigProperties())
               {
                  requiredConfigProperties.add(rcp.getConfigPropertyName().getValue());
               }
            }

            ActivationSpecImpl asi = new ActivationSpecImpl(clzName, configProperties, requiredConfigProperties);
            if (!result.containsKey(type))
               result.put(type, asi);
         }
         
         return result;
      }

      return null;
   }

   /**
    * Get property map
    * @param clz The class
    * @return The map
    * @exception Exception Thrown in case of an error
    */
   private Map<String, Class<?>> createPropertyMap(Class<?> clz) throws Exception
   {
      Map<String, Class<?>> result = new HashMap<>();

      for (Method m : clz.getMethods())
      {
         if (m.getName().startsWith("set"))
         {
            if (m.getReturnType().equals(Void.TYPE) &&
                m.getParameterCount() == 1 &&
                isSupported(m.getParameterTypes()[0]))
               result.put(m.getName().substring(3), m.getParameterTypes()[0]);
         }
      }
      
      return result;
   }

   /**
    * Get the product name for the resource adapter
    * @param raXml The connector
    * @return The value
    */
   private String getProductName(Connector raXml)
   {
      if (raXml != null && !XsdString.isNull(raXml.getEisType()))
         return raXml.getEisType().getValue();

      return "";
   }

   /**
    * Get the product version for the resource adapter
    * @param raXml The connector
    * @return The value
    */
   private String getProductVersion(Connector raXml)
   {
      if (raXml != null)
         if (!XsdString.isNull(raXml.getResourceadapterVersion()))
            return raXml.getResourceadapterVersion().getValue();

      return "";
   }
}
