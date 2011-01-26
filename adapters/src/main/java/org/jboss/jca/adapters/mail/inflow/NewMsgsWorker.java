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

package org.jboss.jca.adapters.mail.inflow;

import java.util.concurrent.PriorityBlockingQueue;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

import org.jboss.logging.Logger;

/**
 * Handles new messages
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class NewMsgsWorker implements Work, WorkListener
{
   private static Logger log = Logger.getLogger(NewMsgsWorker.class);

   private boolean released;

   private WorkManager mgr;

   private PriorityBlockingQueue<MailActivation> pollQueue;

   /**
    * Constructor
    * @param mgr The work manager
    * @param queueSize The queue size
    */
   public NewMsgsWorker(WorkManager mgr, Integer queueSize)
   {
      this.mgr = mgr;
      this.pollQueue = new PriorityBlockingQueue<MailActivation>(queueSize.intValue());
   }

   /**
    * Watch an activation
    * @param activation The activation
    * @exception InterruptedException Thrown if the queue is interrupted
    */
   public void watch(MailActivation activation) throws InterruptedException
   {
      activation.updateNextNewMsgCheckTime(System.currentTimeMillis());

      pollQueue.put(activation);
   }

   /**
    * Release
    */
   public void release()
   {
      released = true;

      log.tracef("released");
   }

   /**
    * Run
    */
   public void run()
   {
      log.tracef("Begin run");

      while (!released)
      {
         try
         {
            MailActivation ma = pollQueue.take();

            // Wait until its time to check for new msgs
            long now = System.currentTimeMillis();
            long nextTime = ma.getNextNewMsgCheckTime();
            long sleepMS = nextTime - now;

            if (sleepMS > 0)
               Thread.sleep(sleepMS);

            if (released)
               break;

            // This has to go after the sleep otherwise we can get into an inconsistent state
            if (ma.isReleased())
                continue;

            // Now schedule excecution of the new msg check
            mgr.scheduleWork(ma, WorkManager.INDEFINITE, null, this);
         }
         catch (InterruptedException e)
         {
            log.warn("Interrupted waiting for new msg check", e);
         }
         catch (WorkException e)
         {
            log.warn("Failed to schedule new msg check", e);            
         }
      }

      log.tracef("End run");
   }

   /**
    * Work accepted
    * @param e The event
    */
   public void workAccepted(WorkEvent e)
   {
      log.tracef("workAccepted: e=%s", e);
   }

   /**
    * Work rejected
    * @param e The event
    */
   public void workRejected(WorkEvent e)
   {
      log.tracef("workRejected: e=%s", e);
   }

   /**
    * Work started
    * @param e The event
    */
   public void workStarted(WorkEvent e)
   {
      log.tracef("workStarted: e=%s", e);
   }

   /**
    * Work completed
    * @param e The event
    */
   public void workCompleted(WorkEvent e)
   {
      log.tracef("workCompleted: e=%s", e);

      MailActivation activation = (MailActivation) e.getWork();
      try
      {
         watch(activation);
      }
      catch (InterruptedException ex)
      {
         log.warn("Failed to reschedule new msg check", ex);
      }
   }
}
