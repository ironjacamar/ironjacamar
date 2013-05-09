/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.api.metadata.common.v11;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WorkManager security settings
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface WorkManagerSecurity extends Serializable
{
   /**
    * Is mapping required
    * @return The value
    */
   public boolean isMappingRequired();

   /**
    * Get the domain
    * @return The value
    */
   public String getDomain();

   /**
    * Get the default principal
    * @return The value
    */
   public String getDefaultPrincipal();

   /**
    * Get the default groups
    * @return The value
    */
   public List<String> getDefaultGroups();

   /**
    * Get the user mapping
    * @return The value
    */
   public Map<String, String> getUserMappings();

   /**
    * Get the group mapping
    * @return The value
    */
   public Map<String, String> getGroupMappings();

   /**
    * A Tag.
    *
    * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
    */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * mapping-required tag
       */
      MAPPING_REQUIRED("mapping-required"),

      /**
       * domain tag
       */
      DOMAIN("domain"),

      /**
       * default-principal tag
       */
      DEFAULT_PRINCIPAL("default-principal"),

      /**
       * default-groups tag
       */
      DEFAULT_GROUPS("default-groups"),

      /**
       * group tag
       */
      GROUP("group"),

      /**
       * mappings tag
       */
      MAPPINGS("mappings"),

      /**
       * users tag
       */
      USERS("users"),

      /**
       * groups tag
       */
      GROUPS("groups"),

      /**
       * map tag
       */
      MAP("map");

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

      private static final Map<String, Tag> MAPE;

      static
      {
         final Map<String, Tag> map = new HashMap<String, Tag>();
         for (Tag element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAPE = map;
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
         final Tag element = MAPE.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }

   /**
    * An Attribute.
    *
    * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
    */
   public enum Attribute
   {
      /** always first */
      UNKNOWN(null),

      /** from */
      FROM("from"),

      /** to */
      TO("to");

      private String name;

      /**
       * Constructor
       * @param name a name
       */
      Attribute(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this attribute
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
