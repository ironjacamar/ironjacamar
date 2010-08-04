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
import org.jboss.jca.common.api.metadata.jbossra.JbossRa;
import org.jboss.jca.common.api.metadata.jbossra.jbossra20.BeanValidationGroup;
import org.jboss.jca.common.api.metadata.jbossra.jbossra20.JbossRa20;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.metadata.MetadataFactory;
import org.jboss.jca.core.api.CloneableBootstrapContext;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.naming.NoopJndiStrategy;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.FailureHelper;
import org.jboss.jca.validator.Key;
import org.jboss.jca.validator.Severity;
import org.jboss.jca.validator.Validate;
import org.jboss.jca.validator.ValidateClass;
import org.jboss.jca.validator.ValidateObject;
import org.jboss.jca.validator.Validator;
import org.jboss.jca.validator.ValidatorException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.Referenceable;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;

import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.api.util.FileUtil;
import com.github.fungal.api.util.Injection;
import com.github.fungal.api.util.JarFilter;
import com.github.fungal.spi.deployers.CloneableDeployer;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * The RA deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 */
public final class RADeployer implements CloneableDeployer
{
   private static Logger log = Logger.getLogger(RADeployer.class);

   private static boolean trace = log.isTraceEnabled();

   /** The transaction manager */
   private static TransactionManager transactionManager = null;

   /** Preform bean validation */
   private static AtomicBoolean beanValidation = new AtomicBoolean(true);

   /** Preform archive validation */
   private static AtomicBoolean archiveValidation = new AtomicBoolean(true);

   /** Archive validation: Fail on Warn */
   private static AtomicBoolean archiveValidationFailOnWarn = new AtomicBoolean(false);

   /** Archive validation: Fail on Error */
   private static AtomicBoolean archiveValidationFailOnError = new AtomicBoolean(true);

   /** Print stream */
   private static PrintStream printStream = null;

   /** Default bootstrap context */
   private static CloneableBootstrapContext defaultBootstrapContext = null;

   /** Bootstrap contexts */
   private static Map<String, CloneableBootstrapContext> bootstrapContexts = null;

   /** Scope deployment */
   private static AtomicBoolean scopeDeployment = new AtomicBoolean(false);

   /** JNDI strategy */
   private static JndiStrategy jndiStrategy = null;

   /**
    * Constructor
    */
   public RADeployer()
   {
   }

   /**
    * Set the transaction manager
    * @param value The value
    */
   public synchronized void setTransactionManager(TransactionManager value)
   {
      transactionManager = value;
   }

   /**
    * Get the transaction manager
    * @return The value
    */
   public synchronized TransactionManager getTransactionManager()
   {
      return transactionManager;
   }

   /**
    * Set if bean validation should be performed
    * @param value The value
    */
   public void setBeanValidation(boolean value)
   {
      beanValidation.set(value);
   }

   /**
    * Should bean validation be performed
    * @return True if validation; otherwise false
    */
   public boolean getBeanValidation()
   {
      return beanValidation.get();
   }

   /**
    * Set if archive validation should be performed
    * @param value The value
    */
   public void setArchiveValidation(boolean value)
   {
      archiveValidation.set(value);
   }

   /**
    * Should archive validation be performed
    * @return True if validation; otherwise false
    */
   public boolean getArchiveValidation()
   {
      return archiveValidation.get();
   }

   /**
    * Set if a failed warning archive validation report should fail the deployment
    * @param value The value
    */
   public void setArchiveValidationFailOnWarn(boolean value)
   {
      archiveValidationFailOnWarn.set(value);
   }

   /**
    * Does a failed archive validation warning report fail the deployment
    * @return True if failing; otherwise false
    */
   public boolean getArchiveValidationFailOnWarn()
   {
      return archiveValidationFailOnWarn.get();
   }

   /**
    * Set if a failed error archive validation report should fail the deployment
    * @param value The value
    */
   public void setArchiveValidationFailOnError(boolean value)
   {
      archiveValidationFailOnError.set(value);
   }

   /**
    * Does a failed archive validation error report fail the deployment
    * @return True if failing; otherwise false
    */
   public boolean getArchiveValidationFailOnError()
   {
      return archiveValidationFailOnError.get();
   }

