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
import org.jboss.jca.test.core.spec.chapter10.common.LongRunningWork;
import org.jboss.jca.test.core.spec.chapter10.common.PriorityWork;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkAdapter;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * WorkManagementModelTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkManagementModelTestCase
{
   /*
    * Bootstrap (MC Facade)
    */
   private static EmbeddedTestMcBootstrap bootstrap;
      
   /**
    * Test for paragraph 1
    * A resource adapter obtains a WorkManager instance from the BootstrapContext
    *            instance provided by the application server during its deployment.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testGetWorkManagerFromBootstrapConext() throws Throwable
   {
      BootstrapContext bootstrapContext = bootstrap.lookup("SimpleBootstrapContext", BootstrapContext.class);

      assertNotNull(bootstrapContext.getWorkManager());
   }

   /**
    * Test for paragraph 2
    * When a Work instance is submitted, one of the free threads picks up the
    *            Work instance, sets up an appropriate execution context and 
    *            calls the run method on the Work instance. 
    * @throws Throwable throwable exception 
    */
   @Test
   public void testOneThreadPickWorkInstance() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      CountDownLatch start = new CountDownLatch(1);
      CountDownLatch done = new CountDownLatch(2);

      LongRunningWork mwA = new LongRunningWork(start, done);
      LongRunningWork mwB = new LongRunningWork(start, done);

      workManager.startWork(mwA);
      workManager.startWork(mwB);

      start.countDown();

      done.await();

      assertFalse(mwA.getThreadId() == mwB.getThreadId());
      assertTrue(mwA.hasPostRun());
      assertTrue(mwB.hasPostRun());
   }
   
   /**
    * Test for paragraph2
    * There is no restriction on the NUMBER of Work instances submitted by a 
    *            resource adapter or when Work instances may be submitted.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testManyWorkInstancesSubmitted() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      final CountDownLatch start1 = new CountDownLatch(1);
      final CountDownLatch done1 = new CountDownLatch(1);
      final CountDownLatch start2 = new CountDownLatch(1);
      final CountDownLatch done2 = new CountDownLatch(1);
      final CountDownLatch start3 = new CountDownLatch(1);
      final CountDownLatch done3 = new CountDownLatch(1);
      
      LongRunningWork work1 = new LongRunningWork(start1, done1);
      LongRunningWork work2 = new LongRunningWork(start2, done2);
      LongRunningWork work3 = new LongRunningWork(start3, done3);
      
      assertFalse(work1.hasPostRun());
      assertFalse(work2.hasPostRun());
      assertFalse(work3.hasPostRun());
      start1.countDown();
      start2.countDown();
      start3.countDown();
      workManager.startWork(work1);
      workManager.startWork(work2);
      workManager.startWork(work3);
      done1.await();
      done2.await();
      done3.await();

      assertTrue(work1.hasPostRun());
      assertTrue(work2.hasPostRun());
      assertTrue(work3.hasPostRun());
   }
   
   /**
    * Test for paragraph 2
    * There is no restriction on the number of Work instances submitted by a 
    *            resource adapter or WHEN Work instances may be submitted.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testAnytimeWorkInstanceSubmitted() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      final CountDownLatch start1 = new CountDownLatch(1);
      final CountDownLatch done1 = new CountDownLatch(1);
      final CountDownLatch start2 = new CountDownLatch(1);
      final CountDownLatch done2 = new CountDownLatch(1);
      final CountDownLatch start3 = new CountDownLatch(1);
      final CountDownLatch done3 = new CountDownLatch(1);
      
      LongRunningWork work1 = new LongRunningWork(start1, done1);
      LongRunningWork work2 = new LongRunningWork(start2, done2);
      LongRunningWork work3 = new LongRunningWork(start3, done3);
      
      assertFalse(work1.hasPostRun());
      assertFalse(work2.hasPostRun());
      assertFalse(work3.hasPostRun());
      start1.countDown();
      start2.countDown();
      start3.countDown();
      workManager.startWork(work1);
      workManager.startWork(work2);

      done1.await();
      done2.await();
      
      workManager.startWork(work3);
      done3.await();

      assertTrue(work1.hasPostRun());
      assertTrue(work2.hasPostRun());
      assertTrue(work3.hasPostRun());
   }
   
   /**
    * Test for paragraph 2
    * When the run method on the Work instance completes, the application 
    *            server reuses the thread.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testThreadBackPoolWhenWorkDone() throws Throwable
   {
      //TODO
      /*
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      ThreadPoolImpl tpImpl = (ThreadPoolImpl)bootstrap.lookup("WorkManagerThreadPool", ThreadPool.class);
      int poolNum = tpImpl.getPoolNumber();
      int poolSize = tpImpl.getPoolSize();


      SimpleWork work = new SimpleWork();
      
      assertFalse(work.isCallRun());
      workManager.doWork(work);
      assertTrue(work.isCallRun());
      
      assertEquals(poolNum, tpImpl.getPoolNumber());
      assertEquals(poolSize, tpImpl.getPoolSize());
      */
   }
   
   /**
    * Test for paragraph 3
    * The application server may decide to reclaim active threads based on load conditions. 
    * @see https://jira.jboss.org/jira/browse/JBJCA-42
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testAsActiveThreadOnLoadCondition() throws Throwable
   {
   }   
   
   /**
    * Test for paragraph 3
    * The resource adapter should periodically monitor such hints and do the 
    *            necessary internal cleanup to avoid any inconsistencies. 
    * @see https://jira.jboss.org/jira/browse/JBJCA-43
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testRaPeriodicalReleaseWorkResource() throws Throwable
   {
   }   
   
   /**
    * Test for paragraph 4
    * the application server must use threads of the same thread priority level to
    *            process Work instances submitted by a specific resource adapter. 
    * @throws Throwable throwable exception 
    */
   @Test
   public void testAsUseThreadSamePriorityLevel() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      List<PriorityWork> listWorks = new ArrayList<PriorityWork>();

      int number = 3;
      CountDownLatch done = new CountDownLatch(number);

      for (int i = 0; i < number; i++)
      {
         PriorityWork pwork = new PriorityWork(done);
         listWorks.add(pwork);
         workManager.doWork(pwork);
      }

      done.await();

      int threadPriortity = -1;
      for (PriorityWork work : listWorks)
      {
         if (threadPriortity == -1)
         {
            threadPriortity = work.getThreadPriority();
         }
         assertEquals(work.getThreadPriority(), threadPriortity);
      }
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

      // Deploy Naming and Transaction
      bootstrap.deploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.deploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      
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
      // Undeploy Transaction and Naming
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
