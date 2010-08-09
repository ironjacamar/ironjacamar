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

package org.jboss.jca.adapters.jdbc.jdk6;

import org.jboss.jca.adapters.jdbc.WrappedResultSet;
import org.jboss.jca.adapters.jdbc.WrappedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * WrappedStatementJDK6.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class WrappedStatementJDK6 extends WrappedStatement
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    * @param lc The connection
    * @param s The statement
    */
   public WrappedStatementJDK6(WrappedConnectionJDK6 lc, Statement s)
   {
      super(lc, s);
   }

   /**
    * Wrap ResultSet
    * @param resultSet The ResultSet
    * @return The result
    */
   protected WrappedResultSet wrapResultSet(ResultSet resultSet)
   {
      return new WrappedResultSetJDK6(this, resultSet);
   }

   /**
    * {@inheritDoc}
    */
   public boolean isClosed() throws SQLException
   {
      lock();
      try
      {
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
         unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isPoolable() throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            return statement.isPoolable();
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
   public void setPoolable(boolean poolable) throws SQLException
   {
      lock();
      try
      {
         Statement statement = getUnderlyingStatement();
         try
         {
            statement.setPoolable(poolable);
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
