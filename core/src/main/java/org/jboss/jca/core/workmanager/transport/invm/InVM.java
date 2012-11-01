/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.workmanager.transport.invm;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;
import org.jboss.jca.core.spi.workmanager.transport.Transport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;
import org.jboss.threads.BlockingExecutor;

/**
 * The in-vm transport
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class InVM implements Transport
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, InVM.class.getName());

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /** Distributed work manager instance */
   protected DistributedWorkManager dwm;

   /** The work manager */
   private final Map<String, DistributedWorkManager> workManagers;

   /**
    * Constructor
    */
   public InVM()
   {
      this.dwm = null;
      this.workManagers = Collections.synchronizedMap(new HashMap<String, DistributedWorkManager>());
   }

   /**
    * {@inheritDoc}
    */
   public void setDistributedWorkManager(DistributedWorkManager dwm)
   {
      this.dwm = dwm;
   }

   /**
    * {@inheritDoc}
    */
   public long ping(String dwm)
   {
      if (!workManagers.keySet().contains(dwm))
         return Long.MAX_VALUE;

      return 0L;
   }

   @Override
   public long getShortRunningFree(String dwm)
   {
      if (!workManagers.keySet().contains(dwm))
         return 0L;
      BlockingExecutor executor = workManagers.get(dwm).getShortRunningThreadPool();
      if (executor != null)
      {
         return executor.getNumberOfFreeThreads();
      }
      else
      {
         return 0L;
      }
   }

   @Override
   public long getLongRunningFree(String dwm)
   {
      if (!workManagers.keySet().contains(dwm))
         return 0L;
      BlockingExecutor executor = workManagers.get(dwm).getLongRunningThreadPool();

      if (executor != null)
      {
         return executor.getNumberOfFreeThreads();
      }
      else
      {
         return 0L;
      }
   }

   @Override
   public void updateShortRunningFree(String id, long freeCount)
   {
      for (NotificationListener nl : dwm.getNotificationListeners())
      {
         nl.updateShortRunningFree(id, freeCount);
      }
   }

   @Override
   public void updateLongRunningFree(String id, long freeCount)
   {
      for (NotificationListener nl : dwm.getNotificationListeners())
      {
         nl.updateLongRunningFree(id, freeCount);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void deltaDoWorkAccepted(String id)
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         for (NotificationListener nl : dwm.getNotificationListeners())
         {
            nl.deltaDoWorkAccepted();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void deltaDoWorkRejected(String id)
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         for (NotificationListener nl : dwm.getNotificationListeners())
         {
            nl.deltaDoWorkRejected();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void deltaStartWorkAccepted(String id)
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         for (NotificationListener nl : dwm.getNotificationListeners())
         {
            nl.deltaStartWorkAccepted();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void deltaStartWorkRejected(String id)
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         for (NotificationListener nl : dwm.getNotificationListeners())
         {
            nl.deltaStartWorkRejected();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void deltaScheduleWorkAccepted(String id)
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         for (NotificationListener nl : dwm.getNotificationListeners())
         {
            nl.deltaScheduleWorkAccepted();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void deltaScheduleWorkRejected(String id)
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         for (NotificationListener nl : dwm.getNotificationListeners())
         {
            nl.deltaScheduleWorkRejected();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void deltaWorkSuccessful(String id)
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         for (NotificationListener nl : dwm.getNotificationListeners())
         {
            nl.deltaWorkSuccessful();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void deltaWorkFailed(String id)
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         for (NotificationListener nl : dwm.getNotificationListeners())
         {
            nl.deltaWorkFailed();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void doWork(String id, DistributableWork work) throws WorkException
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         dwm.localDoWork(work);
      }
      else
      {
         // TODO
         throw new WorkException();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void scheduleWork(String id, DistributableWork work) throws WorkException
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         dwm.localScheduleWork(work);
      }
      else
      {
         // TODO
         throw new WorkException();
      }
   }

   /**
    * {@inheritDoc}
    */
   public long startWork(String id, DistributableWork work) throws WorkException
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         return dwm.localStartWork(work);
      }
      else
      {
         // TODO
         throw new WorkException();
      }
   }

   /**
    * Add a distributed work manager
    * @param dwm The work manager
    */
   public void addDistributedWorkManager(DistributedWorkManager dwm)
   {
      if (trace)
         log.tracef("Adding distributed work manager: %s", dwm);

      workManagers.put(dwm.getId(), dwm);

      for (NotificationListener nl : this.dwm.getNotificationListeners())
      {
         nl.join(dwm.getId());

         nl.updateShortRunningFree(dwm.getId(), getShortRunningFree(dwm.getId()));
         nl.updateLongRunningFree(dwm.getId(), getLongRunningFree(dwm.getId()));
      }
   }

   /**
    * Remove a distributed work manager
    * @param dwm The work manager
    */
   public void removeDistributedWorkManager(DistributedWorkManager dwm)
   {
      if (trace)
         log.tracef("Removing distributed work manager: %s", dwm);

      workManagers.remove(dwm.getId());

      for (NotificationListener nl : this.dwm.getNotificationListeners())
      {
         nl.leave(dwm.getId());
      }
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("InVM@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[workManagers=").append(workManagers.keySet());
      sb.append("]");

      return sb.toString();
   }
}
