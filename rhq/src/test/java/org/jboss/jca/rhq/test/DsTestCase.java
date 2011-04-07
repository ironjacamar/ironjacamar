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

import org.jboss.jca.rhq.embed.core.EmbeddedJcaDiscover;

import java.io.File;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.rhq.core.domain.resource.Resource;
import org.rhq.core.pc.PluginContainer;
import org.rhq.core.pc.PluginContainerConfiguration;
import org.rhq.core.pc.inventory.InventoryManager;
import org.rhq.core.pc.inventory.RuntimeDiscoveryExecutor;
import org.rhq.core.pc.plugin.FileSystemPluginFinder;

import static org.junit.Assert.*;

/**
 * RHQ plugin test cases for Datasource
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
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
