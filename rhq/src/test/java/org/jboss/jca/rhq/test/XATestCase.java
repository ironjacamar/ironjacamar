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

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.api.management.AdminObject;
import org.jboss.jca.core.api.management.ConfigProperty;
import org.jboss.jca.core.api.management.ConnectionFactory;
import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagedConnectionFactory;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.core.api.management.ResourceAdapter;

import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.embed.core.EmbeddedJcaDiscover;
import org.jboss.jca.rhq.rar.xa.XAAdminObjectImpl;
import org.jboss.jca.rhq.rar.xa.XAManagedConnectionFactory;
import org.jboss.jca.rhq.rar.xa.XAResourceAdapter;
import org.jboss.jca.rhq.util.ManagementRepositoryHelper;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.pc.PluginContainer;
import org.rhq.core.pc.PluginContainerConfiguration;
import org.rhq.core.pc.inventory.InventoryManager;
import org.rhq.core.pc.inventory.RuntimeDiscoveryExecutor;
import org.rhq.core.pc.plugin.FileSystemPluginFinder;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;

import static org.junit.Assert.*;

/**
 * RHQ plugin test cases for an XA resource adapter
 * 
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 */
public class XATestCase
{
   /** cf jndi name */
   private static final String CF_JNDI_NAME = "java:/eis/XA";
   
   /** RAR resource */
   private static Resource rarServiceResource;
   
   /** deployed url */
   private static URL deployedUrl;
   
   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      // only xa.rar is deployed
      assertEquals("xa.rar", rarServiceResource.getName());
      
      Set<Resource> subRarServiceRes = rarServiceResource.getChildResources();
      
