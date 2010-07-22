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
package org.jboss.jca.common.metadata.ra.ra16;

import org.jboss.jca.common.metadata.ra.common.ConfigProperty;
import org.jboss.jca.common.metadata.ra.common.LocalizedXsdString;
import org.jboss.jca.common.metadata.ra.common.XsdString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class ConfigProperty16 extends ConfigProperty
{
   /**
    */
   private static final long serialVersionUID = -2025203811838727421L;

   private final Boolean configPropertyIgnore;

   private final Boolean configPropertySupportsDynamicUpdates;

   private final Boolean configPropertyConfidential;

   /**
    * @param description list of descriptions
    * @param configPropertyName name of the property
    * @param configPropertyType full qualified name of the type of the property
    * @param configPropertyValue value
    * @param configPropertyIgnore not mandatory boolean value The element config-property-ignore is used to specify
    *         whether the configuration tools must ignore considering the
    *         configuration property during auto-discovery of
    *         Configuration properties. See the Connector specification for
    *         more details. If unspecified, the container must not ignore
    *         the configuration property during auto-discovery.
    * @param configPropertySupportsDynamicUpdates not mandatory The element
    *             config-property-supports-dynamic-updates is used to specify
    *             whether the configuration property allows its value to be updated, by
    *             application server's configuration tools, during the lifetime of
    *             the JavaBean instance. See the Connector specification for
    *             more details. If unspecified, the container must not dynamically
    *             reconfigure the property.
    * @param configPropertyConfidential The element config-property-confidential is used to specify
    *  whether the configuration property is confidential and
    *  recommends application server's configuration tools to use special
    *  visual aids for editing them. See the Connector specification for
    *  more details. If unspecified, the container must not treat the
    *  property as confidential.
    * @param id XML ID
    */
   public ConfigProperty16(ArrayList<LocalizedXsdString> description, XsdString configPropertyName,
         XsdString configPropertyType, XsdString configPropertyValue, Boolean configPropertyIgnore,
         Boolean configPropertySupportsDynamicUpdates, Boolean configPropertyConfidential, String id)
   {
      super(description, configPropertyName, configPropertyType, configPropertyValue, id);
      this.configPropertyIgnore = configPropertyIgnore;
      this.configPropertySupportsDynamicUpdates = configPropertySupportsDynamicUpdates;
      this.configPropertyConfidential = configPropertyConfidential;
   }

   /**
    * @return configPropertyIgnore
    */
   public Boolean getConfigPropertyIgnore()
   {
      return configPropertyIgnore;
   }

   /**
    * @return configPropertySupportsDynamicUpdates
    */
   public Boolean getConfigPropertySupportsDynamicUpdates()
   {
      return configPropertySupportsDynamicUpdates;
   }

   /**
    * @return configPropertyConfidential
    */
   public Boolean getConfigPropertyConfidential()
   {
      return configPropertyConfidential;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((configPropertyConfidential == null) ? 0 : configPropertyConfidential.hashCode());
      result = prime * result + ((configPropertyIgnore == null) ? 0 : configPropertyIgnore.hashCode());
      result = prime * result + ((configPropertyName == null) ? 0 : configPropertyName.hashCode());
      result = prime * result
            + ((configPropertySupportsDynamicUpdates == null) ? 0 : configPropertySupportsDynamicUpdates.hashCode());
      result = prime * result + ((configPropertyType == null) ? 0 : configPropertyType.hashCode());
      result = prime * result + ((configPropertyValue == null) ? 0 : configPropertyValue.hashCode());
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof ConfigProperty16))
      {
         return false;
      }
      ConfigProperty16 other = (ConfigProperty16) obj;
      if (configPropertyConfidential == null)
      {
         if (other.configPropertyConfidential != null)
         {
            return false;
         }
      }
      else if (!configPropertyConfidential.equals(other.configPropertyConfidential))
      {
         return false;
      }
      if (configPropertyIgnore == null)
      {
         if (other.configPropertyIgnore != null)
         {
            return false;
         }
      }
      else if (!configPropertyIgnore.equals(other.configPropertyIgnore))
      {
         return false;
      }
      if (configPropertyName == null)
      {
         if (other.configPropertyName != null)
         {
            return false;
         }
      }
      else if (!configPropertyName.equals(other.configPropertyName))
      {
         return false;
      }
      if (configPropertySupportsDynamicUpdates == null)
      {
         if (other.configPropertySupportsDynamicUpdates != null)
         {
            return false;
         }
      }
      else if (!configPropertySupportsDynamicUpdates.equals(other.configPropertySupportsDynamicUpdates))
      {
         return false;
      }
      if (configPropertyType == null)
      {
         if (other.configPropertyType != null)
         {
            return false;
         }
      }
      else if (!configPropertyType.equals(other.configPropertyType))
      {
         return false;
      }
      if (configPropertyValue == null)
      {
         if (other.configPropertyValue != null)
         {
            return false;
         }
      }
      else if (!configPropertyValue.equals(other.configPropertyValue))
      {
         return false;
      }
      if (description == null)
      {
         if (other.description != null)
         {
            return false;
         }
      }
      else if (!description.equals(other.description))
      {
         return false;
      }
      if (id == null)
      {
         if (other.id != null)
         {
            return false;
         }
      }
      else if (!id.equals(other.id))
      {
         return false;
      }
      return true;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "ConfigProperty [description=" + description + ", configPropertyName=" + configPropertyName
            + ", configPropertyType=" + configPropertyType + ", configPropertyValue=" + configPropertyValue
            + ", configPropertyIgnore=" + configPropertyIgnore + ", configPropertySupportsDynamicUpdates="
            + configPropertySupportsDynamicUpdates + ", configPropertyConfidential=" + configPropertyConfidential
            + ", id=" + id + "]";
   }

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
       * description-class tag
       */
      DESCRIPTION("description"),

      /**
       * config-property-name TAG
       */
      CONFIG_PROPERTY_NAME("config-property-name"),

      /**
       * config-property-type TAG
       */
      CONFIG_PROPERTY_TYPE("config-property-type"),

      /**
       * config-property-value TAG
       */
      CONFIG_PROPERTY_VALUE("config-property-value"),

      /**
       * config-property-ignore TAG
       */
      CONFIG_PROPERTY_IGNORE("config-property-ignore"),

      /**
       * config-property-supports-dynamic-updates TAG
       */
      CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE("config-property-supports-dynamic-updates"),

      /**
       * config-property-confidential TAG
       */
      CONFIG_PROPERTY_CONFIDENTIAL("config-property-confidential");

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
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
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

      /** id attribute
       *
       */
      ID("id");

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

   }
}
