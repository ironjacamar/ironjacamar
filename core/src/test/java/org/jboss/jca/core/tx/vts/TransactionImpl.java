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
package org.jboss.jca.core.tx.vts;

import org.jboss.jca.core.spi.transaction.local.LocalXAResource;

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

import org.jboss.logging.Logger;

/**
 * A transaction implementation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TransactionImpl implements Transaction, Serializable
{
   /** The logger */
   private static Logger log = Logger.getLogger(TransactionImpl.class);

   private static final long serialVersionUID = 3L;
   private static final XidImpl XID_IMPL = new XidImpl();
   private transient Long key;
   private transient int status;
   private transient Set<Synchronization> syncs;
   private transient Map<XAResource, Integer> enlisted;
   private transient Set<XAResource> delisted;
   private transient Map<Object, Object> resources;
   private transient boolean fail;

   /**
    * Constructor
    * @param key The transaction key
    */
   public TransactionImpl(Long key)
   {
      this.key = key;
      this.status = Status.STATUS_ACTIVE;
      this.syncs = null;
      this.enlisted = null;
      this.delisted = null;
      this.resources = new HashMap<Object, Object>();
      this.fail = false;
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

      log.tracef("commit(): %s", this);

      if (!fail)
      {
         finish(true);
      }
      else
      {
         finish(false);
      }
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

      if (enlisted == null)
         return false;

      log.tracef("delistResource(%s, %d): %s", xaRes, flag, this);

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
         else
         {
            enlisted.put(xaRes, Integer.valueOf(XAResource.TMSUSPEND));
         }

         return true;
      }

      return false;
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

      log.tracef("enlistResource(%s): %s", xaRes, this);

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
            fail = true;

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
      log.tracef("getStatus() => %d: %s", status, this);

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

      log.tracef("registerSynchronization(%s): %s", sync, this);

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

      log.tracef("rollback(): %s", this);

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

      log.tracef("setRollbackOnly(): %s", this);

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
    * Finish transaction
    * @param commit Commit (true), or rollback (false)
    */
   private void finish(boolean commit)
   {
      log.tracef("finish(%s): %s", commit, this);

      if (enlisted != null && !enlisted.isEmpty())
         commit = verifyEnlisted(commit);

      log.tracef("verifyEnlisted(%s): %s", commit, this);

      if (syncs != null)
      {
         for (Synchronization s : syncs)
         {
            s.beforeCompletion();
         }
      }

      if (enlisted != null && !enlisted.isEmpty())
         checkEnlisted(status);

      if (delisted != null && !delisted.isEmpty())
      {
         if (commit && delisted.size() > 1)
         {
            Iterator<XAResource> it = delisted.iterator();
            status = Status.STATUS_PREPARING;
            while (commit && it.hasNext())
            {
               XAResource xar = it.next();
               try
               {
                  if (!(xar instanceof LocalXAResource))
                     xar.prepare(XID_IMPL);
               }
               catch (Throwable t)
               {
                  commit = false;
               }
            }
            status = Status.STATUS_PREPARED;
         }

         for (XAResource xar : delisted)
         {
            try
            {
               if (commit)
               {
                  status = Status.STATUS_COMMITTING;
                  xar.commit(XID_IMPL, true);
                  status = Status.STATUS_COMMITTED;
               }
               else
               {
                  status = Status.STATUS_ROLLING_BACK;
                  xar.rollback(XID_IMPL);
                  status = Status.STATUS_ROLLEDBACK;
               }
            }
            catch (Throwable t)
            {
               try
               {
                  status = Status.STATUS_ROLLING_BACK;
                  xar.rollback(XID_IMPL);
                  status = Status.STATUS_ROLLEDBACK;
               }
               catch (XAException xe)
               {
                  // Yikes
               } 
            }
         }
      }
      else
      {
         if (commit)
         {
            status = Status.STATUS_COMMITTED;
         }
         else
         {
            status = Status.STATUS_ROLLEDBACK;
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

   /**
    * Check enlisted XAResources
    * @param status The transaction status
    */
   private boolean verifyEnlisted(boolean commit)
   {
      // No work for rollback
      if (!commit)
         return false;

      log.tracef("Enlisted: %s: %s", enlisted, this);

      // All enlisted resources must be in suspended mode
      for (Integer state : enlisted.values())
      {
         if (state.intValue() != XAResource.TMNOFLAGS && state.intValue() != XAResource.TMSUSPEND)
            return false;
      }

      return true;
   }

   /**
    * Check enlisted XAResources
    * @param status The transaction status
    * @exception IllegalStateException Thrown if there non-TMSUSPENDed XAResources
    */
   private void checkEnlisted(int status) throws IllegalStateException
   {
      // All ok, now move them into delisted
      int flag = XAResource.TMSUCCESS;

      if (status == Status.STATUS_MARKED_ROLLBACK ||
          status == Status.STATUS_ROLLING_BACK ||
          status == Status.STATUS_ROLLEDBACK)
         flag = XAResource.TMFAIL;

      for (XAResource xaRes : enlisted.keySet())
      {
         try
         {
            xaRes.end(XID_IMPL, flag);

            if (delisted == null)
               delisted = new HashSet<XAResource>();

            delisted.add(xaRes);
         }
         catch (XAException xe)
         {
            throw new IllegalStateException("XAResource can't end", xe);
         }
      }

      enlisted.clear();
   }
}
