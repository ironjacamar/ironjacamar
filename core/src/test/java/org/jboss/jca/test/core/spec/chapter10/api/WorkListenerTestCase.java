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
import org.jboss.jca.test.core.spec.chapter10.common.ShortRunningWork;

import java.util.concurrent.CountDownLatch;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;

import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * WorkListenerTestCase.
 * 
 * Tests for the JCA specific API about WorkListener
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkListenerTestCase
{
   /*
    * Bootstrap (MC Facade)
    */
   private static EmbeddedTestMcBootstrap bootstrap;


   /**
    * workAccepted method
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkAcceptedStatus() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      Work work1 = new ShortRunningWork();
      Work work2 = new ShortRunningWork();
      Work work3 = new ShortRunningWork();
      
      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);

      workManager.doWork(work1, WorkManager.INDEFINITE, null, wa);
      workManager.startWork(work2, WorkManager.INDEFINITE, null, wa);
      workManager.scheduleWork(work3, WorkManager.INDEFINITE, null, wa);

      assertEquals("should be same", 3, callbackCount.getAcceptCount());
   }   
   
   /**
    * Test for paragraph 1 Section 3.3.3
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testWorkRejectedStatus() throws Throwable
   {
      //TODO
   }   
   
   /**
    * Test for paragraph 1 Section 3.3.4
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkStartedStatus() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start = new CountDownLatch(1);
      final CountDownLatch done = new CountDownLatch(1);
      
      Work work1 = new ShortRunningWork();
      Work work2 = new ShortRunningWork();
      Work work3 = new LongRunningWork(start, done);
      
      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);

      workManager.doWork(work1, WorkManager.INDEFINITE, null, wa);
      workManager.startWork(work2, WorkManager.INDEFINITE, null, wa);
      workManager.scheduleWork(work3, WorkManager.INDEFINITE, null, wa);

      assertEquals("should be same", 3, callbackCount.getAcceptCount());
      //assertEquals("should be same", 2, callbackCount.getStartCount());
      //TODO workManagerImpl maybe have a bug here
      
      start.countDown();

      done.await();
   }   
   
   /**
    * Test for paragraph 1 Section 3.3.5
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkCompletedStatus() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      final CountDownLatch start2 = new CountDownLatch(1);
      final CountDownLatch done2 = new CountDownLatch(1);
      final CountDownLatch start3 = new CountDownLatch(1);
      final CountDownLatch done3 = new CountDownLatch(1);
      
      Work work1 = new ShortRunningWork();
      Work work2 = new LongRunningWork(start2, done2);
      Work work3 = new LongRunningWork(start3, done3);
      
      MyWorkAdapter wa = new MyWorkAdapter();
      CallbackCount callbackCount = new CallbackCount();
      wa.setCallbackCount(callbackCount);

      workManager.doWork(work1, WorkManager.INDEFINITE, null, wa);
      workManager.startWork(work2, WorkManager.INDEFINITE, null, wa);
      workManager.scheduleWork(work3, WorkManager.INDEFINITE, null, wa);
      
      assertEquals("should be same", 3, callbackCount.getAcceptCount());
      assertEquals("should be same", 1, callbackCount.getCompletedCount());

      start2.countDown();
      start3.countDown();
      
      done2.await();
      done3.await();
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
      bootstrap.deploy(WorkListenerTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.deploy(WorkListenerTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      
      // Deploy Beans
      bootstrap.deploy(WorkListenerTestCase.class);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy Transaction and Naming
      bootstrap.undeploy(WorkListenerTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.undeploy(WorkListenerTestCase.class.getClassLoader(), "naming-jboss-beans.xml");

      // Undeploy Beans
      bootstrap.undeploy(WorkListenerTestCase.class);

      // Shutdown MC
      bootstrap.shutdown();

      // Set Bootstrap to null
      bootstrap = null;
   }
}
