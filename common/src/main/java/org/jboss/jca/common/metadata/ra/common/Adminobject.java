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
package org.jboss.jca.common.metadata.ra.common;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class Adminobject implements IdDecoratedMetadata
{
   /**
     */
   private static final long serialVersionUID = -7653991400977178783L;

   private final XsdString adminobjectInterface;

   private final XsdString adminobjectClass;

   private final ArrayList<? extends ConfigProperty> configProperty;

   private final String id;

   /**
    * @param adminobjectInterface full qualified name of the interface
    * @param adminobjectClass full qualified name of the implementation class
    * @param configProperty ArrayList of config propeties
    * @param id xmlid
    */
   public Adminobject(final XsdString adminobjectInterface, final XsdString adminobjectClass,
         final ArrayList<? extends ConfigProperty> configProperty, final String id)
   {
      super();
      this.adminobjectInterface = adminobjectInterface;
      this.adminobjectClass = adminobjectClass;
      this.configProperty = configProperty;
      this.id = id;
   }

   /**
    * @return adminobjectInterface
    */
   public XsdString getAdminobjectInterface()
   {
      return adminobjectInterface;
   }

   /**
    * @return adminobjectClass
    */
   public XsdString getAdminobjectClass()
   {
      return adminobjectClass;
   }

   /**
    * @return configProperty
    */
   public List<? extends ConfigProperty> getConfigProperty()
   {
      return Collections.unmodifiableList(configProperty);
   }

   /**
    * {@inheritDoc}
    *
    * @see IdDecoratedMetadata#getId()
    */
   @Override
   public String getId()
   {
      return id;
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
      result = prime * result + ((adminobjectClass == null) ? 0 : adminobjectClass.hashCode());
      result = prime * result + ((adminobjectInterface == null) ? 0 : adminobjectInterface.hashCode());
      result = prime * result + ((configProperty == null) ? 0 : configProperty.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(final Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof Adminobject))
      {
         return false;
      }
      final Adminobject other = (Adminobject) obj;
      if (adminobjectClass == null)
      {
         if (other.adminobjectClass != null)
         {
            return false;
         }
      }
      else if (!adminobjectClass.equals(other.adminobjectClass))
      {
         return false;
      }
      if (adminobjectInterface == null)
      {
         if (other.adminobjectInterface != null)
         {
            return false;
         }
      }
      else if (!adminobjectInterface.equals(other.adminobjectInterface))
      {
         return false;
      }
      if (configProperty == null)
      {
         if (other.configProperty != null)
         {
            return false;
         }
      }
      else if (!configProperty.equals(other.configProperty))
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
      return "Adminobject [adminobjectInterface=" + adminobjectInterface + ", adminobjectClass=" + adminobjectClass
            + ", configProperty=" + configProperty + ", id=" + id + "]";
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
       * adminobject-interface TAG
       */
      ADMINOBJECT_INTERFACE("adminobject-interface"),

      /**
       * adminobject-class TAG
       */
      ADMINOBJECT_CLASS("adminobject-class"),

      /**
       * config-property TAG
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
