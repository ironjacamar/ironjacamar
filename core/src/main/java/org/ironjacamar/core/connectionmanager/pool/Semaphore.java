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

package org.ironjacamar.core.connectionmanager.pool;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * A semaphore implementation
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Semaphore extends java.util.concurrent.Semaphore
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** Statistics */
   private PoolStatisticsImpl statistics;

   /**
    * Constructor
    * @param maxSize The maximum size
    * @param statistics The statistics
    */
   public Semaphore(int maxSize, PoolStatisticsImpl statistics)
   {
      super(maxSize, true);
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

      return super.tryAcquire(timeout, unit);
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
