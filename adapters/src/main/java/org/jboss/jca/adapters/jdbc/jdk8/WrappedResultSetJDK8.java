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
import java.sql.SQLType;

/**
 * WrappedResultSetJDK8.
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WrappedResultSetJDK8 extends WrappedResultSet
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    * @param statement The statement
    * @param resultSet The result set
    * @param spy The spy value
    * @param jndiName The jndi name
    */
   public WrappedResultSetJDK8(WrappedStatement statement, ResultSet resultSet,
                               boolean spy, String jndiName)
   {
      super(statement, resultSet, spy, jndiName);
   }

   /**
    * {@inheritDoc}
    */
   public void updateObject(int columnIndex,
                            Object x,
                            SQLType targetSqlType,
                            int scaleOrLength)
      throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] updateObject(%d, %s, %s, %d)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_RESULTSET,
                                columnIndex, x, targetSqlType, scaleOrLength);

            getWrappedObject().updateObject(columnIndex, x, targetSqlType, scaleOrLength);
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
   public void updateObject(String columnLabel,
                            Object x,
                            SQLType targetSqlType,
                            int scaleOrLength)
      throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] updateObject(%s, %s, %s, %d)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_RESULTSET,
                                columnLabel, x, targetSqlType, scaleOrLength);

            getWrappedObject().updateObject(columnLabel, x, targetSqlType, scaleOrLength);
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
   public void updateObject(int columnIndex,
                            Object x,
                            SQLType targetSqlType)
      throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] updateObject(%d, %s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_RESULTSET,
                                columnIndex, x, targetSqlType);

            getWrappedObject().updateObject(columnIndex, x, targetSqlType);
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
   public void updateObject(String columnLabel,
                            Object x,
                            SQLType targetSqlType)
      throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] updateObject(%s, %s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_RESULTSET,
                                columnLabel, x, targetSqlType);

            getWrappedObject().updateObject(columnLabel, x, targetSqlType);
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
