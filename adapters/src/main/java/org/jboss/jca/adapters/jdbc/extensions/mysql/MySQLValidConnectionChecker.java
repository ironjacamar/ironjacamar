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
 * Implements check valid connection sql Requires MySQL driver 3.1.8 or later.
 * This should work on just about any version of the database itself but will
 * only be "fast" on version 3.22.1 and later. Prior to that version it just
 * does "SELECT 1" anyhow.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="mailto:acoliver ot jbosss dat org">Andrew C. Oliver</a>
 * @author <a href="mailto:jim.moran@jboss.org">Jim Moran</a>
 * @version $Revision: 78074 $
 */
public class MySQLValidConnectionChecker implements ValidConnectionChecker, Serializable
{
   private static Logger log = Logger.getLogger(MySQLValidConnectionChecker.class);

   private static final long serialVersionUID = 1323747853035005642L;

   /**
    * Constructor
    */
   public MySQLValidConnectionChecker()
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SQLException isValidConnection(Connection c)
   {
      Method ping = null;

      try
      {
         ping = c.getClass().getMethod("ping", (Class[])null);
         ping.setAccessible(true);
      }
      catch (Throwable t)
      {
         // Ignore
      }

      //if there is a ping method then use it, otherwise just use a 'SELECT 1' statement
      if (ping != null)
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
            try
            {
               if (rs != null)
                  rs.close();
            }
            catch (SQLException ignore)
            {
               // Ignore
            }
            try
            {
               if (stmt != null)
                  stmt.close();
            }
            catch (SQLException ignore)
            {
               // Ignore
            }
         }
      }

      return null;
   }
}
