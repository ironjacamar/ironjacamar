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

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class XsdString implements IdDecoratedMetadata, CopyableMetaData
{

   /**
    * A NULL immutable instance
    */
   public static final XsdString NULL_XSDSTRING = new XsdString(null, null, null);

   /**
    */
   private static final long serialVersionUID = -3045754045828271173L;

   /**
    * the actual String value
    */
   protected final String value;

   /**
    * XML ID
    */
   protected final String id;

   /**
    * tag name
    */
   protected String tag;

   /**
    * @param value the actual String value
    * @param id XML ID
    * @param tag name
    */
   public XsdString(String value, String id, String tag)
   {
      super();
      this.value = value;
      this.id = id;
      this.tag = tag;
   }

   /**
    * Constructor without tag name
    * 
    * @param value the actual String value
    * @param id XML ID
    */
   public XsdString(String value, String id)
   {
      this(value, id, null);
   }

   /**
    * @return value
    */
   public String getValue()
   {
      return value;
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
    * @see IdDecoratedMetadata#getId()
    */
   public String getTag()
   {
      return tag;
   }
   
   /**
    * {@inheritDoc}
    *
    * @see IdDecoratedMetadata#getId()
    */
   public void setTag(String tg)
   {
      tag = tg;
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
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
      if (!(obj instanceof XsdString))
      {
         return false;
      }
      XsdString other = (XsdString) obj;
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
      if (tag == null)
      {
         if (other.tag != null)
         {
            return false;
         }
      }
      else if (!tag.equals(other.tag))
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
      if (tag == null)
         return value;
      else
         return "<" + tag + (id == null ? "" : " id=\"" + id + "\"") + ">" + value + "</" + tag + ">";
   }

   /**
    *
    * convenient method to test if an {@link XsdString} is null NULLXsdString instance
    * @param xsdString the xsdString to test
    * @return ture if passes xsdString is null or equals to NULLXsdString instance
    *     */
   public static boolean isNull(XsdString xsdString)
   {
      return (xsdString == null || xsdString.equals(NULL_XSDSTRING));
   }

   @Override
   public CopyableMetaData copy()
   {
      return new XsdString(CopyUtil.cloneString(value), CopyUtil.cloneString(id), CopyUtil.cloneString(tag));
   }
}
