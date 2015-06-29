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

import org.jboss.jca.adapters.jdbc.CachedCallableStatement;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.SQLType;

/**
 * CachedCallableStatementJDK7.
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@SuppressWarnings("deprecation")
public class CachedCallableStatementJDK8 extends CachedCallableStatement
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    * @param cs The callable statement
    * @exception SQLException Thrown if an error occurs
    */
   public CachedCallableStatementJDK8(CallableStatement cs) throws SQLException
   {
      super(cs);
   }

   /**
    * {@inheritDoc}
    */
   public void setObject(String parameterName, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException
   {
      getWrappedObject().setObject(parameterName, x, targetSqlType, scaleOrLength);
   }

   /**
    * {@inheritDoc}
    */
   public void setObject(String parameterName, Object x, SQLType targetSqlType) throws SQLException
   {
      getWrappedObject().setObject(parameterName, x, targetSqlType);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex, SQLType sqlType) throws SQLException
   {
      getWrappedObject().registerOutParameter(parameterIndex, sqlType);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex, SQLType sqlType, int scale) throws SQLException
   {
      getWrappedObject().registerOutParameter(parameterIndex, sqlType, scale);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(int parameterIndex, SQLType sqlType, String typeName) throws SQLException
   {
      getWrappedObject().registerOutParameter(parameterIndex, sqlType, typeName);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName, SQLType sqlType) throws SQLException
   {
      getWrappedObject().registerOutParameter(parameterName, sqlType);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName, SQLType sqlType, int scale) throws SQLException
   {
      getWrappedObject().registerOutParameter(parameterName, sqlType, scale);
   }

   /**
    * {@inheritDoc}
    */
   public void registerOutParameter(String parameterName, SQLType sqlType, String typeName) throws SQLException
   {
      getWrappedObject().registerOutParameter(parameterName, sqlType, typeName);
   }
}
