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

import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;
import org.jboss.jca.common.api.ThreadPool;

import org.junit.AfterClass;
import static org.junit.Assert.* ;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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
    * Bootstrap (MC Facade)
    */
   private static EmbeddedTestMcBootstrap bootstrap;
   
   /**
    * Test for paragraph 4 : bullet 1
    */
   @Test
   public void testWorkManagerHasThreadPool() throws Throwable
   {
      org.jboss.jca.core.api.WorkManager workManager = 
         bootstrap.lookup("WorkManager", org.jboss.jca.core.api.WorkManager.class);
      ThreadPool threadPool = workManager.getThreadPool();
      assertNotNull(threadPool);
   }

   /**
    * Test for paragraph 4 : bullet 1
    */
   @Ignore
   public void testRaSharingThreadPool() throws Throwable
   {
      //TODO reuse them efficiently across different resource adapters deployed in its runtime environment
   }
   
   /**
    * Test for paragraph 4 : bullet 4
    */
   @Ignore
   public void testCheckWorkByIntercept() throws Throwable
   {
      //TODO may need to enforce control over the runtime behavior of its system components
      //an application server may choose to intercept operations on a thread object, perform checks, and enforce correct behavior.
   }
   
   /**
    * Test for paragraph 4 : bullet 5
    */
   @Ignore
   public void testAsDisallowRaCreateThread() throws Throwable
   {
      //TODO An application server may disallow resource adapters from creating their own threads based on its security policy setting, enforced by a security manager.
   }
   
   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle start, before the suite is executed
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set a new MC Bootstrap
      bootstrap = EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap();

      // Deploy Naming and Transaction
      bootstrap.deploy(ManageThreadTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.deploy(ManageThreadTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      
      // Deploy Beans
      bootstrap.deploy(ManageThreadTestCase.class);
   }

   /**
    * Lifecycle stop, after the suite is executed
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy Transaction and Naming
      bootstrap.undeploy(ManageThreadTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.undeploy(ManageThreadTestCase.class.getClassLoader(), "naming-jboss-beans.xml");

      // Undeploy Beans
      bootstrap.undeploy(ManageThreadTestCase.class);

      // Shutdown MC
      bootstrap.shutdown();

      // Set Bootstrap to null
      bootstrap = null;
   }
}
