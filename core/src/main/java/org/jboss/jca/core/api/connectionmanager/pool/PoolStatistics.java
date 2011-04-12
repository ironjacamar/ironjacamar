/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface PoolStatistics extends StatisticsPlugin
{
   /**
    * Get active count
    * @return The value
    */
   public int getActiveCount();

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
    * Get max creation time (milliseconds)
    * @return The value
    */
   public long getMaxCreationTime();

   /**
    * Get max used count
    * @return The value
    */
   public int getMaxUsedCount();

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
}
