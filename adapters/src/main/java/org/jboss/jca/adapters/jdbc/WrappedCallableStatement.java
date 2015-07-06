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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

/**
 * WrappedCallableStatement
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class WrappedCallableStatement extends WrappedPreparedStatement implements CallableStatement
{
   private final CallableStatement cs;

   /**
    * Constructor
    * @param lc The connection
    * @param cs The callable statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    */
   public WrappedCallableStatement(final WrappedConnection lc, final CallableStatement cs,
                                   boolean spy, String jndiName, boolean doLocking)
   {
      super(lc, cs, spy, jndiName, doLocking, Constants.SPY_LOGGER_PREFIX_CALLABLE_STATEMENT);
      this.cs = cs;
   }

   /**
    * {@inheritDoc}
    */
   public CallableStatement getUnderlyingStatement() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         if (cs instanceof CachedCallableStatement)
         {
            return ((CachedCallableStatement)cs).getUnderlyingCallableStatement();
         }
         else
         {
            return cs;
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Object getObject(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getObject(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getObject(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Object getObject(int parameterIndex, Map<String, Class<?>> typeMap) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getObject(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex, typeMap);

            return cs.getObject(parameterIndex, typeMap);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Object getObject(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getObject(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getObject(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Object getObject(String parameterName, Map<String, Class<?>> typeMap) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getObject(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, typeMap);

            return cs.getObject(parameterName, typeMap);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean getBoolean(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getBoolean(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getBoolean(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean getBoolean(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getBoolean(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getBoolean(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public byte getByte(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getByte(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getByte(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public byte getByte(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getByte(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);
            
            return cs.getByte(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public short getShort(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getShort(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getShort(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public short getShort(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getShort(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getShort(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getInt(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getInt(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getInt(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getInt(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getInt(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getInt(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getLong(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getLong(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getLong(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public long getLong(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getLong(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getLong(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public float getFloat(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getFloat(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getFloat(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public float getFloat(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getFloat(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getFloat(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public double getDouble(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getDouble(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getDouble(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public double getDouble(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getDouble(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getDouble(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public byte[] getBytes(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getBytes(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getBytes(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public byte[] getBytes(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getBytes(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getBytes(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public URL getURL(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getURL(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getURL(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public URL getURL(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getURL(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getURL(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getString(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getString(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getString(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getString(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getString(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getString(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Ref getRef(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getRef(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getRef(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Ref getRef(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getRef(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getRef(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Time getTime(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getTime(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getTime(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Time getTime(int parameterIndex, Calendar calendar) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getTime(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex, calendar);

            return cs.getTime(parameterIndex, calendar);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Time getTime(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getTime(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getTime(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Time getTime(String parameterName, Calendar calendar) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getTime(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, calendar);

            return cs.getTime(parameterName, calendar);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Date getDate(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getDate(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getDate(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Date getDate(int parameterIndex, Calendar calendar) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getDate(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex, calendar);

            return cs.getDate(parameterIndex, calendar);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Date getDate(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getDate(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getDate(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Date getDate(String parameterName, Calendar calendar) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getDate(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, calendar);

            return cs.getDate(parameterName, calendar);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] registerOutParameter(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex, sqlType);

            cs.registerOutParameter(parameterIndex, sqlType);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] registerOutParameter(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex, sqlType, scale);

            cs.registerOutParameter(parameterIndex, sqlType, scale);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] registerOutParameter(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex, sqlType, typeName);

            cs.registerOutParameter(parameterIndex, sqlType, typeName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName, int sqlType) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] registerOutParameter(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, sqlType);

            cs.registerOutParameter(parameterName, sqlType);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] registerOutParameter(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, sqlType, scale);

            cs.registerOutParameter(parameterName, sqlType, scale);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] registerOutParameter(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, sqlType, typeName);

            cs.registerOutParameter(parameterName, sqlType, typeName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean wasNull() throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] wasNull()",
                                jndiName, spyLoggingCategory);

            return cs.wasNull();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Deprecated
   public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getBigDecimal(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex, scale);

            return cs.getBigDecimal(parameterIndex, scale);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public BigDecimal getBigDecimal(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getBigDecimal(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getBigDecimal(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public BigDecimal getBigDecimal(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getBigDecimal(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getBigDecimal(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Timestamp getTimestamp(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getTimestamp(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getTimestamp(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Timestamp getTimestamp(int parameterIndex, Calendar calendar) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getTimestamp(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex, calendar);

            return cs.getTimestamp(parameterIndex, calendar);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Timestamp getTimestamp(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getTimestamp(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getTimestamp(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Timestamp getTimestamp(String parameterName, Calendar calendar) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getTimestamp(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, calendar);

            return cs.getTimestamp(parameterName, calendar);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Blob getBlob(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getBlob(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getBlob(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Blob getBlob(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getBlob(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getBlob(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Clob getClob(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getClob(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getClob(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Clob getClob(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getClob(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getClob(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Array getArray(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getArray(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getArray(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Array getArray(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getArray(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getArray(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setBoolean(String parameterName, boolean value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setBoolean(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setBoolean(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setByte(String parameterName, byte value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setByte(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setByte(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setShort(String parameterName, short value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setShort(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setShort(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setInt(String parameterName, int value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setInt(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setInt(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setLong(String parameterName, long value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setLong(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setLong(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setFloat(String parameterName, float value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setFloat(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setFloat(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setDouble(String parameterName, double value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setDouble(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setDouble(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setURL(String parameterName, URL value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setURL(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setURL(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setTime(String parameterName, Time value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setTime(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setTime(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setTime(String parameterName, Time value, Calendar calendar) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setTime(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value, calendar);

            cs.setTime(parameterName, value, calendar);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setNull(String parameterName, int value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setNull(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setNull(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setNull(String parameterName, int sqlType, String typeName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setNull(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, sqlType, typeName);

            cs.setNull(parameterName, sqlType, typeName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setBigDecimal(String parameterName, BigDecimal value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setBigDecimal(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setBigDecimal(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setString(String parameterName, String value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setString(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setString(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setBytes(String parameterName, byte[] value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setBytes(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, Arrays.toString(value));

            cs.setBytes(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setDate(String parameterName, Date value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setDate(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setDate(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setDate(String parameterName, Date value, Calendar calendar) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setDate(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value, calendar);

            cs.setDate(parameterName, value, calendar);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setTimestamp(String parameterName, Timestamp value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setTimestamp(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setTimestamp(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setTimestamp(String parameterName, Timestamp value, Calendar calendar) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setTimestamp(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value, calendar);

            cs.setTimestamp(parameterName, value, calendar);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setAsciiStream(String parameterName, InputStream stream, int length) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setAsciiStream(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, stream, length);

            cs.setAsciiStream(parameterName, stream, length);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setBinaryStream(String parameterName, InputStream stream, int length) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setBinaryStream(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, stream, length);

            cs.setBinaryStream(parameterName, stream, length);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setObject(String parameterName, Object value, int sqlType, int scale) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setObject(%s, %s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value, sqlType, scale);

            cs.setObject(parameterName, value, sqlType, scale);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setObject(String parameterName, Object value, int sqlType) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setObject(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value, sqlType);

            cs.setObject(parameterName, value, sqlType);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setObject(String parameterName, Object value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setObject(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setObject(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setCharacterStream(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, reader, length);

            cs.setCharacterStream(parameterName, reader, length);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Reader getCharacterStream(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getCharacterStream(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getCharacterStream(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Reader getCharacterStream(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getCharacterStream(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getCharacterStream(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Reader getNCharacterStream(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getNCharacterStream(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getNCharacterStream(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public Reader getNCharacterStream(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getNCharacterStream(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getCharacterStream(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public NClob getNClob(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getNClob(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getNClob(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public NClob getNClob(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getNClob(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getNClob(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getNString(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getNString(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getNString(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getNString(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getNString(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getNString(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public RowId getRowId(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getRowId(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getRowId(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public RowId getRowId(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getRowId(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getRowId(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public SQLXML getSQLXML(int parameterIndex) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getSQLXML(%s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex);

            return cs.getSQLXML(parameterIndex);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public SQLXML getSQLXML(String parameterName) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getSQLXML(%s)",
                                jndiName, spyLoggingCategory,
                                parameterName);

            return cs.getSQLXML(parameterName);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setAsciiStream(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, x, length);

            cs.setAsciiStream(parameterName, x, length);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setAsciiStream(String parameterName, InputStream x) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setAsciiStream(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, x);

            cs.setAsciiStream(parameterName, x);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setBinaryStream(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, x, length);
            
            cs.setBinaryStream(parameterName, x, length);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setBinaryStream(String parameterName, InputStream x) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setBinaryStream(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, x);

            cs.setBinaryStream(parameterName, x);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setBlob(String parameterName, Blob x) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setBlob(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, x);

            cs.setBlob(parameterName, x);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setBlob(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, inputStream, length);

            cs.setBlob(parameterName, inputStream, length);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setBlob(String parameterName, InputStream inputStream) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setBlob(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, inputStream);

            cs.setBlob(parameterName, inputStream);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setCharacterStream(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, reader, length);

            cs.setCharacterStream(parameterName, reader, length);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setCharacterStream(String parameterName, Reader reader) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setCharacterStream(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, reader);

            cs.setCharacterStream(parameterName, reader);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setClob(String parameterName, Clob x) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setClob(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, x);

            cs.setClob(parameterName, x);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setClob(String parameterName, Reader reader, long length) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setClob(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, reader, length);

            cs.setClob(parameterName, reader, length);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setClob(String parameterName, Reader reader) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setClob(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, reader);

            cs.setClob(parameterName, reader);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setNCharacterStream(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value, length);

            cs.setNCharacterStream(parameterName, value, length);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setNCharacterStream(String parameterName, Reader value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setNCharacterStream(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setNCharacterStream(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setNClob(String parameterName, NClob value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setNClob(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setNClob(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setNClob(String parameterName, Reader reader, long length) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setNClob(%s, %s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, reader, length);

            cs.setNClob(parameterName, reader, length);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setNClob(String parameterName, Reader reader) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setNClob(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, reader);

            cs.setNClob(parameterName, reader);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setNString(String parameterName, String value) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setNString(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, value);

            cs.setNString(parameterName, value);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setRowId(String parameterName, RowId x) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setRowId(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, x);

            cs.setRowId(parameterName, x);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] setSQLXML(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, xmlObject);

            cs.setSQLXML(parameterName, xmlObject);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getObject(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterIndex, type);

            return cs.getObject(parameterIndex, type);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public <T> T getObject(String parameterName, Class<T> type) throws SQLException
   {
      if (doLocking)
         lock();
      try
      {
         checkState();
         try
         {
            if (spy)
               spyLogger.debugf("%s [%s] getObject(%s, %s)",
                                jndiName, spyLoggingCategory,
                                parameterName, type);

            return cs.getObject(parameterName, type);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         if (doLocking)
            unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   protected CallableStatement getWrappedObject() throws SQLException
   {
      return cs;
   }
}
