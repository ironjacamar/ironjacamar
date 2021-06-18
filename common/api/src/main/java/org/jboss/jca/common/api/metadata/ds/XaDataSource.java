/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.api.metadata.ds;

import org.jboss.jca.common.api.metadata.common.Recovery;

import java.util.HashMap;
import java.util.Map;

/**
 * An XaDataSource.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
public interface XaDataSource extends CommonDataSource
{
   /**
    * Get the xaDataSourceClass.
    *
    * @return the xaDataSourceClass.
    */
   public String getXaDataSourceClass();

   /**
    * Get the statement.
    *
    * @return the statement.
    */
   public Statement getStatement();

   /**
    * Get the urlDelimiter.
    *
    * @return the urlDelimiter.
    */
   public String getUrlDelimiter();

   /**
    * Get the urlSelectorStrategyClassName.
    *
    * @return the urlSelectorStrategyClassName.
    */
   public String getUrlSelectorStrategyClassName();

   /**
    * Get the newConnectionSql.
    *
    * @return the newConnectionSql.
    */
   public String getNewConnectionSql();

   /**
    * Get the xaDataSourceProperty.
    *
    * @return the xaDataSourceProperty.
    */
   public Map<String, String> getXaDataSourceProperty();

   /**
    * Get the xaPool.
    *
    * @return the xaPool.
    */
   public DsXaPool getXaPool();

   /**
    * Get the recovery settings.
    *
    * @return the recovery settings.
    */
   public Recovery getRecovery();

   /**
    * Get the url property
    * @return The value
    */
   public String getUrlProperty();

   /**
    * Get the connectable flag
    * @return The value
    */
   public Boolean isConnectable();

   /**
    * Get the tracking flag
    * @return <code>null</code> is container default, a value is an override
    */
   public Boolean isTracking();

   /**
    * A Tag.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * xa-datasource-property tag
       */
      XA_DATASOURCE_PROPERTY("xa-datasource-property"),

      /**
       * xa-datasource-class tag
       */
      XA_DATASOURCE_CLASS("xa-datasource-class"),

      /**
       * driver tag
       */
      DRIVER("driver"),

      /**
       * transaction-isolation tag
       */
      TRANSACTION_ISOLATION("transaction-isolation"),

      /**
       * timeout tag
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
       * url-delimiter tag
       */
      URL_DELIMITER("url-delimiter"),

      /**
       * url-property tag
       */
      URL_PROPERTY("url-property"),

      /**
       * url-selector-strategy-class-name tag
       */
      URL_SELECTOR_STRATEGY_CLASS_NAME("url-selector-strategy-class-name"),

      /**
       * new-connection-sql tag
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
       * Create a new Tag.
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
    * An attribute.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    */
   public enum Attribute
   {
      /** unknown attribute
       *
       */
      UNKNOWN(null),

      /** jndi-name attribute
       *
       */
      JNDI_NAME("jndi-name"),

      /** pool-name attribute
       *
       */
      POOL_NAME("pool-name"),

      /** enabled attribute
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

      /** connectable attribute
      *
      */
      CONNECTABLE("connectable"),

      /** tracking attribute
      *
      */
      TRACKING("tracking"),

      /**
       * mcp attribute
       */
      MCP("mcp"),

      /**
       * enlistment trace attribute
       */
      ENLISTMENT_TRACE("enlistment-trace");

      private String name;

      /**
       * Create a new Tag.
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
