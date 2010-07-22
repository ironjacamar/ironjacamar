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

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class InboundResourceAdapter implements IdDecoratedMetadata
{
   /**
    */
   private static final long serialVersionUID = -2854927981408307535L;

   private final MessageAdapter messageadapter;

   private final String id;

   /**
    * @param messageadapter The messageadapterType specifies information about the
    * messaging capabilities of the resource adapter. This
    * contains information specific to the implementation of the
    * resource adapter library as specified through the
    * messagelistener element.
    * @param id XML ID
    */
   public InboundResourceAdapter(MessageAdapter messageadapter, String id)
   {
      super();
      this.messageadapter = messageadapter;
      this.id = id;
   }

   /**
    * @return messageadapter
    */
   public MessageAdapter getMessageadapter()
   {
      return messageadapter;
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
      result = prime * result + ((messageadapter == null) ? 0 : messageadapter.hashCode());
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
      if (!(obj instanceof InboundResourceAdapter))
      {
         return false;
      }
      InboundResourceAdapter other = (InboundResourceAdapter) obj;
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
      if (messageadapter == null)
      {
         if (other.messageadapter != null)
         {
            return false;
         }
      }
      else if (!messageadapter.equals(other.messageadapter))
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
      return "InboundResourceAdapter [messageadapter=" + messageadapter + ", id=" + id + "]";
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
       * messageadapter TAG
       */
      MESSAGEADAPTER("messageadapter");

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