      // connectionfactory, resource adapter and adminObject
      assertEquals(3, subRarServiceRes.size());
      for (Resource res : subRarServiceRes)
      {
         if (res.getName().equals("XAResourceAdapter"))
         {
            assertEquals("xa.rar#XAResourceAdapter", res.getResourceKey());
         }
         else if (res.getName().equals(CF_JNDI_NAME))
         {
            //assertEquals(1, res.getChildResources().size());
            Resource mcfRes = res.getChildResources().iterator().next();
            assertEquals("ManagedConnectionFactory", mcfRes.getName());
         }
         else if (res.getName().equals("java:/XAAdminObjectImpl"))
         {
            assertEquals("xa.rar#org.jboss.jca.rhq.rar.xa.XAAdminObjectImpl", res.getResourceKey());
         }
         else
         {
            throw new IllegalStateException("Unknown resource name: " + res.getName());
         }
      }
   }
   
   /**
    * Tests AoResourceComponent load resource configuration.
    * 
    * @throws Exception exception
    */
   @Test
   public void testLoadAoResourceConfiguration() throws Exception
   {
      Resource aoResource = null;
      for (Resource res : rarServiceResource.getChildResources())
      {
         if (res.getName().equals("java:/XAAdminObjectImpl"))
         {
            aoResource = res;
         }
      }
      assertNotNull(aoResource);
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      ConfigurationFacet configFacet = (ConfigurationFacet)im.getResourceComponent(aoResource);
      Configuration config = configFacet.loadResourceConfiguration();
      
      String aoJndiName = config.getSimpleValue("jndi-name", null);
      assertEquals("java:/XAAdminObjectImpl", aoJndiName);
      String aoCls = config.getSimpleValue("class-name", null);
      assertEquals("org.jboss.jca.rhq.rar.xa.XAAdminObjectImpl", aoCls);
      String aoIntfCls = config.getSimpleValue("interface-class-name", null);
      assertEquals("org.jboss.jca.rhq.rar.xa.XAAdminObjectInterface", aoIntfCls);
      assertEquals("true", config.getSimpleValue("use-ra-association", "null"));
      
      // config-properties
      PropertyList configPropList = config.getList("config-property");
      List<Property> configs = configPropList.getList();
      assertEquals(1, configs.size());
      PropertyMap aoConfigPropMap = (PropertyMap)configs.get(0);
      assertEquals("aoConfig", aoConfigPropMap.getSimpleValue("name", null));
      assertEquals("java.lang.String", aoConfigPropMap.getSimpleValue("type", null));
      assertEquals("ao-config", aoConfigPropMap.getSimpleValue("value", null));
   }
   
   /**
    * Tests AoResourceComponent update resource configuration.
    * 
    * @throws Exception exception
    */
   @Test
   public void testUpdateAoResourceComponentConfiguration() throws Exception
   {
      Resource aoResource = null;
      for (Resource res : rarServiceResource.getChildResources())
      {
         if (res.getName().equals("java:/XAAdminObjectImpl"))
         {
            aoResource = res;
         }
      }
      assertNotNull(aoResource);
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      ConfigurationFacet configFacet = (ConfigurationFacet)im.getResourceComponent(aoResource);
      Configuration config = configFacet.loadResourceConfiguration();
      PropertyList configPropList = config.getList("config-property");
      List<Property> configs = configPropList.getList();
      assertEquals(1, configs.size());
      PropertyMap aoConfigPropMap = (PropertyMap)configs.get(0);
      aoConfigPropMap.put(new PropertySimple("value", "new-ao-config"));
      ConfigurationUpdateReport updateConfigReport = new ConfigurationUpdateReport(config);
      configFacet.updateResourceConfiguration(updateConfigReport);
      assertEquals(ConfigurationUpdateStatus.SUCCESS, updateConfigReport.getStatus());
      
      ManagementRepository manRepo = ManagementRepositoryManager.getManagementRepository();
      Connector connector = ManagementRepositoryHelper.getConnectorByUniqueId(manRepo, "xa.rar");
      AdminObject ao = connector.getAdminObjects().get(0);
      XAAdminObjectImpl aoObj = (XAAdminObjectImpl)ao.getAdminObject();
      
      // not changed, because of not dynamic
      assertEquals("ao-config", aoObj.getAoConfig());
      
   }
   
   /**
    * Tests CfResourceComponent loadResourceConfiguration
    * 
    * @throws Exception exception
    */
   @Test
   public void testCfLoadResourceConfiguration() throws Exception
   {
      Resource cfResource = null;
      for (Resource res : rarServiceResource.getChildResources())
      {
         if (res.getName().equals(CF_JNDI_NAME))
         {
            cfResource = res;
         }
      }
      assertNotNull(cfResource);
      //assertEquals(1, cfResource.getChildResources().size());
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      
      // test cf loadConfiguration
      Resource cfRes = cfResource;
      ConfigurationFacet mcfConfigFacet = (ConfigurationFacet)im.getResourceComponent(cfRes);
      Configuration mcfConfig = mcfConfigFacet.loadResourceConfiguration();
      
      assertEquals("XA", mcfConfig.getSimpleValue("pool-name", null));
      assertEquals("java:/eis/XA", mcfConfig.getSimpleValue("jndi-name", null));

      assertEquals("0", mcfConfig.getSimpleValue("min-pool-size", null));
      assertEquals("20", mcfConfig.getSimpleValue("max-pool-size", null));
      assertEquals("false", mcfConfig.getSimpleValue("background-validation", null));
      assertEquals("0", mcfConfig.getSimpleValue("background-validation-millis", null));
      assertEquals("0", mcfConfig.getSimpleValue("background-validation-minutes", null));
      assertEquals("30000", mcfConfig.getSimpleValue("blocking-timeout-millis", null));
      assertEquals("30", mcfConfig.getSimpleValue("idle-timeout-minutes", null));
      assertEquals("false", mcfConfig.getSimpleValue("prefill", null));
      assertEquals("false", mcfConfig.getSimpleValue("use-strict-min", null));
      assertEquals("false", mcfConfig.getSimpleValue("use-fast-fail", null));
   }
   
   /**
    * Tests McfResourceComponent loadResourceConfiguration
    * 
    * @throws Exception exception
    */
   @Test
   public void testMcfLoadResourceConfiguration() throws Exception
   {
      Resource cfResource = null;
      for (Resource res : rarServiceResource.getChildResources())
      {
         if (res.getName().equals(CF_JNDI_NAME))
         {
            cfResource = res;
         }
      }
      assertNotNull(cfResource);
      //assertEquals(1, cfResource.getChildResources().size());
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      
      // test mcf loadConfiguration
      Resource mcfRes = cfResource.getChildResources().iterator().next();
      ConfigurationFacet mcfConfigFacet = (ConfigurationFacet)im.getResourceComponent(mcfRes);
      Configuration mcfConfig = mcfConfigFacet.loadResourceConfiguration();
      
      String mcfCls = mcfConfig.getSimpleValue("mcf-class-name", null);
      assertEquals("org.jboss.jca.rhq.rar.xa.XAManagedConnectionFactory", mcfCls);
      assertEquals("true", mcfConfig.getSimpleValue("use-ra-association", null));
      
      // config-properties
      PropertyList configPropList = mcfConfig.getList("config-property");
      List<Property> configs = configPropList.getList();
      assertEquals(1, configs.size());
      PropertyMap managementPropMap = (PropertyMap)configs.get(0);
      assertEquals("management", managementPropMap.getSimpleValue("name", null));
      assertEquals("java.lang.String", managementPropMap.getSimpleValue("type", null));
      assertEquals("rhq", managementPropMap.getSimpleValue("value", null));
   }
   
   /**
    * Tests CfResourceComponent update resource configuration.
    * 
    * @throws Exception exception
    */
   @Test
   public void testCfUpdateResourceConfinguration() throws Exception
   {
      Resource cfResource = null;
      for (Resource res : rarServiceResource.getChildResources())
      {
         if (res.getName().equals(CF_JNDI_NAME))
         {
            cfResource = res;
         }
      }
      assertNotNull(cfResource);
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      
      Resource cfRes = cfResource;
      ConfigurationFacet cfConfigFacet = (ConfigurationFacet)im.getResourceComponent(cfRes);
      Configuration cfConfig = cfConfigFacet.loadResourceConfiguration();
      
      // test cf updateConfiguration
      cfConfig.put(new PropertySimple("min-pool-size", 5));
      cfConfig.put(new PropertySimple("max-pool-size", 15));
      cfConfig.put(new PropertySimple("background-validation", true));
      cfConfig.put(new PropertySimple("background-validation-minutes", 30));
      cfConfig.put(new PropertySimple("blocking-timeout-millis", 10000));
      cfConfig.put(new PropertySimple("idle-timeout-minutes", 15));
      cfConfig.put(new PropertySimple("prefill", false));
      cfConfig.put(new PropertySimple("use-strict-min", true));
      cfConfig.put(new PropertySimple("use-fast-fail", true));
      
      ConfigurationUpdateReport updateConfigReport = new ConfigurationUpdateReport(cfConfig);
      cfConfigFacet.updateResourceConfiguration(updateConfigReport);
      
      ManagementRepository manRepo = ManagementRepositoryManager.getManagementRepository();
      Connector connector = ManagementRepositoryHelper.getConnectorByUniqueId(manRepo, "xa.rar");
      ConnectionFactory mcf = connector.getConnectionFactories().get(0);
      PoolConfiguration poolConfig = mcf.getPoolConfiguration();
      
      assertEquals(5, poolConfig.getMinSize());
      assertEquals(15, poolConfig.getMaxSize());
      assertTrue(poolConfig.isBackgroundValidation());
      assertEquals(30, poolConfig.getBackgroundValidationMinutes());
      assertEquals(10000, poolConfig.getBlockingTimeout());
      assertEquals(15 * 60 * 1000L, poolConfig.getIdleTimeout());
      assertFalse(poolConfig.isPrefill());
      assertTrue(poolConfig.isStrictMin());
      assertTrue(poolConfig.isUseFastFail());
   }
   
   /**
    * Tests McfResourceComponent update resource configuration.
    * 
    * @throws Exception exception
    */
   @Test
   public void testMcfUpdateResourceConfinguration() throws Exception
   {
      Resource cfResource = null;
      for (Resource res : rarServiceResource.getChildResources())
      {
         if (res.getName().equals(CF_JNDI_NAME))
         {
            cfResource = res;
         }
      }
      assertNotNull(cfResource);
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      
      Resource mcfRes = cfResource.getChildResources().iterator().next();
      ConfigurationFacet mcfConfigFacet = (ConfigurationFacet)im.getResourceComponent(mcfRes);
      Configuration mcfConfig = mcfConfigFacet.loadResourceConfiguration();
      
      // test mcf updateConfiguration
      
      PropertyList updateConfigPropList = new PropertyList("config-property");
      PropertyMap mcfConfigPropMap = new PropertyMap("config-property");
      PropertySimple mcfNameProp = new PropertySimple("name", "management");
      PropertySimple mcfTypeProp = new PropertySimple("type", "java.lang.String");
      PropertySimple mcfValueProp = new PropertySimple("value", "new-rhq");
      mcfConfigPropMap.put(mcfNameProp);
      mcfConfigPropMap.put(mcfTypeProp);
      mcfConfigPropMap.put(mcfValueProp);
      updateConfigPropList.add(mcfConfigPropMap);
      mcfConfig.put(updateConfigPropList);
      
      ConfigurationUpdateReport updateConfigReport = new ConfigurationUpdateReport(mcfConfig);
      mcfConfigFacet.updateResourceConfiguration(updateConfigReport);
      
      ManagementRepository manRepo = ManagementRepositoryManager.getManagementRepository();
      Connector connector = ManagementRepositoryHelper.getConnectorByUniqueId(manRepo, "xa.rar");
      ManagedConnectionFactory mcf = connector.getConnectionFactories().get(0).getManagedConnectionFactory();
      
      XAManagedConnectionFactory xaMcf = (XAManagedConnectionFactory)mcf.getManagedConnectionFactory();
      assertEquals("new-rhq", xaMcf.getManagement());
   }
   
   /**
    * Tests RaResourceComponent load resource configuration.
    * 
    * @throws Exception exception
    */
   @Test
   public void testRaLoadResourceCondiguration() throws Exception
   {
      Resource raResource = null;
      for (Resource res : rarServiceResource.getChildResources())
      {
         if (res.getName().equals("XAResourceAdapter"))
         {
            raResource = res;
         }
      }
      assertNotNull(raResource);
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      ConfigurationFacet configFacet = (ConfigurationFacet)im.getResourceComponent(raResource);
      Configuration config = configFacet.loadResourceConfiguration();
      assertEquals("org.jboss.jca.rhq.rar.xa.XAResourceAdapter", config.getSimpleValue("class-name", null));
      
      // config-properties
      PropertyList configPropList = config.getList("config-property");
      List<Property> configs = configPropList.getList();
      assertEquals(3, configs.size());
      for (Property prop : configs)
      {
         PropertyMap raConfigPropMap = (PropertyMap)prop;
         String propName = raConfigPropMap.getSimpleValue("name", null);
         String propType = raConfigPropMap.getSimpleValue("type", null);
         String propValue = raConfigPropMap.getSimpleValue("value", null);
         if (propName.equals("name"))
         {
            assertEquals("java.lang.String", propType);
            assertEquals("Jeff", propValue);
         }
         else if (propName.equals("password"))
         {
            assertEquals("java.lang.String", propType);
            assertEquals("Confidential", propValue);
         }
         else if (propName.equals("score"))
         {
            assertEquals("java.lang.Integer", propType);
            assertEquals("100", propValue);
         }
         else
         {
            throw new IllegalStateException("Unkown prop name: " + propName);
         }
      }
   }
   
   /**
    * Tests RaResourceComponent update resource component.
    * 
    * @throws Exception exception
    */
   @Test
   public void testRaUpdateResourceConfiguration() throws Exception
   {
      Resource raResource = null;
      for (Resource res : rarServiceResource.getChildResources())
      {
         if (res.getName().equals("XAResourceAdapter"))
         {
            raResource = res;
         }
      }
      assertNotNull(raResource);
      PluginContainer pc = PluginContainer.getInstance();
      InventoryManager im = pc.getInventoryManager();
      ConfigurationFacet configFacet = (ConfigurationFacet)im.getResourceComponent(raResource);
      Configuration config = configFacet.loadResourceConfiguration();
      PropertyList configPropList = new PropertyList("config-property");
      
      PropertyMap namePropMap = new PropertyMap("config-property");
      namePropMap.put(new PropertySimple("name", "name"));
      namePropMap.put(new PropertySimple("type", "java.lang.String"));
      namePropMap.put(new PropertySimple("value", "TangZhenni"));
      configPropList.add(namePropMap);
      
      PropertyMap scorePropMap = new PropertyMap("config-property");
      scorePropMap.put(new PropertySimple("name", "score"));
      scorePropMap.put(new PropertySimple("type", "java.lang.Integer"));
      scorePropMap.put(new PropertySimple("value", 99));
      configPropList.add(scorePropMap);
      
      PropertyMap passPropMap = new PropertyMap("config-property");
      passPropMap.put(new PropertySimple("name", "password"));
      passPropMap.put(new PropertySimple("type", "java.lang.String"));
      passPropMap.put(new PropertySimple("value", "123456"));
      configPropList.add(passPropMap);
      
      config.put(configPropList);
      
      ConfigurationUpdateReport updateConfigReport = new ConfigurationUpdateReport(config);
      configFacet.updateResourceConfiguration(updateConfigReport);
      
      assertEquals(ConfigurationUpdateStatus.SUCCESS, updateConfigReport.getStatus());
      
      ManagementRepository manRepo = ManagementRepositoryManager.getManagementRepository();
      Connector connector = ManagementRepositoryHelper.getConnectorByUniqueId(manRepo, "xa.rar");
      XAResourceAdapter ra = (XAResourceAdapter)connector.getResourceAdapter().getResourceAdapter();

      assertEquals("Jeff", ra.getName());
      assertEquals("Confidential", ra.getPassword());
      assertEquals(Integer.valueOf(99), ra.getScore());
      
   }
   
   /**
    * Tests ConfigProperties dynamic and confidential attributes in management model.
    * 
    */
   @Test
   public void testConfigProperiesDynamicAndConfidential()
   {
      ManagementRepository manRepo = EmbeddedJcaDiscover.getInstance().getManagementRepository();
      Connector xaConnector = manRepo.getConnectors().get(0);
      AdminObject ao = xaConnector.getAdminObjects().get(0);
      ManagedConnectionFactory mcf = xaConnector.getConnectionFactories().get(0).getManagedConnectionFactory();
      ResourceAdapter ra = xaConnector.getResourceAdapter();

      // ao-config 
      ConfigProperty aoConfig = ao.getConfigProperties().get(0);
      assertFalse(aoConfig.isConfidential());
      assertFalse(aoConfig.isDynamic());
      
      // management
      ConfigProperty managementConfig = mcf.getConfigProperties().get(0);
      assertTrue(managementConfig.isDynamic());
      assertFalse(managementConfig.isConfidential());
      
      // resource adapter
      for (ConfigProperty raConfig : ra.getConfigProperties())
      {
         if (raConfig.getName().equals("name"))
         {
            assertFalse(raConfig.isConfidential());
            assertFalse(raConfig.isDynamic());
         }
         else if (raConfig.getName().equals("password"))
         {
            assertTrue(raConfig.isConfidential());
            assertFalse(raConfig.isDynamic());
         }
         else if (raConfig.getName().equals("score"))
         {
            assertTrue(raConfig.isDynamic());
            assertFalse(raConfig.isConfidential());
         }
         else
         {
            throw new IllegalStateException("Unknown ConfigProperty: " + raConfig.getName());
         }
      }
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
      deployedUrl = XATestCase.class.getResource("/xa.rar");
      jca.deploy(deployedUrl);
      
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
      jca.undeploy(deployedUrl);
      
      PluginContainer pc = PluginContainer.getInstance();
      pc.shutdown();
   }
}
