/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2010, Red Hat Inc, and individual contributors
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
import org.jboss.jca.common.api.metadata.ds.Driver;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.metadata.ds.DatasourcesImpl;
import org.jboss.jca.common.metadata.ds.DsParser;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.core.naming.ExplicitJndiStrategy;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.mdr.NotFoundException;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.deployers.DeployersLogger;
import org.jboss.jca.deployers.common.AbstractDsDeployer;
import org.jboss.jca.deployers.common.CommonDeployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.util.Injection;
import com.github.fungal.api.util.JMX;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * The -ds.xml deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DsXmlDeployer extends AbstractDsDeployer implements Deployer
{
   /** The logger */
   private static DeployersLogger log = Logger.getMessageLogger(DeployersLogger.class, DsXmlDeployer.class.getName());

   /** jdbcLocal **/
   private String jdbcLocal;

   /** jdbcXA **/
   private String jdbcXA;

   /** The kernel */
   private Kernel kernel;

   /** Metadata repository */
   private MetadataRepository mdr;

   /** ResourceAdapter repository */
   private ResourceAdapterRepository resourceAdapterRepository;

   /** Driver registry */
   private DriverRegistry driverRegistry;

   /**
    * Constructor
    */
   public DsXmlDeployer()
   {
      this.kernel = null;
      this.jdbcLocal = null;
      this.jdbcXA = null;
      this.mdr = null;
      this.resourceAdapterRepository = null;
      this.driverRegistry = null;
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
    * {@inheritDoc}
    */
   protected DeployersLogger getLogger()
   {
      return log;
   }

   /**
    * Set the driver registry
    * @param value The value
    */
   public void setDriverRegistry(DriverRegistry value)
   {
      driverRegistry = value;
   }

   /**
    * {@inheritDoc}
    */
   public boolean accepts(URL url)
   {
      if (url == null || url.toExternalForm().startsWith("jar") || !(url.toExternalForm().endsWith("-ds.xml")))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public int getOrder()
   {
      return Constants.DSXML_DEPLOYER;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized Deployment deploy(URL url, Context context, ClassLoader parent) throws DeployException
   {
      if (log.isDebugEnabled())
      {
         log.debug("Deploying: " + url.toExternalForm());
      }
      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      InputStream is = null;
      try
      {
         File f = getFileFromUrl(url);

         if (!f.exists())
            throw new IOException("Deployment " + url.toExternalForm() + " doesnt exists");

         is = new FileInputStream(f);
         DsParser dsParser = new DsParser();

         DataSources dataSources = dsParser.parse(is);

         String deploymentName = f.getName();

         Set<String> raDeployments = mdr.getResourceAdapters();
         String uniqueJdbcLocalId = null;
         String uniqueJdbcXAId = null;

         for (String s : raDeployments)
         {
            if (s.endsWith(jdbcLocal))
            {
               URL urlJdbcLocal = new URL(s);
               uniqueJdbcLocalId = urlJdbcLocal.toExternalForm();
            }
            else if (s.endsWith(jdbcXA))
            {
               URL urlJdbcXA = new URL(s);
               uniqueJdbcXAId = urlJdbcXA.toExternalForm();
            }
         }

         kernel = context.getKernel();

         if (numberOfDataSources(dataSources) == 1 || verifyTypes(dataSources))
         {
            CommonDeployment c = createObjectsAndInjectValue(url, deploymentName,
                                                             uniqueJdbcLocalId, uniqueJdbcXAId,
                                                             dataSources, parent);

            List<ObjectName> onames = registerManagementView(c.getDataSources(),
                                                             kernel.getMBeanServer(),
                                                             kernel.getName());

            return new DsXmlDeployment(c.getURL(), c.getDeploymentName(),
                                       c.getResourceAdapter(), c.getResourceAdapterKey(),
                                       c.getBootstrapContextIdentifier(),
                                       resourceAdapterRepository,
                                       c.getCfs(), c.getCfJndiNames(), c.getConnectionManagers(),
                                       c.getRecovery(), getXAResourceRecoveryRegistry(),
                                       c.getDataSources(), getManagementRepository(),
                                       onames, kernel.getMBeanServer(),
                                       c.getCl());
         }
         else
         {
            List<DsXmlDeployment> deployments = new ArrayList<DsXmlDeployment>();
            Map<String, Driver> driversMap = new HashMap<String, Driver>();
            for (Driver driver : dataSources.getDrivers())
            {
               driversMap.put(driver.getName(), driver);
            }

            for (DataSource ds : dataSources.getDataSource())
            {
               DataSources dsD = new DatasourcesImpl(Arrays.asList(ds), null, driversMap);
               CommonDeployment c = createObjectsAndInjectValue(url, deploymentName,
                                                                uniqueJdbcLocalId, null,
                                                                dsD, parent);

               List<ObjectName> onames = registerManagementView(c.getDataSources(),
                                                                kernel.getMBeanServer(),
                                                                kernel.getName());

               deployments.add(new DsXmlDeployment(c.getURL(), c.getDeploymentName(),
                                                   c.getResourceAdapter(), c.getResourceAdapterKey(),
                                                   c.getBootstrapContextIdentifier(),
                                                   resourceAdapterRepository,
                                                   c.getCfs(), c.getCfJndiNames(), c.getConnectionManagers(),
                                                   c.getRecovery(), getXAResourceRecoveryRegistry(),
                                                   c.getDataSources(), getManagementRepository(),
                                                   onames, kernel.getMBeanServer(),
                                                   c.getCl()));
            }

            for (XaDataSource xads : dataSources.getXaDataSource())
            {
               DataSources dsD = new DatasourcesImpl(null, Arrays.asList(xads), driversMap);
               CommonDeployment c = createObjectsAndInjectValue(url, deploymentName,
                                                                null, uniqueJdbcXAId,
                                                                dsD, parent);

               List<ObjectName> onames = registerManagementView(c.getDataSources(),
                                                                kernel.getMBeanServer(),
                                                                kernel.getName());

               deployments.add(new DsXmlDeployment(c.getURL(), c.getDeploymentName(),
                                                   c.getResourceAdapter(), c.getResourceAdapterKey(),
                                                   c.getBootstrapContextIdentifier(),
                                                   resourceAdapterRepository,
                                                   c.getCfs(), c.getCfJndiNames(), c.getConnectionManagers(),
                                                   c.getRecovery(), getXAResourceRecoveryRegistry(),
                                                   c.getDataSources(), getManagementRepository(),
                                                   onames, kernel.getMBeanServer(),
                                                   c.getCl()));
            }

            return new DsXmlDeployments(url, deployments, deployments.get(0).getClassLoader());
         }
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
         
         cleanTemporaryFiles(url);
         SecurityActions.setThreadContextClassLoader(oldTCCL);
      }
   }
   
   /**
     * Get File object of URL who locate xml resource
     * @param url url of the archive
     * @return a File object pointing of xml resource
     * @throws URISyntaxException Thrown if the resource adapter cant be found
     * @throws IOException Thrown if the resource adapter cant be accessible
     */
   protected File getFileFromUrl(URL url) throws URISyntaxException, IOException
   {
      return new File(url.toURI());
   }
    
   /**
     * Get IronJacamar Home directory
     * @return a String object
     */
   protected String getIronJacamarHome()
   {
      return SecurityActions.getSystemProperty("iron.jacamar.home");
   }
    
   /**
    * Clean all potential temporary files
    * @param url url of the archive
    * @throws DeployException Thrown if the resource cant be cleaned
    */
   protected void cleanTemporaryFiles(URL url) throws DeployException
   {
   }

   /**
    * Initialize and inject configuration properties
    * @param className The fully qualified class name
    * @param configs The configuration properties
    * @param cl The class loader
    * @return The object
    * @throws org.jboss.jca.deployers.common.DeployException Thrown if the object cant be initialized
    */
   @Override
   public Object initAndInject(String className, List<? extends ConfigProperty> configs, ClassLoader cl)
      throws org.jboss.jca.deployers.common.DeployException
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
                  injector.inject(o, cpmd.getConfigPropertyName().getValue(),
                                  cpmd.getConfigPropertyValue().getValue(), cpmd.getConfigPropertyType().getValue());
            }
         }

         return o;
      }
      catch (Throwable t)
      {
         throw new org.jboss.jca.deployers.common.DeployException("Deployment " + className + " failed", t);
      }
   }

   /**
    * Bind connection factory into JNDI
    * @param deployment The deployment name
    * @param jndi The JNDI name
    * @param cf The connection factory
    * @return The JNDI names bound
    * @throws Throwable thrown if an error occurs
    */
   @Override
   protected String[] bindConnectionFactory(String deployment, String jndi, Object cf) throws Throwable
   {
      JndiStrategy js = new ExplicitJndiStrategy();

      String[] result = js.bindConnectionFactories(deployment, new Object[]{cf}, new String[]{jndi});

      log.infof("Bound data source at: %s", jndi);

      return result;
   }

   /**
    * Get the driver
    * @param driverName The name of the driver
    * @param moduleId The id of the module
    * @return The driver class name; or <code>null</code> if not found
    */
   @Override
   protected String getDriver(String driverName, String moduleId)
   {
      String driver = driverRegistry.getDriver(driverName);

      if (driver == null)
         driver = driverRegistry.getDriver(moduleId);

      return driver;
   }

   /**
    * Start
    */
   public void start()
   {
      if (transactionIntegration == null)
         throw new IllegalStateException("TransactionIntegration not defined");

      if (mdr == null)
         throw new IllegalStateException("MetadataRepository not defined");

      if (resourceAdapterRepository == null)
         throw new IllegalStateException("ResourceAdapterRepository not defined");

      if (jdbcLocal == null)
         throw new IllegalStateException("JDBCLocal not defined");

      if (jdbcXA == null)
         throw new IllegalStateException("JDBCXA not defined");

      if (getCachedConnectionManager() == null)
         throw new IllegalStateException("CachedConnectionManager not defined");
   }

   @Override
   protected ClassLoader getDeploymentClassLoader(String uniqueId)
   {
      try
      {
         URL urlJdbc = new URL(uniqueId);
         List<Deployment> deployments = kernel.getDeployments(urlJdbc);
         Deployment deployment = null;

         if (deployments != null && deployments.size() > 0)
         {
            for (int i = 0; deployment == null && i < deployments.size(); i++)
            {
               deployment = deployments.get(i);
            }

            return deployment.getClassLoader();
         }
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t.getMessage(), t);
      }

      throw new RuntimeException("No classloader was found for: " + uniqueId);
   }

   @Override
   protected SubjectFactory getSubjectFactory(String securityDomain)
      throws org.jboss.jca.deployers.common.DeployException
   {
      log.tracef("getSubjectFactory(%s)", securityDomain);

      if (securityDomain == null || securityDomain.trim().equals(""))
         return null;

      try
      {
         return kernel.getBean(securityDomain, SubjectFactory.class);
      }
      catch (Throwable t)
      {
         throw new org.jboss.jca.deployers.common.DeployException("Error during loookup of security domain: " +
                                                                  securityDomain, t);
      }
   }

   /**
    * Register management view of datasources in JMX
    * @param mgtDses The management view of the datasources
    * @param server The MBeanServer instance
    * @param domain The management domain
    * @return The ObjectName's generated for these datasources
    * @exception JMException Thrown in case of an error
    */
   private List<ObjectName> registerManagementView(org.jboss.jca.core.api.management.DataSource[] mgtDses,
                                                   MBeanServer server, String domain)
      throws JMException
   {
      List<ObjectName> ons = null;

      if (mgtDses != null)
      {
         if (server != null)
         {
            ons = new ArrayList<ObjectName>();
            for (org.jboss.jca.core.api.management.DataSource mgtDs : mgtDses)
            {
               String jndiName = mgtDs.getJndiName();
               if (jndiName.indexOf("/") != -1)
                  jndiName = jndiName.substring(jndiName.lastIndexOf("/") + 1);

               String baseName = domain + ":deployment=" + jndiName;

               if (mgtDs.getPoolConfiguration() != null)
               {
                  String dsPCName = baseName + ",type=PoolConfigutation";

                  DynamicMBean dsPCDMB = JMX.createMBean(mgtDs.getPoolConfiguration(), "Pool configuration");
                  ObjectName dsPCON = new ObjectName(dsPCName);

                  server.registerMBean(dsPCDMB, dsPCON);

                  ons.add(dsPCON);
               }

               if (mgtDs.getPool() != null)
               {
                  String dsPName = baseName + ",type=Pool";

                  DynamicMBean dsPDMB = JMX.createMBean(mgtDs.getPool(), "Pool");
                  ObjectName dsPON = new ObjectName(dsPName);

                  server.registerMBean(dsPDMB, dsPON);

                  ons.add(dsPON);

                  if (mgtDs.getPool().getStatistics() != null)
                  {
                     String dsPSName = baseName + ",type=PoolStatistics";

                     Set<String> writeAttributes = new HashSet<String>();
                     writeAttributes.add("Enabled");
                     Set<String> excludeAttributes = new HashSet<String>();
                     Set<String> excludeOperations = new HashSet<String>();
                     excludeOperations.add("delta(.)*");

                     DynamicMBean dsPSDMB = JMX.createMBean(mgtDs.getPool().getStatistics(), "PoolStatistics",
                                                            writeAttributes, null,
                                                            excludeAttributes, excludeOperations);
                     ObjectName dsPSON = new ObjectName(dsPSName);

                     server.registerMBean(dsPSDMB, dsPSON);

                     ons.add(dsPSON);
                  }
               }

               if (mgtDs.getStatistics() != null)
               {
                  String dsSName = baseName + ",type=Statistics";
                  Set<String> writeAttributes = new HashSet<String>();
                  writeAttributes.add("Enabled");
                  Set<String> excludeAttributes = new HashSet<String>();
                  Set<String> excludeOperations = new HashSet<String>();
                  excludeOperations.add("delta(.)*");

                  DynamicMBean dsSDMB = JMX.createMBean(mgtDs.getStatistics(), "Statistics",
                                                        writeAttributes, null, excludeAttributes, excludeOperations);
                  ObjectName dsSON = new ObjectName(dsSName);

                  server.registerMBean(dsSDMB, dsSON);
                  
                  ons.add(dsSON);
               }
            }
         }
      }

      return ons;
   }

   @Override
   protected javax.resource.spi.ResourceAdapter createRa(String uniqueId, ClassLoader cl)
      throws NotFoundException, Exception, DeployException
   {
      Connector md = mdr.getResourceAdapter(uniqueId);
      ResourceAdapter ra = md.getResourceadapter();
      List<? extends ConfigProperty> l = new ArrayList<ConfigProperty>();

      javax.resource.spi.ResourceAdapter rar =
         (javax.resource.spi.ResourceAdapter)initAndInject(ra.getResourceadapterClass(), l, cl);

      return rar;
   }

   @Override
   protected String registerResourceAdapterToResourceAdapterRepository(javax.resource.spi.ResourceAdapter instance)
   {
      return getResourceAdapterRepository().registerResourceAdapter(instance);
   }

   @Override
   protected ManagedConnectionFactory createMcf(XaDataSource ds, String uniqueId, ClassLoader cl)
      throws NotFoundException, Exception, DeployException
   {
      Merger merger = new Merger();

      Connector md = mdr.getResourceAdapter(uniqueId);
      md = merger.mergeConnectorAndDs(ds, md);
      // Get the first connection definition as there is only one
      ResourceAdapter ra = md.getResourceadapter();
      List<ConnectionDefinition> cds = ra.getOutboundResourceadapter().getConnectionDefinitions();
      ConnectionDefinition cd = cds.get(0);

      // ManagedConnectionFactory
      ManagedConnectionFactory mcf = (ManagedConnectionFactory) initAndInject(cd.getManagedConnectionFactoryClass()
         .getValue(), cd.getConfigProperties(), cl);
      initAndInjectClassLoaderPlugin(mcf, ds);
      return mcf;
   }

   @Override
   protected ManagedConnectionFactory createMcf(DataSource ds, String uniqueId, ClassLoader cl)
      throws NotFoundException, Exception, DeployException
   {
      Merger merger = new Merger();

      Connector md = mdr.getResourceAdapter(uniqueId);
      md = merger.mergeConnectorAndDs(ds, md);

      // Get the first connection definition as there is only one
      ResourceAdapter ra = md.getResourceadapter();
      List<ConnectionDefinition> cds = ra.getOutboundResourceadapter().getConnectionDefinitions();
      ConnectionDefinition cd = cds.get(0);

      // ManagedConnectionFactory
      ManagedConnectionFactory mcf = (ManagedConnectionFactory) initAndInject(cd.getManagedConnectionFactoryClass()
         .getValue(), cd.getConfigProperties(), cl);
      initAndInjectClassLoaderPlugin(mcf, ds);
      return mcf;
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
    * Set the resource adapter repository
    * @param value The value
    */
   public void setResourceAdapterRepository(ResourceAdapterRepository value)
   {
      resourceAdapterRepository = value;
   }

   /**
    * Get the resource adapter repository
    * @return The handle
    */
   public ResourceAdapterRepository getResourceAdapterRepository()
   {
      return resourceAdapterRepository;
   }
}
