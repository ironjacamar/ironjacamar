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
import org.jboss.jca.adapters.jdbc.WrappedCallableStatement;
import org.jboss.jca.adapters.jdbc.WrappedResultSet;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;

/**
 * WrappedCallableStatementJDK8.
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WrappedCallableStatementJDK8 extends WrappedCallableStatement
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    * @param lc The connection
    * @param s The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    */
   public WrappedCallableStatementJDK8(WrappedConnectionJDK8 lc, CallableStatement s,
                                       boolean spy, String jndiName)
   {
      super(lc, s, spy, jndiName);
   }
   
   /**
    * Wrap result set
    * @param resultSet The result set
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
   public void setObject(String parameterName,
                         Object x,
                         SQLType targetSqlType,
                         int scaleOrLength)
      throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (spy)
            spyLogger.debugf("%s [%s] setObject(%s, %s, %s, %d)",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT,
                             parameterName, x, targetSqlType, scaleOrLength);

         statement.setObject(parameterName, x, targetSqlType, scaleOrLength);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setObject(String parameterName,
                         Object x,
                         SQLType targetSqlType)
      throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (spy)
            spyLogger.debugf("%s [%s] setObject(%s, %s, %s)",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT,
                             parameterName, x, targetSqlType);

         statement.setObject(parameterName, x, targetSqlType);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex,
                                    SQLType sqlType)
      throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (spy)
            spyLogger.debugf("%s [%s] registerOutParameter(%d, %s)",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT,
                             parameterIndex, sqlType);

         statement.registerOutParameter(parameterIndex, sqlType);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex,
                                    SQLType sqlType,
                                    int scale)
      throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (spy)
            spyLogger.debugf("%s [%s] registerOutParameter(%d, %s, %d)",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT,
                             parameterIndex, sqlType, scale);

         statement.registerOutParameter(parameterIndex, sqlType, scale);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex,
                                    SQLType sqlType,
                                    String typeName)
      throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (spy)
            spyLogger.debugf("%s [%s] registerOutParameter(%d, %s, %s)",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT,
                             parameterIndex, sqlType, typeName);

         statement.registerOutParameter(parameterIndex, sqlType, typeName);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName,
                                    SQLType sqlType)
      throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (spy)
            spyLogger.debugf("%s [%s] registerOutParameter(%s, %s)",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT,
                             parameterName, sqlType);

         statement.registerOutParameter(parameterName, sqlType);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName,
                                    SQLType sqlType,
                                    int scale)
      throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (spy)
            spyLogger.debugf("%s [%s] registerOutParameter(%s, %s, %d)",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT,
                             parameterName, sqlType, scale);

         statement.registerOutParameter(parameterName, sqlType, scale);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName,
                                    SQLType sqlType,
                                    String typeName)
      throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (spy)
            spyLogger.debugf("%s [%s] registerOutParameter(%s, %s, %s)",
                             jndiName, Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT,
                             parameterName, sqlType, typeName);

         statement.registerOutParameter(parameterName, sqlType, typeName);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }
}
