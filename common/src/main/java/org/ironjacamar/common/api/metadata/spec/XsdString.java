/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.api.metadata.spec;

import org.ironjacamar.common.api.metadata.CopyUtil;
import org.ironjacamar.common.api.metadata.CopyableMetaData;

/**
 * A XML string
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XsdString implements IdDecoratedMetadata, CopyableMetaData<XsdString>
{
   /**
    * A NULL immutable instance
    */
   public static final XsdString NULL_XSDSTRING = new XsdString(null, null, null);

   /**
    */
   private static final long serialVersionUID = 1L;

   /**
    * the actual String value
    */
   protected String value;

   /**
    * XML ID
    */
   protected String id;

   /**
    * tag name
    */
   protected String tag;

   /**
    * Constructor
    * 
    * @param value the actual String value
    * @param id XML ID
    */
   public XsdString(String value, String id)
   {
      this(value, id, null);
   }

   /**
    * Constructor
    * @param value the actual String value
    * @param id XML ID
    * @param tag name
    */
   public XsdString(String value, String id, String tag)
   {
      this.value = value;
      this.id = id;
      this.tag = tag;
   }

   /**
    * Get the XML String content
    * @return The value
    */
   public String getValue()
   {
      return value;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * Get the XML tag content
    * @return The value
    */
   public String getTag()
   {
      return tag;
   }
   
   /**
    * Set the XML tag content
    * @param tg The value
    */
   public void setTag(String tg)
   {
      tag = tg;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString copy()
   {
      return new XsdString(CopyUtil.cloneString(value), CopyUtil.cloneString(id), CopyUtil.cloneString(tag));
   }

   /**
    * {@inheritDoc}
    */
   public boolean hasExpression(String key)
   {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public String getValue(String key, String v)
   {
      return value;
   }

   /**
    * {@inheritDoc}
    */
   public String getValue(String key, String subkey, String v)
   {
      return value;
   }

   /**
    * {@inheritDoc}
    */
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
    */
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
    */
   public String toString()
   {
      if (tag == null)
         return value != null ? value : "";

      return "<" + tag + (id == null ? "" : " id=\"" + id + "\"") + ">" + (value != null ? value : "") +
         "</" + tag + ">";
   }

   /**
    * Convenient method to test if an {@link XsdString} is null NULLXsdString instance
    * @param xsdString the xsdString to test
    * @return ture if passes xsdString is null or equals to NULLXsdString instance
    */
   public static boolean isNull(XsdString xsdString)
   {
      return (xsdString == null || xsdString.equals(NULL_XSDSTRING));
   }
}
