/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import java.util.HashSet;
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
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class TransactionImpl implements Transaction, Serializable
{
   private static final long serialVersionUID = 2L;
   private transient int status;
   private transient Set<Synchronization> syncs;
   private transient Set<XAResource> xas;

   /**
    * Constructor
    */
   public TransactionImpl()
   {
      this.status = Status.STATUS_ACTIVE;
      this.syncs = null;
      this.xas = null;
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
      finish(Status.STATUS_COMMITTED);
   }

   /**
    * {@inheritDoc}
    */
   public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException,
                                                                    SystemException
   {
      if (xas == null)
         return false;

      return xas.remove(xaRes);
   }

   /**
    * {@inheritDoc}
    */
   public boolean enlistResource(XAResource xaRes) throws RollbackException,
                                                          IllegalStateException,
                                                          SystemException
   {
      if (xas == null)
         xas = new HashSet<XAResource>(1);

      return xas.add(xaRes);
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
      if (syncs == null)
         syncs = new HashSet<Synchronization>(1);

      syncs.add(sync);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback() throws IllegalStateException,
                                 SystemException
   {
      finish(Status.STATUS_ROLLEDBACK);
   }

   /**
    * {@inheritDoc}
    */
   public void setRollbackOnly() throws IllegalStateException,
                                        SystemException
   {
      status = Status.STATUS_MARKED_ROLLBACK;
   }

   /**
    * Get rollback only
    * @return The value
    */
   boolean getRollbackOnly()
   {
      return status == Status.STATUS_MARKED_ROLLBACK;
   }

   /**
    * Finish transaction
    * @param st The status
    */
   private void finish(int st)
   {
      if (syncs != null)
      {
         for (Synchronization s : syncs)
         {
            s.beforeCompletion();
         }
      }

      status = st;

      if (syncs != null)
      {
         for (Synchronization s : syncs)
         {
            s.afterCompletion(status);
         }
      }
   }
}
