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

package org.jboss.jca.adapters.jdbc.deadlock;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * Connection close XAResource
 */
class ConnectionCloseXAResource implements XAResource
{
   private static Logger log = Logger.getLogger(ConnectionCloseXAResource.class);
   private Connection connection;
   private int timeout;

   /**
    * Constructor
    * @param connection The connection
    */
   public ConnectionCloseXAResource(Connection connection)
   {
      this.connection = connection;
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean arg1) throws XAException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void end(Xid xid, int arg1) throws XAException
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
   public int getTransactionTimeout() throws XAException
   {
      return timeout;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSameRM(XAResource xaResource) throws XAException
   {
      return (xaResource == this);
   }

   /**
    * {@inheritDoc}
    */
   public int prepare(Xid xid) throws XAException
   {
      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public Xid[] recover(int flag) throws XAException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
      log.info("Closing connection through rollback");
      try
      {
         connection.close();
         log.info("Closed connection through rollback");
      }
      catch (SQLException se)
      {
         log.error("Unexpected error closing exception " + se.getMessage(), se);
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean setTransactionTimeout(int timeout) throws XAException
   {
      this.timeout = timeout;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public void start(Xid xid, int flags) throws XAException
   {
   }
}
