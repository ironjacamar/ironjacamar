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
import org.jboss.jca.common.metadata.merge.Merger;
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
import org.jboss.jca.validator.ValidateObject;
import org.jboss.jca.validator.Validator;
import org.jboss.jca.validator.ValidatorException;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.Referenceable;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.jboss.logging.Logger;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.api.util.FileUtil;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.DeployerPhases;
import com.github.fungal.spi.deployers.Deployment;

/**
 * The RA activator for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public final class RAActivator extends AbstractResourceAdapterDeployer implements DeployerPhases
{
   /** The logger */
   private static Logger log = Logger.getLogger(RAActivator.class);

   /** Trace enabled */
   private static boolean trace = log.isTraceEnabled();

   /** The kernel */
   private Kernel kernel;

   /** Enabled */
   private boolean enabled;

   /** The archives that should be excluded for activation */
   private Set<String> excludeArchives;

   /** The list of generated deployments */
   private List<Deployment> deployments;

   /**
    * Constructor
    */
   public RAActivator()
   {
      kernel = null;
      enabled = true;
      excludeArchives = null;
      deployments = null;
   }

   /**
    * Get the kernel
    * @return The kernel
    */
   public Kernel getKernel()
   {
      return kernel;
   }

   /**
    * Set the kernel
    * @param kernel The kernel
    */
   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }

   /**
    * Get the exclude archives
    * @return The archives
    */
   public Set<String> getExcludeArchives()
   {
      return excludeArchives;
   }

   /**
    * Set the exclude archives
    * @param archives The archives
    */
   public void setExcludeArchives(Set<String> archives)
   {
      this.excludeArchives = archives;
   }

   /**
    * Is enabled
    * @return True if enabled; otherwise false
    */
   public boolean isEnabled()
   {
      return enabled;
   }

   /**
    * Set the eanbled flag
    * @param value The value
    */
   public void setEnabled(boolean value)
   {
      this.enabled = value;
   }
   /**
    * Pre deploy
    * @exception Throwable Thrown if an error occurs
    */
   public void preDeploy() throws Throwable
   {
   }

   /**
    * Post deploy
    * @exception Throwable Thrown if an error occurs
    */
   public void postDeploy() throws Throwable
   {
      if (enabled)
      {
         Set<URL> rarDeployments = getConfiguration().getMetadataRepository().getResourceAdapters();

         for (URL deployment : rarDeployments)
         {
            if (trace)
               log.trace("Processing: " + deployment.toExternalForm());

            boolean include = true;

            if (excludeArchives != null)
            {
               for (String excludedArchive : excludeArchives)
               {
                  if (deployment.toExternalForm().endsWith(excludedArchive))
                     include = false;
               }
            }

            if (include)
            {
               Map<String, List<String>> jndiMappings =
                  getConfiguration().getMetadataRepository().getJndiMappings(deployment);

               // If there isn't any JNDI mappings then the archive isn't active
               // so activate it
               if (jndiMappings == null)
               {
                  Deployment raDeployment = deploy(deployment, kernel.getKernelClassLoader());
                  if (raDeployment != null)
                  {
                     if (deployments == null)
                        deployments = new ArrayList<Deployment>(1);

                     deployments.add(raDeployment);

                     kernel.getMainDeployer().registerDeployment(raDeployment);
                  }
               }
            }
         }
      }
   }

   /**
    * Pre undeploy
    * @exception Throwable Thrown if an error occurs
    */
   public void preUndeploy() throws Throwable
   {
      if (deployments != null)
      {
         for (Deployment raDeployment : deployments)
         {
            try
            {
               kernel.getMainDeployer().unregisterDeployment(raDeployment);
            }
            catch (Throwable t)
            {
               log.warn("Error during undeployment of " + raDeployment.getURL());
            }
         }

         deployments = null;
      }
   }

   /**
    * Post undeploy
    * @exception Throwable Thrown if an error occurs
    */
   public void postUndeploy() throws Throwable
   {
   }

   /**
    * Deploy
    * @param url The url
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   private Deployment deploy(URL url, ClassLoader parent) throws DeployException
   {
      Set<Failure> failures = null;

      log.debug("Deploying: " + url.toExternalForm());

      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      try
      {
         File f = new File(url.toURI());

         if (!f.exists())
            return null;

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

         // Get metadata
         Connector cmd = getConfiguration().getMetadataRepository().getResourceAdapter(url);
         IronJacamar ijmd = getConfiguration().getMetadataRepository().getIronJacamar(url);

         cmd = (new Merger()).mergeConnectorWithCommonIronJacamar(ijmd, cmd);

         ResourceAdapter resourceAdapter = null;
         List<Validate> archiveValidationObjects = new ArrayList<Validate>();
         List<Failure> partialFailures = null;
         List<Object> beanValidationObjects = new ArrayList<Object>();

         String deploymentName = f.getName().substring(0, f.getName().indexOf(".rar"));
         Object[] cfs = null;
         String[] jndis = null;

         // Create objects and inject values
         if (cmd != null)
         {
            // ResourceAdapter
            if (cmd.getVersion() != Version.V_10)
            {
               ResourceAdapter1516 ra1516 = (ResourceAdapter1516)cmd.getResourceadapter();
               if (ra1516 != null && ra1516.getResourceadapterClass() != null)
               {
                  resourceAdapter =
                     (ResourceAdapter) initAndInject(
                         ra1516.getResourceadapterClass(), ra1516.getConfigProperties(), cl);

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

            // ManagedConnectionFactory
            if (cmd.getVersion() == Version.V_10)
            {
               org.jboss.jca.common.api.metadata.common.CommonConnDef ijCD = null;
                        
               if (ijmd != null)
               {
                  ijCD = findConnectionDefinition(((ResourceAdapter10) cmd.getResourceadapter())
                                                  .getManagedConnectionFactoryClass().getValue(),
                                                  ijmd.getConnectionDefinitions());
               }

               if (ijmd == null || ijCD == null || ijCD.isEnabled())
               {
                  ManagedConnectionFactory mcf =
                     (ManagedConnectionFactory) initAndInject(((ResourceAdapter10) cmd.getResourceadapter())
                                                              .getManagedConnectionFactoryClass().getValue(), 
                                                              ((ResourceAdapter10) cmd.getResourceadapter())
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
                                                                  ((ResourceAdapter10) cmd.getResourceadapter())
                                                                  .getConfigProperties()));
                  beanValidationObjects.add(mcf);
                  associateResourceAdapter(resourceAdapter, mcf);

                  // Create the pool
                  PoolConfiguration pc = new PoolConfiguration();
                  PoolFactory pf = new PoolFactory();

                  Boolean noTxSeparatePool = Boolean.FALSE;

                  if (ijCD != null && ijCD.getPool() != null && ijCD.isXa())
                  {
                     org.jboss.jca.common.api.metadata.common.CommonXaPool ijXaPool =
                        (org.jboss.jca.common.api.metadata.common.CommonXaPool)ijCD.getPool();
                     
                     if (ijXaPool != null)
                        noTxSeparatePool = ijXaPool.isNoTxSeparatePool();
                  }

                  Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, noTxSeparatePool.booleanValue());
               
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

                  // Section 7.13 -- Read from metadata -> overwrite with specified value if present
                  if (mcf instanceof TransactionSupport)
                     tsl = ((TransactionSupport) mcf).getTransactionSupport();

                  // Connection manager properties
                  Integer allocationRetry = null;
                  Long allocationRetryWaitMillis = null;

                  if (ijCD != null && ijCD.getTimeOut() != null)
                  {
                     allocationRetry = ijCD.getTimeOut().getAllocationRetry();
                     allocationRetryWaitMillis = ijCD.getTimeOut().getAllocationRetryWaitMillis();
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
                     Boolean interleaving = null;
                     Integer xaResourceTimeout = null;
                     Boolean isSameRMOverride = null;
                     Boolean wrapXAResource = null;
                     Boolean padXid = null;

                     if (ijCD != null && ijCD.getPool() != null && ijCD.isXa())
                     {
                        org.jboss.jca.common.api.metadata.common.CommonXaPool ijXaPool =
                           (org.jboss.jca.common.api.metadata.common.CommonXaPool)ijCD.getPool();
                        
                        if (ijXaPool != null)
                        {
                           interleaving = ijXaPool.isInterleaving();
                           isSameRMOverride = ijXaPool.isSameRmOverride();
                           wrapXAResource = ijXaPool.isWrapXaDataSource();
                           padXid = ijXaPool.isPadXid();
                        }
                     }

                     cm = cmf.createTransactional(tsl,
                                                  pool,
                                                  allocationRetry,
                                                  allocationRetryWaitMillis,
                                                  getConfiguration().getTransactionManager(),
                                                  interleaving,
                                                  xaResourceTimeout,
                                                  isSameRMOverride,
                                                  wrapXAResource,
                                                  padXid);
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
                     String[] jndiNames = bindConnectionFactory(url, deploymentName, cf);
                     cfs = new Object[] {cf};
                     jndis = new String[] {jndiNames[0]};

                     cm.setJndiName(jndiNames[0]);
                  }
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
                     if (cdMetas.size() == 1)
                     {
                        ConnectionDefinition cdMeta = cdMetas.get(0);

                        org.jboss.jca.common.api.metadata.common.CommonConnDef ijCD = null;
                        
                        if (ijmd != null)
                        {
                           ijCD = findConnectionDefinition(cdMeta.getManagedConnectionFactoryClass().getValue(),
                                                           ijmd.getConnectionDefinitions());
                        }

                        if (ijmd == null || ijCD == null || ijCD.isEnabled())
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

                           Boolean noTxSeparatePool = Boolean.FALSE;
                        
                           if (ijCD != null && ijCD.getPool() != null && ijCD.isXa())
                           {
                              org.jboss.jca.common.api.metadata.common.CommonXaPool ijXaPool =
                                 (org.jboss.jca.common.api.metadata.common.CommonXaPool)ijCD.getPool();
                              
                              if (ijXaPool != null)
                                 noTxSeparatePool = ijXaPool.isNoTxSeparatePool();
                           }

                           Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, noTxSeparatePool.booleanValue());
                           
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
                           Integer allocationRetry = null;
                           Long allocationRetryWaitMillis = null;
                        
                           if (ijCD != null && ijCD.getTimeOut() != null)
                           {
                              allocationRetry = ijCD.getTimeOut().getAllocationRetry();
                              allocationRetryWaitMillis = ijCD.getTimeOut().getAllocationRetryWaitMillis();
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
                              Boolean interleaving = null;
                              Integer xaResourceTimeout = null;
                              Boolean isSameRMOverride = null;
                              Boolean wrapXAResource = null;
                              Boolean padXid = null;
                              
                              if (ijCD != null && ijCD.isXa())
                              {
                                 org.jboss.jca.common.api.metadata.common.CommonXaPool ijXaPool =
                                    (org.jboss.jca.common.api.metadata.common.CommonXaPool)ijCD.getPool();
                                 
                                 interleaving = ijXaPool.isInterleaving();
                                 isSameRMOverride = ijXaPool.isSameRmOverride();
                                 wrapXAResource = ijXaPool.isWrapXaDataSource();
                                 padXid = ijXaPool.isPadXid();
                              }
                           
                              cm = cmf.createTransactional(tsl,
                                                           pool,
                                                           allocationRetry,
                                                           allocationRetryWaitMillis,
                                                           getConfiguration().getTransactionManager(),
                                                           interleaving,
                                                           xaResourceTimeout,
                                                           isSameRMOverride,
                                                           wrapXAResource,
                                                           padXid);
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
                              deploymentName = f.getName().substring(0, f.getName().indexOf(".rar"));
                              String[] jndiNames = bindConnectionFactory(url, deploymentName, cf);
                              cfs = new Object[] {cf};
                              jndis = new String[] {jndiNames[0]};
                              
                              cm.setJndiName(jndiNames[0]);
                           }
                        }
                        else
                        {
                           log.warn("There are multiple connection factories for: " + f.getName());
                           log.warn("Use an ironjacamar.xml or a -ra.xml to activate the deployment");
                        }
                     }
                  }
               }
            }

            // ActivationSpec
            if (cmd.getVersion() != Version.V_10)
            {
               ResourceAdapter1516 ra1516 = (ResourceAdapter1516) cmd.getResourceadapter();
               if (ra1516 != null &&
                   ra1516.getInboundResourceadapter() != null &&
                   ra1516.getInboundResourceadapter().getMessageadapter() != null &&
                   ra1516.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null)
               {
                  List<MessageListener> mlMetas =
                     ra1516.getInboundResourceadapter().getMessageadapter().getMessagelisteners();

                  if (mlMetas.size() > 0)
                  {
                     for (MessageListener mlMeta : mlMetas)
                     {
                        if (mlMeta.getActivationspec() != null &&
                            mlMeta.getActivationspec().getActivationspecClass().getValue() != null)
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
            if (cmd.getVersion() != Version.V_10)
            {
               ResourceAdapter1516 ra1516 = (ResourceAdapter1516) cmd.getResourceadapter();
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

         if ((getConfiguration().getArchiveValidationFailOnWarn() && hasFailuresLevel(failures, Severity.WARNING)) ||
             (getConfiguration().getArchiveValidationFailOnError() && hasFailuresLevel(failures, Severity.ERROR)))
         {
            throw new ValidatorException(printFailuresLog(url.getPath(), new Validator(), failures, null), failures);
         }
         else
         {
            printFailuresLog(url.getPath(), new Validator(), failures, null);
         }

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

         log.info("Deployed: " + url.toExternalForm());

         return new RAActivatorDeployment(url,
                                          deploymentName,
                                          resourceAdapter,
                                          getConfiguration().getJndiStrategy(),
                                          getConfiguration().getMetadataRepository(),
                                          cfs,
                                          jndis,
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
    * Start
    */
   @Override
   public void start()
   {
      super.start();

      if (kernel == null)
         throw new IllegalStateException("Kernel not defined");
   }
}
