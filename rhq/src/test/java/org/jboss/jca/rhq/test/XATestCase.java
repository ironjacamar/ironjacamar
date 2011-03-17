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
import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagedConnectionFactory;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.util.ManagementRepositoryHelper;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.rhq.core.domain.configuration.Configuration;
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
 */
public class XATestCase
{

   private PluginContainerConfiguration pcConfig;
   
   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      PluginContainer pc = PluginContainer.getInstance();
      
      InventoryManager im = pc.getInventoryManager();

      im.executeServerScanImmediately();
      
      Resource platformRes = im.getPlatform();
      assertNotNull(platformRes);
      
      assertEquals(1, platformRes.getChildResources().size());
      
      Resource serverRes = platformRes.getChildResources().iterator().next();
      
      RuntimeDiscoveryExecutor discoverExecutor = new RuntimeDiscoveryExecutor(im, pcConfig, serverRes);
      discoverExecutor.run();
      
      assertEquals(1, serverRes.getChildResources().size());
      Resource rarServiceRes = serverRes.getChildResources().iterator().next();
      
      // only xa.rar is deployed
      assertEquals("xa.rar", rarServiceRes.getName());
      
      Set<Resource> subRarServiceRes = rarServiceRes.getChildResources();
      
      // connectionfactory, resource adapter and adminObject
      assertEquals(3, subRarServiceRes.size());
      
