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
package org.jboss.jca.test.core.spec.chapter10.section3;

import org.jboss.jca.test.core.spec.chapter10.SimpleWork;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkAdapter;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

import org.jboss.ejb3.test.mc.bootstrap.EmbeddedTestMcBootstrap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * WorkListenerInterfaceTestCase.
 * 
 * Tests for the JCA specific Chapter 10 Section 3.3/3.4
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class WorkListenerInterfaceTestCase
{
   /*
    * Bootstrap (MC Facade)
    */
   private static EmbeddedTestMcBootstrap bootstrap;
   
   /**
    * Test for paragraph 1 Section 3.3.1
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testWorkSubmitStatus() throws Throwable
   {
   }   

   /**
    * Test for paragraph 1 Section 3.3.2
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkAcceptedStatus() throws Throwable
   {
      final Called called = new Called();
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      SimpleWork work1 = new SimpleWork();
      SimpleWork work2 = new SimpleWork();
      SimpleWork work3 = new SimpleWork();
      
      WorkListener wl1 = new WorkAdapter()
      {
         public void workAccepted(WorkEvent e) 
         {
            assertEquals(e.getType(), WorkEvent.WORK_ACCEPTED);
            synchronized (this) 
            {
               called.acceptCount++;
            }
         }
      };
      workManager.doWork(work1, 0, null, wl1);
      workManager.startWork(work2, 0, null, wl1);
      workManager.scheduleWork(work3, 0, null, wl1);
      assertEquals("should be same", called.acceptCount, 3);
   }   
   
   /**
    * Test for paragraph 1 Section 3.3.3
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testWorkRejectedStatus() throws Throwable
   {
   }   
   
   /**
    * Test for paragraph 1 Section 3.3.4
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testWorkStartedStatus() throws Throwable
   {
      final Called called = new Called();
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      SimpleWork work1 = new SimpleWork();
      SimpleWork work2 = new SimpleWork();
      SimpleWork work3 = new SimpleWork();
      work3.setBlockRun(true);
      
      WorkListener wl1 = new WorkAdapter()
      {
         public void workAccepted(WorkEvent e) 
         {
            assertEquals(e.getType(), WorkEvent.WORK_ACCEPTED);
            synchronized (this) 
            {
               called.acceptCount++;
            }
         }
         public void workStarted(WorkEvent e) 
         {
            assertEquals(e.getType(), WorkEvent.WORK_STARTED);
            synchronized (this) 
            {
               called.startCount++;
            }
         }
      };
      workManager.doWork(work1, SimpleWork.BLOCK_TIME, null, wl1);
      workManager.startWork(work2, SimpleWork.BLOCK_TIME, null, wl1);
      workManager.scheduleWork(work3, SimpleWork.BLOCK_TIME, null, wl1);
      assertEquals("should be same", called.acceptCount, 3);
      assertEquals("should be same", called.startCount, 2);
      //TODO here maybe we have a bug
   }   
   
   /**
    * Test for paragraph 1 Section 3.3.5
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkCompletedStatus() throws Throwable
   {
      final Called called = new Called();
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      SimpleWork work1 = new SimpleWork();
      SimpleWork work2 = new SimpleWork();
      work2.setBlockRun(true);
      SimpleWork work3 = new SimpleWork();
      work3.setBlockRun(true);
      
      WorkListener wl1 = new WorkAdapter()
      {
         public void workAccepted(WorkEvent e) 
         {
            assertEquals(e.getType(), WorkEvent.WORK_ACCEPTED);
            synchronized (this) 
            {
               called.acceptCount++;
            }
         }
         public void workCompleted(WorkEvent e) 
         {
            assertEquals(e.getType(), WorkEvent.WORK_COMPLETED);
            synchronized (this) 
            {
               called.completedCount++;
            }
         }
      };
      workManager.doWork(work1, SimpleWork.BLOCK_TIME, null, wl1);
      workManager.startWork(work2, SimpleWork.BLOCK_TIME, null, wl1);
      workManager.scheduleWork(work3, SimpleWork.BLOCK_TIME, null, wl1);
      assertEquals("should be same", called.acceptCount, 3);
      assertEquals("should be same", called.completedCount, 1);
   }
   
   /**
    * Test for bullet 1 paragraph 2 Section 3.4
    * The WorkEvent instance provides The event type.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkEventInheritEventObject() throws Throwable
   {
      javax.resource.spi.work.WorkEvent workEvent = 
         new javax.resource.spi.work.WorkEvent(new Object(), 0, null, null);
      assertTrue(workEvent instanceof java.util.EventObject);
   }   
   
   /**
    * Test for bullet 2 paragraph 2 Section 3.4
    * The source object, that is, the Work instance, on which the event initially occurred.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testSourceObjectIsInitial() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      SimpleWork work1 = new SimpleWork();
     
      MyWorkAdapter wl1 = new MyWorkAdapter();
      
      workManager.doWork(work1, 0, null, wl1);

      assertEquals("should be same object", workManager , wl1.getSource());
   }   
   
   /**
    * Test for bullet 3 paragraph 2 Section 3.4
    * A handle to the associated Work instance.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testHandleAssociatedWork() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      SimpleWork work1 = new SimpleWork();
     
      MyWorkAdapter wl1 = new MyWorkAdapter();
      
      workManager.doWork(work1, 0, null, wl1);

      assertEquals("should be same object", work1 , wl1.getWork());
   }   
   
   /**
    * Test for bullet 4 paragraph 2 Section 3.4
    * An optional start delay duration in millisecond.
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testStartDelayDuration() throws Throwable
   {
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      SimpleWork work1 = new SimpleWork();
     
      MyWorkAdapter wl1 = new MyWorkAdapter();
      
      workManager.doWork(work1, 0, null, wl1);

      assertTrue(wl1.getStartDuration() >= 0);
      //TODO it seems we haven't impl this feture
   }   
   
   /**
    * Test for paragraph 4 Section 3.4
    * The WorkAdapter class is provided as a convenience for easily creating
    * @throws Throwable throwable exception 
    */
   @Test
   public void testWorkAdapter() throws Throwable
   {
      javax.resource.spi.work.WorkAdapter workAdapter = 
         new javax.resource.spi.work.WorkAdapter();
      assertTrue(workAdapter instanceof javax.resource.spi.work.WorkListener);
   }
   
   /**
    * Test for bullet 1 Section 3.4.1
    * The WorkListener instance must not make any thread assumptions and must be thread-safe
    * @throws Throwable throwable exception 
    */
   @Ignore
   public void testWorkListenerThreadSafe() throws Throwable
   {
   }   
   
   /**
    * Test for bullet 3 Section 3.4.1
    * The WorkListener implementation must not make any assumptions on the ordering of notifications.
    * @throws Throwable throwable exception 
    */
   @Test
   public void testNotificationWithoutOrder() throws Throwable
   {
      final Called called = new Called();
      WorkManager workManager = bootstrap.lookup("WorkManager", WorkManager.class);

      SimpleWork work1 = new SimpleWork();
      SimpleWork work2 = new SimpleWork();
      
      WorkListener wl1 = new WorkAdapter()
      {
         public void workAccepted(WorkEvent e) 
         {
            synchronized (this) 
            {
               called.acceptCount++;
            }
         }
      };
      workManager.doWork(work1, 0, null, wl1);
      workManager.startWork(work2, 0, null, wl1);
      assertEquals("should be same", called.acceptCount , 2);
      
      workManager.startWork(work1, 0, null, wl1);
      workManager.doWork(work2, 0, null, wl1);
      assertEquals("should be same", called.acceptCount , 4);
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
      bootstrap.deploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "naming-jboss-beans.xml");
      bootstrap.deploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      
      // Deploy Beans
      bootstrap.deploy(WorkManagerInterfaceTestCase.class);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy Transaction and Naming
      bootstrap.undeploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "transaction-jboss-beans.xml");
      bootstrap.undeploy(WorkManagerInterfaceTestCase.class.getClassLoader(), "naming-jboss-beans.xml");

      // Undeploy Beans
      bootstrap.undeploy(WorkManagerInterfaceTestCase.class);

      // Shutdown MC
      bootstrap.shutdown();

      // Set Bootstrap to null
      bootstrap = null;
   }
   
   /**
    * class for count called times
    */
   class Called
   {
      /** count accept times */
      int acceptCount;
      /** count start times */
      int startCount;
      /** count completed times */
      int completedCount;
   }
   
   /**
    * MyWorkAdapter
    */
   class MyWorkAdapter extends WorkAdapter
   {
      /** event source */
      private Object source;
      /** event work */
      private Work work;
      /** start duration time */
      private long startDuration;
      
      /**
       * accept work 
       *
       * @param e workEvent
       */
      public void workAccepted(WorkEvent e) 
      {
         source = e.getSource();
         work = e.getWork();
         startDuration = e.getStartDuration();
      }
      
      /**
       * get event source
       *
       * @return Object source
       */
      public Object getSource()
      {
         return source;
      }
      
      /**
       * get event work
       *
       * @return Work work reference
       */
      public Work getWork()
      {
         return work;
      }
      
      /**
       * get start duration time
       *
       * @return long duration time
       */
      public long getStartDuration()
      {
         return startDuration;
      }
   }
}
