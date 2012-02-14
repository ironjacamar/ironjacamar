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

package org.jboss.jca.adapters.jdbc.extensions.mysql;

import org.jboss.jca.adapters.jdbc.spi.ValidConnectionChecker;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.logging.Logger;

/**
 * <p>This class is an implementation of ValidConnectionChecker for MySQL
 * ReplicatedDriver. It supports both isValid and ping methods on the
 * connection object.
 *
 * <p>Please note that the isValid method requires java 6 classes to be present.
 *
 * <p>The code was inspired by MySQLValidConnectionChecker. See it's javadoc for
 * authors info. This code is released under the LGPL license.
 *
 * @author Luc Boudreau (lucboudreau att gmail dott com)
 *
 */
public class MySQLReplicationValidConnectionChecker implements ValidConnectionChecker, Serializable
{
   private static Logger log = Logger.getLogger(MySQLReplicationValidConnectionChecker.class);

   /**
    * Serial version ID
    */
   private static final long serialVersionUID = 2658231045989623858L;

   /**
    * Initiates the ValidConnectionChecker implementation.
    */
   public MySQLReplicationValidConnectionChecker()
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SQLException isValidConnection(Connection c)
   {
      Method isValid = null;
      Method ping = null;

      try
      {
         isValid = c.getClass().getMethod("isValid", new Class<?>[] {});
         isValid.setAccessible(true);
      }
      catch (Throwable t)
      {
         // Ignore
      }

      if (isValid == null)
      {
         try
         {
            ping = c.getClass().getMethod("ping", (Class[])null);
            ping.setAccessible(true);
         }
         catch (Throwable t)
         {
            // Ignore
         }
      }

      if (isValid != null)
      {
         try
         {
            isValid.invoke(c, new Object[] {});
         }
         catch (Exception e)
         {
            if (e instanceof SQLException)
            {
               return (SQLException) e;
            }
            else
            {
               log.warn("Unexpected error", e);
               return new SQLException("IsValid failed: " + e.toString());
            }
         }
      }
      else if (ping != null)
      {
         try
         {
            ping.invoke(c, (Object[])null);
         }
         catch (Exception e)
         {
            if (e instanceof SQLException)
            {
               return (SQLException) e;
            }
            else
            {
               log.warn("Unexpected error", e);
               return new SQLException("Ping failed: " + e.toString());
            }
         }
      }
      else
      {
         //otherwise just use a 'SELECT 1' statement
         Statement stmt = null;
         ResultSet rs = null;

         try
         {
            stmt = c.createStatement();
            rs = stmt.executeQuery("SELECT 1");
         }
         catch (Exception e)
         {
            if (e instanceof SQLException)
            {
               return (SQLException) e;
            }
            else
            {
               log.warn("Unexpected error", e);
               return new SQLException("SELECT 1 failed: " + e.toString());
            }
         }
         finally
         {
            // Cleanup everything and make sure to handle
            // sql exceptions occuring
            try
            {
               if (rs != null)
                  rs.close();
            }
            catch (SQLException e)
            {
               // Ignore
            }
            try
            {
               if (stmt != null)
                  stmt.close();
            }
            catch (SQLException e)
            {
               // Ignore
            }
         }
      }

      return null;
   }
}
