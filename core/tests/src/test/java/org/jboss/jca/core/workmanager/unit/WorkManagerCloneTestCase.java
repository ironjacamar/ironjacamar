/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2026, Red Hat Inc, and individual contributors
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

import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.workmanager.spec.chapter10.common.SimpleWork;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.resource.spi.work.Work;

import org.jboss.arquillian.junit.Arquillian;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Work manager test case for cloning (JBJCA-1545): a clone must not share its shutdown state
 * or its active work with the work manager it was cloned from, or with other clones
 */
@RunWith(Arquillian.class)
public class WorkManagerCloneTestCase
{
   /**
    * Injecting embedded work manager, used as the template
    */
   @Inject(name = "WorkManager")
   WorkManager workManager;

   /**
    * Shutting down one clone must not affect the template or another clone
    * @throws Throwable throwable exception
    */
   @Test
   public void testShutdownOfOneCloneDoesNotAffectOthers() throws Throwable
   {
      WorkManager first = workManager.clone();
      WorkManager second = workManager.clone();

      CountDownLatch started = new CountDownLatch(1);
      CountDownLatch finish = new CountDownLatch(1);
      BlockingWork work = new BlockingWork(started, finish);

      try
      {
         assertFalse(first.isShutdown());
         assertFalse(second.isShutdown());

         // Keep work active on the first clone
         first.scheduleWork(work);
         assertTrue(started.await(10, TimeUnit.SECONDS));

         second.shutdown();

         assertTrue(second.isShutdown());
         assertFalse("The other clone must not be shut down", first.isShutdown());
         assertFalse("The template must not be shut down", workManager.isShutdown());
         assertFalse("Active work of the other clone must not be released", work.isReleased());

         // The other clone still accepts work
         first.doWork(new SimpleWork());
      }
      finally
      {
         finish.countDown();
         first.shutdown();
      }
   }

   /**
    * Work that stays active until it is told to finish
    */
   static class BlockingWork implements Work
   {
      private final CountDownLatch started;

      private final CountDownLatch finish;

      private volatile boolean released;

      /**
       * Constructor
       * @param started Counted down when the work is running
       * @param finish The work completes when this is counted down
       */
      BlockingWork(CountDownLatch started, CountDownLatch finish)
      {
         this.started = started;
         this.finish = finish;
      }

      /**
       * {@inheritDoc}
       */
      public void run()
      {
         started.countDown();

         try
         {
            finish.await(10, TimeUnit.SECONDS);
         }
         catch (InterruptedException e)
         {
            Thread.currentThread().interrupt();
         }
      }

      /**
       * {@inheritDoc}
       */
      public void release()
      {
         released = true;
      }

      /**
       * Was the work released
       * @return True if released
       */
      boolean isReleased()
      {
         return released;
      }
   }
}
