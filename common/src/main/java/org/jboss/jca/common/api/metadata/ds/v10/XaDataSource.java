/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.api.metadata.ds.v10;

import java.util.HashMap;
import java.util.Map;

/**
 * An XaDataSource.
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface XaDataSource extends org.jboss.jca.common.api.metadata.ds.XaDataSource
{
   /**
    * A Tag.
    *
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
    */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
      * xaDatasourceProperty tag
      */
      XA_DATASOURCE_PROPERTY("xa-datasource-property"),
      /**
      * xaDatasourceClass tag
      */
      XA_DATASOURCE_CLASS("xa-datasource-class"),
      /**
      * module tag
      */
      DRIVER("driver"),
      /**
      * transactionIsolation tag
      */
      TRANSACTION_ISOLATION("transaction-isolation"),
      /**
      * timeOut tag
      */
      TIMEOUT("timeout"),
      /**
      * security tag
      */
      SECURITY("security"),
      /**
      * statement tag
      */
      STATEMENT("statement"),
      /**
      * validation tag
      */
      VALIDATION("validation"),
      /**
      * urlDelimiter tag
      */
      URL_DELIMITER("url-delimiter"),
      /**
      * urlSelectorStrategyClassName tag
      */
      URL_SELECTOR_STRATEGY_CLASS_NAME("url-selector-strategy-class-name"),
      /**
      * newConnectionSql tag
      */
      NEW_CONNECTION_SQL("new-connection-sql"),

      /**
       * xa-pool tag
       */
      XA_POOL("xa-pool"),

      /** recovery tag */
      RECOVERY("recovery");

      private String name;

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
       * Set the value
       * @param v The name
       * @return The value
       */
      Tag value(String v)
      {
         name = v;
         return this;
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
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

   /**
    *
    * A Attribute.
    *
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
    *
    */
   public enum Attribute
   {
      /** unknown attribute
       *
       */
      UNKNOWN(null),

      /** jndiName attribute
       *
       */
      JNDI_NAME("jndi-name"),

      /** jndiName attribute
      *
      */
      POOL_NAME("pool-name"),

      /** jndiName attribute
      *
      */
      ENABLED("enabled"),
      /** use-java-context attribute
      *
      */
      USE_JAVA_CONTEXT("use-java-context"),

      /** spy attribute
      *
      */
      SPY("spy"),

      /** use-ccm attribute
      *
      */
      USE_CCM("use-ccm");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Attribute(final String name)
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

      private static final Map<String, Attribute> MAP;

      static
      {
         final Map<String, Attribute> map = new HashMap<String, Attribute>();
         for (Attribute element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAP = map;
      }

      /**
       * Set the value
       * @param v The name
       * @return The value
       */
      Attribute value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName XsdString
      *
      * @param localName a XsdString used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Attribute forName(String localName)
      {
         final Attribute element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }
}
