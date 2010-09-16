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
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapters;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.common.metadata.resourceadapter.ResourceAdapterParser;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.resource.Referenceable;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.jboss.logging.Logger;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.DeployerOrder;
import com.github.fungal.spi.deployers.DeployerPhases;
import com.github.fungal.spi.deployers.Deployment;
import com.github.fungal.spi.deployers.MultiStageDeployer;

/**
 * The -ra.xml deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public final class RaXmlDeployer extends AbstractResourceAdapterDeployer implements Deployer,
                                                                                    MultiStageDeployer,
                                                                                    DeployerOrder,
                                                                                    DeployerPhases
{
   private static Logger log = Logger.getLogger(RaXmlDeployer.class);

   private static boolean trace = log.isTraceEnabled();

   /** The kernel */
   private Kernel kernel;

   /** The list of generated deployments */
   private List<Deployment> deployments;

   /**
    * Constructor
    */
   public RaXmlDeployer()
   {
   }

   /**
    * Deployer order
    * @return The deployment
    */
   public int getOrder()
   {
      return 0;
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
   @Override
   public synchronized Deployment deploy(URL url, ClassLoader parent) throws DeployException
   {
      if (url == null || !(url.toExternalForm().endsWith("-ra.xml")))
         return null;

      log.debug("Deploying: " + url.toExternalForm());

      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      InputStream is = null;
      try
      {
         File f = new File(url.toURI());

         if (!f.exists())
            throw new IOException("Archive " + url.toExternalForm() + " doesnt exists");

         // Parse metadata
         is = new FileInputStream(f);
         ResourceAdapterParser parser = new ResourceAdapterParser();
         ResourceAdapters raXmlDeployment = parser.parse(is);

         int size = raXmlDeployment.getResourceAdapters().size();
         if (size == 1)
         {
            return doDeploy(url, raXmlDeployment.getResourceAdapters().get(0), parent);
         }
         else
         {
            deployments = new ArrayList<Deployment>(size);

            for (org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter raxml :
                    raXmlDeployment.getResourceAdapters())
            {
               Deployment raDeployment = doDeploy(url, raxml, parent);
               if (raDeployment != null)
               {
                  deployments.add(raDeployment);

                  kernel.getMainDeployer().registerDeployment(raDeployment);
               }
            }

            return null;
         }
      }
      catch (DeployException de)
      {
         // Just rethrow
         throw de;
      }
      catch (Throwable t)
      {
         throw new DeployException("Exception during deployment of " + url.toExternalForm(), t);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }

         SecurityActions.setThreadContextClassLoader(oldTCCL);
      }
   }

   /**
    * Deploy an entry in the -ra.xml deployment
    * @param url The deployment url
    * @param raxml The -ra.xml entry
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   private Deployment doDeploy(URL url,
                               org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter raxml,
                               ClassLoader parent)
      throws DeployException
   {
      Set<Failure> failures = null;

      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      try
      {
         // Find the archive in MDR
         String archive = raxml.getArchive();
         URL deployment = null;
         Set<URL> deployments = getConfiguration().getMetadataRepository().getResourceAdapters();

         for (URL u : deployments)
         {
            if (u.toExternalForm().endsWith(archive))
               deployment = u;
         }

         if (deployment == null)
         {
            throw new DeployException("Archive " + archive + " couldn't be resolved in " + url.toExternalForm());
         }

         Connector cmd = getConfiguration().getMetadataRepository().getResourceAdapter(deployment);
         IronJacamar ijmd = getConfiguration().getMetadataRepository().getIronJacamar(deployment);
         File root = getConfiguration().getMetadataRepository().getRoot(deployment);

         cmd = (new Merger()).mergeConnectorWithCommonIronJacamar(raxml, cmd);
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

         javax.resource.spi.ResourceAdapter resourceAdapter = null;
         List<Validate> archiveValidationObjects = new ArrayList<Validate>();
         List<Failure> partialFailures = null;
         List<Object> beanValidationObjects = new ArrayList<Object>();

         String deploymentName = archive.substring(0, archive.indexOf(".rar"));
         Object[] cfs = null;
         String[] jndiNames = null;

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
                     (javax.resource.spi.ResourceAdapter) initAndInject(
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
               // Section 7.13 -- Read from metadata -> overwrite with specified value if present
               if (mcf instanceof TransactionSupport)
                  tsl = ((TransactionSupport) mcf).getTransactionSupport();

               // Connection manager properties
               Integer allocationRetry = null; // TODO
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
                  org.jboss.jca.common.api.metadata.common.CommonConnDef cd =
                     findConnectionDefinition(mcf.getClass().getName(), raxml.getConnectionDefinitions());

                  String jndiName = cd.getJndiName();

                  bindConnectionFactory(deployment, deploymentName, cf, jndiName);
                  cfs = new Object[] {cf};
                  jndiNames = new String[] {jndiName};

                  cm.setJndiName(jndiName);
               }
            }
            else
            {
               ResourceAdapter1516 ra = (ResourceAdapter1516) cmd.getResourceadapter();
               if (ra != null &&
                   ra.getOutboundResourceadapter() != null &&
                   ra.getOutboundResourceadapter().getConnectionDefinitions() != null)
               {
                  List<org.jboss.jca.common.api.metadata.ra.ConnectionDefinition> cdMetas =
                     ra.getOutboundResourceadapter().getConnectionDefinitions();

                  if (cdMetas.size() > 0)
                  {
                     cfs = new Object[cdMetas.size()];
                     jndiNames = new String[cdMetas.size()];

                     for (int cdIndex = 0; cdIndex < cdMetas.size(); cdIndex++)
                     {
                        org.jboss.jca.common.api.metadata.ra.ConnectionDefinition cdMeta = cdMetas.get(cdIndex);

                        org.jboss.jca.common.api.metadata.common.CommonConnDef cdRaXml =
                           findConnectionDefinition(cdMeta.getManagedConnectionFactoryClass().getValue(),
                                                    raxml.getConnectionDefinitions());

                        if (cdRaXml != null && cdRaXml.isEnabled())
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
                           Integer allocationRetry = null; // TODO
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
                              String jndiName = cdRaXml.getJndiName();

                              bindConnectionFactory(deployment, deploymentName, cf, jndiName);
                              cfs[cdIndex] = cf;
                              jndiNames[cdIndex] = jndiName;

                              cm.setJndiName(jndiName);
                           }
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

            if (raxml.getBeanValidationGroups() != null &&
                raxml.getBeanValidationGroups().size() > 0)
            {
               List<String> groups = raxml.getBeanValidationGroups();

               groupsClasses = new ArrayList<Class>(groups.size());
               for (String group : groups)
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

            if (raxml.getBootstrapContext() != null &&
                !raxml.getBootstrapContext().trim().equals(""))
            {
               bootstrapIdentifier = raxml.getBootstrapContext();
            }

            startContext(resourceAdapter, bootstrapIdentifier);
         }

         log.info("Deployed: " + url.toExternalForm());

         return new RaXmlDeployment(url,
                                    deployment,
                                    deploymentName,
                                    resourceAdapter,
                                    getConfiguration().getJndiStrategy(),
                                    getConfiguration().getMetadataRepository(),
                                    cfs,
                                    jndiNames,
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
    * Find the JNDI name for a connection factory
    * @param clz The fully quilified class name for the managed connection factory
    * @param defs The connection definitions
    * @return The JNDI name
    */
   private org.jboss.jca.common.api.metadata.common.CommonConnDef findConnectionDefinition(String clz,
      List<org.jboss.jca.common.api.metadata.common.CommonConnDef> defs)
   {
      if (defs != null)
      {
         // If there is only one we will return that
         if (defs.size() == 1)
            return defs.get(0);

         // If there are multiple definitions the MCF class name is mandatory
         if (clz == null)
            throw new IllegalArgumentException("ManagedConnectionFactory must be defined in class-name");

         for (org.jboss.jca.common.api.metadata.common.CommonConnDef cd : defs)
         {
            if (clz.equals(cd.getClassName()))
               return cd;
         }
      }

      return null;
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
