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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

/**
 * A transaction implementation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TransactionImpl implements Transaction, Serializable
{
   private static final long serialVersionUID = 3L;
   private transient Long key;
   private transient int status;
   private transient Set<Synchronization> syncs;
   private transient Map<Object, Object> resources;

   /**
    * Constructor
    * @param key The transaction key
    */
   public TransactionImpl(Long key)
   {
      this.key = key;
      this.status = Status.STATUS_ACTIVE;
      this.syncs = new HashSet<Synchronization>();
      this.resources = new HashMap<Object, Object>();
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
      if (status == Status.STATUS_UNKNOWN)
         throw new IllegalStateException("Status unknown");

      if (status == Status.STATUS_MARKED_ROLLBACK)
         throw new IllegalStateException("Status marked rollback");

      finish(true);
   }

   /**
    * {@inheritDoc}
    */
   public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException,
                                                                    SystemException
   {
      if (status == Status.STATUS_UNKNOWN)
         throw new IllegalStateException("Status unknown");

      if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK)
         throw new IllegalStateException("Status not valid");

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public boolean enlistResource(XAResource xaRes) throws RollbackException,
                                                          IllegalStateException,
                                                          SystemException
   {
      if (status == Status.STATUS_UNKNOWN)
         throw new IllegalStateException("Status unknown");

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public int getStatus() throws SystemException
   {
      return status;
   }

   /**
    * {@inheritDoc}
    */
   public void registerSynchronization(Synchronization sync) throws RollbackException,
                                                                    IllegalStateException,
                                                                    SystemException
   {
      if (status == Status.STATUS_UNKNOWN)
         throw new IllegalStateException("Status unknown");

      syncs.add(sync);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback() throws IllegalStateException,
                                 SystemException
   {
      if (status == Status.STATUS_UNKNOWN)
         throw new IllegalStateException("Status unknown");

      finish(false);
   }

   /**
    * {@inheritDoc}
    */
   public void setRollbackOnly() throws IllegalStateException,
                                        SystemException
   {
      if (status == Status.STATUS_UNKNOWN)
         throw new IllegalStateException("Status unknown");

      status = Status.STATUS_MARKED_ROLLBACK;
   }

   /**
    * Get rollback only
    * @return The value
    */
   boolean getRollbackOnly()
   {
      if (status == Status.STATUS_UNKNOWN)
         throw new IllegalStateException("Status unknown");

      return status == Status.STATUS_MARKED_ROLLBACK;
   }

   /**
    * Put a resource
    * @param key The key
    * @param value The value
    */
   void putResource(Object key, Object value)
   {
      resources.put(key, value);
   }

   /**
    * Get a resource
    * @param key The key
    * @return The value
    */
   Object getResource(Object key)
   {
      return resources.get(key);
   }

   /**
    * Get the transaction key
    * @return The value
    */
   Long getKey()
   {
      return key;
   }

   /**
    * Active
    */
   void active()
   {
      status = Status.STATUS_ACTIVE;
   }

   /**
    * Finish transaction
    * @param commit Commit (true), or rollback (false)
    */
   private void finish(boolean commit)
   {
      for (Synchronization s : syncs)
      {
         s.beforeCompletion();
      }

      if (commit)
      {
         status = Status.STATUS_COMMITTED;
      }
      else
      {
         status = Status.STATUS_ROLLEDBACK;
      }

      for (Synchronization s : syncs)
      {
         s.afterCompletion(status);
      }

      status = Status.STATUS_UNKNOWN;
      syncs.clear();
      resources.clear();
   }
}
