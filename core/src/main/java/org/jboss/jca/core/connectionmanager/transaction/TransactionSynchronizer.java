/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.transaction;

import org.jboss.jca.core.CoreLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.logging.Logger;

/**
 * Organizes transaction synchronization done by JCA.
 * 
 * <p> 
 * This class exists to make sure all Tx synchronizations
 * are invoked before the cached connection manager closes any
 * closed connections.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author gurkanerdogdu
 * @version $Rev$
 */
public class TransactionSynchronizer implements Synchronization
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, TransactionSynchronizer.class.getName());

   /** The transaction synchronizations */
   private static ConcurrentMap<Integer, TransactionSynchronizer> txSynchs =
      new ConcurrentHashMap<Integer, TransactionSynchronizer>();
   
   /** The locks */
   private static ConcurrentMap<Integer, Lock> locks =
      new ConcurrentHashMap<Integer, Lock>();
   
   /** The transaction */
   private Transaction tx;
   
   /** The enlisting thread */
   private Thread enlistingThread;
   
   /** Unenlisted */
   private List<Synchronization> unenlisted;
   
   /** Enlisted */
   private List<Synchronization> enlisted;
   
   /** The cached connection manager synchronization */
   private Synchronization ccmSynch;
   
   /**
    * Create a new transaction synchronizer
    * 
    * @param tx the transaction to synchronize with
    */
   private TransactionSynchronizer(Transaction tx)
   {
      this.tx = tx;
   }
   
   /**
    * Add a new Tx synchronization that has not been enlisted
    * 
    * @param synch the synchronization
    */
   public synchronized void addUnenlisted(Synchronization synch)
   {
      if (unenlisted == null)
         unenlisted = new ArrayList<Synchronization>(1);

      unenlisted.add(synch);
   }
   
   /**
    * Get the unenlisted synchronizations
    * and say we are enlisting if some are returned.
    * 
    * @return the unenlisted synchronizations
    */
   public synchronized List<Synchronization> getUnenlisted()
   {
      Thread currentThread = Thread.currentThread();

      while (enlistingThread != null && enlistingThread != currentThread)
      {
         boolean interrupted = false;
         try
         {
            wait();
         }
         catch (InterruptedException e)
         {
            interrupted = true;
         }
         if (interrupted)
            currentThread.interrupt();
      }

      List<Synchronization> result = unenlisted;
      unenlisted = null;

      if (result != null)
      {
         enlistingThread = currentThread;
      }

      return result;
   }
   
   /**
    * The synchronization is now enlisted
    * 
    * @param synch the synchronization
    */
   public synchronized void addEnlisted(Synchronization synch)
   {
      if (enlisted == null)
         enlisted = new ArrayList<Synchronization>(1);

      enlisted.add(synch);
   }
   
   /**
    * Remove an enlisted synchronization
    * 
    * @param synch the synchronization
    * @return true when the synchronization was enlisted
    */
   public synchronized boolean removeEnlisted(Synchronization synch)
   {
      return enlisted.remove(synch);
   }
   
   /**
    * This thread has finished enlisting.
    */
   public synchronized void enlisted()
   {
      Thread currentThread = Thread.currentThread();

      if (enlistingThread == null || enlistingThread != currentThread)
      {
         log.threadIsnotEnlistingThread(currentThread, enlistingThread, 
            new Exception("STACKTRACE"));
         return;
      }

      enlistingThread = null;
      notifyAll();
   }
   
   /**
    * Get a registered transaction synchronizer.
    *
    * @param tx the transaction
    * @param tsr the transaction synchronization registry
    * @throws SystemException sys. exception
    * @throws RollbackException rollback exception
    * @return the registered transaction synchronizer for this transaction
    */
   public static TransactionSynchronizer getRegisteredSynchronizer(Transaction tx, 
                                                                   TransactionSynchronizationRegistry tsr)
      throws SystemException, RollbackException
   {
      Integer idx = Integer.valueOf(System.identityHashCode(tx));
      TransactionSynchronizer result = txSynchs.get(idx);
      if (result == null)
      {
         TransactionSynchronizer newResult = new TransactionSynchronizer(tx);
         result = txSynchs.putIfAbsent(idx, newResult);
         if (result == null)
         {
            result = newResult;
            if (tsr != null)
            {
               tsr.registerInterposedSynchronization(result);
            }
            else
            {
               tx.registerSynchronization(result);
            }
         }
      }
      return result;
   }

   /**
    * Check whether we have a CCM synchronization
    * 
    * @param tx the transaction
    * @return synch
    */
   public static Synchronization getCCMSynchronization(Transaction tx)
   {
      Integer idx = Integer.valueOf(System.identityHashCode(tx));
      TransactionSynchronizer ts = txSynchs.get(idx);
      if (ts != null)
         return ts.ccmSynch;  

      return null;  
   }
   
   /**
    * Register a new CCM synchronization
    *
    * @param tx the transaction
    * @param synch the synchronization
    * @param tsr the transaction synchronization registry
    * @throws Exception e
    */
   public static void registerCCMSynchronization(Transaction tx,
                                                 Synchronization synch,
                                                 TransactionSynchronizationRegistry tsr)
      throws Exception
   {
      TransactionSynchronizer ts = getRegisteredSynchronizer(tx, tsr);
      ts.ccmSynch = synch;
   }

   /**
    * Lock for the given transaction
    * 
    * @param tx the transaction
    */
   public static void lock(Transaction tx)
   {
      Integer idx = Integer.valueOf(System.identityHashCode(tx));
      Lock lock = locks.get(idx);
      if (lock == null)
      {
         Lock newLock = new ReentrantLock(true);
         lock = locks.putIfAbsent(idx, newLock);
         if (lock == null)
         {
            lock = newLock;
         }
      }

      try
      {
         lock.lockInterruptibly();
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException("Unable to get synchronization", e);
      }
   }
   
   /**
    * Unlock for the given transaction
    * 
    * @param tx the transaction
    */
   public static void unlock(Transaction tx)
   {
      Integer idx = Integer.valueOf(System.identityHashCode(tx));
      Lock lock = locks.get(idx);

      if (lock != null)
         lock.unlock();
   }

   /**
    * {@inheritDoc}
    */
   public void beforeCompletion()
   {
      if (enlisted != null)
      {
         for (Synchronization synch : enlisted)
         {
            invokeBefore(synch);
         }
      }
      
      if (ccmSynch != null)
      {
         invokeBefore(ccmSynch);  
      }
   }

   /**
    * {@inheritDoc}
    */
   public void afterCompletion(int status)
   {
      if (enlisted != null)
      {
         for (Synchronization synch : enlisted)
         {
            invokeAfter(synch, status);
         }
      }
      
      if (ccmSynch != null)
      {
         invokeAfter(ccmSynch, status);  
      }

      // Cleanup the maps
      Integer idx = Integer.valueOf(System.identityHashCode(tx));
      txSynchs.remove(idx);
      locks.remove(idx);
   }

   /**
    * Invoke a beforeCompletion
    * 
    * @param synch the synchronization
    */
   protected void invokeBefore(Synchronization synch)
   {
      try
      {
         synch.beforeCompletion();
      }
      catch (Throwable t)
      {
         log.transactionErrorInBeforeCompletion(tx, synch, t);
      }
   }

   /**
    * Invoke an afterCompletion
    * 
    * @param synch the synchronization
    * @param status the status of the transaction
    */
   protected void invokeAfter(Synchronization synch, int status)
   {
      try
      {
         synch.afterCompletion(status);
      }
      catch (Throwable t)
      {
         log.transactionErrorInAfterCompletion(tx, synch, t);
      }
   }   
}
