/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2006, Red Hat Inc, and individual contributors
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author gurkanerdogdu
 * @version $Rev$
 */
public class TransactionSynchronizer implements Synchronization
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, TransactionSynchronizer.class.getName());

   /** Trace */
   private static boolean trace = log.isTraceEnabled();

   /** The records */
   private static ConcurrentMap<Transaction, Record> records =
      new ConcurrentHashMap<Transaction, Record>();
   
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
      if (enlisted == null)
         return false;

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
      Record record = records.get(tx);
      if (record == null)
      {
         Record newRecord = new Record(new ReentrantLock(true), new TransactionSynchronizer(tx));
         record = records.putIfAbsent(tx, newRecord);
         if (record == null)
         {
            record = newRecord;

            if (trace)
               log.tracef("Adding: %s [%s]", System.identityHashCode(tx), tx.toString());

            try
            {
               if (tsr != null)
               {
                  tsr.registerInterposedSynchronization(record.getTransactionSynchronizer());
               }
               else
               {
                  tx.registerSynchronization(record.getTransactionSynchronizer());
               }
            }
            catch (Throwable t)
            {
               records.remove(tx);

               if (t instanceof SystemException)
               {
                  throw (SystemException)t;
               }
               else if (t instanceof RollbackException)
               {
                  throw (RollbackException)t;
               }
               else
               {
                  SystemException se = new SystemException(t.getMessage());
                  se.initCause(t);
                  throw se;
               }
            }
         }
      }
      return record.getTransactionSynchronizer();
   }

   /**
    * Check whether we have a CCM synchronization
    * 
    * @param tx the transaction
    * @return synch
    */
   public static Synchronization getCCMSynchronization(Transaction tx)
   {
      Record record = records.get(tx);
      if (record != null)
         return record.getTransactionSynchronizer().ccmSynch;  

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
    * @param tsr the transaction synchronization registry
    * @throws SystemException sys. exception
    * @throws RollbackException rollback exception
    */
   public static void lock(Transaction tx, TransactionSynchronizationRegistry tsr)
      throws SystemException, RollbackException
   {
      Record record = records.get(tx);
      if (record == null)
      {
         Record newRecord = new Record(new ReentrantLock(true), new TransactionSynchronizer(tx));
         record = records.putIfAbsent(tx, newRecord);
         if (record == null)
         {
            record = newRecord;

            if (trace)
               log.tracef("Adding: %s [%s]", System.identityHashCode(tx), tx.toString());

            try
            {
               if (tsr != null)
               {
                  tsr.registerInterposedSynchronization(record.getTransactionSynchronizer());
               }
               else
               {
                  tx.registerSynchronization(record.getTransactionSynchronizer());
               }
            }
            catch (Throwable t)
            {
               records.remove(tx);

               if (t instanceof SystemException)
               {
                  throw (SystemException)t;
               }
               else if (t instanceof RollbackException)
               {
                  throw (RollbackException)t;
               }
               else
               {
                  SystemException se = new SystemException(t.getMessage());
                  se.initCause(t);
                  throw se;
               }
            }
         }
      }

      Lock lock = record.getLock();

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
      Record record = records.get(tx);

      if (record != null)
         record.getLock().unlock();
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

      // Try with hashCode first
      if (records.remove(tx) == null)
      {
         // Cleanup the maps -- only trust TransactionSynchronizer
         Object altKey = null;

         Iterator<Map.Entry<Transaction, Record>> iterator = records.entrySet().iterator();
         while (altKey == null && iterator.hasNext())
         {
            Map.Entry<Transaction, Record> next = iterator.next();
            if (next.getValue().getTransactionSynchronizer().equals(this))
            {
               altKey = next.getKey();
            }
         }

         if (altKey != null)
         {
            records.remove(altKey);

            if (trace)
               log.tracef("Removed: %s [%s]", System.identityHashCode(tx), tx.toString());
         }
         else
         {
            log.transactionNotFound(tx);
         }
      }
      else
      {
         if (trace)
            log.tracef("Removed: %s [%s]", System.identityHashCode(tx), tx.toString());
      }
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

   /**
    * A record for a transaction
    */
   static class Record
   {
      private Lock lock;
      private TransactionSynchronizer txSync;

      /**
       * Constructor
       * @param lock The lock
       * @param txSync The transaction synchronizer
       */
      Record(Lock lock, TransactionSynchronizer txSync)
      {
         this.lock = lock;
         this.txSync = txSync;
      }

      /**
       * Get the lock
       * @return The value
       */
      Lock getLock()
      {
         return lock;
      }

      /**
       * Get the transaction synchronizer
       * @return The synchronizer
       */
      TransactionSynchronizer getTransactionSynchronizer()
      {
         return txSync;
      }
   }
}
