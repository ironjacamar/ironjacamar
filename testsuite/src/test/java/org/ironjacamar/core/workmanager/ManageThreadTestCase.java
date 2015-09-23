/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.workmanager;

import org.ironjacamar.core.workmanager.support.LongRunningWork;
import org.ironjacamar.core.workmanager.support.PriorityWork;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.wm.WorkConnection;
import org.ironjacamar.rars.wm.WorkConnectionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ManageThreadTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 1,3
 * 
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class ManageThreadTestCase
{
   /** The user transaction */
   @Resource(mappedName = "java:/eis/WorkConnectionFactory")
   private static WorkConnectionFactory wcf;


   /**
    * The resource adapter
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private static ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createWorkRar();
   }

   /**
    * The activation
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private static ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createWorkDeployment(null);
   }

   /**
    * Test for paragraph 4 : bullet 1
    * @throws Throwable throwable exception 
    */
   /*@Test
   public void testWorkManagerHasThreadPools() throws Throwable
   {
      WorkManager workManager = wcf.getConnection().getWorkManager();
      assertNotNull(workManager);
      assertTrue(wcf instanceof WorkManagedConnection);
      BlockingExecutor shortRunning = ((WorkManagedConnection) wcf).getShortRunningThreadPool();
      assertNotNull(shortRunning);

      BlockingExecutor longRunning = workManager.getLongRunningThreadPool();
      assertNotNull(longRunning);
      assertFalse(shortRunning.equals(longRunning));
   }*/
   /**
    * Test that the installed work manager has an XA terminator instance
    * @throws Throwable throwable exception 
    */
   /*@Test
   public void testXATerminator() throws Throwable
   {
      WorkManager workManager = wcf.getConnection().getWorkManager();
      assertTrue(wcf instanceof WorkManagedConnection);

      assertNotNull((WorkManagedConnection) wcf).getXATerminator());
      assertTrue(workManager.getXATerminator() instanceof org.ironjacamar.core.spi.transaction.xa.XATerminator);
   }*/

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
      WorkConnection wc = wcf.getConnection();
      CountDownLatch start = new CountDownLatch(1);
      CountDownLatch done = new CountDownLatch(2);

      LongRunningWork mwA = new LongRunningWork(start, done);
      LongRunningWork mwB = new LongRunningWork(start, done);

      wc.startWork(mwA);
      wc.startWork(mwB);

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
      WorkConnection wc = wcf.getConnection();
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
      wc.startWork(work1);
      wc.startWork(work2);

      done1.await();
      done2.await();
      
      wc.startWork(work3);
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
      WorkConnection wc = wcf.getConnection();
      List<PriorityWork> listWorks = new ArrayList<PriorityWork>();

      int number = 3;
      CountDownLatch done = new CountDownLatch(number);

      for (int i = 0; i < number; i++)
      {
         PriorityWork pwork = new PriorityWork(done);
         listWorks.add(pwork);
         wc.doWork(pwork);
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
