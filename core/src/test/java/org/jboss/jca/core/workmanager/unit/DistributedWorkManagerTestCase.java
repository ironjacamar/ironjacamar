/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.workmanager.unit;

import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;

import org.jboss.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DistributedWorkManagerTestCase.
 * 
 * Tests for the JBoss specific distributed work manager functionality.
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class DistributedWorkManagerTestCase
{
   private static Logger log = Logger.getLogger(DistributedWorkManagerTestCase.class);

   private static Embedded embedded;
   
   private static DistributedWorkManager dwm1;
   private static DistributedWorkManager dwm2;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Test that the used distributed work managers are an instance of the 
    * <code>javax.resource.spi.work.DistributableWorkManager</code> interface
    * @throws Throwable throwable exception 
    */
   @Test
   public void testInstanceOf() throws Throwable
   {
      assertNotNull(dwm1);
      assertTrue(dwm1 instanceof javax.resource.spi.work.DistributableWorkManager);

      assertNotNull(dwm2);
      assertTrue(dwm2 instanceof javax.resource.spi.work.DistributableWorkManager);
   }

   /**
    * Test that the used distributed work managers are configured
    * @throws Throwable throwable exception 
    */
   @Test
   public void testConfigured() throws Throwable
   {
      assertNotNull(dwm1);
      assertNotNull(dwm1.getPolicy());
      assertNotNull(dwm1.getSelector());
      assertNotNull(dwm1.getTransport());

      assertNotNull(dwm2);
      assertNotNull(dwm2.getPolicy());
      assertNotNull(dwm2.getSelector());
      assertNotNull(dwm2.getTransport());
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

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

      // Deploy DistributedWorkManager
      URL dwm =
         DistributedWorkManagerTestCase.class.getClassLoader().getResource("dwm.xml");

      embedded.deploy(dwm);

      dwm1 = embedded.lookup("DistributedWorkManager1", DistributedWorkManager.class);
      dwm2 = embedded.lookup("DistributedWorkManager2", DistributedWorkManager.class);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy DistributedWorkManager
      URL dwm =
         DistributedWorkManagerTestCase.class.getClassLoader().getResource("dwm.xml");

      embedded.undeploy(dwm);

      dwm1 = null;
      dwm2 = null;

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
