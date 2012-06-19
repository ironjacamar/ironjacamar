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

import org.jboss.jca.core.workmanager.spec.chapter10.common.NestCharWork;
import org.jboss.jca.core.workmanager.spec.chapter10.common.ShortRunningWork;
import org.jboss.jca.embedded.arquillian.Inject;

import java.util.concurrent.CountDownLatch;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.Xid;

import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * WorkManagerInterfaceTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3.3
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class WorkManagerInterfaceTestCase
{
   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager workManager;

  
   /**
    * Test for paragraph 3
    * doWork method: this provides a first in, first out (FIFO) execution start 
    *      ordering and last in, first out (LIFO) execution completion ordering guarantee.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testFifoStartLifoFinish() throws Throwable
   {

      final CountDownLatch startA = new CountDownLatch(1);
      final CountDownLatch doneA = new CountDownLatch(1);
      NestCharWork workA = new NestCharWork("A", startA, doneA);
      
      final CountDownLatch startB = new CountDownLatch(1);
      final CountDownLatch doneB = new CountDownLatch(1);
      NestCharWork workB = new NestCharWork("B", startB, doneB);
      
      workA.emptyBuffer();
      workA.setNestDo(true);
      workA.setWorkManager(workManager);
      workA.setWork(workB);
      startA.countDown();
      startB.countDown();
      workManager.doWork(workA);

      doneA.await();
      doneB.await();

      assertEquals(workA.getBuffer(), "BA");
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

      final CountDownLatch startA = new CountDownLatch(1);
      final CountDownLatch doneA = new CountDownLatch(1);
      NestCharWork workA = new NestCharWork("A", startA, doneA);
      
      final CountDownLatch startB = new CountDownLatch(1);
      final CountDownLatch doneB = new CountDownLatch(1);
      NestCharWork workB = new NestCharWork("B", startB, doneB);
      
      workA.emptyBuffer();
      workA.setWorkManager(workManager);
      workA.setWork(workB);
      startA.countDown();
      startB.countDown();
      workManager.startWork(workA);
      workManager.startWork(workB);

      doneA.await();
      doneB.await();

      assertEquals(workA.getBuffer(), "AB");
   }
   
   
   /**
    * Test for bullet 4 Section 3.3.6
    * When the application server is unable to recreate an execution context if it is  
    *                      specified for the submitted Work instance, it must throw a
    *                      WorkCompletedException set to an appropriate error code.
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkCompletedException.class)
   public void testThrowWorkCompletedException() throws Throwable
   {
      ExecutionContext ec = new ExecutionContext();
      ShortRunningWork work = new ShortRunningWork();
      ec.setXid(new XidImpl());
      ec.setTransactionTimeout(Long.MAX_VALUE);

      workManager.doWork(work, WorkManager.INDEFINITE, ec, null);

   }
   /**
    * Implementation of Xid for test purpose
    * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
    *
    */
   static class XidImpl implements Xid
   {
      
      /**
       * {@inheritDoc}
       */
      public byte[] getBranchQualifier()
      {
         return null;
      }

      /**
       * {@inheritDoc}
       */
      public int getFormatId()
      {
         return 0;
      }
      /**
       * {@inheritDoc}
       */
      public byte[] getGlobalTransactionId()
      {
         return null;
      }

   }
}

