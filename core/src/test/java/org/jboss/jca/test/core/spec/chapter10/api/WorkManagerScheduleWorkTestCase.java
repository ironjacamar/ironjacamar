/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.core.spec.chapter10.api;

import org.jboss.jca.test.core.spec.chapter10.common.CallbackCount;
import org.jboss.jca.test.core.spec.chapter10.common.LongRunningWork;
import org.jboss.jca.test.core.spec.chapter10.common.MyWorkAdapter;

import java.util.concurrent.CountDownLatch;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * WorkManagerScheduleWorkTestCase.
 * 
 * Tests for the JCA specific API about WorkManager
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkManagerScheduleWorkTestCase
{
   /*
    * Bootstrap (MC Facade)
    */
   private static EmbeddedTestMcBootstrap bootstrap;
   
   /**
    * scheduleWork method: TThis call does not block and returns immediately once a
    *                Work instance has been accepted for processing.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.scheduleWork(work);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * scheduleWork method:This call does not block and returns immediately once a
    *    Work instance has been accepted for processing. Negative test against Null Work 
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testScheduleWorkNullWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      Work work = null;
      workManager.scheduleWork(work);
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *    Work instance has been accepted for processing. test for expected WorkException
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testScheduleWorkThrowWorkException() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      Work work = null;
      workManager.scheduleWork(work);
   }
   
   /**
    * scheduleWork method: throws WorkCompletedException indicates that a Work instance has completed 
    * execution with an exception.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testScheduleWorkThrowWorkCompletedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * scheduleWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testScheduleWorkThrowWorkRejectedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *     Work instance has been accepted for processing. test default parameter A maximum timeout
    *      value indicates that an action be performed arbitrarily without any time constraint.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkFullSpec() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *   Work instance has been accepted for processing. test for expected WorkException
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testScheduleWorkFullSpecNullWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      Work work = null;
      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, null);
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *     Work instance has been accepted for processing.  test default parameter A maximum timeout value 
    *     indicates that an action be performed arbitrarily without any time constraint.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkFullSpecWithIndefiniteStartTimeout() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *    Work instance has been accepted for processing. test IMMEDIATE parameter A zero timeout value 
    *    indicates an action be performed immediately. The WorkManager implementation
    *    must timeout the action as soon as possible.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkFullSpecWithImmediateStartTimeout() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.scheduleWork(work, WorkManager.IMMEDIATE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *       Work instance has been accepted for processing. test UNKNOWN parameter A constant 
    *       to indicate an unknown start delay duration or other unknown values.
    * @throws Throwable throwable exception 
    */
   @Test(expected = IllegalArgumentException.class)
   public void testScheduleWorkFullSpecWithUnknowStartTimeout() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.scheduleWork(work, WorkManager.UNKNOWN, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *   Work instance has been accepted for processing. test negative parameter constant 
    *   to indicate an negative value start delay duration
    * @throws Throwable throwable exception 
    */
   @Test(expected = IllegalArgumentException.class)
   public void testScheduleWorkFullSpecWithNegativeStartTimeout() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.scheduleWork(work, -5, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *   Work instance has been accepted for processing. test ExecutionContext parameter object containing 
    *   the execution context with which the submitted Work instance must be executed.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkFullSpecWithExecutionContext() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      ExecutionContext ec = new ExecutionContext();
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.scheduleWork(work, WorkManager.INDEFINITE, ec, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
      
   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *    Work instance has been accepted for processing. test ExecutionContext parameter object containing 
    *    the execution context with which the submitted Work instance must be executed.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkFullSpecWithNullExecutionContext() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *   Work instance has been accepted for processing. test WorkListener parameter workListener an object 
    *   which would be notified when the various Work processing events
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkFullSpecWithWorkListener() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();


      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);
      
      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, wa);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   
   /**
    * scheduleWork method:  This call does not block and returns immediately once a
    *     Work instance has been accepted for processing.test WorkListener parameter 
    *     workListener an object which would be notified when the various Work processing events
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkFullSpecWithWorkNullListener() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
      
   /**
    * scheduleWork method: throws WorkException Thrown if an error occurs
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testScheduleWorkFullSpecThrowWorkException() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      Work work = null;
      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, null);
   }
   
   /**
    * scheduleWork method: throws WorkCompletedException indicates that a Work instance has completed 
    * execution with an exception.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testScheduleWorkFullSpecThrowWorkCompletedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * scheduleWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testScheduleWorkFullSpecThrowWorkRejectedException() throws Throwable
   {
      //TODO
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
      bootstrap.deploy(WorkManagerScheduleWorkTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.deploy(WorkManagerScheduleWorkTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.deploy(WorkManagerScheduleWorkTestCase.class.getClassLoader(), "workmanager-jboss-beans.xml");
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy WorkManager, Transaction and Naming
      bootstrap.undeploy(WorkManagerScheduleWorkTestCase.class.getClassLoader(), "workmanager-jboss-beans.xml");
      bootstrap.undeploy(WorkManagerScheduleWorkTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.undeploy(WorkManagerScheduleWorkTestCase.class.getClassLoader(), "naming-jboss-beans.xml");

      // Shutdown MC
      bootstrap.shutdown();

      // Set Bootstrap to null
      bootstrap = null;
   }
}
