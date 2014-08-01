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
package org.jboss.jca.core.connectionmanager.listener;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.transaction.TransactionSynchronizer;
import org.jboss.jca.core.connectionmanager.tx.TxConnectionManagerImpl;
import org.jboss.jca.core.spi.transaction.ConnectableResource;
import org.jboss.jca.core.spi.transaction.TxUtils;
import org.jboss.jca.core.spi.transaction.local.LocalXAResource;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * Tx connection listener.
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class TxConnectionListener extends AbstractConnectionListener
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, 
      TxConnectionListener.class.getName());
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** Disable failed to enlist message */
   private static boolean disableFailedtoEnlist = false;

   /**Transaction synch. instance*/
   private TransactionSynchronization transactionSynchronization;

   /**XAResource instance*/
   private final XAResource xaResource;

   /** XAResource timeout */
   private final int xaResourceTimeout;

   /** Whether there is a local transaction */
   private final AtomicBoolean localTransaction = new AtomicBoolean(false);

   /** Nuke connection on boundary */
   private boolean killConnectionOnBoundary;

   static
   {
      String value = SecurityActions.getSystemProperty("ironjacamar.disable_enlistment_trace");

      if (value != null && !value.trim().equals(""))
      {
         try
         {
            int equal = value.indexOf("=");
            if (equal != -1)
            {
               String setting = value.substring(equal + 1);
               disableFailedtoEnlist = Boolean.valueOf(setting);
            }
            else
            {
               // Assume enable
               disableFailedtoEnlist = true;
            }
         }
         catch (Throwable t)
         {
            throw new RuntimeException("Unable to parse ironjacamar.disable_enlistment_trace: " + value);
         }
      }
   }

   /**
    * Creates a new tx listener.
    * @param cm connection manager
    * @param mc managed connection
    * @param pool pool
    * @param context context
    * @param flushStrategy flushStrategy
    * @param xaResource xaresource instance
    * @param xaResourceTimeout timeout for the XAResource
    * @throws ResourceException if aexception while creating
    */
   public TxConnectionListener(final ConnectionManager cm, final ManagedConnection mc,
                               final Pool pool, final Object context, final FlushStrategy flushStrategy,
                               final XAResource xaResource, final int xaResourceTimeout)
      throws ResourceException
   {
      super(cm, mc, pool, context, flushStrategy);

      this.xaResource = xaResource;
      this.xaResourceTimeout = xaResourceTimeout;
      this.killConnectionOnBoundary = false;

      if (xaResource instanceof LocalXAResource)
      {
         ((LocalXAResource) xaResource).setConnectionListener(this);
      }
      if (xaResource instanceof ConnectableResource)
      {
         ((ConnectableResource) xaResource).setConnectableResourceListener(this);
      }

      // Kill connection on boundary logic
      String value = SecurityActions.getSystemProperty("ironjacamar.kill_connection_on_boundary");
      if (value != null && !value.trim().equals(""))
      {
         if (pool.getName().equals(value))
            killConnectionOnBoundary = true;
      }
   }

   /**
    * {@inheritDoc}
    */
   protected CoreLogger getLogger()
   {
      return log;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void used()
   {
      super.used();

      // Do a reset of the underlying XAResource timeout
      if (!(xaResource instanceof LocalXAResource) && xaResourceTimeout > 0)
      {
         try
         {
            xaResource.setTransactionTimeout(xaResourceTimeout);
         }
         catch (XAException e)
         {
            log.debug("XAException happend during return for: " + getPool().getName(), e);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void enlist() throws SystemException
   {
      // This method is a bit convulted, but it has to be such because
      // there is a race condition in the transaction manager where it
      // unlocks during the enlist of the XAResource. It does this
      // to avoid distributed deadlocks and to ensure the transaction
      // timeout can fail a badly behaving resource during the enlist.
      //
      // When two threads in the same transaction are trying to enlist
      // connections they could be from the same resource manager
      // or even the same connection when tracking the connection by transaction.
      //
      // For the same connection, we only want to do the real enlist once.
      // For two connections from the same resource manager we don't
      // want the join before the initial start request.
      //
      // The solution is to build up a list of unenlisted resources
      // in the TransactionSynchronizer and then choose one of the
      // threads that is contending in the transaction to enlist them
      // in order. The actual order doesn't really matter as it is the
      // transaction manager that calculates the enlist flags and determines
      // whether the XAResource was already enlisted.
      //
      // Once there are no unenlisted resources the threads are released
      // to return the result of the enlistments.
      //
      // In practice, a thread just takes a snapshot to try to avoid one
      // thread having to do all the work. If it did not do them all
      // the next waiting thread will do the next snapshot until there
      // there is either no snapshot or no waiting threads.
      //
      // A downside to this design is a thread could have its resource enlisted by
      // an earlier thread while it enlists some later thread's resource.
      // Since they are all a part of the same transaction, this is probably
      // not a real issue.

      // No transaction associated with the thread
      TransactionManager tm = getConnectionManager().getTransactionIntegration().getTransactionManager();
      int status = tm.getStatus();
      if (status == Status.STATUS_NO_TRANSACTION)
      {
         if (transactionSynchronization != null && transactionSynchronization.currentTx != null)
         {
            String error = "Attempt to use connection outside a transaction when already a tx!";
            if (trace)
            {
               log.trace(error + " " + this);
            }

            throw new IllegalStateException(error);
         }
         if (trace)
         {
            log.trace("No transaction, no need to enlist: " + this);
         }

         return;
      }

      // Inactive transaction
      Transaction threadTx = tm.getTransaction();
      if (threadTx == null || status != Status.STATUS_ACTIVE)
      {
         String error = "Transaction " + threadTx + " is not active " + TxUtils.getStatusAsString(status);
         if (trace)
         {
            log.trace(error + " cl=" + this);
         }

         throw new IllegalStateException(error);
      }

      if (trace)
      {
         log.trace("Pre-enlist: " + this + " threadTx=" + threadTx);
      }

      // Our synchronization
      TransactionSynchronization ourSynchronization = null;

      // Serializes enlistment when two different threads are enlisting
      // different connections in the same transaction concurrently
      TransactionSynchronizer synchronizer = null;

      try
      {
         TransactionSynchronizer.lock(threadTx,
                                      getConnectionManager().getTransactionIntegration()
                                      .getTransactionSynchronizationRegistry());
      }
      catch (Exception e)
      {
         setTrackByTx(false);
         TxConnectionManagerImpl.rethrowAsSystemException("Exception during lock", threadTx, e);
      }

      try
      {
         // Interleaving should have an unenlisted transaction
         // TODO We should be able to do some sharing shouldn't we?
         if (!isTrackByTx() && transactionSynchronization != null)
         {
            String error = "Can't enlist - already a tx!";
            if (trace)
            {
               log.trace(error + " " + this);
            }
            throw new IllegalStateException(error);
         }

         // Check for different transaction
         if (transactionSynchronization != null && !transactionSynchronization.currentTx.equals(threadTx))
         {
            String error = "Trying to change transaction " + threadTx + " in enlist!";
            if (trace)
            {
               log.trace(error + " " + this);
            }
            throw new IllegalStateException(error);
         }

         // Get the synchronizer
         try
         {
            if (this.trace)
            {
               log.trace("Get synchronizer " + this + " threadTx=" + threadTx);
            }

            synchronizer =
               TransactionSynchronizer.getRegisteredSynchronizer(threadTx,
                  getConnectionManager().getTransactionIntegration().getTransactionSynchronizationRegistry());
         }
         catch (Throwable t)
         {
            setTrackByTx(false);
            TxConnectionManagerImpl.rethrowAsSystemException("Cannot register synchronization", threadTx, t);
         }

         // First time through, create a transaction synchronization
         if (transactionSynchronization == null)
         {
            TransactionSynchronization synchronization = new TransactionSynchronization(threadTx, isTrackByTx());
            synchronizer.addUnenlisted(synchronization);
            transactionSynchronization = synchronization;
         }

         ourSynchronization = transactionSynchronization;
      }
      finally
      {
         TransactionSynchronizer.unlock(threadTx);
      }

      // Perform the enlistment(s)
      List<Synchronization> unenlisted = synchronizer.getUnenlisted();
      if (unenlisted != null)
      {
         try
         {
            int size = unenlisted.size();
            for (int i = 0; i < size; ++i)
            {
               TransactionSynchronization sync = (TransactionSynchronization) unenlisted.get(i);
               if (sync.enlist())
               {
                  synchronizer.addEnlisted(sync);
               }
            }
         }
         finally
         {
            synchronizer.enlisted();
         }
      }

      // What was the result of our enlistment?
      if (this.trace)
      {
         log.trace("Check enlisted " + this + " threadTx=" + threadTx);
      }

      ourSynchronization.checkEnlisted();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void delist() throws ResourceException
   {
      if (trace)
         log.trace("delisting " + this);

      try
      {
         if (!isTrackByTx() && transactionSynchronization != null)
         {
            Transaction tx = transactionSynchronization.currentTx;
            TransactionSynchronization synchronization = transactionSynchronization;
            transactionSynchronization = null;
            if (TxUtils.isUncommitted(tx))
            {
               if (synchronization.enlisted)
               {
                  TransactionSynchronizer synchronizer =
                     TransactionSynchronizer.getRegisteredSynchronizer(tx,
                                                                       getConnectionManager().
                                                                       getTransactionIntegration().
                                                                       getTransactionSynchronizationRegistry());
                  
                  if (!synchronizer.removeEnlisted(synchronization))
                  {
                     if (trace)
                        log.tracef("%s not found in %s", synchronization, synchronizer);
                  }
               }

               if (trace)
                  log.tracef("delistResource(%s, TMSUSPEND)", getXAResource());

               boolean suspendResult = tx.delistResource(getXAResource(), XAResource.TMSUSPEND);

               if (!suspendResult)
               {
                  throw new ResourceException(bundle.failureDelistResource(this));
               }
               else
               {
                  if (trace)
                     log.trace("delist-suspend " + this);
               }
            }
         }

         if (trace)
            log.trace("delisted " + this);
      }
      catch (ResourceException re)
      {
         throw re;
      }
      catch (Throwable t)
      {
         throw new ResourceException(bundle.errorInDelist(), t);
      }
   }

   //local will return this, xa will return one from mc.
   /**
    * Get XA resource.
    * @return xa resource
    */
   protected XAResource getXAResource()
   {
      return xaResource;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void connectionClosed(ConnectionEvent ce)
   {
      if (trace)
         log.trace("connectionClosed called mc=" + this.getManagedConnection());
      if (this.getManagedConnection() != (ManagedConnection)ce.getSource())
         throw new IllegalArgumentException("ConnectionClosed event received from wrong ManagedConnection! Expected: " +
               this.getManagedConnection() + ", actual: " + ce.getSource());

      if (getCachedConnectionManager() != null)
      {
         try
         {
            this.getCachedConnectionManager().unregisterConnection(this.getConnectionManager(),
                                                                   ce.getConnectionHandle());
         }
         catch (Throwable t)
         {
            log.throwableFromUnregisterConnection(t);
         }
      }

      try
      {
         if (wasFreed(ce.getConnectionHandle()))
         {
            delist();
            if (trace)
               log.trace("isManagedConnectionFree=true mc=" + this.getManagedConnection());
            this.getConnectionManager().returnManagedConnection(this, false);
         }
         else
         {
            if (trace)
               log.trace("isManagedConnectionFree=false mc=" + this.getManagedConnection());
         }
      }
      catch (Throwable t)
      {
         log.errorWhileClosingConnectionHandle(t);
         this.getConnectionManager().returnManagedConnection(this, true);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void localTransactionStarted(ConnectionEvent ce)
   {
      localTransaction.set(true);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void localTransactionCommitted(ConnectionEvent ce)
   {
      localTransaction.set(false);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void localTransactionRolledback(ConnectionEvent ce)
   {
      localTransaction.set(false);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void tidyup() throws ResourceException
   {
      // We have a hanging transaction
      if (localTransaction.get())
      {
         LocalTransaction local = null;
         ManagedConnection mc = getManagedConnection();
         try
         {
            local = mc.getLocalTransaction();
         }
         catch (Throwable t)
         {
            throw new ResourceException(bundle.unfinishedLocalTransaction(this), t);
         }
         if (local == null)
            throw new ResourceException(bundle.unfinishedLocalTransactionNotProvideLocalTransaction(this));
         else
         {
            local.rollback();
            log.debug("Unfinished local transaction was rolled back." + this);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void connectionErrorOccurred(ConnectionEvent ce)
   {
      transactionSynchronization = null;
      super.connectionErrorOccurred(ce);
   }

   /**
    * {@inheritDoc}
    */
   //Important method!!
   @Override
   public boolean isManagedConnectionFree()
   {
      if (isTrackByTx() && transactionSynchronization != null)
         return false;
      return super.isManagedConnectionFree();
   }

   /**
    * This method changes the number of handles or
    * the track-by-tx value depending on the parameter passed in
    * @param handle The handle; if <code>null</code> track-by-tx is changed
    * @return True if the managed connection was freed
    */
   synchronized boolean wasFreed(Object handle)
   {
      if (handle != null)
      {
         if (isManagedConnectionFree())
         {
            // This shouldn't really happen now all the state is changed atomically
            return false;
         }

         // Change the number of handles
         getConnectionManager().unregisterAssociation(this, handle);
      }
      else
      {
         if (!isTrackByTx())
         {
            // Only change the state once
            return false;
         }

         // Set track-by-tx to false
         setTrackByTx(false);
      }

      // Return if the managed connection was just freed
      return isManagedConnectionFree();
   }

   /**
    * Transaction sync. class.
    * Please note this class has public access is for test purposes only.
    * Except for test purposes it should be considered private!
    * Don't use it outside enclosing class or testcase!
    */
   public class TransactionSynchronization implements Synchronization
   {
      /**Error message*/
      private final Throwable failedToEnlist;

      /** Transaction */
      protected final Transaction currentTx;

      /** This is the status when we were registered */
      private final boolean wasTrackByTx;

      /** Whether we are enlisted */
      private boolean enlisted;

      /** Any error during enlistment */
      private Throwable enlistError;

      /**
       * Create a new TransactionSynchronization.transaction
       * @param tx transaction
       *
       * @param trackByTx whether this is track by connection
       */
      public TransactionSynchronization(final Transaction tx, final boolean trackByTx)
      {
         this.currentTx = tx;
         this.wasTrackByTx = trackByTx;
         this.enlisted = false;
         this.enlistError = null;

         if (!disableFailedtoEnlist)
         {
            this.failedToEnlist = new Throwable("Unabled to enlist resource, see the previous warnings.");
         }
         else
         {
            this.failedToEnlist = null;
         }
      }

      /**
       * Get the result of the enlistment.
       *
       * @throws SystemException for any error
       */
      public void checkEnlisted() throws SystemException
      {
         if (this.enlistError != null)
         {
            String error = "Error enlisting resource in transaction=" + this.currentTx;
            if (trace)
            {
               log.trace(error + " " + TxConnectionListener.this);
            }

            // Wrap the error to give a reasonable stacktrace since the resource
            // could have been enlisted by a different thread
            if (!disableFailedtoEnlist && enlistError == failedToEnlist)
            {
               throw new SystemException(bundle.systemExceptionWhenFailedToEnlistEqualsCurrentTx(
                     failedToEnlist, this.currentTx));
            }
            else
            {
               SystemException e = new SystemException(error);
               e.initCause(enlistError);
               throw e;
            }
         }
         if (!enlisted)
         {
            String error = "Resource is not enlisted in transaction=" + currentTx;
            if (trace)
            {
               log.trace(error + " " + TxConnectionListener.this);
            }
            throw new IllegalStateException("Resource was not enlisted.");
         }
      }

      /**
       * Enlist the resource
       *
       * @return true when enlisted, false otherwise
       */
      public boolean enlist()
      {
         if (trace)
         {
            log.trace("Enlisting resource " + TxConnectionListener.this);
         }
         try
         {
            XAResource resource = getXAResource();
            if (!currentTx.enlistResource(resource))
            {
               if (!disableFailedtoEnlist)
               {
                  enlistError = failedToEnlist;
               }
               else
               {
                  enlistError = new Throwable("Failed to enlist");
               }
            }
         }
         catch (Throwable t)
         {
            enlistError = t;
         }

         synchronized (this)
         {
            if (enlistError != null)
            {
               if (trace)
               {
                  log.trace("Failed to enlist resource " + TxConnectionListener.this, enlistError);
               }

               setTrackByTx(false);
               transactionSynchronization = null;

               return false;
            }

            enlisted = true;

            if (trace)
            {
               log.trace("Enlisted resource " + TxConnectionListener.this);
            }

            return true;
         }
      }

      /**
       * {@inheritDoc}
       */
      public void beforeCompletion()
      {
         //No-op
      }

      /**
       * {@inheritDoc}
       */
      public void afterCompletion(int status)
      {
         // The connection got destroyed during the transaction
         if (getState().equals(ConnectionState.DESTROYED))
         {
            return;
         }

         // Are we still in the original transaction?
         if (!this.equals(transactionSynchronization))
         {
            // If we are interleaving transactions we have nothing to do
            if (!wasTrackByTx)
            {
               return;
            }
            else
            {
               // There is something wrong with the pooling
               String message = "afterCompletion called with wrong tx! Expected: " +
                  this + ", actual: " + transactionSynchronization;
               IllegalStateException e = new IllegalStateException(message);
               log.somethingWrongWithPooling(e);
            }
         }
         // "Delist"
         transactionSynchronization = null;
         // This is where we close when doing track by transaction
         if (wasTrackByTx)
         {
            if (trace)
            {
               log.trace("afterCompletion(" + status + ") isTrackByTx=" + isTrackByTx() +
                         " for " + TxConnectionListener.this);
            }

            if (wasFreed(null))
            {
               getConnectionManager().returnManagedConnection(TxConnectionListener.this, false);
            }
            else if (killConnectionOnBoundary)
            {
               log.activeHandles(getPool() != null ? getPool().getName() : "Unknown", connectionHandles.size());
               getConnectionManager().returnManagedConnection(TxConnectionListener.this, true);
            }
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String toString()
      {
         StringBuffer buffer = new StringBuffer();
         buffer.append("TxSync@").append(System.identityHashCode(this));
         buffer.append("{tx=").append(currentTx);
         buffer.append(" wasTrackByTx=").append(wasTrackByTx);
         buffer.append(" enlisted=").append(enlisted);
         buffer.append("}");
         return buffer.toString();
      }
   }

   /**
    * {@inheritDoc}
    */
   // For debugging
   @Override
   protected void toString(StringBuffer buffer)
   {
      buffer.append(" xaResource=").append(xaResource);
      buffer.append(" txSync=").append(transactionSynchronization);
   }

   /**
    * Get the transactionSynchronization.
    * Please note this package protected method is for test purposes only. Don't use it!
    *
    * @return the transactionSynchronization.
    */
   final TransactionSynchronization getTransactionSynchronization()
   {
      return transactionSynchronization;
   }

   /**
    * Set the transactionSynchronization.
    * Please note this package protected method is for test purposes only. Don't use it!
    *
    * @param transactionSynchronization The transactionSynchronization to set.
    */
   final void setTransactionSynchronization(TransactionSynchronization transactionSynchronization)
   {
      this.transactionSynchronization = transactionSynchronization;
   }
}
