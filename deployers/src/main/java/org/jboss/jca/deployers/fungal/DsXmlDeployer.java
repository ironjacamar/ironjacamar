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

import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.metadata.MetadataFactory;
import org.jboss.jca.common.metadata.ds.DsParser;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.naming.ExplicitJndiStrategy;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.naming.JndiStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.util.Injection;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * The -ds.xml deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public final class DsXmlDeployer implements Deployer
{
   private static Logger log = Logger.getLogger(DsXmlDeployer.class);

   private static boolean trace = log.isTraceEnabled();

   private String jdbcLocal;

   private String jdbcXA;

   /** The transaction manager */
   private TransactionManager transactionManager;

   /** Metadata repository */
   private MetadataRepository mdr;

   /** The kernel */
   private Kernel kernel;

   /**
    * Constructor
    */
   public DsXmlDeployer()
   {
      transactionManager = null;
      jdbcLocal = null;
      jdbcXA = null;
      mdr = null;
      kernel = null;
   }

   /**
    * Set the name for the JDBC Local resource adapter
    * @param value The value
    */
   public void setJDBCLocal(String value)
   {
      jdbcLocal = value;
   }

   /**
    * Get the name for the JDBC Local resource adapter
    * @return The value
    */
   public String getJDBCLocal()
   {
      return jdbcLocal;
   }

   /**
    * Set the name for the JDBC XA resource adapter
    * @param value The value
    */
   public void setJDBCXA(String value)
   {
      jdbcXA = value;
   }

   /**
    * Get the name for the JDBC Xa resource adapter
    * @return The value
    */
   public String getJDBCXA()
   {
      return jdbcXA;
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
    * Set the kernel
    * @param value The value
    */
   public void setKernel(Kernel value)
   {
      kernel = value;
   }

   /**
    * Get the kernel
    * @return The handle
    */
   public Kernel getKernel()
   {
      return kernel;
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
      if (url == null || !(url.toExternalForm().endsWith("-ds.xml")))
         return null;

      log.debug("Deploying: " + url.toExternalForm());
      
      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      InputStream is = null;
      try
      {
         File f = new File(url.toURI());

         if (!f.exists())
            throw new IOException("Deployment " + url.toExternalForm() + " doesnt exists");

         is = new FileInputStream(f);
         DsParser dsParser = new DsParser();

         DataSources dataSources = dsParser.parse(is);

         String deploymentName = f.getName();

         Set<URL> raDeployments = mdr.getResourceAdapters();
         URL urlJdbcLocal = null;
         URL urlJdbcXA = null;

         for (URL u : raDeployments)
         {
            if (u.toExternalForm().endsWith(jdbcLocal))
            {
               urlJdbcLocal = u;
            }
            else if (u.toExternalForm().endsWith(jdbcXA))
            {
               urlJdbcXA = u;
            }
         }

         List<Object> cfs = new ArrayList<Object>(1);
         List<String> jndis = new ArrayList<String>(1);

         if (urlJdbcLocal != null)
         {
            List<DataSource> ds = dataSources.getDataSource();
            if (ds != null)
            {
               Deployment jdbcLocalDeployment = kernel.getDeployment(urlJdbcLocal);

               for (DataSource dataSource : ds)
               {
                  try
                  {
                     Object cf = deployDataSource(dataSource, urlJdbcLocal, jdbcLocalDeployment.getClassLoader());
                     String jndiName = dataSource.getJndiName();

                     if (!jndiName.startsWith("java:/"))
                        jndiName = "java:/" + jndiName;

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

         if (urlJdbcXA != null)
         {
            List<XaDataSource> xads = dataSources.getXaDataSource();
            if (xads != null)
            {
               Deployment jdbcXADeployment = kernel.getDeployment(urlJdbcXA);

               for (XaDataSource xaDataSource : xads)
               {
                  try
                  {
                     Object cf = deployXADataSource(xaDataSource, urlJdbcXA, jdbcXADeployment.getClassLoader());
                     String jndiName = xaDataSource.getJndiName();

                     if (!jndiName.startsWith("java:/"))
                        jndiName = "java:/" + jndiName;

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
         
         return new DsXmlDeployment(url, 
                                    deploymentName, 
                                    cfs.toArray(new Object[cfs.size()]),
                                    jndis.toArray(new String[jndis.size()]), 
                                    parent);
      }
      catch (DeployException de)
      {
         // Just rethrow
         throw de;
      }
      catch (Throwable t)
      {
         throw new DeployException("Deployment " + url.toExternalForm() + " failed", t);
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
    * Deploy a datasource
    * @param ds The datasource
    * @param ra The resource adapter
    * @param cl The class loader
    * @return The connection factory
    * @exception Throwable Thrown if an error occurs during deployment
    */
   private Object deployDataSource(DataSource ds, URL ra, ClassLoader cl) throws Throwable
   {
      log.debug("DataSource=" + ds);

      MetadataFactory metaDataFactory = new MetadataFactory();

      Connector md = mdr.getResourceAdapter(ra);
      md = metaDataFactory.mergeConnectorAndDs(ds, md);

      // Get the first connection definition as there is only one
      ResourceAdapter1516 ra1516 = (ResourceAdapter1516)md.getResourceadapter();
      List<ConnectionDefinition> cds = ra1516.getOutboundResourceadapter().getConnectionDefinitions();
      ConnectionDefinition cd = cds.get(0);

      // ManagedConnectionFactory
      ManagedConnectionFactory mcf =
         (ManagedConnectionFactory)initAndInject(cd.getManagedConnectionFactoryClass().getValue(),
                                                 cd.getConfigProperties(),
                                                 cl);
      // Create the pool
      Integer minSize = ds.getMinPoolSize();
      Integer maxSize = ds.getMaxPoolSize();
      Long blockingTimeout = ds.getTimeOut() != null ? ds.getTimeOut().getBlockingTimeoutMillis() : null;
      Long idleTimeout = ds.getTimeOut() != null ? ds.getTimeOut().getIdleTimeoutMinutes() : null;
      Long backgroundValidationInterval = null; // TODO
      Boolean prefill = ds.isPrefill();
      Boolean strictMin = null; // TODO
      Boolean useFastFail = ds.getValidation() != null ? ds.getValidation().isUseFastFail() : null;

      PoolConfiguration pc = createPoolConfiguration(minSize,
                                                     maxSize,
                                                     blockingTimeout, // TODO
                                                     idleTimeout,
                                                     backgroundValidationInterval,
                                                     prefill,
                                                     strictMin,
                                                     useFastFail);

      PoolFactory pf = new PoolFactory();
      Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true);

      // Select the correct connection manager
      TransactionSupportLevel tsl = TransactionSupportLevel.LocalTransaction;
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ConnectionManager cm = cmf.create(tsl, pool, getTransactionManager());

      // ConnectionFactory
      Object cf = mcf.createConnectionFactory(cm);
      
      return cf;
   }

   /**
    * Deploy an XA datasource
    * @param ds The datasource
    * @param ra The resource adapter
    * @param cl The class loader
    * @return The connection factory
    * @exception Throwable Thrown if an error occurs during deployment
    */
   private Object deployXADataSource(XaDataSource ds, URL ra, ClassLoader cl) throws Throwable
   {
      log.debug("XaDataSource=" + ds);

      MetadataFactory metaDataFactory = new MetadataFactory();

      Connector md = mdr.getResourceAdapter(ra);
      md = metaDataFactory.mergeConnectorAndDs(ds, md);

      // Get the first connection definition as there is only one
      ResourceAdapter1516 ra1516 = (ResourceAdapter1516)md.getResourceadapter();
      List<ConnectionDefinition> cds = ra1516.getOutboundResourceadapter().getConnectionDefinitions();
      ConnectionDefinition cd = cds.get(0);

      // ManagedConnectionFactory
      ManagedConnectionFactory mcf =
         (ManagedConnectionFactory)initAndInject(cd.getManagedConnectionFactoryClass().getValue(),
                                                 cd.getConfigProperties(),
                                                 cl);
      // Create the pool
      Integer minSize = ds.getMinPoolSize();
      Integer maxSize = ds.getMaxPoolSize();
      Long blockingTimeout = ds.getTimeOut() != null ? ds.getTimeOut().getBlockingTimeoutMillis() : null;
      Long idleTimeout = ds.getTimeOut() != null ? ds.getTimeOut().getIdleTimeoutMinutes() : null;
      Long backgroundValidationInterval = null; // TODO
      Boolean prefill = ds.isPrefill();
      Boolean strictMin = null; // TODO
      Boolean useFastFail = ds.getValidation() != null ? ds.getValidation().isUseFastFail() : null;

      PoolConfiguration pc = createPoolConfiguration(minSize,
                                                     maxSize,
                                                     blockingTimeout, // TODO
                                                     idleTimeout,
                                                     backgroundValidationInterval,
                                                     prefill,
                                                     strictMin,
                                                     useFastFail);

      PoolFactory pf = new PoolFactory();
      Pool pool = pf.create(PoolStrategy.ONE_POOL, mcf, pc, true);

      // Select the correct connection manager
      TransactionSupportLevel tsl = TransactionSupportLevel.LocalTransaction;
      ConnectionManagerFactory cmf = new ConnectionManagerFactory();
      ConnectionManager cm = cmf.create(tsl, pool, getTransactionManager());

      // ConnectionFactory
      Object cf = mcf.createConnectionFactory(cm);
      
      return cf;
   }

   /**
    * Create an instance of the pool configuration based on the input
    * @param minSize The min size
    * @param maxSize The max size
    * @param blockingTimeout The blocking timeout
    * @param idleTimeout The idle timeout
    * @param backgroundValidationInterval The background valdation interval
    * @param prefill The prefill
    * @param strictMin The strict min
    * @param useFastFail The use fast fail
    * @return The configuration
    */
   private PoolConfiguration createPoolConfiguration(Integer minSize,
                                                     Integer maxSize,
                                                     Long blockingTimeout,
                                                     Long idleTimeout,
                                                     Long backgroundValidationInterval,
                                                     Boolean prefill,
                                                     Boolean strictMin,
                                                     Boolean useFastFail)
   {
      PoolConfiguration pc = new PoolConfiguration();

      if (minSize != null)
         pc.setMinSize(minSize.intValue());

      if (maxSize != null)
         pc.setMaxSize(maxSize.intValue());

      if (blockingTimeout != null)
         pc.setBlockingTimeout(blockingTimeout.intValue()); // TODO - Long -> int

      if (idleTimeout != null)
         pc.setIdleTimeout(idleTimeout.longValue());

      if (backgroundValidationInterval != null)
         pc.setBackgroundValidationInterval(backgroundValidationInterval.longValue());

      if (prefill != null)
         pc.setPrefill(prefill.booleanValue());

      if (strictMin != null)
         pc.setStrictMin(strictMin.booleanValue());

      if (useFastFail != null)
         pc.setUseFastFail(useFastFail.booleanValue());

      return pc;
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
    * Bind connection factory into JNDI
    * @param deployment The deployment name
    * @param jndi The JNDI name
    * @param cf The connection factory
    * @return The JNDI names bound
    * @exception Exception thrown if an error occurs
    */
   private String[] bindConnectionFactory(String deployment, String jndi, Object cf) throws Throwable
   {
      JndiStrategy js = new ExplicitJndiStrategy();

      return js.bindConnectionFactories(deployment, new Object[] {cf}, new String[] {jndi});
   }

   /**
    * Start
    */
   public void start()
   {
      if (transactionManager == null)
         throw new IllegalStateException("TransactionManager not defined");

      if (mdr == null)
         throw new IllegalStateException("MetadataRepository not defined");

      if (kernel == null)
         throw new IllegalStateException("Kernel not defined");
   }
}
