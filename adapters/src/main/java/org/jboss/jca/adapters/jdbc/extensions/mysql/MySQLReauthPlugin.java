/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.adapters.jdbc.extensions.mysql;

import org.jboss.jca.adapters.jdbc.spi.reauth.ReauthPlugin;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * MySQL plugin for reauthentication
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class MySQLReauthPlugin implements ReauthPlugin
{
   /**
    * Default constructor
    */
   public MySQLReauthPlugin()
   {
   }

   /**
    * Initialize
    * @param cl The class loader which can be used for initialization
    * @exception SQLException Thrown in case of an error
    */
   public synchronized void initialize(ClassLoader cl) throws SQLException
   {
      Class<?> mysqlConnection = null;

      try
      {
         mysqlConnection = Class.forName("com.mysql.jdbc.Connection", true, cl);
      }
      catch (Throwable t) 
      {
         // Ignore
      }

      if (mysqlConnection == null)
      {
         try
         {
            mysqlConnection = Class.forName("com.mysql.jdbc.Connection", true, getClass().getClassLoader());
         }
         catch (Throwable t) 
         {
            // Ignore
         }
      }

      if (mysqlConnection == null)
      {
         try
         {
            ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
            mysqlConnection = Class.forName("com.mysql.jdbc.Connection", true, tccl);
         }
         catch (Throwable t) 
         {
            throw new SQLException("Cannot resolve com.mysq.jdbc.Connection", t);
         }
      }

      try
      {
         Method changeUser = mysqlConnection.getMethod("changeUser", new Class[] {String.class, String.class});
      }
      catch (Throwable t)
      {
         throw new SQLException("Cannot resolve com.mysq.jdbc.Connection changeUser method", t);
      }
   }

   /**
    * Reauthenticate
    * @param c The connection
    * @param userName The user name
    * @param password The password
    * @exception SQLException Thrown in case of an error
    */
   public synchronized void reauthenticate(Connection c, String userName, String password) throws SQLException
   {
      Object[] params = new Object[] {userName, password};
      try
      {
         Method changeUser = c.getClass().getMethod("changeUser", new Class[] {String.class, String.class});
         changeUser.setAccessible(true);
         changeUser.invoke(c, params);
      }
      catch (Throwable t) 
      {
         Throwable cause = t.getCause();

         if (cause instanceof SQLException)
         {
            throw (SQLException)cause;
         }
         else
         {
            throw new SQLException("Unexpected error in changeUser", t);
         }
      }
   }
}
