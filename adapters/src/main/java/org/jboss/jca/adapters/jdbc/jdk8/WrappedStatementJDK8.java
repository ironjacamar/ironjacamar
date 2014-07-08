/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.jdk8;

import org.jboss.jca.adapters.jdbc.Constants;
import org.jboss.jca.adapters.jdbc.WrappedResultSet;
import org.jboss.jca.adapters.jdbc.WrappedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * WrappedStatementJDK8.
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WrappedStatementJDK8 extends WrappedStatement
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    * @param lc The connection
    * @param s The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    */
   public WrappedStatementJDK8(WrappedConnectionJDK8 lc, Statement s, boolean spy, String jndiName)
   {
      super(lc, s, spy, jndiName);
   }

   /**
    * Wrap ResultSet
    * @param resultSet The ResultSet
    * @param spy The spy value
    * @param jndiName The jndi name
    * @return The result
    */
   protected WrappedResultSet wrapResultSet(ResultSet resultSet, boolean spy, String jndiName)
   {
      return new WrappedResultSetJDK8(this, resultSet, spy, jndiName);
   }

   /**
    * {@inheritDoc}
    */
   public void closeOnCompletion() throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] closeOnCompletion()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_STATEMENT);

            statement.closeOnCompletion();
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
   public boolean isCloseOnCompletion() throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] isCloseOnCompletion()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_STATEMENT);

            return statement.isCloseOnCompletion();
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
   public long getLargeUpdateCount()
      throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getLargeUpdateCount()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_STATEMENT);

            return statement.getLargeUpdateCount();
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
   public void setLargeMaxRows(long max)
      throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setLargeMaxRows(%d)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_STATEMENT,
                                max);

            statement.setLargeMaxRows(max);
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
   public long getLargeMaxRows()
      throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getLargeMaxRows()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_STATEMENT);

            return statement.getLargeMaxRows();
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
   public long[] executeLargeBatch()
      throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] executeLargeBatch()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_STATEMENT);

            return statement.executeLargeBatch();
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
   public long executeLargeUpdate(String sql)
      throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] executeLargeUpdate(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_STATEMENT,
                                sql);

            return statement.executeLargeUpdate(sql);
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
   public long executeLargeUpdate(String sql,
                                  int autoGeneratedKeys)
      throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] executeLargeUpdate(%s, %d)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_STATEMENT,
                                sql, autoGeneratedKeys);

            return statement.executeLargeUpdate(sql, autoGeneratedKeys);
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
   public long executeLargeUpdate(String sql,
                                  int[] columnIndexes)
      throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] executeLargeUpdate(%s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_STATEMENT,
                                sql, columnIndexes);

            return statement.executeLargeUpdate(sql, columnIndexes);
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
   public long executeLargeUpdate(String sql,
                                  String[] columnNames)
      throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] executeLargeUpdate(%s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_STATEMENT,
                                sql, columnNames);

            return statement.executeLargeUpdate(sql, columnNames);
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
}
