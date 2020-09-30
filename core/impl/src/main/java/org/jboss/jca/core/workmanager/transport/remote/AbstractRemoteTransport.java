/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.api.workmanager.DistributedWorkManagerStatisticsValues;
import org.jboss.jca.core.api.workmanager.StatisticsExecutor;
import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.spi.workmanager.Address;
import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;
import org.jboss.jca.core.spi.workmanager.transport.Transport;
import org.jboss.jca.core.workmanager.ClassBundle;
import org.jboss.jca.core.workmanager.ClassBundleFactory;
import org.jboss.jca.core.workmanager.WorkManagerCoordinator;
import org.jboss.jca.core.workmanager.WorkManagerEvent;
import org.jboss.jca.core.workmanager.WorkManagerEventQueue;
import org.jboss.jca.core.workmanager.transport.remote.ProtocolMessages.Request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

import org.jboss.logging.Logger;

/**
 * An abstract transport for remote communication
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @param <T> the type
 */
public abstract class AbstractRemoteTransport<T> implements Transport
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, AbstractRemoteTransport.class.getName());

   /** The identifier of the transport */
   private String id;

   /** The kernel executorService*/
   protected ExecutorService executorService;

   /** The nodes */
   protected Map<Address, T> nodes;

   /**
    * Constructor
    */
   public AbstractRemoteTransport()
   {
      this.executorService = null;
      this.nodes = Collections.synchronizedMap(new HashMap<Address, T>());
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * Set the identifier
    * @param id The value
    */
   public void setId(String id)
   {
      this.id = id;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public long ping(Address address)
   {
      log.tracef("PING(%s)", address);

      if (address.getTransportId() == null || getId().equals(address.getTransportId()))
         return localPing();

      long start = System.currentTimeMillis();
      try
      {
         T addr = nodes.get(address);
         sendMessage(addr, Request.PING);
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
   public long getShortRunningFree(Address address)
   {
      log.tracef("GET_SHORT_RUNNING_FREE(%s)", address);

      if (address.getTransportId() == null || getId().equals(address.getTransportId()))
         return localGetShortRunningFree(address);

      try
      {
         T addr = nodes.get(address);
         return (long)sendMessage(addr, Request.GET_SHORTRUNNING_FREE, address);
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
   public long getLongRunningFree(Address address)
   {
      log.tracef("GET_LONGRUNNING_FREE(%s)", address);

      if (address.getTransportId() == null || getId().equals(address.getTransportId()))
         return localGetLongRunningFree(address);

      try
      {
         T addr = nodes.get(address);
         return (long)sendMessage(addr, Request.GET_LONGRUNNING_FREE, address);
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
   public void updateShortRunningFree(Address address, long freeCount)
   {
      log.tracef("UPDATE_SHORT_RUNNING_FREE(%s, %d)", address, freeCount);

      localUpdateShortRunningFree(address, freeCount);

      if (address.getTransportId() != null && getId().equals(address.getTransportId()))
      {
         for (Entry<Address, T> entry : nodes.entrySet())
         {
            Address a = entry.getKey();
            if (!getId().equals(a.getTransportId()))
            {
               try
               {
                  sendMessage(entry.getValue(), Request.UPDATE_SHORTRUNNING_FREE, address, freeCount);
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
      }
   }

   @Override
   public void updateLongRunningFree(Address address, long freeCount)
   {
      log.tracef("UPDATE_LONG_RUNNING_FREE(%s, %d)", address, freeCount);

      localUpdateLongRunningFree(address, freeCount);

      if (address.getTransportId() != null && getId().equals(address.getTransportId()))
      {
         for (Entry<Address, T> entry : nodes.entrySet())
         {
            Address a = entry.getKey();
            if (!getId().equals(a.getTransportId()))
            {
               try
               {
                  sendMessage(entry.getValue(), Request.UPDATE_LONGRUNNING_FREE, address, freeCount);
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
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DistributedWorkManagerStatisticsValues getDistributedStatistics(Address address)
   {
      log.tracef("GET_DISTRIBUTED_STATISTICS(%s)", address);

      if (address.getTransportId() == null || getId().equals(address.getTransportId()))
         return localGetDistributedStatistics(address);

      try
      {
         T addr = nodes.get(address);
         return (DistributedWorkManagerStatisticsValues)sendMessage(addr, Request.GET_DISTRIBUTED_STATISTICS, address);
      }
      catch (WorkException e1)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Error", e1);
         }
         return null;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void clearDistributedStatistics(Address address)
   {
      log.tracef("CLEAR_DISTRIBUTED_STATISTICS(%s)", address);

      if (!getId().equals(address.getTransportId()))
         localClearDistributedStatistics(address);

      if (address.getTransportId() != null && getId().equals(address.getTransportId()))
      {
         for (Entry<Address, T> entry : nodes.entrySet())
         {
            Address a = entry.getKey();
            if (!getId().equals(a.getTransportId()))
            {
               try
               {
                  sendMessage(entry.getValue(), Request.CLEAR_DISTRIBUTED_STATISTICS, address);
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
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void deltaDoWorkAccepted(Address address)
   {
      log.tracef("DELTA_DOWORK_ACCEPTED(%s)", address);

      if (address.getTransportId() != null && !getId().equals(address.getTransportId()))
      {
         try
         {
            T addr = nodes.get(address);
            sendMessage(addr, Request.DELTA_DOWORK_ACCEPTED, address);
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
   public void deltaDoWorkRejected(Address address)
   {
      log.tracef("DELTA_DOWORK_REJECTED(%s)", address);

      if (address.getTransportId() != null && !getId().equals(address.getTransportId()))
      {
         try
         {
            T addr = nodes.get(address);
            sendMessage(addr, Request.DELTA_DOWORK_REJECTED, address);
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
   public void deltaStartWorkAccepted(Address address)
   {
      log.tracef("DELTA_STARTWORK_ACCEPTED(%s)", address);

      if (address.getTransportId() != null && !getId().equals(address.getTransportId()))
      {
         try
         {
            T addr = nodes.get(address);
            sendMessage(addr, Request.DELTA_STARTWORK_ACCEPTED, address);
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
   public void deltaStartWorkRejected(Address address)
   {
      log.tracef("DELTA_STARTWORK_REJECTED(%s)", address);

      if (address.getTransportId() != null && !getId().equals(address.getTransportId()))
      {
         try
         {
            T addr = nodes.get(address);
            sendMessage(addr, Request.DELTA_STARTWORK_REJECTED, address);
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
   public void deltaScheduleWorkAccepted(Address address)
   {
      log.tracef("DELTA_SCHEDULEWORK_ACCEPTED(%s)", address);

      if (address.getTransportId() != null && !getId().equals(address.getTransportId()))
      {
         try
         {
            T addr = nodes.get(address);
            sendMessage(addr, Request.DELTA_SCHEDULEWORK_ACCEPTED, address);
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
   public void deltaScheduleWorkRejected(Address address)
   {
      log.tracef("DELTA_SCHEDULEWORK_REJECTED(%s)", address);

      if (address.getTransportId() != null && !getId().equals(address.getTransportId()))
      {
         try
         {
            T addr = nodes.get(address);
            sendMessage(addr, Request.DELTA_SCHEDULEWORK_REJECTED, address);
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
   public void deltaWorkSuccessful(Address address)
   {
      log.tracef("DELTA_WORK_SUCCESSFUL(%s)", address);

      if (address.getTransportId() != null && !getId().equals(address.getTransportId()))
      {
         try
         {
            T addr = nodes.get(address);
            sendMessage(addr, Request.DELTA_WORK_SUCCESSFUL, address);
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
   public void deltaWorkFailed(Address address)
   {
      log.tracef("DELTA_WORK_FAILED(%s)", address);

      if (address.getTransportId() != null && !getId().equals(address.getTransportId()))
      {
         try
         {
            T addr = nodes.get(address);
            sendMessage(addr, Request.DELTA_WORK_FAILED, address);
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
   public void doWork(Address address, DistributableWork work) throws WorkException
   {
      log.tracef("DO_WORK(%s, %s)", address, work);

      ClassBundle cb = ClassBundleFactory.createClassBundle(work);

      T addr = nodes.get(address);
      sendMessage(addr, Request.DO_WORK, address, cb, work);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void scheduleWork(Address address, DistributableWork work) throws WorkException
   {
      log.tracef("SCHEDULE_WORK(%s, %s)", address, work);

      ClassBundle cb = ClassBundleFactory.createClassBundle(work);

      T addr = nodes.get(address);
      sendMessage(addr, Request.SCHEDULE_WORK, address, cb, work);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public long startWork(Address address, DistributableWork work) throws WorkException
   {
      log.tracef("START_WORK(%s, %s)", address, work);

      ClassBundle cb = ClassBundleFactory.createClassBundle(work);

      T addr = nodes.get(address);
      return (long)sendMessage(addr, Request.START_WORK, address, cb, work);
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
    * {@inheritDoc}
    */
   public void register(Address address)
   {
      nodes.put(address, null);

      if (address.getTransportId() == null || address.getTransportId().equals(getId()))
      {
         Set<T> sent = new HashSet<T>();
         for (T addr : nodes.values())
         {
            if (addr != null && !sent.contains(addr))
            {
               sent.add(addr);
               try
               {
                  sendMessage(addr, Request.WORKMANAGER_ADD, address, (Serializable)getOwnAddress());
               }
               catch (Throwable t)
               {
                  log.error("Register " + t.getMessage(), t);
               }
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void unregister(Address address)
   {
      nodes.remove(address);

      if (address.getTransportId() == null || address.getTransportId().equals(getId()))
      {
         Set<T> sent = new HashSet<T>();
         for (T addr : nodes.values())
         {
            if (addr != null && !sent.contains(addr))
            {
               sent.add(addr);
               try
               {
                  sendMessage(addr, Request.WORKMANAGER_REMOVE, address);
               }
               catch (Throwable t)
               {
                  log.error("Unregister: " + t.getMessage(), t);
               }
            }
         }
      }
   }

   /**
    * Get the addresses
    * @param physicalAddress the physical address
    * @return The logical addresses associated
    */
   public Set<Address> getAddresses(T physicalAddress)
   {
      Set<Address> result = new HashSet<Address>();

      for (Map.Entry<Address, T> entry : nodes.entrySet())
      {
         if (entry.getValue() == null || entry.getValue().equals(physicalAddress))
         {
            result.add(entry.getKey());
         }
      }

      log.tracef("Addresses: %s", result);

      return Collections.unmodifiableSet(result);
   }

   /**
    * join
    * @param logicalAddress the logical address
    * @param physicalAddress the physical address
    */
   public void join(Address logicalAddress, T physicalAddress)
   {
      log.tracef("JOIN(%s, %s)", logicalAddress, physicalAddress);

      if (!nodes.containsKey(logicalAddress))
      {
         nodes.put(logicalAddress, physicalAddress);

         WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
         DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(logicalAddress);

         if (dwm != null)
         {
            Collection<NotificationListener> copy =
               new ArrayList<NotificationListener>(dwm.getNotificationListeners());
            for (NotificationListener nl : copy)
            {
               nl.join(logicalAddress);
            }
         }
         else
         {
            WorkManagerEventQueue wmeq = WorkManagerEventQueue.getInstance();
            wmeq.addEvent(new WorkManagerEvent(WorkManagerEvent.TYPE_JOIN, logicalAddress));
         }
      }
   }

   /**
    * leave
    * @param physicalAddress the physical address
    */
   public void leave(T physicalAddress)
   {
      log.tracef("LEAVE(%s)", physicalAddress);

      Set<Address> remove = new HashSet<Address>();

      for (Map.Entry<Address, T> entry : nodes.entrySet())
      {
         if (entry.getValue() != null && entry.getValue().equals(physicalAddress))
         {
            remove.add(entry.getKey());
         }
      }

      if (remove.size() > 0)
      {
         WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();

         for (Address logicalAddress : remove)
         {
            nodes.remove(logicalAddress);

            DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(logicalAddress);

            if (dwm != null)
            {
               Collection<NotificationListener> copy =
                  new ArrayList<NotificationListener>(dwm.getNotificationListeners());
               for (NotificationListener nl : copy)
               {
                  nl.leave(logicalAddress);
               }
            }
            else
            {
               WorkManagerEventQueue wmeq = WorkManagerEventQueue.getInstance();
               wmeq.addEvent(new WorkManagerEvent(WorkManagerEvent.TYPE_LEAVE, logicalAddress));
            }
         }
      }
   }

   /**
    * localPing
    * @return the ping value
    */
   public long localPing()
   {
      log.tracef("LOCAL_PING()");

      return 0L;
   }

   /**
    * localWorkManagerAdd
    *
    * @param address the logical address
    * @param physicalAddress the physical address
    */
   public void localWorkManagerAdd(Address address, T physicalAddress)
   {
      log.tracef("LOCAL_WORKMANAGER_ADD(%s, %s)", address, physicalAddress);

      join(address, physicalAddress);
   }

   /**
    * localWorkManagerRemove
    *
    * @param address the logical address
    */
   public void localWorkManagerRemove(Address address)
   {
      log.tracef("LOCAL_WORKMANAGER_REMOVE(%s)", address);

      nodes.remove(address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.leave(address);
         }
      }
      else
      {
         WorkManagerEventQueue wmeq = WorkManagerEventQueue.getInstance();
         wmeq.addEvent(new WorkManagerEvent(WorkManagerEvent.TYPE_LEAVE, address));
      }
   }

   /**
    * localDoWork
    *
    * @param address the logical address
    * @param work the work
    * @throws WorkException in case of error
    */
   public void localDoWork(Address address, DistributableWork work) throws WorkException
   {
      log.tracef("LOCAL_DO_WORK(%s, %s)", address, work);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      dwm.localDoWork(work);
   }

   /**
    * localStartWork
    *
    * @param address the logical address
    * @param work the work
    * @return the start value
    * @throws WorkException in case of error
    */
   public long localStartWork(Address address, DistributableWork work) throws WorkException
   {
      log.tracef("LOCAL_START_WORK(%s, %s)", address, work);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      return dwm.localStartWork(work);
   }

   /**
    * localScheduleWork
    *
    * @param address the logical address
    * @param work the work
    * @throws WorkException in case of error
    */
   public void localScheduleWork(Address address, DistributableWork work) throws WorkException
   {
      log.tracef("LOCAL_SCHEDULE_WORK(%s, %s)", address, work);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      dwm.localScheduleWork(work);
   }

   /**
    * localGetShortRunningFree
    *
    * @param address the logical address
    * @return the free count
    */
   public long localGetShortRunningFree(Address address)
   {
      log.tracef("LOCAL_GET_SHORTRUNNING_FREE(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      WorkManager wm = wmc.resolveWorkManager(address);

      if (wm != null)
      {
         StatisticsExecutor executor = wm.getShortRunningThreadPool();
         if (executor != null)
            return executor.getNumberOfFreeThreads();
      }

      return 0L;
   }

   /**
    * localGetLongRunningFree
    *
    * @param address the logical address
    * @return the free count
    */
   public long localGetLongRunningFree(Address address)
   {
      log.tracef("LOCAL_GET_LONGRUNNING_FREE(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      WorkManager wm = wmc.resolveWorkManager(address);

      if (wm != null)
      {
         StatisticsExecutor executor = wm.getLongRunningThreadPool();
         if (executor != null)
            return executor.getNumberOfFreeThreads();
      }

      return 0L;
   }

   /**
    * localUpdateShortRunningFree
    *
    * @param logicalAddress the logical address
    * @param freeCount the free count
    */
   public void localUpdateShortRunningFree(Address logicalAddress, Long freeCount)
   {
      log.tracef("LOCAL_UPDATE_SHORTRUNNING_FREE(%s, %d)", logicalAddress, freeCount);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(logicalAddress);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.updateShortRunningFree(logicalAddress, freeCount);
         }
      }
      else
      {
         WorkManagerEventQueue wmeq = WorkManagerEventQueue.getInstance();
         wmeq.addEvent(new WorkManagerEvent(WorkManagerEvent.TYPE_UPDATE_SHORT_RUNNING, logicalAddress, freeCount));
      }
   }

   /**
    * localUpdateLongRunningFree
    *
    * @param logicalAddress the logical address
    * @param freeCount the free count
    */
   public void localUpdateLongRunningFree(Address logicalAddress, Long freeCount)
   {
      log.tracef("LOCAL_UPDATE_LONGRUNNING_FREE(%s, %d)", logicalAddress, freeCount);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(logicalAddress);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.updateLongRunningFree(logicalAddress, freeCount);
         }
      }
      else
      {
         WorkManagerEventQueue wmeq = WorkManagerEventQueue.getInstance();
         wmeq.addEvent(new WorkManagerEvent(WorkManagerEvent.TYPE_UPDATE_LONG_RUNNING, logicalAddress, freeCount));
      }
   }

   /**
    * localGetDistributedStatistics
    *
    * @param address the logical address
    * @return The value
    */
   public DistributedWorkManagerStatisticsValues localGetDistributedStatistics(Address address)
   {
      log.tracef("LOCAL_GET_DISTRIBUTED_STATISTICS(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      if (dwm != null)
      {
         DistributedWorkManagerStatisticsValues values =
            new DistributedWorkManagerStatisticsValues(dwm.getDistributedStatistics().getWorkSuccessful(),
                                                       dwm.getDistributedStatistics().getWorkFailed(),
                                                       dwm.getDistributedStatistics().getDoWorkAccepted(),
                                                       dwm.getDistributedStatistics().getDoWorkRejected(),
                                                       dwm.getDistributedStatistics().getScheduleWorkAccepted(),
                                                       dwm.getDistributedStatistics().getScheduleWorkRejected(),
                                                       dwm.getDistributedStatistics().getStartWorkAccepted(),
                                                       dwm.getDistributedStatistics().getStartWorkRejected());

         return values;
      }

      return null;
   }

   /**
    * localClearDistributedStatistics
    *
    * @param logicalAddress the logical address
    */
   public void localClearDistributedStatistics(Address logicalAddress)
   {
      log.tracef("LOCAL_CLEAR_DISTRIBUTED_STATISTICS(%s)", logicalAddress);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(logicalAddress);

      if (dwm != null)
      {
         if (dwm.isDistributedStatisticsEnabled())
         {
            DistributedWorkManagerStatisticsValues v =
               new DistributedWorkManagerStatisticsValues(0, 0, 0, 0, 0, 0, 0, 0);
            dwm.getDistributedStatistics().initialize(v);
         }
      }
   }

   /**
    * Local delta doWork accepted
    * @param address the logical address
    */
   public void localDeltaDoWorkAccepted(Address address)
   {
      log.tracef("LOCAL_DELTA_DOWORK_ACCEPTED(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.deltaDoWorkAccepted();
         }
      }
   }

   /**
    * Local delta doWork rejected
    * @param address the logical address
    */
   public void localDeltaDoWorkRejected(Address address)
   {
      log.tracef("LOCAL_DELTA_DOWORK_REJECTED(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.deltaDoWorkRejected();
         }
      }
   }

   /**
    * Local delta startWork accepted
    * @param address the logical address
    */
   public void localDeltaStartWorkAccepted(Address address)
   {
      log.tracef("LOCAL_DELTA_STARTWORK_ACCEPTED(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.deltaStartWorkAccepted();
         }
      }
   }

   /**
    * Local delta startWork rejected
    * @param address the logical address
    */
   public void localDeltaStartWorkRejected(Address address)
   {
      log.tracef("LOCAL_DELTA_STARTWORK_REJECTED(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.deltaStartWorkRejected();
         }
      }
   }

   /**
    * Local delta scheduleWork accepted
    * @param address the logical address
    */
   public void localDeltaScheduleWorkAccepted(Address address)
   {
      log.tracef("LOCAL_DELTA_SCHEDULEWORK_ACCEPTED(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.deltaScheduleWorkAccepted();
         }
      }
   }

   /**
    * Local delta scheduleWork rejected
    * @param address the logical address
    */
   public void localDeltaScheduleWorkRejected(Address address)
   {
      log.tracef("LOCAL_DELTA_SCHEDULEWORK_REJECTED(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.deltaScheduleWorkRejected();
         }
      }
   }

   /**
    * Local delta work successful
    * @param address the logical address
    */
   public void localDeltaWorkSuccessful(Address address)
   {
      log.tracef("LOCAL_DELTA_WORK_SUCCESSFUL(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.deltaWorkSuccessful();
         }
      }
   }

   /**
    * Local delta work failed
    * @param address the logical address
    */
   public void localDeltaWorkFailed(Address address)
   {
      log.tracef("LOCAL_DELTA_WORK_FAILED(%s)", address);

      WorkManagerCoordinator wmc = WorkManagerCoordinator.getInstance();
      DistributedWorkManager dwm = wmc.resolveDistributedWorkManager(address);

      if (dwm != null)
      {
         Collection<NotificationListener> copy =
            new ArrayList<NotificationListener>(dwm.getNotificationListeners());
         for (NotificationListener nl : copy)
         {
            nl.deltaWorkFailed();
         }
      }
   }

   /**
    * Get the own address
    * @return The value
    */
   protected abstract T getOwnAddress();

   /**
    * send a messagge using specific protocol. Method overridden in specific protocol implementation classes
    *
    * @param physicalAddress the physical address
    * @param request the request
    * @param parameters the parameters
    * @return the returned value. Can be null if requested operation return a void
    * @throws WorkException in case of problem with the work
    */
   protected abstract Serializable sendMessage(T physicalAddress, Request request, Serializable... parameters)
      throws WorkException;
}
