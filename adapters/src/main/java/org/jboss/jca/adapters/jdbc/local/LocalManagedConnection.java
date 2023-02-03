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

package org.jboss.jca.adapters.jdbc.local;

import org.jboss.jca.adapters.jdbc.BaseWrapperManagedConnection;
import org.jboss.jca.core.spi.transaction.local.LocalResourceException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.LocalTransaction;
import javax.transaction.xa.XAResource;

/**
 * LocalManagedConnection
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @version $Revision: 71788 $
 */
public class LocalManagedConnection extends BaseWrapperManagedConnection implements LocalTransaction
{

   /**
    * Constructor
    * @param mcf The managed connection factory
    * @param con The connection
    * @param props The properties
    * @param transactionIsolation The transaction isolation
    * @param psCacheSize The prepared statement cache size
    * @exception SQLException Thrown if an error occurs
    */
   public LocalManagedConnection(final LocalManagedConnectionFactory mcf,
                                 final Connection con, 
                                 final Properties props,
                                 final int transactionIsolation,
                                 final int psCacheSize)
      throws SQLException
   {
      super(mcf, con, props, transactionIsolation, psCacheSize);
   }

   /**
    * {@inheritDoc}
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      return this;
   }

   /**
    * {@inheritDoc}
    */
   public XAResource getXAResource() throws ResourceException
   {
      throw new ResourceException(bundle.localTransactionOnly());
   }

   /**
    * {@inheritDoc}
    */
   public void commit() throws ResourceException
   {
      lock();
      try
      {
         synchronized (stateLock)
         {
            if (inManagedTransaction)
               inManagedTransaction = false;
         }
         try
         {
            con.commit();
         }
         catch (SQLException e)
         {
            if (mcf.isExceptionFatal(e))
            {
               broadcastConnectionError(e);
               throw new LocalResourceException(e.getMessage(), e);
            }
            else
            {
               checkException(e);
            }
         }
      }
      finally
      {
         unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void rollback() throws ResourceException
   {
      lock();
      try
      {
         synchronized (stateLock)
         {
            if (inManagedTransaction)
               inManagedTransaction = false;
         }
         try
         {
            con.rollback();
         }
         catch (SQLException e)
         {
            if (mcf.isExceptionFatal(e))
            {
               broadcastConnectionError(e);
               throw new LocalResourceException(e.getMessage(), e);
            }
            else
            {
               try
               {
                  checkException(e);
               }
               catch (Exception e2)
               {
                  // We are ignoring since we just need the notification
               }
            }
         }
      }
      finally
      {
         unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void begin() throws ResourceException
   {
      lock();
      try
      {
         synchronized (stateLock)
         {
            if (!inManagedTransaction)
            {
               try
               {
                  if (underlyingAutoCommit)
                  {
                     underlyingAutoCommit = false;
                     con.setAutoCommit(false);
                  }
                  checkState();
                  inManagedTransaction = true;
               }
               catch (SQLException e)
               {
                  checkException(e);
               }
            }
            else
               throw new ResourceException(bundle.localTransactionNested());
         }
      }
      finally
      {
         unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isXA()
   {
      return false;
   }
}
