/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.resource.ResourceException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.spi.transaction.ConnectableResource;
import org.jboss.jca.core.spi.transaction.ConnectableResourceListener;
import org.jboss.jca.core.spi.transaction.local.LocalXAException;
import org.jboss.logging.Messages;

/**
 * Local connectable XA resource implementation.
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LocalConnectableXAResourceImpl extends LocalXAResourceImpl 
   implements ConnectableResource, org.jboss.tm.ConnectableResource
{    
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
    
   /** Connectable resource */
   private ConnectableResource cr;
   
   /** The connectable resource listener */
   private ConnectableResourceListener crl;
   
   // Oracle: 08000 (No more data to read from socket)
   // DB2: 08001 (Insufficient data. ERRORCODE=-4499, SQLSTATE=08001)
   // Sybase: JZ006 (Connection is already closed.)
   // Postgres: 08006 (An I/O error occurred while sending to the backend.)
   // Mysql: 08007 (Transaction resolution unknown.)
   // Mssql: 08S01 (Server did not return a response. The connection has been closed.) 
   private static final List<String> RMFAIL_SQL_ERRORCODES = Arrays.asList(new String[] {"08000", "08001", "JZ006", "08006", "08007", "08S01"});

   /**
    * Creates a new instance.
    * @param productName product name
    * @param productVersion product version
    * @param jndiName jndi name
    * @param cr connectable resource
    */
   public LocalConnectableXAResourceImpl(String productName, String productVersion,
                                         String jndiName, ConnectableResource cr)
   {
      super(productName, productVersion, jndiName);
      this.cr = cr;
      this.crl = null;
   }

   /**
    * {@inheritDoc}
    */
   public Object getConnection() throws Exception
   {
      Object result = cr.getConnection();

      if (crl != null)
         crl.handleCreated(result);

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public void setConnectableResourceListener(ConnectableResourceListener crl)
   {
      this.crl = crl;
   }
   
   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      if (!xid.equals(currentXid))
      {
         throw new LocalXAException(bundle.wrongXidInCommit(currentXid, xid), XAException.XAER_PROTO);
         
      }
      
      currentXid = null;

      try
      {
         cl.getManagedConnection().getLocalTransaction().commit();
      }
      catch (ResourceException re)
      {
         connectionManager.returnManagedConnection(cl, true);
         Throwable cause = re.getCause();
         if (cause instanceof SQLException) {
             SQLException sqle = (SQLException) cause;
             String sqlState = sqle.getSQLState();
             if (RMFAIL_SQL_ERRORCODES.contains(sqlState)) {
                 throw new LocalXAException(bundle.couldNotCommitLocalTx(), XAException.XAER_RMFAIL, re);
             }
         }
         throw new LocalXAException(bundle.couldNotCommitLocalTx(), XAException.XA_RBROLLBACK, re);
      }
   }   
}
