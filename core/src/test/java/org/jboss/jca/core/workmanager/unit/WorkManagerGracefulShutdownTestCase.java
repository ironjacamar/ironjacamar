/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.workmanager.unit;

import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkRejectedException;

import org.jboss.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Work manager test cases for graceful shutdown
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WorkManagerGracefulShutdownTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static final Logger LOG = Logger.getLogger(WorkManagerGracefulShutdownTestCase.class);

   /*
    * Embedded
    */
   private Embedded embedded;

   /**
    * Work manager
    */
   private WorkManager workManager;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Test graceful shutdown
    * @throws Throwable throwable exception 
    */
   @Test
   public void testShutdown() throws Throwable
   {
      assertFalse(workManager.isShutdown());

      workManager.prepareShutdown();

      assertTrue(workManager.isShutdown());
   }

   /**
    * Test doWork
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testDoWork() throws Throwable
   {
      workManager.prepareShutdown();

      workManager.doWork(new Work()
         {
            /**
             * Run
             */
            public void run()
            {
            }

            /**
             * Release
             */
            public void release()
            {
            }
         });
   }

   /**
    * Test startWork
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testStartWork() throws Throwable
   {
      workManager.prepareShutdown();

      workManager.startWork(new Work()
         {
            /**
             * Run
             */
            public void run()
            {
            }

            /**
             * Release
             */
            public void release()
            {
            }
         });
   }

   /**
    * Test scheduleWork
    * @throws Throwable throwable exception 
    */
   @Test(expected = WorkRejectedException.class)
   public void testScheduleWork() throws Throwable
   {
      workManager.prepareShutdown();

      workManager.scheduleWork(new Work()
         {
            /**
             * Run
             */
            public void run()
            {
            }

            /**
             * Release
             */
            public void release()
            {
            }
         });
   }

   /**
    * Test doWorkRejected
    */
   @Test
   public void testDoWorkRejected() throws Throwable
   {
      workManager.prepareShutdown();
      TestWorkListener listener = new TestWorkListener();
      try {
         workManager.doWork(new Work()
            {
               /**
                * Run
                */
               public void run()
               {
               }

               /**
                * Release
                */
               public void release()
               {
               }
            }, WorkManager.INDEFINITE, null, listener);
      } catch (WorkRejectedException e) {}
      assertTrue(listener.wasRejected);
   }

   /**
    * Test startWorkRejected
    */
   @Test
   public void testStartWorkRejected() throws Throwable
   {
      workManager.prepareShutdown();
      TestWorkListener listener = new TestWorkListener();
      try {
         workManager.startWork(new Work()
            {
               /**
                * Run
                */
               public void run()
               {
               }

               /**
                * Release
                */
               public void release()
               {
               }
            }, WorkManager.INDEFINITE, null, listener);
      } catch (WorkRejectedException e) {}
      assertTrue(listener.wasRejected);
   }

   /**
    * Test scheduleWorkRejected
    */
   @Test
   public void testScheduleWorkRejected() throws Throwable
   {
      workManager.prepareShutdown();
      TestWorkListener listener = new TestWorkListener();
      try {
         workManager.scheduleWork(new Work()
            {
               /**
                * Run
                */
               public void run()
               {
               }

               /**
                * Release
                */
               public void release()
               {
               }
            }, WorkManager.INDEFINITE, null, listener);
      } catch (WorkRejectedException e) {}
      assertTrue(listener.wasRejected);
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @Before
   public void before() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create();

      // Startup
      embedded.startup();

      workManager = embedded.lookup("WorkManager", WorkManager.class);
      assertNotNull(workManager);
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @After
   public void after() throws Throwable
   {
      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }

   private class TestWorkListener implements WorkListener {
      boolean wasRejected = false;

      /**
      * workAccepted
      */
      public void workAccepted(WorkEvent e)
      {
      }

      /**
      * workCompleted
      */
      public void workCompleted(WorkEvent e)
      {
      }

      /**
      * workRejected
      */
      public void workRejected(WorkEvent e)
      {
         wasRejected = true;
      }

      /**
      * workStarted
      */
      public void workStarted(WorkEvent e)
      {
      }
   }
}
