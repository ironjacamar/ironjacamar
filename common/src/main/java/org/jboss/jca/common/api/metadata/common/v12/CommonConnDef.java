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
package org.jboss.jca.common.api.metadata.common.v12;

import java.util.HashMap;
import java.util.Map;

/**
 * A ConnectionDefinition.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface CommonConnDef extends org.jboss.jca.common.api.metadata.common.v11.CommonConnDef
{
   /**
    * Get the connectable flag
    * @return True, if connectable should be supported
    */
   public Boolean isConnectable();

   /**
    * Get the tracking flag
    * @return <code>null</code> is container default, a value is an override
    */
   public Boolean isTracking();

   /**
    *
    * A Tag.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * config-property tag
       */
      CONFIG_PROPERTY("config-property"),
      /**
       * pool tag
       */
      XA_POOL("xa-pool"),
      /**
       * pool tag
       */
      POOL("pool"),
      /**
       * security tag
       */
      SECURITY("security"),
      /**
       * timeout tag
       */
      TIMEOUT("timeout"),
      /**
       * validation tag
       */
      VALIDATION("validation"),

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
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum Attribute
   {

      /** always first
      *
      */
      UNKNOWN(null),
      /** jndiName attribute
        *
        */
      JNDI_NAME("jndi-name"),

      /** class-name attribute
      *
      */
      CLASS_NAME("class-name"),

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
      /** use-ccm attribute
      *
      */
      USE_CCM("use-ccm"),
      /** sharable attribute
      *
      */
      SHARABLE("sharable"),
      /** enlistment attribute
      *
      */
      ENLISTMENT("enlistment"),
      /** connectable attribute
      *
      */
      CONNECTABLE("connectable"),
      /** tracking attribute
      *
      */
      TRACKING("tracking");

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
