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

import org.jboss.jca.test.core.spec.chapter10.common.BlockRunningWork;
import org.jboss.jca.test.core.spec.chapter10.common.CallbackCount;
import org.jboss.jca.test.core.spec.chapter10.common.MyWorkAdapter;
import org.jboss.jca.test.core.spec.chapter10.common.NestCharWork;
import org.jboss.jca.test.core.spec.chapter10.common.ShortRunningWork;


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
 * WorkManagerInterfaceTestCase.
 * 
 * Tests for the JCA specific API about WorkManager
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkManagerDoWorkTestCase
{
   /*
    * Bootstrap (MC Facade)
    */
   private static EmbeddedTestMcBootstrap bootstrap;
   
   /**
    * doWork method: This call blocks until the Work instance completes execution.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      workManager.doWork(work);
      assertTrue(work.hasCallRun());

   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution.
    * Negative test against Null Work 
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testDoWorkNullWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      Work work = null;
      workManager.doWork(work);
   }
   
   /**
    * doWork method: throws WorkException Thrown if an error occurs
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testDoWorkThrowWorkException() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      Work work = null;
      workManager.doWork(work);
   }
   
   /**
    * doWork method: throws WorkCompletedException indicates that a Work instance has completed 
    * execution with an exception.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testDoWorkThrowWorkCompletedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * doWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testDoWorkThrowWorkRejectedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test default 
    * parameter A maximum timeout value indicates that an action be performed arbitrarily without any time constraint.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkFullSpec() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      workManager.doWork(work, WorkManager.INDEFINITE, null, null);
      assertTrue(work.hasCallRun());
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. 
    * Negative test against Null Work 
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testDoWorkFullSpecNullWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      Work work = null;
      workManager.doWork(work, WorkManager.INDEFINITE, null, null);
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test default 
    * parameter A maximum timeout value indicates that an action be performed arbitrarily without any time constraint.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkFullSpecWithIndefiniteStartTimeout() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      workManager.doWork(work, WorkManager.INDEFINITE, null, null);
      assertTrue(work.hasCallRun());
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test IMMEDIATE 
    * parameter A zero timeout value indicates an action be performed immediately. The WorkManager implementation
    *  must timeout the action as soon as possible.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkFullSpecWithImmediateStartTimeout() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      workManager.doWork(work, WorkManager.IMMEDIATE, null, null);
      assertTrue(work.hasCallRun());
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test negative parameter constant 
    * to indicate an negative value start delay duration
    * @throws Throwable throwable exception 
    */
   @Test(expected = IllegalArgumentException.class)
   public void testDoWorkFullSpecWithNegativeStartTimeout() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      workManager.doWork(work, -5, null, null);
      assertTrue(work.hasCallRun());
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test ExecutionContext paraman. 
    * object containing the execution context with which the submitted Work instance must be executed.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkFullSpecWithExecutionContext() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      ExecutionContext ec = new ExecutionContext();
      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      workManager.doWork(work, WorkManager.INDEFINITE, ec, null);
      assertTrue(work.hasCallRun());
   }
      
   /**
    * doWork method: This call blocks until the Work instance completes execution. test ExecutionContext paraman. 
    * object containing the execution context with which the submitted Work instance must be executed.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkFullSpecWithNullExecutionContext() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      workManager.doWork(work, WorkManager.INDEFINITE, null, null);
      assertTrue(work.hasCallRun());
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test WorkListener param 
    * workListener an object which would be notified when the various Work processing events
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkFullSpecWithWorkListener() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);

      workManager.doWork(work, WorkManager.INDEFINITE, null, wa);
      assertEquals(1, callbackCount.getAcceptCount());
      assertTrue(work.hasCallRun());
   }
   
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test WorkListener param 
    * workListener an object which would be notified when the various Work processing events
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkFullSpecWithWorkNullListener() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      workManager.doWork(work, WorkManager.INDEFINITE, null, null);
      assertTrue(work.hasCallRun());
   }
      
   /**
    * doWork method: throws WorkException Thrown if an error occurs
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testDoWorkFullSpecThrowWorkException() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);
      Work work = null;
      workManager.doWork(work, WorkManager.INDEFINITE, null, null);
   }
   
   /**
    * doWork method: throws WorkCompletedException indicates that a Work instance has completed 
    * execution with an exception.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testDoWorkFullSpecThrowWorkCompletedException() throws Throwable
   {
      //TODO
   }
   
   /**
    * doWork method: throws WorkRejectedException indicates that a Work instance has been 
    * rejected from further processing.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testDoWorkFullSpecThrowWorkRejectedException() throws Throwable
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
      bootstrap.deploy(WorkManagerDoWorkTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.deploy(WorkManagerDoWorkTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.deploy(WorkManagerDoWorkTestCase.class.getClassLoader(), "workmanager-jboss-beans.xml");
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy WorkManager, Transaction and Naming
      bootstrap.undeploy(WorkManagerDoWorkTestCase.class.getClassLoader(), "workmanager-jboss-beans.xml");
      bootstrap.undeploy(WorkManagerDoWorkTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.undeploy(WorkManagerDoWorkTestCase.class.getClassLoader(), "naming-jboss-beans.xml");

      // Shutdown MC
      bootstrap.shutdown();

      // Set Bootstrap to null
      bootstrap = null;
   }
}
