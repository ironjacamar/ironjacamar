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

import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.io.File;
import java.net.URL;

import org.jboss.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.rhq.core.clientapi.server.discovery.InventoryReport;
import org.rhq.core.pc.PluginContainer;
import org.rhq.core.pc.PluginContainerConfiguration;
import org.rhq.core.pc.inventory.InventoryManager;
import org.rhq.core.pc.plugin.FileSystemPluginFinder;

import static org.junit.Assert.*;

/**
 * RHQ plugin test cases for an XA resource adapter
 * 
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 */
public class XATestCase
{
   /** The logger */
   private static Logger log = Logger.getLogger(XATestCase.class);

   /** Embedded */
   private static Embedded embedded;
   
   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      URL deployment = null;
      try
      {
         ManagementRepository manRepo = embedded.lookup("ManagementRepository", ManagementRepository.class);
         assertNotNull(manRepo);
         assertNotNull(manRepo.getConnectors());
         assertEquals(0, manRepo.getConnectors().size());

         deployment = XATestCase.class.getClassLoader().getResource("xa.rar");
         embedded.deploy(deployment);

         assertEquals(1, manRepo.getConnectors().size());

         File pluginDir = new File(System.getProperty("archives.dir"));

         PluginContainerConfiguration pcConfig = new PluginContainerConfiguration();
         pcConfig.setPluginFinder(new FileSystemPluginFinder(pluginDir));
         pcConfig.setPluginDirectory(pluginDir);
         pcConfig.setInsideAgent(false);

         PluginContainer pc = PluginContainer.getInstance();

         pc.setConfiguration(pcConfig);
         pc.initialize();

         InventoryManager im = pc.getInventoryManager();

         InventoryReport report = im.executeServerScanImmediately();
      }
      finally
      {
         if (deployment != null)
            embedded.undeploy(deployment);
      }
   }

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create();
      // Startup
      embedded.startup();
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
