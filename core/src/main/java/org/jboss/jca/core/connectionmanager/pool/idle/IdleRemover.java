/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2011, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.idle;

import org.jboss.jca.core.CoreLogger;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.logging.Logger;

/**
 * Idle remover
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class IdleRemover
{
   /** Logger instance */
   private static CoreLogger logger = Logger.getMessageLogger(CoreLogger.class, IdleRemover.class.getName());
   
   /** Thread name */
   private static final String THREAD_NAME = "IdleRemover";
   
   /** Singleton instance */
   private static IdleRemover instance = new IdleRemover();
   
   /** Registered pool instances */
   private CopyOnWriteArrayList<IdleConnectionRemovalSupport> registeredPools = 
      new CopyOnWriteArrayList<IdleConnectionRemovalSupport>();
   
   /** Executor service */
   private ExecutorService executorService;
   
   /** Is the executor external */
   private boolean isExternal;

   /** The interval */
   private long interval;

   /** The next scan */
   private long next;
   
   /** Shutdown */
   private AtomicBoolean shutdown;

   /** Lock */
   private Lock lock;
   
   /** Condition */
   private Condition condition;
   
   /**
    * Private constructor.
    */
   private IdleRemover()
   {
      this.executorService = null;
      this.isExternal = false;
      this.interval = Long.MAX_VALUE;
      this.next = Long.MAX_VALUE;
      this.shutdown = new AtomicBoolean(false);
      this.lock = new ReentrantLock(true);
      this.condition = lock.newCondition();
   }

   /**
    * Get the instance
    * @return The value
    */
   public static IdleRemover getInstance()
   {
      return instance;
   }
   
   /**
    * Set the executor service
    * @param v The value
    */
   public void setExecutorService(ExecutorService v)
   {
      if (v != null)
      {
         this.executorService = v;
         this.isExternal = true;
      }
      else
      {
         this.executorService = null;
         this.isExternal = false;
      }
   }

   /**
    * Start
    * @exception Throwable Thrown if an error occurs
    */
   public void start() throws Throwable
   {
      if (!isExternal)
      {
         this.executorService = Executors.newSingleThreadExecutor(new IdleRemoverThreadFactory());
      }

      this.shutdown.set(false);
      this.interval = Long.MAX_VALUE;
      this.next = Long.MAX_VALUE;

      this.executorService.execute(new IdleRemoverRunner());
   }

   /**
    * Stop
    * @exception Throwable Thrown if an error occurs
    */
   public void stop() throws Throwable
   {
      instance.shutdown.set(true);

      if (!isExternal)
      {
         instance.executorService.shutdownNow();
         instance.executorService = null;
      }

      instance.registeredPools.clear();
   }
   
   /**
    * Register pool for connection validation.
    * @param mcp managed connection pool
    * @param interval validation interval
    */
   public void registerPool(IdleConnectionRemovalSupport mcp, long interval)
   {
      logger.debugf("Register pool: %s (interval=%s)", mcp, interval);

      instance.internalRegisterPool(mcp, interval);
   }
   
   /**
    * Unregister pool instance for connection validation.
    * @param mcp pool instance
    */
   public void unregisterPool(IdleConnectionRemovalSupport mcp)
   {
      logger.debugf("Unregister pool: %s", mcp);

      instance.internalUnregisterPool(mcp);
   }

   private void internalRegisterPool(IdleConnectionRemovalSupport mcp, long interval)
   {
      try
      {
         this.lock.lock();
         
         this.registeredPools.addIfAbsent(mcp);
         
         if (interval > 1 && interval / 2 < this.interval) 
         {
            this.interval = interval / 2;
            long maybeNext = System.currentTimeMillis() + this.interval;
            if (next > maybeNext && maybeNext > 0) 
            {
               next = maybeNext;
               if (logger.isDebugEnabled())
               {
                  logger.debug("About to notify thread: old next: " + next + ", new next: " + maybeNext);
               }               
               
               this.condition.signal();
            }
         }         
      } 
      finally
      {
         this.lock.unlock();
      }
   }
   
   private void internalUnregisterPool(IdleConnectionRemovalSupport mcp)
   {
      this.registeredPools.remove(mcp);
      
      if (this.registeredPools.size() == 0) 
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("Setting interval to Long.MAX_VALUE");  
         }
         
         interval = Long.MAX_VALUE;
      }
   }
         
   /**
    * Thread factory.
    */
   private static class IdleRemoverThreadFactory implements ThreadFactory
   {
      /**
       * {@inheritDoc}
       */
      public Thread newThread(Runnable r)
      {
         Thread thread = new Thread(r, IdleRemover.THREAD_NAME);
         thread.setDaemon(true);
         
         return thread;
      }      
   }
   
   /**
    * IdleRemoverRunner
    */
   private class IdleRemoverRunner implements Runnable
   {
      /**
       * {@inheritDoc}
       */
      public void run()
      {
         final ClassLoader oldTccl = SecurityActions.getThreadContextClassLoader();
         SecurityActions.setThreadContextClassLoader(IdleRemover.class.getClassLoader());
         
         try
         {
            lock.lock();
            
            while (!shutdown.get())
            {
               boolean result = instance.condition.await(instance.interval, TimeUnit.MILLISECONDS);

               if (logger.isTraceEnabled())
               {
                  logger.trace("Result of await: " + result);
               }

               if (logger.isDebugEnabled())
               {
                  logger.debug("Notifying pools, interval: " + interval);  
               }
     
               for (IdleConnectionRemovalSupport mcp : registeredPools)
               {
                  mcp.removeIdleConnections();
               }

               next = System.currentTimeMillis() + interval;
               
               if (next < 0)
               {
                  next = Long.MAX_VALUE;  
               }
            }            
         }
         catch (InterruptedException e)
         {
            if (!shutdown.get())
               logger.returningConnectionValidatorInterrupted();
         }
         catch (RuntimeException e)
         {
            logger.connectionValidatorIgnoredUnexpectedRuntimeException(e);
         }
         catch (Exception e)
         {
            logger.connectionValidatorIgnoredUnexpectedError(e);
         }         
         finally
         {
            lock.unlock();  
            SecurityActions.setThreadContextClassLoader(oldTccl);
         }
      }
   }
}
