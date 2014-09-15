/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

import org.jboss.jca.core.spi.transaction.XAResourceStatistics;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

/**
 * Local XA resource implementation with statistics
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LocalXAResourceStatImpl extends LocalXAResourceImpl
{
   private XAResourceStatistics xastat;

   /**
    * Creates a new instance.
    * @param productName product name
    * @param productVersion product version
    * @param jndiName jndi name
    * @param xastat The statistics
    */
   public LocalXAResourceStatImpl(String productName, String productVersion,
                                  String jndiName, XAResourceStatistics xastat)
   {
      super(productName, productVersion, jndiName);
      this.xastat = xastat;
   }

   /**
    * {@inheritDoc}
    */
   public void start(Xid xid, int flags) throws XAException
   {
      long l1 = System.currentTimeMillis();
      try
      {
         super.start(xid, flags);
      }
      finally
      {
         xastat.deltaStart(System.currentTimeMillis() - l1);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      long l1 = System.currentTimeMillis();
      try
      {
         super.commit(xid, onePhase);
      }
      finally
      {
         xastat.deltaCommit(System.currentTimeMillis() - l1);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
      long l1 = System.currentTimeMillis();
      try
      {
         super.rollback(xid);
      }
      finally
      {
         xastat.deltaRollback(System.currentTimeMillis() - l1);
      }
   }
}
