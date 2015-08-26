/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */


package org.ironjacamar.core.api.workmanager;

import java.io.Serializable;

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
