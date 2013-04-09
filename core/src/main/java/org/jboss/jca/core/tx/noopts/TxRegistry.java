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

/**
 * The transaction registry
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
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
      txs.put(Long.valueOf(Thread.currentThread().getId()), new TransactionImpl());
   }

   /**
    * End a transaction
    */
   public void endTransaction()
   {
      txs.remove(Long.valueOf(Thread.currentThread().getId()));
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
