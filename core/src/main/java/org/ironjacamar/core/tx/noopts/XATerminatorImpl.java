/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.tx.noopts;

import org.ironjacamar.core.spi.transaction.xa.XATerminator;

import java.io.Serializable;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * An XATerminator implementation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XATerminatorImpl implements XATerminator, Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    */
   public XATerminatorImpl()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void forget(Xid xid) throws XAException
   {
   }

   /**
    * {@inheritDoc}
    */
   public int prepare(Xid xid) throws XAException
   {
      return XAResource.XA_OK;
   }

   /**
    * {@inheritDoc}
    */
   public Xid[] recover(int flag) throws XAException
   {
      return new Xid[0];
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void registerWork(Work work, Xid xid, long timeout) throws WorkCompletedException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void startWork(Work work, Xid xid) throws WorkCompletedException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void endWork(Work work, Xid xid)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void cancelWork(Work work, Xid xid)
   {
   }
}
