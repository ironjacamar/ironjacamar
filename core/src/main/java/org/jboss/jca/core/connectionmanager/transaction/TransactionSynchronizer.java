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
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

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

   /** The records */
   private static ConcurrentMap<Object, Record> records =
      new ConcurrentHashMap<Object, Record>(512, 0.75f, 512);
   
   /** The transaction */
   private Transaction tx;

   /** The identifier */
   private Object identifier;
   
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
    * @param id the identifier for the transaction
    */
   private TransactionSynchronizer(Transaction tx, Object id)
   {
      this.tx = tx;
      this.identifier = id;
      this.enlistingThread = null;
      this.unenlisted = new ArrayList<Synchronization>(1);
      this.enlisted = new ArrayList<Synchronization>(1);
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
    * @param ti the transaction integration
    * @throws SystemException sys. exception
    * @throws RollbackException rollback exception
    * @return the registered transaction synchronizer for this transaction
    */
   public static TransactionSynchronizer getRegisteredSynchronizer(Transaction tx, 
                                                                   TransactionIntegration ti)
      throws SystemException, RollbackException
   {
      Object id = ti.getIdentifier(tx);
      Record record = records.get(id);
      if (record == null)
      {
         Record newRecord = new Record(new ReentrantLock(true), new TransactionSynchronizer(tx, id));
         record = records.putIfAbsent(id, newRecord);
         if (record == null)
         {
            record = newRecord;

            if (log.isTraceEnabled())
               log.tracef("Adding: %s [%s]", System.identityHashCode(id), id.toString());

            try
            {
               if (ti.getTransactionSynchronizationRegistry() != null)
               {
                  ti.getTransactionSynchronizationRegistry().
                     registerInterposedSynchronization(record.getTransactionSynchronizer());
               }
               else
               {
                  tx.registerSynchronization(record.getTransactionSynchronizer());
               }
            }
            catch (Throwable t)
            {
               records.remove(id);

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
    * @param ti the transaction integration
    * @return synch
    */
   public static Synchronization getCCMSynchronization(Transaction tx, TransactionIntegration ti)
   {
      Record record = records.get(ti.getIdentifier(tx));
      if (record != null)
         return record.getTransactionSynchronizer().ccmSynch;  

      return null;  
   }
   
   /**
    * Register a new CCM synchronization
    *
    * @param tx the transaction
    * @param synch the synchronization
    * @param ti the transaction integration
    * @throws Exception e
    */
   public static void registerCCMSynchronization(Transaction tx,
                                                 Synchronization synch,
                                                 TransactionIntegration ti)
      throws Exception
   {
      TransactionSynchronizer ts = getRegisteredSynchronizer(tx, ti);
      ts.ccmSynch = synch;
   }

   /**
    * Lock for the given transaction
    * 
    * @param tx the transaction
    * @param ti the transaction integration
    * @throws SystemException sys. exception
    * @throws RollbackException rollback exception
    */
   public static void lock(Transaction tx, TransactionIntegration ti)
      throws SystemException, RollbackException
   {
      Object id = ti.getIdentifier(tx);
      Record record = records.get(id);
      if (record == null)
      {
         Record newRecord = new Record(new ReentrantLock(true), new TransactionSynchronizer(tx, id));
         record = records.putIfAbsent(id, newRecord);
         if (record == null)
         {
            record = newRecord;

            if (log.isTraceEnabled())
               log.tracef("Adding: %s [%s]", System.identityHashCode(id), id.toString());

            try
            {
               if (ti.getTransactionSynchronizationRegistry() != null)
               {
                  ti.getTransactionSynchronizationRegistry().
                     registerInterposedSynchronization(record.getTransactionSynchronizer());
               }
               else
               {
                  tx.registerSynchronization(record.getTransactionSynchronizer());
               }
            }
            catch (Throwable t)
            {
               records.remove(id);

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
    * @param ti the transaction integration
    */
   public static void unlock(Transaction tx, TransactionIntegration ti)
   {
      Record record = records.get(ti.getIdentifier(tx));

      if (record != null)
         record.getLock().unlock();
   }

   /**
    * {@inheritDoc}
    */
   public void beforeCompletion()
   {
      if (ccmSynch != null)
      {
         invokeBefore(ccmSynch);  
      }

      for (Synchronization synch : enlisted)
      {
         invokeBefore(synch);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void afterCompletion(int status)
   {
      if (ccmSynch != null)
      {
         invokeAfter(ccmSynch, status);  
      }

      for (Synchronization synch : enlisted)
      {
         invokeAfter(synch, status);
      }

      // Cleanup the maps
      if (records.remove(identifier) == null)
      {
         // The identifier wasn't stable - scan for it
         Object altKey = null;
         Iterator<Map.Entry<Object, Record>> iterator = records.entrySet().iterator();
         while (altKey == null && iterator.hasNext())
         {
            Map.Entry<Object, Record> next = iterator.next();
            if (next.getValue().getTransactionSynchronizer().equals(this))
            {
               altKey = next.getKey();
            }
         }

         if (altKey != null)
         {
            records.remove(altKey);

            if (log.isTraceEnabled())
               log.tracef("Removed: %s [%s]", System.identityHashCode(identifier), identifier.toString());
         }
         else
         {
            log.transactionNotFound(identifier);
         }
      }
      else
      {
         if (log.isTraceEnabled())
            log.tracef("Removed: %s [%s]", System.identityHashCode(identifier), identifier.toString());
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
