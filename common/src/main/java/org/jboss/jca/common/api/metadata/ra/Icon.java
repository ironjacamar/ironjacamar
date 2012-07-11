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
package org.jboss.jca.common.api.metadata.ra;


import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class Icon implements IdDecoratedMetadata, LocalizedMetadata, CopyableMetaData
{
   /**
    */
   private static final long serialVersionUID = 7809751095477978996L;

   private final Path smallIcon;

   private final Path largeIcon;

   private final String lang;

   private final String id;

   /**
    * @param smallIcon .
    * @param largeIcon .
    * @param lang .
    * @param id .
    */
   public Icon(Path smallIcon, Path largeIcon, String lang, String id)
   {
      super();
      this.smallIcon = smallIcon;
      this.largeIcon = largeIcon;
      this.lang = lang;
      this.id = id;
   }

   /**
    * @param smallIcon .
    * @param largeIcon .
    * @param id .
    */
   public Icon(Path smallIcon, Path largeIcon, String id)
   {
      super();
      this.smallIcon = smallIcon;
      this.largeIcon = largeIcon;
      this.lang = "en";
      this.id = id;
   }

   /**
    * @return smallIcon
    */
   public Path getSmallIcon()
   {
      return smallIcon;
   }


   /**
    * @return largeIcon
    */
   public Path getLargeIcon()
   {
      return largeIcon;
   }

   /**
    * @return lang
    */
   @Override
   public String getLang()
   {
      return lang;
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
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((lang == null) ? 0 : lang.hashCode());
      result = prime * result + ((largeIcon == null) ? 0 : largeIcon.hashCode());
      result = prime * result + ((smallIcon == null) ? 0 : smallIcon.hashCode());
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
      if (!(obj instanceof Icon))
      {
         return false;
      }
      Icon other = (Icon) obj;
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
      if (lang == null)
      {
         if (other.lang != null)
         {
            return false;
         }
      }
      else if (!lang.equals(other.lang))
      {
         return false;
      }
      if (largeIcon == null)
      {
         if (other.largeIcon != null)
         {
            return false;
         }
      }
      else if (!largeIcon.equals(other.largeIcon))
      {
         return false;
      }
      if (smallIcon == null)
      {
         if (other.smallIcon != null)
         {
            return false;
         }
      }
      else if (!smallIcon.equals(other.smallIcon))
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
      return "Icon [smallIcon=" + smallIcon + ", largeIcon=" + largeIcon + ", lang=" + lang + ", id=" + id + "]";
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
       * small-icon tag
       */
      SMALL_ICON("small-icon"),

      /**
       * large-icon tag
       */
      LARGE_ICON("large-icon");

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
       * lang atttribute
       */
      LANG("lang");

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

   @Override
   public CopyableMetaData copy()
   {
      return new Icon(CopyUtil.clone(smallIcon), CopyUtil.clone(largeIcon), CopyUtil.cloneString(id));
   }
}
