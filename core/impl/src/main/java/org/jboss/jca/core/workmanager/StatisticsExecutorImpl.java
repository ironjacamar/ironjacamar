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

package org.jboss.jca.core.workmanager;

import org.jboss.jca.core.api.workmanager.StatisticsExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.jboss.threads.EnhancedQueueExecutor;

/**
 * A StatisticsExecutor implementation keeping track of numberOfFreeThreads
 *
 * @author Stefano Maestri
 */

public class StatisticsExecutorImpl implements StatisticsExecutor
{
   private final Executor realExecutor;

   /**
    * StatisticsExecutorImpl constructor
    * @param realExecutor the real executor we are delegating
    */
   public StatisticsExecutorImpl(Executor realExecutor)
   {
      this.realExecutor = realExecutor;
   }


   @Override
   public void execute(Runnable runnable)
   {
      realExecutor.execute(runnable);
   }

   @Override
   public long getNumberOfFreeThreads()
   {
      if (realExecutor instanceof EnhancedQueueExecutor)
      {
         return ((EnhancedQueueExecutor) realExecutor).getMaximumPoolSize() -
                ((EnhancedQueueExecutor) realExecutor).getActiveCount();
      }
      else
      {
         return 0;
      }
   }
}
