/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.api.metadata.common;

import org.jboss.jca.common.api.metadata.JCAMetadata;
import org.jboss.jca.common.api.metadata.ValidatableMetadata;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Capacity definition 
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Capacity implements JCAMetadata, ValidatableMetadata
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Extension incrementer;
   private Extension decrementer;

   /**
    * Constructor
    *
    * @param incrementer The incrementer plugin
    * @param decrementer The decrementer plugin
    * @throws ValidateException in case of not valid metadata creation
    */
   public Capacity(Extension incrementer, Extension decrementer) throws ValidateException
   {
      super();
      this.incrementer = incrementer;
      this.decrementer = decrementer;
      this.validate();
   }

   /**
    * Get the incrementer
    * @return The value
    */
   public Extension getIncrementer()
   {
      return incrementer;
   }

   /**
    * Get the decrementer
    * @return The value
    */
   public Extension getDecrementer()
   {
      return decrementer;
   }

   @Override
   public void validate() throws ValidateException
   {
      // all values are ok
   }

   @Override
   public int hashCode()
   {
      int prime = 31;
      int result = 7;
      result = prime * result + ((incrementer == null) ? 7 : incrementer.hashCode());
      result = prime * result + ((decrementer == null) ? 7 : decrementer.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;

      if (obj == null)
         return false;

      if (!(obj instanceof Capacity))
         return false;

      Capacity other = (Capacity) obj;

      if (incrementer == null)
      {
         if (other.incrementer != null)
            return false;
      }
      else if (!incrementer.equals(other.incrementer))
         return false;

      if (decrementer == null)
      {
         if (other.decrementer != null)
            return false;
      }
      else if (!decrementer.equals(other.decrementer))
         return false;

      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<capacity>");

      if (incrementer != null)
      {
         sb.append("<").append(Tag.INCREMENTER);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(incrementer.getClassName()).append("\"");
         sb.append(">");

         if (incrementer.getConfigPropertiesMap() != null && incrementer.getConfigPropertiesMap().size() > 0)
         {
            Iterator<Map.Entry<String, String>> it = incrementer.getConfigPropertiesMap().entrySet().iterator();
            
            while (it.hasNext())
            {
               Map.Entry<String, String> entry = it.next();

               sb.append("<").append(Extension.Tag.CONFIG_PROPERTY);
               sb.append(" name=\"").append(entry.getKey()).append("\">");
               sb.append(entry.getValue());
               sb.append("</").append(Extension.Tag.CONFIG_PROPERTY).append(">");
            }
         }

         sb.append("</").append(Tag.INCREMENTER).append(">");
      }
      if (decrementer != null)
      {
         sb.append("<").append(Tag.DECREMENTER);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(decrementer.getClassName()).append("\"");
         sb.append(">");

         if (decrementer.getConfigPropertiesMap() != null && decrementer.getConfigPropertiesMap().size() > 0)
         {
            Iterator<Map.Entry<String, String>> it = decrementer.getConfigPropertiesMap().entrySet().iterator();
            
            while (it.hasNext())
            {
               Map.Entry<String, String> entry = it.next();

               sb.append("<").append(Extension.Tag.CONFIG_PROPERTY);
               sb.append(" name=\"").append(entry.getKey()).append("\">");
               sb.append(entry.getValue());
               sb.append("</").append(Extension.Tag.CONFIG_PROPERTY).append(">");
            }
         }

         sb.append("</").append(Tag.DECREMENTER).append(">");
      }

      sb.append("</capacity>");
      
      return sb.toString();
   }

   /**
    * A tag
    *
    * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
    */
   public enum Tag
   {
      /** always first */
      UNKNOWN(null),

      /** incrementer */
      INCREMENTER("incrementer"),

      /** decrementer */
      DECREMENTER("decrementer");

      private String name;

      /**
       * Create a new tag
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
}

