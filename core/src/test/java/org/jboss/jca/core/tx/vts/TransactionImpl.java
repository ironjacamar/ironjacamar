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
package org.jboss.jca.core.tx.vts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

/**
 * A transaction implementation
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class TransactionImpl implements Transaction, Serializable
{
   private static final long serialVersionUID = 2L;
   private static final XidImpl XID_IMPL = new XidImpl();
   private transient int status;
   private transient Set<Synchronization> syncs;
   private transient Map<XAResource, Integer> enlisted;
   private transient Set<XAResource> delisted;

   /**
    * Constructor
    */
   public TransactionImpl()
   {
      this.status = Status.STATUS_ACTIVE;
      this.syncs = null;
      this.enlisted = null;
      this.delisted = null;
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

      finish(Status.STATUS_COMMITTED);
   }

   /**
    * {@inheritDoc}
    */
   public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException,
                                                                    SystemException
   {
      if (status == Status.STATUS_UNKNOWN)
         throw new IllegalStateException("Status unknown");

      if (enlisted == null)
         throw new IllegalStateException("No XAResouce instances registrered");

      if (enlisted.containsKey(xaRes))
      {
         try
         {
            xaRes.end(XID_IMPL, flag);
         }
         catch (XAException xe)
         {
            SystemException se = new SystemException(xe.getMessage());
            se.initCause(xe);
            throw se;
         }

         if (flag != XAResource.TMSUSPEND)
         {
            enlisted.remove(xaRes);
            
            if (delisted == null)
               delisted = new HashSet<XAResource>();

            delisted.add(xaRes);
         }

         return true;
      }

      throw new IllegalStateException("Unknown XAResource: " + xaRes);
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

      if (enlisted == null)
         enlisted = new HashMap<XAResource, Integer>();

      if (!enlisted.containsKey(xaRes))
      {
         try
         {
            xaRes.start(XID_IMPL, XAResource.TMNOFLAGS);
         }
         catch (XAException xe)
         {
            SystemException se = new SystemException(xe.getMessage());
            se.initCause(xe);
            throw se;
         }

         enlisted.put(xaRes, Integer.valueOf(XAResource.TMNOFLAGS));
         return true;
      }
      else if (enlisted.containsKey(xaRes))
      {
         if (enlisted.get(xaRes).intValue() != XAResource.TMSUSPEND)
            throw new IllegalStateException("XAResource not suspended");

         try
         {
            xaRes.start(XID_IMPL, XAResource.TMRESUME);
         }
         catch (XAException xe)
         {
            SystemException se = new SystemException(xe.getMessage());
            se.initCause(xe);
            throw se;
         }

         enlisted.put(xaRes, Integer.valueOf(XAResource.TMRESUME));
         return true;
      }

      throw new IllegalStateException("Unknown XAResource: " + xaRes);
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
      if (status == Status.STATUS_UNKNOWN)
         throw new IllegalStateException("Status unknown");

      finish(Status.STATUS_ROLLEDBACK);
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

      if (enlisted != null && !enlisted.isEmpty())
         throw new IllegalStateException("XAResource instances still registrered");

      status = st;

      if (delisted != null && !delisted.isEmpty())
      {
         if (status == Status.STATUS_COMMITTED)
         {
            boolean commit = true;

            Iterator<XAResource> it = delisted.iterator();
            while (commit && it.hasNext())
            {
               XAResource xar = it.next();
               try
               {
                  xar.prepare(XID_IMPL);
               }
               catch (Throwable t)
               {
                  commit = false;
               }
            }

            if (!commit)
               status = Status.STATUS_MARKED_ROLLBACK;
         }

         for (XAResource xar : delisted)
         {
            try
            {
               if (status == Status.STATUS_COMMITTED)
               {
                  xar.commit(XID_IMPL, true);
               }
               else
               {
                  xar.rollback(XID_IMPL);
               }
            }
            catch (Throwable t)
            {
               try
               {
                  xar.rollback(XID_IMPL);
               }
               catch (XAException xe)
               {
                  // Yikes
               } 
            }
         }
      }

      if (syncs != null)
      {
         for (Synchronization s : syncs)
         {
            s.afterCompletion(status);
         }
      }

      status = Status.STATUS_UNKNOWN;

      if (syncs != null)
         syncs = null;

      if (enlisted != null)
         enlisted = null;

      if (delisted != null)
         delisted = null;
   }
}
