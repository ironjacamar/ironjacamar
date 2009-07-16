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
package org.jboss.jca.test.core.spec.chapter10.section3;

import org.jboss.jca.test.core.spec.chapter10.common.BlockRunningWork;
import org.jboss.jca.test.core.spec.chapter10.common.NestCharWork;

import java.util.concurrent.CountDownLatch;

import javax.resource.spi.BootstrapContext;

import javax.resource.spi.work.WorkManager;

import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * WorkManagerInterfaceTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3.3
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkManagerInterfaceTestCase
{
   /*
    * Bootstrap (MC Facade)
    */
   private static EmbeddedTestMcBootstrap bootstrap;
   
   
   /**
    * Test for paragraph 1
    * WorkManager instance can be obtained by calling the getWorkManager method of the BootstrapContext instance.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testGetWorkManagerFromBootstrapConext() throws Throwable
   {
      
      BootstrapContext bootstrapContext = bootstrap.lookup("SimpleBootstrapContext", BootstrapContext.class);

      assertNotNull(bootstrapContext.getWorkManager());
   }

   /**
    * Test for paragraph 3
    * doWork method: This call blocks until the Work instance completes execution.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkMethod() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      final CountDownLatch before = new CountDownLatch(1);
      final CountDownLatch hold = new CountDownLatch(1);
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      BlockRunningWork mw = new BlockRunningWork(before, hold, start, done);

      assertFalse(mw.hasPreRun());
      assertFalse(mw.hasPostRun());

      before.countDown();
      start.countDown();
      workManager.doWork(mw);
      hold.await();
      done.await();

      assertTrue(mw.hasPreRun());
      assertTrue(mw.hasPostRun());
   }
   
   /**
    * Test for paragraph 3
    * doWork method: this provides a first in, first out (FIFO) execution start 
    *      ordering and last in, first out (LIFO) execution completion ordering guarantee.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testFifoStartLifoFinish() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      final CountDownLatch startA = new CountDownLatch(1);
      final CountDownLatch doneA = new CountDownLatch(1);
      NestCharWork workA = new NestCharWork("A", startA, doneA);
      
      final CountDownLatch startB = new CountDownLatch(1);
      final CountDownLatch doneB = new CountDownLatch(1);
      NestCharWork workB = new NestCharWork("B", startB, doneB);
      
      workA.emptyBuffer();
      workA.setNestDo(true);
      workA.setWorkManager(workManager);
      workA.setWorkManager(workB);
      startA.countDown();
      startB.countDown();
      workManager.doWork(workA);

      doneA.await();
      doneB.await();

      assertEquals(workA.getBuffer(), "BA");
   }
   
   /**
    * Test for paragraph 4
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkMethod() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      final CountDownLatch before = new CountDownLatch(1);
      final CountDownLatch hold = new CountDownLatch(1);
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      BlockRunningWork mw = new BlockRunningWork(before, hold, start, done);

      assertFalse(mw.hasPreRun());
      assertFalse(mw.hasPostRun());

      before.countDown();
      workManager.startWork(mw);
      hold.await();
      assertTrue(mw.hasPreRun());
      assertFalse(mw.hasPostRun());
      
      start.countDown();
      done.await();

      assertTrue(mw.hasPreRun());
      assertTrue(mw.hasPostRun());      
   }

   /**
    * Test for paragraph 4
    * startWork method: This returns the time elapsed in milliseconds from Work acceptance until 
    * the start of execution.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testReturnTimeBeforeStart() throws Throwable
   {
      //TODO test against BasicThreadPool.java?
   }
   
   /**
    * Test for paragraph 4
    * startWork method: A value of -1 (WorkManager.UNKNOWN) must be returned, if the actual start 
    * delay duration is unknown.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testUnknownReturnedIfDonotKnowDelay() throws Throwable
   {
      //TODO test against BasicThreadPool.java?
   }
   
   /**
    * Test for paragraph 4
    * startWork method: this provides a FIFO execution start ordering guarantee, 
    *                 but no execution completion ordering guarantee.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testFifoStart() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      final CountDownLatch startA = new CountDownLatch(1);
      final CountDownLatch doneA = new CountDownLatch(1);
      NestCharWork workA = new NestCharWork("A", startA, doneA);
      
      final CountDownLatch startB = new CountDownLatch(1);
      final CountDownLatch doneB = new CountDownLatch(1);
      NestCharWork workB = new NestCharWork("B", startB, doneB);
      
      workA.emptyBuffer();
      workA.setWorkManager(workManager);
      workA.setWorkManager(workB);
      startA.countDown();
      startB.countDown();
      workManager.startWork(workA);
      workManager.startWork(workB);

      doneA.await();
      doneB.await();

      assertEquals(workA.getBuffer(), "AB");
   }
   
   /**
    * Test for paragraph 5
    * scheduleWork method: This call does not block and returns immediately once a
    *                Work instance has been accepted for processing.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkMethod() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      final CountDownLatch before = new CountDownLatch(1);
      final CountDownLatch hold = new CountDownLatch(1);
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      BlockRunningWork mw = new BlockRunningWork(before, hold, start, done);

      assertFalse(mw.hasPreRun());
      assertFalse(mw.hasPostRun());

      workManager.scheduleWork(mw);
      before.countDown();
      hold.await();
      assertTrue(mw.hasPreRun());
      assertFalse(mw.hasPostRun());
      
      start.countDown();
      done.await();

      assertTrue(mw.hasPreRun());
      assertTrue(mw.hasPostRun()); 
   }
   
   /**
    * Test for bullet 1 Section 3.3.6
    * The application server must implement the WorkManager interface
    * @throws Throwable throwable exception 
    */
   @Test
   public void testAsImplementWorkManagerInterface() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      assertNotNull(workManager);
   }   
   
   /**
    * Test for bullet 2 Section 3.3.6
    * The application server must allow nested Work submissions.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testAllowNestedWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      final CountDownLatch startA = new CountDownLatch(1);
      final CountDownLatch doneA = new CountDownLatch(1);
      NestCharWork workA = new NestCharWork("A", startA, doneA);
      
      final CountDownLatch startB = new CountDownLatch(1);
      final CountDownLatch doneB = new CountDownLatch(1);
      NestCharWork workB = new NestCharWork("B", startB, doneB);
      
      workA.emptyBuffer();
      workA.setNestDo(true);
      workA.setWorkManager(workManager);
      workA.setWorkManager(workB);
      startA.countDown();
      startB.countDown();
      workManager.doWork(workA);

      doneA.await();
      doneB.await();

      assertEquals(workA.getBuffer(), "BA");
   }
   
   /**
    * Test for bullet 4 Section 3.3.6
    * When the application server is unable to recreate an execution context if it is  
    *                      specified for the submitted Work instance, it must throw a
    *                      WorkCompletedException set to an appropriate error code.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testThrowWorkCompletedException() throws Throwable
   {
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
      // Create and set a new MC Bootstrap
      bootstrap = EmbeddedTestMcBootstrap.createEmbeddedMcBootstrap();

      // Deploy Naming, Transaction and WorkManager
      bootstrap.deploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.deploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.deploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "workmanager-jboss-beans.xml");
      
      // Deploy Beans
      bootstrap.deploy(WorkManagerInterfaceTestCase.class);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy WorkManager, Transaction and Naming
      bootstrap.undeploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "workmanager-jboss-beans.xml");
      bootstrap.undeploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.undeploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "naming-jboss-beans.xml");

      // Undeploy Beans
      bootstrap.undeploy(WorkManagerInterfaceTestCase.class);

      // Shutdown MC
      bootstrap.shutdown();

      // Set Bootstrap to null
      bootstrap = null;
   }
}

