/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;

import org.jboss.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * WorkManagerTestCase.
 * 
 * Tests for the JBoss specific work manager functionality.
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class WorkManagerTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger LOG = Logger.getLogger(WorkManagerTestCase.class);

   /*
    * Embedded
    */
   private static Embedded embedded;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Test that the installed work manager is an instance of the 
    * <code>javax.resource.spi.work.WorkManager</code> interface
    * @throws Throwable throwable exception 
    */
   @Test
   public void testInstanceOf() throws Throwable
   {
      org.jboss.jca.core.api.workmanager.WorkManager workManager = 
         embedded.lookup("WorkManager", org.jboss.jca.core.api.workmanager.WorkManager.class);

      assertNotNull(workManager);
      assertTrue(workManager instanceof javax.resource.spi.work.WorkManager);
   }

   /**
    * Test that the installed work manager has a thread pool instance
    * @throws Throwable throwable exception 
    */
   @Test
   public void testThreadPool() throws Throwable
   {
      org.jboss.jca.core.api.workmanager.WorkManager workManager = 
         embedded.lookup("WorkManager", org.jboss.jca.core.api.workmanager.WorkManager.class);

      assertNotNull(workManager);
      assertNotNull(workManager.getShortRunningThreadPool());
      assertNotNull(workManager.getLongRunningThreadPool());
   }

   /**
    * Test that the installed work manager has an XA terminator instance
    * @throws Throwable throwable exception 
    */
   @Test
   public void testXATerminator() throws Throwable
   {
      org.jboss.jca.core.api.workmanager.WorkManager workManager = 
         embedded.lookup("WorkManager", org.jboss.jca.core.api.workmanager.WorkManager.class);

      assertNotNull(workManager);
      assertNotNull(workManager.getXATerminator());
      assertTrue(workManager.getXATerminator() instanceof org.jboss.jca.core.spi.transaction.xa.XATerminator);
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
      embedded = EmbeddedFactory.create(false);

      // Startup
      embedded.startup();

      // Deploy Naming, Transaction and WorkManager
      URL naming =
         WorkManagerTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction =
         WorkManagerTestCase.class.getClassLoader().getResource("transaction.xml");
      URL wm =
         WorkManagerTestCase.class.getClassLoader().getResource("workmanager.xml");

      embedded.deploy(naming);
      embedded.deploy(transaction);
      embedded.deploy(wm);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy WorkManager, Transaction and Naming
      URL naming =
         WorkManagerTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction =
         WorkManagerTestCase.class.getClassLoader().getResource("transaction.xml");
      URL wm =
         WorkManagerTestCase.class.getClassLoader().getResource("workmanager.xml");

      embedded.undeploy(wm);
      embedded.undeploy(transaction);
      embedded.undeploy(naming);

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
