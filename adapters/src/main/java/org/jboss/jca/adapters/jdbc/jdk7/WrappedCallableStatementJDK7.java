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

package org.jboss.jca.adapters.jdbc.jdk7;

import org.jboss.jca.adapters.jdbc.Constants;
import org.jboss.jca.adapters.jdbc.WrappedCallableStatement;
import org.jboss.jca.adapters.jdbc.WrappedResultSet;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * WrappedCallableStatementJDK7.
 * 
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WrappedCallableStatementJDK7 extends WrappedCallableStatement
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    * @param lc The connection
    * @param s The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    */
   public WrappedCallableStatementJDK7(WrappedConnectionJDK7 lc, CallableStatement s,
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
      return new WrappedResultSetJDK7(this, resultSet, spy, jndiName);
   }

   /**
    * {@inheritDoc}
    */
   public void closeOnCompletion() throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (isSpy())
            spyLogger.debugf("%s [%s] closeOnCompletion()",
                             getJndiName(), Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT);

         statement.closeOnCompletion();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isCloseOnCompletion() throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (isSpy())
            spyLogger.debugf("%s [%s] isCloseOnCompletion()",
                             getJndiName(), Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT);

         return statement.isCloseOnCompletion();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (isSpy())
            spyLogger.debugf("%s [%s] getObject(%s, %s)",
                             getJndiName(), Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT,
                             parameterIndex, type);

         return statement.getObject(parameterIndex, type);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public <T> T getObject(String parameterName, Class<T> type) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         if (isSpy())
            spyLogger.debugf("%s [%s] getObject(%s, %s)",
                             getJndiName(), Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT,
                             parameterName, type);

         return statement.getObject(parameterName, type);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }
}
