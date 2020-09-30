/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.api.connectionmanager.pool;

import org.jboss.jca.core.spi.statistics.StatisticsPlugin;

/**
 * The pool statistics
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface PoolStatistics extends StatisticsPlugin
{
   /**
    * Get active count
    * @return The value
    */
   public int getActiveCount();

   /**
    * Get the available count
    * @return The value
    */
   public int getAvailableCount();

   /**
    * Get the average time spent waiting on a connection (milliseconds)
    * @return The value
    */
   public long getAverageBlockingTime();

   /**
    * Get the average time spent creating a connection (milliseconds)
    * @return The value
    */
   public long getAverageCreationTime();

   /**
    * Get the average time spent obtaining a connection (milliseconds)
    * @return The value
    */
   public long getAverageGetTime();

   /**
    * Get the average time for a connection in the pool (milliseconds)
    * @return The value
    */
   public long getAveragePoolTime();

   /**
    * Get the average time spent using a connection (milliseconds)
    * @return The value
    */
   public long getAverageUsageTime();

   /**
    * Get the blocking failure count
    * @return The value
    */
   public int getBlockingFailureCount();

   /**
    * Get created count
    * @return The value
    */
   public int getCreatedCount();

   /**
    * Get destroyed count
    * @return The value
    */
   public int getDestroyedCount();

   /**
    * Get idle count
    * @return The value
    */
   public int getIdleCount();

   /**
    * Get in use count
    * @return The value
    */
   public int getInUseCount();

   /**
    * Get max creation time (milliseconds)
    * @return The value
    */
   public long getMaxCreationTime();

   /**
    * Get max get time (milliseconds)
    * @return The value
    */
   public long getMaxGetTime();

   /**
    * Get max pool time (milliseconds)
    * @return The value
    */
   public long getMaxPoolTime();

   /**
    * Get max usage time (milliseconds)
    * @return The value
    */
   public long getMaxUsageTime();

   /**
    * Get max used count
    * @return The value
    */
   public int getMaxUsedCount();

   /**
    * Get max wait count
    * @return The value
    */
   public int getMaxWaitCount();

   /**
    * Get max wait time (milliseconds)
    * @return The value
    */
   public long getMaxWaitTime();

   /**
    * Get timed out
    * @return The value
    */
   public int getTimedOut();

   /**
    * Get the total time spent waiting on connections (milliseconds)
    * @return The value
    */
   public long getTotalBlockingTime();

   /**
    * Get the total time spent creating connections (milliseconds)
    * @return The value
    */
   public long getTotalCreationTime();

   /**
    * Get the total time spent obtaining connections (milliseconds)
    * @return The value
    */
   public long getTotalGetTime();

   /**
    * Get the total time for connections in the pool (milliseconds)
    * @return The value
    */
   public long getTotalPoolTime();

   /**
    * Get the total time spent using connections (milliseconds)
    * @return The value
    */
   public long getTotalUsageTime();

   /**
    * Get wait count
    * @return The value
    */
   public int getWaitCount();
}
