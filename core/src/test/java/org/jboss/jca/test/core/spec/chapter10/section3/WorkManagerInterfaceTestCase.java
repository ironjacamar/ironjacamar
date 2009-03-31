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
import org.jboss.jca.test.core.spec.chapter10.common.CallbackCount;
import org.jboss.jca.test.core.spec.chapter10.common.MyWorkAdapter;
import org.jboss.jca.test.core.spec.chapter10.common.NestCharWork;

import java.util.concurrent.CountDownLatch;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.work.Work;
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
    * doWork method: throws WorkException Thrown if an error occurs
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testDoWorkMethodThrowWorkException() throws Throwable
   {
      //TODO
   }
   
   /**
    * doWork method: throws WorkCompletedException indicates that a Workinstance has completed 
    * execution with an exception.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testDoWorkMethodThrowWorkCompletedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * doWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testDoWorkMethodThrowWorkRejectedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test defalut 
    * param A maximum timeout value indicates that an action be performed arbitrarily without any time constraint.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkMethodWithDefaultParams() throws Throwable
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
      workManager.doWork(mw, WorkManager.INDEFINITE, null, null);
      hold.await();
      done.await();

      assertTrue(mw.hasPreRun());
      assertTrue(mw.hasPostRun());
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test IMMEDIATE 
    * param A zero timeout value indicates an action be performed immediately. The WorkManager implementation
    *  must timeout the action as soon as possible.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testDoWorkMethodWithImmediateStart() throws Throwable
   {
      //TODO
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test UNKNOWN param A constant 
    * to indicate an unknown start delay duration or other unknown values.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testDoWorkMethodWithUnknowStart() throws Throwable
   {
      //TODO
   }
   /**
    * doWork method: This call blocks until the Work instance completes execution. test ExecutionContext paraman. 
    * object containing the execution context with which the submitted Work instance must be executed.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testDoWorkMethodWithExecutionContextParams() throws Throwable
   {
      //TODO
   }
   /**
    * doWork method: This call blocks until the Work instance completes execution. test WorkListener param 
    * workListener an object which would be notified when the various Work processing events
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkMethodWithWorkListenerParams() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      final CountDownLatch before = new CountDownLatch(1);
      final CountDownLatch hold = new CountDownLatch(1);
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      BlockRunningWork mw = new BlockRunningWork(before, hold, start, done);
      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);

      assertFalse(mw.hasPreRun());
      assertFalse(mw.hasPostRun());

      before.countDown();
      start.countDown();
      workManager.doWork(mw, WorkManager.INDEFINITE, null, wa);
      hold.await();
      done.await();

      assertEquals(1, callbackCount.getAcceptCount());
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
    * startWork method: throws WorkException Thrown if an error occurs
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkMethodThrowWorkException() throws Throwable
   {
      //TODO
   }
   
   /**
    * startWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkMethodThrowWorkRejectedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * startWork method: return the time elapsed from Work acceptance until start of execution.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkMethodReturnLong() throws Throwable
   {
      //TODO
   }
   
   /**
    * startWork method: This call blocks until the Work instance starts execution but not until its completion.
    *  test defalut param A maximum timeout value indicates that an action be performed arbitrarily without
    *   any time constraint.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkMethodWithDefaultParams() throws Throwable
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
      workManager.startWork(mw, WorkManager.INDEFINITE, null, null);
      hold.await();
      assertTrue(mw.hasPreRun());
      assertFalse(mw.hasPostRun());
      
      start.countDown();
      done.await();

      assertTrue(mw.hasPreRun());
      assertTrue(mw.hasPostRun());  
   }
   
   /**
    * startWork method: This call blocks until the Work instance starts execution but not until its completion. 
    * test IMMEDIATE param A zero timeout value indicates an action be performed immediately. The WorkManager 
    * implementation must timeout the action as soon as possible.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkMethodWithImmediateStart() throws Throwable
   {
      //TODO
   }
   
   /**
    * startWork method: This call blocks until the Work instance starts execution but not until its completion. 
    * test UNKNOWN param A constant to indicate an unknown start delay duration or other unknown values.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkMethodWithUnknowStart() throws Throwable
   {
      //TODO
   }
   /**
    * startWork method: This call blocks until the Work instance starts execution but not until its completion
    *    test ExecutionContext param. 
    * object containing the execution context with which the submitted Work instance must be executed.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkMethodWithExecutionContextParams() throws Throwable
   {
      //TODO
   }
   /**
    * startWork method: This call blocks until the Work instance starts execution but not until
    *  its completion.  test WorkListener param 
    * workListener an object which would be notified when the various Work processing events
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkMethodWithWorkListenerParams() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      final CountDownLatch before = new CountDownLatch(1);
      final CountDownLatch hold = new CountDownLatch(1);
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      BlockRunningWork mw = new BlockRunningWork(before, hold, start, done);
      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);

      assertFalse(mw.hasPreRun());
      assertFalse(mw.hasPostRun());

      before.countDown();
      start.countDown();
      workManager.startWork(mw, WorkManager.INDEFINITE, null, wa);
      hold.await();
      done.await();

      assertEquals(1, callbackCount.getAcceptCount());
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
    * scheduleWork method: throws WorkException Thrown if an error occurs
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testScheduleWorkMethodThrowWorkException() throws Throwable
   {
      //TODO
   }
   
   /**
    * scheduleWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testScheduleWorkMethodThrowWorkRejectedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * scheduleWork method: throws WorkCompletedException indicates that a Workinstance has completed 
    * execution with an exception.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testScheduleWorkMethodThrowWorkCompletedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a Work instance has been 
    * accepted for processing.
    *  test defalut param A maximum timeout value indicates that an action be performed arbitrarily without
    *   any time constraint.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkMethodWithDefaultParams() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      final CountDownLatch before = new CountDownLatch(1);
      final CountDownLatch hold = new CountDownLatch(1);
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      BlockRunningWork mw = new BlockRunningWork(before, hold, start, done);

      assertFalse(mw.hasPreRun());
      assertFalse(mw.hasPostRun());

      workManager.scheduleWork(mw, WorkManager.INDEFINITE, null, null);
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
    * scheduleWork method: This call does not block and returns immediately once a Work instance has been
    *  accepted for processing.
    * test IMMEDIATE param A zero timeout value indicates an action be performed immediately. The WorkManager 
    * implementation must timeout the action as soon as possible.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testScheduleWorkMethodWithImmediateStart() throws Throwable
   {
      //TODO
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a Work instance has been 
    * accepted for processing.
    * test UNKNOWN param A constant to indicate an unknown start delay duration or other unknown values.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testScheduleWorkMethodWithUnknowStart() throws Throwable
   {
      //TODO
   }
   /**
    * scheduleWork method: ThThis call does not block and returns immediately once a Work instance has been 
    * accepted for processing.
    *    test ExecutionContext param. 
    * object containing the execution context with which the submitted Work instance must be executed.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testScheduleWorkMethodWithExecutionContextParams() throws Throwable
   {
      //TODO
   }
   /**
    * scheduleWork method: This call does not block and returns immediately once a Work instance has been 
    * accepted for processing.  test WorkListener param 
    * workListener an object which would be notified when the various Work processing events
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkMethodWithWorkListenerParams() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      
      final CountDownLatch before = new CountDownLatch(1);
      final CountDownLatch hold = new CountDownLatch(1);
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      BlockRunningWork mw = new BlockRunningWork(before, hold, start, done);
      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);

      assertFalse(mw.hasPreRun());
      assertFalse(mw.hasPostRun());

      before.countDown();
      start.countDown();
      workManager.scheduleWork(mw, WorkManager.INDEFINITE, null, wa);
      hold.await();
      done.await();

      assertEquals(1, callbackCount.getAcceptCount());
      assertTrue(mw.hasPreRun());
      assertTrue(mw.hasPostRun());
   }
   
   /**
    * Test for paragraph 6
    * The optional startTimeout parameter specifies a time duration in milliseconds within which 
    *      the execution of the Work instance must start. Otherwise, the Work instance 
    *      is rejected with a WorkRejectedException set to an appropriate error code (WorkException.START_TIMED_OUT).
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartTimeoutThrowWorkRejectedException() throws Throwable
   {
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

