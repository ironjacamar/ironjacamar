/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.connectionmanager.listener.stable;

import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.core.CoreBundle;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.TransactionSynchronization;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;
import org.ironjacamar.core.connectionmanager.pool.stable.StablePool;
import org.ironjacamar.core.spi.transaction.TxUtils;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROY;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Messages;

/**
 * An abstract transactional connection listener for the stable configuration
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractTransactionalConnectionListener extends
   org.ironjacamar.core.connectionmanager.listener.AbstractTransactionalConnectionListener
{

   /** The Message bundle */
   CoreBundle bundle = Messages.getBundle(CoreBundle.class);

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
      super(cm, mc, credential, xaResource, xaResourceTimeout, mcp, flushStrategy);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void enlist() throws ResourceException
   {
      if (isEnlisted())
      {
         try
         {
            StablePool sp = (StablePool)cm.getPool();
            Object tx = sp.verifyConnectionListener(this);
            if (tx != null)
               throw new ResourceException(tx.toString());
         }
         finally
         {
            setState(DESTROY);
         }
      }

      super.enlist();
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void delist() throws ResourceException
   {
      if (!isEnlisted())
      {
         setState(DESTROY);
      }

      super.delist();
   }

   /**
    * Create the transaction synchronization object
    * @return The object
    */
   @Override
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

      /** */
      private Throwable throwable;
      
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
         this.throwable = new Throwable(bundle.unableToEnlist());
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
               enlistError = new ResourceException(bundle.failedToEnlist(), throwable);
            }
         }
         catch (Exception e)
         {
            enlistError = new ResourceException(e);
            enlistError.initCause(throwable);
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

            if (connectionHandles.isEmpty())
            {
               cm.returnConnectionListener(AbstractTransactionalConnectionListener.this, false);
            }
         }
      }
   }
}
