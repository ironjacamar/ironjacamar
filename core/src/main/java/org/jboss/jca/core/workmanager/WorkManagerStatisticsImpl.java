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

package org.jboss.jca.core.workmanager;

import org.jboss.jca.core.api.workmanager.WorkManagerStatistics;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The JBoss work manager statistics implementation
 */
public class WorkManagerStatisticsImpl implements WorkManagerStatistics
{
   /** Active */
   private AtomicInteger active;

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

   /**
    * Constructor
    */
   public WorkManagerStatisticsImpl()
   {
      active = new AtomicInteger(0);
      successful = new AtomicInteger(0);
      failed = new AtomicInteger(0);
      doWorkAccepted = new AtomicInteger(0);
      doWorkRejected = new AtomicInteger(0);
      scheduleWorkAccepted = new AtomicInteger(0);
      scheduleWorkRejected = new AtomicInteger(0);
      startWorkAccepted = new AtomicInteger(0);
      startWorkRejected = new AtomicInteger(0);
   }

   /**
    * {@inheritDoc}
    */
   public int getWorkActive()
   {
      return active.get();
   }

   /**
    * Set the number of active work instances
    * @param v The value
    */
   void setWorkActive(int v)
   {
      active.set(v);
   }

   /**
    * {@inheritDoc}
    */
   public int getWorkSuccessful()
   {
      return successful.get();
   }

   /**
    * Delta work successful
    */
   void deltaWorkSuccessful()
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
    * Delta work failed
    */
   void deltaWorkFailed()
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
    * Delta doWork accepted 
    */
   void deltaDoWorkAccepted()
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
    * Delta doWork rejected
    */
   void deltaDoWorkRejected()
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
    * Delta scheduleWork accepted 
    */
   void deltaScheduleWorkAccepted()
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
    * Delta scheduleWork rejected
    */
   void deltaScheduleWorkRejected()
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
    * Delta startWork accepted 
    */
   void deltaStartWorkAccepted()
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
    * Delta startWork rejected
    */
   void deltaStartWorkRejected()
   {
      startWorkRejected.incrementAndGet();
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void clear()
   {
      active.set(0);
      successful.set(0);
      failed.set(0);
      doWorkAccepted.set(0);
      doWorkRejected.set(0);
      scheduleWorkAccepted.set(0);
      scheduleWorkRejected.set(0);
      startWorkAccepted.set(0);
      startWorkRejected.set(0);
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("WorkManagerStatistics@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[active=").append(getWorkActive());
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
