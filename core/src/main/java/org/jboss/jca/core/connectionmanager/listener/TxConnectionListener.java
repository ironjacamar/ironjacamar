/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.connectionmanager.TxConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.transaction.LockKey;
import org.jboss.jca.core.connectionmanager.transaction.TransactionSynchronizer;
import org.jboss.jca.core.connectionmanager.tx.TxConnectionManagerImpl;
import org.jboss.jca.core.spi.transaction.ConnectableResource;
import org.jboss.jca.core.spi.transaction.TxUtils;
import org.jboss.jca.core.spi.transaction.local.LocalXAResource;
import org.jboss.jca.core.tracer.Tracer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * Tx connection listener.
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
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

   /** Delist resource */
   private boolean doDelistResource;

   /** Set rollback */
   private boolean doSetRollbackOnly;

   /** Enlistment trace */
   private Boolean enlistmentTrace;

   static
   {
      String value = SecurityActions.getSystemProperty("ironjacamar.disable_enlistment_trace");

      if (value != null && !value.trim().equals(""))
      {
         try
         {
            disableFailedtoEnlist = Boolean.valueOf(value);
         }
         catch (Throwable t)
         {
            // Assume enable
            disableFailedtoEnlist = true;
         }
      }
   }

   /**
    * Creates a new tx listener.
    * @param cm connection manager
    * @param mc managed connection
    * @param pool pool
    * @param mcp mcp
    * @param flushStrategy flushStrategy
    * @param tracking tracking
    * @param enlistmentTrace enlistmentTrace
    * @param xaResource xaresource instance
    * @param xaResourceTimeout timeout for the XAResource
    * @throws ResourceException if aexception while creating
    */
   public TxConnectionListener(final ConnectionManager cm, final ManagedConnection mc,
                               final Pool pool, final ManagedConnectionPool mcp, final FlushStrategy flushStrategy,
                               final Boolean tracking, final Boolean enlistmentTrace,
                               final XAResource xaResource, final int xaResourceTimeout)
      throws ResourceException
   {
      super(cm, mc, pool, mcp, flushStrategy, tracking);

      this.xaResource = xaResource;
      this.xaResourceTimeout = xaResourceTimeout;
      this.doDelistResource = true;
      this.doSetRollbackOnly = true;
      this.enlistmentTrace = enlistmentTrace;
      
      if (xaResource instanceof LocalXAResource)
      {
         ((LocalXAResource) xaResource).setConnectionListener(this);
      }
      if (xaResource instanceof ConnectableResource)
      {
         ((ConnectableResource) xaResource).setConnectableResourceListener(this);
      }

      String value = SecurityActions.getSystemProperty("ironjacamar.no_delist_resource");
      if (value != null && !value.trim().equals(""))
      {
         StringTokenizer st = new StringTokenizer(value, ",");
         while (doDelistResource && st.hasMoreTokens())
         {
            if (getPool().getName().equals(st.nextToken()))
               doDelistResource = false;
         }
      }
      value = SecurityActions.getSystemProperty("ironjacamar.no_delist_resource_all");
      if (value != null && !value.trim().equals(""))
      {
         doDelistResource = false;
      }

      value = SecurityActions.getSystemProperty("ironjacamar.rollback_on_fatal_error");
      if (value != null && !value.trim().equals(""))
      {
         if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))
         {
            doSetRollbackOnly = Boolean.parseBoolean(value);
         }
         else
         {
            StringTokenizer st = new StringTokenizer(value, ",");
            while (doSetRollbackOnly && st.hasMoreTokens())
            {
               if (getPool().getName().equals(st.nextToken()))
                  doSetRollbackOnly = false;
            }
         }
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
   public void toPool()
   {
      super.toPool();

      // Do a reset of the underlying XAResource timeout
      if (!(xaResource instanceof LocalXAResource) && xaResourceTimeout > 0)
      {
         try
         {
            xaResource.setTransactionTimeout(xaResourceTimeout);
         }
         catch (XAException e)
         {
            log.debugf(e, "XAException happend during return for: %s",
                       getPool() != null ? getPool().getName() : "Unknown");
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

      // If we are already enlisted there is no reason to check again, as this method
      // could be called multiple times during a transaction lifecycle.
      // We know that we can only be inside this method if we are allowed to
      if (isEnlisted() || getState().equals(ConnectionState.DESTROY) || getState().equals(ConnectionState.DESTROYED))
         return;

      // No transaction associated with the thread
      TransactionManager tm = getConnectionManager().getTransactionIntegration().getTransactionManager();
      int status = tm.getStatus();

      if (status == Status.STATUS_NO_TRANSACTION)
      {
         if (transactionSynchronization != null && transactionSynchronization.currentTx != null)
         {
            String error = "Attempt to use connection outside a transaction when already a tx!";
            log.tracef("%s %s", error, this);
            

            throw new IllegalStateException(error);
         }
         log.tracef("No transaction, no need to enlist: %s", this);

         return;
      }

      // Inactive transaction
      Transaction threadTx = tm.getTransaction();
      if (threadTx == null || status != Status.STATUS_ACTIVE)
      {
         TxConnectionManager txConnectionManager = (TxConnectionManager)getConnectionManager();

         if (!txConnectionManager.isAllowMarkedForRollback())
         {
            String error = "Transaction " + threadTx + " is not active " + TxUtils.getStatusAsString(status);
            log.tracef("%s cl=%s", error, this);

            throw new IllegalStateException(error);
         }
      }

      log.tracef("Pre-enlist: %s threadTx=%s", this, threadTx);

      // Our synchronization
      TransactionSynchronization ourSynchronization = null;

      // Serializes enlistment when two different threads are enlisting
      // different connections in the same transaction concurrently
      TransactionSynchronizer synchronizer = null;

      try
      {
         TransactionSynchronizer.lock(threadTx,
                                      getConnectionManager().getTransactionIntegration());
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
            log.tracef("%s %s", error, this);
            throw new IllegalStateException(error);
         }

         // Check for different transaction
         if (transactionSynchronization != null && !transactionSynchronization.currentTx.equals(threadTx))
         {
            String error = "Trying to change transaction " + threadTx + " in enlist!";
            log.tracef("%s %s", error, this);
            throw new IllegalStateException(error);
         }

         // Get the synchronizer
         try
         {
            log.tracef("Get synchronizer %s threadTx=%s", this, threadTx);

            synchronizer =
               TransactionSynchronizer.getRegisteredSynchronizer(threadTx,
                  getConnectionManager().getTransactionIntegration());
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
         TransactionSynchronizer.unlock(threadTx, getConnectionManager().getTransactionIntegration());
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
      log.tracef("Check enlisted %s threadTx=%s", this, threadTx);

      ourSynchronization.checkEnlisted();
      setEnlisted(true);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean delist() throws ResourceException
   {
      log.tracef("delisting %s", this);

      boolean success = true;

      try
      {
         if (!isTrackByTx())
         {
            if (transactionSynchronization != null)
            {
               // SUSPEND
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
                                                                          getTransactionIntegration());

                     if (!synchronizer.removeEnlisted(synchronization))
                     {
                        log.tracef("%s not found in %s", synchronization, synchronizer);
                     }   
                  }

                  if (!getState().equals(ConnectionState.DESTROYED))
                  {
                     log.tracef("delistResource(%s, TMSUSPEND)", getXAResource());

                     boolean suspendResult = tx.delistResource(getXAResource(), XAResource.TMSUSPEND);

                     if (Tracer.isEnabled())
                        Tracer.delistConnectionListener(getPool() != null ? getPool().getName() : null,
                                                        getManagedConnectionPool(),
                                                        this, tx.toString(),
                                                        suspendResult, false, true);

                     if (!suspendResult)
                     {
                        throw new ResourceException(bundle.failureDelistResource(this));
                     }
                     else
                     {
                        log.tracef("delist-suspend: %s", this);
                     }
                  }
               }
            }
            else
            {
               // SUCCESS / FAIL
               if (!getState().equals(ConnectionState.DESTROYED) &&
                   isManagedConnectionFree() &&
                   isEnlisted() &&
                   doDelistResource)
               {
                  if (getConnectionManager().getTransactionIntegration() != null &&
                      getConnectionManager().getTransactionIntegration().getTransactionManager() != null)
                  {
                     Transaction tx = getConnectionManager().getTransactionIntegration().
                        getTransactionManager().getTransaction();

                     if (TxUtils.isUncommitted(tx))
                     {
                        if (TxUtils.isActive(tx))
                        {
                           log.tracef("delistResource(%s, TMSUCCESS)", getXAResource());

                           boolean successResult = tx.delistResource(getXAResource(), XAResource.TMSUCCESS);

                           if (Tracer.isEnabled())
                              Tracer.delistConnectionListener(getPool() != null ? getPool().getName() : null,
                                                              getManagedConnectionPool(),
                                                              this, tx.toString(),
                                                              true, false, true);

                           if (successResult)
                           {
                              log.tracef("delist-success: %s", this);
                           }
                           else
                           {
                              log.debugf("delist-success failed: %s", this);
                              success = false;
                           }
                        }
                        else
                        {
                           log.tracef("delistResource(%s, TMFAIL)", getXAResource());

                           boolean failResult = tx.delistResource(getXAResource(), XAResource.TMFAIL);
                           
                           if (Tracer.isEnabled())
                              Tracer.delistConnectionListener(getPool() != null ? getPool().getName() : null,
                                                              getManagedConnectionPool(),
                                                              this, tx.toString(),
                                                              false, false, true);

                           if (failResult)
                           {
                              log.tracef("delist-fail: %s", this);
                           }
                           else
                           {
                              log.debugf("delist-fail failed: %s", this);

                              success = false;
                           }
                        }
                     }
                  }
               }
            }

            setEnlisted(false);
         }

         log.tracef("delisted %s", this);

         return success;
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

   /**
    * {@inheritDoc}
    */
   @Override
   public void dissociate() throws ResourceException
   {
      log.tracef("dissociate: %s", this);

      try
      {
         TransactionManager tm = getConnectionManager().getTransactionIntegration().getTransactionManager();
         int status = tm.getStatus();

         log.tracef("dissociate: status=%s", TxUtils.getStatusAsString(status));

         if (status != Status.STATUS_NO_TRANSACTION)
         {
            if (isEnlisted())
            {
               if (doDelistResource)
               {
                  Transaction tx = tm.getTransaction();
                  boolean delistResult = tx.delistResource(getXAResource(), XAResource.TMSUCCESS);

                  log.tracef("dissociate: delistResult=%s", delistResult);
               }
            }
            else
            {
               log.tracef("dissociate: not enlisted (%s)", this);
            }

            if (isTrackByTx())
            {
               ManagedConnectionPool mcp = getManagedConnectionPool();
               TransactionSynchronizationRegistry tsr =
                  getConnectionManager().getTransactionIntegration().getTransactionSynchronizationRegistry();

               Lock lock = (Lock)tsr.getResource(LockKey.INSTANCE);
               if (lock != null)
               {
                  try
                  {
                     lock.lockInterruptibly();
                  }
                  catch (InterruptedException ie)
                  {
                     Thread.interrupted();
                     
                     throw new ResourceException(bundle.unableObtainLock(), ie);
                  }

                  try
                  {
                     tsr.putResource(mcp, null);
                  }
                  finally
                  {
                     lock.unlock();
                  }
               }
            }
         }

         localTransaction.set(false);
         setTrackByTx(false);
      
         if (transactionSynchronization != null)
         {
            transactionSynchronization.cancel();
            transactionSynchronization = null;
         }

         setEnlisted(false);
      }
      catch (Throwable t)
      {
         throw new ResourceException(bundle.errorInDissociate(), t);
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
      log.tracef("connectionClosed called mc=%s", this.getManagedConnection());
      if (this.getManagedConnection() != (ManagedConnection)ce.getSource())
         throw new IllegalArgumentException("ConnectionClosed event received from wrong ManagedConnection! Expected: " +
               this.getManagedConnection() + ", actual: " + ce.getSource());

      if (getCachedConnectionManager() != null)
      {
         try
         {
            this.getCachedConnectionManager().unregisterConnection(this.getConnectionManager(), this,
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
            boolean success = delist();

            log.tracef("isManagedConnectionFree=true mc=%s", this.getManagedConnection());

            if (success || (tracking != null && !tracking.booleanValue()))
            {
               this.getConnectionManager().returnManagedConnection(this, false);
            }
            else
            {
               log.delistingFailed(getPool() != null ? getPool().getName() : "Unknown", new Exception());
               this.getConnectionManager().returnManagedConnection(this, true);
            }
         }
         else
         {
            log.tracef("isManagedConnectionFree=false mc=%s", this.getManagedConnection());
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
            log.debugf("Unfinished local transaction was rolled back.%s", this);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   void haltCatchFire()
   {
      if (isEnlisted())
      {
         if (transactionSynchronization != null)
            transactionSynchronization.cancel();

         String txId = "";

         if (getConnectionManager().getTransactionIntegration() != null &&
             getConnectionManager().getTransactionIntegration().getTransactionManager() != null)
         {
            Transaction tx = null;
            try
            {
               tx = getConnectionManager().getTransactionIntegration().getTransactionManager().getTransaction();

               if (Tracer.isEnabled() && tx != null)
                  txId = tx.toString();
               
               if (TxUtils.isUncommitted(tx) && doDelistResource)
               {
                  log.tracef("connectionErrorOccurred: delistResource(%s, TMFAIL)", getXAResource());

                  boolean failResult = tx.delistResource(getXAResource(), XAResource.TMFAIL);
                  
                  if (failResult)
                  {
                     log.tracef("connectionErrorOccurred: delist-fail: %s", this);
                  }
                  else
                  {
                     log.debugf("connectionErrorOccurred: delist-fail failed: %s", this);
                  }
               }
            }
            catch (Exception e)
            {
               log.debugf(e, "connectionErrorOccurred: Exception during delistResource=%s", e.getMessage());
            }
            finally
            {
               if (TxUtils.isUncommitted(tx) && doSetRollbackOnly)
               {
                  try
                  {
                     tx.setRollbackOnly();
                  }
                  catch (Exception e)
                  {
                     // Just a hint
                  }
               }
            }
         }
         
         if (Tracer.isEnabled())
         {
            Tracer.delistConnectionListener(getPool() != null ? getPool().getName() : null,
                                            getManagedConnectionPool(),
                                            this, txId, false, true, false);
         }
      }

      // Prepare to explode
      setEnlisted(false);
      transactionSynchronization = null;
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
   boolean wasFreed(Object handle)
   {
      if (handle != null)
      {
         // Unregister the handle
         unregisterConnection(handle);
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

      /** Record enlist */
      private final boolean recordEnlist;

      /** Transaction */
      protected final Transaction currentTx;

      /** This is the status when we were registered */
      private final boolean wasTrackByTx;

      /** Whether we are enlisted */
      private boolean enlisted;

      /** Any error during enlistment */
      private Throwable enlistError;

      /** Cancel */
      private boolean cancel;

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
         this.cancel = false;

         if (TxConnectionListener.this.enlistmentTrace != null)
         {
            this.recordEnlist = TxConnectionListener.this.enlistmentTrace.booleanValue();
         }
         else
         {
            this.recordEnlist = !disableFailedtoEnlist;
         }
         
         if (this.recordEnlist)
         {
            this.failedToEnlist = new Throwable("Unabled to enlist resource, see the previous warnings.");
         }
         else
         {
            this.failedToEnlist = null;
         }

         log.tracef("%s: Constructor", toString());
      }

      /**
       * Set the cancel flag
       */
      public void cancel()
      {
         cancel = true;
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
            log.tracef("%s %s", error, TxConnectionListener.this);

            // Wrap the error to give a reasonable stacktrace since the resource
            // could have been enlisted by a different thread
            if (recordEnlist && enlistError == failedToEnlist)
            {
               SystemException se =
                  new SystemException(bundle.systemExceptionWhenFailedToEnlistEqualsCurrentTx(
                     failedToEnlist, this.currentTx));

               if (Tracer.isEnabled())
                  Tracer.exception(getPool() != null ? getPool().getName() : null, 
                                   getManagedConnectionPool(),
                                   TxConnectionListener.this, se);

               throw se;

            }
            else
            {
               SystemException e = new SystemException(error);
               e.initCause(enlistError);

               if (Tracer.isEnabled())
                  Tracer.exception(getPool() != null ? getPool().getName() : null, 
                                   getManagedConnectionPool(),
                                   TxConnectionListener.this, e);

               throw e;
            }
         }
         if (!enlisted)
         {
            String error = "Resource is not enlisted in transaction=" + currentTx;
            log.tracef("%s %s", error, TxConnectionListener.this);
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
         log.tracef("Enlisting resource %s", TxConnectionListener.this);
         try
         {
            XAResource resource = getXAResource();
            if (!currentTx.enlistResource(resource))
            {
               if (Tracer.isEnabled())
                  Tracer.enlistConnectionListener(getPool() != null ? getPool().getName() : null, 
                                                  getManagedConnectionPool(),
                                                  TxConnectionListener.this, currentTx.toString(), false,
                                                  !TxConnectionListener.this.isTrackByTx());

               if (recordEnlist)
               {
                  enlistError = failedToEnlist;
               }
               else
               {
                  enlistError = new Throwable("Failed to enlist");
               }
            }
            else
            {
               if (Tracer.isEnabled())
                  Tracer.enlistConnectionListener(getPool() != null ? getPool().getName() : null,
                                                  getManagedConnectionPool(),
                                                  TxConnectionListener.this, currentTx.toString(), true,
                                                  !TxConnectionListener.this.isTrackByTx());
            }
         }
         catch (Throwable t)
         {
            enlistError = t;

            if (Tracer.isEnabled())
               Tracer.enlistConnectionListener(getPool() != null ? getPool().getName() : null,
                                               getManagedConnectionPool(),
                                               TxConnectionListener.this, currentTx.toString(), false,
                                               !TxConnectionListener.this.isTrackByTx());
            }

         synchronized (this)
         {
            if (enlistError != null)
            {
               if (log.isTraceEnabled())
               {
                  log.trace("Failed to enlist resource " + TxConnectionListener.this, enlistError);
               }

               setTrackByTx(false);
               transactionSynchronization = null;

               return false;
            }

            enlisted = true;

            log.tracef("Enlisted resource %s", TxConnectionListener.this);

            return true;
         }
      }

      /**
       * {@inheritDoc}
       */
      public void beforeCompletion()
      {
         if (enlisted && !cancel)
         {
            try
            {
               if (this.equals(transactionSynchronization) && wasTrackByTx && doDelistResource)
               {
                  if (TxUtils.isUncommitted(currentTx))
                  {
                     if (TxUtils.isActive(currentTx))
                     {
                        log.tracef("delistResource(%s, TMSUCCESS)", TxConnectionListener.this.getXAResource());

                        currentTx.delistResource(TxConnectionListener.this.getXAResource(), XAResource.TMSUCCESS);

                        if (Tracer.isEnabled())
                           Tracer.delistConnectionListener(getPool() != null ? getPool().getName() : null,
                                                           getManagedConnectionPool(),
                                                           TxConnectionListener.this, currentTx.toString(),
                                                           true, false, false);
                     }
                     else
                     {
                        log.tracef("delistResource(%s, TMFAIL)", TxConnectionListener.this.getXAResource());

                        currentTx.delistResource(TxConnectionListener.this.getXAResource(), XAResource.TMFAIL);

                        if (Tracer.isEnabled())
                           Tracer.delistConnectionListener(getPool() != null ? getPool().getName() : null,
                                                           getManagedConnectionPool(),
                                                           TxConnectionListener.this, currentTx.toString(),
                                                           false, false, false);
                     }
                  }
                  else
                  {
                     if (log.isTraceEnabled())
                         log.tracef("Non-uncommitted transaction for %s (%s)", TxConnectionListener.this,
                                   currentTx != null ? TxUtils.getStatusAsString(currentTx.getStatus()) : "None");
                  }
               }
               else
               {
                  log.tracef("No delistResource for: %s", TxConnectionListener.this);
               }
            }
            catch (Throwable t)
            {
               log.beforeCompletionErrorOccured(TxConnectionListener.this, t);
            }
         }
         else
         {
            log.tracef("Unenlisted resource: %s", TxConnectionListener.this);
         }
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

         if (!cancel)
         {
            // Are we still in the original transaction?
            if (!this.equals(transactionSynchronization))
            {
               // If we are interleaving transactions we have nothing to do
               if (!wasTrackByTx)
               {
                  TxConnectionListener.this.setEnlisted(false);
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
            TxConnectionListener.this.setEnlisted(false);
            transactionSynchronization = null;

            // This is where we close when doing track by transaction
            if (wasTrackByTx)
            {
               log.tracef("afterCompletion(%d) isTrackByTx=%b for %s" 
                          , status, isTrackByTx(), TxConnectionListener.this);

               if (wasFreed(null))
               {
                  if (Tracer.isEnabled() && status == Status.STATUS_ROLLEDBACK)
                     Tracer.delistConnectionListener(getPool() != null ? getPool().getName() : null,
                                                     getManagedConnectionPool(),
                                                     TxConnectionListener.this, "", true, true, false);

                  getConnectionManager().returnManagedConnection(TxConnectionListener.this, false);
               }
               else
               {
                  if (tracking == null || tracking.booleanValue())
                  {
                     log.activeHandles(getPool() != null ? getPool().getName() : "Unknown", connectionHandles.size());

                     if (tracking != null && tracking.booleanValue())
                     {
                        Iterator<Map.Entry<Object, Exception>> it = connectionTraces.entrySet().iterator();
                        while (it.hasNext())
                        {
                           Map.Entry<Object, Exception> entry = it.next();
                           log.activeHandle(entry.getKey(), entry.getValue());
                        }

                        log.txConnectionListenerBoundary(new Exception());
                     }

                     if (Tracer.isEnabled())
                     {
                        for (Object c : connectionHandles)
                        {
                           Tracer.clearConnection(getPool() != null ? getPool().getName() : null,
                                                  getManagedConnectionPool(),
                                                  TxConnectionListener.this, c);
                        }
                     }

                     getConnectionManager().returnManagedConnection(TxConnectionListener.this, true);
                  }
                  else
                  {
                     if (log.isTraceEnabled())
                        log.tracef(new Exception("Connection across boundary"), "ConnectionListener=%s",
                                   TxConnectionListener.this);
                  }
               }
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
         buffer.append("TransactionSynchronization@").append(System.identityHashCode(this));
         buffer.append("{tx=").append(currentTx);
         buffer.append(" wasTrackByTx=").append(wasTrackByTx);
         buffer.append(" enlisted=").append(enlisted);
         buffer.append(" cancel=").append(cancel);
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
