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

package org.jboss.jca.core.spi.workmanager.transport;

import org.jboss.jca.core.api.workmanager.DistributedWorkManagerStatisticsValues;
import org.jboss.jca.core.spi.workmanager.Address;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

/**
 * The transport interface defines the methods for the physical transport
 * of the work instances for a distributed work manager
 */
public interface Transport
{
   /**
    * Get the identifier of the transport
    * @return The value
    */
   public String getId();

   /**
    * Startup the transport
    * @exception Throwable In case of an error
    */
   public void startup() throws Throwable;

   /**
    * Shutdown the transport
    * @exception Throwable In case of an error
    */
   public void shutdown() throws Throwable;

   /**
    * Initialize the transport
    * @exception Throwable In case of an error
    */
   public void initialize() throws Throwable;

   /**
    * Is the transport initialized
    * @return The value
    */
   public boolean isInitialized();

   /**
    * Register
    * @param address The address
    */
   public void register(Address address);

   /**
    * Unregister
    * @param address The address
    */
   public void unregister(Address address);

   /**
    * Ping time to a distributed work manager
    * @param address The address
    * @return The ping time in milliseconds
    */
   public long ping(Address address);

   /**
    * Get The number of free thread in short running pool from a distributed work manager
    * @param address The address
    * @return The number of free thread in short running pool
    */
   public long getShortRunningFree(Address address);

   /**
    * Get The number of free thread in long running pool from a distributed work manager
    * @param address The address
    * @return The number of free thread in long running pool
    */
   public long getLongRunningFree(Address address);

   /**
    * Update The number of free thread in short running pool from a distributed work manager
    *
    * @param address The address
    * @param freeCount the number of freeThread
    */
   public void updateShortRunningFree(Address address, long freeCount);

   /**
    * Update The number of free thread in long running pool from a distributed work manager
    *
    * @param address The address
    * @param freeCount the number of freeThread
    */
   public void updateLongRunningFree(Address address, long freeCount);

   /**
    * Get the distributed statistics
    * @param address The address
    * @return The value
    */
   public DistributedWorkManagerStatisticsValues getDistributedStatistics(Address address);

   /**
    * Clear distributed statistics
    * @param address The address
    */
   public void clearDistributedStatistics(Address address);

   /**
    * Delta doWork accepted
    * @param address The address
    */
   public void deltaDoWorkAccepted(Address address);

   /**
    * Delta doWork rejected
    * @param address The address
    */
   public void deltaDoWorkRejected(Address address);

   /**
    * Delta startWork accepted
    * @param address The address
    */
   public void deltaStartWorkAccepted(Address address);

   /**
    * Delta startWork rejected
    * @param address The address
    */
   public void deltaStartWorkRejected(Address address);

   /**
    * Delta scheduleWork accepted
    * @param address The address
    */
   public void deltaScheduleWorkAccepted(Address address);

   /**
    * Delta scheduleWork rejected
    * @param address The address
    */
   public void deltaScheduleWorkRejected(Address address);

   /**
    * Delta work successful
    * @param address The address
    */
   public void deltaWorkSuccessful(Address address);

   /**
    * Delta work failed
    * @param address The address
    */
   public void deltaWorkFailed(Address address);

   /**
    * doWork
    * @param address The address
    * @param work The work
    * @exception WorkException Thrown if an error occurs
    */
   public void doWork(Address address, DistributableWork work) throws WorkException;

   /**
    * scheduleWork
    * @param address The address
    * @param work The work
    * @exception WorkException Thrown if an error occurs
    */
   public void scheduleWork(Address address, DistributableWork work) throws WorkException;

   /**
    * startWork
    * @param address The address
    * @param work The work
    * @return The delay
    * @exception WorkException Thrown if an error occurs
    */
   public long startWork(Address address, DistributableWork work) throws WorkException;
}
