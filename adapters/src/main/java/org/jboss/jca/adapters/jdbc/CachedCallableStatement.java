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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * A cache wrapper for java.sql.CallableStatement
 *
 * @author <a href="mailto:andrewarro@mail.ru">Andrew Belomutskiy</a>
 * @author sstark@redhat.com
 * @version $Revision: 76129 $
 */
@SuppressWarnings("deprecation")
public abstract class CachedCallableStatement extends CachedPreparedStatement
   implements CallableStatement
{
   private final CallableStatement cs;

   /**
    * Constructor
    * @param cs The statement
    * @exception SQLException Thrown if an error occurs
    */
   public CachedCallableStatement(CallableStatement cs) throws SQLException
   {
      super(cs);
      this.cs = cs;
   }

   /**
    * {@inheritDoc}
    */
   public boolean wasNull() throws SQLException
   {
      return cs.wasNull();
   }

   /**
    * {@inheritDoc}
    */
   public byte getByte(int parameterIndex) throws SQLException
   {
      return cs.getByte(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public double getDouble(int parameterIndex) throws SQLException
   {
      return cs.getDouble(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public float getFloat(int parameterIndex) throws SQLException
   {
      return cs.getFloat(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public int getInt(int parameterIndex) throws SQLException
   {
      return cs.getInt(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public long getLong(int parameterIndex) throws SQLException
   {
      return cs.getLong(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public short getShort(int parameterIndex) throws SQLException
   {
      return cs.getShort(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public boolean getBoolean(int parameterIndex) throws SQLException
   {
      return cs.getBoolean(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public byte[] getBytes(int parameterIndex) throws SQLException
   {
      return cs.getBytes(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException
   {
      cs.registerOutParameter(parameterIndex, sqlType);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException
   {
      cs.registerOutParameter(parameterIndex, sqlType, scale);
   }

   /**
    * {@inheritDoc}
    */
   public Object getObject(int parameterIndex) throws SQLException
   {
      return cs.getObject(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public String getString(int parameterIndex) throws SQLException
   {
      return cs.getString(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException
   {
      cs.registerOutParameter(paramIndex, sqlType, typeName);
   }

   /**
    * {@inheritDoc}
    */
   public byte getByte(String parameterName) throws SQLException
   {
      return cs.getByte(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public double getDouble(String parameterName) throws SQLException
   {
      return cs.getDouble(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public float getFloat(String parameterName) throws SQLException
   {
      return cs.getFloat(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public int getInt(String parameterName) throws SQLException
   {
      return cs.getInt(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public long getLong(String parameterName) throws SQLException
   {
      return cs.getLong(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public short getShort(String parameterName) throws SQLException
   {
      return cs.getShort(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public boolean getBoolean(String parameterName) throws SQLException
   {
      return cs.getBoolean(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public byte[] getBytes(String parameterName) throws SQLException
   {
      return cs.getBytes(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public void setByte(String parameterName, byte x) throws SQLException
   {
      cs.setByte(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setDouble(String parameterName, double x) throws SQLException
   {
      cs.setDouble(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setFloat(String parameterName, float x) throws SQLException
   {
      cs.setFloat(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName, int sqlType) throws SQLException
   {
      cs.registerOutParameter(parameterName, sqlType);
   }

   /**
    * {@inheritDoc}
    */
   public void setInt(String parameterName, int x) throws SQLException
   {
      cs.setInt(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setNull(String parameterName, int sqlType) throws SQLException
   {
      cs.setNull(parameterName, sqlType);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException
   {
      cs.registerOutParameter(parameterName, sqlType, scale);
   }

   /**
    * {@inheritDoc}
    */
   public void setLong(String parameterName, long x) throws SQLException
   {
      cs.setLong(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setShort(String parameterName, short x) throws SQLException
   {
      cs.setShort(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setBoolean(String parameterName, boolean x) throws SQLException
   {
      cs.setBoolean(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setBytes(String parameterName, byte[] x) throws SQLException
   {
      cs.setBytes(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public BigDecimal getBigDecimal(int parameterIndex) throws SQLException
   {
      return cs.getBigDecimal(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException
   {
      return cs.getBigDecimal(parameterIndex, scale);
   }

   /**
    * {@inheritDoc}
    */
   public URL getURL(int parameterIndex) throws SQLException
   {
      return cs.getURL(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public Array getArray(int i) throws SQLException
   {
      return cs.getArray(i);
   }

   /**
    * {@inheritDoc}
    */
   public Blob getBlob(int i) throws SQLException
   {
      return cs.getBlob(i);
   }

   /**
    * {@inheritDoc}
    */
   public Clob getClob(int i) throws SQLException
   {
      return cs.getClob(i);
   }

   /**
    * {@inheritDoc}
    */
   public Date getDate(int parameterIndex) throws SQLException
   {
      return cs.getDate(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public Ref getRef(int i) throws SQLException
   {
      return cs.getRef(i);
   }

   /**
    * {@inheritDoc}
    */
   public Time getTime(int parameterIndex) throws SQLException
   {
      return cs.getTime(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public Timestamp getTimestamp(int parameterIndex) throws SQLException
   {
      return cs.getTimestamp(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException
   {
      cs.setAsciiStream(parameterName, x, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException
   {
      cs.setBinaryStream(parameterName, x, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException
   {
      cs.setCharacterStream(parameterName, reader, length);
   }

   /**
    * {@inheritDoc}
    */
   public Object getObject(String parameterName) throws SQLException
   {
      return cs.getObject(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public void setObject(String parameterName, Object x) throws SQLException
   {
      cs.setObject(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException
   {
      cs.setObject(parameterName, x, targetSqlType);
   }

   /**
    * {@inheritDoc}
    */
   public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException
   {
      cs.setObject(parameterName, x, targetSqlType, scale);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Object getObject(int i, Map<String, Class<?>> map) throws SQLException
   {
      return cs.getObject(i, map);
   }

   /**
    * {@inheritDoc}
    */
   public String getString(String parameterName) throws SQLException
   {
      return cs.getString(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException
   {
      cs.registerOutParameter(parameterName, sqlType, typeName);
   }

   /**
    * {@inheritDoc}
    */
   public void setNull(String parameterName, int sqlType, String typeName) throws SQLException
   {
      cs.setNull(parameterName, sqlType, typeName);
   }

   /**
    * {@inheritDoc}
    */
   public void setString(String parameterName, String x) throws SQLException
   {
      cs.setString(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public BigDecimal getBigDecimal(String parameterName) throws SQLException
   {
      return cs.getBigDecimal(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException
   {
      cs.setBigDecimal(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public URL getURL(String parameterName) throws SQLException
   {
      return cs.getURL(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public void setURL(String parameterName, URL val) throws SQLException
   {
      cs.setURL(parameterName, val);
   }

   /**
    * {@inheritDoc}
    */
   public Array getArray(String parameterName) throws SQLException
   {
      return cs.getArray(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public Blob getBlob(String parameterName) throws SQLException
   {
      return cs.getBlob(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public Clob getClob(String parameterName) throws SQLException
   {
      return cs.getClob(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public Date getDate(String parameterName) throws SQLException
   {
      return cs.getDate(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public void setDate(String parameterName, Date x) throws SQLException
   {
      cs.setDate(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public Date getDate(int parameterIndex, Calendar cal) throws SQLException
   {
      return cs.getDate(parameterIndex, cal);
   }

   /**
    * {@inheritDoc}
    */
   public Ref getRef(String parameterName) throws SQLException
   {
      return cs.getRef(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public Time getTime(String parameterName) throws SQLException
   {
      return cs.getTime(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public void setTime(String parameterName, Time x) throws SQLException
   {
      cs.setTime(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public Time getTime(int parameterIndex, Calendar cal) throws SQLException
   {
      return cs.getTime(parameterIndex, cal);
   }

   /**
    * {@inheritDoc}
    */
   public Timestamp getTimestamp(String parameterName) throws SQLException
   {
      return cs.getTimestamp(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public void setTimestamp(String parameterName, Timestamp x) throws SQLException
   {
      cs.setTimestamp(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException
   {
      return cs.getTimestamp(parameterIndex, cal);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException
   {
      return cs.getObject(parameterName, map);
   }

   /**
    * {@inheritDoc}
    */
   public Date getDate(String parameterName, Calendar cal) throws SQLException
   {
      return cs.getDate(parameterName, cal);
   }

   /**
    * {@inheritDoc}
    */
   public Time getTime(String parameterName, Calendar cal) throws SQLException
   {
      return cs.getTime(parameterName, cal);
   }

   /**
    * {@inheritDoc}
    */
   public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException
   {
      return cs.getTimestamp(parameterName, cal);
   }

   /**
    * {@inheritDoc}
    */
   public void setDate(String parameterName, Date x, Calendar cal) throws SQLException
   {
      cs.setDate(parameterName, x, cal);
   }

   /**
    * {@inheritDoc}
    */
   public void setTime(String parameterName, Time x, Calendar cal) throws SQLException
   {
      cs.setTime(parameterName, x, cal);
   }

   /**
    * {@inheritDoc}
    */
   public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException
   {
      cs.setTimestamp(parameterName, x, cal);
   }

   /**
    * {@inheritDoc}
    */
   public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
   {
      cs.setAsciiStream(parameterIndex, x, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
   {
      cs.setAsciiStream(parameterIndex, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
   {
      cs.setBinaryStream(parameterIndex, x, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
   {
      cs.setBinaryStream(parameterIndex, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
   {
      cs.setBlob(parameterIndex, inputStream, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
   {
      cs.setBlob(parameterIndex, inputStream);
   }

   /**
    * {@inheritDoc}
    */
   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
   {
      cs.setCharacterStream(parameterIndex, reader, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
   {
      cs.setCharacterStream(parameterIndex, reader);
   }

   /**
    * {@inheritDoc}
    */
   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
   {
      cs.setClob(parameterIndex, reader, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setClob(int parameterIndex, Reader reader) throws SQLException
   {
      cs.setClob(parameterIndex, reader);
   }

   /**
    * {@inheritDoc}
    */
   public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
   {
      cs.setNCharacterStream(parameterIndex, value, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
   {
      cs.setNCharacterStream(parameterIndex, value);
   }

   /**
    * {@inheritDoc}
    */
   public void setNClob(int parameterIndex, NClob value) throws SQLException
   {
      cs.setNClob(parameterIndex, value);
   }

   /**
    * {@inheritDoc}
    */
   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
   {
      cs.setNClob(parameterIndex, reader, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setNClob(int parameterIndex, Reader reader) throws SQLException
   {
      cs.setNClob(parameterIndex, reader);
   }

   /**
    * {@inheritDoc}
    */
   public void setNString(int parameterIndex, String value) throws SQLException
   {
      cs.setNString(parameterIndex, value);
   }

   /**
    * {@inheritDoc}
    */
   public void setRowId(int parameterIndex, RowId x) throws SQLException
   {
      cs.setRowId(parameterIndex, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
   {
      cs.setSQLXML(parameterIndex, xmlObject);
   }

   /**
    * {@inheritDoc}
    */
   public boolean isClosed() throws SQLException
   {
      return cs.isClosed();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isPoolable() throws SQLException
   {
      return cs.isPoolable();
   }

   /**
    * {@inheritDoc}
    */
   public void setPoolable(boolean poolable) throws SQLException
   {
      cs.setPoolable(poolable);
   }

   /**
    * {@inheritDoc}
    */
   public Reader getCharacterStream(int parameterIndex) throws SQLException
   {
      return cs.getCharacterStream(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public Reader getCharacterStream(String parameterName) throws SQLException
   {
      return cs.getCharacterStream(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public Reader getNCharacterStream(int parameterIndex) throws SQLException
   {
      return cs.getNCharacterStream(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public Reader getNCharacterStream(String parameterName) throws SQLException
   {
      return cs.getNCharacterStream(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public NClob getNClob(int parameterIndex) throws SQLException
   {
      return cs.getNClob(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public NClob getNClob(String parameterName) throws SQLException
   {
      return cs.getNClob(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public String getNString(int parameterIndex) throws SQLException
   {
      return cs.getNString(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public String getNString(String parameterName) throws SQLException
   {
      return cs.getNString(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public RowId getRowId(int parameterIndex) throws SQLException
   {
      return cs.getRowId(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public RowId getRowId(String parameterName) throws SQLException
   {
      return cs.getRowId(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public SQLXML getSQLXML(int parameterIndex) throws SQLException
   {
      return cs.getSQLXML(parameterIndex);
   }

   /**
    * {@inheritDoc}
    */
   public SQLXML getSQLXML(String parameterName) throws SQLException
   {
      return cs.getSQLXML(parameterName);
   }

   /**
    * {@inheritDoc}
    */
   public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException
   {
      cs.setAsciiStream(parameterName, x, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setAsciiStream(String parameterName, InputStream x) throws SQLException
   {
      cs.setAsciiStream(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException
   {
      cs.setBinaryStream(parameterName, x, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setBinaryStream(String parameterName, InputStream x) throws SQLException
   {
      cs.setBinaryStream(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setBlob(String parameterName, Blob x) throws SQLException
   {
      cs.setBlob(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException
   {
      cs.setBlob(parameterName, inputStream, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setBlob(String parameterName, InputStream inputStream) throws SQLException
   {
      cs.setBlob(parameterName, inputStream);
   }

   /**
    * {@inheritDoc}
    */
   public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException
   {
      cs.setCharacterStream(parameterName, reader, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setCharacterStream(String parameterName, Reader reader) throws SQLException
   {
      cs.setCharacterStream(parameterName, reader);
   }

   /**
    * {@inheritDoc}
    */
   public void setClob(String parameterName, Clob x) throws SQLException
   {
      cs.setClob(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setClob(String parameterName, Reader reader, long length) throws SQLException
   {
      cs.setClob(parameterName, reader, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setClob(String parameterName, Reader reader) throws SQLException
   {
      cs.setClob(parameterName, reader);
   }

   /**
    * {@inheritDoc}
    */
   public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException
   {
      cs.setNCharacterStream(parameterName, value, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setNCharacterStream(String parameterName, Reader value) throws SQLException
   {
      cs.setNCharacterStream(parameterName, value);
   }

   /**
    * {@inheritDoc}
    */
   public void setNClob(String parameterName, NClob value) throws SQLException
   {
      cs.setNClob(parameterName, value);
   }

   /**
    * {@inheritDoc}
    */
   public void setNClob(String parameterName, Reader reader, long length) throws SQLException
   {
      cs.setNClob(parameterName, reader, length);
   }

   /**
    * {@inheritDoc}
    */
   public void setNClob(String parameterName, Reader reader) throws SQLException
   {
      cs.setNClob(parameterName, reader);
   }

   /**
    * {@inheritDoc}
    */
   public void setNString(String parameterName, String value) throws SQLException
   {
      cs.setNString(parameterName, value);
   }

   /**
    * {@inheritDoc}
    */
   public void setRowId(String parameterName, RowId x) throws SQLException
   {
      cs.setRowId(parameterName, x);
   }

   /**
    * {@inheritDoc}
    */
   public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
   {
      cs.setSQLXML(parameterName, xmlObject);
   }

   /**
    * {@inheritDoc}
    */
   public void closeOnCompletion() throws SQLException
   {
      cs.closeOnCompletion();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isCloseOnCompletion() throws SQLException
   {
      return cs.isCloseOnCompletion();
   }

   /**
    * {@inheritDoc}
    */
   public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException
   {
      return cs.getObject(parameterIndex, type);
   }

   /**
    * {@inheritDoc}
    */
   public <T> T getObject(String parameterName, Class<T> type) throws SQLException
   {
      return cs.getObject(parameterName, type);
   }

   /**
    * {@inheritDoc}
    */
   protected CallableStatement getWrappedObject() throws SQLException
   {
      return cs;
   }
}
