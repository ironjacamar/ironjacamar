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

package org.jboss.jca.adapters.jdbc.extensions.novendor;

import org.jboss.jca.adapters.jdbc.spi.ValidConnectionChecker;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Always throws a SQLException
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class SQLExceptionValidConnectionChecker implements ValidConnectionChecker, Serializable
{
   private static final long serialVersionUID = 1L;

   private String reason;
   private String sqlState;
   private Integer vendorCode;

   /**
    * Constructor
    */
   public SQLExceptionValidConnectionChecker()
   {
      this.reason = null;
      this.sqlState = null;
      this.vendorCode = null;
   }

   /**
    * Get the reason
    * @return The value
    */
   public String getReason()
   {
      return reason;
   }

   /**
    * Set the reason
    * @param v The value
    */
   public void setReason(String v)
   {
      reason = v;
   }

   /**
    * Get the SQL state
    * @return The value
    */
   public String getSQLState()
   {
      return sqlState;
   }

   /**
    * Set the SQL state
    * @param v The value
    */
   public void setSQLState(String v)
   {
      sqlState = v;
   }

   /**
    * Get the vendor code
    * @return The value
    */
   public Integer getVendorCode()
   {
      return vendorCode;
   }

   /**
    * Set the vendor code
    * @param v The value
    */
   public void setVendorCode(Integer v)
   {
      vendorCode = v;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SQLException isValidConnection(Connection c)
   {
      if (reason != null && sqlState != null && vendorCode != null)
      {
         return new SQLException(reason, sqlState, vendorCode.intValue());
      }
      else if (reason != null && sqlState != null)
      {
         return new SQLException(reason, sqlState);
      }
      else if (reason != null)
      {
         return new SQLException(reason);
      }
      else
      {
         return new SQLException("SQLExceptionValidConnectionChecker");
      }
   }
}
