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

import org.jboss.jca.core.spi.transaction.local.TransactionLocal;

import javax.transaction.Transaction;

import org.jboss.logging.Logger;

/**
 * A transaction local delegate implementation
 */
public class TransactionLocalDelegateImpl
   implements org.jboss.jca.core.spi.transaction.local.TransactionLocalDelegate
{
   /** The logger */
   private static Logger log = Logger.getLogger(TransactionLocalDelegateImpl.class);

   /** Delegator */
   private org.jboss.tm.TransactionLocalDelegate delegator;

   /**
    * Constructor
    * @param delegator The delegator
    */
   public TransactionLocalDelegateImpl(org.jboss.tm.TransactionLocalDelegate delegator)
   {
      this.delegator = delegator;
   }

   /**
    * {@inheritDoc}
    */
   public Object getValue(TransactionLocal local, Transaction tx)
   {
      if (!(local instanceof TransactionLocalImpl))
         throw new IllegalArgumentException("Local is not a TransactionLocalImpl instance");

      return delegator.getValue((TransactionLocalImpl)local, tx);
   }

   /**
    * {@inheritDoc}
    */
   public void storeValue(TransactionLocal local, Transaction tx, Object value)
   {
      if (!(local instanceof TransactionLocalImpl))
         throw new IllegalArgumentException("Local is not a TransactionLocalImpl instance");

      delegator.storeValue((TransactionLocalImpl)local, tx, value);
   }

   /**
    * {@inheritDoc}
    */
   public boolean containsValue(TransactionLocal local, Transaction tx)
   {
      if (!(local instanceof TransactionLocalImpl))
         throw new IllegalArgumentException("Local is not a TransactionLocalImpl instance");

      return delegator.containsValue((TransactionLocalImpl)local, tx);
   }
   
   /**
    * {@inheritDoc}
    */
   public void lock(TransactionLocal local, Transaction tx) throws InterruptedException
   {
      if (!(local instanceof TransactionLocalImpl))
         throw new IllegalArgumentException("Local is not a TransactionLocalImpl instance");

      delegator.lock((TransactionLocalImpl)local, tx);
   }
   
   /**
    * {@inheritDoc}
    */
   public void unlock(TransactionLocal local, Transaction tx)
   {
      if (!(local instanceof TransactionLocalImpl))
         throw new IllegalArgumentException("Local is not a TransactionLocalImpl instance");

      delegator.unlock((TransactionLocalImpl)local, tx);
   }
}
