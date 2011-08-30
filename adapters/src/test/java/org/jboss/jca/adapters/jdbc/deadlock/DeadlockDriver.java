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

package org.jboss.jca.adapters.jdbc.deadlock;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Deadlock JDBC driver
 */
public class DeadlockDriver implements Driver
{
   private static final String PREFIX = "deadlock:";

   /**
    * {@inheritDoc}
    */
   public Connection connect(String url, Properties info) throws SQLException
   {
      if (isOurUrl(url))
      {
         Connection connection = DriverManager.getConnection(getUrl(url), info);
         Connection result =
            (Connection)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                               new Class[] {Connection.class},
                                               new DeadlockConnectionHandler(connection));
         return result;
      }

      throw new SQLException("Incorrect url: " + url);
   }

   /**
    * {@inheritDoc}
    */
   public boolean acceptsURL(String url) throws SQLException
   {
      return isOurUrl(url);
   }

   /**
    * {@inheritDoc}
    */
   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
   {
      if (isOurUrl(url))
      {
         String driverUrl = getUrl(url);
         Driver driver = DriverManager.getDriver(driverUrl);
         return driver.getPropertyInfo(driverUrl, info);
      }

      throw new SQLException("Incorrect url: " + url);
   }

   /**
    * {@inheritDoc}
    */
   public int getMajorVersion()
   {
      return 1;
   }

   /**
    * {@inheritDoc}
    */
   public int getMinorVersion()
   {
      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public boolean jdbcCompliant()
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      throw new SQLFeatureNotSupportedException();
   }

   private boolean isOurUrl(final String url)
   {
      return url.startsWith(PREFIX);
   }

   private String getUrl(final String url)
   {
      if (isOurUrl(url))
      {
         return url.substring(PREFIX.length());
      }
      else
      {
         return url;
      }
   }
}
