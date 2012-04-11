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
import org.jboss.jca.common.metadata.ds.v11.DsParser;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.core.naming.ExplicitJndiStrategy;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.mdr.NotFoundException;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.deployers.DeployersLogger;
import org.jboss.jca.deployers.common.AbstractDsDeployer;
import org.jboss.jca.deployers.common.CommonDeployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;
import org.jboss.security.SubjectFactory;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.util.Injection;
import com.github.fungal.api.util.JMX;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;

/**
 * The -ds.xml deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public final class DsXmlDeployer extends AbstractDsDeployer implements Deployer
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

         CommonDeployment c = createObjectsAndInjectValue(url, deploymentName,
                                                          uniqueJdbcLocalId, uniqueJdbcXAId,
                                                          dataSources, parent);

         List<ObjectName> onames = registerManagementView(c.getDataSources(),
                                                          kernel.getMBeanServer(),
                                                          kernel.getName());

         return new DsXmlDeployment(c.getURL(), c.getDeploymentName(),
                                    c.getCfs(), c.getCfJndiNames(), c.getConnectionManagers(),
                                    c.getRecovery(), getXAResourceRecoveryRegistry(),
                                    c.getDataSources(), getManagementRepository(),
                                    onames, kernel.getMBeanServer(),
                                    c.getCl());
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

      if (kernel == null)
         throw new IllegalStateException("Kernel not defined");

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
         return kernel.getDeployment(urlJdbc).getClassLoader();
      }
      catch (Throwable t)
      {
         throw new RuntimeException(t.getMessage(), t);
      }
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
                     excludeAttributes.add("Names");
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
                  excludeAttributes.add("Names");
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
   protected ManagedConnectionFactory createMcf(XaDataSource ds, String uniqueId, ClassLoader cl)
      throws NotFoundException, Exception, DeployException
   {
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
      ResourceAdapter1516 ra1516 = (ResourceAdapter1516) md.getResourceadapter();
      List<ConnectionDefinition> cds = ra1516.getOutboundResourceadapter().getConnectionDefinitions();
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

}
