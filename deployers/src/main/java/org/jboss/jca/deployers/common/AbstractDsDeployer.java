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

import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.connectionmanager.xa.XAResourceRecoveryImpl;
import org.jboss.jca.core.spi.mdr.MetadataRepository;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.security.SubjectFactory;
import org.jboss.tm.XAResourceRecoveryRegistry;

/**
 * An abstract deployer implementation for datasources
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractDsDeployer
{
   /** log **/
   protected Logger log;

   /** The transaction manager */
   protected TransactionManager transactionManager;

   /** Metadata repository */
   protected MetadataRepository mdr;

   /** xaResourceRecoveryRegistry */
   protected XAResourceRecoveryRegistry xaResourceRecoveryRegistry;

   /**
    * Create a new AbstractDsDeployer.
    * @param log The logger
    */
   public AbstractDsDeployer(Logger log)
   {
      this.log = log;
      this.transactionManager = null;
      this.mdr = null;
   }

   /**
    * Set the transaction manager
    * @param value The value
    */
   public void setTransactionManager(TransactionManager value)
   {
      transactionManager = value;
   }

   /**
    * Get the transaction manager
    * @return The value
    */
   public TransactionManager getTransactionManager()
   {
      return transactionManager;
   }

   /**
    * Set the metadata repository
    * @param value The value
    */
   public void setMetadataRepository(MetadataRepository value)
   {
      mdr = value;
   }

   /**
    * Get the metadata repository
    * @return The handle
    */
   public MetadataRepository getMetadataRepository()
   {
      return mdr;
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

         if (uniqueJdbcLocalId != null)
         {
            List<DataSource> ds = dataSources.getDataSource();
            if (ds != null)
            {
               ClassLoader jdbcLocalDeploymentCl = getDeploymentClassLoader(uniqueJdbcLocalId);

               for (DataSource dataSource : ds)
               {
                  try
                  {
                     String jndiName = dataSource.getJndiName();

                     if (dataSource.isUseJavaContext() != null && dataSource.isUseJavaContext().booleanValue() &&
                         !jndiName.startsWith("java:/"))
                     {
                        jndiName = "java:/" + jndiName;
                     }

                     Object cf = deployDataSource(dataSource, jndiName, uniqueJdbcLocalId, jdbcLocalDeploymentCl);

                     bindConnectionFactory(deploymentName, jndiName, cf);

                     cfs.add(cf);
                     jndis.add(jndiName);
                  }
                  catch (Throwable t)
                  {
                     log.error("Error during the deployment of " + dataSource.getJndiName(), t);
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
                  try
                  {
                     String jndiName = xaDataSource.getJndiName();

                     if (xaDataSource.isUseJavaContext() != null && xaDataSource.isUseJavaContext().booleanValue() &&
                         !jndiName.startsWith("java:/"))
                     {
                        jndiName = "java:/" + jndiName;
                     }

                     Object cf = deployXADataSource(xaDataSource, jndiName, uniqueJdbcXAId, jdbcXADeploymentCl);

                     bindConnectionFactory(deploymentName, jndiName, cf);

                     cfs.add(cf);
                     jndis.add(jndiName);
                  }
                  catch (Throwable t)
                  {
                     log.error("Error during the deployment of " + xaDataSource.getJndiName(), t);
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
                                     jndis.toArray(new String[jndis.size()]), null, null, null, parentClassLoader,
                                     log);
      }
      catch (Throwable t)
      {
         throw new DeployException("Deployment " + url.toExternalForm() + " failed", t);
      }
   }

   /**
    * Deploy a datasource
    * @param ds The datasource
    * @param jndiName The JNDI name
    * @param uniqueId The unique id for the resource adapter
    * @param cl The class loader
    * @return The connection factory
    * @exception Throwable Thrown if an error occurs during deployment
    */
   private Object deployDataSource(DataSource ds, String jndiName, String uniqueId, ClassLoader cl) throws Throwable
   {
      log.debug("DataSource=" + ds);

      Merger merger = new Merger();

      Connector md = mdr.getResourceAdapter(uniqueId);
      md = merger.mergeConnectorAndDs(ds, md);

      // Get the first connection definition as there is only one
      ResourceAdapter1516 ra1516 = (ResourceAdapter1516) md.getResourceadapter();
      List<ConnectionDefinition> cds = ra1516.getOutboundResourceadapter().getConnectionDefinitions();
      ConnectionDefinition cd = cds.get(0);

      // ManagedConnectionFactory
      ManagedConnectionFactory mcf = (ManagedConnectionFactory) initAndInject(cd.getManagedConnectionFactoryClass()
         .getValue(), cd.getConfigProperties(), cl);
      // Create the pool
      PoolConfiguration pc = createPoolConfiguration(ds.getPool(), ds.getTimeOut(), ds.getValidation());

      PoolFactory pf = new PoolFactory();
      PoolStrategy strategy = PoolStrategy.ONE_POOL;

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
            strategy = PoolStrategy.POOL_BY_SUBJECT;
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

      // Select the correct connection manager
      TransactionSupportLevel tsl = TransactionSupportLevel.LocalTransaction;
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ConnectionManager cm =
         cmf.createTransactional(tsl, pool, getSubjectFactory(securityDomain), securityDomain,
                                 allocationRetry, allocationRetryWaitMillis,
                                 getTransactionManager(), null, null, null, null, null);

      cm.setJndiName(jndiName);

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

      // ConnectionFactory
      return mcf.createConnectionFactory(cm);
   }

   /**
    * Deploy an XA datasource
    * @param ds The datasource
    * @param jndiName The JNDI name
    * @param uniqueId The unique id for the resource adapter
    * @param cl The class loader
    * @return The connection factory
    * @exception Throwable Thrown if an error occurs during deployment
    */
   private Object deployXADataSource(XaDataSource ds, String jndiName, String uniqueId, ClassLoader cl)
      throws Throwable
   {
      log.debug("XaDataSource=" + ds);

      Merger merger = new Merger();

      Connector md = mdr.getResourceAdapter(uniqueId);
      md = merger.mergeConnectorAndDs(ds, md);

      // Get the first connection definition as there is only one
      ResourceAdapter1516 ra1516 = (ResourceAdapter1516) md.getResourceadapter();
      List<ConnectionDefinition> cds = ra1516.getOutboundResourceadapter().getConnectionDefinitions();
      ConnectionDefinition cd = cds.get(0);

      // ManagedConnectionFactory
      ManagedConnectionFactory mcf = (ManagedConnectionFactory) initAndInject(cd.getManagedConnectionFactoryClass()
         .getValue(), cd.getConfigProperties(), cl);
      // Create the pool
      PoolConfiguration pc = createPoolConfiguration(ds.getXaPool(), ds.getTimeOut(), ds.getValidation());

      Boolean noTxSeparatePool = Boolean.FALSE;

      if (ds.getXaPool() != null && ds.getXaPool().isNoTxSeparatePool() != null)
         noTxSeparatePool = ds.getXaPool().isNoTxSeparatePool();

      PoolFactory pf = new PoolFactory();
      PoolStrategy strategy = PoolStrategy.ONE_POOL;

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
            strategy = PoolStrategy.POOL_BY_SUBJECT;
            securityDomain = ds.getSecurity().getSecurityDomain();
         }
      }

      Pool pool = pf.create(strategy, mcf, pc, noTxSeparatePool.booleanValue());

      // Connection manager properties
      Integer allocationRetry = null;
      Long allocationRetryWaitMillis = null;
      Boolean interleaving = null;
      Integer xaResourceTimeout = null;
      Boolean isSameRMOverride = null;
      Boolean wrapXAResource = null;
      Boolean padXid = null;

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
         wrapXAResource = ds.getXaPool().isWrapXaDataSource();
         padXid = ds.getXaPool().isPadXid();
      }

      // Select the correct connection manager
      TransactionSupportLevel tsl = TransactionSupportLevel.XATransaction;
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ConnectionManager cm =
         cmf.createTransactional(tsl, pool, getSubjectFactory(securityDomain), securityDomain,
                                 allocationRetry, allocationRetryWaitMillis,
                                 getTransactionManager(), interleaving,
                                 xaResourceTimeout, isSameRMOverride, wrapXAResource, padXid);

      cm.setJndiName(jndiName);

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
      XAResourceRecoveryImpl resourceRecovery = null;
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
         resourceRecovery = new XAResourceRecoveryImpl(
                                                       mcf,
                                                       padXid,
                                                       isSameRMOverride,
                                                       wrapXAResource,
                                                       recoverUser,
                                                       recoverPassword,
                                                       recoverSecurityDomain,
                                                       null);

      }

      if (getXAResourceRecoveryRegistry() != null && resourceRecovery != null)
      {
         resourceRecovery.registerXaRecovery(getXAResourceRecoveryRegistry(),
            cm.getJndiName());
      }
      // ConnectionFactory
      return mcf.createConnectionFactory(cm);
   }

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
            pc.setIdleTimeout(tp.getIdleTimeoutMinutes().longValue());
      }

      if (vp != null)
      {
         if (vp.isBackgroundValidation() != null)
            pc.setBackgroundValidation(vp.isBackgroundValidation().booleanValue());

         if (vp.getBackgroundValidationMinutes() != null)
            pc.setBackgroundValidationMinutes(vp.getBackgroundValidationMinutes().intValue());

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
    * Get a subject factory
    * @param securityDomain The security domain
    * @return The subject factory; must return <code>null</code> if security domain isn't defined
    * @exception DeployException Thrown if the security domain can't be resolved
    */
   protected abstract SubjectFactory getSubjectFactory(String securityDomain) throws DeployException;

   /** Get the xAResourceRecoveryRegistry.
    *
    * @return the xAResourceRecoveryRegistry.
    */
   public final XAResourceRecoveryRegistry getXAResourceRecoveryRegistry()
   {
      return xaResourceRecoveryRegistry;
   }

   /**
    * Set the xAResourceRecoveryRegistry.
    *
    * @param xAResourceRecoveryRegistry The xAResourceRecoveryRegistry to set.
    */
   public final void setXAResourceRecoveryRegistry(XAResourceRecoveryRegistry xAResourceRecoveryRegistry)
   {
      xaResourceRecoveryRegistry = xAResourceRecoveryRegistry;
   }
}
