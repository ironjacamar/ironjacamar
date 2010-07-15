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

package org.jboss.jca.core.connectionmanager.pool.validator;

import org.jboss.jca.core.connectionmanager.pool.InternalManagedConnectionPool;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.logging.Logger;

/**
 * Connection validator class.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev: $
 */
public class ConnectionValidator
{
   /**Logger instance*/
   private static Logger logger = Logger.getLogger(ConnectionValidator.class);
   
   /**Validator thread name*/
   private static final String VALIDATOR_THREAD_NAME = "JBossConnectionValidator";
   
   /**Registered internal pool instances*/
   private CopyOnWriteArrayList<InternalManagedConnectionPool> registeredPools = 
      new CopyOnWriteArrayList<InternalManagedConnectionPool>();
   
   /**Validator executor service*/
   private ExecutorService executorService = null;
   
   /**Singleton instance*/
   private static ConnectionValidator instance = new ConnectionValidator();
   
   /** The interval */
   private long interval = Long.MAX_VALUE;

   /** The next */
 //important initialization!
   private long next = Long.MAX_VALUE;
   
   /**Lock for condition*/
   private Lock lock = new ReentrantLock();
   
   /**Condition*/
   private Condition condition = lock.newCondition();
   
   
   /**
    * Private constructor.
    */
   private ConnectionValidator()
   {
      this.executorService = Executors.newSingleThreadExecutor(new ValidatorThreadFactory());
      this.executorService.execute(new JBossConnectionValidator());
   }
   
   /**
    * Register pool for connection validation.
    * @param mcp managed connection pool
    * @param interval validation interval
    */
   public static void registerPool(InternalManagedConnectionPool mcp, long interval)
   {
      instance.internalRegisterPool(mcp, interval);
   }
   
   /**
    * Unregister pool instance for connection validation.
    * @param mcp pool instance
    */
   public static void unregisterPool(InternalManagedConnectionPool mcp)
   {
      instance.internalUnregisterPool(mcp);
   }
   
   private void internalRegisterPool(InternalManagedConnectionPool mcp, long interval)
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
                  logger.debug("internalRegisterPool: about to notify thread: old next: " +
                        next + ", new next: " + maybeNext);  
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
   
   private void internalUnregisterPool(InternalManagedConnectionPool mcp)
   {
      this.registeredPools.remove(mcp);
      
      if (this.registeredPools.size() == 0) 
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("internalUnregisterPool: setting interval to Long.MAX_VALUE");  
         }
         
         interval = Long.MAX_VALUE;
      }
   }
   
   /**
    * Setup context class loader.
    */
   private void setupContextClassLoader()
   {
      // Could be null if loaded from system classloader
      final ClassLoader cl = ConnectionValidator.class.getClassLoader();
      if (cl == null)
      {
         return;  
      }
      
      SecurityManager sm = System.getSecurityManager();
      
      if (sm == null)
      {
         Thread.currentThread().setContextClassLoader(cl);
         
         return;
      }
      
      AccessController.doPrivileged(new ClassLoaderAction(cl));
 
   }
   
   /**
    * Priviledge action. 
    */
   private static class ClassLoaderAction implements PrivilegedAction<Object>
   {
      private ClassLoader classLoader;
      
      public ClassLoaderAction(ClassLoader cl)
      {
         this.classLoader = cl;
      }
      
      public Object run()
      {
         Thread.currentThread().setContextClassLoader(classLoader);
         
         return null;
      }
      
   }
   
   /**
    * Wait for background thread.
    */
   public static void waitForBackgroundThread()
   {
      try
      {
         instance.lock.lock();
         
      }
      finally
      {
         instance.lock.unlock();  
      }
   }
   
   
   /**
    * Thread factory.
    */
   private static class ValidatorThreadFactory implements ThreadFactory
   {
      /**
       * {@inheritDoc}
       */
      public Thread newThread(Runnable r)
      {
         Thread thread = new Thread(r, ConnectionValidator.VALIDATOR_THREAD_NAME);
         thread.setDaemon(true);
         
         return thread;
      }      
   }
   
   /**
    * JBossConnectionValidator.
    *
    */
   private class JBossConnectionValidator implements Runnable
   {
      
      /**
       * {@inheritDoc}
       */
      public void run()
      {
         setupContextClassLoader();
         
         try
         {
            lock.lock();
            
            while (true)
            {
               boolean result = instance.condition.await(instance.interval, TimeUnit.MILLISECONDS);
               
               if (logger.isTraceEnabled())
               {
                  logger.trace("Result of await ConnectionValidator: " + result);
               }
               
               if (logger.isDebugEnabled())
               {
                  logger.debug("run: ConnectionValidator notifying pools, interval: " + interval);  
               }
     
               for (InternalManagedConnectionPool mcp : registeredPools)
               {
                  mcp.validateConnections();
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
            logger.info("run: ConnectionValidator has been interrupted, returning");
            
            return;  
         }
         catch (RuntimeException e)
         {
            logger.warn("run: ConnectionValidator ignored unexpected runtime exception", e);
         }
         catch (Exception e)
         {
            logger.warn("run: ConnectionValidator ignored unexpected error", e);
         }         
         finally
         {
            lock.unlock();  
         }         
      }      
   }
}
