/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.extensions.oracle;

import org.jboss.jca.adapters.jdbc.spi.reauth.ReauthPlugin;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Oracle plugin for reauthentication.
 *
 * @author <a href="mailto:ricardo.arguello@gmail.com">Ricardo Arguello</a>
 */
public class OracleReauthPlugin implements ReauthPlugin
{
   /**
    * Default constructor
    */
   public OracleReauthPlugin()
   {
   }

   /**
    * Initialize
    * @param cl The class loader which can be used for initialization
    * @exception SQLException Thrown in case of an error
    */
   public synchronized void initialize(ClassLoader cl) throws SQLException
   {
      Class<?> oracleConnection = null;

      try
      {
         oracleConnection = Class.forName("oracle.jdbc.OracleConnection", true, cl);
      }
      catch (Throwable t)
      {
         // Ignore
      }

      if (oracleConnection == null)
      {
         try
         {
            oracleConnection = Class.forName("oracle.jdbc.OracleConnection", true, getClass().getClassLoader());
         }
         catch (Throwable t)
         {
            // Ignore
         }
      }

      if (oracleConnection == null)
      {
         try
         {
            ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
            oracleConnection = Class.forName("oracle.jdbc.OracleConnection", true, tccl);
         }
         catch (Throwable t)
         {
            throw new SQLException("Cannot resolve oracle.jdbc.OracleConnection", t);
         }
      }

      try
      {
         oracleConnection.getMethod("openProxySession", new Class<?>[] {int.class, Properties.class});
      }
      catch (Throwable t)
      {
         throw new SQLException("Cannot resolve oracle.jdbc.OracleConnection openProxySession method", t);
      }

      try
      {
         oracleConnection.getField("PROXYTYPE_USER_NAME");
      }
      catch (Throwable t)
      {
         throw new SQLException("Cannot resolve oracle.jdbc.OracleConnection PROXYTYPE_USER_NAME field", t);
      }

      try
      {
         oracleConnection.getField("PROXY_USER_NAME");
      }
      catch (Throwable t)
      {
         throw new SQLException("Cannot resolve oracle.jdbc.OracleConnection PROXY_USER_NAME field", t);
      }

      try
      {
         oracleConnection.getField("PROXY_USER_PASSWORD");
      }
      catch (Throwable t)
      {
         throw new SQLException("Cannot resolve oracle.jdbc.OracleConnection PROXY_USER_PASSWORD field", t);
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
      try
      {
         int proxyTypeUserName = c.getClass().getField("PROXYTYPE_USER_NAME").getInt(c);
         String proxyUserName = (String) c.getClass().getField("PROXY_USER_NAME").get(c);
         String proxyPassword = (String) c.getClass().getField("PROXY_USER_PASSWORD").get(c);

         Properties props = new Properties();
         props.put(proxyUserName, userName);

         if (password != null)
         {
            props.put(proxyPassword, password);
         }

         Object[] params = new Object[] {proxyTypeUserName, props};

         Method openProxySession = c.getClass().getMethod("openProxySession",
                                                          new Class<?>[] {int.class, Properties.class});
         SecurityActions.setAccessible(openProxySession);
         openProxySession.invoke(c, params);
      }
      catch (Throwable t)
      {
         Throwable cause = t.getCause();

         if (cause instanceof SQLException)
         {
            throw (SQLException) cause;
         }
         else
         {
            throw new SQLException("Unexpected error in openProxySession", t);
         }
      }
   }
}
