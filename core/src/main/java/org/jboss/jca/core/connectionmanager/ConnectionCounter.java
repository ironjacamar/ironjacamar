/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.connectionmanager;

/**
 * Counter for connection related operations.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev$ $Date$
 *
 */
public class ConnectionCounter
{
   /**Number of created connection*/
   private volatile int created = 0;

   /**Number of destroyed connection*/
   private volatile int destroyed = 0;

   /**Total wait time to get Connection from Pool.*/
   private volatile long totalBlockTime;
   
   /**Idle timed out Connection Count.*/
   private volatile int timedOut;

   /**The maximum wait time */      
   private volatile long maxWaitTime;
   
   /**
    * Returns current connection count
    * @return current connection count
    */
   public int getGuaranteedCount()
   {
      return created - destroyed;
   }

   /**
    * Returns current connection count
    * @return current connection count
    */   
   public int getCount()
   {
      return created - destroyed;
   }

   /**
    * Returns number of created connection.
    * @return connection created
    */
   public int getCreatedCount()
   {
      return created;
   }

   /**
    * Returns number of destroyed connection.
    * @return destroyed connection
    */
   public int getDestroyedCount()
   {
      return destroyed;
   }

   /**
    * Increment created.
    */
   public void inc()
   {
      ++created;
   }

   /**
    * Increment destroyed.
    */
   public void dec()
   {
      ++destroyed;
   }

   /**
    * Update block time.
    * @param latest latest block
    */
   public void updateBlockTime(long latest)
   {
      totalBlockTime += latest;
      if (maxWaitTime < latest)
      {
         maxWaitTime = latest;  
      }
   }

   /**
    * Returns total block time.
    * @return total block time
    */
   public long getTotalBlockTime()
   {
      return totalBlockTime;
   }

   /**
    * Gets timed out connections.
    * @return timeout
    */
   public int getTimedOut()
   {
      return timedOut;
   }
   
   /**
    * Increment timeout connection.
    */
   public void incTimedOut()
   {
      ++timedOut;
   }

   /**
    * Returns max wait time.
    * @return max wait time
    */
   public long getMaxWaitTime()
   {
      return maxWaitTime;
   }

}
