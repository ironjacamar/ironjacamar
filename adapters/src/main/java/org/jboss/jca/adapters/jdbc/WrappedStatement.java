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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.logging.Logger;

/**
 * A wrapper for a statement.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class WrappedStatement extends JBossWrapper implements Statement, StatementAccess
{
   /** The spy logger */
   protected static Logger spyLogger = Logger.getLogger(Constants.SPY_LOGGER_CATEGORY);
   private final WrappedConnection lc;
   private final Statement s;

   /** The result sets */
   private HashMap<WrappedResultSet, Throwable> resultSets;

   /** Whether we are closed */
   private AtomicBoolean closed = new AtomicBoolean(false);

   /** Spy functionality */
   protected final boolean spy;

   /** The jndi name */
   protected final String jndiName;

   /** The spy logging category */
   protected final String spyLoggingCategory;
   
   /** Default fetch size */
   protected static Integer defaultFetchSize = null;

   /** Timeout set */
   private int timeoutSet;

   /** Do locking */
   protected final boolean doLocking;
   
   static
   {
      String dfs = SecurityActions.getSystemProperty("ironjacamar.jdbc.defaultfetchsize");
      if (dfs != null)
         defaultFetchSize = Integer.valueOf(dfs);
   }

   /**
    * Constructor
    * @param lc The connection
    * @param s The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    */
   public WrappedStatement(final WrappedConnection lc, Statement s, final boolean spy, final String jndiName,
                           final boolean doLocking)
   {
      this(lc, s, spy, jndiName, doLocking, Constants.SPY_LOGGER_PREFIX_STATEMENT);
   }

   /**
    * Constructor
    * @param lc The connection
    * @param s The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    * @param spyLoggingCategory The spy logging category
    */
   protected WrappedStatement(final WrappedConnection lc, Statement s, final boolean spy, final String jndiName,
                              final boolean doLocking, final String spyLoggingCategory)
   {
      this.lc = lc;
      this.s = s;
      this.spy = spy;
      this.jndiName = jndiName;
      this.doLocking = doLocking;
      this.spyLoggingCategory = spyLoggingCategory;
      this.timeoutSet = 0;

      if (defaultFetchSize != null)
      {
         try
         {
            s.setFetchSize(defaultFetchSize.intValue());
         }
         catch (SQLException ignore)
         {
            // Ignore
         }
      }

      lc.registerStatement(this);
   }

   /**
    * Lock connection
    * @exception SQLException Thrown if an error occurs
    */
   protected void lock() throws SQLException
   {
      lc.lock();
   }

   /**
    * Unlock connection
    */
   protected void unlock()
   {
      lc.unlock();
   }

   /**
    * {@inheritDoc}
    */
   public void close() throws SQLException
   {
      if (closed.get())
         return;

      if (spy)
         spyLogger.debugf("%s [%s] close()", jndiName, spyLoggingCategory);
         
      closed.set(true);
      lc.unregisterStatement(this);
      internalClose();
   }

   /**
    * {@inheritDoc}
    */
   public boolean execute(String sql) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();

            if (spy)
               spyLogger.debugf("%s [%s] execute(%s)", jndiName, spyLoggingCategory, sql);
         
            return s.execute(sql);
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
   public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();

            if (spy)
               spyLogger.debugf("%s [%s] execute(%s, %s)",
                                jndiName, spyLoggingCategory, sql, autoGeneratedKeys);
         
            return s.execute(sql, autoGeneratedKeys);
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
   public boolean execute(String sql, int[] columnIndexes) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();

            if (spy)
               spyLogger.debugf("%s [%s] execute(%s, %s)",
                                jndiName, spyLoggingCategory,
                                sql, Arrays.toString(columnIndexes));
         
            return s.execute(sql, columnIndexes);
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
   public boolean execute(String sql, String[] columnNames) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();

            if (spy)
               spyLogger.debugf("%s [%s] execute(%s, %s)",
                                jndiName, spyLoggingCategory,
                                sql, Arrays.toString(columnNames));
         
            return s.execute(sql, columnNames);
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
   public Connection getConnection() throws SQLException
   {
      if (spy)
         spyLogger.debugf("%s [%s] getConnection()", jndiName, spyLoggingCategory);
         
      return lc;
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
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getWarnings()", jndiName, spyLoggingCategory);
         
            return s.getWarnings();
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
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] clearWarnings()", jndiName, spyLoggingCategory);
         
            s.clearWarnings();
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
   public ResultSet executeQuery(String sql) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();

            if (spy)
               spyLogger.debugf("%s [%s] executeQuery(%s)",
                                jndiName, spyLoggingCategory, sql);
         
            ResultSet result = s.executeQuery(sql);
            return registerResultSet(result);
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
   public int executeUpdate(String sql) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();

            if (spy)
               spyLogger.debugf("%s [%s] executeUpdate(%s)",
                                jndiName, spyLoggingCategory, sql);
         
            return s.executeUpdate(sql);
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
   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();

            if (spy)
               spyLogger.debugf("%s [%s] executeUpdate(%s, %s)",
                                jndiName, spyLoggingCategory, sql, autoGeneratedKeys);
         
            return s.executeUpdate(sql, autoGeneratedKeys);
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
   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();

            if (spy)
               spyLogger.debugf("%s [%s] executeUpdate(%s, %s)",
                                jndiName, spyLoggingCategory,
                                sql, Arrays.toString(columnIndexes));
         
            return s.executeUpdate(sql, columnIndexes);
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
   public int executeUpdate(String sql, String[] columnNames) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();

            if (spy)
               spyLogger.debugf("%s [%s] executeUpdate(%s, %s)",
                                jndiName, spyLoggingCategory,
                                sql, Arrays.toString(columnNames));
         
            return s.executeUpdate(sql, columnNames);
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
   public int getMaxFieldSize() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getMaxFieldSize()",
                                jndiName, spyLoggingCategory);
         
            return s.getMaxFieldSize();
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
   public void setMaxFieldSize(int max) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setMaxFieldSize(%s)",
                                jndiName, spyLoggingCategory, max);
         
            s.setMaxFieldSize(max);
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
   public int getMaxRows() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getMaxRows()", jndiName, spyLoggingCategory);
         
            return s.getMaxRows();
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
   public void setMaxRows(int max) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setMaxRows(%s)",
                                jndiName, spyLoggingCategory, max);
         
            s.setMaxRows(max);
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
   public void setEscapeProcessing(boolean enable) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setEscapeProcessing(%s)",
                                jndiName, spyLoggingCategory, enable);
         
            s.setEscapeProcessing(enable);
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
   public int getQueryTimeout() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getQueryTimeout()", jndiName, spyLoggingCategory);
         
            return s.getQueryTimeout();
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
   public void setQueryTimeout(int timeout) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setQueryTimeout(%s)",
                                jndiName, spyLoggingCategory, timeout);
         
            s.setQueryTimeout(timeout);
            timeoutSet = timeout;
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
   public void cancel() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] cancel()", jndiName, spyLoggingCategory);
         
            s.cancel();
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
   public void setCursorName(String name) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setCursorName(%s)",
                                jndiName, spyLoggingCategory, name);
         
            s.setCursorName(name);
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
   public ResultSet getResultSet() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getResultSet()", jndiName, spyLoggingCategory);
         
            ResultSet result = s.getResultSet();
            if (result == null)
               return null;
            else
               return registerResultSet(result);
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
   public int getUpdateCount() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getUpdateCount()", jndiName, spyLoggingCategory);
         
            return s.getUpdateCount();
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
   public boolean getMoreResults() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getMoreResults()",
                                jndiName, spyLoggingCategory);
         
            return s.getMoreResults();
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
   public boolean getMoreResults(int current) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getMoreResults(%s)", 
                                jndiName, spyLoggingCategory, current);
         
            return s.getMoreResults(current);
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
   public void setFetchDirection(int direction) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setFetchDirection(%s)",
                                jndiName, spyLoggingCategory, direction);
         
            s.setFetchDirection(direction);
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
   public int getFetchDirection() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getFetchDirection()",
                                jndiName, spyLoggingCategory);
         
            return s.getFetchDirection();
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
   public void setFetchSize(int rows) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setFetchSize(%s)",
                                jndiName, spyLoggingCategory, rows);
         
            s.setFetchSize(rows);
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
   public int getFetchSize() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getFetchSize()",
                                jndiName, spyLoggingCategory);
         
            return s.getFetchSize();
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
   public int getResultSetConcurrency() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getResultSetConcurrency()",
                                jndiName, spyLoggingCategory);
         
            return s.getResultSetConcurrency();
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
   public int getResultSetType() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getResultSetType()",
                                jndiName, spyLoggingCategory);
         
            return s.getResultSetType();
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
   public void addBatch(String sql) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] addBatch(%s)",
                                jndiName, spyLoggingCategory, sql);
         
            s.addBatch(sql);
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
   public void clearBatch() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] clearBatch()",
                                jndiName, spyLoggingCategory);
         
            s.clearBatch();
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
   public int[] executeBatch() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkTransaction();
         try
         {
            checkConfiguredQueryTimeout();

            if (spy)
               spyLogger.debugf("%s [%s] executeBatch()",
                                jndiName, spyLoggingCategory);
         
            return s.executeBatch();
         }
         catch (Throwable t)
         {
            throw checkException(t);
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
   public ResultSet getGeneratedKeys() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getGeneratedKeys()",
                                jndiName, spyLoggingCategory);
         
            ResultSet resultSet = s.getGeneratedKeys();
            return registerResultSet(resultSet);
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
   public int getResultSetHoldability() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getResultSetHoldability()",
                                jndiName, spyLoggingCategory);
         
            return s.getResultSetHoldability();
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
   public boolean isClosed() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         if (spy)
            spyLogger.debugf("%s [%s] isClosed()",
                             jndiName, spyLoggingCategory);

         Statement wrapped = getWrappedObject();
         if (wrapped == null)
            return true;
         return wrapped.isClosed();
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
    * {@inheritDoc}
    */
   public boolean isPoolable() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] isPoolable()",
                                jndiName, spyLoggingCategory);

            return s.isPoolable();
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
   public void setPoolable(boolean poolable) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setPoolable(%s)",
                                jndiName, spyLoggingCategory,
                                poolable);

            s.setPoolable(poolable);
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
   public void closeOnCompletion() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] closeOnCompletion()",
                                jndiName, spyLoggingCategory);

            s.closeOnCompletion();
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
   public boolean isCloseOnCompletion() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] isCloseOnCompletion()",
                                jndiName, spyLoggingCategory);

            return s.isCloseOnCompletion();
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
   public Statement getUnderlyingStatement() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         return s;
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * Get the wrapped statement
    * @return The result
    * @exception SQLException Thrown if an error occurs
    */
   protected Statement getWrappedObject() throws SQLException
   {
      return s;
   }

   /**
    * Check exception
    * @param t The throwable
    * @return The result
    * @exception SQLException Thrown if an error occurs
    */
   protected SQLException checkException(Throwable t)
      throws SQLException
   {
      throw lc.checkException(t);
   }

   /**
    * Check transaction
    * @exception SQLException Thrown if an error occurs
    */
   protected void checkTransaction()
      throws SQLException
   {
      checkState();
      lc.checkTransaction();
   }

   /**
    * Check configured query timeout
    * @exception SQLException Thrown if an error occurs
    */
   protected void checkConfiguredQueryTimeout() throws SQLException
   {
      lc.checkConfiguredQueryTimeout(this, timeoutSet);
   }

   /**
    * Check if transaction is active
    * @exception SQLException Thrown if an error occurs
    */
   protected void checkTransactionActive() throws SQLException
   {
      lc.checkTransactionActive();
   }

   /**
    * Close
    * @exception SQLException Thrown if an error occurs
    */
   protected void internalClose() throws SQLException
   {
      closed.set(true);
      try
      {
         closeResultSets();
      }
      finally
      {
         s.close();
      }
      timeoutSet = 0;
   }

   /**
    * Check state
    * @exception SQLException Thrown if an error occurs
    */
   protected void checkState() throws SQLException
   {
      if (closed.get())
         throw new SQLException(bundle.statementClosed());
   }

   /**
    * Wrap the result set
    * @param resultSet The result set
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    * @return The result
    */
   protected abstract WrappedResultSet wrapResultSet(ResultSet resultSet, boolean spy, String jndiName,
                                                     boolean doLocking);
   
   /**
    * Register a result set
    * @param resultSet The result set
    * @return The result
    */
   protected ResultSet registerResultSet(ResultSet resultSet)
   {
      if (resultSet != null)
         resultSet = wrapResultSet(resultSet, spy, jndiName, doLocking);
      
      if (lc.getTrackStatements() == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         return resultSet;

      WrappedResultSet wrapped = (WrappedResultSet) resultSet;
      
      synchronized (this)
      {
         if (resultSets == null)
            resultSets = new HashMap<WrappedResultSet, Throwable>(1);
         
         if (lc.getTrackStatements() == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_TRUE_INT)
            resultSets.put(wrapped, new Throwable("STACKTRACE"));
         else
            resultSets.put(wrapped, null);
      }
      return resultSet;
   }

   /**
    * Unregister a result set
    * @param resultSet The result set
    */
   protected void unregisterResultSet(WrappedResultSet resultSet)
   {
      if (lc.getTrackStatements() == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         return;

      synchronized (this)
      {
         if (resultSets != null)
            resultSets.remove(resultSet);
      }
   }

   /**
    * Close result sets
    */
   protected void closeResultSets()
   {
      if (lc.getTrackStatements() == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         return;

      synchronized (this)
      {
         if (resultSets == null)
            return;

         for (Iterator<Map.Entry<WrappedResultSet, Throwable>> i = resultSets.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry<WrappedResultSet, Throwable> entry = i.next();
            WrappedResultSet resultSet = entry.getKey();

            if (lc.getTrackStatements() == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_TRUE_INT)
            {
               Throwable stackTrace = entry.getValue();
               lc.getLogger().closingResultSet(resultSet.toString(), stackTrace);
            }

            try
            {
               resultSet.internalClose();
            }
            catch (Throwable t)
            {
               lc.getLogger().errorDuringClosingResultSet(resultSet.toString(), t);
            }
         }

         resultSets.clear();
      }
   }
}
