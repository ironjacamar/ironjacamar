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

import org.jboss.jca.adapters.jdbc.BaseWrapperManagedConnection;
import org.jboss.jca.adapters.jdbc.Constants;
import org.jboss.jca.adapters.jdbc.WrappedCallableStatement;
import org.jboss.jca.adapters.jdbc.WrappedConnection;
import org.jboss.jca.adapters.jdbc.WrappedPreparedStatement;
import org.jboss.jca.adapters.jdbc.WrappedStatement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executor;

/**
 * WrappedConnectionJDK7.
 * 
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
@SuppressWarnings("unchecked")
public class WrappedConnectionJDK7 extends WrappedConnection
{
   private static final long serialVersionUID = 1L;

   /**
    * Create a new WrappedConnectionJDK7.
    * 
    * @param mc the managed connection
    * @param spy The spy value
    * @param jndiName The jndi name
    */
   public WrappedConnectionJDK7(BaseWrapperManagedConnection mc, boolean spy, String jndiName)
   {
      super(mc, spy, jndiName);
   }

   /**
    * Wrap statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @return The result
    */
   protected WrappedStatement wrapStatement(Statement statement, boolean spy, String jndiName)
   {
      return new WrappedStatementJDK7(this, statement, spy, jndiName);
   }

   /**
    * Wrap prepared statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @return The result
    */
   protected WrappedPreparedStatement wrapPreparedStatement(PreparedStatement statement, boolean spy, String jndiName)
   {
      return new WrappedPreparedStatementJDK7(this, statement, spy, jndiName);
   }

   /**
    * Wrap callable statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @return The result
    */
   protected WrappedCallableStatement wrapCallableStatement(CallableStatement statement, boolean spy, String jndiName)
   {
      return new WrappedCallableStatementJDK7(this, statement, spy, jndiName);
   }

   /**
    * {@inheritDoc}
    */
   public void setSchema(String schema) throws SQLException
   {
      lock();
      try
      {
         Connection c = getUnderlyingConnection();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setSchema(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                schema);

            c.setSchema(schema);
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
   public String getSchema() throws SQLException
   {
      lock();
      try
      {
         Connection c = getUnderlyingConnection();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getSchema()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return c.getSchema();
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
   public void abort(Executor executor) throws SQLException
   {
      lock();
      try
      {
         Connection c = getUnderlyingConnection();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] abort(%s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                executor);

            c.abort(executor);
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
   public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
   {
      lock();
      try
      {
         Connection c = getUnderlyingConnection();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setNetworkTimeout(%s, %s)",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION,
                                executor, milliseconds);

            c.setNetworkTimeout(executor, milliseconds);
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
   public int getNetworkTimeout() throws SQLException
   {
      lock();
      try
      {
         Connection c = getUnderlyingConnection();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getNetworkTimeout()",
                                jndiName, Constants.SPY_LOGGER_PREFIX_CONNECTION);

            return c.getNetworkTimeout();
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
