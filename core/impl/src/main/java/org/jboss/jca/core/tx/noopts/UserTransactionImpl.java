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

import org.jboss.jca.core.spi.transaction.usertx.UserTransactionProvider;
import org.jboss.jca.core.spi.transaction.usertx.UserTransactionRegistry;

import java.io.Serializable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;

/**
 * A transaction manager implementation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class UserTransactionImpl implements UserTransactionProvider, UserTransaction, Serializable
{
   private static final long serialVersionUID = 2L;
   private static final String JNDI_NAME = "java:/UserTransaction";
   private transient TxRegistry registry;
   private transient UserTransactionRegistry userTransactionRegistry;

   /**
    * Constructor
    */
   public UserTransactionImpl()
   {
      this.registry = null;
      this.userTransactionRegistry = null;
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
    * Set the user transaction registry
    * @param v The value
    */
   public void setUserTransactionRegistry(UserTransactionRegistry v)
   {
      userTransactionRegistry = v;
   }

   /**
    * {@inheritDoc}
    */
   public void begin() throws NotSupportedException,
                              SystemException
   {
      Transaction tx = registry.getTransaction();

      if (tx != null && tx.getStatus() != Status.STATUS_UNKNOWN)
         throw new SystemException();

      registry.startTransaction();

      if (userTransactionRegistry != null)
         userTransactionRegistry.userTransactionStarted();
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

      if (tx.getStatus() == Status.STATUS_ROLLING_BACK ||
          tx.getStatus() == Status.STATUS_ROLLEDBACK ||
          tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
         throw new RollbackException();

      registry.commitTransaction();
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

      registry.rollbackTransaction();
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
   public void setTransactionTimeout(int seconds) throws SystemException
   {
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