   /**
    * Set the print stream
    * @param value The value
    */
   public synchronized void setPrintStream(PrintStream value)
   {
      printStream = value;
   }

   /**
    * Get the print stream
    * @return The handle
    */
   public synchronized PrintStream getPrintStream()
   {
      return printStream;
   }

   /**
    * Set the default bootstrap context
    * @param value The value
    */
   public synchronized void setDefaultBootstrapContext(CloneableBootstrapContext value)
   {
      defaultBootstrapContext = value;
   }

   /**
    * Get the default bootstrap context
    * @return The handle
    */
   public synchronized CloneableBootstrapContext getDefaultBootstrapContext()
   {
      return defaultBootstrapContext;
   }

   /**
    * Set the bootstrap context map
    * @param value The value
    */
   public synchronized void setBootstrapContexts(Map<String, CloneableBootstrapContext> value)
   {
      bootstrapContexts = value;
   }

   /**
    * Get the bootstrap context map
    * @return The handle
    */
   public synchronized Map<String, CloneableBootstrapContext> getBootstrapContexts()
   {
      return bootstrapContexts;
   }

   /**
    * Set if deployments should be scoped
    * @param value The value
    */
   public void setScopeDeployment(boolean value)
   {
      scopeDeployment.set(value);
   }

   /**
    * Are the deployments scoped
    * @return True if scoped; otherwise false
    */
   public boolean getScopeDeployment()
   {
      return scopeDeployment.get();
   }

   /**
    * Set the JNDI strategy
    * @param value The value
    */
   public synchronized void setJndiStrategy(JndiStrategy value)
   {
      jndiStrategy = value;
   }

   /**
    * Get the JNDI strategy
    * @return The handle
    */
   public synchronized JndiStrategy getJndiStrategy()
   {
      return jndiStrategy;
   }

