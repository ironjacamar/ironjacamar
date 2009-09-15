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

import org.jboss.jca.common.api.JBossResourceException;
import org.jboss.jca.core.connectionmanager.AbstractConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionRecord;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.TxConnectionListener;
import org.jboss.jca.core.connectionmanager.xa.LocalXAResource;
import org.jboss.jca.core.connectionmanager.xa.XAResourceWrapperImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.jboss.tm.TransactionTimeoutConfiguration;
import org.jboss.tm.TxUtils;
import org.jboss.util.NestedRuntimeException;

/**
 * The TxConnectionManager is a JBoss ConnectionManager
 * implementation for jca adapters implementing LocalTransaction and XAResource support.
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
 * @version $Revision: 77961 $
 */
public class TxConnectionManager extends AbstractConnectionManager
{
   /**Transaction manager instance*/
   private TransactionManager transactionManager;

   /**Interleaving or not*/
   private boolean interleaving;

   /**Local tx or not*/
   private boolean localTransactions;
   
   /**XA resource timeout*/
   private int xaResourceTimeout = 0;
   
   /**Xid pad*/
   private boolean padXid;
   
   /**XA resource wrapped or not*/
   private boolean wrapXAResource;

   /**Same RM override*/
   private Boolean isSameRMOverrideValue;
   
   /**Log trace*/
   private boolean trace = getLog().isTraceEnabled();
      
   /**
    * Default managed TxConnectionManager constructor for mbean instances.
    */
   public TxConnectionManager()
   {
   }

   /**
    * Gets transaction manager instance.
    * @return transaction manager
    */
   public TransactionManager getTransactionManager()
   {
      return this.transactionManager;
   }

   /**
    * Sets transaction manager.
    * @param tm transaction manager
    */
   public void setTransactionManager(TransactionManager tm)
   {
      this.transactionManager = tm;
   }

   /**
    * Gets track connection by tx.
    * @return track connection by tx
    */
   @Deprecated
   public boolean isTrackConnectionByTx()
   {
      getLog().warn("isTrackConnectionByTx() is deprecated in favor of isInterleaving()");
      return !isInterleaving();
   }

