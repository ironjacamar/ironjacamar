/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.connectionmanager.listener;

import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.TransactionalConnectionManager;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;
import org.ironjacamar.core.spi.transaction.ConnectableResource;
import org.ironjacamar.core.spi.transaction.TransactionIntegration;
import org.ironjacamar.core.spi.transaction.TxUtils;
import org.ironjacamar.core.spi.transaction.local.LocalXAResource;
import org.ironjacamar.core.tracer.Tracer;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROY;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROYED;

import java.util.Iterator;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * An abstract transactional connection listener, which is enlisted on the transaction boundary
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractTransactionalConnectionListener extends AbstractConnectionListener
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           AbstractTransactionalConnectionListener.class.getName());

   /** Transaction synchronization instance */
   protected TransactionSynchronization transactionSynchronization;

   /** Enlisted flag */
   protected boolean enlisted;
   
   /** XAResource instance */
   protected XAResource xaResource;

   /** XAResource timeout */
   protected int xaResourceTimeout;
   
   /** Whether there is a local transaction */
   protected boolean localTransaction;

   /**
    * Constructor
    * @param cm The connection manager
    * @param mc The managed connection
    * @param credential The credential
    * @param xaResource The associated XAResource
    * @param xaResourceTimeout The timeout for the XAResource instance
    * @param mcp The ManagedConnectionPool
    * @param flushStrategy The FlushStrategy
    */
   public AbstractTransactionalConnectionListener(ConnectionManager cm, ManagedConnection mc, Credential credential,
                                                  XAResource xaResource, int xaResourceTimeout,
                                                   ManagedConnectionPool mcp, FlushStrategy flushStrategy)

   {
      super(cm, mc, credential, mcp, flushStrategy);

      this.transactionSynchronization = null;
      this.enlisted = false;
      this.xaResource = xaResource;
      this.xaResourceTimeout = xaResourceTimeout;
      this.localTransaction = false;

      if (xaResource instanceof LocalXAResource)
      {
         ((LocalXAResource)xaResource).setConnectionManager(cm);
         ((LocalXAResource)xaResource).setConnectionListener(this);
      }
      if (xaResource instanceof ConnectableResource)
      {
         ((ConnectableResource)xaResource).setConnectableResourceListener(this);
      }

      resetXAResourceTimeout();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isEnlisted()
   {
      return enlisted;
   }

   /**
    * {@inheritDoc}
    */
   public void enlist() throws ResourceException
   {
      if (isEnlisted() || getState() == DESTROY || getState() == DESTROYED)
         return;

      log.tracef("Enlisting: %s", this);
      
      try
      {
         TransactionalConnectionManager txCM = (TransactionalConnectionManager)cm;
         Transaction tx = txCM.getTransactionIntegration().getTransactionManager().getTransaction();
      
         transactionSynchronization = createTransactionSynchronization();
         transactionSynchronization.init(tx);
         transactionSynchronization.enlist();

         txCM.getTransactionIntegration().getTransactionSynchronizationRegistry().
            registerInterposedSynchronization(transactionSynchronization);

         enlisted = true;

         log.tracef("Enlisted: %s", this);
      }
      catch (ResourceException re)
      {
         throw re;
      }
      catch (Exception e)
      {
         throw new ResourceException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void delist() throws ResourceException
   {
      log.tracef("Delisting: %s", this);
      
      try
      {
         TransactionalConnectionManager txCM = (TransactionalConnectionManager)cm;
         TransactionManager tm = txCM.getTransactionIntegration().getTransactionManager();
         int status = tm.getStatus();

         if (status != Status.STATUS_NO_TRANSACTION && enlisted)
         {
            Transaction tx = tm.getTransaction();
            boolean delistResult = tx.delistResource(xaResource, XAResource.TMSUCCESS);

            if (Tracer.isEnabled())
               Tracer.delistConnectionListener(cm.getPool().getConfiguration().getId(),
                                               getManagedConnectionPool(),
                                               this, tx.toString(),
                                               true, false, false);

            if (delistResult)
            {
               log.tracef("delist-success: %s", this);
            }
            else
            {
               log.debugf("delist-success failed: %s", this);
            }
         }

         localTransaction = false;
      
         if (transactionSynchronization != null)
         {
            transactionSynchronization.cancel();
            transactionSynchronization = null;
         }

         enlisted = false;
         log.tracef("Delisted: %s", this);
      }
      catch (Exception e)
      {
         throw new ResourceException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void localTransactionStarted(ConnectionEvent ce)
   {
      localTransaction = true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void localTransactionCommitted(ConnectionEvent ce)
   {
      localTransaction = false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void localTransactionRolledback(ConnectionEvent ce)
   {
      localTransaction = false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void toPool() throws ResourceException
   {
      if (localTransaction)
      {
         LocalTransaction localTransaction = null;
         ManagedConnection mc = getManagedConnection();
         try
         {
            localTransaction = mc.getLocalTransaction();
         }
         catch (Throwable t)
         {
            throw new ResourceException(t);
         }

         if (localTransaction == null)
         {
            throw new ResourceException();
         }
         else
         {
            localTransaction.rollback();
         }
      }

      resetXAResourceTimeout();
      
      super.toPool();
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
         TransactionalConnectionManager txCM = (TransactionalConnectionManager)cm;
         TransactionIntegration ti = txCM.getTransactionIntegration();

         if (ti != null)
         {
            Transaction tx = null;
            try
            {
               tx = ti.getTransactionManager().getTransaction();

               if (Tracer.isEnabled() && tx != null)
                  txId = tx.toString();
               
               if (TxUtils.isUncommitted(tx))
               {
                  log.tracef("connectionErrorOccurred: delistResource(%s, TMFAIL)", xaResource);

                  boolean failResult = tx.delistResource(xaResource, XAResource.TMFAIL);
                  
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
               if (TxUtils.isUncommitted(tx))
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
            Tracer.delistConnectionListener(cm.getPool().getConfiguration().getId(),
                                            getManagedConnectionPool(),
                                            this, txId, false, true, false);
         }
      }

      // Prepare to explode
      enlisted = false;
      transactionSynchronization = null;
   }

   /**
    * Reset XAResource timeout
    */
   private void resetXAResourceTimeout()
   {
      // Do a reset of the underlying XAResource timeout
      if (!(xaResource instanceof LocalXAResource) && xaResourceTimeout > 0)
      {
         try
         {
            xaResource.setTransactionTimeout(xaResourceTimeout);
         }
         catch (XAException e)
         {
            log.debugf(e, "Exception during resetXAResourceTimeout for %s", this);
         }
      }
   }

   /**
    * Create the transaction synchronization object
    * @return The object
    */
   protected TransactionSynchronization createTransactionSynchronization()
   {
      return new TransactionSynchronizationImpl();
   }
   
   /**
    * Transaction synchronization
    */
   class TransactionSynchronizationImpl implements TransactionSynchronization
   {
      /** Transaction */
      private Transaction transaction;

      /** Cancel */
      private boolean cancel;

      /**
       * Constructor
       */
      public TransactionSynchronizationImpl()
      {
      }

      /**
       * {@inheritDoc}
       */
      public void init(Transaction tx)
      {
         this.transaction = tx;
         this.cancel = false;
      }

      /**
       * {@inheritDoc}
       */
      public void enlist() throws ResourceException
      {
         ResourceException enlistError = null;
         try
         {
            if (!transaction.enlistResource(xaResource))
            {
               if (Tracer.isEnabled())
                  Tracer.enlistConnectionListener(cm.getPool().getConfiguration().getId(), 
                                                  getManagedConnectionPool(),
                                                  AbstractTransactionalConnectionListener.this,
                                                  transaction.toString(), false, false);

               enlistError = new ResourceException("Failed to enlist");
            }
            else
            {
               if (Tracer.isEnabled())
                  Tracer.enlistConnectionListener(cm.getPool().getConfiguration().getId(), 
                                                  getManagedConnectionPool(),
                                                  AbstractTransactionalConnectionListener.this,
                                                  transaction.toString(), true, false);
            }
         }
         catch (Exception e)
         {
            enlistError = new ResourceException(e);

            if (Tracer.isEnabled())
               Tracer.enlistConnectionListener(cm.getPool().getConfiguration().getId(), 
                                               getManagedConnectionPool(),
                                               AbstractTransactionalConnectionListener.this,
                                               transaction.toString(), false, false);
         }

         if (enlistError != null)
         {
            if (Tracer.isEnabled())
               Tracer.exception(cm.getPool().getConfiguration().getId(), 
                                getManagedConnectionPool(),
                                AbstractTransactionalConnectionListener.this, enlistError);

            transactionSynchronization = null;
            enlisted = false;

            throw enlistError;
         }
      }

      /**
       * {@inheritDoc}
       */
      public void cancel()
      {
         cancel = true;
      }

      /**
       * {@inheritDoc}
       */
      public void beforeCompletion()
      {
         if (!cancel)
         {
            log.tracef("beforeCompletion: %s", AbstractTransactionalConnectionListener.this);
            try
            {
               if (TxUtils.isUncommitted(transaction))
               {
                  if (TxUtils.isActive(transaction))
                  {
                     log.tracef("delistResource(%s, TMSUCCESS)", xaResource);

                     transaction.delistResource(xaResource, XAResource.TMSUCCESS);

                     if (Tracer.isEnabled())
                        Tracer.delistConnectionListener(cm.getPool().getConfiguration().getId(),
                                                        getManagedConnectionPool(),
                                                        AbstractTransactionalConnectionListener.this,
                                                        transaction.toString(),
                                                        true, false, false);


                  }
                  else
                  {
                     log.tracef("delistResource(%s, TMFAIL)", xaResource);

                     transaction.delistResource(xaResource, XAResource.TMFAIL);

                     if (Tracer.isEnabled())
                        Tracer.delistConnectionListener(cm.getPool().getConfiguration().getId(),
                                                        getManagedConnectionPool(),
                                                        AbstractTransactionalConnectionListener.this,
                                                        transaction.toString(),
                                                        false, false, false);
                  }
               }
               else
               {
                  log.tracef("Non-uncommitted transaction for %s (%s)", AbstractTransactionalConnectionListener.this,
                             transaction != null ? TxUtils.getStatusAsString(transaction.getStatus()) : "None");
               }
            }
            catch (Exception e)
            {
               log.beforeCompletionErrorOccured(AbstractTransactionalConnectionListener.this, e);
            }
         }
         else
         {
            log.tracef("Unenlisted resource: %s", AbstractTransactionalConnectionListener.this);
         }
      }

      /**
       * {@inheritDoc}
       */
      public void afterCompletion(int status)
      {
         if (!cancel)
         {
            log.tracef("afterCompletion(%s): %s", status, AbstractTransactionalConnectionListener.this);

            // "Delist"
            transactionSynchronization = null;
            enlisted = false;

            if (connectionHandles.isEmpty())
            {
               if (Tracer.isEnabled() && status == Status.STATUS_ROLLEDBACK)
                  Tracer.delistConnectionListener(cm.getPool().getConfiguration().getId(),
                                                  getManagedConnectionPool(),
                                                  AbstractTransactionalConnectionListener.this, "",
                                                  true, true, false);

               cm.returnConnectionListener(AbstractTransactionalConnectionListener.this, false);
            }
            else
            {
               if (cm.getConnectionManagerConfiguration().isTracking() == null ||
                   cm.getConnectionManagerConfiguration().isTracking().booleanValue())
               {
                  log.activeHandles(cm.getPool().getConfiguration().getId(), connectionHandles.size());

                  if (connectionTraces != null)
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
                        Tracer.clearConnection(cm.getPool().getConfiguration().getId(),
                                               getManagedConnectionPool(),
                                               AbstractTransactionalConnectionListener.this, c);
                     }
                  }

                  cm.returnConnectionListener(AbstractTransactionalConnectionListener.this, true);
               }
               else
               {
                  log.tracef(new Exception("Connection across boundary"), "ConnectionListener=%s",
                             AbstractTransactionalConnectionListener.this);
               }
            }
         }
      }
   }
}
