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

package org.jboss.jca.core.spi.workmanager.notification;

import org.jboss.jca.core.spi.workmanager.Address;

/**
 * The notification listener gets notified about changes
 */
public interface NotificationListener
{
   /**
    * A distributed work manager joined
    * @param address The address
    */
   public void join(Address address);

   /**
    * A distributed work manager left
    * @param address The address
    */
   public void leave(Address address);

   /**
    * Update the short thread pool information for a distributed work manager
    * @param address The address
    * @param free The number of free threads
    */
   public void updateShortRunningFree(Address address, long free);

   /**
    * Update the long thread pool information for a distributed work manager
    * @param address The address
    * @param free The number of free threads
    */
   public void updateLongRunningFree(Address address, long free);

   /**
    * Delta doWork accepted
    */
   public void deltaDoWorkAccepted();

   /**
    * Delta doWork rejected
    */
   public void deltaDoWorkRejected();

   /**
    * Delta startWork accepted
    */
   public void deltaStartWorkAccepted();

   /**
    * Delta startWork rejected
    */
   public void deltaStartWorkRejected();

   /**
    * Delta scheduleWork accepted
    */
   public void deltaScheduleWorkAccepted();

   /**
    * Delta scheduleWork rejected
    */
   public void deltaScheduleWorkRejected();

   /**
    * Delta work successful
    */
   public void deltaWorkSuccessful();

   /**
    * Delta work failed
    */
   public void deltaWorkFailed();
}
