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
package org.jboss.jca.core.workmanager.spec.chapter10.api;

import org.jboss.jca.core.workmanager.spec.chapter10.common.MyWorkAdapter;
import org.jboss.jca.core.workmanager.spec.chapter10.common.ShortRunningWork;
import org.jboss.jca.core.workmanager.spec.chapter10.common.SimpleWork;
import org.jboss.jca.embedded.arquillian.Inject;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * WorkManagerInterfaceTestCase.
 * 
 * Tests for the JCA specific API about WorkManager
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class WorkManagerDoWorkTestCase
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
    * doWork method: This call blocks until the Work instance completes execution.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWork() throws Throwable
   {

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
      Work work = null;
      workManager.doWork(work);
   }
   
   /**
    * doWork method: throws WorkCompletedException indicates that a Work instance has completed 
    * execution with an exception.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkCompletedException.class)
   public void testDoWorkThrowWorkCompletedException() throws Throwable
   {
      SimpleWork work = new SimpleWork();
      work.setThrowWorkAException(true);
      workManager.doWork(work);
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. 
    * Negative test against Null Work 
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testDoWorkFullSpecNullWork() throws Throwable
   {
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
   @Test(expected = WorkRejectedException.class)
   public void testDoWorkFullSpecWithNegativeStartTimeout() throws Throwable
   {
      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      workManager.doWork(work, -5, null, null);
   }
   
   /**
    * doWork method: This call blocks until the Work instance completes execution. test ExecutionContext paraman. 
    * object containing the execution context with which the submitted Work instance must be executed.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoWorkFullSpecWithExecutionContext() throws Throwable
   {
      ExecutionContext ec = new ExecutionContext();
      ShortRunningWork work = new ShortRunningWork();
      assertFalse(work.hasCallRun());

      workManager.doWork(work, WorkManager.INDEFINITE, ec, null);
      assertTrue(work.hasCallRun());
   }
      
   /**
    * doWork method: throws WorkCompletedException indicates that a Work instance has completed 
    * execution with an exception.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkCompletedException.class)
   public void testDoWorkFullSpecThrowWorkCompletedException() throws Throwable
   {
      SimpleWork work = new SimpleWork();
      work.setThrowWorkAException(true);
      workManager.doWork(work, WorkManager.INDEFINITE, null, null);

   }
   /**
    * Work manager should throw WorkRejected Exception
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testDoWorkRejected() throws Throwable
   {

      ShortRunningWork work = new ShortRunningWork();

      rejectingWorkManager.doWork(work);
   }
   
   /**
    * Work manager should throw WorkRejected Exception (full signature)
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testDoWorkFullSignatureRejected() throws Throwable
   {

      ShortRunningWork work = new ShortRunningWork();
      MyWorkAdapter wa = new MyWorkAdapter();
      
      rejectingWorkManager.doWork(work, WorkManager.INDEFINITE, null, wa);
   }
}
