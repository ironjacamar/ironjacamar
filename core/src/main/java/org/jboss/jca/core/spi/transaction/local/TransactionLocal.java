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
package org.jboss.jca.core.spi.transaction.local;

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
public interface TransactionLocal
{
   /**
    * Lock the TransactionLocal using the current transaction<p>
    *
    * WARN: The current implemention just "locks the transactions"
    *
    * @throws IllegalStateException if the transaction is not active
    * @throws InterruptedException if the thread is interrupted
    */
   public void lock() throws InterruptedException;

   /**
    * Lock the TransactionLocal using the provided transaction<p>
    *
    * WARN: The current implemention just "locks the transactions"
    *
    * @param transaction the transaction
    * @throws IllegalStateException if the transaction is not active
    * @throws InterruptedException if the thread is interrupted
    */
   public void lock(Transaction transaction) throws InterruptedException;

   /**
    * Unlock the TransactionLocal using the current transaction
    */
   public void unlock();

   /**
    * Unlock the ThreadLocal using the provided transaction
    *
    * @param transaction the transaction
    */
   public void unlock(Transaction transaction);

   /**
    * Returns the value of this TransactionLocal variable associated with the
    * thread context transaction. Creates and initializes the copy if this is
    * the first time the method is called in a transaction.
    *
    * @return the value of this TransactionLocal
    */
   public Object get();

   /**
    * Returns the value of this TransactionLocal variable associated with the
    * specified transaction. Creates and initializes the copy if this is the
    * first time the method is called in a transaction.
    *
    * @param transaction the transaction for which the variable it to
    * be retrieved
    * @return the value of this TransactionLocal
    * @throws IllegalStateException if an error occures while registering
    * a synchronization callback with the transaction
    */
   public Object get(Transaction transaction);

   /**
    * Sets the value of this TransactionLocal variable associtated with the
    * thread context transaction. This is only used to change the value from
    * the one assigned by the initialValue method, and many applications will
    * have no need for this functionality.
    *
    * @param value the value to be associated with the thread context
    * transactions's TransactionLocal
    */
   public void set(Object value);

   /**
    * Sets the value of this TransactionLocal variable associtated with the
    * specified transaction. This is only used to change the value from
    * the one assigned by the initialValue method, and many applications will
    * have no need for this functionality.
    *
    * @param transaction the transaction for which the value will be set
    * @param value the value to be associated with the thread context
    * transactions's TransactionLocal
    */
   public void set(Transaction transaction, Object value);

   /**
    * Get the current transaction
    * @return The value
    */
   public Transaction getTransaction();
}
