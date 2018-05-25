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
package org.jboss.jca.core.connectionmanager.tx;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.connectionmanager.AbstractConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionRecord;
import org.jboss.jca.core.connectionmanager.TxConnectionManager;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.TxConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.transaction.LockKey;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.spi.transaction.TransactionTimeoutConfiguration;
import org.jboss.jca.core.spi.transaction.TxUtils;
import org.jboss.jca.core.spi.transaction.XAResourceStatistics;
import org.jboss.jca.core.tx.jbossts.XAResourceWrapperStatImpl;
import org.wildfly.transaction.client.AbstractTransaction;
import org.wildfly.transaction.client.ContextTransactionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.DissociatableManagedConnection;
import javax.resource.spi.LazyEnlistableManagedConnection;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * The TxConnectionManager is a JBoss ConnectionManager
 * implementation for JCA adapters implementing LocalTransaction and XAResource support.
 * 
 * It implements a ConnectionEventListener that implements XAResource to
 * manage transactions through the Transaction Manager. To assure that all
 * work in a local transaction occurs over the same ManagedConnection, it
 * includes a xid to ManagedConnection map.  When a Connection is requested
 * or a transaction started with a connection handle in use, it checks to
 * see if a ManagedConnection already exists enrolled in the global
 * transaction and uses it if found. Otherwise a free ManagedConnection
 * has its LocalTransaction started and is used.  From the
 * BaseConnectionManager2, it includes functionality to obtain managed
 * connections from
 * a ManagedConnectionPool mbean, find the Subject from a SubjectSecurityDomain,
 * and interact with the CachedConnectionManager for connections held over
 * transaction and method boundaries.  Important mbean references are to a
 * ManagedConnectionPool supplier (typically a JBossManagedConnectionPool), and a
 * RARDeployment representing the ManagedConnectionFactory.
 *
 * This connection manager has to perform the following operations:
 *
 * 1. When an application component requests a new ConnectionHandle,
 *    it must find a ManagedConnection, and make sure a
 *    ConnectionEventListener is registered. It must inform the
 *    CachedConnectionManager that a connection handle has been given
 *    out. It needs to count the number of handles for each
 *    ManagedConnection.  If there is a current transaction, it must
 *    enlist the ManagedConnection's LocalTransaction in the transaction
 *    using the ConnectionEventListeners XAResource XAResource implementation.
 * Entry point: ConnectionManager.allocateConnection.
 * written.
 *
 * 2. When a ConnectionClosed event is received from the
 *    ConnectionEventListener, it must reduce the handle count.  If
 *    the handle count is zero, the XAResource should be delisted from
 *    the Transaction, if any. The CachedConnectionManager must be
 *    notified that the connection is closed.
 * Entry point: ConnectionEventListener.ConnectionClosed.
 * written
 *
 *3. When a transaction begun notification is received from the
 * UserTransaction (via the CachedConnectionManager, all
 * managedConnections associated with the current object must be
 * enlisted in the transaction.
 *  Entry point: (from
 * CachedConnectionManager)
 * ConnectionCacheListener.transactionStarted(Transaction,
 * Collection). The collection is of ConnectionRecord objects.
 * written.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="wprice@redhat.com">Weston Price</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TxConnectionManagerImpl extends AbstractConnectionManager implements TxConnectionManager
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, TxConnectionManager.class.getName());

   /** Serial version uid */
   private static final long serialVersionUID = 1L;
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /** Allow marked for rollback */
   private static boolean allowMarkedForRollback = false;
   
   /** Allow marked for rollback fast fail */
   private static boolean allowMarkedForRollbackFastFail = false;
   
   /** Transaction manager instance */
   private transient TransactionManager transactionManager;

   /** Transaction synchronization registry */
   private transient TransactionSynchronizationRegistry transactionSynchronizationRegistry;

   /** Transaction integration */
   private TransactionIntegration txIntegration;

   /** Interleaving or not */
   private boolean interleaving;

   /** Local tx or not */
   private boolean localTransactions;
   
   /** XA resource timeout */
   private int xaResourceTimeout = 0;
   
   /** Xid pad */
   private boolean padXid;
   
   /** XA resource wrapped or not */
   private boolean wrapXAResource = true;

   /** Same RM override */
   private Boolean isSameRMOverride;

   static
   {
      String value = SecurityActions.getSystemProperty("ironjacamar.allow_marked_for_rollback");
      if (value != null && !value.trim().equals(""))
      {
         try
         {
            allowMarkedForRollback = Boolean.valueOf(value);
         }
         catch (Throwable t)
         {
            // Assume enable
            allowMarkedForRollback = true;
         }
      }

      value = SecurityActions.getSystemProperty("ironjacamar.allow_marked_for_rollback_fast_fail");
      if (value != null && !value.trim().equals(""))
      {
         try
         {
            allowMarkedForRollbackFastFail = Boolean.valueOf(value);
         }
         catch (Throwable t)
         {
            // Assume enable
            allowMarkedForRollbackFastFail = true;
         }
      }
   }
   
   /**
    * Constructor
    * @param txIntegration The transaction integration layer
    * @param localTransactions Is local transactions enabled
    */
   public TxConnectionManagerImpl(final TransactionIntegration txIntegration,
                                  final boolean localTransactions)
   {
      if (txIntegration == null)
         throw new IllegalArgumentException("TransactionIntegration is null");

      this.transactionManager = txIntegration.getTransactionManager();
      this.transactionSynchronizationRegistry = txIntegration.getTransactionSynchronizationRegistry();
      this.txIntegration = txIntegration;

      setLocalTransactions(localTransactions);
   }

   /**
    * Get the logger.
    * @return The value
    */
   protected CoreLogger getLogger()
   {
      return log;
   }

   /**
    * Get the transaction integration instance
    * @return The transaction integration
    */
   public TransactionIntegration getTransactionIntegration()
   {
      return txIntegration;
   }

   /**
    * Gets interleaving flag.
    * @return interleaving flag
    */
   public boolean isInterleaving()
   {
      return interleaving;
   }
   
   /**
    * Sets interleaving flag.
    * @param value interleaving
    */
   public void setInterleaving(boolean value)
   {
      interleaving = value;

      if (interleaving)
         setSharable(false);
   }
   
   /**
    * Returns local tx or not.
    * @return local tx or not
    */
   public boolean isLocalTransactions()
   {
      return localTransactions;
   }

   /**
    * Set the local transaction
    * @param v The value
    */
   void setLocalTransactions(boolean v)
   {
      localTransactions = v;

      if (v)
      {
         setInterleaving(false);
      }
   }

   /**
    * Gets XA resource transaction time out.
    * @return xa resource transaction timeout
    */
   public int getXAResourceTimeout()
   {
      return xaResourceTimeout;
   }
   
   /**
    * Sets XA resource transaction timeout.
    * @param timeout xa resource transaction timeout
    */
   public void setXAResourceTimeout(int timeout)
   {
      xaResourceTimeout = timeout;
   }
   
   /**
    * Get the IsSameRMOverride value
    * @return The value
    */
   public Boolean getIsSameRMOverride()
   {
      return isSameRMOverride;
   }
   
   /**
    * Set the IsSameRMOverride value.
    * @param v The value
    */
   public void setIsSameRMOverride(Boolean v)
   {
      isSameRMOverride = v;
   }

   /**
    * Returns true if wrap xa resource.
    * @return true if wrap xa resource
    */
   public boolean getWrapXAResource()
   {      
      return wrapXAResource;      
   }
   
   /**
    * Set if the XAResource should be wrapped
    * @param v The value
    */
   public void setWrapXAResource(boolean v)
   {
      wrapXAResource = v;
   }
   
   /**
    * Get PadXis status
    * @return The value
    */
   public boolean getPadXid()
   {
      return padXid;
   }
   
   /**
    * Set if the Xid should be padded
    * @param v The value
    */
   public void setPadXid(boolean v)
   {
      padXid = v;
   }
   
   /**
    * {@inheritDoc}
    */
   public boolean isAllowMarkedForRollback()
   {
      return allowMarkedForRollback;
   }

   /**
    * Get the XAResource statistics instance, if supported
    * @return The value
    */
   private XAResourceStatistics getXAResourceStatistics()
   {
      if (getPool() != null && getPool().getStatistics() != null &&
          getPool().getStatistics() instanceof XAResourceStatistics)
      {
         return (XAResourceStatistics)getPool().getStatistics();
      }

      return null;
   }

   /**
    * Gets time left.
    * @param errorRollback error rollback
    * @return time left in ms
    * @throws RollbackException if exception
    */
   public long getTimeLeftBeforeTransactionTimeout(boolean errorRollback) throws RollbackException
   {
      if (transactionManager == null)
      {
         throw new IllegalStateException("No transaction manager: " + getCachedConnectionManager());  
      }

      if (transactionManager instanceof TransactionTimeoutConfiguration)
      {
         return ((TransactionTimeoutConfiguration)transactionManager).
            getTimeLeftBeforeTransactionTimeout(errorRollback);  
      }

      if (transactionManager instanceof ContextTransactionManager)
      {
         AbstractTransaction transaction = ((ContextTransactionManager) transactionManager).getTransaction();
         if (transaction != null) {
            return transaction.getEstimatedRemainingTime() * 1000;
         }
      }

      return -1;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener getManagedConnection(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      Transaction trackByTransaction = null;
      try
      {
         Transaction tx = transactionManager.getTransaction();
         if (tx != null)
         {
            if (!allowMarkedForRollback)
            {
               if (!TxUtils.isActive(tx))
                  if (!getPool().hasConnection(subject, cri) || allowMarkedForRollbackFastFail)
                     throw new ResourceException(bundle.transactionNotActive(tx));  
            }
            else
            {
               if (!TxUtils.isUncommitted(tx))
                  throw new ResourceException(bundle.transactionNotActive(tx));  
            }
         }
         
         if (!interleaving)
         {
            trackByTransaction = tx;
         }
      }
      catch (Throwable t)
      {
         throw new ResourceException(bundle.errorCheckingForTransaction(), t);
      }

      log.tracef("getManagedConnection interleaving=%s , tx=%s", interleaving, trackByTransaction);  
      
      return super.getManagedConnection(trackByTransaction, subject, cri);
   }

   /**
    * {@inheritDoc}
    */
   public void transactionStarted(Collection<ConnectionRecord> crs) throws SystemException
   {
      Set<ConnectionListener> cls = new HashSet<ConnectionListener>(crs.size());
      for (ConnectionRecord cr : crs)
      {
         ConnectionListener cl = cr.getConnectionListener();
         if (!cls.contains(cl))
         {
            cls.add(cl);
            if (shouldEnlist(cl.getManagedConnection()))
            {
               if (!isInterleaving())
                  cl.setTrackByTx(true);

               cl.enlist();
            }

            if (!isInterleaving())
            {
               cl.setTrackByTx(true);

               ManagedConnectionPool mcp = cl.getManagedConnectionPool();
               Transaction tx = transactionManager.getTransaction();

               // The lock may need to be initialized if we are in the first lazy enlistment
               Lock lock = getLock();

               if (lock == null)
                  rethrowAsSystemException("Unable to obtain lock with JCA lazy enlistment scenario", tx,
                                           new SystemException(
                                                            "Unable to obtain lock with JCA lazy enlistment scenario"));

               try
               {
                  lock.lockInterruptibly();
               }
               catch (Throwable t)
               {
                  rethrowAsSystemException("Unable to begin transaction with JCA lazy enlistment scenario", 
                                           tx, t);
               }

               try
               {
                  transactionSynchronizationRegistry.putResource(mcp, cl);
               }
               catch (Throwable t)
               {
                  rethrowAsSystemException("Unable to register JCA lazy enlistment scenario", 
                                           tx, new SystemException("Unable to register JCA lazy enlistment scenario"));
               }
               finally
               {
                  lock.unlock();
               }
            }
         }
      }
   }

   /**
    * Init lock
    * @return The lock
    */
   private synchronized Lock initLock()
   {
      if (transactionSynchronizationRegistry != null && transactionSynchronizationRegistry.getTransactionKey() != null)
      {
         if (transactionSynchronizationRegistry.getResource(LockKey.INSTANCE) == null)
         {
            Lock lock = new ReentrantLock(true);
            transactionSynchronizationRegistry.putResource(LockKey.INSTANCE, lock);
            return lock;
         }
         else
         {
            return (Lock)transactionSynchronizationRegistry.getResource(LockKey.INSTANCE);
         }
      }

      return null;
   }

   /**
    * Get lock
    * @return The lock; <code>null</code> if TX isn't active
    */
   private Lock getLock()
   {
      Lock result = null;
      try
      {
         if (transactionSynchronizationRegistry != null &&
             transactionSynchronizationRegistry.getTransactionKey() != null)
         {
            result = (Lock)transactionSynchronizationRegistry.getResource(LockKey.INSTANCE);
            if (result == null)
            {
               result = initLock();
            }
         }
      }
      catch (Throwable t)
      {
         // Catch all exceptions
      }

      return result;
   }

   /**
    * {@inheritDoc}
    */
   protected void managedConnectionReconnected(ConnectionListener cl) throws ResourceException
   {
      try
      {
         if (shouldEnlist(cl.getManagedConnection()))
            cl.enlist();
      }
      catch (Throwable t)
      {
         if (log.isTraceEnabled())
            log.trace("Could not enlist in transaction on entering meta-aware object! " + cl, t);  

         throw new ResourceException(bundle.notEnlistInTransactionOnEnteringMetaAwareObject(), t);
      }
   }

   /**
    * {@inheritDoc}
    */
   protected void managedConnectionDisconnected(ConnectionListener cl) throws ResourceException
   {
      Throwable throwable = null;
      try
      {
         cl.delist();
      }
      catch (Throwable t)
      {
         throwable = t;
      }

      //if there are no more handles and tx is complete, we can return to pool.
      if (cl.isManagedConnectionFree())
      {
         log.tracef("Disconnected isManagedConnectionFree=true cl=%s", cl);

         returnManagedConnection(cl, false);
      }
      else
      {
         log.tracef("Disconnected isManagedConnectionFree=false cl=%s", cl);
      }

      // Rethrow the error
      if (throwable != null)
      {
         throw new ResourceException(bundle.couldNotDelistResourceThenTransactionRollback(), throwable);  
      }      
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener createConnectionListener(ManagedConnection mc, ManagedConnectionPool mcp)
      throws ResourceException
   {
      XAResource xaResource = null;
      int explicitXAResourceTimeout = 0;
      
      if (localTransactions)
      {
         String eisProductName = null;
         String eisProductVersion = null;

         try
         {
            if (mc.getMetaData() != null)
            {
               eisProductName = mc.getMetaData().getEISProductName();
               eisProductVersion = mc.getMetaData().getEISProductVersion();
            }
         }
         catch (ResourceException re)
         {
            // Ignore
         }

         if (eisProductName == null)
            eisProductName = getJndiName();

         if (eisProductVersion == null)
            eisProductVersion = getJndiName();

         if (isConnectable())
         {
            if (mc instanceof org.jboss.jca.core.spi.transaction.ConnectableResource)
            {
               org.jboss.jca.core.spi.transaction.ConnectableResource cr =
                  (org.jboss.jca.core.spi.transaction.ConnectableResource)mc;

               xaResource = txIntegration.createConnectableLocalXAResource(this, eisProductName, eisProductVersion,
                                                                           getJndiName(), cr,
                                                                           getXAResourceStatistics());
            }
            else if (txIntegration.isConnectableResource(mc))
            {
               xaResource = txIntegration.createConnectableLocalXAResource(this, eisProductName, eisProductVersion,
                                                                           getJndiName(), mc,
                                                                           getXAResourceStatistics());
            }
         }

         if (xaResource == null)
            xaResource = txIntegration.createLocalXAResource(this, eisProductName, eisProductVersion, getJndiName(),
                                                             getXAResourceStatistics());
    
         if (xaResourceTimeout != 0)
         {
            log.debugf("XAResource transaction timeout cannot be set for local transactions: %s", getJndiName());
         }
      }      
      else
      {         
         if (wrapXAResource)
         {
            String eisProductName = null;
            String eisProductVersion = null;

            try
            {
               if (mc.getMetaData() != null)
               {
                  eisProductName = mc.getMetaData().getEISProductName();
                  eisProductVersion = mc.getMetaData().getEISProductVersion();
               }
            }
            catch (ResourceException re)
            {
               // Ignore
            }

            if (eisProductName == null)
               eisProductName = getJndiName();

            if (eisProductVersion == null)
               eisProductVersion = getJndiName();

            if (isConnectable())
            {
               if (mc instanceof org.jboss.jca.core.spi.transaction.ConnectableResource)
               {
                  org.jboss.jca.core.spi.transaction.ConnectableResource cr =
                     (org.jboss.jca.core.spi.transaction.ConnectableResource)mc;

                  xaResource = txIntegration.createConnectableXAResourceWrapper(mc.getXAResource(), padXid, 
                                                                                isSameRMOverride, 
                                                                                eisProductName, eisProductVersion,
                                                                                getJndiName(),
                                                                                cr,
                                                                                getXAResourceStatistics());
               }
               else if (txIntegration.isConnectableResource(mc))
               {
                  xaResource = txIntegration.createConnectableXAResourceWrapper(mc.getXAResource(), padXid, 
                                                                                isSameRMOverride, 
                                                                                eisProductName, eisProductVersion,
                                                                                getJndiName(),
                                                                                mc,
                                                                                getXAResourceStatistics());
               }
            }

            if (xaResource == null)
            {
               XAResource xar = mc.getXAResource();

               if (!(xar instanceof org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper) ||
                       (getXAResourceStatistics() != null &&
                               getXAResourceStatistics().isEnabled() &&
                               !(xar instanceof XAResourceWrapperStatImpl)))
               {
                  if (!(xar instanceof org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper))
                  {
                     xaResource = txIntegration
                           .createXAResourceWrapper(xar, padXid, isSameRMOverride, eisProductName, eisProductVersion,
                                 getJndiName(), txIntegration.isFirstResource(mc), getXAResourceStatistics());
                  }
                  else
                  {
                     xaResource = txIntegration
                           .createXAResourceWrapper(xar, padXid, isSameRMOverride, eisProductName, eisProductVersion,
                                 ((org.jboss.jca.core.spi.transaction.xa.XAResourceWrapper) xar).getJndiName(),
                                 txIntegration.isFirstResource(mc), getXAResourceStatistics());
                  }
               }
               else
               {
                  xaResource = xar;
               }
            }

            log.tracef("Generating XAResourceWrapper for TxConnectionManager (%s)", this);
         }
         else
         {
            log.trace("Not wrapping XAResource.");

            xaResource = mc.getXAResource();
         }
                                
         if (xaResourceTimeout != 0)
         {
            try
            {
               if (xaResource.setTransactionTimeout(xaResourceTimeout))
               {
                  explicitXAResourceTimeout = xaResourceTimeout;
               }
               else
               {
                  log.debugf("XAResource does not support transaction timeout configuration: %s", getJndiName());
               }
            }
            catch (XAException e)
            {
               throw new ResourceException(bundle.unableSetXAResourceTransactionTimeout(getJndiName()), e);
            }
         }
      }

      ConnectionListener cli = new TxConnectionListener(this, mc, getPool(), mcp,
              getFlushStrategy(), getTracking(),
              getEnlistmentTrace() == null ? null : getEnlistmentTrace().getEnlistmentTrace(),
              xaResource, explicitXAResourceTimeout);
      mc.addConnectionEventListener(cli);
      return cli;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isTransactional()
   {
      try
      {
         return !TxUtils.isCompleted(transactionManager.getTransaction());
      }
      catch (SystemException se)
      {
         throw new RuntimeException("Error during isTransactional()", se);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public int getTransactionTimeout() throws SystemException
   {
      throw new RuntimeException("NYI: getTransactionTimeout()");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void lazyEnlist(ManagedConnection mc) throws ResourceException
   {
      if (!isEnlistment())
         throw new ResourceException(bundle.enlistmentNotEnabled());

      if (mc == null || !(mc instanceof LazyEnlistableManagedConnection))
         throw new ResourceException(bundle.managedConnectionNotLazyEnlistable(mc));

      ConnectionListener cl = getPool().findConnectionListener(mc);
      if (cl != null)
      {
         if (cl.isEnlisted())
            throw new ResourceException(bundle.connectionListenerAlreadyEnlisted(cl));

         try
         {
            if (!isInterleaving())
            {
               Lock lock = getLock();

               if (lock == null)
               {
                  if (!isTransactional())
                  {
                     log.tracef("No transaction, no need to lazy enlist: %s", this);
                     return;
                  }
                  if (cl != null)
                  {
                     log.tracef("Killing connection tracked by transaction=%s", cl);

                     getPool().returnConnection(cl, true);
                  }

                  throw new ResourceException(bundle.unableObtainLock());
               }

               try
               {
                  lock.lockInterruptibly();
               }
               catch (InterruptedException ie)
               {
                  Thread.interrupted();

                  if (cl != null)
                  {
                     log.tracef("Killing connection tracked by transaction=%s", cl);

                     getPool().returnConnection(cl, true);
                  }

                  throw new ResourceException(bundle.unableObtainLock(), ie);
               }
               try
               {
                  ConnectionListener existing =
                     (ConnectionListener)transactionSynchronizationRegistry.getResource(cl.getManagedConnectionPool());

                  if (existing == null)
                  {
                     // We are the first ManagedConnection to enlist in this transaction
                     log.tracef("New connection tracked by transaction=%s", cl);

                     // We need to set track-by-transaction before we enlist
                     cl.setTrackByTx(true);
                     cl.enlist();

                     transactionSynchronizationRegistry.putResource(cl.getManagedConnectionPool(), cl);
                  }
                  else
                  {
                     log.tracef("Already an enlisted connection in the pool tracked by transaction=%s (new=%s)",
                                   existing, cl);

                     if (cl.supportsLazyAssociation())
                     {
                        // Dissociate if possible, as the reconnect will pick up the track-by-transaction cl
                        // and we can return this cl to the pool
                        DissociatableManagedConnection dmc = (DissociatableManagedConnection)cl.getManagedConnection();
                        dmc.dissociateConnections();

                        getPool().returnConnection(cl, false);
                     }
                     else
                     {
                        if (isLocalTransactions())
                           log.multipleLocalTransactionConnectionListenerEnlisted(getPool().getName(), cl);

                        // Ok, lets enlist and roll the dice
                        cl.setTrackByTx(true);
                        cl.enlist();
                     }
                  }
               }
               catch (Throwable t)
               {
                  if (cl != null)
                  {
                     log.tracef("Killing connection tracked by transaction=%s", cl);

                     getPool().returnConnection(cl, true);
                  }

                  throw new ResourceException(bundle.unableGetConnectionListener(), t);
               }
               finally
               {
                  lock.unlock();
               }
            }
            else
            {
               // We always enlist interleaved connection listeners
               cl.enlist();
            }
         }
         catch (Throwable t)
         {
            throw new ResourceException(bundle.errorDuringEnlistment(), t);
         }
      }
      else
      {
         throw new ResourceException(bundle.unableToFindConnectionListener());
      }
   }

   /**
    * Should the managed connection be enlisted
    * @param mc The managed connection
    * @return True if enlist, otherwise false
    */
   private boolean shouldEnlist(ManagedConnection mc)
   {
      if (isEnlistment() && mc instanceof LazyEnlistableManagedConnection)
         return false;

      return true;
   }

   /**
    * RethrowAsSystemException.
    * @param context context
    * @param tx transaction
    * @param t throwable
    * @throws SystemException system exception
    */
   public static void rethrowAsSystemException(String context, Transaction tx, Throwable t)
      throws SystemException
   {
      if (t instanceof SystemException)
         throw (SystemException) t;

      if (t instanceof RuntimeException)
         throw (RuntimeException) t;

      if (t instanceof Error)
         throw (Error) t;

      if (t instanceof RollbackException)
         throw new IllegalStateException(context + " tx=" + tx + " marked for rollback.");

      throw new RuntimeException(context + " tx=" + tx + " got unexpected error ", t);
   }


   private void writeObject(ObjectOutputStream out)
      throws IOException
   {
   }


   private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
   }
}
