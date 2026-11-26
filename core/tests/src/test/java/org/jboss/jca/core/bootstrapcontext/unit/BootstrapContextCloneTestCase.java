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

package org.jboss.jca.core.bootstrapcontext.unit;

import org.jboss.jca.core.api.bootstrap.CloneableBootstrapContext;
import org.jboss.jca.core.bootstrapcontext.BaseCloneableBootstrapContext;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Bootstrap context test case for cloning (JBJCA-1546): a clone must not share its timers
 * with the bootstrap context it was cloned from, or with other clones
 */
public class BootstrapContextCloneTestCase
{
   /**
    * Shutting down one clone must not cancel the timers of the template or of another clone
    * @throws Throwable throwable exception
    */
   @Test
   public void testShutdownOfOneCloneDoesNotCancelOtherTimers() throws Throwable
   {
      BaseCloneableBootstrapContext template = new BaseCloneableBootstrapContext();
      template.setName("template");

      // The template has a timer before it is cloned
      Timer templateTimer = template.createTimer();

      CloneableBootstrapContext first = template.clone();
      CloneableBootstrapContext second = template.clone();

      try
      {
         Timer firstTimer = first.createTimer();

         second.shutdown();

         // A cancelled timer throws IllegalStateException when a task is scheduled
         assertTimerIsActive("Timer of the other clone must not be cancelled", firstTimer);
         assertTimerIsActive("Timer of the template must not be cancelled", templateTimer);
      }
      finally
      {
         first.shutdown();
         template.shutdown();
      }
   }

   /**
    * Assert that a task can be scheduled and is executed by the timer
    * @param message The assertion message
    * @param timer The timer
    * @throws Throwable throwable exception
    */
   private void assertTimerIsActive(String message, Timer timer) throws Throwable
   {
      final CountDownLatch executed = new CountDownLatch(1);

      try
      {
         timer.schedule(new TimerTask()
         {
            public void run()
            {
               executed.countDown();
            }
         }, 0);
      }
      catch (IllegalStateException e)
      {
         fail(message);
      }

      assertTrue(message, executed.await(10, TimeUnit.SECONDS));
   }
}
