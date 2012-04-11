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
 *
 * A DataSource.
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface DataSource extends org.jboss.jca.common.api.metadata.ds.DataSource
{
   /**
    * A Tag.
    */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * pool tag
       */
      POOL("pool"),
      /**
       * connection-url tag
       */
      CONNECTION_URL("connection-url"),
      /**
      * driverClass tag
      */
      DRIVER_CLASS("driver-class"),
      /**
      * dataSourceClass tag
      */
      DATASOURCE_CLASS("datasource-class"),
      /**
      * module tag
      */
      DRIVER("driver"),
      /**
      * transactionIsolation tag
      */
      TRANSACTION_ISOLATION("transaction-isolation"),
      /**
      * connectionProperty tag
      */
      CONNECTION_PROPERTY("connection-property"),
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
      NEW_CONNECTION_SQL("new-connection-sql");

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

   /**
    * An Attribute.
    */
   public enum Attribute
   {

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
      USE_CCM("use-ccm"),

      /**
       * jta attribute
       */
      JTA("jta");

      private final String name;

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
   }
}
