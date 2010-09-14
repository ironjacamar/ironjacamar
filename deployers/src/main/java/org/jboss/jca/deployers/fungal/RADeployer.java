/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.jca.deployers.fungal;

import org.jboss.jca.common.annotations.Annotations;
import org.jboss.jca.common.annotations.repository.papaki.AnnotationScannerFactory;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.metadata.MetadataFactory;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScanner;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.Key;
import org.jboss.jca.validator.Severity;
import org.jboss.jca.validator.Validate;
import org.jboss.jca.validator.ValidateClass;
import org.jboss.jca.validator.ValidateObject;
import org.jboss.jca.validator.Validator;
import org.jboss.jca.validator.ValidatorException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.resource.Referenceable;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.jboss.logging.Logger;

import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.api.util.FileUtil;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.DeployerOrder;
import com.github.fungal.spi.deployers.Deployment;
import com.github.fungal.spi.deployers.MultiStageDeployer;

/**
 * The RA deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 */
public final class RADeployer extends AbstractResourceAdapterDeployer implements Deployer,
                                                                                 MultiStageDeployer,
                                                                                 DeployerOrder
{
   private static Logger log = Logger.getLogger(RADeployer.class);

   private static boolean trace = log.isTraceEnabled();

   /**
    * Constructor
    */
   public RADeployer()
   {
   }

   /**
    * Deployer order
    * @return The deployment
    */
   @Override
   public int getOrder()
   {
      return Integer.MIN_VALUE;
   }

   /**
    * Deploy
    * @param url The url
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   @SuppressWarnings("rawtypes")
   @Override
   public synchronized Deployment deploy(URL url, ClassLoader parent) throws DeployException
   {
      if (url == null || !(url.toExternalForm().endsWith(".rar") || url.toExternalForm().endsWith(".rar/")))
         return null;

      Set<Failure> failures = null;

      log.debug("Deploying: " + url.toExternalForm());

      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      try
      {
         File f = new File(url.toURI());

         if (!f.exists())
            throw new IOException("Archive " + url.toExternalForm() + " doesnt exists");

         File root = null;
         File destination = null;

         if (f.isFile())
         {
            FileUtil fileUtil = new FileUtil();
            destination = new File(SecurityActions.getSystemProperty("iron.jacamar.home"), "/tmp/");
            root = fileUtil.extract(f, destination);
         }
         else
         {
            root = f;
         }

         // Create classloader
         URL[] urls = getUrls(root);
         KernelClassLoader cl = null;
         if (getConfiguration().getScopeDeployment())
         {
            cl = ClassLoaderFactory.create(ClassLoaderFactory.TYPE_PARENT_LAST, urls, parent);
         }
         else
         {
            cl = ClassLoaderFactory.create(ClassLoaderFactory.TYPE_PARENT_FIRST, urls, parent);
         }
         SecurityActions.setThreadContextClassLoader(cl);

         // Parse metadata
         MetadataFactory metadataFactory = new MetadataFactory();
         Connector cmd = metadataFactory.getStandardMetaData(root);
         IronJacamar ijmd = metadataFactory.getIronJacamarMetaData(root);

         // Annotation scanning
         Annotations annotator = new Annotations();
         AnnotationScanner scanner = (new AnnotationScannerFactory()).createScanner();
         AnnotationRepository repository = scanner.scan(cl.getURLs(), cl);
         cmd = annotator.merge(cmd, repository);

         // Validate metadata
         cmd.validate();

         // Merge metadata
         //TODO: merge ironjacamar with connector properties. Select the right list of properties
         // and use MetadataFavtory.mergeConfigProperties(Map<String, String>, List<? extends ConfigProperty> )

         // Notify regarding license terms
         if (cmd != null && cmd.getLicense() != null && cmd.getLicense().isLicenseRequired())
            log.info("Required license terms for " + url.toExternalForm());

         ResourceAdapter resourceAdapter = null;
         List<Validate> archiveValidationObjects = new ArrayList<Validate>();
         List<Failure> partialFailures = null;
         List<Object> beanValidationObjects = new ArrayList<Object>();
         Object[] cfs = null;

         String deploymentName = f.getName().substring(0, f.getName().indexOf(".rar"));

         // Check metadata for JNDI information and activate explicit
         boolean activateDeployment = false;

         // Create objects and inject values
         if (cmd != null)
         {
            // ResourceAdapter
            if (cmd.getVersion() != Version.V_10)
            {
               ResourceAdapter1516 ra1516 = (ResourceAdapter1516)cmd.getResourceadapter();
               if (ra1516 != null && ra1516.getResourceadapterClass() != null)
               {
                  partialFailures =
                        validateArchive(url, Arrays.asList((Validate) new ValidateClass(Key.RESOURCE_ADAPTER,
                           ra1516.getResourceadapterClass(), cl, cmd.getResourceadapter().getConfigProperties())));

                  if (partialFailures != null)
                  {
                     failures = new HashSet<Failure>();
                     failures.addAll(partialFailures);
                  }

                  if (!(getConfiguration().getArchiveValidationFailOnError() &&
                        hasFailuresLevel(failures, Severity.ERROR)))
                  {
                     if (activateDeployment)
                     {
                        resourceAdapter =
                           (ResourceAdapter)initAndInject(ra1516.getResourceadapterClass(),
                                                          ra1516.getConfigProperties(), cl);

                        if (trace)
                        {
                           log.trace("ResourceAdapter: " + resourceAdapter.getClass().getName());
                           log.trace("ResourceAdapter defined in classloader: " +
                                     resourceAdapter.getClass().getClassLoader());
                        }

                        archiveValidationObjects.add(new ValidateObject(Key.RESOURCE_ADAPTER,
                                                                        resourceAdapter,
                                                                        ra1516.getConfigProperties()));
                        beanValidationObjects.add(resourceAdapter);
                     }
                  }
               }
            }

            // ManagedConnectionFactory
            if (cmd.getVersion() == Version.V_10)
            {
               ResourceAdapter10 ra10 = (ResourceAdapter10)cmd.getResourceadapter();

               if (activateDeployment)
               {
                  ManagedConnectionFactory mcf =
                     (ManagedConnectionFactory) initAndInject(ra10.getManagedConnectionFactoryClass().getValue(),
                                                              ra10.getConfigProperties(), cl);

                  if (trace)
                  {
                     log.trace("ManagedConnectionFactory: " + mcf.getClass().getName());
                     log.trace("ManagedConnectionFactory defined in classloader: " +
                               mcf.getClass().getClassLoader());
                  }

                  mcf.setLogWriter(new PrintWriter(getConfiguration().getPrintStream()));

                  archiveValidationObjects.add(new ValidateObject(Key.MANAGED_CONNECTION_FACTORY,
                                                                  mcf, ra10.getConfigProperties()));
                  beanValidationObjects.add(mcf);
                  associateResourceAdapter(resourceAdapter, mcf);

                  // Create the pool
                  PoolConfiguration pc = new PoolConfiguration();
                  PoolFactory pf = new PoolFactory();

                  Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true);

                  // Add a connection manager
                  ConnectionManagerFactory cmf = new ConnectionManagerFactory();
                  ConnectionManager cm = null;

                  TransactionSupportLevel tsl = TransactionSupportLevel.NoTransaction;
                  TransactionSupportEnum tsmd = TransactionSupportEnum.NoTransaction;

                  tsmd = ((ResourceAdapter10) cmd.getResourceadapter()).getTransactionSupport();

                  if (tsmd == TransactionSupportEnum.NoTransaction)
                  {
                     tsl = TransactionSupportLevel.NoTransaction;
                  }
                  else if (tsmd == TransactionSupportEnum.LocalTransaction)
                  {
                     tsl = TransactionSupportLevel.LocalTransaction;
                  }
                  else if (tsmd == TransactionSupportEnum.XATransaction)
                  {
                     tsl = TransactionSupportLevel.XATransaction;
                  }
                  
                  // Connection manager properties
                  Long allocationRetry = null; // TODO
                  Long allocationRetryWaitMillis = null;
               
                  if (ijmd != null)
                  {
                     /*
                       TODO
                       allocationRetry = ijmd.getTimeOut().getAllocationRetry();
                       allocationRetryWaitMillis = ijmd.getTimeOut().getAllocationRetryWaitMillis();
                     */
                  }

                  // Select the correct connection manager
                  if (tsl == TransactionSupportLevel.NoTransaction)
                  {
                     cm = cmf.createNonTransactional(tsl, 
                                                     pool,
                                                     allocationRetry,
                                                     allocationRetryWaitMillis);
                  }
                  else
                  {
                     cm = cmf.createTransactional(tsl, 
                                                  pool,
                                                  allocationRetry,
                                                  allocationRetryWaitMillis,
                                                  getConfiguration().getTransactionManager());
                  }

                  // ConnectionFactory
                  Object cf = mcf.createConnectionFactory(cm);

                  if (cf == null)
                  {
                     log.error("ConnectionFactory is null");
                  }
                  else
                  {
                     if (trace)
                     {
                        log.trace("ConnectionFactory: " + cf.getClass().getName());
                        log.trace("ConnectionFactory defined in classloader: "
                                  + cf.getClass().getClassLoader());
                     }
                  }

                  archiveValidationObjects.add(new ValidateObject(Key.CONNECTION_FACTORY, cf));

                  if (cf != null && cf instanceof Serializable && cf instanceof Referenceable)
                  {
                     String[] jndis = bindConnectionFactory(url, deploymentName, cf);
                     cfs = new Object[] {cf};
                     
                     cm.setJndiName(jndis[0]);
                  }
               }
            }
            else
            {
               ResourceAdapter1516 ra = (ResourceAdapter1516) cmd.getResourceadapter();
               if (ra != null && ra.getOutboundResourceadapter() != null &&
                   ra.getOutboundResourceadapter().getConnectionDefinitions() != null)
               {
                  List<ConnectionDefinition> cdMetas = ra.getOutboundResourceadapter().getConnectionDefinitions();
                  if (cdMetas.size() > 0)
                  {
                     for (ConnectionDefinition cdMeta : cdMetas)
                     {
                        partialFailures =
                              validateArchive(url, Arrays
                                    .asList((Validate) new ValidateClass(Key.MANAGED_CONNECTION_FACTORY, cdMeta
                                          .getManagedConnectionFactoryClass().getValue(), cl, cdMeta
                                          .getConfigProperties())));
                        if (partialFailures != null)
                        {
                           failures = new HashSet<Failure>();
                           failures.addAll(partialFailures);
                        }

                        if (!(getConfiguration().getArchiveValidationFailOnError() &&
                              hasFailuresLevel(failures, Severity.ERROR)))
                        {
                           if (activateDeployment)
                           {
                              ManagedConnectionFactory mcf =
                                 (ManagedConnectionFactory) initAndInject(cdMeta.getManagedConnectionFactoryClass()
                                                                          .getValue(), cdMeta
                                                                          .getConfigProperties(), cl);

                              if (trace)
                              {
                                 log.trace("ManagedConnectionFactory: " + mcf.getClass().getName());
                                 log.trace("ManagedConnectionFactory defined in classloader: " +
                                           mcf.getClass().getClassLoader());
                              }

                              mcf.setLogWriter(new PrintWriter(getConfiguration().getPrintStream()));

                              archiveValidationObjects.add(new ValidateObject(Key.MANAGED_CONNECTION_FACTORY,
                                                                              mcf,
                                                                              cdMeta.getConfigProperties()));
                              beanValidationObjects.add(mcf);
                              associateResourceAdapter(resourceAdapter, mcf);

                              // Create the pool
                              PoolConfiguration pc = new PoolConfiguration();
                              PoolFactory pf = new PoolFactory();

                              Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true);

                              // Add a connection manager
                              ConnectionManagerFactory cmf = new ConnectionManagerFactory();
                              ConnectionManager cm = null;
                              TransactionSupportLevel tsl = TransactionSupportLevel.NoTransaction;
                              TransactionSupportEnum tsmd = TransactionSupportEnum.NoTransaction;

                              tsmd = ra.getOutboundResourceadapter().getTransactionSupport();

                              if (tsmd == TransactionSupportEnum.NoTransaction)
                              {
                                 tsl = TransactionSupportLevel.NoTransaction;
                              }
                              else if (tsmd == TransactionSupportEnum.LocalTransaction)
                              {
                                 tsl = TransactionSupportLevel.LocalTransaction;
                              }
                              else if (tsmd == TransactionSupportEnum.XATransaction)
                              {
                                 tsl = TransactionSupportLevel.XATransaction;
                              }

                              // Section 7.13 -- Read from metadata -> overwrite with specified value if present
                              if (mcf instanceof TransactionSupport)
                                 tsl = ((TransactionSupport) mcf).getTransactionSupport();

                              // Connection manager properties
                              Long allocationRetry = null; // TODO
                              Long allocationRetryWaitMillis = null;
                              
                              if (ijmd != null)
                              {
                                 /*
                                   TODO
                                   allocationRetry = ijmd.getTimeOut().getAllocationRetry();
                                   allocationRetryWaitMillis = ijmd.getTimeOut().getAllocationRetryWaitMillis();
                                 */
                              }

                              // Select the correct connection manager
                              if (tsl == TransactionSupportLevel.NoTransaction)
                              {
                                 cm = cmf.createNonTransactional(tsl,
                                                                 pool,
                                                                 allocationRetry,
                                                                 allocationRetryWaitMillis);
                              }
                              else
                              {
                                 cm = cmf.createTransactional(tsl, 
                                                              pool,
                                                              allocationRetry,
                                                              allocationRetryWaitMillis,
                                                              getConfiguration().getTransactionManager());
                              }

                              // ConnectionFactory
                              Object cf = mcf.createConnectionFactory(cm);

                              if (cf == null)
                              {
                                 log.error("ConnectionFactory is null");
                              }
                              else
                              {
                                 if (trace)
                                 {
                                    log.trace("ConnectionFactory: " + cf.getClass().getName());
                                    log.trace("ConnectionFactory defined in classloader: "
                                              + cf.getClass().getClassLoader());
                                 }
                              }

                              archiveValidationObjects.add(new ValidateObject(Key.CONNECTION_FACTORY, cf));

                              if (cf != null && cf instanceof Serializable && cf instanceof Referenceable)
                              {
                                 if (cdMetas.size() == 1)
                                 {
                                    deploymentName = f.getName().substring(0, f.getName().indexOf(".rar"));
                                    String[] jndis = bindConnectionFactory(url, deploymentName, cf);
                                    cfs = new Object[] {cf};

                                    cm.setJndiName(jndis[0]);
                                 }
                                 else
                                 {
                                    log.warn("NYI: There are multiple connection factories for: " + f.getName());
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            // ActivationSpec
            if (cmd.getVersion() != Version.V_10)
            {
               ResourceAdapter1516 ra1516 = (ResourceAdapter1516)cmd.getResourceadapter();
               if (ra1516 != null &&
                   ra1516.getInboundResourceadapter() != null &&
                   ra1516.getInboundResourceadapter().getMessageadapter() != null &&
                   ra1516.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null)
               {
                  List<MessageListener> mlMetas = ra1516.getInboundResourceadapter().getMessageadapter().
                     getMessagelisteners();

                  if (mlMetas.size() > 0)
                  {
                     for (MessageListener mlMeta : mlMetas)
                     {
                        if (mlMeta.getActivationspec() != null &&
                            mlMeta.getActivationspec().getActivationspecClass().getValue() != null)
                        {
                           partialFailures =
                              validateArchive(url, Arrays.asList((Validate) new ValidateClass(Key.ACTIVATION_SPEC,
                                 mlMeta.getActivationspec().getActivationspecClass().getValue(), cl,
                                 mlMeta.getActivationspec().getConfigProperties())));

                           if (partialFailures != null)
                           {
                              failures = new HashSet<Failure>();
                              failures.addAll(partialFailures);
                           }

                           if (!(getConfiguration().getArchiveValidationFailOnError() &&
                                 hasFailuresLevel(failures, Severity.ERROR)))
                           {
                              if (activateDeployment)
                              {
                                 List<? extends ConfigProperty> cpm = mlMeta.getActivationspec().getConfigProperties();

                                 Object o = initAndInject(mlMeta
                                    .getActivationspec().getActivationspecClass().getValue(), cpm, cl);

                                 if (trace)
                                 {
                                    log.trace("ActivationSpec: " + o.getClass().getName());
                                    log.trace("ActivationSpec defined in classloader: " +
                                              o.getClass().getClassLoader());
                                 }

                                 archiveValidationObjects.add(new ValidateObject(Key.ACTIVATION_SPEC, o, cpm));
                                 beanValidationObjects.add(o);
                                 associateResourceAdapter(resourceAdapter, o);
                              }
                           }
                        }
                     }
                  }
               }
            }

            // AdminObject
            if (cmd.getVersion() != Version.V_10)
            {
               ResourceAdapter1516 ra1516 = (ResourceAdapter1516)cmd.getResourceadapter();
               if (ra1516 != null && ra1516.getAdminObjects() != null)
               {
                  List<AdminObject> aoMetas = ((ResourceAdapter1516) cmd.getResourceadapter()).getAdminObjects();
                  if (aoMetas.size() > 0)
                  {
                     for (AdminObject aoMeta : aoMetas)
                     {
                        if (aoMeta.getAdminobjectClass() != null &&
                            aoMeta.getAdminobjectClass().getValue() != null)
                        {
                           partialFailures =
                              validateArchive(url, Arrays.asList((Validate) new ValidateClass(Key.ADMIN_OBJECT,
                                 aoMeta.getAdminobjectClass().getValue(), cl, aoMeta.getConfigProperties())));

                           if (partialFailures != null)
                           {
                              failures = new HashSet<Failure>();
                              failures.addAll(partialFailures);
                           }

                           if (!(getConfiguration().getArchiveValidationFailOnError() &&
                                 hasFailuresLevel(failures, Severity.ERROR)))
                           {
                              if (activateDeployment)
                              {
                                 Object o =
                                    initAndInject(aoMeta.getAdminobjectClass().getValue(), aoMeta.getConfigProperties(),
                                                  cl);

                                 if (trace)
                                 {
                                    log.trace("AdminObject: " + o.getClass().getName());
                                    log.trace("AdminObject defined in classloader: " + o.getClass().getClassLoader());
                                 }

                                 archiveValidationObjects
                                    .add(new ValidateObject(Key.ADMIN_OBJECT, o, aoMeta.getConfigProperties()));
                                 beanValidationObjects.add(o);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         // Archive validation
         partialFailures = validateArchive(url, archiveValidationObjects);

         if (partialFailures != null)
         {
            if (failures == null)
            {
               failures = new HashSet<Failure>();
            }
            failures.addAll(partialFailures);
         }

         if ((getConfiguration().getArchiveValidationFailOnWarn() && hasFailuresLevel(failures, Severity.WARNING)) ||
             (getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
         {
            throw new ValidatorException(printFailuresLog(url.getPath(), new Validator(), failures, null), failures);
         }
         else
         {
            printFailuresLog(url.getPath(), new Validator(), failures, null);
         }

         if (cmd != null)
         {
            // Register with MDR
            getConfiguration().getMetadataRepository().registerResourceAdapter(url, root, cmd, ijmd);
         }

         if (activateDeployment)
         {
            // Bean validation
            if (getConfiguration().getBeanValidation())
            {
               List<Class> groupsClasses = null;

               if (ijmd != null && ijmd.getBeanValidationGroups() != null &&
                   ijmd.getBeanValidationGroups().size() > 0)
               {
                  groupsClasses = new ArrayList<Class>();
                  for (String group : ijmd.getBeanValidationGroups())
                  {
                     groupsClasses.add(Class.forName(group, true, cl));
                  }
               }

               if (beanValidationObjects.size() > 0)
               {
                  BeanValidation beanValidator = new BeanValidation();
                  for (Object o : beanValidationObjects)
                  {
                     beanValidator.validate(o, groupsClasses);
                  }
               }
            }

            // Activate deployment
            if (resourceAdapter != null)
            {
               String bootstrapIdentifier = null;

               if (ijmd != null)
               {
                  bootstrapIdentifier = ijmd.getBootstrapContext();
               }

               startContext(resourceAdapter, bootstrapIdentifier);
            }
         }

         if (activateDeployment)
         {
            log.info("Deployed: " + url.toExternalForm());
         }
         else
         {
            log.debug("Activated: " + url.toExternalForm());
         }

         return new RADeployment(url,
                                 deploymentName,
                                 activateDeployment,
                                 resourceAdapter,
                                 getConfiguration().getJndiStrategy(),
                                 getConfiguration().getMetadataRepository(),
                                 cfs,
                                 destination,
                                 cl,
                                 log);

      }
      catch (DeployException de)
      {
         // Just rethrow
         throw de;
      }
      catch (Throwable t)
      {
         if ((getConfiguration().getArchiveValidationFailOnWarn() && hasFailuresLevel(failures, Severity.WARNING)) ||
             (getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
         {
            throw new DeployException("Deployment " + url.toExternalForm() + " failed",
               new ValidatorException(printFailuresLog(url.getPath(), new Validator(), failures, null), failures));
         }
         else
         {
            printFailuresLog(url.getPath(), new Validator(), failures, null);
            throw new DeployException("Deployment " + url.toExternalForm() + " failed", t);
         }
      }
      finally
      {
         SecurityActions.setThreadContextClassLoader(oldTCCL);
      }
   }
}
