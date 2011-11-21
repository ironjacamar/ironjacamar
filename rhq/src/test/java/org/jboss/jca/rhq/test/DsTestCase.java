/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.rhq.test;

import org.jboss.jca.core.api.connectionmanager.pool.Pool;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.api.connectionmanager.pool.PoolStatistics;
import org.jboss.jca.core.api.management.DataSource;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.embed.core.EmbeddedJcaDiscover;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.InitialContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.DataType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.pc.PluginContainer;
import org.rhq.core.pc.PluginContainerConfiguration;
import org.rhq.core.pc.inventory.InventoryManager;
import org.rhq.core.pc.inventory.RuntimeDiscoveryExecutor;
import org.rhq.core.pc.plugin.FileSystemPluginFinder;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import static org.junit.Assert.*;

/**
 * RHQ plugin test cases for Datasource
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>  
 */
public class DsTestCase
{
   
   /** RAR resource */
   private static Resource rarServiceResource;
   
   /** deployed url */
   private static URL deployedUrl;
   
   /** deployed ds */
   private static URL ds;
   
   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      assertEquals("java:/H2DS", rarServiceResource.getName());

   }
   
   /**
    * test Datasource LoadResourceConfiguration.
    * 
    * @throws Throwable exception
    */
   @Test
   public void testDsLoadResourceConfiguration() throws Throwable
   {
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      ConfigurationFacet configFacet = (ConfigurationFacet)im.getResourceComponent(rarServiceResource);
      Configuration config = configFacet.loadResourceConfiguration();
      
      assertEquals("java:/H2DS", config.getSimpleValue("jndi-name", null));
      assertFalse(Boolean.valueOf(config.getSimpleValue("xa", null)));
      
      assertEquals("H2DS", config.getSimpleValue("pool-name", null));
      testLoadPoolConfigurationIntialValue(config);
   }
   
   /**
    * Tests load PoolConfiguration initial value.
    * 
    * @param config RHQ Configuration of PoolConfiguration
    */
   protected static void testLoadPoolConfigurationIntialValue(Configuration config)
   {
      assertEquals("0", config.getSimpleValue("min-pool-size", null));
      assertEquals("20", config.getSimpleValue("max-pool-size", null));
      assertEquals("false", config.getSimpleValue("background-validation", null));
      assertEquals("0", config.getSimpleValue("background-validation-millis", null));
      assertEquals("30000", config.getSimpleValue("blocking-timeout-millis", null));
      assertEquals("30", config.getSimpleValue("idle-timeout-minutes", null));
      assertEquals("false", config.getSimpleValue("prefill", null));
      assertEquals("false", config.getSimpleValue("use-strict-min", null));
      assertEquals("false", config.getSimpleValue("use-fast-fail", null));
   }
   
   /**
    * test Datasource update ResourceConfiguration.
    * 
    * @throws Throwable exception
    */
   @Test
   public void testDsUpdateResourceConfiguration() throws Throwable
   {
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      ConfigurationFacet configFacet = (ConfigurationFacet)im.getResourceComponent(rarServiceResource);
      Configuration config = configFacet.loadResourceConfiguration();
      DataSource ds = getDataSource();
      PoolConfiguration poolConfig = ds.getPoolConfiguration();
      testUpdatePoolConfig(configFacet, config, poolConfig);
   }
   
   /**
    * Tests updates PoolConfiguration.
    * 
    * @param configFacet ConfigurationFacet
    * @param config Configuration
    * @param poolConfig PoolConfiguraton
    */
   protected static void testUpdatePoolConfig(ConfigurationFacet configFacet, Configuration config, 
         PoolConfiguration poolConfig)
   {
      int oldMinPoolSize = poolConfig.getMinSize();
      int oldMaxPoolSize = poolConfig.getMaxSize();
      boolean oldBackgroundValidation = poolConfig.isBackgroundValidation();
      long oldBackgroundValidationTime = poolConfig.getBackgroundValidationMillis();
      long oldBlockingTimeout = poolConfig.getBlockingTimeout();
      int oldIdleTimeout = poolConfig.getIdleTimeoutMinutes();
      boolean oldPreFill = poolConfig.isPrefill();
      boolean oldUseStrictMin = poolConfig.isStrictMin();
      boolean oldUseFastFail = poolConfig.isUseFastFail();
      
      int minPoolSize = 5;
      int maxPoolSize = 15;
      long backValidationTime = 30000;
      long blockingTimeout = 10000;
      int idleTimeOutMinutes = 15;
      
      config.put(new PropertySimple("min-pool-size", minPoolSize));
      config.put(new PropertySimple("max-pool-size", maxPoolSize));
      config.put(new PropertySimple("background-validation", true));
      config.put(new PropertySimple("background-validation-millis", backValidationTime));
      config.put(new PropertySimple("blocking-timeout-millis", blockingTimeout));
      config.put(new PropertySimple("idle-timeout-minutes", idleTimeOutMinutes));
      config.put(new PropertySimple("prefill", false));
      config.put(new PropertySimple("use-strict-min", true));
      config.put(new PropertySimple("use-fast-fail", true));
      
      ConfigurationUpdateReport updateConfigReport = new ConfigurationUpdateReport(config);
      configFacet.updateResourceConfiguration(updateConfigReport);
      
      assertEquals(minPoolSize, poolConfig.getMinSize());
      assertEquals(maxPoolSize, poolConfig.getMaxSize());
      assertTrue(poolConfig.isBackgroundValidation());
      assertEquals(backValidationTime, poolConfig.getBackgroundValidationMillis());
      assertEquals(blockingTimeout, poolConfig.getBlockingTimeout());
      assertEquals(idleTimeOutMinutes, poolConfig.getIdleTimeoutMinutes());
      assertFalse(poolConfig.isPrefill());
      assertTrue(poolConfig.isStrictMin());
      assertTrue(poolConfig.isUseFastFail());
      
      poolConfig.setBackgroundValidation(oldBackgroundValidation);
      poolConfig.setBackgroundValidationMillis(oldBackgroundValidationTime);
      poolConfig.setBlockingTimeout(oldBlockingTimeout);
      poolConfig.setIdleTimeoutMinutes(oldIdleTimeout);
      poolConfig.setMaxSize(oldMaxPoolSize);
      poolConfig.setMinSize(oldMinPoolSize);
      poolConfig.setPrefill(oldPreFill);
      poolConfig.setStrictMin(oldUseStrictMin);
      poolConfig.setUseFastFail(oldUseFastFail);
   }
   
   /**
    * Gets the associated DataSource
    * @return datasource
    */
   private DataSource getDataSource()
   {
      ManagementRepository manRepo = ManagementRepositoryManager.getManagementRepository();
      List<DataSource> datasources = manRepo.getDataSources();
      assertEquals(1, datasources.size());
      DataSource ds = datasources.get(0);
      return ds;
   }
   
   /**
    * Tests DsResourceComponent pool testConnection.
    * 
    * @throws Throwable the exception.
    */
   @Test
   public void testDsPoolOperationTestConnection() throws Throwable
   {
      testPoolOperationTestConnection(rarServiceResource);
   }
   
   /**
    * Tests pool testConnection.
    * 
    * @param poolRes Resource 
    * @throws Throwable the exception.
    */
   protected static void testPoolOperationTestConnection(Resource poolRes) throws Throwable
   {
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      OperationFacet cfOpertaionFacet = (OperationFacet)im.getResourceComponent(poolRes);
      OperationResult result = cfOpertaionFacet.invokeOperation("testConnection", null);
      assertNotNull(result);
      Configuration config = result.getComplexResults();
      assertEquals("true", config.getSimpleValue("result", null));
   }
 
   /**
    * test DataSource Pool.flush(boolean kill)
    * 
    * @throws Throwable exception
    */
   @Test
   public void testDsPoolFlush() throws Throwable
   {
      DataSource ds = getDataSource();
      Pool pool = ds.getPool();
      
      // set prefill to false
      boolean oldPrefill = ds.getPoolConfiguration().isPrefill();
      ds.getPoolConfiguration().setPrefill(false);
      PoolStatistics poolStatistics = pool.getStatistics();
      
      InitialContext context = new InitialContext();
      javax.sql.DataSource sqlDS = (javax.sql.DataSource)context.lookup(ds.getJndiName());
      Connection conn = sqlDS.getConnection();
      
      assertTrue(poolStatistics.getActiveCount() >= 1);
      
      pool.flush(true);  // it flushes all connections from the pool.
      
      assertEquals(0, poolStatistics.getActiveCount());
      assertEquals(0, poolStatistics.getCreatedCount());
      assertEquals(ds.getPoolConfiguration().getMaxSize(), poolStatistics.getAvailableCount());
      
      conn.close();
      
      conn = sqlDS.getConnection();
      pool.flush(); // only flushes idle connections
      assertTrue(poolStatistics.getActiveCount() >= 1);
      assertTrue(poolStatistics.getCreatedCount() >= 1);
      
      conn.close();
      pool.flush(); // conn is closed, so it will be released.
      assertTrue(poolStatistics.getActiveCount() >= 0);
      assertTrue(poolStatistics.getCreatedCount() >= 0);
      
      ds.getPoolConfiguration().setPrefill(oldPrefill);
   }
   
   
   /**
    * Tests DataSource metrics
    * 
    * @throws Throwable the exception
    */
   @Test
   public void testDsMetrics() throws Throwable
   {
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      MeasurementFacet facet = (MeasurementFacet)im.getResourceComponent(rarServiceResource);
      MeasurementReport report = new MeasurementReport();
      Set<MeasurementScheduleRequest> requests = new HashSet<MeasurementScheduleRequest>();
      DataType measurement = DataType.MEASUREMENT;
      MeasurementScheduleRequest request = new MeasurementScheduleRequest(0, "ActiveCount", 1, true, measurement);
      requests.add(request);
      
      String reqName = "PreparedStatementCacheAccessCount";
      request = new MeasurementScheduleRequest(0, reqName, 1, true, measurement);
      requests.add(request);
      
      InitialContext context = new InitialContext();
      javax.sql.DataSource sqlDS = (javax.sql.DataSource)context.lookup(getDataSource().getJndiName());
      Connection conn = sqlDS.getConnection();
      PreparedStatement pstmt = conn.prepareStatement("SHOW SCHEMAS");
      
      facet.getValues(report, requests);
      
      for (MeasurementDataNumeric data : report.getNumericData())
      {
         if (data.getName().equals("ActiveCount"))
         {
            assertTrue(data.getValue() >= 1);
         }
         else if (data.getName().equals(reqName))
         {
            assertTrue(data.getValue() >= 1);
         }
      }
      pstmt.close();
      conn.close();
   }
   
   
   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      File pluginDir = new File(System.getProperty("archives.dir"));
      PluginContainerConfiguration pcConfig = new PluginContainerConfiguration();
      pcConfig.setPluginFinder(new FileSystemPluginFinder(pluginDir));
      pcConfig.setPluginDirectory(pluginDir);
      pcConfig.setInsideAgent(false);

      PluginContainer pc = PluginContainer.getInstance();

      pc.setConfiguration(pcConfig);
      pc.initialize();
      
      EmbeddedJcaDiscover jca = EmbeddedJcaDiscover.getInstance();
      deployedUrl = DsTestCase.class.getResource("/jdbc-local.rar");
      jca.deploy(deployedUrl);
      ds = DsTestCase.class.getResource("/h2-ds.xml");
      jca.deploy(ds);
      
      InventoryManager im = pc.getInventoryManager();
      im.executeServerScanImmediately();

      Resource platformRes = im.getPlatform();
      Resource serverRes = platformRes.getChildResources().iterator().next();

      RuntimeDiscoveryExecutor discoverExecutor = new RuntimeDiscoveryExecutor(im, pcConfig, serverRes);
      discoverExecutor.run();

      rarServiceResource = serverRes.getChildResources().iterator().next();
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      EmbeddedJcaDiscover jca = EmbeddedJcaDiscover.getInstance();
      jca.undeploy(ds);
      jca.undeploy(deployedUrl);
      
      PluginContainer pc = PluginContainer.getInstance();
      pc.shutdown();
   }
}
