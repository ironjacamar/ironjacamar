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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The JBoss distributed work manager statistics values
 */
public class DistributedWorkManagerStatisticsValues implements Serializable
{
   /** Serial version UID */
   private static final long serialVersionUID = 1L;

   /** Successful */
   private int successful;

   /** Failed */
   private int failed;

   /** DoWork: Accepted */
   private int doWorkAccepted;

   /** DoWork: Rejected */
   private int doWorkRejected;

   /** ScheduleWork: Accepted */
   private int scheduleWorkAccepted;

   /** ScheduleWork: Rejected */
   private int scheduleWorkRejected;

   /** StartWork: Accepted */
   private int startWorkAccepted;

   /** StartWork: Rejected */
   private int startWorkRejected;



   private static final String SUCCESSFUL = "SUCCESSFUL";
   private static final String FAILED = "FAILED";
   private static final String DO_WORK_ACCEPTED = "DO_WORK_ACCEPTED";
   private static final String DO_WORK_REJECTED = "DO_WORK_REJECTED";
   private static final String SCHEDULE_WORK_ACCEPTED = "SCHEDULE_WORK_ACCEPTED";
   private static final String SCHEDULE_WORK_REJECTED = "SCHEDULE_WORK_REJECTED";
   private static final String START_WORK_ACCEPTED = "START_WORK_ACCEPTED";
   private static final String START_WORK_REJECTED = "START_WORK_REJECTED";

   /**
    * create an instance from a Map
    * @param map the map
    * @return the instance
    */
   public static DistributedWorkManagerStatisticsValues fromMap(Map<String, Integer> map)
   {
      return new DistributedWorkManagerStatisticsValues(map.get(SUCCESSFUL), map.get(FAILED), map.get(DO_WORK_ACCEPTED),
            map.get(DO_WORK_REJECTED), map.get(SCHEDULE_WORK_ACCEPTED), map.get(SCHEDULE_WORK_REJECTED),
            map.get(START_WORK_ACCEPTED), map.get(START_WORK_REJECTED));

   }

   /**
    * return a map representing the instance
    * @return the map
    */
   public Map<String, Integer> toMap()
   {
      Map<String, Integer> returnMap = new LinkedHashMap<String, Integer>(8);
      returnMap.put(SUCCESSFUL, this.getWorkSuccessful());
      returnMap.put(FAILED, this.getWorkFailed());
      returnMap.put(DO_WORK_ACCEPTED, this.getDoWorkAccepted());
      returnMap.put(DO_WORK_REJECTED, this.getDoWorkRejected());
      returnMap.put(SCHEDULE_WORK_ACCEPTED, this.getScheduleWorkAccepted());
      returnMap.put(SCHEDULE_WORK_ACCEPTED, this.getScheduleWorkRejected());
      returnMap.put(START_WORK_ACCEPTED, this.getStartWorkAccepted());
      returnMap.put(START_WORK_REJECTED, this.getStartWorkRejected());
      return returnMap;
   }



   /**
    * Constructor
    * @param successful successful
    * @param failed failed
    * @param doWorkAccepted doWorkAccepted
    * @param doWorkRejected doWorkRejected
    * @param scheduleWorkAccepted scheduleWorkAccepted
    * @param scheduleWorkRejected scheduleWorkRejected
    * @param startWorkAccepted startWorkAccepted
    * @param startWorkRejected startWorkRejected
    */
   public DistributedWorkManagerStatisticsValues(int successful,
                                                 int failed,
                                                 int doWorkAccepted,
                                                 int doWorkRejected,
                                                 int scheduleWorkAccepted,
                                                 int scheduleWorkRejected,
                                                 int startWorkAccepted,
                                                 int startWorkRejected)
   {
      this.successful = successful;
      this.failed = failed;
      this.doWorkAccepted = doWorkAccepted;
      this.doWorkRejected = doWorkRejected;
      this.scheduleWorkAccepted = scheduleWorkAccepted;
      this.scheduleWorkRejected = scheduleWorkRejected;
      this.startWorkAccepted = startWorkAccepted;
      this.startWorkRejected = startWorkRejected;
   }

   /**
    * {@inheritDoc}
    */
   public int getWorkSuccessful()
   {
      return successful;
   }

   /**
    * {@inheritDoc}
    */
   public int getWorkFailed()
   {
      return failed;
   }

   /**
    * {@inheritDoc}
    */
   public int getDoWorkAccepted()
   {
      return doWorkAccepted;
   }

   /**
    * {@inheritDoc}
    */
   public int getDoWorkRejected()
   {
      return doWorkRejected;
   }

   /**
    * {@inheritDoc}
    */
   public int getScheduleWorkAccepted()
   {
      return scheduleWorkAccepted;
   }

   /**
    * {@inheritDoc}
    */
   public int getScheduleWorkRejected()
   {
      return scheduleWorkRejected;
   }

   /**
    * {@inheritDoc}
    */
   public int getStartWorkAccepted()
   {
      return startWorkAccepted;
   }

   /**
    * {@inheritDoc}
    */
   public int getStartWorkRejected()
   {
      return startWorkRejected;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("DistributedWorkManagerStatisticsValues@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[successful=").append(getWorkSuccessful());
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
