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
import org.ironjacamar.core.bootstrapcontext.BootstrapContextCoordinator;
import org.ironjacamar.core.bootstrapcontext.CloneableBootstrapContext;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.ConnectionManagerFactory;
import org.ironjacamar.core.connectionmanager.pool.Capacity;
import org.ironjacamar.core.connectionmanager.pool.JanitorFactory;
import org.ironjacamar.core.connectionmanager.pool.PoolFactory;
import org.ironjacamar.core.connectionmanager.pool.capacity.CapacityFactory;
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
import org.ironjacamar.core.spi.bv.BeanValidation;
import org.ironjacamar.core.spi.classloading.ClassLoaderPlugin;
import org.ironjacamar.core.spi.naming.JndiStrategy;
import org.ironjacamar.core.spi.recovery.RecoveryPlugin;
import org.ironjacamar.core.spi.security.SubjectFactory;
import org.ironjacamar.core.spi.transaction.TransactionIntegration;
import org.ironjacamar.core.spi.transaction.recovery.XAResourceRecovery;
import org.ironjacamar.core.util.Injection;
import org.ironjacamar.deployers.DeployersBundle;
import org.ironjacamar.deployers.DeployersLogger;
import org.ironjacamar.validator.Failure;
import org.ironjacamar.validator.FailureHelper;
import org.ironjacamar.validator.Key;
import org.ironjacamar.validator.Severity;
import org.ironjacamar.validator.Validate;
import org.ironjacamar.validator.ValidateClass;
import org.ironjacamar.validator.Validator;
import org.ironjacamar.validator.ValidatorException;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.TransactionSupport;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidatorFactory;

import org.jboss.logging.Messages;

