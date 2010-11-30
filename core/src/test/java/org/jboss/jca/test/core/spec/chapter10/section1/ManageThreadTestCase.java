/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.core.spec.chapter10.section1;

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;

import org.jboss.threads.BlockingExecutor;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * ManageThreadTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 1
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class ManageThreadTestCase
{
   /*
    * Embedded
    */
   private static Embedded embedded;
   
   /**
    * Test for paragraph 4 : bullet 1
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkManagerHasThreadPool() throws Throwable
   {
      org.jboss.jca.core.api.workmanager.WorkManager workManager = 
         embedded.lookup("WorkManager", org.jboss.jca.core.api.workmanager.WorkManager.class);

      BlockingExecutor shortRunning = workManager.getShortRunningThreadPool();
      assertNotNull(shortRunning);

      BlockingExecutor longRunning = workManager.getShortRunningThreadPool();
      assertNotNull(longRunning);
   }

   /**
    * Test for paragraph 4 : bullet 1
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testRaSharingThreadPool() throws Throwable
   {
      //TODO reuse them efficiently across different resource adapters deployed in its runtime environment
   }
   
   /**
    * Test for paragraph 4 : bullet 4
    * @see https://jira.jboss.org/jira/browse/JBJCA-40
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testCheckWorkByIntercept() throws Throwable
   {
      //TODO may need to enforce control over the runtime behavior of its system components
      //an application server may choose to intercept operations on a thread object, perform checks, 
      //and enforce correct behavior.
   }
   
   /**
    * Test for paragraph 4 : bullet 5
    * @see https://jira.jboss.org/jira/browse/JBJCA-41
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testAsDisallowRaCreateThread() throws Throwable
   {
      //TODO An application server may disallow resource adapters from creating their own threads based 
      //on its security policy setting, enforced by a security manager.
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
         ManageThreadTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction =
         ManageThreadTestCase.class.getClassLoader().getResource("transaction.xml");
      URL wm =
         ManageThreadTestCase.class.getClassLoader().getResource("workmanager.xml");

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
         ManageThreadTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction =
         ManageThreadTestCase.class.getClassLoader().getResource("transaction.xml");
      URL wm =
         ManageThreadTestCase.class.getClassLoader().getResource("workmanager.xml");

      embedded.undeploy(wm);
      embedded.undeploy(transaction);
      embedded.undeploy(naming);

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }
}
