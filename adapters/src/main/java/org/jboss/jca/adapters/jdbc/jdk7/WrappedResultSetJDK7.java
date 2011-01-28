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
import org.jboss.jca.adapters.jdbc.WrappedResultSet;
import org.jboss.jca.adapters.jdbc.WrappedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * WrappedResultSetJDK7.
 * 
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WrappedResultSetJDK7 extends WrappedResultSet
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    * @param statement The statement
    * @param resultSet The result set
    * @param spy The spy value
    * @param jndiName The jndi name
    */
   public WrappedResultSetJDK7(WrappedStatement statement, ResultSet resultSet,
                               boolean spy, String jndiName)
   {
      super(statement, resultSet, spy, jndiName);
   }

   /**
    * {@inheritDoc}
    */
   public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         if (isSpy())
            spyLogger.debugf("%s [%s] getObject(%s, %s)",
                             getJndiName(), Constants.SPY_LOGGER_PREFIX_RESULTSET,
                             parameterIndex, type);

         return resultSet.getObject(parameterIndex, type);
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
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         if (isSpy())
            spyLogger.debugf("%s [%s] getObject(%s, %s)",
                             getJndiName(), Constants.SPY_LOGGER_PREFIX_RESULTSET,
                             parameterName, type);

         return resultSet.getObject(parameterName, type);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }
}
