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

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import org.rhq.core.clientapi.descriptor.configuration.ConfigurationDescriptor;
import org.rhq.core.clientapi.descriptor.plugin.OperationDescriptor;
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
      File pluginDir = new File(System.getProperty("target.dir"));
      File pluginFile = new File(pluginDir, "ironjacamar-rhq.jar");
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
      assertEquals("IronJacamar_AS7", serverDesc.getName());
      
      List<ServiceDescriptor> services = serverDesc.getServices();
      assertEquals(1, services.size());
      
      ServiceDescriptor rarServiceDesc = services.get(0);
      assertEquals("Resource Adapter Archive (RAR)", rarServiceDesc.getName());
      assertEquals("org.jboss.jca.rhq.ra.RarResourceDiscoveryComponent", rarServiceDesc.getDiscovery());
      assertEquals("org.jboss.jca.rhq.ra.RarResourceComponent", rarServiceDesc.getClazz());
      assertEquals(ResourceCreateDeletePolicy.BOTH, rarServiceDesc.getCreateDeletePolicy());
      assertEquals(ResourceCreationData.CONTENT, rarServiceDesc.getCreationDataType());
      
      // RAR service has the plugin configuration
      ConfigurationDescriptor pluginConfDesc = rarServiceDesc.getPluginConfiguration();
      assertNotNull(pluginConfDesc);
      
      // 4 operations for 
      List<OperationDescriptor> rarOperDescs = rarServiceDesc.getOperation();
      assertEquals(4, rarOperDescs.size());
      
      // RAR service has resource configuration
      ConfigurationDescriptor resConfDesc = rarServiceDesc.getResourceConfiguration();
      assertNotNull(resConfDesc);
      
      // 2 sub services in RAR service
      List<ServiceDescriptor> subServiceDesc = rarServiceDesc.getServices();
      assertEquals(2, subServiceDesc.size());
      
   }
   
}
