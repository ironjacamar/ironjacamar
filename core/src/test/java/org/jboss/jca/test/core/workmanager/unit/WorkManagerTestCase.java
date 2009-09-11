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

package org.jboss.jca.test.core.workmanager.unit;

import org.jboss.jca.embedded.EmbeddedJCA;

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
   private static EmbeddedJCA embedded;

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
      org.jboss.jca.core.api.WorkManager workManager = 
         embedded.lookup("WorkManager", org.jboss.jca.core.api.WorkManager.class);

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
      org.jboss.jca.core.api.WorkManager workManager = 
         embedded.lookup("WorkManager", org.jboss.jca.core.api.WorkManager.class);

      assertNotNull(workManager);
      assertNotNull(workManager.getThreadPool());
      assertTrue(workManager.getThreadPool() instanceof org.jboss.jca.common.api.ThreadPool);
   }

   /**
    * Test that the installed work manager has an XA terminator instance
    * @throws Throwable throwable exception 
    */
   @Test
   public void testXATerminator() throws Throwable
   {
      org.jboss.jca.core.api.WorkManager workManager = 
         embedded.lookup("WorkManager", org.jboss.jca.core.api.WorkManager.class);

      assertNotNull(workManager);
      assertNotNull(workManager.getXATerminator());
      assertTrue(workManager.getXATerminator() instanceof org.jboss.tm.JBossXATerminator);
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
      embedded = new EmbeddedJCA(false);

      // Startup
      embedded.startup();

      // Deploy Naming, Transaction and WorkManager
      embedded.deploy(WorkManagerTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      embedded.deploy(WorkManagerTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      embedded.deploy(WorkManagerTestCase.class.getClassLoader(), "workmanager-jboss-beans.xml");
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy WorkManager, Transaction and Naming
      embedded.undeploy(WorkManagerTestCase.class.getClassLoader(), "workmanager-jboss-beans.xml");
      embedded.undeploy(WorkManagerTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      embedded.undeploy(WorkManagerTestCase.class.getClassLoader(), "naming-jboss-beans.xml");

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
