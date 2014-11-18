/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.api;

import org.jboss.jca.core.connectionmanager.pool.PoolStatisticsImpl;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * A semaphore implementation that supports statistics
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Semaphore extends java.util.concurrent.Semaphore
{
   /** Serial version uid */
   private static final long serialVersionUID = 4L;

   /** Max size */
   private int maxSize;

   /** Statistics */
   private PoolStatisticsImpl statistics;

   /**
    * Constructor
    * @param maxSize The maxumum size
    * @param fairness The fairness
    * @param statistics The statistics module
    */
   public Semaphore(int maxSize, boolean fairness, PoolStatisticsImpl statistics)
   {
      super(maxSize, fairness);
      this.maxSize = maxSize;
      this.statistics = statistics;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException
   {
      if (statistics.isEnabled())
         statistics.setMaxWaitCount(getQueueLength());

      boolean result = super.tryAcquire(timeout, unit);

      if (result && statistics.isEnabled())
      {
         statistics.setInUsedCount(maxSize - availablePermits());
      }

      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void release()
   {
      super.release();

      if (statistics.isEnabled())
      {
         statistics.setInUsedCount(maxSize - availablePermits());
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Collection<Thread> getQueuedThreads()
   {
      return super.getQueuedThreads();
   }
}
