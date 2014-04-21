/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.transaction.SystemException;

/**
 * The transaction registry
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TxRegistry implements Serializable
{
   private static final long serialVersionUID = 1L;
   private ConcurrentMap<Long, TransactionImpl> txs;

   /**
    * Constructor
    */
   public TxRegistry()
   {
      this.txs = new ConcurrentHashMap<Long, TransactionImpl>();
   }

   /**
    * Get the transaction for the current thread
    * @return The value
    */
   public TransactionImpl getTransaction()
   {
      return txs.get(Long.valueOf(Thread.currentThread().getId()));
   }

   /**
    * Start a transaction
    */
   public void startTransaction()
   {
      Long key = Long.valueOf(Thread.currentThread().getId());
      TransactionImpl tx = txs.get(key);

      if (tx == null)
      {
         TransactionImpl newTx = new TransactionImpl(key);
         tx = txs.putIfAbsent(key, newTx);
         if (tx == null)
            tx = newTx;
      }

      tx.active();
   }

   /**
    * Commit a transaction
    * @exception SystemException Thrown if an error occurs
    */
   public void commitTransaction() throws SystemException
   {
      Long key = Long.valueOf(Thread.currentThread().getId());
      TransactionImpl tx = txs.get(key);
      if (tx != null)
      {
         try
         {
            tx.commit();
         }
         catch (Throwable t)
         {
            SystemException se = new SystemException("Error during commit");
            se.initCause(t);
            throw se;
         }
      }
      else
      {
         throw new IllegalStateException("No transaction to commit");
      }
   }

   /**
    * Rollback a transaction
    * @exception SystemException Thrown if an error occurs
    */
   public void rollbackTransaction() throws SystemException
   {
      Long key = Long.valueOf(Thread.currentThread().getId());
      TransactionImpl tx = txs.get(key);
      if (tx != null)
      {
         try
         {
            tx.rollback();
         }
         catch (Throwable t)
         {
            SystemException se = new SystemException("Error during rollback");
            se.initCause(t);
            throw se;
         }
      }
      else
      {
         throw new IllegalStateException("No transaction to rollback");
      }
   }

   /**
    * Assign a transaction
    * @param v The value
    */
   public void assignTransaction(TransactionImpl v)
   {
      txs.put(Long.valueOf(Thread.currentThread().getId()), v);
   }
}
