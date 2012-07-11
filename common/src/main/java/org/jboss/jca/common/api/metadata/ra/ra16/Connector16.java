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
package org.jboss.jca.common.api.metadata.ra.ra16;

import org.jboss.jca.common.api.metadata.ra.Icon;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.ra15.Connector15;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * A Connector16.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public interface Connector16 extends Connector15
{
   /**
    * VERSION constant
    */
   public static final String XML_VERSION = "1.6";

   /**
    * @return requiredWorkContext
    */
   public abstract List<String> getRequiredWorkContexts();

   /**
    * @return moduleName
    */
   public abstract String getModuleName();

   /**
    * @return description
    */
   public abstract List<LocalizedXsdString> getDescriptions();

   /**
    * @return displayName
    */
   public abstract List<LocalizedXsdString> getDisplayNames();

   /**
    * @return icon
    */
   public abstract List<Icon> getIcons();

   /**
    * @return metadataComplete
    */
   public abstract boolean isMetadataComplete();


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
       * module-name tag
       */
      MODULE_NAME("module-name"),

      /**
       * vendor-name tag
       */
      VENDOR_NAME("vendor-name"),

      /**
       * eis-type tag
       */
      EIS_TYPE("eis-type"),

      /**
       * resourceadapter-version tag
       */
      RESOURCEADPTER_VERSION("resourceadapter-version"),

      /**
       * license tag
       */
      LICENSE("license"),

      /**
       * resourceadapter tag
       */
      RESOURCEADAPTER("resourceadapter"),

      /**
       * required-work-context tag
       */
      REQUIRED_WORK_CONTEXT("required-work-context"),

      /**
       * description tag
       */
      DESCRIPTION("description"),

      /**
       * icon tag
       */
      ICON("icon"),

      /**
       * display-name tag
       */
      DISPLAY_NAME("display-name");

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
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
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
       * metadata-complete attribute
       */
      METADATA_COMPLETE("metadata-complete"),

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
