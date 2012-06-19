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
package org.jboss.jca.core.workmanager.spec.chapter10.api;

import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.workmanager.spec.chapter10.common.LongRunningWork;
import org.jboss.jca.core.workmanager.spec.chapter10.common.PriorityWork;
import org.jboss.jca.embedded.arquillian.Inject;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.threads.BlockingExecutor;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * ManageThreadTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 1,3
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class ManageThreadTestCase
{
   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager workManager;
   
   /**
    * Test for paragraph 4 : bullet 1
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkManagerHasThreadPools() throws Throwable
   {
      assertNotNull(workManager);
      assertTrue(workManager instanceof javax.resource.spi.work.WorkManager);
      BlockingExecutor shortRunning = workManager.getShortRunningThreadPool();
      assertNotNull(shortRunning);

      BlockingExecutor longRunning = workManager.getLongRunningThreadPool();
      assertNotNull(longRunning);
      assertFalse(shortRunning.equals(longRunning));
   }
   /**
    * Test that the installed work manager has an XA terminator instance
    * @throws Throwable throwable exception 
    */
   @Test
   public void testXATerminator() throws Throwable
   {
      assertNotNull(workManager.getXATerminator());
      assertTrue(workManager.getXATerminator() instanceof org.jboss.jca.core.spi.transaction.xa.XATerminator);
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
    * Test for paragraph 2
    * There is no restriction on the number of Work instances submitted by a 
    *            resource adapter or WHEN Work instances may be submitted.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testAnytimeWorkInstanceSubmitted() throws Throwable
   {
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
    * Test for paragraph 4
    * the application server must use threads of the same thread priority level to
    *            process Work instances submitted by a specific resource adapter. 
    * @throws Throwable throwable exception 
    */
   @Test
   public void testAsUseThreadSamePriorityLevel() throws Throwable
   {
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

   /**
    * Test for paragraph 4 : bullet 1
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testRaSharingThreadPool() throws Throwable
   {
      //TODO reuse them efficiently across different resource adapters deployed in its runtime environment
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
    * Test for paragraph 4 : bullet 4
    * @see https://jira.jboss.org/jira/browse/JBJCA-40
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testCheckWorkByIntercept() throws Throwable
   {
      //TODO may need to enforce control over the runtime behavior of its system components
      //an application server may choose to intercept operations on a thread object, perform checks, 
      //and enforce correct behavior.
   }
   
   /**
    * Test for paragraph 4 : bullet 5
    * @see https://jira.jboss.org/jira/browse/JBJCA-41
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testAsDisallowRaCreateThread() throws Throwable
   {
      //TODO An application server may disallow resource adapters from creating their own threads based 
      //on its security policy setting, enforced by a security manager.
   }
   
}
