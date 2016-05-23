/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

import org.ironjacamar.core.CoreLogger;

import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
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
 * Idle connection remover
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class IdleConnectionRemover
{
   /** The logger */
   private static CoreLogger logger = Logger.getMessageLogger(CoreLogger.class,
         IdleConnectionRemover.class.getName());

   /** Thread name */
   private static final String THREAD_NAME = "IronJacamar IdleConnectionRemover";
   
   /** Singleton instance */
   private static IdleConnectionRemover instance = new IdleConnectionRemover();
   
   /** Registered pool instances */
   private TreeMap<Key, ManagedConnectionPool> registeredPools;
   
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
   private IdleConnectionRemover()
   {
      this.registeredPools = new TreeMap<Key, ManagedConnectionPool>(new KeyComparator());
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
   public static IdleConnectionRemover getInstance()
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
         executorService = v;
         isExternal = true;
      }
      else
      {
         executorService = null;
         isExternal = false;
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
         executorService = Executors.newSingleThreadExecutor(new IdleThreadFactory());
      }

      shutdown.set(false);
      interval = Long.MAX_VALUE;
      next = Long.MAX_VALUE;

      executorService.execute(new IdleConnectionRemoverRunner());
   }

   /**
    * Stop
    * @exception Throwable Thrown if an error occurs
    */
   public void stop() throws Throwable
   {
      shutdown.set(true);

      if (!isExternal && executorService != null)
      {
         executorService.shutdownNow();
         executorService = null;
      }

      registeredPools.clear();
   }
   
   /**
    * Register pool for idle connection cleanup
    * @param mcp managed connection pool
    * @param mcpInterval validation interval
    */
   public void registerPool(ManagedConnectionPool mcp, long mcpInterval)
   {
      try
      {
         lock.lock();
         
         synchronized (registeredPools)
         {
            registeredPools.put(new Key(System.identityHashCode(mcp), System.currentTimeMillis(), mcpInterval), mcp);
         }
         
         if (mcpInterval > 1 && mcpInterval / 2 < interval) 
         {
            interval = interval / 2;
            long maybeNext = System.currentTimeMillis() + interval;
            if (next > maybeNext && maybeNext > 0) 
            {
               next = maybeNext;
               condition.signal();
            }
         }
      } 
      finally
      {
         lock.unlock();
      }
   }
   
   /**
    * Unregister pool instance for idle connection cleanup
    * @param mcp pool instance
    */
   public void unregisterPool(ManagedConnectionPool mcp)
   {
      synchronized (registeredPools)
      {
         registeredPools.values().remove(mcp);
      
         if (registeredPools.isEmpty()) 
            interval = Long.MAX_VALUE;
      }
   }

   /**
    * Thread factory
    */
   private static class IdleThreadFactory implements ThreadFactory
   {
      /**
       * {@inheritDoc}
       */
      public Thread newThread(Runnable r)
      {
         Thread thread = new Thread(r, IdleConnectionRemover.THREAD_NAME);
         thread.setDaemon(true);
         
         return thread;
      }      
   }
   
   /**
    * IdleConnectionRemoverRunner
    */
   private class IdleConnectionRemoverRunner implements Runnable
   {
      /**
       * {@inheritDoc}
       */
      public void run()
      {
         final ClassLoader oldTccl = SecurityActions.getThreadContextClassLoader();
         SecurityActions.setThreadContextClassLoader(IdleConnectionRemover.class.getClassLoader());
         
         try
         {
            lock.lock();
            
            while (!shutdown.get())
            {
               boolean result = instance.condition.await(instance.interval, TimeUnit.MILLISECONDS);

               // We only scan the ManagedConnectionPools that needs to be
               NavigableMap<Key, ManagedConnectionPool> entries =
                  registeredPools.headMap(new Key(System.currentTimeMillis()), true);

               for (Map.Entry<Key, ManagedConnectionPool> entry : entries.entrySet())
               {
                  entry.getValue().removeIdleConnections();
                  entry.getKey().update();
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

   /**
    * Key for ManagedConnectionPool instance
    */
   private static class Key
   {
      private int id;
      private long timestamp;
      private long interval;

      /**
       * Constructor used for scanning
       * @param timestamp The timestamp
       */
      public Key(long timestamp)
      {
         this(-1, timestamp, 0);
      }

      /**
       * Constructor
       * @param id The system identity of the ManagedConnectionPool
       * @param timestamp The timestamp
       * @param interval The interval
       */
      public Key(int id, long timestamp, long interval)
      {
         this.id = id;
         this.timestamp = timestamp;
         this.interval = interval;
      }

      /**
       * Update the timestamp
       */
      public void update()
      {
         timestamp = System.currentTimeMillis();
      }

      /**
       * {@inheritDoc}
       */
      public int hashCode()
      {
         int hash = 7;
         hash += 7 * id;
         hash += (int)(7L * timestamp);
         hash += (int)(7L * interval);
         return hash;
      }

      /**
       * {@inheritDoc}
       */
      public boolean equals(Object o)
      {
         if (o == this)
            return true;

         if (o == null || !(o instanceof Key))
            return false;

         Key k = (Key)o;

         return id == k.id;
      }

      /**
       * {@inheritDoc}
       */
      public String toString()
      {
         return "[" + Integer.toHexString(id) + "," + timestamp  + "," + interval + "]"; 
      }
   }

   /**
    * Comparator for Key
    */
   private static class KeyComparator implements Comparator<Key>
   {
      /**
       * Constructor
       */
      public KeyComparator()
      {
      }

      /**
       * {@inheritDoc}
       */
      public int compare(Key k1, Key k2)
      {
         long t1 = k1.timestamp + (k1.interval / 2);
         long t2 = k2.timestamp + (k2.interval / 2);
         
         if (t1 < t2)
            return -1;
         
         if (t1 > t2)
            return 1;

         if (k1.id != -1 && k2.id != -1)
         {
            if (k1.id < k2.id)
               return -1;
         
            if (k1.id > k2.id)
               return 1;
         }
         else
         {
            if (k1.id == -1)
            {
               return 1;
            }
            else
            {               
               return -1;
            }
         }
         
         return 0;
      }

      /**
       * {@inheritDoc}
       */
      public int hashCode()
      {
         return 42;
      }

      /**
       * {@inheritDoc}
       */
      public boolean equals(Object o)
      {
         if (o == this)
            return true;

         if (o == null || !(o instanceof KeyComparator))
            return false;

         return true;
      }
   }
}
