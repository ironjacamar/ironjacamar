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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

import javax.resource.spi.ManagedConnectionMetaData;

/**
 * Managed connection metadata
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ManagedConnectionMetaDataImpl implements ManagedConnectionMetaData, Serializable
{
   /** Serial version UID */
   private static final long serialVersionUID = 1L;

   /** Product name */
   private String productName;

   /** Product version */
   private String productVersion;

   /** Max connections */
   private int maxConnections;

   /** User */
   private String user;

   /**
    * Constructor
    * @param connection The connection
    * @param user The user
    */
   ManagedConnectionMetaDataImpl(Connection connection, String user)
   {
      try
      {
         if (connection != null && connection.getMetaData() != null)
         {
            productName = connection.getMetaData().getDatabaseProductName();
            productVersion = connection.getMetaData().getDatabaseProductVersion();
         }
      }
      catch (SQLException se)
      {
         // Nothing to do
      }

      if (productName == null)
         productName = "";

      if (productVersion == null)
         productVersion = "";

      this.maxConnections = 1;
      this.user = user;
   }

   /**
    * {@inheritDoc}
    */
   public String getEISProductName()
   {
      return productName;
   }

   /**
    * {@inheritDoc}
    */
   public String getEISProductVersion()
   {
      return productVersion;
   }
    
   /**
    * {@inheritDoc}
    */
   public int getMaxConnections()
   {
      return maxConnections;
   }

   /**
    * {@inheritDoc}
    */
   public String getUserName()
   {
      return user;
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ManagedConnectionMetaDataImpl@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[");
      sb.append(" productName=").append(productName);
      sb.append(" productVersion=").append(productVersion);
      sb.append(" maxConnections=").append(maxConnections);
      sb.append(" user=").append(user);
      sb.append("]");

      return sb.toString();
   }
}
