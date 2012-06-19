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
import org.jboss.jca.core.api.workmanager.WorkManagerStatistics;
import org.jboss.jca.embedded.arquillian.Inject;

import java.util.concurrent.CountDownLatch;

import javax.resource.spi.work.Work;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Work manager test cases for graceful shutdown
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
@RunWith(Arquillian.class)
public class WorkManagerShutdownTestCase
{

   private static final Logger LOG = Logger.getLogger(WorkManagerShutdownTestCase.class);

   /**
    * Injecting embedded work manager
    */
   @Inject(name = "WorkManager")
   WorkManager workManager;

   /**
    * Test graceful shutdown
    * @throws Throwable throwable exception 
    */
   @Test
   public void testShutdown() throws Throwable
   {
      WorkManagerStatistics stat = workManager.getStatistics();
      assertNotNull(stat);
      assertFalse(workManager.isShutdown());

      CountDownLatch start1 = new CountDownLatch(1);
      CountDownLatch done1 = new CountDownLatch(1);
      CountDownLatch start2 = new CountDownLatch(1);
      CountDownLatch done2 = new CountDownLatch(1);

      SWork work1 = new SWork(start1, done1);
      SWork work2 = new SWork(start2, done2);
      workManager.startWork(work1);
      workManager.scheduleWork(work2);
      assertEquals(1, stat.getStartWorkAccepted());
      assertEquals(1, stat.getScheduleWorkAccepted());

      assertFalse(work1.isReleased());
      assertFalse(work2.isReleased());
      while (stat.getWorkActive() < 2);
      LOG.info("Before shutdown:" + stat.toString());

      workManager.shutdown();
      assertTrue(workManager.isShutdown());

      assertEquals(2, stat.getWorkSuccessful());
      done1.await();
      done2.await();
      assertTrue(work1.isReleased());
      assertTrue(work2.isReleased());
      assertEquals(0, stat.getWorkActive());
      LOG.info("After shutdown:" + stat.toString());
   }

   /**
    * Special work implementation for test purpose
    * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
    *
    */
   static class SWork implements Work
   {
      /** released flag */
      boolean released;

      private CountDownLatch start;

      private CountDownLatch done;

      /**
       * Constructor
       * @param s - start CountDownLatch
       * @param d - done CountDownLatch
       */
      public SWork(CountDownLatch s, CountDownLatch d)
      {
         start = s;
         done = d;
         setReleased(false);
      }

      /**
       * {@inheritDoc}
       */
      public void run()
      {
         try
         {
            start.await();
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
         setReleased(true);

      }

      /**
       * {@inheritDoc}
       */
      public void release()
      {
         if (!isReleased())
            start.countDown();
         else
            done.countDown();
      }

      /**
       * getter
       * @return released
       */
      public boolean isReleased()
      {
         return released;
      }

      /**
       * setter
       * @param v - value for released
       */
      public void setReleased(boolean v)
      {
         released = v;
      }
   }
}
