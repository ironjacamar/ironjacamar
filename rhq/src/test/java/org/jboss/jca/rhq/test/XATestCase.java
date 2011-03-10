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
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.rhq.core.domain.resource.Resource;
import org.rhq.core.pc.PluginContainer;
import org.rhq.core.pc.PluginContainerConfiguration;
import org.rhq.core.pc.inventory.InventoryManager;
import org.rhq.core.pc.inventory.RuntimeDiscoveryExecutor;
import org.rhq.core.pc.plugin.FileSystemPluginFinder;

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
      
      // connectionfactory, managedConnectionFactory and adminObject
      assertEquals(3, subRarServiceRes.size());
      
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
