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

import org.ironjacamar.core.workmanager.support.CallbackCount;
import org.ironjacamar.core.workmanager.support.LongRunningWork;
import org.ironjacamar.core.workmanager.support.ShortRunningWork;
import org.ironjacamar.core.workmanager.support.StatusWorkAdapter;
import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.wm.WorkConnectionFactory;

import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * WorkAdapterTestCase.
 * 
 * Tests for the JCA specific API about WorkAdapter
 * 
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class WorkAdapterAndWorkListenerTestCase
{

   /**
    * Injecting embedded work manager
    */
   @Resource(mappedName = "java:/eis/WorkConnectionFactory")
   private WorkConnectionFactory wcf;

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
    * Test for paragraph 1 Section 3.3.3
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkRejectedStatus() throws Throwable
   {
      Work work1 = new ShortRunningWork();
      StatusWorkAdapter wa = new StatusWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);
      try
      {
         wcf.getConnection().doWork(work1, WorkManager.UNKNOWN, null, wa);
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
         assertEquals(wcf.getConnection().getWorkManager(), wa.getSource());
         assertEquals(work1, wa.getWork());
         assertNotNull(wa.getException());
      }
      try
      {
         wcf.getConnection().startWork(work1, WorkManager.UNKNOWN, null, wa);
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
         assertEquals(wcf.getConnection().getWorkManager(), wa.getSource());
         assertEquals(work1, wa.getWork());
         assertNotNull(wa.getException());

      }
      try
      {
         wcf.getConnection().scheduleWork(work1, WorkManager.UNKNOWN, null, wa);
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
         assertEquals(wcf.getConnection().getWorkManager(), wa.getSource());
         assertEquals(work1, wa.getWork());
         assertNotNull(wa.getException());

      }
   }

   /**
    * Test for paragraph 1 Sections 3.3.2-3.3.5
    * @throws Throwable throwable exception 
    */
   //@Test
   public void testWorkStatuses() throws Throwable
   {
      final CountDownLatch start2 = new CountDownLatch(1);
      final CountDownLatch done2 = new CountDownLatch(1);
      final CountDownLatch start3 = new CountDownLatch(1);
      final CountDownLatch done3 = new CountDownLatch(1);

      ShortRunningWork work1 = new ShortRunningWork();
      LongRunningWork work2 = new LongRunningWork(start2, done2);
      LongRunningWork work3 = new LongRunningWork(start3, done3);

      StatusWorkAdapter wa = new StatusWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);

      wcf.getConnection().doWork(work1, WorkManager.INDEFINITE, null, wa);

      assertEquals(wcf.getConnection().getWorkManager(), wa.getSource());
      assertEquals(work1, wa.getWork());

      wcf.getConnection().startWork(work2, WorkManager.INDEFINITE, null, wa);

      assertEquals(wcf.getConnection().getWorkManager(), wa.getSource());
      assertEquals(work2, wa.getWork());

      //TODO not implemented 
      //assertTrue(wa.getStartDuration()>0);

      wcf.getConnection().scheduleWork(work3, WorkManager.INDEFINITE, null, wa);

      assertEquals("should be same", 3, callbackCount.getAcceptCount());
      assertEquals("should be same", 2, callbackCount.getStartCount());
      assertEquals("should be same", 1, callbackCount.getCompletedCount());

      start2.countDown();
      start3.countDown();

      done2.await();
      done3.await();
   }
}