   /**
    * Deploy
    * @param url The url
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   @Override
   public Deployment deploy(URL url, ClassLoader parent) throws DeployException
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
         if (scopeDeployment.get())
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
         JbossRa jrmd = metadataFactory.getJBossMetaData(root);

         // Annotation scanning
         Annotations annotator = new Annotations();
         cmd = annotator.scan(cmd, cl.getURLs(), cl);

         // Validate metadata
         cmd.validate();

         // Merge metadata
         cmd.merge(jrmd);

         // Notify regarding license terms
         if (cmd != null && cmd.getLicense() != null && cmd.getLicense().isLicenseRequired())
            log.info("Required license terms for " + url.toExternalForm());

         ResourceAdapter resourceAdapter = null;
         List<Validate> archiveValidationObjects = new ArrayList<Validate>();
         List<Failure> partialFailures = null;
         List<Object> beanValidationObjects = new ArrayList<Object>();

         String deploymentName = null;
         Object[] cfs = null;

         // Create objects and inject values
         if (cmd != null)
         {
            // ResourceAdapter
            if (cmd.getVersion() != Version.V_10)
            {
               if (cmd.getResourceadapter() != null && cmd.getResourceadapter().getClass() != null)
               {
                  partialFailures =
                        validateArchive(url, Arrays.asList((Validate) new ValidateClass(Key.RESOURCE_ADAPTER,
                              ((ResourceAdapter1516) cmd.getResourceadapter()).getResourceadapterClass(),
                                                                                     cl,
                                                                                     cmd.getResourceadapter()
                                                                                           .getConfigProperties())));
                  if (partialFailures != null)
                  {
                     failures = new HashSet<Failure>();
                     failures.addAll(partialFailures);
                  }

                  if (!(getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
                  {
                     resourceAdapter =
                           (ResourceAdapter) initAndInject(
                                 ((ResourceAdapter1516) cmd.getResourceadapter()).getResourceadapterClass(), cmd
                                       .getResourceadapter().getConfigProperties(),
                                 cl);

                     if (trace)
                     {
                        log.trace("ResourceAdapter: " + resourceAdapter.getClass().getName());
                        log.trace("ResourceAdapter defined in classloader: " +
                                  resourceAdapter.getClass().getClassLoader());
                     }

                     archiveValidationObjects.add(new ValidateObject(Key.RESOURCE_ADAPTER,
                                                                     resourceAdapter,
                                                                     cmd.getResourceadapter().getConfigProperties()));
                     beanValidationObjects.add(resourceAdapter);
                  }
               }
            }

            // ManagedConnectionFactory
            if (cmd.getVersion() == Version.V_10)
            {

               ManagedConnectionFactory mcf =
                     (ManagedConnectionFactory) initAndInject(((ResourceAdapter10) cmd.getResourceadapter())
                           .getManagedConnectionFactoryClass()
                           .getValue(), ((ResourceAdapter10) cmd.getResourceadapter())
                           .getConfigProperties(), cl);

               if (trace)
               {
                  log.trace("ManagedConnectionFactory: " + mcf.getClass().getName());
                  log.trace("ManagedConnectionFactory defined in classloader: " +
                         mcf.getClass().getClassLoader());
               }

               mcf.setLogWriter(new PrintWriter(printStream));

               archiveValidationObjects.add(new ValidateObject(Key.MANAGED_CONNECTION_FACTORY,
                                                            mcf,
                                                            ((ResourceAdapter10) cmd.getResourceadapter())
                                                                  .getConfigProperties()));
               beanValidationObjects.add(mcf);
               associateResourceAdapter(resourceAdapter, mcf);

               // Create the pool
               PoolConfiguration pc = new PoolConfiguration();
               PoolFactory pf = new PoolFactory();

               Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true);

               // Add a connection manager
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
               // Section 7.13 -- Read from metadata -> overwrite with specified value if present
               if (mcf instanceof TransactionSupport)
                  tsl = ((TransactionSupport) mcf).getTransactionSupport();

               // Select the correct connection manager
               ConnectionManagerFactory cmf = new ConnectionManagerFactory();
               cm = cmf.create(tsl, pool, transactionManager);

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
                  deploymentName = f.getName().substring(0, f.getName().indexOf(".rar"));
                  bindConnectionFactory(deploymentName, cf);
                  cfs = new Object[]
                  {cf};
               }

            }
            else
            {
               ResourceAdapter1516 ra = (ResourceAdapter1516) cmd.getResourceadapter();
               if (ra != null &&
                     ra.getOutboundResourceadapter() != null &&
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
                                          .getManagedconnectionfactoryClass().getValue(), cl, cdMeta
                                          .getConfigProperties())));
                        if (partialFailures != null)
                        {
                           failures = new HashSet<Failure>();
                           failures.addAll(partialFailures);
                        }

                        if (!(getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
                        {
                           ManagedConnectionFactory mcf =
                                 (ManagedConnectionFactory) initAndInject(cdMeta.getManagedconnectionfactoryClass()
                                       .getValue(), cdMeta
                                       .getConfigProperties(), cl);

                           if (trace)
                           {
                              log.trace("ManagedConnectionFactory: " + mcf.getClass().getName());
                              log.trace("ManagedConnectionFactory defined in classloader: " +
                                        mcf.getClass().getClassLoader());
                           }

                           mcf.setLogWriter(new PrintWriter(printStream));

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

                           // Select the correct connection manager
                           ConnectionManagerFactory cmf = new ConnectionManagerFactory();
                           cm = cmf.create(tsl, pool, transactionManager);

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
                                 bindConnectionFactory(deploymentName, cf);
                                 cfs = new Object[]
                                 {cf};
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

            // ActivationSpec
            if (cmd.getVersion() != Version.V_10
                  && cmd.getResourceadapter() != null
                  && ((ResourceAdapter1516) cmd.getResourceadapter()).getInboundResourceadapter() != null
                  && ((ResourceAdapter1516) cmd.getResourceadapter()).getInboundResourceadapter()
                        .getMessageadapter() != null
                  && ((ResourceAdapter1516) cmd.getResourceadapter()).getInboundResourceadapter().getMessageadapter()
                        .getMessagelisteners() != null)
            {
               List<MessageListener> mlMetas = ((ResourceAdapter1516) cmd.getResourceadapter())
                     .getInboundResourceadapter().getMessageadapter().getMessagelisteners();
               if (mlMetas.size() > 0)
               {
                  for (MessageListener mlMeta : mlMetas)
                  {
                     if (mlMeta.getActivationspec() != null
                           && mlMeta.getActivationspec().getActivationspecClass().getValue() != null)
                     {
                        partialFailures =
                              validateArchive(url, Arrays
                                    .asList((Validate) new ValidateClass(Key.ACTIVATION_SPEC, mlMeta
                                          .getActivationspec().getActivationspecClass().getValue(), cl, mlMeta
                                          .getActivationspec().getConfigProperties())));

                        if (partialFailures != null)
                        {
                           failures = new HashSet<Failure>();
                           failures.addAll(partialFailures);
                        }

                        if (!(getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
                        {
                           List<? extends ConfigProperty> cpm = mlMeta
                                 .getActivationspec().getConfigProperties();

                           Object o = initAndInject(mlMeta
                                 .getActivationspec().getActivationspecClass().getValue(), cpm, cl);

                           if (trace)
                           {
                              log.trace("ActivationSpec: " + o.getClass().getName());
                              log.trace("ActivationSpec defined in classloader: " + o.getClass().getClassLoader());
                           }

                           archiveValidationObjects.add(new ValidateObject(Key.ACTIVATION_SPEC, o, cpm));
                           beanValidationObjects.add(o);
                           associateResourceAdapter(resourceAdapter, o);
                        }
                     }
                  }
               }
            }

            // AdminObject
            if (cmd.getVersion() != Version.V_10 && cmd.getResourceadapter() != null &&
                  ((ResourceAdapter1516) cmd.getResourceadapter()).getAdminobjects() != null)
            {
               List<AdminObject> aoMetas = ((ResourceAdapter1516) cmd.getResourceadapter()).getAdminobjects();
               if (aoMetas.size() > 0)
               {
                  for (AdminObject aoMeta : aoMetas)
                  {
                     if (aoMeta.getAdminobjectClass() != null
                           && aoMeta.getAdminobjectClass().getValue() != null)
                     {
                        partialFailures =
                              validateArchive(url, Arrays
                                    .asList((Validate) new ValidateClass(Key.ADMIN_OBJECT,
                                          aoMeta.getAdminobjectClass().getValue(), cl, aoMeta.getConfigProperties())));

                        if (partialFailures != null)
                        {
                           failures = new HashSet<Failure>();
                           failures.addAll(partialFailures);
                        }

                        if (!(getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
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

         if ((getArchiveValidationFailOnWarn() && hasFailuresLevel(failures, Severity.WARNING)) ||
               (getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
         {
            throw new ValidatorException(printFailuresLog(url.getPath(), new Validator(), failures, null), failures);
         }
         else
         {
            printFailuresLog(url.getPath(), new Validator(), failures, null);
         }

         // Bean validation
         if (getBeanValidation())
         {
            JbossRa20 jrmd20 = null;
            List<Class> groupsClasses = null;
            if (jrmd instanceof JbossRa20)
            {
               jrmd20 = (JbossRa20) jrmd;
            }
            if (jrmd20 != null && jrmd20.getBeanValidationGroups() != null
                  && jrmd20.getBeanValidationGroups().size() > 0)
            {
               BeanValidationGroup bvGroups = jrmd20.getBeanValidationGroups().get(0);
               groupsClasses = new ArrayList<Class>();
               for (String group : bvGroups.getBeanValidationGroup())
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

            if (jrmd != null && jrmd instanceof JbossRa20)
            {
               JbossRa20 jrmd20 = (JbossRa20) jrmd;
               bootstrapIdentifier = jrmd20.getBootstrapContext();
            }

            startContext(resourceAdapter, bootstrapIdentifier);
         }

         log.info("Deployed: " + url.toExternalForm());
         RADeployment depoyment = new RADeployment(url, deploymentName, resourceAdapter, jndiStrategy, cfs,
               destination, cl);
         return depoyment;
      }
      catch (DeployException de)
      {
         // Just rethrow
         throw de;
      }
      catch (Throwable t)
      {
         if ((getArchiveValidationFailOnWarn() && hasFailuresLevel(failures, Severity.WARNING)) ||
               (getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
            throw new DeployException("Deployment " + url.toExternalForm() + " failed",
                  new ValidatorException(printFailuresLog(url.getPath(), new Validator(), failures, null), failures));
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

   /**
    * validate archive
    *
    * @param url of the archive
    * @param archiveValidation classes and/or to validate.
    * @return The list of failures gotten. Null in case of no failures or if validation is not run according to
    *   {@link #getArchiveValidation()} Settin
    */
   //IT IS PACKAGE PROTECTED ONLY FOR TESTS ACCESSIBILITY
   List<Failure> validateArchive(URL url, List<Validate> archiveValidation)
   {
      // Archive validation
      if (!getArchiveValidation())
      {
         return null;
      }
      Validator validator = new Validator();
      List<Failure> failures = validator.validate(archiveValidation);

      return failures;
   }

