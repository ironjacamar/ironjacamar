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

import org.ironjacamar.core.api.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.TransactionalConnectionManager;
import org.ironjacamar.core.spi.transaction.ConnectableResource;
import org.ironjacamar.core.spi.transaction.TxUtils;
import org.ironjacamar.core.spi.transaction.local.LocalXAResource;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROY;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROYED;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ManagedConnection;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

/**
 * An abstract transactional connection listener, which is enlisted on the transaction boundary
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractTransactionalConnectionListener extends AbstractConnectionListener
{
   /** Transaction synchronization instance */
   private TransactionSynchronization transactionSynchronization;

   /** Enlisted flag */
   private boolean enlisted;
   
   /** XAResource instance */
   private XAResource xaResource;

   /** Whether there is a local transaction */
   private boolean localTransaction;

   /**
    * Constructor
    * @param cm The connection manager
    * @param mc The managed connection
    * @param credential The credential
    * @param xaResource The associated XAResource
    */
   public AbstractTransactionalConnectionListener(ConnectionManager cm, ManagedConnection mc, Credential credential,
                                                  XAResource xaResource)
   {
      super(cm, mc, credential);

      this.transactionSynchronization = null;
      this.enlisted = false;
      this.xaResource = xaResource;
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
      
         transactionSynchronization = new TransactionSynchronization(tx);
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
    * Transaction synchronization
    */
   class TransactionSynchronization implements Synchronization
   {
      /** Transaction */
      private Transaction transaction;

      /** Cancel */
      private boolean cancel;

      /**
       * Constructor
       * @param tx The transaction
       */
      public TransactionSynchronization(Transaction tx)
      {
         this.transaction = tx;
         this.cancel = false;
      }

      /**
       * Enlist
       * @exception ResourceException Thrown if an error occurs
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
       * Set the cancel flag
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
