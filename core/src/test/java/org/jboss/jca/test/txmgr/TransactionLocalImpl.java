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
package org.jboss.jca.test.txmgr;

import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.spi.transaction.local.TransactionLocal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transaction;

/**
 * A TransactionLocal implementation
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class TransactionLocalImpl implements TransactionLocal
{
   private TransactionIntegration ti;
   private Map<Transaction, Object> values;

   /**
    * Constructor
    * @param ti The transaction integration
    */
   public TransactionLocalImpl(TransactionIntegration ti)
   {
      this.ti = ti;
      this.values = Collections.synchronizedMap(new HashMap<Transaction, Object>());
   }

   /**
    * {@inheritDoc}
    */
   public void lock() throws InterruptedException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void lock(Transaction transaction) throws InterruptedException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void unlock()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void unlock(Transaction transaction)
   {
   }

   /**
    * {@inheritDoc}
    */
   public synchronized Object get()
   {
      return get(getTransaction());
   }

   /**
    * {@inheritDoc}
    */
   public synchronized Object get(Transaction transaction)
   {
      return values.get(transaction);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void set(Object value)
   {
      set(getTransaction(), value);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void set(Transaction transaction, Object value)
   {
      values.put(transaction, value);
   }

   /**
    * Get the current transaction
    * @return The value
    */
   public synchronized Transaction getTransaction()
   {
      try
      {
         return ti.getTransactionManager().getTransaction();
      }
      catch (Throwable t)
      {
         throw new IllegalStateException(t.getMessage(), t);
      }
   }
}
