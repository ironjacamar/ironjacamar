/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.adapters.jdbc.extensions.novendor;

import org.jboss.jca.adapters.jdbc.spi.ValidConnectionChecker;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Use JDBC4 for connection validation
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class JDBC4ValidConnectionChecker implements ValidConnectionChecker, Serializable
{
   private static final long serialVersionUID = 1L;

   private int pingTimeout;

   /**
    * Constructor
    */
   public JDBC4ValidConnectionChecker()
   {
      pingTimeout = 5;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SQLException isValidConnection(Connection c)
   {
      try
      {
         boolean result = c.isValid(pingTimeout);

         if (!result)
            return new SQLException("Invalid connection: " + c);
      }
      catch (SQLException sqle)
      {
         return sqle;
      }

      return null;
   }

   /**
    * Get the pingTimeOut.
    *
    * @return the pingTimeOut.
    */
   public int getPingTimeOut()
   {
      return pingTimeout;
   }

   /**
    * Set the pingTimeOut.
    *
    * @param v The pingTimeOut to set.
    */
   public void setPingTimeOut(int v)
   {
      this.pingTimeout = v;
   }
}
