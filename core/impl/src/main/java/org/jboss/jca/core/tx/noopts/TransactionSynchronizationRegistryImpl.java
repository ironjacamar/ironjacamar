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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * A transaction synchronization registry implementation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TransactionSynchronizationRegistryImpl implements TransactionSynchronizationRegistry, Serializable
{
   private static final long serialVersionUID = 3L;
   private static final String JNDI_NAME = "java:/TransactionSynchronizationRegistry";
   private transient TxRegistry registry;

   /**
    * Constructor
    */
   public TransactionSynchronizationRegistryImpl()
   {
      this.registry = null;
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
    * {@inheritDoc}
    */
   public Object getTransactionKey()
   {
      TransactionImpl tx = registry.getTransaction();

      if (tx != null)
         return tx.getKey();

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public void putResource(Object key, Object value)
   {
      TransactionImpl tx = registry.getTransaction();
      tx.putResource(key, value);
   }

   /**
    * {@inheritDoc}
    */
   public Object getResource(Object key)
   {
      TransactionImpl tx = registry.getTransaction();
      return tx.getResource(key);
   }

   /**
    * {@inheritDoc}
    */
   public void registerInterposedSynchronization(Synchronization sync)
   {
      TransactionImpl tx = registry.getTransaction();

      if (tx == null)
         throw new IllegalStateException();

      try
      {
         tx.registerSynchronization(sync);
      }
      catch (Throwable t)
      {
         // Nothing to do
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getTransactionStatus()
   {
      TransactionImpl tx = registry.getTransaction();

      if (tx == null)
         return Status.STATUS_NO_TRANSACTION;

      try
      {
         return tx.getStatus();
      }
      catch (Throwable t)
      {
         return Status.STATUS_UNKNOWN;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setRollbackOnly()
   {
      TransactionImpl tx = registry.getTransaction();

      if (tx == null)
         throw new IllegalStateException();

      try
      {
         tx.setRollbackOnly();
      }
      catch (Throwable t)
      {
         // Nothing to do
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean getRollbackOnly()
   {
      TransactionImpl tx = registry.getTransaction();

      if (tx == null)
         throw new IllegalStateException();

      return tx.getRollbackOnly();
   }

   /**
    * Start
    * @exception Throwable Thrown if an error occurs
    */
   public void start() throws Throwable
   {
      Context context = new InitialContext();

      context.bind(JNDI_NAME, this);

      context.close();
   }

   /**
    * Stop
    * @exception Throwable Thrown if an error occurs
    */
   public void stop() throws Throwable
   {
      Context context = new InitialContext();

      context.unbind(JNDI_NAME);

      context.close();
   }
}