   /**
    * print Failures into Log files.
    *
    * @param urlFileName filename Of deployed rar
    * @param validator validator instance used to run validation rules
    * @param failures the list of Failures to be printed
    * @param reportDirectory where to put various logs
    * @param fhInput optional parameter. Normally used only for test or in case of
    *   FailureHelper already present in context
    * @return the error Text
    *
    */
   //IT IS PACKAGE PROTECTED ONLY FOR TESTS ACCESSIBILITY
   String printFailuresLog(String urlFileName, Validator validator, Collection<Failure> failures,
         File reportDirectory, FailureHelper... fhInput)
   {
      String errorText = "";
      FailureHelper fh = null;
      if (fhInput.length == 0)
         fh = new FailureHelper(failures);
      else
         fh = fhInput[0];

      if (failures != null && failures.size() > 0)
      {
         if (reportDirectory == null)
         {
            reportDirectory = new File(SecurityActions.getSystemProperty("iron.jacamar.home"), "/log/");
         }
         if (reportDirectory.exists())
         {
            int lastSlashIndex = urlFileName.lastIndexOf("/");
            int lastSepaIndex = urlFileName.lastIndexOf(File.separator);

            int lastIndex = lastSlashIndex > lastSepaIndex ? lastSlashIndex : lastSepaIndex;
            if (lastIndex != -1)
               urlFileName = urlFileName.substring(lastIndex + 1);
            urlFileName += ".log";

            File report = new File(reportDirectory, urlFileName);
            FileWriter fw = null;
            BufferedWriter bw = null;
            try
            {
               fw = new FileWriter(report);
               bw = new BufferedWriter(fw, 8192);
               bw.write(fh.asText(validator.getResourceBundle()));
               bw.flush();

               errorText = "Validation failures - see: " + report.getAbsolutePath();
            }
            catch (IOException ioe)
            {
               log.warn(ioe.getMessage(), ioe);
            }
            finally
            {
               if (bw != null)
               {
                  try
                  {
                     bw.close();
                  }
                  catch (IOException ignore)
                  {
                     // Ignore
                  }
               }
               if (fw != null)
               {
                  try
                  {
                     fw.close();
                  }
                  catch (IOException ignore)
                  {
                     // Ignore
                  }
               }
            }
         }
         else
         {
            errorText = fh.asText(validator.getResourceBundle());
         }
      }
      return errorText;
   }

