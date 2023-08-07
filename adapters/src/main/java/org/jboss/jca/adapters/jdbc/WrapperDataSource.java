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

package org.jboss.jca.adapters.jdbc;

import org.jboss.jca.adapters.jdbc.spi.ClassLoaderPlugin;
import org.jboss.jca.core.spi.transaction.TransactionTimeoutConfiguration;
import org.jboss.jca.core.spi.transaction.TxUtils;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransactionRollbackException;

import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.Status;

import org.jboss.logging.Logger;

/**
 * WrapperDataSource
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @version $Revision: 71788 $
 */
public class WrapperDataSource extends JBossWrapper implements Referenceable, DataSource, Serializable
{
   private static final long serialVersionUID = 3570285419164793501L;

   private static Logger spyLogger = Logger.getLogger(Constants.SPY_LOGGER_CATEGORY);

   private final BaseWrapperManagedConnectionFactory mcf;
   private final ConnectionManager cm;

   private PrintWriter logger;
   private Reference reference;

   private ConnectionRequestInfo defaultCRI;

   private final ClassLoaderPlugin classLoaderPlugin;

   /**
    * Constructor
    * @param mcf The managed connection factory
    * @param cm The connection manager
    */
   protected WrapperDataSource(BaseWrapperManagedConnectionFactory mcf,
         ConnectionManager cm, ClassLoaderPlugin classLoaderPlugin) {
      this.mcf = mcf;
      this.cm = cm;
      this.classLoaderPlugin = classLoaderPlugin;
      
      if (mcf.getUserName() != null)
      {
         this.defaultCRI = new WrappedConnectionRequestInfo(mcf.getUserName(), mcf.getPassword());
      }
      else
      {
         this.defaultCRI = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public PrintWriter getLogWriter() throws SQLException
   {
      return logger;
   }

   /**
    * {@inheritDoc}
    */
   public void setLogWriter(PrintWriter pw) throws SQLException
   {
      logger = pw;
   }

   /**
    * {@inheritDoc}
    */
   public int getLoginTimeout() throws SQLException
   {
      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public void setLoginTimeout(int param1) throws SQLException
   {
   }

   /**
    * {@inheritDoc}
    */
   public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      throw new SQLFeatureNotSupportedException();
   }

   /**
    * {@inheritDoc}
    */
   public Connection getConnection() throws SQLException
   {
      ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
      mcf.setOriginalTCCLn(tccl);
      try
      {
         try {
            SecurityActions.setThreadContextClassLoader(classLoaderPlugin.getClassLoader());
         } catch(Throwable t)
         {
            //just ignore

         }
         if (mcf.getSpy().booleanValue())
            spyLogger.debugf("%s [%s] getConnection()",
                             mcf.getJndiName(), Constants.SPY_LOGGER_PREFIX_DATASOURCE);

         WrappedConnection wc = (WrappedConnection) cm.allocateConnection(mcf, defaultCRI);
         wc.setDataSource(this);
         wc.setSpy(mcf.getSpy().booleanValue());
         wc.setJndiName(mcf.getJndiName());
         return wc;
      }
      catch (ResourceException re)
      {
         throw new SQLException(re);
      }
      finally
      {
         mcf.setOriginalTCCLn(null); // fix for WFLY-12676 memory leak
         SecurityActions.setThreadContextClassLoader(tccl);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Connection getConnection(String user, String password) throws SQLException
   {
      ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
      ConnectionRequestInfo cri = new WrappedConnectionRequestInfo(user, password);
      try
      {
         SecurityActions.setThreadContextClassLoader(classLoaderPlugin.getClassLoader());
         if (mcf.getSpy().booleanValue())
            spyLogger.debugf("%s [%s] getConnection(%s, ****)",
                             mcf.getJndiName(), Constants.SPY_LOGGER_PREFIX_DATASOURCE, user);

         WrappedConnection wc = (WrappedConnection) cm.allocateConnection(mcf, cri);
         wc.setDataSource(this);
         wc.setSpy(mcf.getSpy().booleanValue());
         wc.setJndiName(mcf.getJndiName());
         return wc;
      }
      catch (ResourceException re)
      {
         throw new SQLException(re);
      }
      finally
      {
         SecurityActions.setThreadContextClassLoader(tccl);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setReference(final Reference reference)
   {
      this.reference = reference;
   }

   /**
    * {@inheritDoc}
    */
   public Reference getReference()
   {
      return reference;
   }

   /**
    * Get the time left before a transaction timeout
    * @return The amount in seconds; <code>-1</code> if no timeout
    * @exception SQLException Thrown if an error occurs
    */
   protected int getTimeLeftBeforeTransactionTimeout() throws SQLException
   {
      try
      {
         if (cm instanceof TransactionTimeoutConfiguration)
         {
            long timeout = ((TransactionTimeoutConfiguration) cm).getTimeLeftBeforeTransactionTimeout(true);
            // No timeout
            if (timeout == -1)
               return -1;
            // No remaining transaction timeout. This is a very rare case but possible to happen.
            if (timeout == 0)
               throw new SQLTimeoutException(bundle.transactionCannotProceed("No remaining transaction timeout"));
            // Round up to the nearest second
            long result = timeout / 1000;
            if ((timeout % 1000) != 0)
               ++result;
            return (int) result;
         }
         else
            return -1;
      }
      catch (RollbackException e)
      {
         throw new SQLTransactionRollbackException(e);
      }
   }

   /**
    * Check whether a tranasction is active
    *
    * @throws SQLException if the transaction is not active, preparing, prepared or committing or
    *                      for any error in the transaction manager
    */
   protected void checkTransactionActive() throws SQLException
   {
      if (!mcf.isJTA().booleanValue())
         return;

      try
      {
         int status = Status.STATUS_NO_TRANSACTION;

         if (mcf.getTransactionSynchronizationRegistry() != null)
            status = mcf.getTransactionSynchronizationRegistry().getTransactionStatus();

         if (status == Status.STATUS_NO_TRANSACTION)
            return;

         // Only allow states that will actually succeed
         if (status != Status.STATUS_ACTIVE && status != Status.STATUS_PREPARING &&
             status != Status.STATUS_PREPARED && status != Status.STATUS_COMMITTING)
         {
            String transactionCannotProceed = bundle.transactionCannotProceed(TxUtils.getStatusAsString(status));
            if (status == Status.STATUS_ROLLEDBACK) {
              throw new SQLTransactionRollbackException(transactionCannotProceed);
            } else {
              throw new SQLException(transactionCannotProceed);
            }
         }
      }
      catch (SQLException se)
      {
         throw se;
      }
      catch (Throwable t)
      {
         throw new SQLException(t.getMessage(), t);
      }
   }
}
