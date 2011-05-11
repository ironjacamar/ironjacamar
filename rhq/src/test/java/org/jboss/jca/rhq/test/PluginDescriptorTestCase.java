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

import org.jboss.jca.rhq.ds.DsResourceComponent;
import org.jboss.jca.rhq.ds.DsResourceDiscoveryComponent;
import org.jboss.jca.rhq.ra.AoResourceComponent;
import org.jboss.jca.rhq.ra.AoResourceDiscoveryComponent;
import org.jboss.jca.rhq.ra.CfResourceComponent;
import org.jboss.jca.rhq.ra.CfResourceDiscoveryComponent;
import org.jboss.jca.rhq.ra.McfResourceComponent;
import org.jboss.jca.rhq.ra.McfResourceDiscoveryComponent;
import org.jboss.jca.rhq.ra.RaResourceComponent;
import org.jboss.jca.rhq.ra.RaResourceDiscoveryComponent;
import org.jboss.jca.rhq.ra.RarResourceComponent;
import org.jboss.jca.rhq.ra.RarResourceDiscoveryComponent;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import org.rhq.core.clientapi.descriptor.configuration.ConfigurationDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.PluginDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.ResourceCreateDeletePolicy;
import org.rhq.core.clientapi.descriptor.plugin.ResourceCreationData;
import org.rhq.core.clientapi.descriptor.plugin.ServerDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.ServiceDescriptor;
import org.rhq.core.pc.plugin.PluginDescriptorLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test cases on RHQ plugin descriptor validation.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class PluginDescriptorTestCase
{

   
   /**
    * Check plugin descriptor of syntax and structure
    * 
    * @throws Throwable if there is an error on the plug-in descriptor, like syntax error.
    */
   @Test
   public void testValidatePluginDescriptor() throws Throwable
   {
      File pluginDir = new File(System.getProperty("archives.dir"));
      File pluginFile = new File(pluginDir, "ironjacamar-rhq-test-all.jar");
      URL pluginURL = pluginFile.toURI().toURL();
      PluginDescriptorLoader pluginLoader = new PluginDescriptorLoader(pluginURL, getClass().getClassLoader());
      
      // check syntax 
      PluginDescriptor pluginDesc = pluginLoader.loadPluginDescriptor();
      assertNotNull(pluginDesc);
      
      // check name and displayName
      assertEquals("IronJacamar Plugin", pluginDesc.getDisplayName());
      assertEquals("IronJacamar", pluginDesc.getName());
      
      // check server/service structure
      List<ServerDescriptor> servers = pluginDesc.getServers();
      assertEquals(1, servers.size());
      
      ServerDescriptor serverDesc = servers.get(0);
      assertEquals("IronJacamar (JCA)", serverDesc.getName());
      
      List<ServiceDescriptor> services = serverDesc.getServices();
      assertEquals(2, services.size());
      
      for (ServiceDescriptor serviceDesc : services)
      {
         if (serviceDesc.getName().equals("Resource Adapter Archive (RAR)"))
         {
            assertEquals(RarResourceDiscoveryComponent.class.getName(), serviceDesc.getDiscovery());
            assertEquals(RarResourceComponent.class.getName(), serviceDesc.getClazz());
            assertEquals(ResourceCreateDeletePolicy.BOTH, serviceDesc.getCreateDeletePolicy());
            assertEquals(ResourceCreationData.CONTENT, serviceDesc.getCreationDataType());
            
            // RAR service has the plugin configuration
            ConfigurationDescriptor pluginConfDesc = serviceDesc.getPluginConfiguration();
            assertNotNull(pluginConfDesc);
            
            // RAR service has resource configuration
            ConfigurationDescriptor resConfDesc = serviceDesc.getResourceConfiguration();
            assertNotNull(resConfDesc);
            
            // 3 sub services in RAR service
            List<ServiceDescriptor> subServiceDesc = serviceDesc.getServices();
            assertEquals(3, subServiceDesc.size());
            
            // test discovery and class definitions
            for (ServiceDescriptor sd : subServiceDesc)
            {
               if (sd.getName().equals("Resource Adapter"))
               {
                  assertEquals(RaResourceDiscoveryComponent.class.getName(), sd.getDiscovery());
                  assertEquals(RaResourceComponent.class.getName(), sd.getClazz());
               }
               else if (sd.getName().equals("Connection Factory"))
               {
                  assertEquals(CfResourceDiscoveryComponent.class.getName(), sd.getDiscovery());
                  assertEquals(CfResourceComponent.class.getName(), sd.getClazz());
                  List<ServiceDescriptor> cfSubServices = sd.getServices();
                  assertEquals(1, cfSubServices.size());
                  ServiceDescriptor mcfServiceDescriptor = cfSubServices.get(0);
                  assertEquals(McfResourceDiscoveryComponent.class.getName(), mcfServiceDescriptor.getDiscovery());
                  assertEquals(McfResourceComponent.class.getName(), mcfServiceDescriptor.getClazz());
               }
               else if (sd.getName().equals("Admin Object"))
               {
                  assertEquals(AoResourceDiscoveryComponent.class.getName(), sd.getDiscovery());
                  assertEquals(AoResourceComponent.class.getName(), sd.getClazz());
               }
               else
               {
                  throw new IllegalStateException("Unkown ResourceDescriptor name: " + sd.getName());
               }
            }
         }
         else if (serviceDesc.getName().equals("Datasources"))
         {
            assertEquals(DsResourceDiscoveryComponent.class.getName(), serviceDesc.getDiscovery());
            assertEquals(DsResourceComponent.class.getName(), serviceDesc.getClazz());
         }
         else
         {
            throw new IllegalStateException("Unkown ResourceDescriptor name: " + serviceDesc.getName());
         }
      }
   }
   
}