   private boolean hasFailuresLevel(Collection<Failure> failures, int severity)
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
    * Start the resource adapter
    * @param resourceAdapter The resource adapter
    * @param bootstrapIdentifier The bootstrap context identifier; may be <code>null</code>
    * @throws DeployException Thrown if the resource adapter cant be started
    */
   @SuppressWarnings("unchecked")
   private void startContext(ResourceAdapter resourceAdapter, String bootstrapIdentifier) throws DeployException
   {
      try
      {
         Class clz = resourceAdapter.getClass();
         Method start = clz.getMethod("start", new Class[]
         {BootstrapContext.class});

         CloneableBootstrapContext cbc = null;

         if (bootstrapIdentifier != null && bootstrapContexts != null)
         {
            CloneableBootstrapContext bc = bootstrapContexts.get(bootstrapIdentifier);

            if (bc != null)
               cbc = bc.clone();
         }

         if (cbc == null)
            cbc = defaultBootstrapContext.clone();

         start.invoke(resourceAdapter, new Object[]
         {cbc});
      }
      catch (InvocationTargetException ite)
      {
         throw new DeployException("Unable to start " +
                                   resourceAdapter.getClass().getName(), ite.getTargetException());
      }
      catch (Throwable t)
      {
         throw new DeployException("Unable to start " + resourceAdapter.getClass().getName(), t);
      }
   }

