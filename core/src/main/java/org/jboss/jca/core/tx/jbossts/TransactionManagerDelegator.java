/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

import org.jboss.jca.core.spi.transaction.TransactionTimeoutConfiguration;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * This class provide a delegator implementation of the transaction manager
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TransactionManagerDelegator implements TransactionManager, TransactionTimeoutConfiguration
{
   /** The transaction manager */
   private TransactionManager tm;

   /**
    * Constructor
    * @param tm The transaction manager
    */
   public TransactionManagerDelegator(TransactionManager tm)
   {
      this.tm = tm;
   }

   /**
    * {@inheritDoc}
    */
   public void begin() throws NotSupportedException, SystemException
   {
      tm.begin();
   }

   /**
    * {@inheritDoc}
    */
   public void commit() throws RollbackException, HeuristicMixedException,
                               HeuristicRollbackException, SecurityException, IllegalStateException,
                               SystemException
   {
      tm.commit();
   }

   /**
    * {@inheritDoc}
    */
   public int getStatus() throws SystemException
   {
      return tm.getStatus();
   }

   /**
    * {@inheritDoc}
    */
   public Transaction getTransaction() throws SystemException
   {
      return tm.getTransaction();
   }

   /**
    * {@inheritDoc}
    */
   public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException
   {
      tm.resume(tobj);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback() throws IllegalStateException, SecurityException, SystemException
   {
      tm.rollback();
   }

   /**
    * {@inheritDoc}
    */
   public void setRollbackOnly() throws IllegalStateException, SystemException
   {
      tm.setRollbackOnly();
   }

   /**
    * {@inheritDoc}
    */
   public void setTransactionTimeout(int seconds) throws SystemException
   {
      tm.setTransactionTimeout(seconds);
   }

   /**
    * {@inheritDoc}
    */
   public Transaction suspend() throws SystemException
   {
      return tm.suspend();
   }

   /**
    * {@inheritDoc}
    */
   public int getTransactionTimeout() throws SystemException
   {
      if (tm instanceof org.jboss.tm.TransactionTimeoutConfiguration)
         return ((org.jboss.tm.TransactionTimeoutConfiguration)tm).getTransactionTimeout();

      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public long getTimeLeftBeforeTransactionTimeout(boolean errorRollback) throws RollbackException
   {
      if (tm instanceof org.jboss.tm.TransactionTimeoutConfiguration)
         return ((org.jboss.tm.TransactionTimeoutConfiguration)tm).getTimeLeftBeforeTransactionTimeout(errorRollback);

      return 0;
   }
}
