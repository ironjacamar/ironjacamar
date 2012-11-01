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
package org.jboss.jca.core.workmanager.transport.remote;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;
import org.jboss.jca.core.spi.workmanager.transport.Transport;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Request;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

import org.jboss.logging.Logger;
import org.jboss.threads.BlockingExecutor;

/**
 * An abstract transport for remote communication
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @param <T> the type
 */
public abstract class AbstractRemoteTransport<T> implements Transport
{
   /** The logger */
   protected static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, AbstractRemoteTransport.class.getName());

   /** Whether trace is enabled */
   protected static boolean trace = log.isTraceEnabled();

   /** Distributed work manager instance */
   protected DistributedWorkManager dwm;

   /** The kernel executorService*/
   protected ExecutorService executorService;

   /** The work manager */
   protected Map<String, T> workManagers;

   /**
    * Constructor
    */
   public AbstractRemoteTransport()
   {
      this.dwm = null;
      this.executorService = null;
      this.workManagers = Collections.synchronizedMap(new HashMap<String, T>());
   }

   /**
    * Init
    */
   protected void init()
   {
      if (getWorkManagers() != null)
      {
         for (Map.Entry<String, T> entry : getWorkManagers().entrySet())
         {
            String id = entry.getKey();

            for (NotificationListener nl : dwm.getNotificationListeners())
            {
               nl.join(id);
               nl.updateShortRunningFree(id, getShortRunningFree(id));
               nl.updateLongRunningFree(id, getLongRunningFree(id));
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setDistributedWorkManager(DistributedWorkManager dwm)
   {
      this.dwm = dwm;
      init();
   }

   /**
    * get The distributed work manager
    * @return the ditributed work manager
    */
   public DistributedWorkManager getDistributedWorkManager()
   {
      return dwm;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public long ping(String id)
   {
      if (trace)
         log.tracef("PING(%s)", id);

      if (dwm.getId().equals(id))
         return localPing();

      long start = System.currentTimeMillis();
      try
      {
         T address = workManagers.get(id);
         sendMessage(address, Request.PING);
      }
      catch (WorkException e1)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Error", e1);
         }
         return Long.MAX_VALUE;
      }

      return System.currentTimeMillis() - start;
   }

   @Override
   public long getShortRunningFree(String id)
   {
      if (trace)
         log.tracef("GET_SHORT_RUNNING_FREE(%s)", id);

      if (dwm.getId().equals(id))
         return localGetShortRunningFree();

      try
      {
         T address = workManagers.get(id);
         return sendMessage(address, Request.GET_SHORTRUNNING_FREE);
      }
      catch (WorkException e1)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Error", e1);
         }
         return 0L;
      }
   }

   @Override
   public long getLongRunningFree(String id)
   {
      if (trace)
         log.tracef("GET_LONGRUNNING_FREE(%s)", id);

      if (dwm.getId().equals(id))
         return localGetLongRunningFree();

      try
      {
         T address = workManagers.get(id);
         return sendMessage(address, Request.GET_LONGRUNNING_FREE);
      }
      catch (WorkException e1)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Error", e1);
         }
         return 0L;
      }
   }

   @Override
   public void updateShortRunningFree(String id, long freeCount)
   {
      if (trace)
         log.tracef("UPDATE_SHORT_RUNNING_FREE(%s,%d) from %s", id, freeCount, dwm.getId());
      try
      {
         for (Entry<String, T> entry : workManagers.entrySet())
         {
            if (entry.getKey().equals(dwm.getId()))
            {
               localUpdateShortRunningFree(id, freeCount);
            }
            else
            {
               sendMessage(entry.getValue(), Request.UPDATE_SHORTRUNNING_FREE, id, freeCount);
            }
         }
      }
      catch (WorkException e1)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Error", e1);
         }
      }
   }

   @Override
   public void updateLongRunningFree(String id, long freeCount)
   {
      if (trace)
         log.tracef("UPDATE_LONG_RUNNING_FREE(%s,%d) from %s", id, freeCount, dwm.getId());
      try
      {
         for (Entry<String, T> entry : workManagers.entrySet())
         {
            if (entry.getKey().equals(dwm.getId()))
            {
               localUpdateLongRunningFree(id, freeCount);
            }
            else
            {
               sendMessage(entry.getValue(), Request.UPDATE_LONGRUNNING_FREE, id, freeCount);
            }
         }
      }
      catch (WorkException e1)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Error", e1);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void deltaDoWorkAccepted(String id)
   {
      if (trace)
         log.tracef("DELTA_DOWORK_ACCEPTED(%s) from %s", id, dwm.getId());

      if (!dwm.getId().equals(id))
      {
         try
         {
            T address = workManagers.get(id);
            sendMessage(address, Request.DELTA_DOWORK_ACCEPTED);
         }
         catch (WorkException e1)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Error", e1);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void deltaDoWorkRejected(String id)
   {
      if (trace)
         log.tracef("DELTA_DOWORK_REJECTED(%s) from %s", id, dwm.getId());

      if (!dwm.getId().equals(id))
      {
         try
         {
            T address = workManagers.get(id);
            sendMessage(address, Request.DELTA_DOWORK_REJECTED);
         }
         catch (WorkException e1)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Error", e1);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void deltaStartWorkAccepted(String id)
   {
      if (trace)
         log.tracef("DELTA_STARTWORK_ACCEPTED(%s) from %s", id, dwm.getId());

      if (!dwm.getId().equals(id))
      {
         try
         {
            T address = workManagers.get(id);
            sendMessage(address, Request.DELTA_STARTWORK_ACCEPTED);
         }
         catch (WorkException e1)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Error", e1);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void deltaStartWorkRejected(String id)
   {
      if (trace)
         log.tracef("DELTA_STARTWORK_REJECTED(%s) from %s", id, dwm.getId());

      if (!dwm.getId().equals(id))
      {
         try
         {
            T address = workManagers.get(id);
            sendMessage(address, Request.DELTA_STARTWORK_REJECTED);
         }
         catch (WorkException e1)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Error", e1);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void deltaScheduleWorkAccepted(String id)
   {
      if (trace)
         log.tracef("DELTA_SCHEDULEWORK_ACCEPTED(%s) from %s", id, dwm.getId());

      if (!dwm.getId().equals(id))
      {
         try
         {
            T address = workManagers.get(id);
            sendMessage(address, Request.DELTA_SCHEDULEWORK_ACCEPTED);
         }
         catch (WorkException e1)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Error", e1);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void deltaScheduleWorkRejected(String id)
   {
      if (trace)
         log.tracef("DELTA_SCHEDULEWORK_REJECTED(%s) from %s", id, dwm.getId());

      if (!dwm.getId().equals(id))
      {
         try
         {
            T address = workManagers.get(id);
            sendMessage(address, Request.DELTA_SCHEDULEWORK_REJECTED);
         }
         catch (WorkException e1)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Error", e1);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void deltaWorkSuccessful(String id)
   {
      if (trace)
         log.tracef("DELTA_WORK_SUCCESSFUL(%s) from %s", id, dwm.getId());

      if (!dwm.getId().equals(id))
      {
         try
         {
            T address = workManagers.get(id);
            sendMessage(address, Request.DELTA_WORK_SUCCESSFUL);
         }
         catch (WorkException e1)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Error", e1);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void deltaWorkFailed(String id)
   {
      if (trace)
         log.tracef("DELTA_WORK_FAILED(%s) from %s", id, dwm.getId());

      if (!dwm.getId().equals(id))
      {
         try
         {
            T address = workManagers.get(id);
            sendMessage(address, Request.DELTA_WORK_FAILED);
         }
         catch (WorkException e1)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Error", e1);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void doWork(String id, DistributableWork work) throws WorkException
   {
      if (trace)
         log.tracef("DO_WORK(%s, %s)", id, work);

      T address = workManagers.get(id);

      sendMessage(address, Request.DO_WORK, work);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void scheduleWork(String id, DistributableWork work) throws WorkException
   {
      if (trace)
         log.tracef("SCHEDULE_WORK(%s, %s)", id, work);

      T address = workManagers.get(id);

      sendMessage(address, Request.SCHEDULE_WORK, work);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public long startWork(String id, DistributableWork work) throws WorkException
   {
      if (trace)
         log.tracef("START_WORK(%s, %s)", id, work);

      T address = workManagers.get(id);

      return sendMessage(address, Request.START_WORK, work);
   }

   /**
    * Get the executorService.
    *
    * @return the executorService.
    */
   public ExecutorService getExecutorService()
   {
      return executorService;
   }

   /**
    * Set the executorService.
    *
    * @param executorService The executorService to set.
    */
   public void setExecutorService(ExecutorService executorService)
   {
      this.executorService = executorService;
   }

   /**
    * Get the workManagers.
    *
    * @return the workManagers.
    */
   public Map<String, T> getWorkManagers()
   {
      return workManagers;
   }

   /**
    * Set the workManagers.
    *
    * @param workManagers The workManagers to set.
    */
   public void setWorkManagers(Map<String, T> workManagers)
   {
      this.workManagers = workManagers;
   }

   /**
    *
    * join
    *
    * @param id the id
    * @param address the address
    */
   public void join(String id, T address)
   {
      if (trace)
         log.tracef("JOIN(%s, %s)", id, address);

      this.getWorkManagers().put(id, address);

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.join(id);
      }
   }

   /**
    *
    * leave
    *
    * @param id the id
    */
   public void leave(String id)
   {
      if (trace)
         log.tracef("LEAVE(%s)", id);

      this.getWorkManagers().remove(id);

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.leave(id);
      }
   }

   /**
    *
    * localPing
    *
    * @return the ping value
    */
   public Long localPing()
   {
      //do nothing, just send an answer.
      if (trace)
         log.tracef("LOCAL_PING()");
      return 0L;
   }

   /**
    *
    * localDoWork
    *
    * @param work the work
    * @throws WorkException in case of error
    */
   public void localDoWork(DistributableWork work) throws WorkException
   {
      if (trace)
         log.tracef("LOCAL_DO_WORK(%s)", work);

      this.getDistributedWorkManager().localDoWork(work);
   }

   /**
    *
    * localStartWork
    *
    * @param work the work
    * @return the start value
    * @throws WorkException in case of error
    */
   public Long localStartWork(DistributableWork work) throws WorkException
   {
      if (trace)
         log.tracef("LOCAL_START_WORK(%s)", work);

      return this.getDistributedWorkManager().localStartWork(work);
   }

   /**
    *
    * localScheduleWork
    *
    * @param work the work
    * @throws WorkException in case of error
    */
   public void localScheduleWork(DistributableWork work) throws WorkException
   {
      if (trace)
         log.tracef("LOCAL_SCHEDULE_WORK(%s)", work);

      this.getDistributedWorkManager().localScheduleWork(work);
   }

   /**
    *
    * localGetShortRunningFree
    *
    * @return the free count
    */
   public Long localGetShortRunningFree()
   {
      if (trace)
         log.tracef("LOCAL_GET_SHORTRUNNING_FREE()");

      BlockingExecutor executor = this.getDistributedWorkManager().getShortRunningThreadPool();
      if (executor != null)
      {
         return executor.getNumberOfFreeThreads();
      }
      else
      {
         return 0L;
      }
   }

   /**
    *
    * localGetLongRunningFree
    *
    * @return the free count
    */
   public Long localGetLongRunningFree()
   {
      if (trace)
         log.tracef("LOCAL_GET_LONGRUNNING_FREE()");

      BlockingExecutor executor = this.getDistributedWorkManager().getLongRunningThreadPool();
      if (executor != null)
      {
         return executor.getNumberOfFreeThreads();
      }
      else
      {
         return 0L;
      }
   }

   /**
    * localUpdateLongRunningFree
    *
    * @param id the id
    * @param freeCount the free count
    */
   public void localUpdateLongRunningFree(String id, Long freeCount)
   {
      if (trace)
         log.tracef("LOCAL_UPDATE_LONGRUNNING_FREE(%s, %d)", id, freeCount);


      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.updateLongRunningFree(id, freeCount);
      }
   }

   /**
    * localUpdateShortRunningFree
    *
    * @param id the id
    * @param freeCount the free count
    */
   public void localUpdateShortRunningFree(String id, Long freeCount)
   {
      if (trace)
         log.tracef("LOCAL_UPDATE_SHORTRUNNING_FREE(%s, %d)", id, freeCount);

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.updateShortRunningFree(id, freeCount);
      }
   }

   /**
    * Local delta doWork accepted
    */
   public void localDeltaDoWorkAccepted()
   {
      if (trace)
         log.tracef("LOCAL_DELTA_DOWORK_ACCEPTED()");

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.deltaDoWorkAccepted();
      }
   }

   /**
    * Local delta doWork rejected
    */
   public void localDeltaDoWorkRejected()
   {
      if (trace)
         log.tracef("LOCAL_DELTA_DOWORK_REJECTED()");

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.deltaDoWorkRejected();
      }
   }

   /**
    * Local delta startWork accepted
    */
   public void localDeltaStartWorkAccepted()
   {
      if (trace)
         log.tracef("LOCAL_DELTA_STARTWORK_ACCEPTED()");

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.deltaStartWorkAccepted();
      }
   }

   /**
    * Local delta startWork rejected
    */
   public void localDeltaStartWorkRejected()
   {
      if (trace)
         log.tracef("LOCAL_DELTA_STARTWORK_REJECTED()");

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.deltaStartWorkRejected();
      }
   }

   /**
    * Local delta scheduleWork accepted
    */
   public void localDeltaScheduleWorkAccepted()
   {
      if (trace)
         log.tracef("LOCAL_DELTA_SCHEDULEWORK_ACCEPTED()");

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.deltaScheduleWorkAccepted();
      }
   }

   /**
    * Local delta scheduleWork rejected
    */
   public void localDeltaScheduleWorkRejected()
   {
      if (trace)
         log.tracef("LOCAL_DELTA_SCHEDULEWORK_REJECTED()");

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.deltaScheduleWorkRejected();
      }
   }

   /**
    * Local delta work successful
    */
   public void localDeltaWorkSuccessful()
   {
      if (trace)
         log.tracef("LOCAL_DELTA_WORK_SUCCESSFUL()");

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.deltaWorkSuccessful();
      }
   }

   /**
    * Local delta work failed
    */
   public void localDeltaWorkFailed()
   {
      if (trace)
         log.tracef("LOCAL_DELTA_WORK_FAILED()");

      for (NotificationListener nl : this.getDistributedWorkManager().getNotificationListeners())
      {
         nl.deltaWorkFailed();
      }
   }

   /**
    * send a messagge using specific protocol. Method overridden in specific protocol implementation classes
    *
    * @param address the address
    * @param request the request
    * @param parameters the parameters
    * @return the returned long value. Can be null if requested operation return a void
    * @throws WorkException in case of problem with the work
    */
   protected abstract Long sendMessage(T address, Request request, Serializable... parameters)
      throws WorkException;
}
