/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.as.converters.wls.api.metadata.v13;

import org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * A WeblogicConnector v1.3.
 *
 * @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
 *
 */
public interface WeblogicConnector13 extends WeblogicConnector
{
   /**
    * VERSION constant
    */
   public static final String XML_VERSION = "1.3";

   /**
   *
   * A Tag.
   *
   * @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
   *
   */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * module-name tag
       */
      MODULE_NAME("module-name"),

      /**
       * vendor-name tag
       */
      VENDOR_NAME("vendor-name"),

      /**
       * native-libdir tag
       */
      NATIVE_LIBDIR("native-libdir"),
      
      /**
       * jndi-name tag
       */
      JNDI_NAME("jndi-name"),
      
      /**
       * enable-access-outside-app tag
       */
      ENABLE_ACCESS_OUTSIDE_APP("enable-access-outside-app"),
      
      /**
       * enable-global-access-to-classes tag
       */
      ENABLE_GLOBAL_ACCESS_TO_CLASSES("enable-global-access-to-classes"),
      
      /**
       * work-manager tag
       */
      WORK_MANAGER("work-manager"),
      
      /**
       * connector-work-manager tag
       */
      CONNECTOR_WORK_MANAGER("connector-work-manager"),
      
      /**
       * security tag
       */
      RESOURCE_ADAPTER_SECURITY("security"),
      
      /**
       * properties tag
       */
      PROPERTIES("properties"),
      
      /**
       * admin-objects tag
       */
      ADMIN_OBJECTS("admin-objects"),
      
      /**
       * outbound-resource-adapter tag
       */
      OUTBOUND_RESOURCE_ADAPTER("outbound-resource-adapter");
      
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
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
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
    * @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
    *
    */
   public enum Attribute
   {
      /** unknown attribute
       *
       */
      UNKNOWN(null),

      /** id attribute
       *
       */
      ID("id"),

      /**
       * version attribute
       */
      VERSION("version");

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
