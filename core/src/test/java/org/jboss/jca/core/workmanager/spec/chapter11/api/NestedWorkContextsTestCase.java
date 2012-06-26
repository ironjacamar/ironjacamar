/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.workmanager.spec.chapter11.api;

import java.util.concurrent.CountDownLatch;

import org.jboss.jca.core.workmanager.spec.chapter11.common.NestProviderWork;
import org.jboss.jca.core.workmanager.spec.chapter11.common.SecurityContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.TransactionContextCustom;
import org.jboss.jca.core.workmanager.spec.chapter11.common.UnsupportedContext;
import org.jboss.jca.embedded.arquillian.Inject;

import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

/**
 * NestedWorkContextsTestCase.
 * Because nested Work submissions are allowed in the Connector WorkManager, the
 * Connector WorkManager must support nested contexts unless the WorkContext
 * type prohibits them. 
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
@RunWith(Arquillian.class)
public class NestedWorkContextsTestCase
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
      NestProviderWork workA = new NestProviderWork("A", startA, doneA);
      workA.addContext(new TransactionContext());
      
      final CountDownLatch startB = new CountDownLatch(1);
      final CountDownLatch doneB = new CountDownLatch(1);
      NestProviderWork workB = new NestProviderWork("B", startB, doneB);
      workB.addContext(new TransactionContextCustom());
      
      workA.emptyBuffers();
      workA.setNestDo(true);
      workA.setWorkManager(workManager);
      workA.setWork(workB);
      startA.countDown();
      startB.countDown();
      workManager.doWork(workA);

      doneA.await();
      doneB.await();

      assertEquals(workA.getBufStart(), "AB");
      assertEquals(workA.getBufDo(), "BA");
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
      NestProviderWork workA = new NestProviderWork("A", startA, doneA);
      workA.addContext(new SecurityContextCustom());
      
      final CountDownLatch startB = new CountDownLatch(1);
      final CountDownLatch doneB = new CountDownLatch(1);
      NestProviderWork workB = new NestProviderWork("B", startB, doneB);
      workB.addContext(new HintsContext());
      
      workA.emptyBuffers();
      workA.setNestDo(false);
      workA.setWorkManager(workManager);
      workA.setWork(workB);
      startA.countDown();
      startB.countDown();
      workManager.startWork(workA);

      doneA.await();
      doneB.await();

      assertEquals(workA.getBufStart(), "AB");
   }
   
   /**
    * Test unsupported context nested doWork. 
    * @throws Throwable throwable exception 
    */
   @Test(expected = Throwable.class)
   public void testDoWorkUnsupportedContext() throws Throwable
   {

      final CountDownLatch startA = new CountDownLatch(0);
      final CountDownLatch doneA = new CountDownLatch(0);
      NestProviderWork workA = new NestProviderWork("A", startA, doneA);
      workA.addContext(new TransactionContext());
      
      final CountDownLatch startB = new CountDownLatch(0);
      final CountDownLatch doneB = new CountDownLatch(0);
      NestProviderWork workB = new NestProviderWork("B", startB, doneB);
      workB.addContext(new UnsupportedContext());
      
      workA.emptyBuffers();
      workA.setNestDo(true);
      workA.setWorkManager(workManager);
      workA.setWork(workB);
      workManager.doWork(workA);
   }
   
   
   /**
    * Test unsupported context outer startWork
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkException.class)
   public void testStartWorkUnsupportedContext() throws Throwable
   {

      final CountDownLatch startA = new CountDownLatch(0);
      final CountDownLatch doneA = new CountDownLatch(0);
      NestProviderWork workA = new NestProviderWork("A", startA, doneA);
      workA.addContext(new UnsupportedContext());
      
      final CountDownLatch startB = new CountDownLatch(0);
      final CountDownLatch doneB = new CountDownLatch(0);
      NestProviderWork workB = new NestProviderWork("B", startB, doneB);
      workB.addContext(new HintsContext());
      
      workA.emptyBuffers();
      workA.setNestDo(false);
      workA.setWorkManager(workManager);
      workA.setWork(workB);
      workManager.startWork(workA);
   }
}
