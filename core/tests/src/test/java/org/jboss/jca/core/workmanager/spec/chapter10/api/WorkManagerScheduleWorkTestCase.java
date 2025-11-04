/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.workmanager.spec.chapter10.api;

import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.core.workmanager.spec.chapter10.common.LongRunningWork;
import org.jboss.jca.core.workmanager.spec.chapter10.common.ShortRunningWork;
import org.jboss.jca.core.workmanager.spec.chapter10.common.SimpleWork;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.resource.spi.work.ExecutionContext;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkManager;
import jakarta.resource.spi.work.WorkRejectedException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * WorkManagerScheduleWorkTestCase.
 * 
 * Tests for the JCA specific API about WorkManager
 * 
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class WorkManagerScheduleWorkTestCase
{
   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager workManager;

   /**
    * Injecting embedded work manager, that rejects works
    */
   @Inject(name = "RejectingWorkManager")
   WorkManager rejectingWorkManager;

   /**
    * Define the rejecting work manager deployment
    * @return The deployment archive
    */
   @Deployment
   public static InputStreamDescriptor createDistributedWorkManagerDeployment()
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("rejecting-workmanager.xml",
            cl.getResourceAsStream("rejecting-workmanager.xml"));
      return isd;
   }

   /**
    * scheduleWork method: TThis call does not block and returns immediately once a
    *                Work instance has been accepted for processing.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWork() throws Throwable
   {
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
      Work work = null;
      workManager.scheduleWork(work);
   }

   /**
    * scheduleWork method: impossible to throw WorkCompletedException because schedule method returns  
    * after accept stage.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testScheduleWorkThrowWorkCompletedException() throws Throwable
   {
      SimpleWork work = new SimpleWork();
      work.setThrowWorkAException(true);
      workManager.scheduleWork(work);
   }

   /**
    * scheduleWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testScheduleWorkThrowWorkRejectedException() throws Throwable
   {
      ShortRunningWork work = new ShortRunningWork();
      rejectingWorkManager.scheduleWork(work);
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
      Work work = null;
      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, null);
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
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.scheduleWork(work, WorkManager.IMMEDIATE, null, null);

      assertTrue(done.await(1, TimeUnit.SECONDS));
      assertTrue(work.hasPostRun());
   }

   /**
    * scheduleWork method: This call does not block and returns immediately once a
    *       Work instance has been accepted for processing. test UNKNOWN parameter A constant 
    *       to indicate an unknown start delay duration or other unknown values.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testScheduleWorkFullSpecWithUnknowStartTimeout() throws Throwable
   {
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
   @Test(expected = WorkRejectedException.class)
   public void testScheduleWorkFullSpecWithNegativeStartTimeout() throws Throwable
   {
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
    * scheduleWork method: impossible to throw WorkCompletedException because schedule method returns  
    * after accept stage.
    * @throws Throwable throwable exception 
    */
   @Test
   @Ignore //this may fail on some environments
   public void testScheduleWorkFullSpecThrowWorkCompletedException() throws Throwable
   {
      SimpleWork work = new SimpleWork();
      work.setThrowWorkAException(true);
      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, null);
   }

   /**
    * scheduleWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testScheduleWorkFullSpecThrowWorkRejectedException() throws Throwable
   {
      ShortRunningWork work = new ShortRunningWork();
      rejectingWorkManager.scheduleWork(work, WorkManager.INDEFINITE, null, null);
   }
}