/**
 * Base class for resource adapter deployers
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractResourceAdapterDeployer
{
   /** The bundle */
   private static DeployersBundle bundle = Messages.getBundle(DeployersBundle.class);

   /** the logger **/
   protected final DeployersLogger log;

   /** The DeploymentRepository */
   protected DeploymentRepository deploymentRepository;

   /** The MetadataRepository */
   protected MetadataRepository metadataRepository;

   /** The BootstrapContext */
   protected BootstrapContextCoordinator bootstrapContextCoordinator;

   /** The JndiStrategy */
   protected JndiStrategy jndiStrategy;

   /** The TransactionIntegration */
   protected TransactionIntegration transactionIntegration;

   /** The CachedConnectionManager */
   protected CachedConnectionManager cachedConnectionManager;

   /** The Subject Factory */
   protected SubjectFactory subjectFactory;

   /** The class loader plugin */
   protected ClassLoaderPlugin classLoaderPlugin;

   /** The bean validation */
   protected BeanValidation beanValidation;

   /** The default pool type */
   protected String defaultPoolType;

   /** archiveValidation option */
   protected boolean archiveValidation;

   /** archiveValidation FailOnWarn option */
   protected boolean archiveValidationFailOnWarn;

   /** archiveValidation FailOnError option */
   protected boolean archiveValidationFailOnError;

   /** List of objs needing a validation */
   private List<Validate> validationObj = new ArrayList<>();
   
   /**
    * Constructor
    */
   public AbstractResourceAdapterDeployer()
   {
      this.log = getLogger();
      this.deploymentRepository = null;
      this.metadataRepository = null;
      this.bootstrapContextCoordinator = null;
      this.jndiStrategy = null;
      this.transactionIntegration = null;
      this.cachedConnectionManager = null;
      this.subjectFactory = null;
      this.classLoaderPlugin = null;
      this.beanValidation = null;
      this.defaultPoolType = null;
      this.archiveValidation = false;
      this.archiveValidationFailOnError = false;
      this.archiveValidationFailOnWarn = false;
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
   public void setBootstrapContextCoordinator(BootstrapContextCoordinator v)
   {
      this.bootstrapContextCoordinator = v;
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
    * Set the class loader plugin
    * @param classLoaderPlugin The value
    */
   public void setClassLoaderPlugin(ClassLoaderPlugin classLoaderPlugin)
   {
      this.classLoaderPlugin = classLoaderPlugin;
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
    * Set the bean validation
    * @param v The value
    */
   public void setBeanValidation(BeanValidation v)
   {
      this.beanValidation = v;
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
    * Set the archive validation option
    * @param archiveValidation the boolean setting the config option
    */
   public void setArchiveValidation(boolean archiveValidation)
   {
      this.archiveValidation = archiveValidation;
   }

   /**
    * Set the fail on warn option
    * @param archiveValidationFailOnWarn the boolean setting the config option
    */
   public void setArchiveValidationFailOnWarn(boolean archiveValidationFailOnWarn)
   {
      this.archiveValidationFailOnWarn = archiveValidationFailOnWarn;
   }

   /**
    * Set the fail on error option
    * @param archiveValidationFailOnError the boolean setting the config option
    */
   public void setArchiveValidationFailOnError(boolean archiveValidationFailOnError)
   {
      this.archiveValidationFailOnError = archiveValidationFailOnError;
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
    * @param archiveName The name of the archive
    * @param root The root directory of the extracted resource adapter
    * @param cl The class loader
    * @return The deployment
    * @exception DeployException Thrown if a deployment error occurs
    */
   public Deployment activate(Connector connector, Activation activation, String archiveName, File root, ClassLoader cl)
      throws DeployException
   {
      log.tracef("Connector=%s", connector);
      log.tracef("Activation=%s", stripPassword(activation.toString()));
      log.tracef("ArchiveName=%s", archiveName);
      log.tracef("Root=%s", root.getAbsolutePath());

      try
      {
         DeploymentBuilder builder = new DeploymentBuilder();
         TransactionSupportEnum transactionSupport = getTransactionSupport(connector, activation);

         Metadata md = metadataRepository.findByName(archiveName);

         builder.identifier(activation.getId());
         builder.name(md.getName());

         builder.metadata(connector);
         builder.activation(activation);

         builder.archive(md.getArchive());
         builder.classLoader(cl);
         builder.classLoaderPlugin(classLoaderPlugin);

         loadNativeLibraries(root);

         if (connector.getResourceadapter().getResourceadapterClass() != null)
         {
            CloneableBootstrapContext bootstrapContext = createBootstrapContext(connector, activation);
            createResourceAdapter(builder, connector.getResourceadapter().getResourceadapterClass(),
                  connector.getResourceadapter().getConfigProperties(), activation.getConfigProperties(),
                  transactionSupport, getProductName(connector), getProductVersion(connector),
                  connector.getResourceadapter().getInboundResourceadapter(), bootstrapContext);
         }

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

         Set<Failure> failures = new HashSet<>();
         failures = validateArchive(validationObj, failures);

         if (archiveValidationFailOnWarn &&
               (hasFailuresLevel(failures, Severity.WARNING) || hasFailuresLevel(failures, Severity.ERROR)) ||
               (archiveValidationFailOnError && hasFailuresLevel(failures, Severity.ERROR)))
         {
            Validator v = new Validator();
            throw new ValidatorException(printFailuresLog(v, failures), failures,
                  v.getResourceBundle());
         }
         else
         {
            if (failures != null && failures.size() > 0)
            {
               log.validationInvalidArchive(root.getName());
            }

            printFailuresLog(new Validator(), failures);
         }

         if (is16(deployment.getMetadata()))
            verifyBeanValidation(deployment);

         deployment.activate();
      
         if (!deploymentRepository.registerDeployment(deployment))
            throw new DeployException(bundle.unableToRegister(deployment.getIdentifier(), deployment.getName()));

         log.deployed(archiveName);

         return deployment;
      }
      catch (DeployException de)
      {
         throw de;
      }
      catch (Exception e)
      {
         throw new DeployException(bundle.deploymentFailed(archiveName), e);
      }
   }

   private CloneableBootstrapContext createBootstrapContext(Connector connector, Activation activation)
   {
      CloneableBootstrapContext bootstrapContext;
      String bootstrapContextName;
      if (activation != null && activation.getBootstrapContext() != null &&
            !activation.getBootstrapContext().trim().equals(""))
      {
         bootstrapContextName = activation.getBootstrapContext();

         String bootstrapContextIdentifier = bootstrapContextCoordinator
               .createIdentifier(connector.getResourceadapter().getResourceadapterClass(),
                     connector.getResourceadapter().getConfigProperties(), bootstrapContextName);
         bootstrapContext = bootstrapContextCoordinator
               .createBootstrapContext(bootstrapContextIdentifier, bootstrapContextName);
      }
      else
      {
         bootstrapContext = bootstrapContextCoordinator.getDefaultBootstrapContext();
      }
      return bootstrapContext;
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
    * @param bootstrapContext the bootstrapContext to use
    * @throws DeployException Thrown if the resource adapter cant be created
    */
   protected void
      createResourceAdapter(DeploymentBuilder builder,
                            String raClz,
                            Collection<org.ironjacamar.common.api.metadata.spec.ConfigProperty> configProperties,
                            Map<String, String> overrides,
                            TransactionSupportEnum transactionSupport,
                            String productName, String productVersion,
                            InboundResourceAdapter ira, CloneableBootstrapContext bootstrapContext)
      throws DeployException
   {
      try
      {
         Class<?> clz = Class.forName(raClz, true, builder.getClassLoader());
         javax.resource.spi.ResourceAdapter resourceAdapter =
            (javax.resource.spi.ResourceAdapter)clz.newInstance();

         validationObj.add(new ValidateClass(Key.RESOURCE_ADAPTER, clz, configProperties));

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
         bootstrapContext.setResourceAdapter(resourceAdapter);
         builder.resourceAdapter(new ResourceAdapterImpl(resourceAdapter, bootstrapContext, dcps,
               statisticsPlugin, productName, productVersion,
               createInboundMapping(ira, builder.getClassLoader()),
               is16(builder.getMetadata()), beanValidation,
               builder.getActivation().getBeanValidationGroups(),
               ti));
      }
      catch (Throwable t)
      {
         throw new DeployException(bundle.unableToCreateResourceAdapter(raClz), t);
      }
   }

   /**
    * Create connection definition instance
    * @param builder The deployment builder
    * @param connector The metadata
    * @param cd The connection definition
    * @param transactionSupport The transaction support level
    * @throws DeployException Thrown if the connection definition cant be created
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
         TransactionSupportEnum tse = transactionSupport;
         String mcfClass = findManagedConnectionFactory(cd.getClassName(), connector);
         Class<?> clz = Class.forName(mcfClass, true, builder.getClassLoader());
         javax.resource.spi.ManagedConnectionFactory mcf =
            (javax.resource.spi.ManagedConnectionFactory)clz.newInstance();

         Collection<org.ironjacamar.common.api.metadata.spec.ConfigProperty> configProperties =
               findConfigProperties(mcfClass, connector);
         validationObj.add(new ValidateClass(Key.MANAGED_CONNECTION_FACTORY, clz, configProperties));

         Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty> dcps =
            injectConfigProperties(mcf, configProperties, cd.getConfigProperties(),
                                   builder.getClassLoader());

         if (mcf instanceof TransactionSupport)
         {
            TransactionSupport.TransactionSupportLevel tsl = ((TransactionSupport) mcf).getTransactionSupport();
            if (tsl == TransactionSupport.TransactionSupportLevel.NoTransaction)
            {
               tse = TransactionSupportEnum.NoTransaction;
            }
            else if (tsl == TransactionSupport.TransactionSupportLevel.LocalTransaction)
            {
               tse = TransactionSupportEnum.LocalTransaction;
            }
            else
            {
               tse = TransactionSupportEnum.XATransaction;
            }

            if (tse != transactionSupport)
               log.changedTransactionSupport(cd.getJndiName());
         }

         ConnectionManagerConfiguration cmc = new ConnectionManagerConfiguration();
         applyConnectionManagerConfiguration(cmc, cd);
         applyConnectionManagerConfiguration(cmc, cd.getSecurity());
         applyConnectionManagerConfiguration(cmc, cd.getTimeout());
         if (isXA(tse))
            applyConnectionManagerConfiguration(cmc, (org.ironjacamar.common.api.metadata.common.XaPool)cd.getPool());
         
         ConnectionManager cm =
            ConnectionManagerFactory.createConnectionManager(tse, mcf,
                                                             cd.isUseCcm() ? cachedConnectionManager : null,
                                                             cmc,
                                                             transactionIntegration);
         if (subjectFactory != null)
            cm.setSubjectFactory(subjectFactory);

         String poolType = cd.getPool() != null ? cd.getPool().getType() : null;
         String janitorType = cd.getPool() != null ? cd.getPool().getJanitor() : null;

         if (poolType == null || poolType.equals(""))
            poolType = defaultPoolType;

         PoolConfiguration pc = new PoolConfiguration();
         pc.setId(cd.getId() != null ? cd.getId() : cd.getJndiName());
         applyPoolConfiguration(pc, cd.getPool());
         applyPoolConfiguration(pc, cd.getTimeout());
         applyPoolConfiguration(pc, cd.getValidation());

         org.ironjacamar.core.connectionmanager.pool.Pool pool = PoolFactory.createPool(poolType, cm, pc);
         cm.setPool(pool);

         Capacity capacity = CapacityFactory
               .create(cd.getPool() != null ? cd.getPool().getCapacity() : null, this.classLoaderPlugin);
         pool.setCapacity(capacity);
         org.ironjacamar.core.api.deploymentrepository.Pool dpool = new PoolImpl(pool, null,
               capacity.getIncrementer(), capacity.getDecrementer());

         org.ironjacamar.core.connectionmanager.pool.Janitor janitor = JanitorFactory.createJanitor(janitorType);
         janitor.setPool(pool);
         pool.setJanitor(janitor);

         org.ironjacamar.core.spi.statistics.StatisticsPlugin statisticsPlugin = null;
         if (mcf instanceof org.ironjacamar.core.spi.statistics.Statistics)
            statisticsPlugin = ((org.ironjacamar.core.spi.statistics.Statistics)mcf).getStatistics();
         
         org.ironjacamar.core.api.deploymentrepository.Recovery recovery = null;
         if (isXA(tse))
         {
            recovery = createRecovery(mcf, cd);
         }

         if (builder.getResourceAdapter() != null)
            associateResourceAdapter(builder.getResourceAdapter().getResourceAdapter(), mcf);

         // Create ConnectionFactory
         Object cf = mcf.createConnectionFactory(cm);

         validationObj.add(new ValidateClass(Key.CONNECTION_FACTORY, cf.getClass()));

         builder.connectionFactory(new ConnectionFactoryImpl(cd.getJndiName(), cf, dcps, cd, cm, dpool,
                                                             statisticsPlugin, recovery, jndiStrategy));
      }
      catch (Throwable t)
      {
         throw new DeployException(bundle.unableToCreateConnectionDefinition(cd.getId(), cd.getJndiName()), t);
      }
   }

   /**
    * Create admin object instance
    * @param builder The deployment builder
    * @param connector The metadata
    * @param ao The admin object
    * @throws DeployException Thrown if the admin object cant be created
    */
   protected void createAdminObject(DeploymentBuilder builder, Connector connector, AdminObject ao)
      throws DeployException
   {
      try
      {
         String aoClass = findAdminObject(ao.getClassName(), connector);
         Class<?> clz = Class.forName(aoClass, true, builder.getClassLoader());

         Object adminObject = clz.newInstance();
         Collection<org.ironjacamar.common.api.metadata.spec.ConfigProperty> configProperties = findConfigProperties(
               aoClass, connector);

         Collection<org.ironjacamar.core.api.deploymentrepository.ConfigProperty> dcps = injectConfigProperties(
               adminObject, configProperties, ao.getConfigProperties(), builder.getClassLoader());

         validationObj.add(new ValidateClass(Key.ADMIN_OBJECT, clz, configProperties));

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
         throw new DeployException(bundle.unableToCreateAdminObject(ao.getId(), ao.getJndiName()), t);
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
      if (configProperties != null && !configProperties.isEmpty())
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
            if (overrides != null)
            {
               if (overrides.containsKey(cp.getConfigPropertyName().getValue()))
               {
                  value = overrides.get(cp.getConfigPropertyName().getValue());
               }
               else
               {
                  String alternative = cp.getConfigPropertyName().getValue().substring(0, 1).toUpperCase();
                  if (cp.getConfigPropertyName().getValue().length() > 1)
                     alternative += cp.getConfigPropertyName().getValue().substring(1);

                  if (overrides.containsKey(alternative))
                  {
                     value = overrides.get(alternative);
                  }
                  else
                  {
                     log.tracef("%s: Override for %s not found", o.getClass().getName(),
                                cp.getConfigPropertyName().getValue());
                  }
               }
            }

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
                     throw new DeployException(bundle.unableToInject(o.getClass().getName(),
                           cp.getConfigPropertyName().getValue(),
                           value.toString()), t);
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
      if (resourceAdapter != null && object != null && object instanceof ResourceAdapterAssociation)
      {
         try
         {
            ResourceAdapterAssociation raa = (ResourceAdapterAssociation)object;
            raa.setResourceAdapter(resourceAdapter);
         }
         catch (Throwable t)
         {
            throw new DeployException(bundle.unableToAssociate(object.getClass().getName()), t);
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
      if (s != null && s.getSecurityDomain() != null)
      {
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
            validationObj.add(new ValidateClass(Key.ACTIVATION_SPEC, clz, as.getConfigProperties()));

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
      if (raXml != null && !XsdString.isNull(raXml.getResourceadapterVersion()))
         return raXml.getResourceadapterVersion().getValue();

      return "";
   }

   /**
    * Strip password
    * @param str The string
    * @return The result
    */
   private String stripPassword(String str)
   {
      if (str.indexOf("<password>") == -1)
         return str;

      Pattern pattern = Pattern.compile("<password>[^<]*</password>");
      String[] strs = pattern.split(str);

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < strs.length; i++)
      {
         String s = strs[i];
         sb.append(s);
         if (i < strs.length - 1)
            sb.append("<password>****</password>");
      }

      return sb.toString();
   }

   /**
    * Is a 1.6+ deployment
    * @param connector The metadata
    * @return True if 1.6+, otherwise false
    */
   private boolean is16(Connector connector)
   {
      if (connector == null ||
          connector.getVersion() == Connector.Version.V_16 ||
          connector.getVersion() == Connector.Version.V_17 ||
          connector.getVersion() == Connector.Version.V_20)
         return true;

      return false;
   }

   /**
    * Verify deployment against bean validation
    * @param deployment The deployment
    * @exception DeployException Thrown in case of a violation
    */
   @SuppressWarnings("unchecked")
   private void verifyBeanValidation(Deployment deployment) throws DeployException
   {
      if (beanValidation != null)
      {
         ValidatorFactory vf = null;

         try
         {
            vf = beanValidation.getValidatorFactory();
            javax.validation.Validator v = vf.getValidator();

            Collection<String> l = deployment.getActivation().getBeanValidationGroups();
            if (l == null || l.isEmpty())
               l = Arrays.asList(javax.validation.groups.Default.class.getName());

            Collection<Class<?>> groups = new ArrayList<>();
            for (String clz : l)
            {
               try
               {
                  groups.add(Class.forName(clz, true, deployment.getClassLoader()));
               }
               catch (ClassNotFoundException e)
               {
                  throw new DeployException(bundle.unableToLoadBeanValidationGroup(clz, deployment.getIdentifier()),
                        e);
               }
            }
         
            Set failures = new HashSet();

            if (deployment.getResourceAdapter() != null)
            {

               Set f = v.validate(deployment.getResourceAdapter().getResourceAdapter(),
                     groups.toArray(new Class<?>[groups.size()]));
               if (!f.isEmpty())
                  failures.addAll(f);

            }
            if (deployment.getConnectionFactories() != null)
            {
               for (org.ironjacamar.core.api.deploymentrepository.ConnectionFactory cf :
                       deployment.getConnectionFactories())
               {

                  Set f = v.validate(cf.getConnectionFactory(), groups.toArray(new Class<?>[groups.size()]));
                  if (!f.isEmpty())
                     failures.addAll(f);

               }
            }
            if (deployment.getAdminObjects() != null)
            {
               for (org.ironjacamar.core.api.deploymentrepository.AdminObject ao :
                       deployment.getAdminObjects())
               {

                  Set f = v.validate(ao.getAdminObject(), groups.toArray(new Class<?>[groups.size()]));
                  if (!f.isEmpty())
                     failures.addAll(f);

               }
            }
         
            if (!failures.isEmpty())
            {
               throw new DeployException(bundle.violationOfValidationRule(deployment.getIdentifier()),
                     new ConstraintViolationException(failures));
            }
         }
         finally
         {
            if (vf != null)
               vf.close();
         }
      }
   }

   /**
    * Load native libraries
    * @param root The deployment root
    */
   private void loadNativeLibraries(File root)
   {
      if (root != null && root.exists())
      {
         List<String> libs = new ArrayList<String>();

         if (root.isDirectory())
         {
            if (root.listFiles() != null)
            {
               for (File f : root.listFiles())
               {
                  if (f.isFile())
                  {
                     String fileName = f.getName().toLowerCase(Locale.US);
                     if (fileName.endsWith(".a") || fileName.endsWith(".so") || fileName.endsWith(".dll"))
                     {
                        libs.add(f.getAbsolutePath());
                     }
                  }
               }
            }
            else
            {
               log.debugf("Root is a directory, but there were an I/O error: %s", root.getAbsolutePath());
            }
         }

         if (libs.size() > 0)
         {
            for (String lib : libs)
            {
               try
               {
                  SecurityActions.load(lib);
                  log.debugf("Loaded library: %s", lib);
               }
               catch (Throwable t)
               {
                  log.debugf("Unable to load library: %s", lib);
               }
            }
         }
         else
         {
            log.debugf("No native libraries for %s", root.getAbsolutePath());
         }
      }
   }

   /**
    * validate archive
    *
    * @param archiveValidationObjs archiveValidation archiveValidation classes and/or to validate.
    * @param failures failures failures original list of failures
    * @return The list of failures gotten with all new failures added. Null in case of no failures
    * or if validation is not run according to archiveValidation Setting. It returns null also if
    * the concrete implementation of this class set validateClasses instance variable to flase and the list of
    * archiveValidation contains one or more instance of {@link ValidateClass} type
    */
   public Set<Failure> validateArchive(List<Validate> archiveValidationObjs, Set<Failure> failures)
   {
      // Archive validation
      if (!archiveValidation)
      {
         return null;
      }

      for (Validate validate : archiveValidationObjs)
      {
         if (!(validate instanceof Validate))
            return null;
      }

      org.ironjacamar.validator.Validator validator = new org.ironjacamar.validator.Validator();
      List<Failure> partialFailures = validator.validate(archiveValidationObjs);

      if (partialFailures != null)
      {
         if (failures == null)
         {
            failures = new HashSet<>();
         }
         failures.addAll(partialFailures);
      }

      return failures;
   }

   /**
    * Check for failures at a certain level
    * @param failures failures failures The failures
    * @param severity severity severity The level
    * @return True if a failure is found with the specified severity; otherwise false
    */
   protected boolean hasFailuresLevel(Collection<Failure> failures, int severity)
   {
      if (failures != null)
      {
         for (Failure failure : failures)
         {
            if (failure.getSeverity() == severity)
            {
               return true;
            }
         }
      }
      return false;
   }


   /**
    * print Failures into Log files.
    *
    * @param validator validator validator validator instance used to run validation rules
    * @param failures failures failures the list of Failures to be printed
    * @param fhInput fhInput fhInput optional parameter. Normally used only for test or in case of
    *   FailureHelper already present in context
    * @return the error Text
    *
    */
   public String printFailuresLog(Validator validator, Collection<Failure> failures, FailureHelper... fhInput)
   {
      String errorText = "";
      FailureHelper fh = null;
      if (fhInput.length == 0)
         fh = new FailureHelper(failures);
      else
         fh = fhInput[0];

      if (failures != null && failures.size() > 0)
      {
         errorText = fh.asText(validator.getResourceBundle());
      }
      return errorText;
   }

   /**
    * Get the logger
    * @return The value
    */
   protected abstract DeployersLogger getLogger();

   /**
    *
    * get The directory where write error reports
    *
    * @return the directory as {@link File}
    */
   protected File getReportDirectory()
   {
      return new File(SecurityActions.getSystemProperty("ironjacamar.home"), "/log/");
   }

}
