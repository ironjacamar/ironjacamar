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

import org.jboss.jca.adapters.AdaptersLogger;
import org.jboss.jca.adapters.jdbc.spi.ClassLoaderPlugin;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.resource.ResourceException;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * A wrapper for a connection.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @version $Revision: 96595 $
 */
public abstract class WrappedConnection extends JBossWrapper implements Connection
{
   private static AdaptersLogger log = Logger.getMessageLogger(AdaptersLogger.class, WrappedConnection.class.getName());

   /** The spy logger */
   protected static Logger spyLogger = Logger.getLogger(Constants.SPY_LOGGER_CATEGORY);

   private volatile BaseWrapperManagedConnection mc;
   private BaseWrapperManagedConnection lockedMC;
   private int lockCount;

   private WrapperDataSource dataSource;

   private HashMap<WrappedStatement, Throwable> statements;

   private boolean closed = false;

   private int trackStatements;

   /** Spy functionality */
   protected boolean spy = false;

   /** The jndi name */
   protected String jndiName = null;

   /** Do locking */
   protected final boolean doLocking;
   
   private final ClassLoaderPlugin classLoaderPlugin;

   private Optional<MethodHandle> requestBegin,requestEnd;
   /**
    * Constructor
    * @param mc The managed connection
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    */
   public WrappedConnection(final BaseWrapperManagedConnection mc, boolean spy, String jndiName,
                            final boolean doLocking, ClassLoaderPlugin classLoaderPlugin)
   {
      setManagedConnection(mc);
      setSpy(spy);
      setJndiName(jndiName);
      this.doLocking = doLocking;
      this.classLoaderPlugin = classLoaderPlugin;

      sqlConnectionNotifyRequestBegin();
      
   }

   /**
    * Set the managed connection
    * @param mc The managed connection
    */
   void setManagedConnection(final BaseWrapperManagedConnection mc)
   {
      this.mc = mc;

      if (mc != null)
      {
         trackStatements = mc.getTrackStatements();
         // This will only work because JDBC wrapped connections are not returned to a pool;
         // only the mc inside the WrappedConnection is returned to the pool.
         // That means the only moment this method is called with a non-null mc is
         // during WrappedConnection creation
         if (lockCount > 0) {
            throw new IllegalStateException(bundle.wrappedConnectionInUse());
         }
      }
      else
      {
         // do not reset lockedMC reference once the connection is returned to the pool (JBJCA-1367)
         closed = true;
      }
   }

   /**
    * Set the spy value
    * @param v The value
    */
   void setSpy(boolean v)
   {
      this.spy = v;
   }

   /**
    * Set the jndi name value
    * @param v The value
    */
   void setJndiName(String v)
   {
      this.jndiName = v;
   }

   /**
    * Lock connection
    * @exception SQLException Thrown if an error occurs
    */
   protected void lock() throws SQLException
   {
      BaseWrapperManagedConnection mc = this.mc;
      if (mc != null)
      {
         mc.tryLock();
         if (lockedMC == null)
            lockedMC = mc;

         lockCount++;
      }
      else
      {
         throw new SQLException(bundle.connectionNotAssociated(this.toString()));
      }
   }

   /**
    * Unlock connection
    */
   protected void unlock()
   {
      BaseWrapperManagedConnection mc = this.lockedMC;
      if (--lockCount == 0)
         lockedMC = null;

      if (mc != null)
         mc.unlock();
   }

   /**
    * Get the datasource
    * @return The value
    */
   public WrapperDataSource getDataSource()
   {
      return dataSource;
   }

   /**
    * Set the datasource
    * @param dataSource The value
    */
   protected void setDataSource(WrapperDataSource dataSource)
   {
      this.dataSource = dataSource;
   }

   /**
    * {@inheritDoc}
    */
   public void setReadOnly(boolean readOnly) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkStatus();

         if (spy)
            spyLogger.debugf("%s [%s] setReadOnly(%s)",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, readOnly);

