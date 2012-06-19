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

import org.jboss.jca.core.workmanager.spec.chapter10.common.CallbackCount;
import org.jboss.jca.core.workmanager.spec.chapter10.common.LongRunningWork;
import org.jboss.jca.core.workmanager.spec.chapter10.common.MyWorkAdapter;
import org.jboss.jca.core.workmanager.spec.chapter10.common.ShortRunningWork;
import org.jboss.jca.embedded.arquillian.Inject;

import java.util.concurrent.CountDownLatch;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * WorkAdapterTestCase.
 * 
 * Tests for the JCA specific API about WorkAdapter
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class WorkAdapterAndWorkListenerTestCase
{

   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager workManager;

   /**
    * Test for paragraph 1 Section 3.3.3
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkRejectedStatus() throws Throwable
   {
      Work work1 = new ShortRunningWork();
      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);
      try
      {
         workManager.doWork(work1, WorkManager.UNKNOWN, null, wa);
         fail("there should be WorkRejectedException");
      }
      catch (WorkRejectedException e)
      {
         //Expected
      }
      finally
      {
         assertEquals("should be same", 1, callbackCount.getRejectedCount());
         assertEquals("should be same", 0, callbackCount.getStartCount());
         assertEquals(workManager, wa.getSource());
         assertEquals(work1, wa.getWork());
         assertNotNull(wa.getException());
      }
      try
      {
         workManager.startWork(work1, WorkManager.UNKNOWN, null, wa);
         fail("there should be WorkRejectedException");
      }
      catch (WorkRejectedException e)
      {
         //Expected
      }
      finally
      {
         assertEquals("should be same", 2, callbackCount.getRejectedCount());
         assertEquals("should be same", 0, callbackCount.getStartCount());
         assertEquals(workManager, wa.getSource());
         assertEquals(work1, wa.getWork());
         assertNotNull(wa.getException());

      }
      try
      {
         workManager.scheduleWork(work1, WorkManager.UNKNOWN, null, wa);
         fail("there should be WorkRejectedException");
      }
      catch (WorkRejectedException e)
      {
         //Expected
      }
      finally
      {
         assertEquals("should be same", 3, callbackCount.getRejectedCount());
         assertEquals("should be same", 0, callbackCount.getStartCount());
         assertEquals(workManager, wa.getSource());
         assertEquals(work1, wa.getWork());
         assertNotNull(wa.getException());

      }
   }

   /**
    * Test for paragraph 1 Sections 3.3.2-3.3.5
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkStatuses() throws Throwable
   {
      final CountDownLatch start2 = new CountDownLatch(1);
      final CountDownLatch done2 = new CountDownLatch(1);
      final CountDownLatch start3 = new CountDownLatch(1);
      final CountDownLatch done3 = new CountDownLatch(1);

      ShortRunningWork work1 = new ShortRunningWork();
      LongRunningWork work2 = new LongRunningWork(start2, done2);
      LongRunningWork work3 = new LongRunningWork(start3, done3);

      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);

      workManager.doWork(work1, WorkManager.INDEFINITE, null, wa);

      assertEquals(workManager, wa.getSource());
      assertEquals(work1, wa.getWork());

      workManager.startWork(work2, WorkManager.INDEFINITE, null, wa);

      assertEquals(workManager, wa.getSource());
      assertEquals(work2, wa.getWork());

      //TODO not implemented 
      //assertTrue(wa.getStartDuration()>0);

      workManager.scheduleWork(work3, WorkManager.INDEFINITE, null, wa);

      assertEquals("should be same", 3, callbackCount.getAcceptCount());
      assertEquals("should be same", 2, callbackCount.getStartCount());
      assertEquals("should be same", 1, callbackCount.getCompletedCount());

      start2.countDown();
      start3.countDown();

      done2.await();
      done3.await();
   }
}
