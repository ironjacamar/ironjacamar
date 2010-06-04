/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.web;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.thread.ThreadPool;

/**
 * An implementation of the Jetty ThreadPool interface using an ExecutorService
 */
public class ExecutorThreadPool extends AbstractLifeCycle implements ThreadPool
{
   private static Logger log = Logger.getLogger(ExecutorThreadPool.class);
   private static boolean trace = log.isTraceEnabled();

   private final ExecutorService executor;

   /**
    * Constrcutor
    * @param executor The executor service
    */
   public ExecutorThreadPool(ExecutorService executor)
   {
      this.executor = executor;
   }

   /**
    * Dispatch a job
    * @param job The job
    * @return True if dispatched; otherwise false
    */
   public boolean dispatch(Runnable job)
   {
      try
      {       
         executor.execute(job);
         return true;
      }
      catch (RejectedExecutionException e)
      {
         log.warn("Dispatch rejected", e);
         return false;
      }
   }

   /**
    * Get the number of idle threads
    * @return The number; -1 if not supported
    */
   public int getIdleThreads()
   {
      if (executor instanceof ThreadPoolExecutor)
      {
         final ThreadPoolExecutor tpe = (ThreadPoolExecutor)executor;
         return tpe.getPoolSize() - tpe.getActiveCount();
      }
      return -1;
   }

   /**
    * Get the number of threads
    * @return The number; -1 if not supported
    */
   public int getThreads()
   {
      if (executor instanceof ThreadPoolExecutor)
      {
         final ThreadPoolExecutor tpe = (ThreadPoolExecutor)executor;
         return tpe.getPoolSize();
      }
      return -1;
   }

   /**
    * Is the pool low on threads ?
    * @return True if active threads >= maximum number of threads
    */
   public boolean isLowOnThreads()
   {
      if (executor instanceof ThreadPoolExecutor)
      {
         final ThreadPoolExecutor tpe = (ThreadPoolExecutor)executor;
         return tpe.getActiveCount() >= tpe.getMaximumPoolSize();
      }
      return false;
   }

   /**
    * Join - await termination of pool
    * @exception InterruptedException Thrown if interrupted
    */
   public void join() throws InterruptedException
   {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
   }

   /**
    * Start
    * @exception Exception Thrown if an error occurs
    */
   @Override
   protected void doStart() throws Exception
   {
      super.doStart();
   }

   /**
    * Stop
    * @exception Exception Thrown if an error occurs
    */
   @Override
   protected void doStop() throws Exception
   {
      super.doStop();
   }
}