         mc.setJdbcReadOnly(readOnly);
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isReadOnly() throws SQLException
   {
      checkStatus();

      if (spy)
         spyLogger.debugf("%s [%s] isReadOnly()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

      return mc.isJdbcReadOnly();
   }

   /**
    * Invalidate a connection
    * @exception SQLException if a database access error occurs
    */
   public void invalidate() throws SQLException
   {
      if (spy)
         spyLogger.debugf("%s [%s] invalidate()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

      returnConnection(true);
   }

   /**
    * {@inheritDoc}
    */
   public void close() throws SQLException
   {
      if (spy)
         spyLogger.debugf("%s [%s] close()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

      returnConnection(false);
   }

   /**
    * {@inheritDoc}
    */
   private void returnConnection(boolean error) throws SQLException
   {
      closed = true;

      if (mc != null)
      {
         if (trackStatements != BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         {
            synchronized (this)
            {
               if (statements != null && statements.size() > 0)
               {
                  for (Iterator<Map.Entry<WrappedStatement, Throwable>> i = statements.entrySet().iterator();
                       i.hasNext();)
                  {
                     Map.Entry<WrappedStatement, Throwable> entry = i.next();
                     WrappedStatement ws = entry.getKey();
                     if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_TRUE_INT)
                     {
                        Throwable stackTrace = entry.getValue();
                        log.closingStatement(jndiName, stackTrace);
                     }
                     try
                     {
                        ws.internalClose();
                     }
                     catch (Throwable t)
                     {
                        log.errorDuringClosingStatement(jndiName, t);
                     }
                  }
               }
            }
         }
         if (!error)
         {
            mc.closeHandle(this);
         }
         else
         {
            mc.errorHandle(this);
         }
      }
      mc = null;
      dataSource = null;

      sqlConnectionNotifyRequestEnd();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isClosed() throws SQLException
   {
      if (spy)
         spyLogger.debugf("%s [%s] isClosed()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

      return closed;
   }

   /**
    * Wrap statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    * @return The wrapped statement
    */
   protected abstract WrappedStatement wrapStatement(Statement statement, boolean spy, String jndiName,
                                                     boolean doLocking);

   /**
    * {@inheritDoc}
    */
   public Statement createStatement() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] createStatement()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            Statement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<Statement>() {
               public Statement produce() throws Exception {
                  return mc.getRealConnection().createStatement();
               }
            });

            return wrapStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] createStatement(%s, %s)", 
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                resultSetType, resultSetConcurrency);

            Statement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<Statement>() {
               public Statement produce() throws Exception {
                  return mc.getRealConnection().createStatement(resultSetType, resultSetConcurrency);
               }
            });
            return wrapStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] createStatement(%s, %s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                resultSetType, resultSetConcurrency, resultSetHoldability);

            Statement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<Statement>() {
               public Statement produce() throws Exception {
                  return mc.getRealConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
               }
            });
            return wrapStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * Wrap a prepared statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    * @return The wrapped prepared statement
    */
   protected abstract WrappedPreparedStatement wrapPreparedStatement(PreparedStatement statement,
                                                                     boolean spy, String jndiName,
                                                                     boolean doLocking);

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(String sql) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] prepareStatement(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, sql);

            PreparedStatement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<PreparedStatement>() {
               public PreparedStatement produce() throws Exception {
                  return mc.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY);
               }
            });
            return wrapPreparedStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] prepareStatement(%s, %s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                sql, resultSetType, resultSetConcurrency);

            PreparedStatement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<PreparedStatement>() {
               public PreparedStatement produce() throws Exception {
                  return mc.prepareStatement(sql, resultSetType, resultSetConcurrency);
               }
            });
            return wrapPreparedStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
         int resultSetHoldability) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] prepareStatement(%s, %s, %s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                sql, resultSetType, resultSetConcurrency, resultSetHoldability);

            PreparedStatement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<PreparedStatement>() {
               public PreparedStatement produce() throws Exception {
                  return mc.getRealConnection().prepareStatement(sql, resultSetType,
                        resultSetConcurrency, resultSetHoldability);
               }
            });
            return wrapPreparedStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] prepareStatement(%s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                sql, autoGeneratedKeys);

            PreparedStatement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<PreparedStatement>() {
               public PreparedStatement produce() throws Exception {
                  return mc.getRealConnection().prepareStatement(sql, autoGeneratedKeys);
               }
            });
            return wrapPreparedStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] prepareStatement(%s, %s)"
                                , jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                sql, Arrays.toString(columnIndexes));

            PreparedStatement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<PreparedStatement>() {
               public PreparedStatement produce() throws Exception {
                  return mc.getRealConnection().prepareStatement(sql, columnIndexes);
               }
            });
            return wrapPreparedStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] prepareStatement(%s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                sql, Arrays.toString(columnNames));

            PreparedStatement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<PreparedStatement>() {
               public PreparedStatement produce() throws Exception {
                  return mc.getRealConnection().prepareStatement(sql, columnNames);
               }
            });
            return wrapPreparedStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * Wrap a callable statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    * @return The wrapped callable statement
    */
   protected abstract WrappedCallableStatement wrapCallableStatement(CallableStatement statement,
                                                                     boolean spy, String jndiName,
                                                                     boolean doLocking);

   /**
    * {@inheritDoc}
    */
   public CallableStatement prepareCall(String sql) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] prepareCall(%s)", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, sql);

            CallableStatement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<CallableStatement>() {
               public CallableStatement produce() throws Exception {
                  return mc.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
               }
            });
            return wrapCallableStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] prepareCall(%s, %s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                sql, resultSetType, resultSetConcurrency);

            CallableStatement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<CallableStatement>() {
               public CallableStatement produce() throws Exception {
                  return mc.prepareCall(sql, resultSetType, resultSetConcurrency);
               }
            });
            return wrapCallableStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                        int resultSetHoldability) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] prepareCall(%s, %s, %s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                sql, resultSetType, resultSetConcurrency, resultSetHoldability);

            CallableStatement stmt = SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<CallableStatement>() {
               public CallableStatement produce() throws Exception {
                  return mc.getRealConnection()
                        .prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
               }
            });
            return wrapCallableStatement(stmt, spy, jndiName, doLocking);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public String nativeSQL(String sql) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] nativeSQL(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, sql);

            return SecurityActions.executeInTccl(classLoaderPlugin.getClassLoader(), new SecurityActions.Producer<String>() {
               public String produce() throws Exception {
                  return mc.getRealConnection().nativeSQL(sql);
               }
            });
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setAutoCommit(boolean autocommit) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkStatus();

         if (spy)
            spyLogger.debugf("%s [%s] setAutoCommit(%s)",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, autocommit);

         mc.setJdbcAutoCommit(autocommit);
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean getAutoCommit() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkStatus();

         if (spy)
            spyLogger.debugf("%s [%s] getAutoCommit()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

         return mc.isJdbcAutoCommit();
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void commit() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] commit()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            mc.jdbcCommit();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void rollback() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] rollback()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            mc.jdbcRollback();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Savepoint savepoint) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] rollback(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, savepoint);

            mc.jdbcRollback(savepoint);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public DatabaseMetaData getMetaData() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getMetaData()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().getMetaData();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setCatalog(String catalog) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setCatalog(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, catalog);

            mc.getRealConnection().setCatalog(catalog);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getCatalog() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getCatalog()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().getCatalog();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setTransactionIsolation(int isolationLevel) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkStatus();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setTransactionIsolation(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, isolationLevel);

            mc.setJdbcTransactionIsolation(isolationLevel);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getTransactionIsolation() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkStatus();

         if (spy)
            spyLogger.debugf("%s [%s] getTransactionIsolation()",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

         return mc.getJdbcTransactionIsolation();
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public SQLWarning getWarnings() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getWarnings()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().getWarnings();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void clearWarnings() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] clearWarnings()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            mc.getRealConnection().clearWarnings();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Map<String, Class<?>> getTypeMap() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getTypeMap()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().getTypeMap();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void setTypeMap(Map<String, Class<?>> typeMap) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setTypeMap(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, typeMap);

            mc.getRealConnection().setTypeMap(typeMap);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setHoldability(int holdability) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setHoldability(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, holdability);

            mc.getRealConnection().setHoldability(holdability);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getHoldability() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getHoldability()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().getHoldability();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Savepoint setSavepoint() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setSavepoint()", jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().setSavepoint();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Savepoint setSavepoint(String name) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setSavepoint(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, name);

            return mc.getRealConnection().setSavepoint(name);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void releaseSavepoint(Savepoint savepoint) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] releaseSavepoint(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION, savepoint);

            mc.getRealConnection().releaseSavepoint(savepoint);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }


   /**
    * {@inheritDoc}
    */
   public Array createArrayOf(String typeName, Object[] elements) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] createArrayOf(%s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                typeName, Arrays.toString(elements));

            return mc.getRealConnection().createArrayOf(typeName, elements);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Blob createBlob() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] createBlob()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().createBlob();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Clob createClob() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] createClob()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().createClob();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public NClob createNClob() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] createNClob()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().createNClob();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public SQLXML createSQLXML() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] createSQLXML()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().createSQLXML();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Struct createStruct(String typeName, Object[] attributes) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] createStruct(%s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                typeName, Arrays.toString(attributes));

            return mc.getRealConnection().createStruct(typeName, attributes);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Properties getClientInfo() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getClientInfo()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().getClientInfo();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getClientInfo(String name) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getClientInfo(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                name);

            return mc.getRealConnection().getClientInfo(name);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isValid(int timeout) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkStatus();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] isValid(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                timeout);

            return mc.getRealConnection().isValid(timeout);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setClientInfo(Properties properties) throws SQLClientInfoException
   {
      try
      {
         if (doLocking)
            lock();
         try
         {
            checkTransaction();
            try
            {
               if (spy)
                  spyLogger.debugf("%s [%s] setClientInfo(%s)",
                                   jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                   properties);

               mc.getRealConnection().setClientInfo(properties);
            }
            catch (Throwable t)
            {
               try
               {
                  checkException(t);
               }
               catch (SQLClientInfoException e)
               {
                  throw e;
               }
               catch (SQLException e)
               {
                  SQLClientInfoException scie = new SQLClientInfoException();
                  scie.initCause(e);
                  throw scie;
               }
            }
         }
         finally
         {
            if (doLocking)
               unlock();
         }
      }
      catch (SQLClientInfoException e)
      {
         throw e;
      }
      catch (SQLException e)
      {
         SQLClientInfoException t = new SQLClientInfoException();
         t.initCause(e);
         throw t;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setClientInfo(String name, String value) throws SQLClientInfoException
   {
      try
      {
         if (doLocking)
            lock();
         try
         {
            checkTransaction();
            try
            {
               if (spy)
                  spyLogger.debugf("%s [%s] setClientInfo(%s, %s)",
                                   jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                   name, value);

               mc.getRealConnection().setClientInfo(name, value);
            }
            catch (Throwable t)
            {
               try
               {
                  checkException(t);
               }
               catch (SQLClientInfoException e)
               {
                  throw e;
               }
               catch (SQLException e)
               {
                  SQLClientInfoException scie = new SQLClientInfoException();
                  scie.initCause(e);
                  throw scie;
               }
            }
         }
         finally
         {
            if (doLocking)
               unlock();
         }
      }
      catch (SQLClientInfoException e)
      {
         throw e;
      }
      catch (SQLException e)
      {
         SQLClientInfoException t = new SQLClientInfoException();
         t.initCause(e);
         throw t;
      }
   }


   /**
    * {@inheritDoc}
    */
   public void setSchema(String schema) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setSchema(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                schema);

            mc.getRealConnection().setSchema(schema);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getSchema() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getSchema()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().getSchema();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void abort(Executor executor) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] abort(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                executor);

            mc.getRealConnection().abort(executor);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setNetworkTimeout(%s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                executor, milliseconds);

            mc.getRealConnection().setNetworkTimeout(executor, milliseconds);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getNetworkTimeout() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getNetworkTimeout()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return mc.getRealConnection().getNetworkTimeout();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Connection getUnderlyingConnection() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         return mc.getRealConnection();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * Returns true if the underlying connection is handled by an XA resource manager
    * @return The value
    */
   public boolean isXA()
   {
      return mc.isXA();
   }

   /**
    * Returns the XAResource if the connection is an XA based one
    * @return The value
    * @exception ResourceException Thrown if it isn't an XA based connection
    */
   public XAResource getXAResource() throws ResourceException
   {
      return mc.getXAResource();
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      checkStatus();
      return super.isWrapperFor(iface);
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      checkStatus();
      mc.checkTransaction();
      return super.unwrap(iface);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Connection getWrappedObject() throws SQLException
   {
      return mc.getRealConnection();
   }

   /**
    * {@inheritDoc}
    */
   protected void checkTransaction() throws SQLException
   {
      checkStatus();
      mc.checkTransaction();
   }

   /**
    * {@inheritDoc}
    */
   void checkTransactionActive() throws SQLException
   {
      if (dataSource == null)
         return;
      dataSource.checkTransactionActive();
   }

   /**
    * The checkStatus method checks that the handle has not been closed and
    * that it is associated with a managed connection.
    *
    * @exception SQLException if an error occurs
    */
   protected void checkStatus() throws SQLException
   {
      if (closed)
         throw new SQLException(bundle.connectionClosed());
      if (mc == null)
         throw new SQLException(bundle.connectionNotAssociated(this.toString()));
      checkTransactionActive();
   }

   /**
    * The base checkException method rethrows the supplied exception, informing
    * the ManagedConnection of the error. Subclasses may override this to
    * filter exceptions based on their severity.
    *
    * @param t a throwable
    * @return the sql exception
    * @exception SQLException if an error occurs
    */
   protected SQLException checkException(Throwable t) throws SQLException
   {
      Throwable result = t;
      if (result instanceof AbstractMethodError)
      {
         result = new SQLFeatureNotSupportedException(bundle.methodNotImplemented(), result);
      }

      if (mc != null)
         result = mc.connectionError(result);

      if (result instanceof SQLException)
      {
         throw (SQLException) result;
      }
      else
      {
         throw new SQLException("Error", result);
      }

   }

   /**
    * Get the track statement status
    * @return The value
    */
   int getTrackStatements()
   {
      return trackStatements;
   }

   /**
    * Register a statement
    * @param ws The statement
    */
   void registerStatement(WrappedStatement ws)
   {
      if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         return;

      synchronized (this)
      {
         if (statements == null)
            statements = new HashMap<WrappedStatement, Throwable>(1);

         if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_TRUE_INT)
            statements.put(ws, new Throwable("STACKTRACE"));
         else
            statements.put(ws, null);
      }
   }

   /**
    * Unregister a statement
    * @param ws The statement
    */
   void unregisterStatement(WrappedStatement ws)
   {
      if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         return;
      synchronized (this)
      {
         if (statements != null)
            statements.remove(ws);
      }
   }

   /**
    * Check configured query timeout
    * @param ws The statement
    * @param explicitTimeout An explicit timeout value set
    * @exception SQLException Thrown if an error occurs
    */
   void checkConfiguredQueryTimeout(WrappedStatement ws, int explicitTimeout) throws SQLException
   {
      if (mc == null || dataSource == null)
         return;

      int timeout = 0;

      // Use the transaction timeout
      if (mc.isTransactionQueryTimeout())
      {
         timeout = dataSource.getTimeLeftBeforeTransactionTimeout();
         if (timeout > 0 && explicitTimeout > 0 && timeout > explicitTimeout)
            timeout = explicitTimeout;
      }

      // Look for a configured value
      if (timeout <= 0 && explicitTimeout <= 0)
         timeout = mc.getQueryTimeout();

      if (timeout > 0)
         ws.setQueryTimeout(timeout);
   }

   /**
    * Get the logger
    * @return The value
    */
   AdaptersLogger getLogger()
   {
      return log;
   }

   private void sqlConnectionNotifyRequestBegin()
   {
      if (requestBegin == null)
      {
         requestBegin = lookupNotifyMethod("beginRequest");
      }
      if (requestBegin.isPresent())
         invokeNotifyMethod(requestBegin.get(), "beginRequest");
   }

   private void sqlConnectionNotifyRequestEnd()
   {
      if (requestEnd == null)
      {
         requestEnd = lookupNotifyMethod("endRequest");
      }
      if (requestEnd.isPresent())
         invokeNotifyMethod(requestEnd.get(), "endRequest");
   }

   private Optional<MethodHandle> lookupNotifyMethod(String methodName)
   {
      try {
         Class<?> sqlConnection = getSqlConnection();
         MethodHandle mh = SecurityActions.getMethodHandle(sqlConnection, methodName);
         if (mh == null)
            return Optional.empty();
         else
            return Optional.of(mh);
      } catch (SQLException e)
      {
         if (spy)
            spyLogger.debugf("Unable to invoke java.sql.Connection#%s: %s", methodName, e.getMessage());
         return Optional.empty();
      }
   }

   private void invokeNotifyMethod(MethodHandle mh, String methodName)
   {
      try
      {
         mh.invokeExact(mc.getRealConnection());
         if (spy)
            spyLogger.debugf("java.sql.Connection#%s has been invoked", methodName);
      } catch (Throwable t)
      {
         if (spy)
            spyLogger.debugf("Unable to invoke java.sql.Connection#%s: %s", methodName, t.getMessage());
      }
   }

   private Class<?> getSqlConnection() throws SQLException
   {
      Class<?> sqlConnection = null;

      if (sqlConnection == null)
      {
         try
         {
            sqlConnection = Class.forName("java.sql.Connection", true,
                    SecurityActions.getClassLoader(getClass()));
         } catch (Throwable t)
         {
            // Ignore
         }
      }

      if (sqlConnection == null)
      {
         try
         {
            ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
            sqlConnection = Class.forName("java.sql.Connection", true, tccl);
         } catch (Throwable t)
         {
            throw new SQLException("Cannot resolve java.sql.Connection", t);
         }
      }
      return sqlConnection;
   }
}
