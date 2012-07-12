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

package org.jboss.jca.adapters.jdbc.spi;

import javax.sql.XADataSource;

/**
 * XAData entry
 */
public final class XAData
{
   private final XADataSource xaDataSource;
   private final String url;

   /**
    * Constructor
    * @param xaDataSource The XADataSource
    * @param url The URL for the datasource
    */
   public XAData(final XADataSource xaDataSource, final String url)
   {
      this.xaDataSource = xaDataSource;
      this.url = url;
   }

   /**
    * Get the XADataSource
    * @return The value
    */
   public XADataSource getXADataSource()
   {
      return xaDataSource;
   }
   
   /**
    * Get the URL
    * @return The value
    */
   public String getUrl()
   {
      return url;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object o)
   {
      if (o == null)
         return false;

      if (this == o)
         return true;

      if (!(o instanceof XAData))
         return false;

      final XAData xaData = (XAData)o;

      if (!url.equals(xaData.getUrl()))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      return url.hashCode();
   }
   
   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("XAData@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[xaDataSource=").append(xaDataSource);
      sb.append(" url=").append(url);
      sb.append("]");

      return sb.toString();
   }
}
