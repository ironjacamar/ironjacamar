/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.jboss.jca.as.tracer;

import org.jboss.jca.core.tracer.TraceEvent;

import java.util.List;

/**
 * An interaction
 */
public class Interaction
{
   /** Thread */
   private long thread;
   
   /** Start time */
   private long startTime;

   /** End time */
   private long endTime;

   /** Events */
   private List<TraceEvent> events;

   /** Status */
   private TraceEventStatus status;

   /** Cached transaction */
   private String transaction;

   /**
    * Constructor
    * @param thread The thread id
    * @param startTime The start time
    * @param endTime The end time
    * @param events The events
    * @param status The status
    */
   public Interaction(long thread, long startTime, long endTime,
                      List<TraceEvent> events, TraceEventStatus status)
   {
      this.thread = thread;
      this.startTime = startTime;
      this.endTime = endTime;
      this.events = events;
      this.status = status;
      this.transaction = null;
   }

   /**
    * Get thread id
    * @return The value
    */
   public long getThread()
   {
      return thread;
   }

   /**
    * Get start time
    * @return The value
    */
   public long getStartTime()
   {
      return startTime;
   }

   /**
    * Get end time
    * @return The value
    */
   public long getEndTime()
   {
      return endTime;
   }

   /**
    * Get events
    * @return The value
    */
   public List<TraceEvent> getEvents()
   {
      return events;
   }

   /**
    * Get status
    * @return The value
    */
   public TraceEventStatus getStatus()
   {
      return status;
   }

   /**
    * Get pool
    * @return The value
    */
   public String getPool()
   {
      return events.get(0).getPool();
   }

   /**
    * Get managed connection pool
    * @return The value
    */
   public String getManagedConnectionPool()
   {
      return events.get(0).getManagedConnectionPool();
   }

   /**
    * Get connection listener
    * @return The value
    */
   public String getConnectionListener()
   {
      return events.get(0).getConnectionListener();
   }

   /**
    * Get transaction
    * @return The value
    */
   public String getTransaction()
   {
      if (transaction == null)
      {
         TraceEvent transactionEvent =
            TraceEventHelper.getType(events,
                                     TraceEvent.ENLIST_CONNECTION_LISTENER,
                                     TraceEvent.ENLIST_CONNECTION_LISTENER_FAILED,
                                     TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER,
                                     TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER_FAILED);
      
         if (transactionEvent != null)
         {
            transaction = transactionEvent.getPayload1();
         }
         else
         {
            transaction = "";
         }
      }

      if (!"".equals(transaction))
         return transaction;

      return null;
   }
}
