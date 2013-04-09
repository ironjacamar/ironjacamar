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

package org.jboss.jca.core.api.workmanager;

import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;
import org.jboss.jca.core.spi.workmanager.policy.Policy;
import org.jboss.jca.core.spi.workmanager.selector.Selector;
import org.jboss.jca.core.spi.workmanager.transport.Transport;

import java.util.Collection;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

/**
 * The JBoss specific distributed work manager interface
 */
public interface DistributedWorkManager extends javax.resource.spi.work.DistributableWorkManager, WorkManager
{
   /**
    * Get the policy
    * @return The value
    */
   public Policy getPolicy();

   /**
    * Set the policy
    * @param v The value
    */
   public void setPolicy(Policy v);

   /**
    * Get the selector
    * @return The value
    */
   public Selector getSelector();

   /**
    * Set the selector
    * @param v The value
    */
   public void setSelector(Selector v);

   /**
    * Get the transport
    * @return The value
    */
   public Transport getTransport();

   /**
    * Set the transport
    * @param v The value
    */
   public void setTransport(Transport v);

   /**
    * Is distributed statistics enabled
    * @return True if enabled; otherwise false
    */
   public boolean isDistributedStatisticsEnabled();

   /**
    * Set the distributed statistics enabled flag
    * @param v The value
    */
   public void setDistributedStatisticsEnabled(boolean v);

   /**
    * Get the notification listeners attached
    * @return The value
    */
   public Collection<NotificationListener> getNotificationListeners();

   /**
    * Toggle distribution of Work instances for doWork
    * @param v The value
    */
   public void setDoWorkDistributionEnabled(boolean v);

   /**
    * Is distribution of Work instances for doWork enabled
    * @return True if enabled, otherwise false
    */
   public boolean isDoWorkDistributionEnabled();

   /**
    * Toggle distribution of Work instances for startWork
    * @param v The value
    */
   public void setStartWorkDistributionEnabled(boolean v);

   /**
    * Is distribution of Work instances for startWork enabled
    * @return True if enabled, otherwise false
    */
   public boolean isStartWorkDistributionEnabled();

   /**
    * Toggle distribution of Work instances for scheduleWork
    * @param v The value
    */
   public void setScheduleWorkDistributionEnabled(boolean v);

   /**
    * Is distribution of Work instances for scheduleWork enabled
    * @return True if enabled, otherwise false
    */
   public boolean isScheduleWorkDistributionEnabled();

   /**
    * doWork locally
    * @param work The work
    * @exception WorkException Thrown if an error occurs
    */
   public void localDoWork(Work work) throws WorkException;

   /**
    * scheduleWork locally
    * @param work The work
    * @exception WorkException Thrown if an error occurs
    */
   public void localScheduleWork(Work work) throws WorkException;

   /**
    * startWork locally
    * @param work The work
    * @return The delay
    * @exception WorkException Thrown if an error occurs
    */
   public long localStartWork(Work work) throws WorkException;

   /**
    * Get the distributed statistics
    * @return The value
    */
   public DistributedWorkManagerStatistics getDistributedStatistics();

   /**
    * Initialize
    */
   public void initialize();
}
