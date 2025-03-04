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

import jakarta.resource.spi.work.ExecutionContext;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkManager;
import jakarta.resource.spi.work.WorkRejectedException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * WorkManagerStartWorkTestCase.
 * 
 * Tests for the JCA specific API about WorkManager
 * 
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class WorkManagerStartWorkTestCase
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
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWork() throws Throwable
   {
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      long time = workManager.startWork(work);
      done.await();
      assertTrue(work.hasPostRun());
      assertTrue(time > WorkManager.UNKNOWN);
   }

   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. Negative test against Null Work 
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testStartWorkNullWork() throws Throwable
   {
      Work work = null;
      workManager.startWork(work);
   }

   /**
    * startWork method: doesn't throw WorkCompletedException, because it returns after start stage 
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkThrowWorkCompletedException() throws Throwable
   {
      SimpleWork work = new SimpleWork();
      work.setThrowWorkAException(true);
      workManager.scheduleWork(work);
   }

   /**
    * startWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testStartWorkThrowWorkRejectedException() throws Throwable
   {
      ShortRunningWork work = new ShortRunningWork();
      rejectingWorkManager.scheduleWork(work);
   }

   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. test default 
    * param A maximum timeout value indicates that an action be performed arbitrarily without any time constraint.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkFullSpec() throws Throwable
   {
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      long time = workManager.startWork(work, WorkManager.INDEFINITE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
      assertTrue(time >= WorkManager.UNKNOWN);
   }

   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. test for expected WorkException
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testStartWorkFullSpecNullWork() throws Throwable
   {
      Work work = null;
      workManager.startWork(work, WorkManager.INDEFINITE, null, null);
   }

   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. test IMMEDIATE parameter A zero timeout value 
    *       indicates an action be performed immediately. The WorkManager implementation
    *       must timeout the action as soon as possible.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkFullSpecWithImmediateStartTimeout() throws Throwable
   {
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.startWork(work, WorkManager.IMMEDIATE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }

   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. test UNKNOWN parameter A constant 
    *       to indicate an unknown start delay duration or other unknown values.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testStartWorkFullSpecWithUnknowStartTimeout() throws Throwable
   {
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.startWork(work, WorkManager.UNKNOWN, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }

   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. test negative parameter constant 
    *       to indicate an negative value start delay duration
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testStartWorkFullSpecWithNegativeStartTimeout() throws Throwable
   {
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.startWork(work, -5, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }

   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. test ExecutionContext parameter object containing 
    *       the execution context with which the submitted Work instance must be executed.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkFullSpecWithExecutionContext() throws Throwable
   {
      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);

      ExecutionContext ec = new ExecutionContext();
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.startWork(work, WorkManager.INDEFINITE, ec, null);
      done.await();
      assertTrue(work.hasPostRun());
   }

   /**
    * startWork method: doesn't throw WorkCompletedException, because it returns after start stage 
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkFullSpecThrowWorkCompletedException() throws Throwable
   {
      SimpleWork work = new SimpleWork();
      work.setThrowWorkAException(true);
      workManager.scheduleWork(work, WorkManager.INDEFINITE, null, null);

   }

   /**
    * startWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testStartWorkFullSpecThrowWorkRejectedException() throws Throwable
   {
      ShortRunningWork work = new ShortRunningWork();
      rejectingWorkManager.scheduleWork(work);
   }
}
