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
 * 5. When an "entering object" notification is received from the
 * CachedConnectionInterceptor, all the connections for the current
 * object must be associated with a ManagedConnection.  if there is a
 * Transaction, the XAResource must be enlisted with it.
 *  Entry point: ConnectionCacheListener.reconnect(Collection conns) The Collection
 * is of ConnectionRecord objects.
 * written.
 *
 * 6. When a "leaving object" notification is received from the
 * CachedConnectionInterceptor, all the managedConnections for the
 * current object must have their XAResources delisted from the
 * current Transaction, if any, and cleanup called on each
 * ManagedConnection.
 * Entry point: ConnectionCacheListener.disconnect(Collection conns).
 * written.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class TxConnectionManagerImpl extends AbstractConnectionManager implements TxConnectionManager
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, TxConnectionManager.class.getName());

   /** Serial version uid */
   private static final long serialVersionUID = 1L;
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
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
   private boolean isSameRMOverride;
   
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
         setInterleaving(false);
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
   public boolean getIsSameRMOverride()
   {
      return isSameRMOverride;
   }
   
   /**
    * Set the IsSameRMOverride value.
    * @param v The value
    */
   public void setIsSameRMOverride(boolean v)
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
    * Gets time left.
    * @param errorRollback error rollback
    * @return time left
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
         if (tx != null && !TxUtils.isActive(tx))
         {
            throw new ResourceException(bundle.transactionNotActive(tx));  
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

      if (trace)
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
            cl.enlist();

            if (!isInterleaving())
            {
               cl.setTrackByTx(true);

               ManagedConnectionPool mcp = (ManagedConnectionPool)cl.getContext();
               Transaction tx = transactionManager.getTransaction();

               // The lock may need to be initialized if we are in the first lazy enlistment
               Lock lock = getLock();
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
    * @return The lock
    */
   private Lock getLock()
   {
      Lock result = null;

      if (transactionSynchronizationRegistry != null && transactionSynchronizationRegistry.getTransactionKey() != null)
      {
         result = (Lock)transactionSynchronizationRegistry.getResource(LockKey.INSTANCE);
         if (result == null)
         {
            result = initLock();
         }
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
         cl.enlist();
      }
      catch (Throwable t)
      {
         if (trace)
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
         if (trace)
            log.tracef("Disconnected isManagedConnectionFree=true cl=%s", cl);

         returnManagedConnection(cl, false);
      }
      else
      {
         if (trace)
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
   public ConnectionListener createConnectionListener(ManagedConnection mc, Object context)
      throws ResourceException
   {
      XAResource xaResource = null;
      
      if (localTransactions)
      {
         xaResource = txIntegration.createLocalXAResource(this);
    
         if (xaResourceTimeout != 0)
         {
            log.debug("XAResource transaction timeout cannot be set for local transactions: " + getJndiName());  
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

            if (trace)
               log.tracef("Generating XAResourceWrapper for TxConnectionManager (%s)", this);

            xaResource = txIntegration.createXAResourceWrapper(mc.getXAResource(), padXid, 
                                                               isSameRMOverride, 
                                                               eisProductName, eisProductVersion,
                                                               getJndiName());
         }
         else
         {
            if (trace)
               log.tracef("Not wrapping XAResource.");

            xaResource = mc.getXAResource();
         }
                                
         if (xaResourceTimeout != 0)
         {
            try
            {
               if (!xaResource.setTransactionTimeout(xaResourceTimeout))
                  log.debug("XAResource does not support transaction timeout configuration: " + getJndiName());
            }
            catch (XAException e)
            {
               throw new ResourceException(bundle.unableSetXAResourceTransactionTimeout(getJndiName()), e);
            }
         }
      }

      ConnectionListener cli = new TxConnectionListener(this, mc, getPool(), context, getFlushStrategy(), xaResource);
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
