/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.tx.noopts;

import org.jboss.jca.core.spi.transaction.xa.XATerminator;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * A transaction manager implementation
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class TransactionManagerImpl implements TransactionManager
{
   private TxRegistry registry;
   private XATerminator terminator;

   /**
    * Constructor
    */
   public TransactionManagerImpl()
   {
      this.registry = null;
      this.terminator = null;
   }

   /**
    * Set the registry
    * @param v The value
    */
   public void setRegistry(TxRegistry v)
   {
      registry = v;
   }

   /**
    * Get the terminator
    * @return The value
    */
   public XATerminator getXATerminator()
   {
      return terminator;
   }

   /**
    * Set the terminator
    * @param v The value
    */
   public void setXATerminator(XATerminator v)
   {
      terminator = v;
   }

   /**
    * {@inheritDoc}
    */
   public void begin() throws NotSupportedException,
                              SystemException
   {
      Transaction tx = registry.getTransaction();
      
      if (tx != null)
         throw new NotSupportedException();

      registry.startTransaction();
   }

   /**
    * {@inheritDoc}
    */
   public void commit() throws RollbackException,
                               HeuristicMixedException,
                               HeuristicRollbackException,
                               SecurityException,
                               IllegalStateException,
                               SystemException
   {
      Transaction tx = registry.getTransaction();

      if (tx == null)
         throw new SystemException();

      if (tx.getStatus() == Status.STATUS_ROLLEDBACK ||
          tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
         throw new RollbackException();

      registry.endTransaction();
   }

   /**
    * {@inheritDoc}
    */
   public int getStatus() throws SystemException
   {
      Transaction tx = registry.getTransaction();

      if (tx == null)
         return Status.STATUS_NO_TRANSACTION;

      return tx.getStatus();
   }

   /**
    * {@inheritDoc}
    */
   public Transaction getTransaction() throws SystemException
   {
      return registry.getTransaction();
   }

   /**
    * {@inheritDoc}
    */
   public void resume(Transaction tobj) throws InvalidTransactionException,
                                               IllegalStateException,
                                               SystemException
   {
      if (!(tobj instanceof TransactionImpl))
         throw new SystemException();

      registry.assignTransaction((TransactionImpl)tobj);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback() throws IllegalStateException,
                                 SecurityException,
                                 SystemException
   {
      Transaction tx = registry.getTransaction();

      if (tx == null)
         throw new IllegalStateException();

      registry.endTransaction();
   }

   /**
    * {@inheritDoc}
    */
   public void setRollbackOnly() throws IllegalStateException,
                                        SystemException
   {
      Transaction tx = registry.getTransaction();

      if (tx == null)
         throw new IllegalStateException();

      tx.setRollbackOnly();
   }

   /**
    * {@inheritDoc}
    */
   public void setTransactionTimeout(int seconds) throws SystemException
   {
   }

   /**
    * {@inheritDoc}
    */
   public Transaction suspend() throws SystemException
   {
      Transaction tx = registry.getTransaction();

      registry.assignTransaction(null);

      return tx;
   }
}
