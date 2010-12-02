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
package org.jboss.jca.common.api.metadata.ds;

import org.jboss.jca.common.api.metadata.JCAMetadata;
import org.jboss.jca.common.api.metadata.ValidatableMetadata;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * A JdbcAdapterExtension.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public final class JdbcAdapterExtension implements JCAMetadata, ValidatableMetadata
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -6275984008991105644L;

   private final String className;

   private final Map<String, String> configPropertiesMap;

   /**
    * Create a new JdbcAdapterExtension.
    *
    * @param className the className
    * @param configPropertiesMap configPropertiesMap
    * @throws ValidateException ValidateException
    */
   public JdbcAdapterExtension(String className, Map<String, String> configPropertiesMap) throws ValidateException
   {
      super();
      this.className = className;
      if (configPropertiesMap != null)
      {
         this.configPropertiesMap = new HashMap<String, String>(configPropertiesMap.size());
         this.configPropertiesMap.putAll(configPropertiesMap);
      }
      else
      {
         this.configPropertiesMap = Collections.emptyMap();
      }
      this.validate();
   }

   /**
    * Get the className.
    *
    * @return the className.
    */
   public final String getClassName()
   {
      return className;
   }

   /**
    * Get the configPropertiesMap.
    *
    * @return the configPropertiesMap.
    */
   public final Map<String, String> getConfigPropertiesMap()
   {
      return Collections.unmodifiableMap(configPropertiesMap);
   }

   @Override
   public String toString()
   {
      return "JdbcAdapterExtension [className=" + className + ", configPropertiesMap=" + configPropertiesMap + "]";
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((className == null) ? 0 : className.hashCode());
      result = prime * result + ((configPropertiesMap == null) ? 0 : configPropertiesMap.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof JdbcAdapterExtension))
         return false;
      JdbcAdapterExtension other = (JdbcAdapterExtension) obj;
      if (className == null)
      {
         if (other.className != null)
            return false;
      }
      else if (!className.equals(other.className))
         return false;
      if (configPropertiesMap == null)
      {
         if (other.configPropertiesMap != null)
            return false;
      }
      else if (!configPropertiesMap.equals(other.configPropertiesMap))
         return false;
      return true;
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
       * pool tag

      /**
      * config-property tag
      */
      CONFIG_PROPERTY("config-property");

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
    *
    * A Attribute.
    *
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
    *
    */
   public enum Attribute
   {

      /** class-name attribute
      *
      */
      CLASS_NAME("class-name");

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

   @Override
   public void validate() throws ValidateException
   {
      if (this.className == null || className.trim().length() == 0)
         throw new ValidateException("connectionUrl is required in " + this.getClass().getCanonicalName());
   }

}

