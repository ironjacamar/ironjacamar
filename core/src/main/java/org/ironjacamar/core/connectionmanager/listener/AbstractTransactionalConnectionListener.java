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

import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.TransactionalConnectionManager;
import org.ironjacamar.core.spi.transaction.ConnectableResource;
import org.ironjacamar.core.spi.transaction.TxUtils;
import org.ironjacamar.core.spi.transaction.local.LocalXAResource;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROY;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROYED;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

/**
 * An abstract transactional connection listener, which is enlisted on the transaction boundary
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractTransactionalConnectionListener extends AbstractConnectionListener
{
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
    */
   public AbstractTransactionalConnectionListener(ConnectionManager cm, ManagedConnection mc, Credential credential,
                                                  XAResource xaResource, int xaResourceTimeout)
   {
      super(cm, mc, credential);

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
         /*
           ((ConnectableResource) xaResource).setConnectableResourceListener(this);
         */
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
            // TODO: log
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
               enlistError = new ResourceException("Failed to enlist");
            }
         }
         catch (Exception e)
         {
            enlistError = new ResourceException(e);
         }

         if (enlistError != null)
         {
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
            try
            {
               if (TxUtils.isUncommitted(transaction))
               {
                  if (TxUtils.isActive(transaction))
                  {
                     transaction.delistResource(xaResource, XAResource.TMSUCCESS);
                  }
                  else
                  {
                     transaction.delistResource(xaResource, XAResource.TMFAIL);
                  }
               }
            }
            catch (Exception e)
            {
               // TODO
            }
         }
      }

      /**
       * {@inheritDoc}
       */
      public void afterCompletion(int status)
      {
         if (!cancel)
         {
            // "Delist"
            transactionSynchronization = null;
            enlisted = false;

            if (connectionHandles.size() == 0)
            {
               cm.returnConnectionListener(AbstractTransactionalConnectionListener.this, false);
            }
         }
      }
   }
}
