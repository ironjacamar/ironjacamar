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
package org.jboss.jca.common.metadataimpl.ra.common;

import org.jboss.jca.common.api.metadata.ra.OverrideElementAttribute;
import org.jboss.jca.common.api.metadata.ra.RaConfigProperty;
import org.jboss.jca.common.metadataimpl.JCAMetadata;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 * @param <T> generic type of this property.
 * See {@link #buildRaConfigProperty(String, String, String, OverrideElementAttribute))}
 *  for build right implementation
 *
 */
public class RaConfigPropertyImpl<T> implements JCAMetadata, RaConfigProperty<T>
{

   /**
    */
   private static final long serialVersionUID = 7845799671062777306L;

   private final String name;

   private final T value;

   private final String typeName;

   private final OverrideElementAttribute overrideElementAttribute;

   /**
    * @param name the name of the property
    * @param value the value of the property
    * @param overrideElementAttribute the override-element attribute
    */
   private RaConfigPropertyImpl(String name, T value, OverrideElementAttribute overrideElementAttribute)
   {
      super();
      this.name = name;
      this.value = value;
      this.typeName = value.getClass().getName();
      this.overrideElementAttribute = overrideElementAttribute == null
            ? OverrideElementAttribute.RESOURCE_ADAPTER
            : overrideElementAttribute;
   }

   /**
    * @param name the name of the property
    * @param value the value of the property
    * @param typeName full qualified name of value's type
    * @param overrideElementAttribute the override-element attribute
    */
   private RaConfigPropertyImpl(String name, T value, String typeName,
         OverrideElementAttribute overrideElementAttribute)
   {
      super();
      this.name = name;
      this.value = value;
      this.typeName = typeName;
      this.overrideElementAttribute = overrideElementAttribute == null
            ? OverrideElementAttribute.RESOURCE_ADAPTER
            : overrideElementAttribute;
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
    *   In case passed type is one of above ones a correct actualised {@link RaConfigPropertyImpl} is returned.
    *   TypeName field will be set accordly
    *
    *   In case the passed type isn't one of above ones (possible for jboss-ra_1_0.xsd) an RaConfigProperty<Object>
    *   is returned and typeName will be set as passed parameter type.
    *
    *
    *
    *
    * @param name name of the property
    * @param value value of the property.
    * @param type the full qualified name of the class to be actualised
    * @return the actualised instance
    * @param overrideElementAttribute the override-element attribute. Possible value are defined in the enumeration;
    *  if it is null the default RESOURCEADAPTER is used
    * @throws NumberFormatException in case passed value isn't assignable to type class
    */
   public static RaConfigProperty<?> buildRaConfigProperty(String name, String value, String type,
         OverrideElementAttribute overrideElementAttribute) throws NumberFormatException
   {
      if (type == null || type.trim().length() == 0)
      {
         return new RaConfigPropertyImpl<String>(name, value, overrideElementAttribute);
      }
      if ("java.lang.Boolean".equals(type))
      {
         return new RaConfigPropertyImpl<Boolean>(name, Boolean.valueOf(value), overrideElementAttribute);
      }
      else if ("java.lang.String".equals(type))
      {
         return new RaConfigPropertyImpl<String>(name, value, overrideElementAttribute);
      }
      else if ("java.lang.Integer".equals(type))
      {
         return new RaConfigPropertyImpl<Integer>(name, Integer.valueOf(value), overrideElementAttribute);
      }
      else if ("java.lang.Double".equals(type))
      {
         return new RaConfigPropertyImpl<Double>(name, Double.valueOf(value), overrideElementAttribute);
      }
      else if ("java.lang.Byte".equals(type))
      {
         return new RaConfigPropertyImpl<Byte>(name, Byte.valueOf(value), overrideElementAttribute);
      }
      else if ("java.lang.Long".equals(type))
      {
         return new RaConfigPropertyImpl<Long>(name, Long.valueOf(value), overrideElementAttribute);
      }
      else if ("java.lang.Float".equals(type))
      {
         return new RaConfigPropertyImpl<Float>(name, Float.valueOf(value), overrideElementAttribute);
      }
      else if ("java.lang.Character".equals(type))
      {
         return new RaConfigPropertyImpl<Character>(name, Character.valueOf(value.charAt(0)), overrideElementAttribute);
      }
      else
      {
         return new RaConfigPropertyImpl<Object>(name, value, type, overrideElementAttribute);
      }

   }

   /**
    * @return name
    */
   @Override
   public synchronized String getName()
   {
      return name;
   }

   /**
    * @return value
    */
   @Override
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
      if (!(obj instanceof RaConfigPropertyImpl))
      {
         return false;
      }
      RaConfigPropertyImpl other = (RaConfigPropertyImpl) obj;
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
    * typeName getter
    *
    * @return the typeName
    */
   @Override
   public String getTypeName()
   {
      return typeName;
   }

   /**
    *
    * override-element-attribute metadata getter
    *
    * @return the enum instance {@link OverrideElementAttribute}
    */
   @Override
   public OverrideElementAttribute getOverrideElementAttribute()
   {
      return overrideElementAttribute;
   }

}
