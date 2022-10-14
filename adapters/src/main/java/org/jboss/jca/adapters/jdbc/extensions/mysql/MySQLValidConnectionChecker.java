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

package org.jboss.jca.adapters.jdbc.extensions.mysql;

import org.jboss.jca.adapters.jdbc.CheckValidConnectionSQL;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implements check valid connection sql Requires MySQL driver 3.1.8 or later.
 * This should work on just about any version of the database itself but will
 * only be "fast" on version 3.22.1 and later. Prior to that version it just
 * does "SELECT 1" anyhow.
 *
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author Andrew C. Oliver
 * @author Jim Moran
 * @version $Revision: 78074 $
 */
public class MySQLValidConnectionChecker extends CheckValidConnectionSQL
{
   private static final long serialVersionUID = 1323747853035005642L;

   private static final String QUERY = "SELECT 1";

   /**
    * Constructor
    */
   public MySQLValidConnectionChecker()
   {
      super(QUERY);
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
         ping = SecurityActions.getMethod(c.getClass(), "ping", (Class[])null);
         SecurityActions.setAccessible(ping);
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
               return new SQLException("Ping failed: " + e.toString(), e);
            }
         }
      }
      else
      {
         return super.isValidConnection(c);
      }

      return null;
   }
}
