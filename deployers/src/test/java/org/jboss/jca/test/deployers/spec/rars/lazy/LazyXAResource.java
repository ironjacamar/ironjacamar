/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.deployers.spec.rars.lazy;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * Lazy XAResource
 */
public class LazyXAResource implements XAResource
{
   /** The logger */
   private static Logger log = Logger.getLogger(LazyXAResource.class);

   /**
    * Constructor
    */
   public LazyXAResource()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      log.tracef("commit(%s, %s)", xid, onePhase);
   }

   /**
    * {@inheritDoc}
    */
   public void end(Xid xid, int flags) throws XAException
   {
      log.tracef("end(%s, %s)", xid, flags);
   }

   /**
    * {@inheritDoc}
    */
   public void forget(Xid xid) throws XAException
   {
      log.tracef("forget(%s)", xid);
   }

   /**
    * {@inheritDoc}
    */
   public int getTransactionTimeout() throws XAException
   {
      log.tracef("getTransactionTimeout()");
      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSameRM(XAResource xares) throws XAException
   {
      log.tracef("isSameRM(%s)", xares);
      
      if (xares != null)
         return xares instanceof LazyXAResource;

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public int prepare(Xid xid) throws XAException
   {
      log.tracef("prepare(%s)", xid);
      return XAResource.XA_OK;
   }

   /**
    * {@inheritDoc}
    */
   public Xid[] recover(int flag) throws XAException
   {
      log.tracef("recover(%s)", flag);
      return new Xid[] {};
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
      log.tracef("rollback(%s)", xid);
   }

   /**
    * {@inheritDoc}
    */
   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      log.tracef("setTransactionTimeout(%s)", seconds);
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public void start(Xid xid, int flags) throws XAException
   {
      log.tracef("start(%s, %s)", xid, flags);
   }
}
