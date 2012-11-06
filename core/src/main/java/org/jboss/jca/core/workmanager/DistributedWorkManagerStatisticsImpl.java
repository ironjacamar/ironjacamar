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

package org.jboss.jca.core.workmanager;

import org.jboss.jca.core.api.workmanager.DistributedWorkManagerStatistics;
import org.jboss.jca.core.api.workmanager.DistributedWorkManagerStatisticsValues;
import org.jboss.jca.core.spi.workmanager.Address;
import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;
import org.jboss.jca.core.spi.workmanager.transport.Transport;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The JBoss distributed work manager statistics implementation
 */
public class DistributedWorkManagerStatisticsImpl implements DistributedWorkManagerStatistics,
                                                             NotificationListener
{
   /** Own identifier */
   private Address own;

   /** Transport */
   private Transport transport;

   /** Work managers */
   private Set<Address> workManagers;

   /** Successful */
   private AtomicInteger successful;

   /** Failed */
   private AtomicInteger failed;

   /** DoWork: Accepted */
   private AtomicInteger doWorkAccepted;

   /** DoWork: Rejected */
   private AtomicInteger doWorkRejected;

   /** ScheduleWork: Accepted */
   private AtomicInteger scheduleWorkAccepted;

   /** ScheduleWork: Rejected */
   private AtomicInteger scheduleWorkRejected;

   /** StartWork: Accepted */
   private AtomicInteger startWorkAccepted;

   /** StartWork: Rejected */
   private AtomicInteger startWorkRejected;

   /** Initialized */
   private boolean initialized;

   /**
    * Constructor
    * @param ownId The local distributed work managers identifier
    * @param t The transport
    */
   public DistributedWorkManagerStatisticsImpl(Address ownId, Transport t)
   {
      this.own = ownId;
      this.transport = t;
      this.workManagers = Collections.synchronizedSet(new HashSet<Address>());
      this.successful = new AtomicInteger(0);
      this.failed = new AtomicInteger(0);
      this.doWorkAccepted = new AtomicInteger(0);
      this.doWorkRejected = new AtomicInteger(0);
      this.scheduleWorkAccepted = new AtomicInteger(0);
      this.scheduleWorkRejected = new AtomicInteger(0);
      this.startWorkAccepted = new AtomicInteger(0);
      this.startWorkRejected = new AtomicInteger(0);
      this.initialized = false;
   }

   /**
    * {@inheritDoc}
    */
   public void initialize(DistributedWorkManagerStatisticsValues values)
   {
      successful.set(values.getWorkSuccessful());
      failed.set(values.getWorkFailed());
      doWorkAccepted.set(values.getDoWorkAccepted());
      doWorkRejected.set(values.getDoWorkRejected());
      scheduleWorkAccepted.set(values.getScheduleWorkAccepted());
      scheduleWorkRejected.set(values.getScheduleWorkRejected());
      startWorkAccepted.set(values.getStartWorkAccepted());
      startWorkRejected.set(values.getStartWorkRejected());
      initialized = true;
   }

   /**
    * {@inheritDoc}
    */
   public void join(Address address)
   {
      workManagers.add(address);

      if (!initialized)
      {
         if (!own.equals(address))
         {
            DistributedWorkManagerStatisticsValues values = transport.getDistributedStatistics(address);
            if (values != null)
               initialize(values);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void leave(Address address)
   {
      workManagers.remove(address);
   }

   /**
    * {@inheritDoc}
    */
   public void updateShortRunningFree(Address address, long free)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void updateLongRunningFree(Address address, long free)
   {
   }

   /**
    * {@inheritDoc}
    */
   public int getWorkActive()
   {
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   public int getWorkSuccessful()
   {
      return successful.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaWorkSuccessful()
   {
      successful.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getWorkFailed()
   {
      return failed.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaWorkFailed()
   {
      failed.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getDoWorkAccepted()
   {
      return doWorkAccepted.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaDoWorkAccepted()
   {
      doWorkAccepted.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getDoWorkRejected()
   {
      return doWorkRejected.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaDoWorkRejected()
   {
      doWorkRejected.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getScheduleWorkAccepted()
   {
      return scheduleWorkAccepted.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaScheduleWorkAccepted()
   {
      scheduleWorkAccepted.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getScheduleWorkRejected()
   {
      return scheduleWorkRejected.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaScheduleWorkRejected()
   {
      scheduleWorkRejected.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getStartWorkAccepted()
   {
      return startWorkAccepted.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaStartWorkAccepted()
   {
      startWorkAccepted.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public int getStartWorkRejected()
   {
      return startWorkRejected.get();
   }

   /**
    * {@inheritDoc}
    */
   public void deltaStartWorkRejected()
   {
      startWorkRejected.incrementAndGet();
   }

   /**
    * Send: doWork accepted
    */
   void sendDeltaDoWorkAccepted()
   {
      doWorkAccepted.incrementAndGet();

      for (Address address : workManagers)
      {
         if (!own.equals(address))
            transport.deltaDoWorkAccepted(address);
      }
   }

   /**
    * Send: doWork rejected
    */
   void sendDeltaDoWorkRejected()
   {
      doWorkRejected.incrementAndGet();

      for (Address address : workManagers)
      {
         if (!own.equals(address))
            transport.deltaDoWorkRejected(address);
      }
   }

   /**
    * Send: scheduleWork accepted
    */
   void sendDeltaScheduleWorkAccepted()
   {
      scheduleWorkAccepted.incrementAndGet();

      for (Address address : workManagers)
      {
         if (!own.equals(address))
            transport.deltaScheduleWorkAccepted(address);
      }
   }

   /**
    * Send: scheduleWork rejected
    */
   void sendDeltaScheduleWorkRejected()
   {
      scheduleWorkRejected.incrementAndGet();

      for (Address address : workManagers)
      {
         if (!own.equals(address))
            transport.deltaScheduleWorkRejected(address);
      }
   }

   /**
    * Send: startWork accepted
    */
   void sendDeltaStartWorkAccepted()
   {
      startWorkAccepted.incrementAndGet();

      for (Address address : workManagers)
      {
         if (!own.equals(address))
            transport.deltaStartWorkAccepted(address);
      }
   }

   /**
    * Send: startWork rejected
    */
   void sendDeltaStartWorkRejected()
   {
      startWorkRejected.incrementAndGet();

      for (Address address : workManagers)
      {
         if (!own.equals(address))
            transport.deltaStartWorkRejected(address);
      }
   }

   /**
    * Send: work successful
    */
   void sendDeltaWorkSuccessful()
   {
      successful.incrementAndGet();

      for (Address address : workManagers)
      {
         if (!own.equals(address))
            transport.deltaWorkSuccessful(address);
      }
   }

   /**
    * Send: work failed
    */
   void sendDeltaWorkFailed()
   {
      failed.incrementAndGet();

      for (Address address : workManagers)
      {
         if (!own.equals(address))
            transport.deltaWorkFailed(address);
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

      sb.append("DistributedWorkManagerStatisticsImpl@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[transport=").append(transport);
      sb.append(" successful=").append(getWorkSuccessful());
      sb.append(" failed=").append(getWorkFailed());
      sb.append(" doWorkAccepted=").append(getDoWorkAccepted());
      sb.append(" doWorkRejected=").append(getDoWorkRejected());
      sb.append(" scheduleWorkAccepted=").append(getScheduleWorkAccepted());
      sb.append(" scheduleWorkRejected=").append(getScheduleWorkRejected());
      sb.append(" startWorkAccepted=").append(getStartWorkAccepted());
      sb.append(" startWorkRejected=").append(getStartWorkRejected());
      sb.append("]");

      return sb.toString();
   }
}
