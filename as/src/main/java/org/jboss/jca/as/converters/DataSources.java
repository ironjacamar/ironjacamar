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
package org.jboss.jca.as.converters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * DataSources
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public interface DataSources
{
   /**
    * Get the local tx datasource.
    *
    * @return the local tx datasource.
    */
   public List<LocalTxDataSource> getLocalTxDataSource();

   /**
    * Get the No Tx datasource.
    *
    * @return the NoTx datasource.
    */
   public List<NoTxDataSource> getNoTxDataSource();

   /**
    * Get the xaDataSource.
    *
    * @return the xaDataSource.
    */
   public List<XaDataSource> getXaDataSource();

   /**
   *
   * A Tag.
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   *
   */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * no-tx-datasource tag
       */
      NO_TX_DATASOURCE("no-tx-datasource"),

      /**
       * no-tx-datasource tag
       */
      LOCAL_TX_DATASOURCE("local-tx-datasource"),
      
      /**
       * xa-datasource tag
       */
      XA_DATASOURCE("xa-datasource");

      private final String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Tag(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }

      /**
       * {@inheritDoc}
       */
      public String toString()
      {
         return name;
      }

      private static final Map<String, Tag> MAP;

      static
      {
         final Map<String, Tag> map = new HashMap<String, Tag>();
         for (Tag element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAP = map;
      }

      /**
      *
      * Static method to get enum instance given localName XsdString
      *
      * @param localName a XsdString used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
      }
   }
}