      for (Resource res : subRarServiceRes)
      {
         ConfigurationFacet configFacet = (ConfigurationFacet)im.getResourceComponent(res);
         Configuration config = configFacet.loadResourceConfiguration();
         
         if (res.getName().equals("XAResourceAdapter"))
         {
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
         else if (res.getName().equals("ConnectionFactory"))
         {
            assertEquals(1, res.getChildResources().size());
            
            // test mcf loadConfiguration
            Resource mcfRes = res.getChildResources().iterator().next();
            ConfigurationFacet mcfConfigFacet = (ConfigurationFacet)im.getResourceComponent(mcfRes);
            Configuration mcfConfig = mcfConfigFacet.loadResourceConfiguration();
            
            assertEquals("XA", mcfConfig.getSimpleValue("pool-name", null));
//            assertEquals("", mcfConfig.getSimpleValue("jndi-name", null));
            String mcfCls = mcfConfig.getSimpleValue("mcf-class-name", null);
            assertEquals("org.jboss.jca.rhq.rar.xa.XAManagedConnectionFactory", mcfCls);
            assertEquals("true", mcfConfig.getSimpleValue("use-ra-association", null));
            assertEquals("0", mcfConfig.getSimpleValue("min-pool-size", null));
            assertEquals("20", mcfConfig.getSimpleValue("max-pool-size", null));
            assertEquals("false", mcfConfig.getSimpleValue("background-validation", null));
            assertEquals("0", mcfConfig.getSimpleValue("background-validation-millis", null));
            assertEquals("0", mcfConfig.getSimpleValue("background-validation-minutes", null));
            assertEquals("30000", mcfConfig.getSimpleValue("blocking-timeout-millis", null));
            assertEquals("30", mcfConfig.getSimpleValue("idle-timeout-minutes", null));
            assertEquals("true", mcfConfig.getSimpleValue("prefill", null));
            assertEquals("false", mcfConfig.getSimpleValue("use-strict-min", null));
            assertEquals("false", mcfConfig.getSimpleValue("use-fast-fail", null));
            
            // config-properties
            PropertyList configPropList = mcfConfig.getList("config-property");
            List<Property> configs = configPropList.getList();
            assertEquals(1, configs.size());
            PropertyMap managementPropMap = (PropertyMap)configs.get(0);
            assertEquals("management", managementPropMap.getSimpleValue("name", null));
            assertEquals("java.lang.String", managementPropMap.getSimpleValue("type", null));
            assertEquals("rhq", managementPropMap.getSimpleValue("value", null));
            
            // test mcf updateConfiguration
            mcfConfig.put(new PropertySimple("jndi-name", "TestMcfJndiName"));
            mcfConfig.put(new PropertySimple("min-pool-size", 5));
            mcfConfig.put(new PropertySimple("max-pool-size", 15));
            mcfConfig.put(new PropertySimple("background-validation", true));
            mcfConfig.put(new PropertySimple("background-validation-minutes", 30));
            mcfConfig.put(new PropertySimple("blocking-timeout-millis", 10000));
            mcfConfig.put(new PropertySimple("idle-timeout-minutes", 15));
            mcfConfig.put(new PropertySimple("prefill", false));
            mcfConfig.put(new PropertySimple("use-strict-min", true));
            mcfConfig.put(new PropertySimple("use-fast-fail", true));
            
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
            ManagedConnectionFactory mcf = connector.getManagedConnectionFactories().get(0);
            PoolConfiguration poolConfig = mcf.getPoolConfiguration();
            
            assertEquals("TestMcfJndiName", mcf.getJndiName());
            assertEquals(5, poolConfig.getMinSize());
            assertEquals(15, poolConfig.getMaxSize());
            assertTrue(poolConfig.isBackgroundValidation());
            assertEquals(30, poolConfig.getBackgroundValidationMinutes());
            assertEquals(10000, poolConfig.getBlockingTimeout());
            assertEquals(15 * 60 * 1000L, poolConfig.getIdleTimeout());
            assertFalse(poolConfig.isPrefill());
            assertTrue(poolConfig.isStrictMin());
            assertTrue(poolConfig.isUseFastFail());
//            XAManagedConnectionFactory xaMcf = (XAManagedConnectionFactory)mcf.getManagedConnectionFactory();
//            assertEquals("new-rhq", xaMcf.getManagement());
         }
         else if (res.getName().equals("java:/XAAdminObjectImpl"))
         {
            String aoJndiName = config.getSimpleValue("jndi-name", null);
            assertEquals("java:/XAAdminObjectImpl", aoJndiName);
            String aoCls = config.getSimpleValue("class-name", null);
            assertEquals("org.jboss.jca.rhq.rar.xa.XAAdminObjectImpl", aoCls);
            assertEquals("true", config.getSimpleValue("use-ra-association", "null"));
            
            // config-properties
//            PropertyList configPropList = config.getList("config-property");
//            List<Property> configs = configPropList.getList();
//            assertEquals(1, configs.size());
//            PropertyMap aoConfigPropMap = (PropertyMap)configs.get(0);
//            assertEquals("aoConfig", aoConfigPropMap.getSimpleValue("name", null));
//            assertEquals("java.lang.String", aoConfigPropMap.getSimpleValue("type", null));
//            assertEquals("ao-config", aoConfigPropMap.getSimpleValue("value", null));
            
            // test update AdminObject config-properties
            
//            aoConfigPropMap.put(new PropertySimple("value", "new-ao-config"));
//            
//            ConfigurationUpdateReport updateConfigReport = new ConfigurationUpdateReport(config);
//            configFacet.updateResourceConfiguration(updateConfigReport);
//            
//            assertEquals(ConfigurationUpdateStatus.SUCCESS, updateConfigReport.getStatus());
//            
//            ManagementRepository manRepo = ManagementRepositoryManager.getManagementRepository();
//            Connector connector = ManagementRepositoryHelper.getConnectorByUniqueId(manRepo, "xa.rar");
//            AdminObject ao = connector.getAdminObjects().get(0);
//            XAAdminObjectImpl aoObj = (XAAdminObjectImpl)ao.getAdminObject();
//            
//            assertEquals("new-ao-config", aoObj.getAoConfig());
            
            
         }
         else
         {
            throw new IllegalStateException("UnKnown resource name: " + res.getName());
         }
      }
      
   }

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @Before
   public void setUp() throws Throwable
   {
      File pluginDir = new File(System.getProperty("archives.dir"));
      pcConfig = new PluginContainerConfiguration();
      pcConfig.setPluginFinder(new FileSystemPluginFinder(pluginDir));
      pcConfig.setPluginDirectory(pluginDir);
      pcConfig.setInsideAgent(false);

      PluginContainer pc = PluginContainer.getInstance();

      pc.setConfiguration(pcConfig);
      pc.initialize();
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @After
   public void tearDown() throws Throwable
   {
      PluginContainer pc = PluginContainer.getInstance();
      pc.shutdown();
   }
}
