/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.tx.jbossts;

import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.spi.transaction.local.TransactionLocalDelegate;

import javax.transaction.SystemException;
import javax.transaction.Transaction;

/**
 * A TransactionLocal is similar to ThreadLocal except it is keyed on the
 * Transactions. A transaction local variable is cleared after the transaction
 * completes.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 */
public class TransactionLocalImpl extends org.jboss.tm.TransactionLocal 
   implements org.jboss.jca.core.spi.transaction.local.TransactionLocal
{
   /**
    * To simplify null values handling in the preloaded data pool we use
    * this value instead of 'null'
    */
   private static final Object NULL_VALUE = new Object();

   /** The transaction integration */
   private final TransactionIntegration transactionIntegration;

   /** The delegator */
   private final TransactionLocalDelegate delegate;

   /**
    * Creates a transaction local variable. 
    * Using the given transaction integration
    *
    * @param ti The transaction intgration
    */
   public TransactionLocalImpl(TransactionIntegration ti)
   {
      super(ti.getTransactionManager());

      if (ti.getTransactionLocalDelegate() == null)
         throw new IllegalArgumentException("Null transaction local delegate");

      this.transactionIntegration = ti;
      this.delegate = ti.getTransactionLocalDelegate();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void lock() throws InterruptedException
   {
      lock(getTransaction());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void lock(Transaction transaction) throws InterruptedException
   {
      // ignore when there is no transaction
      if (transaction == null)
         return;

      delegate.lock(this, transaction);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void unlock()
   {
      unlock(getTransaction());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void unlock(Transaction transaction)
   {
      // ignore when there is no transaction
      if (transaction == null)
         return;

      delegate.unlock(this, transaction);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Object initialValue()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Object getValue(Transaction tx)
   {
      return delegate.getValue(this, tx);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void storeValue(Transaction tx, Object value)
   {
      delegate.storeValue(this, tx, value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected boolean containsValue(Transaction tx)
   {
      return delegate.containsValue(this, tx);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object get()
   {
      return get(getTransaction());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object get(Transaction transaction)
   {
      if (transaction == null)
         return initialValue();

      Object value = getValue(transaction);

      // is we didn't get a value initalize this object with initialValue()
      if (value == null)
      {
         // get the initial value
         value = initialValue();

         // if value is null replace it with the null value standin
         if (value == null)
         {
            value = NULL_VALUE;
         }

         // store the value
         try
         {
            storeValue(transaction, value);
         }
         catch (IllegalStateException e)
         {
            // depending on the delegate implementation it may be considered an error to
            // call storeValue after the tx has ended. Further, the tx ending may have
            // caused the disposal of a previously stored initial value.
            // for user convenience we ignore such errors and return the initialvalue here.
            return initialValue();
         }
      }

      // if the value is the null standin return null
      if (value == NULL_VALUE)
      {
         return null;
      }

      // finall return the value
      return value;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void set(Object value)
   {
      set(getTransaction(), value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void set(Transaction transaction, Object value)
   {
      if (transaction == null)
         throw new IllegalStateException("there is no transaction");

      // If this transaction is unknown, register for synchroniztion callback,
      // and call initialValue to give subclasses a chance to do some
      // initialization.
      if (!containsValue(transaction))
      {
         initialValue();
      }

      // if value is null replace it with the null value standin
      if (value == null)
      {
         value = NULL_VALUE;
      }

      // finally store the value
      storeValue(transaction, value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Transaction getTransaction()
   {
      if (transactionIntegration.getTransactionManager() == null)
         throw new IllegalStateException("Null transaction manager");

      try
      {
         return transactionIntegration.getTransactionManager().getTransaction();
      }
      catch (SystemException e)
      {
         throw new IllegalStateException("An error occured while getting the " +
               "transaction associated with the current thread: " + e);
      }
   }
}