   /**
    * Associate resource adapter with ojects if they implement ResourceAdapterAssociation
    * @param resourceAdapter The resource adapter
    * @param object The of possible association object
    * @throws DeployException Thrown if the resource adapter cant be started
    */
   @SuppressWarnings("unchecked")
   private void associateResourceAdapter(ResourceAdapter resourceAdapter, Object object)
      throws DeployException
   {
      if (resourceAdapter != null && object != null)
      {
         if (object instanceof ResourceAdapterAssociation)
         {
            try
            {
               Class clz = object.getClass();

               Method setResourceAdapter = clz.getMethod("setResourceAdapter",
                                                         new Class[]
                                                         {ResourceAdapter.class});

               setResourceAdapter.invoke(object, new Object[]
               {resourceAdapter});
            }
            catch (Throwable t)
            {
               throw new DeployException("Unable to associate " + object.getClass().getName(), t);
            }
         }
      }
   }

   /**
    * Initialize and inject configuration properties
    * @param className The fully qualified class name
    * @param configs The configuration properties
    * @param cl The class loader
    * @return The object
    * @throws DeployException Thrown if the object cant be initialized
    */
   private Object initAndInject(String className,
                                List<? extends ConfigProperty> configs,
                                ClassLoader cl)
      throws DeployException
   {
      try
      {
         Class clz = Class.forName(className, true, cl);
         Object o = clz.newInstance();

         if (configs != null)
         {
            Injection injector = new Injection();
            for (ConfigProperty cpmd : configs)
            {
               if (cpmd.isValueSet())
                  injector.inject(cpmd.getConfigPropertyType().getValue(), cpmd.getConfigPropertyName().getValue(),
                        cpmd.getConfigPropertyValue().getValue(), o);
            }
         }

         return o;
      }
      catch (Throwable t)
      {
         throw new DeployException("Deployment " + className + " failed", t);
      }
   }

   /**
    * Get the URLs for the directory and all libraries located in the directory
    * @param directory The directory
    * @return The URLs
    * @exception MalformedURLException MalformedURLException
    * @exception IOException IOException
    */
   private URL[] getUrls(File directory) throws MalformedURLException, IOException
   {
      List<URL> list = new LinkedList<URL>();

      if (directory.exists() && directory.isDirectory())
      {
         // Add directory
         list.add(directory.toURI().toURL());

         // Add the contents of the directory too
         File[] jars = directory.listFiles(new JarFilter());

         if (jars != null)
         {
            for (int j = 0; j < jars.length; j++)
            {
               list.add(jars[j].getCanonicalFile().toURI().toURL());
            }
         }
      }
      return list.toArray(new URL[list.size()]);
   }

   /**
    * Bind connection factory into JNDI
    * @param deployment The deployment name
    * @param cf The connection factory
    * @return The JNDI names bound
    * @exception Exception thrown if an error occurs
    */
   private String[] bindConnectionFactory(String deployment, Object cf) throws Throwable
   {
      if (jndiStrategy == null)
         jndiStrategy = new NoopJndiStrategy();

      JndiStrategy js = jndiStrategy.clone();

      return js.bindConnectionFactories(deployment, new Object[]
      {cf});
   }

   /**
    * Start
    */
   public void start()
   {
      if (defaultBootstrapContext == null)
         throw new IllegalStateException("DefaultBootstrapContext not defined");

      if (printStream == null)
         throw new IllegalStateException("PrintStream not defined");
   }

   /**
    * Clone
    * @return The copy of the object
    * @exception CloneNotSupportedException Thrown if a copy can't be created
    */
   @Override
   public Deployer clone() throws CloneNotSupportedException
   {
      return new RADeployer();
   }
}
