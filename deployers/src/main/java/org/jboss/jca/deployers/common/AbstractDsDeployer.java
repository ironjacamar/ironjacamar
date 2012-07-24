/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.deployers.common;

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.CommonDataSource;
import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.metadata.ds.v10.DataSourceImpl;
import org.jboss.jca.common.metadata.ds.v10.XADataSourceImpl;
import org.jboss.jca.common.metadata.ra.common.ConfigPropertyImpl;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.connectionmanager.pool.api.PrefillPool;
import org.jboss.jca.core.recovery.DefaultRecoveryPlugin;
import org.jboss.jca.core.spi.mdr.NotFoundException;
import org.jboss.jca.core.spi.recovery.RecoveryPlugin;
import org.jboss.jca.core.spi.statistics.Statistics;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecovery;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecoveryRegistry;
import org.jboss.jca.deployers.DeployersBundle;
import org.jboss.jca.deployers.DeployersLogger;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.logging.Messages;
import org.jboss.security.SubjectFactory;

/**
 * An abstract deployer implementation for datasources
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractDsDeployer
{
   /** The bundle */
   private static DeployersBundle bundle = Messages.getBundle(DeployersBundle.class);

   /** log **/
   protected DeployersLogger log;

   /** The transaction integration */
   protected TransactionIntegration transactionIntegration;

   /** xaResourceRecoveryRegistry */
   protected XAResourceRecoveryRegistry xaResourceRecoveryRegistry;

   /** The ManagementRepository */
   private ManagementRepository managementRepository = null;

   /** Cached connection manager */
   private CachedConnectionManager ccm;

   /**
    * Create a new AbstractDsDeployer.
    */
   public AbstractDsDeployer()
   {
      this.log = getLogger();
      this.transactionIntegration = null;
      this.ccm = null;
   }

   /**
    * Set the transaction integration
    * @param value The value
    */
   public void setTransactionIntegration(TransactionIntegration value)
   {
      transactionIntegration = value;
   }

   /**
    * Get the transaction integration
    * @return The value
    */
   public TransactionIntegration getTransactionIntegration()
   {
      return transactionIntegration;
   }

   /**
    * Get the managementRepository.
    *
    * @return the managementRepository.
    */
   public ManagementRepository getManagementRepository()
   {
      return managementRepository;
   }

   /**
    * Set the managementRepository.
    *
    * @param managementRepository The managementRepository to set.
    */
   public void setManagementRepository(ManagementRepository managementRepository)
   {
      this.managementRepository = managementRepository;
   }

   /**
    * Set the ccm
    * @param value The value
    */
   public void setCachedConnectionManager(CachedConnectionManager value)
   {
      ccm = value;
   }

   /**
    * Get the ccm
    * @return The handle
    */
   public CachedConnectionManager getCachedConnectionManager()
   {
      return ccm;
   }

   /** Get the xAResourceRecoveryRegistry.
    *
    * @return the xAResourceRecoveryRegistry.
    */
   public XAResourceRecoveryRegistry getXAResourceRecoveryRegistry()
   {
      return xaResourceRecoveryRegistry;
   }

   /**
    * Set the xAResourceRecoveryRegistry.
    *
    * @param xAResourceRecoveryRegistry The xAResourceRecoveryRegistry to set.
    */
   public void setXAResourceRecoveryRegistry(XAResourceRecoveryRegistry xAResourceRecoveryRegistry)
   {
      xaResourceRecoveryRegistry = xAResourceRecoveryRegistry;
   }

   /**
   *
   * create objects and inject value for this depployment. it is a general method returning a {@link CommonDeployment}
   * to be used to exchange objects needed to real injection in the container
   *
   * @param url url
   * @param deploymentName deploymentName
   * @param uniqueJdbcLocalId uniqueJdbcLocalId
   * @param uniqueJdbcXAId uniqueJdbcXAId
   * @param parentClassLoader cl
   * @param dataSources datasources metadata defined in xml
   * @return return the exchange POJO with value useful for injection in the container (fungal or AS)
   * @throws DeployException DeployException
   */
   protected CommonDeployment createObjectsAndInjectValue(URL url,
                                                          String deploymentName,
                                                          String uniqueJdbcLocalId,
                                                          String uniqueJdbcXAId,
                                                          DataSources dataSources,
                                                          ClassLoader parentClassLoader)
      throws DeployException
   {
      try
      {
         List<Object> cfs = new ArrayList<Object>(1);
         List<String> jndis = new ArrayList<String>(1);
         List<ConnectionManager> cms = new ArrayList<ConnectionManager>(1);
         List<XAResourceRecovery> recoveryModules = new ArrayList<XAResourceRecovery>(1);
         List<org.jboss.jca.core.api.management.DataSource> mgts =
            new ArrayList<org.jboss.jca.core.api.management.DataSource>(1);

         if (uniqueJdbcLocalId != null)
         {
            List<DataSource> ds = dataSources.getDataSource();
            if (ds != null)
            {
               ClassLoader jdbcLocalDeploymentCl = getDeploymentClassLoader(uniqueJdbcLocalId);

               for (DataSource dataSource : ds)
               {
                  if (log.isTraceEnabled())
                     log.tracef("DataSource=%s", dataSource);

                  if (dataSource.isEnabled())
                  {
                     String jndiName = buildJndiName(dataSource.getJndiName(), dataSource.isUseJavaContext());

                     try
                     {
                        org.jboss.jca.core.api.management.DataSource mgtDataSource =
                           new org.jboss.jca.core.api.management.DataSource(false);

                        ConnectionManager[] cm = new ConnectionManager[1];

                        if (dataSource.getDriverClass() == null && dataSource.getDriver() != null &&
                            dataSource instanceof DataSourceImpl)
                        {
                           String driverClass = null;

                           if (dataSources.getDriver(dataSource.getDriver()) != null)
                              driverClass = dataSources.getDriver(dataSource.getDriver()).getDriverClass();

                           if (driverClass != null)
                              ((DataSourceImpl) dataSource).forceDriverClass(driverClass);
                        }

                        if (dataSource.getDriverClass() == null && dataSource.getDriver() != null &&
                            dataSource instanceof DataSourceImpl)
                        {
                           String driverName = dataSource.getDriver();
                           String moduleId = null;

                           if (dataSources.getDriver(dataSource.getDriver()) != null)
                              moduleId = dataSources.getDriver(dataSource.getDriver()).getModule();

                           String driverClass = getDriver(driverName, moduleId);

                           if (driverClass != null)
                              ((DataSourceImpl) dataSource).forceDriverClass(driverClass);
                        }

                        if (dataSource.getDriverClass() == null && dataSource.getDataSourceClass() == null &&
                            dataSource.getDriver() != null && dataSource instanceof DataSourceImpl)
                        {
                           String driverName = dataSource.getDriver();

                           if (dataSources.getDriver(driverName) != null)
                           {
                              String dataSourceClass = dataSources.getDriver(driverName).getDataSourceClass();
                              ((DataSourceImpl) dataSource).forceDataSourceClass(dataSourceClass);
                           }
                        }

                        Object cf = deployDataSource(dataSource, jndiName,
                                                     uniqueJdbcLocalId, cm, mgtDataSource, jdbcLocalDeploymentCl);

                        bindConnectionFactory(deploymentName, jndiName, cf);

                        cfs.add(cf);
                        jndis.add(jndiName);
                        cms.add(cm[0]);
                        mgts.add(mgtDataSource);
                     }
                     catch (Throwable t)
                     {
                        log.error("Error during the deployment of " + jndiName, t);
                     }
                  }
               }
            }
         }
         else
         {
            if (dataSources.getDataSource() != null && dataSources.getDataSource().size() > 0)
               log.error("Deployment of datasources disabled since jdbc-local.rar couldn't be found");
         }

         if (uniqueJdbcXAId != null)
         {
            List<XaDataSource> xads = dataSources.getXaDataSource();
            if (xads != null)
            {
               ClassLoader jdbcXADeploymentCl = getDeploymentClassLoader(uniqueJdbcXAId);

               for (XaDataSource xaDataSource : xads)
               {
                  if (log.isTraceEnabled())
                     log.tracef("XaDataSource=%s", xaDataSource);

                  if (xaDataSource.isEnabled())
                  {
                     String jndiName = buildJndiName(xaDataSource.getJndiName(), xaDataSource.isUseJavaContext());

                     try
                     {
                        org.jboss.jca.core.api.management.DataSource mgtDataSource =
                           new org.jboss.jca.core.api.management.DataSource(true);

                        XAResourceRecovery[] recovery = new XAResourceRecovery[1];
                        ConnectionManager[] cm = new ConnectionManager[1];

                        if (xaDataSource.getXaDataSourceClass() == null && xaDataSource.getDriver() != null &&
                            xaDataSource instanceof XADataSourceImpl)
                        {
                           ((XADataSourceImpl) xaDataSource).forceXaDataSourceClass(dataSources.getDriver(
                              xaDataSource
                                 .getDriver()).getXaDataSourceClass());
                        }

                        Object cf = deployXADataSource(xaDataSource,
                                                       jndiName, uniqueJdbcXAId, cm,
                                                       recovery,
                                                       mgtDataSource,
                                                       jdbcXADeploymentCl);

                        recoveryModules.add(recovery[0]);

                        bindConnectionFactory(deploymentName, jndiName, cf);

                        cfs.add(cf);
                        jndis.add(jndiName);
                        cms.add(cm[0]);
                        mgts.add(mgtDataSource);
                     }
                     catch (Throwable t)
                     {
                        log.error("Error during the deployment of " + jndiName, t);
                     }
                  }
               }
            }
         }
         else
         {
            if (dataSources.getXaDataSource() != null && dataSources.getXaDataSource().size() > 0)
               log.error("Deployment of XA datasources disabled since jdbc-xa.rar couldn't be found");
         }

         return new CommonDeployment(url, deploymentName, true, null, null, cfs.toArray(new Object[cfs.size()]),
                                     jndis.toArray(new String[jndis.size()]),
                                     cms.toArray(new ConnectionManager[cms.size()]),
                                     null, null,
                                     recoveryModules.toArray(new XAResourceRecovery[recoveryModules.size()]),
                                     null,
                                     mgts.toArray(new org.jboss.jca.core.api.management.DataSource[mgts.size()]),
                                     parentClassLoader, log);
      }
      catch (Throwable t)
      {
         throw new DeployException(bundle.deploymentFailed(url.toExternalForm()), t);
      }
   }

   /**
    * Build the jndi name
    * @param jndiName The jndi name
    * @param javaContext The java context
    * @return The value
    */
   protected String buildJndiName(String jndiName, Boolean javaContext)
   {
      if (javaContext != null)
      {
         if (javaContext.booleanValue() && !jndiName.startsWith("java:"))
         {
            jndiName = "java:" + jndiName;
         }
         else if (!javaContext.booleanValue() && jndiName.startsWith("java:"))
         {
            jndiName = jndiName.substring(6);
         }
      }

      return jndiName;
   }

   /**
    * Get the driver
    * @param driverName The name of the driver
    * @param moduleId The id of the module
    * @return The driver class name; or <code>null</code> if not found
    */
   protected String getDriver(String driverName, String moduleId)
   {
      return null;
   }

   /**
    * Deploy a datasource
    * @param ds The datasource
    * @param jndiName The JNDI name
    * @param uniqueId The unique id for the resource adapter
    * @param cma The connection manager array
    * @param mgtDs The management of a datasource
    * @param cl The class loader
    * @return The connection factory
    * @exception Throwable Thrown if an error occurs during deployment
    */
   private Object deployDataSource(DataSource ds, String jndiName, String uniqueId, ConnectionManager[] cma,
                                   org.jboss.jca.core.api.management.DataSource mgtDs, ClassLoader cl) throws Throwable
   {
      ManagedConnectionFactory mcf = createMcf(ds, uniqueId, cl);

      initAndInjectClassLoaderPlugin(mcf, ds);
      // Create the pool
      PoolConfiguration pc = createPoolConfiguration(ds.getPool(), ds.getTimeOut(), ds.getValidation());

      PoolFactory pf = new PoolFactory();
      PoolStrategy strategy = PoolStrategy.ONE_POOL;

      boolean allowMultipleUsers = false;
      if (ds.getPool() != null && ds.getPool() instanceof org.jboss.jca.common.api.metadata.ds.v11.DsPool)
      {
         org.jboss.jca.common.api.metadata.ds.v11.DsPool dsPool =
            (org.jboss.jca.common.api.metadata.ds.v11.DsPool)ds.getPool();

         if (dsPool.isAllowMultipleUsers() != null && dsPool.isAllowMultipleUsers().booleanValue())
         {
            strategy = PoolStrategy.POOL_BY_CRI;
            allowMultipleUsers = true;
         }
      }

      // Security
      String securityDomain = null;
      if (ds.getSecurity() != null)
      {
         if (ds.getSecurity().getReauthPlugin() != null)
         {
            strategy = PoolStrategy.REAUTH;
            securityDomain = ds.getSecurity().getSecurityDomain();
         }
         else if (ds.getSecurity().getSecurityDomain() != null)
         {
            if (!allowMultipleUsers)
            {
               strategy = PoolStrategy.POOL_BY_SUBJECT;
            }
            else
            {
               strategy = PoolStrategy.POOL_BY_SUBJECT_AND_CRI;
            }
            securityDomain = ds.getSecurity().getSecurityDomain();
         }
      }

      Pool pool = pf.create(strategy, mcf, pc, false);

      // Connection manager properties
      Integer allocationRetry = null;
      Long allocationRetryWaitMillis = null;

      if (ds.getTimeOut() != null)
      {
         allocationRetry = ds.getTimeOut().getAllocationRetry();
         allocationRetryWaitMillis = ds.getTimeOut().getAllocationRetryWaitMillis();
      }

      // Register data sources
      mgtDs.setJndiName(jndiName);
      mgtDs.setPoolConfiguration(pc);
      mgtDs.setPool(pool);

      if (mcf instanceof Statistics)
         mgtDs.setStatistics(((Statistics)mcf).getStatistics());

      log.debugf("Adding management datasource: %s", mgtDs);
      getManagementRepository().getDataSources().add(mgtDs);

      // Flush strategy
      FlushStrategy flushStrategy = FlushStrategy.FAILING_CONNECTION_ONLY;
      if (ds.getPool() != null)
         flushStrategy = ds.getPool().getFlushStrategy();

      // Select the correct connection manager
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ConnectionManager cm = null;

      if (ds.isJTA())
      {
         cm = cmf.createTransactional(TransactionSupportLevel.LocalTransaction, pool, 
                                      getSubjectFactory(securityDomain), securityDomain,
                                      ds.isUseCcm(), getCachedConnectionManager(),
                                      flushStrategy,
                                      allocationRetry, allocationRetryWaitMillis,
                                      getTransactionIntegration(),
                                      null, null, null, null, null);
      }
      else
      {
         cm = cmf.createNonTransactional(TransactionSupportLevel.NoTransaction, pool, 
                                         getSubjectFactory(securityDomain), securityDomain,
                                         ds.isUseCcm(), getCachedConnectionManager(),
                                         flushStrategy, allocationRetry, allocationRetryWaitMillis);
      }

      cm.setJndiName(jndiName);
      cma[0] = cm;

      String poolName = null;
      if (ds.getPoolName() != null)
      {
         poolName = ds.getPoolName();
      }

      if (poolName == null)
         poolName = jndiName;

      pool.setName(poolName);

      // Spy
      if (ds.isSpy())
      {
         injectValue(mcf, "setSpy", Boolean.TRUE);
         injectValue(mcf, "setJndiName", jndiName);
      }

      // JTA
      if (ds.isJTA())
      {
         injectValue(mcf, "setJTA", Boolean.TRUE);
      }
      else
      {
         injectValue(mcf, "setJTA", Boolean.FALSE);
      }

      // Reauth
      if (strategy == PoolStrategy.REAUTH)
      {
         injectValue(mcf, "setReauthEnabled", Boolean.TRUE);
         injectValue(mcf, "setReauthPluginClassName", ds.getSecurity().getReauthPlugin().getClassName());

         Map<String, String> mps = ds.getSecurity().getReauthPlugin().getConfigPropertiesMap();
         if (mps != null && mps.size() > 0)
         {
            StringBuilder reauthPluginProperties = new StringBuilder();

            Iterator<Map.Entry<String, String>> entryIterator = mps.entrySet().iterator();
            while (entryIterator.hasNext())
            {
               Map.Entry<String, String> entry = entryIterator.next();

               reauthPluginProperties.append(entry.getKey());
               reauthPluginProperties.append("|");
               reauthPluginProperties.append(entry.getValue());

               if (entryIterator.hasNext())
                  reauthPluginProperties.append(",");
            }

            injectValue(mcf, "setReauthPluginProperties", reauthPluginProperties.toString());
         }
      }

      // Prefill
      if (pool instanceof PrefillPool)
      {
         PrefillPool pp = (PrefillPool)pool;
         SubjectFactory subjectFactory = getSubjectFactory(securityDomain);
         Subject subject = null;

         if (subjectFactory != null)
            subject = createSubject(subjectFactory, securityDomain, mcf);

         pp.prefill(subject, null, false);
      }

      // ConnectionFactory
      return mcf.createConnectionFactory(cm);
   }

   /**
    * Deploy an XA datasource
    * @param ds The datasource
    * @param jndiName The JNDI name
    * @param uniqueId The unique id for the resource adapter
    * @param cma The connection manager array
    * @param recovery The recovery module
    * @param mgtDs The management of a datasource
    * @param cl The class loader
    * @return The connection factory
    * @exception Throwable Thrown if an error occurs during deployment
    */
   private Object deployXADataSource(XaDataSource ds, String jndiName, String uniqueId, ConnectionManager[] cma,
                                     XAResourceRecovery[] recovery,
                                     org.jboss.jca.core.api.management.DataSource mgtDs, ClassLoader cl)
      throws Throwable
   {
      ManagedConnectionFactory mcf = createMcf(ds, uniqueId, cl);

      initAndInjectClassLoaderPlugin(mcf, ds);
      // Create the pool
      PoolConfiguration pc = createPoolConfiguration(ds.getXaPool(), ds.getTimeOut(), ds.getValidation());

      Boolean noTxSeparatePool = Defaults.NO_TX_SEPARATE_POOL;

      if (ds.getXaPool() != null && ds.getXaPool().isNoTxSeparatePool() != null)
         noTxSeparatePool = ds.getXaPool().isNoTxSeparatePool();

      PoolFactory pf = new PoolFactory();
      PoolStrategy strategy = PoolStrategy.ONE_POOL;

      boolean allowMultipleUsers = false;
      if (ds.getXaPool() != null && ds.getXaPool() instanceof org.jboss.jca.common.api.metadata.ds.v11.DsXaPool)
      {
         org.jboss.jca.common.api.metadata.ds.v11.DsXaPool dsXaPool =
            (org.jboss.jca.common.api.metadata.ds.v11.DsXaPool)ds.getXaPool();

         if (dsXaPool.isAllowMultipleUsers() != null && dsXaPool.isAllowMultipleUsers().booleanValue())
         {
            strategy = PoolStrategy.POOL_BY_CRI;
            allowMultipleUsers = true;
         }
      }

      // Security
      String securityDomain = null;
      if (ds.getSecurity() != null)
      {
         if (ds.getSecurity().getReauthPlugin() != null)
         {
            strategy = PoolStrategy.REAUTH;
            securityDomain = ds.getSecurity().getSecurityDomain();
         }
         else if (ds.getSecurity().getSecurityDomain() != null)
         {
            if (!allowMultipleUsers)
            {
               strategy = PoolStrategy.POOL_BY_SUBJECT;
            }
            else
            {
               strategy = PoolStrategy.POOL_BY_SUBJECT_AND_CRI;
            }
            securityDomain = ds.getSecurity().getSecurityDomain();
         }
      }

      Pool pool = pf.create(strategy, mcf, pc, noTxSeparatePool.booleanValue());

      // Connection manager properties
      Integer allocationRetry = null;
      Long allocationRetryWaitMillis = null;
      Boolean interleaving = Defaults.INTERLEAVING;
      Integer xaResourceTimeout = null;
      Boolean isSameRMOverride = Defaults.IS_SAME_RM_OVERRIDE;
      Boolean wrapXAResource = Defaults.WRAP_XA_RESOURCE;
      Boolean padXid = Defaults.PAD_XID;

      if (ds.getTimeOut() != null)
      {
         allocationRetry = ds.getTimeOut().getAllocationRetry();
         allocationRetryWaitMillis = ds.getTimeOut().getAllocationRetryWaitMillis();
         xaResourceTimeout = ds.getTimeOut().getXaResourceTimeout();
      }

      if (ds.getXaPool() != null)
      {
         interleaving = ds.getXaPool().isInterleaving();
         isSameRMOverride = ds.getXaPool().isSameRmOverride();
         wrapXAResource = ds.getXaPool().isWrapXaResource();
         padXid = ds.getXaPool().isPadXid();
      }

      // Register data sources
      mgtDs.setJndiName(jndiName);
      mgtDs.setPoolConfiguration(pc);
      mgtDs.setPool(pool);

      if (mcf instanceof Statistics)
         mgtDs.setStatistics(((Statistics)mcf).getStatistics());

      log.debugf("Adding management datasource: %s", mgtDs);
      getManagementRepository().getDataSources().add(mgtDs);

      // Flush strategy
      FlushStrategy flushStrategy = FlushStrategy.FAILING_CONNECTION_ONLY;
      if (ds.getXaPool() != null)
         flushStrategy = ds.getXaPool().getFlushStrategy();

      // Select the correct connection manager
      TransactionSupportLevel tsl = TransactionSupportLevel.XATransaction;
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ConnectionManager cm =
         cmf.createTransactional(tsl, pool, getSubjectFactory(securityDomain), securityDomain,
                                 ds.isUseCcm(), getCachedConnectionManager(),
                                 flushStrategy,
                                 allocationRetry, allocationRetryWaitMillis,
                                 getTransactionIntegration(), interleaving,
                                 xaResourceTimeout, isSameRMOverride, wrapXAResource, padXid);

      cm.setJndiName(jndiName);
      cma[0] = cm;

      String poolName = null;
      if (ds.getPoolName() != null)
      {
         poolName = ds.getPoolName();
      }

      if (poolName == null)
         poolName = jndiName;

      pool.setName(poolName);

      // Spy
      if (ds.isSpy())
      {
         injectValue(mcf, "setSpy", Boolean.TRUE);
         injectValue(mcf, "setJndiName", jndiName);
      }

      // Reauth
      if (strategy == PoolStrategy.REAUTH)
      {
         injectValue(mcf, "setReauthEnabled", Boolean.TRUE);
         injectValue(mcf, "setReauthPluginClassName", ds.getSecurity().getReauthPlugin().getClassName());

         Map<String, String> mps = ds.getSecurity().getReauthPlugin().getConfigPropertiesMap();
         if (mps != null && mps.size() > 0)
         {
            StringBuilder reauthPluginProperties = new StringBuilder();

            Iterator<Map.Entry<String, String>> entryIterator = mps.entrySet().iterator();
            while (entryIterator.hasNext())
            {
               Map.Entry<String, String> entry = entryIterator.next();

               reauthPluginProperties.append(entry.getKey());
               reauthPluginProperties.append("|");
               reauthPluginProperties.append(entry.getValue());

               if (entryIterator.hasNext())
                  reauthPluginProperties.append(",");
            }

            injectValue(mcf, "setReauthPluginProperties", reauthPluginProperties.toString());
         }
      }

      Recovery recoveryMD = ds.getRecovery();
      String defaultSecurityDomain = null;
      String defaultUserName = null;
      String defaultPassword = null;

      if (ds.getSecurity() != null)
      {
         defaultSecurityDomain = ds.getSecurity().getSecurityDomain();
         defaultUserName = ds.getSecurity().getUserName();
         defaultPassword = ds.getSecurity().getPassword();
      }

      String recoverSecurityDomain = defaultSecurityDomain;
      String recoverUser = defaultUserName;
      String recoverPassword = defaultPassword;

      XAResourceRecovery recoveryImpl = null;

      if (recoveryMD == null || !recoveryMD.getNoRecovery())
      {
         // If we have an XAResourceRecoveryRegistry and the deployment is XA
         // lets register it for XA Resource Recovery using the "recover" definitions
         // from the -ds.xml file. Fallback to the standard definitions for
         // user name, password. Keep a seperate reference to the security-domain

         Credential credential = recoveryMD != null ? recoveryMD.getCredential() : null;
         if (credential != null)
         {
            recoverSecurityDomain = credential.getSecurityDomain();

            recoverUser = credential.getUserName();
            recoverPassword = credential.getPassword();
         }

         if (log.isDebugEnabled())
         {
            if (recoverUser != null)
            {
               log.debug("RecoverUser=" + recoverUser);
            }
            else if (recoverSecurityDomain != null)
            {
               log.debug("RecoverSecurityDomain=" + recoverSecurityDomain);
            }
         }

         RecoveryPlugin plugin = null;

         if (recoveryMD != null && recoveryMD.getRecoverPlugin() != null)
         {
            List<ConfigProperty> configProperties = null;
            if (recoveryMD.getRecoverPlugin().getConfigPropertiesMap() != null)
            {
               configProperties = new ArrayList<ConfigProperty>(recoveryMD.getRecoverPlugin()
                  .getConfigPropertiesMap().size());
               for (Entry<String, String> property : recoveryMD.getRecoverPlugin().getConfigPropertiesMap()
                  .entrySet())
               {
                  ConfigProperty c = new ConfigPropertyImpl(null,
                                                            new XsdString(property.getKey(), null),
                                                            new XsdString("String", null),
                                                            new XsdString(property.getValue(), null),
                                                            null);
                  configProperties.add(c);
               }

               plugin = (RecoveryPlugin) initAndInject(recoveryMD.getRecoverPlugin().getClassName(),
                  configProperties, cl);
            }
         }
         else
         {
            plugin = new DefaultRecoveryPlugin();
         }

         recoveryImpl =
            getTransactionIntegration().createXAResourceRecovery(mcf,
                                                                 padXid,
                                                                 isSameRMOverride,
                                                                 wrapXAResource,
                                                                 recoverUser,
                                                                 recoverPassword,
                                                                 recoverSecurityDomain,
                                                                 getSubjectFactory(recoverSecurityDomain),
                                                                 plugin);

      }

      if (getTransactionIntegration().getRecoveryRegistry() != null && recoveryImpl != null)
      {
         recoveryImpl.setJndiName(cm.getJndiName());
         getTransactionIntegration().getRecoveryRegistry().addXAResourceRecovery(recoveryImpl);

         recovery[0] = recoveryImpl;
      }

      // Prefill
      if (pool instanceof PrefillPool)
      {
         PrefillPool pp = (PrefillPool)pool;
         SubjectFactory subjectFactory = getSubjectFactory(securityDomain);
         Subject subject = null;

         if (subjectFactory != null)
            subject = createSubject(subjectFactory, securityDomain, mcf);

         pp.prefill(subject, null, noTxSeparatePool.booleanValue());
      }

      // ConnectionFactory
      return mcf.createConnectionFactory(cm);
   }

   /**
    * Create Mcf for xads
    *
    * @param ds the xsds
    * @param uniqueId the uniqueId
    * @param cl the classloader
    * @return the mcf
    * @throws NotFoundException in case it's not found in cl
    * @throws Exception in case of other errro
    * @throws DeployException in case of deoloy error
    */
   protected abstract ManagedConnectionFactory createMcf(XaDataSource ds, String uniqueId, ClassLoader cl)
      throws NotFoundException, Exception, DeployException;

   /**
    * Create Mcf for ds
    *
    * @param ds the xsds
    * @param uniqueId the uniqueId
    * @param cl the classloader
    * @return the mcf
    * @throws NotFoundException in case it's not found in cl
    * @throws Exception in case of other errro
    * @throws DeployException in case of deoloy error
    */
   protected abstract ManagedConnectionFactory createMcf(DataSource ds, String uniqueId, ClassLoader cl)
      throws NotFoundException, Exception, DeployException;

   /**
    * Create an instance of the pool configuration based on the input
    * @param pp The pool parameters
    * @param tp The timeout parameters
    * @param vp The validation parameters
    * @return The configuration
    */
   private PoolConfiguration createPoolConfiguration(CommonPool pp, CommonTimeOut tp, CommonValidation vp)
   {
      PoolConfiguration pc = new PoolConfiguration();

      if (pp != null)
      {
         if (pp.getMinPoolSize() != null)
            pc.setMinSize(pp.getMinPoolSize().intValue());

         if (pp.getMaxPoolSize() != null)
            pc.setMaxSize(pp.getMaxPoolSize().intValue());

         if (pp.isPrefill() != null)
            pc.setPrefill(pp.isPrefill());

         if (pp.isUseStrictMin() != null)
            pc.setStrictMin(pp.isUseStrictMin());
      }

      if (tp != null)
      {
         if (tp.getBlockingTimeoutMillis() != null)
            pc.setBlockingTimeout(tp.getBlockingTimeoutMillis().longValue());

         if (tp.getIdleTimeoutMinutes() != null)
            pc.setIdleTimeoutMinutes(tp.getIdleTimeoutMinutes().intValue());
      }

      if (vp != null)
      {
         if (vp.isBackgroundValidation() != null)
            pc.setBackgroundValidation(vp.isBackgroundValidation().booleanValue());

         if (vp.getBackgroundValidationMillis() != null)
            pc.setBackgroundValidationMillis(vp.getBackgroundValidationMillis().intValue());

         if (vp.isUseFastFail() != null)
            pc.setUseFastFail(vp.isUseFastFail());
      }

      return pc;
   }

   /**
    * Inject value
    * @param o The object
    * @param methodName The method name
    * @param value The value
    * @exception Exception Thrown in case of an error
    */
   private void injectValue(Object o, String methodName, Object value) throws Exception
   {
      // Method has to be public
      Method[] methods = o.getClass().getMethods();
      if (methods != null)
      {
         boolean found = false;
         for (int i = 0; !found && i < methods.length; i++)
         {
            Method m = methods[i];
            m.setAccessible(true);

            if (m.getName().equals(methodName) && m.getParameterTypes().length == 1)
            {
               m.invoke(o, value);
               found = true;
            }
         }
      }
   }

   /**
    * Provide the classloader of the deployment identified by the unique id
    * @param uniqueId The
    * @return The classloader used by this deployment
    */
   protected abstract ClassLoader getDeploymentClassLoader(String uniqueId);

   /**
    * Bind connection factory into JNDI
    * @param deployment The deployment name
    * @param cf The connection factory
    * @param jndi passed jndi name
    * @return The JNDI names bound
    * @exception Throwable Thrown if an error occurs
    */
   protected abstract String[] bindConnectionFactory(String deployment, String jndi, Object cf) throws Throwable;

   /**
    * Initialize and inject configuration properties
    * @param className The fully qualified class name
    * @param configs The configuration properties
    * @param cl The class loader
    * @return The object
    * @throws DeployException Thrown if the object cant be initialized
    */
   protected abstract Object initAndInject(String className, List<? extends ConfigProperty> configs, ClassLoader cl)
      throws DeployException;

   /**
    *
    * Initialize and inject class loader plugin
    *
    * @param mcf The managed connection factory
    * @param dsMetadata The dataSource metadata
    * @throws DeployException Thrown if the object cant be initialized or injected
    */
   protected void initAndInjectClassLoaderPlugin(ManagedConnectionFactory mcf, CommonDataSource dsMetadata)
      throws DeployException
   {
      //Default impl is doing nothing, delagating to MCF the default plugin instance creation
   }


   /**
    * Get a subject factory
    * @param securityDomain The security domain
    * @return The subject factory; must return <code>null</code> if security domain isn't defined
    * @exception DeployException Thrown if the security domain can't be resolved
    */
   protected abstract SubjectFactory getSubjectFactory(String securityDomain) throws DeployException;

   /**
    * Get the logger
    * @return The value
    */
   protected abstract DeployersLogger getLogger();

   /**
    * Create a subject
    * @param subjectFactory The subject factory
    * @param securityDomain The security domain
    * @param mcf The managed connection factory
    * @return The subject; <code>null</code> in case of an error
    */
   protected Subject createSubject(final SubjectFactory subjectFactory,
                                   final String securityDomain,
                                   final ManagedConnectionFactory mcf)
   {
      if (subjectFactory == null)
         throw new IllegalArgumentException("SubjectFactory is null");

      if (securityDomain == null)
         throw new IllegalArgumentException("SecurityDomain is null");

      return AccessController.doPrivileged(new PrivilegedAction<Subject>()
      {
         public Subject run()
         {
            try
            {
               Subject subject = subjectFactory.createSubject(securityDomain);

               Set<PasswordCredential> pcs = subject.getPrivateCredentials(PasswordCredential.class);
               if (pcs != null && pcs.size() > 0)
               {
                  for (PasswordCredential pc : pcs)
                  {
                     pc.setManagedConnectionFactory(mcf);
                  }
               }

               if (log.isDebugEnabled())
                  log.debug("Subject=" + subject);

               return subject;
            }
            catch (Throwable t)
            {
               log.error("Exception during createSubject()" + t.getMessage(), t);
            }

            return null;
         }
      });
   }
}
