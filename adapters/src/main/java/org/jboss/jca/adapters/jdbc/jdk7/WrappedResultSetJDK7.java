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

import org.jboss.jca.adapters.jdbc.WrappedResultSet;
import org.jboss.jca.adapters.jdbc.WrappedStatement;

import java.io.InputStream;
import java.io.Reader;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

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
    */
   public WrappedResultSetJDK7(WrappedStatement statement, ResultSet resultSet)
   {
      super(statement, resultSet);
   }

   /**
    * {@inheritDoc}
    */
   public int getHoldability() throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getHoldability();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Reader getNCharacterStream(int columnIndex) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getNCharacterStream(columnIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Reader getNCharacterStream(String columnLabel) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getNCharacterStream(columnLabel);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public NClob getNClob(int columnIndex) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getNClob(columnIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public NClob getNClob(String columnLabel) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getNClob(columnLabel);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getNString(int columnIndex) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getNString(columnIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getNString(String columnLabel) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getNString(columnLabel);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public RowId getRowId(int columnIndex) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getRowId(columnIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public RowId getRowId(String columnLabel) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getRowId(columnLabel);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public SQLXML getSQLXML(int columnIndex) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getSQLXML(columnIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public SQLXML getSQLXML(String columnLabel) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         return resultSet.getSQLXML(columnLabel);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isClosed() throws SQLException
   {
      ResultSet resultSet = getWrappedObject();
      if (resultSet == null)
         return true;
      try
      {
         return resultSet.isClosed();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateAsciiStream(columnIndex, x, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateAsciiStream(columnIndex, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateAsciiStream(columnLabel, x, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateAsciiStream(columnLabel, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateBinaryStream(columnIndex, x, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateBinaryStream(columnIndex, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateBinaryStream(columnLabel, x, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateBinaryStream(columnLabel, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateBlob(columnIndex, inputStream, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateBlob(columnIndex, inputStream);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateBlob(columnLabel, inputStream, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateBlob(columnLabel, inputStream);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateCharacterStream(columnIndex, x, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateCharacterStream(int columnIndex, Reader x) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateCharacterStream(columnIndex, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateCharacterStream(columnLabel, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateCharacterStream(columnLabel, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateClob(int columnIndex, Reader reader, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateClob(columnIndex, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateClob(int columnIndex, Reader reader) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateClob(columnIndex, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateClob(String columnLabel, Reader reader, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateClob(columnLabel, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateClob(String columnLabel, Reader reader) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateClob(columnLabel, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNCharacterStream(columnIndex, x, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNCharacterStream(columnIndex, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNCharacterStream(columnLabel, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNCharacterStream(columnLabel, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNClob(int columnIndex, NClob clob) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNClob(columnIndex, clob);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNClob(columnIndex, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNClob(int columnIndex, Reader reader) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNClob(columnIndex, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNClob(String columnLabel, NClob clob) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNClob(columnLabel, clob);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNClob(columnLabel, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNClob(String columnLabel, Reader reader) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNClob(columnLabel, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNString(int columnIndex, String string) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNString(columnIndex, string);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateNString(String columnLabel, String string) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateNString(columnLabel, string);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateRowId(int columnIndex, RowId x) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateRowId(columnIndex, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateRowId(String columnLabel, RowId x) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateRowId(columnLabel, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateSQLXML(columnIndex, xmlObject);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException
   {
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
         resultSet.updateSQLXML(columnLabel, xmlObject);
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
      ResultSet resultSet = getUnderlyingResultSet();
      try
      {
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
         return resultSet.getObject(parameterName, type);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }
}
