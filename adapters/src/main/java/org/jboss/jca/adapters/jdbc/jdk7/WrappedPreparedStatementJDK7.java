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

import org.jboss.jca.adapters.jdbc.WrappedPreparedStatement;
import org.jboss.jca.adapters.jdbc.WrappedResultSet;

import java.io.InputStream;
import java.io.Reader;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

/**
 * WrappedPreparedStatementJDK7.
 * 
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WrappedPreparedStatementJDK7 extends WrappedPreparedStatement
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    * @param lc The connection
    * @param s The prepared statement
    */
   public WrappedPreparedStatementJDK7(WrappedConnectionJDK7 lc, PreparedStatement s)
   {
      super(lc, s);
   }
   
   /**
    * Wrap the result set
    * @param resultSet The result set
    * @return The result
    */
   protected WrappedResultSet wrapResultSet(ResultSet resultSet)
   {
      return new WrappedResultSetJDK7(this, resultSet);
   }

   /**
    * {@inheritDoc}
    */
   public boolean isClosed() throws SQLException
   {
      lock();
      try
      {
         PreparedStatement wrapped = getWrappedObject();
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
         PreparedStatement statement = getUnderlyingStatement();
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
         PreparedStatement statement = getUnderlyingStatement();
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

   /**
    * {@inheritDoc}
    */
   public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setAsciiStream(parameterIndex, x, length);
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
   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setAsciiStream(parameterIndex, x);
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
   public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setBinaryStream(parameterIndex, x, length);
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
   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setBinaryStream(parameterIndex, x);
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
   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setBlob(parameterIndex, inputStream, length);
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
   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setBlob(parameterIndex, inputStream);
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
   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setCharacterStream(parameterIndex, reader, length);
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
   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setCharacterStream(parameterIndex, reader);
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
   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setClob(parameterIndex, reader, length);
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
   public void setClob(int parameterIndex, Reader reader) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setClob(parameterIndex, reader);
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
   public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setNCharacterStream(parameterIndex, value, length);
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
   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setNCharacterStream(parameterIndex, value);
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
   public void setNClob(int parameterIndex, NClob value) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setNClob(parameterIndex, value);
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
   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setNClob(parameterIndex, reader, length);
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
   public void setNClob(int parameterIndex, Reader reader) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setNClob(parameterIndex, reader);
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
   public void setNString(int parameterIndex, String value) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setNString(parameterIndex, value);
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
   public void setRowId(int parameterIndex, RowId x) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setRowId(parameterIndex, x);
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
   public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
            statement.setSQLXML(parameterIndex, xmlObject);
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
   public void closeOnCompletion() throws SQLException
   {
      lock();
      try
      {
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
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
         PreparedStatement statement = getUnderlyingStatement();
         try
         {
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
}
