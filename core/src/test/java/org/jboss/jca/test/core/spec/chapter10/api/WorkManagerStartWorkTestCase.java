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

import javax.resource.spi.BootstrapContext;
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
 * WorkManagerStartWorkTestCase.
 * 
 * Tests for the JCA specific API about WorkManager
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkManagerStartWorkTestCase
{
   /*
    * Bootstrap (MC Facade)
    */
   private static EmbeddedTestMcBootstrap bootstrap;
   
   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.startWork(work);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. Negative test against Null Work 
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testStartWorkNullWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      Work work = null;
      workManager.startWork(work);
   }
   
   
   /**
    * startWork method: return the time elapsed from Work acceptance until start of execution.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkReturnLong() throws Throwable
   {
      //TODO
   }
   
   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. test for expected WorkException
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testStartWorkThrowWorkException() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      Work work = null;
      workManager.startWork(work);
   }
   
   /**
    * startWork method: throws WorkCompletedException indicates that a Workinstance has completed 
    * execution with an exception.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkThrowWorkCompletedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * startWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkThrowWorkRejectedException() throws Throwable
   {
      //TODO
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
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.startWork(work, WorkManager.INDEFINITE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. test for expected WorkException
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testStartWorkFullSpecNullWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      Work work = null;
      workManager.startWork(work, WorkManager.INDEFINITE, null, null);
   }
   
   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion.  test default parameter A maximum timeout value 
    *       indicates that an action be performed arbitrarily without any time constraint.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkFullSpecWithIndefiniteStartTimeout() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.startWork(work, WorkManager.INDEFINITE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
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
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

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
   @Test(expected = IllegalArgumentException.class)
   public void testStartWorkFullSpecWithUnknowStartTimeout() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

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
   @Test(expected = IllegalArgumentException.class)
   public void testStartWorkFullSpecWithNegativeStartTimeout() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

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
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

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
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. test ExecutionContext parameter object containing 
    *       the execution context with which the submitted Work instance must be executed.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkFullSpecWithNullExecutionContext() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.startWork(work, WorkManager.INDEFINITE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * startWork method: This call blocks until the Work instance starts execution but
    *       not until its completion. test WorkListener parameter workListener an object 
    *       which would be notified when the various Work processing events
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkFullSpecWithWorkListener() throws Throwable
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
      
      workManager.startWork(work, WorkManager.INDEFINITE, null, wa);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   
   /**
    * startWork method:  This call blocks until the Work instance starts execution but
    *       not until its completion. test WorkListener parameter 
    *       workListener an object which would be notified when the various Work processing events
    * @throws Throwable throwable exception 
    */
   @Test
   public void testStartWorkFullSpecWithWorkNullListener() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      LongRunningWork work = new LongRunningWork(start, done);
      assertFalse(work.hasPostRun());
      start.countDown();

      workManager.startWork(work, WorkManager.INDEFINITE, null, null);
      done.await();
      assertTrue(work.hasPostRun());
   }
   
   /**
    * startWork method: return the time elapsed from Work acceptance until start of execution.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkFullSpecReturnLong() throws Throwable
   {
      //TODO
   }
   
      
   /**
    * startWork method: throws WorkException Thrown if an error occurs
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testStartWorkFullSpecThrowWorkException() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      Work work = null;
      workManager.startWork(work, WorkManager.INDEFINITE, null, null);
   }
   
   /**
    * startWork method: throws WorkCompletedException indicates that a Work instance has completed 
    * execution with an exception.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkFullSpecThrowWorkCompletedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * startWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartWorkFullSpecThrowWorkRejectedException() throws Throwable
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

      // Deploy Naming and Transaction
      bootstrap.deploy(WorkManagerStartWorkTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.deploy(WorkManagerStartWorkTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      
      // Deploy Beans
      bootstrap.deploy(WorkManagerStartWorkTestCase.class);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy Transaction and Naming
      bootstrap.undeploy(WorkManagerStartWorkTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.undeploy(WorkManagerStartWorkTestCase.class.getClassLoader(), "naming-jboss-beans.xml");

      // Undeploy Beans
      bootstrap.undeploy(WorkManagerStartWorkTestCase.class);

      // Shutdown MC
      bootstrap.shutdown();

      // Set Bootstrap to null
      bootstrap = null;
   }
}