   /**
    * Set track connection by tx.
    * @param trackConnectionByTx track connection by tx
    */
   @Deprecated
   public void setTrackConnectionByTx(boolean trackConnectionByTx)
   {
      getLog().warn("setTrackConnectionByTx(boolean value) is deprecated in favor of setInterleaving(boolean value)");
      setInterleaving(!trackConnectionByTx);
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
      this.interleaving = value;
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
    * Sets local transaction or not.
    * @param localTransactions local transaction flag
    */
   public void setLocalTransactions(boolean localTransactions)
   {
      this.localTransactions = localTransactions;
      if (localTransactions)
         setInterleaving(false);
   }

   /**
    * Gets XA resource transaction time out.
    * @return xa resource transaction timeout
    */
   public int getXAResourceTransactionTimeout()
   {
      return xaResourceTimeout;
   }
   
   /**
    * Sets XA resource transaction timeout.
    * @param timeout xa resource transaction timeout
    */
   public void setXAResourceTransactionTimeout(int timeout)
   {
      this.xaResourceTimeout = timeout;
   }
   
   /**
    * Get the IsSameRMOverrideValue value.
    * 
    * @return the IsSameRMOverrideValue value.
    */
   public Boolean getIsSameRMOverrideValue()
   {
      return isSameRMOverrideValue;
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
    * Sets use xa wrapper.
    * @param useXAWrapper use xa wrapper
    */
   public void setWrapXAResource(boolean useXAWrapper)
   {
      this.wrapXAResource = useXAWrapper;
      
   }
   
   /**
    * Gets pad.
    * @return pad 
    */
   public boolean getPadXid()
   {
      return this.padXid;
      
   }
   
   /**
    * Sets pad.
    * @param padXid pad
    */
   public void setPadXid(boolean padXid)
   {
      this.padXid = padXid;
   }
   /**
    * Set the IsSameRMOverrideValue value.
    * 
    * @param isSameRMOverrideValue The new IsSameRMOverrideValue value.
    */
   public void setIsSameRMOverrideValue(Boolean isSameRMOverrideValue)
   {
      this.isSameRMOverrideValue = isSameRMOverrideValue;
   }
   
   /**
    * Gets time left.
    * @param errorRollback error rollback
    * @return time left
    * @throws RollbackException if exception
    */
   public long getTimeLeftBeforeTransactionTimeout(boolean errorRollback) throws RollbackException
   {
      if (this.transactionManager == null)
      {
         throw new IllegalStateException("No transaction manager: " + getCachedConnectionManager());  
      }

      if (this.transactionManager instanceof TransactionTimeoutConfiguration)
      {
         return ((TransactionTimeoutConfiguration) this.transactionManager).
            getTimeLeftBeforeTransactionTimeout(errorRollback);  
      }
      
      return -1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void checkTransactionActive() throws RollbackException, SystemException
   {
      if (this.transactionManager == null)
      {
         throw new IllegalStateException("No transaction manager: " + getCachedConnectionManager());  
      }
      
      Transaction tx = this.transactionManager.getTransaction();
      if (tx != null)
      {
         int status = tx.getStatus();
         // Only allow states that will actually succeed
         if (status != Status.STATUS_ACTIVE && status != Status.STATUS_PREPARING && 
               status != Status.STATUS_PREPARED && status != Status.STATUS_COMMITTING)
         {
            throw new RollbackException("Transaction " + tx + " cannot proceed " + TxUtils.getStatusAsString(status));  
         }
      }
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
         Transaction tx = this.transactionManager.getTransaction();
         if (tx != null && !TxUtils.isActive(tx))
         {
            throw new ResourceException("Transaction is not active: tx=" + tx);  
         }
         
         if (!interleaving)
         {
            trackByTransaction = tx;  
         }
      }
      catch (Throwable t)
      {
         JBossResourceException.rethrowAsResourceException("Error checking for a transaction.", t);
      }

      if (this.trace)
      {
         getLog().trace("getManagedConnection interleaving=" + interleaving + " tx=" + trackByTransaction);  
      }
      
      return super.getManagedConnection(trackByTransaction, subject, cri);
   }

   /**
    * {@inheritDoc}
    */
   public void transactionStarted(Collection<ConnectionRecord> crs) throws SystemException
   {
      Set<ConnectionListener> cls = new HashSet<ConnectionListener>();
      for (Iterator<ConnectionRecord> i = crs.iterator(); i.hasNext(); )
      {
         ConnectionRecord cr = i.next();
         ConnectionListener cl = cr.getConnectionListener();
         if (!cls.contains(cl))
         {
            cls.add(cl);
            cl.enlist();
         }
      }
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
         {
            getLog().trace("Could not enlist in transaction on entering meta-aware object! " + cl, t);  
         }
         throw new JBossResourceException("Could not enlist in transaction on entering meta-aware object!", t);
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
            getLog().trace("Disconnected isManagedConnectionFree=true" + " cl=" + cl);
         returnManagedConnection(cl, false);
      }
      else if (trace)
      {
         getLog().trace("Disconnected isManagedConnectionFree=false" + " cl=" + cl);
      }

      // Rethrow the error
      if (throwable != null)
      {
         JBossResourceException.rethrowAsResourceException(
               "Could not delist resource, probably a transaction rollback? ", throwable);  
      }      
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener createConnectionListener(ManagedConnection mc, Object context)
      throws ResourceException
   {
      XAResource xaResource = null;
      
      if (this.localTransactions)
      {
         xaResource = new LocalXAResource(this);
    
         if (xaResourceTimeout != 0)
         {
            getLog().debug("XAResource transaction timeout cannot be set for local transactions: " + getJndiName());  
         }
      }
      
      else
      {
         
         if (this.wrapXAResource)
         {
            String eisProductName = null;
            String eisProductVersion = null;

            try
            {
               eisProductName = mc.getMetaData().getEISProductName();
               eisProductVersion = mc.getMetaData().getEISProductVersion();
            }
            catch (ResourceException re)
            {
               // Ignore
            }

            getLog().trace("Generating XAResourceWrapper for TxConnectionManager" + this);
            xaResource = new XAResourceWrapperImpl(mc.getXAResource(), padXid, 
                  isSameRMOverrideValue, eisProductName, eisProductVersion);
         }
         
         else
         {
            getLog().trace("Not wrapping XAResource.");
            xaResource = mc.getXAResource();
         }
                                
         if (xaResourceTimeout != 0)
         {
            try
            {
               if (!xaResource.setTransactionTimeout(xaResourceTimeout))
                  getLog().debug("XAResource does not support transaction timeout configuration: " + getJndiName());
            }
            catch (XAException e)
            {
               throw new JBossResourceException("Unable to set XAResource transaction timeout: " + getJndiName(), e);
            }
         }
      }

      ConnectionListener cli = new TxConnectionListener(this, mc, getPoolingStrategy(), context, xaResource);
      mc.addConnectionEventListener(cli);
      return cli;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isTransactional()
   {
      return !TxUtils.isCompleted(this.transactionManager);
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
      throw new NestedRuntimeException(context + " tx=" + tx + " got unexpected error ", t);
   }
   
}
