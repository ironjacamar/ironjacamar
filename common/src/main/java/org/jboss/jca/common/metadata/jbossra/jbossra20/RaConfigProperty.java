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
package org.jboss.jca.common.metadata.jbossra.jbossra20;

import org.jboss.jca.common.metadata.JCAMetadata;
import org.jboss.jca.common.metadata.jbossra.JbossRaParser.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 * @param <T> generic type of this property. See {@link #buildRaConfigProperty(String, String, String)}
 *  for build right implementation
 *
 */
public class RaConfigProperty<T> implements JCAMetadata
{

   /**
    */
   private static final long serialVersionUID = 7845799671062777306L;

   private final String name;

   private final T value;

   private final String typeName;

   /**
    * @param name the name of the property
    * @param value the value of the property
    */
   private RaConfigProperty(String name, T value)
   {
      super();
      this.name = name;
      this.value = value;
      this.typeName = value.getClass().getName();
   }

   /**
    * @param name the name of the property
    * @param value the value of the property
    * @param typeName full qualified name of value's type
    */
   private RaConfigProperty(String name, T value, String type)
   {
      super();
      this.name = name;
      this.value = value;
      this.typeName = value.getClass().getName();
   }

   /**
    *
    * Static method to build actualised implementation of this generic class.
    * According to jboss-ra_2_0.xsd value values are:
    *   java.lang.Boolean
    *   java.lang.String
    *   java.lang.Integer
    *   java.lang.Double
    *   java.lang.Byte
    *   java.lang.Short
    *   java.lang.Long
    *   java.lang.Float
    *   java.lang.Character
    *
    *   In case passed type is one of above ones a correct actualised {@link RaConfigProperty} is returned.
    *   TypeName field will be set accordly
    *
    *   In case the passed type isn't one of above ones (possible for jboss-ra_1_0.xsd) an RaConfigProperty<Object>
    *   is returned and typeName will be set as passed parameter type.
    *
    *
    * @param name name of the property
    * @param value value of the property.
    * @param type the full qualified name of the class to be actualised
    * @return the actualised instance
    * @throws NumberFormatException in case passed value isn't assignable to type class
    */
   public static RaConfigProperty<?> buildRaConfigProperty(String name, String value, String type)
      throws NumberFormatException
   {
      if (type == null || type.trim().length() == 0)
      {
         return new RaConfigProperty<String>(name, value);
      }
      if ("java.lang.Boolean".equals(type))
      {
         return new RaConfigProperty<Boolean>(name, Boolean.valueOf(value));
      }
      else if ("java.lang.String".equals(type))
      {
         return new RaConfigProperty<String>(name, value);
      }
      else if ("java.lang.Integer".equals(type))
      {
         return new RaConfigProperty<Integer>(name, Integer.valueOf(value));
      }
      else if ("java.lang.Double".equals(type))
      {
         return new RaConfigProperty<Double>(name, Double.valueOf(value));
      }
      else if ("java.lang.Byte".equals(type))
      {
         return new RaConfigProperty<Byte>(name, Byte.valueOf(value));
      }
      else if ("java.lang.Long".equals(type))
      {
         return new RaConfigProperty<Long>(name, Long.valueOf(value));
      }
      else if ("java.lang.Float".equals(type))
      {
         return new RaConfigProperty<Float>(name, Float.valueOf(value));
      }
      else if ("java.lang.Character".equals(type))
      {
         return new RaConfigProperty<Character>(name, Character.valueOf(value.charAt(0)));
      }
      else
      {
         return new RaConfigProperty<Object>(name, value, type);
      }

   }

   /**
    * @return name
    */
   public synchronized String getName()
   {
      return name;
   }

   /**
    * @return value
    */
   public synchronized T getValue()
   {
      return value;
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
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());
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
      if (!(obj instanceof RaConfigProperty))
      {
         return false;
      }
      RaConfigProperty other = (RaConfigProperty) obj;
      if (name == null)
      {
         if (other.name != null)
         {
            return false;
         }
      }
      else if (!name.equals(other.name))
      {
         return false;
      }
      if (value == null)
      {
         if (other.value != null)
         {
            return false;
         }
      }
      else if (!value.equals(other.value))
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
      return "RaConfigProperty [name=" + name + ", value=" + value + "]";
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

      /** jboss-ra-property-name tag name
       *
       */
      RA_CONFIG_PROPERTY_NAME("ra-config-property-name"),

      /** jboss-ra-property-value tag name
       *
       */
      RA_CONFIG_PROPERTY_VALUE("ra-config-property-value"),

      /** jboss-ra-property-type tag name
       *
       */
      RA_CONFIG_PROPERTY_TYPE("ra-config-property-type");

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

}
