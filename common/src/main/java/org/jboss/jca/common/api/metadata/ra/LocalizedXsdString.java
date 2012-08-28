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
public class LocalizedXsdString extends XsdString implements LocalizedMetadata
{
   /**
    */
   private static final long serialVersionUID = -7778684576336929347L;

   private String lang;

   /**
    * @param value value of the String
    * @param id XML ID
    * @param lang language
    * @param tag name
    */
   public LocalizedXsdString(String value, String id, String lang, String tag)
   {
      super(value, id, tag);
      this.lang = lang;
   }

   /**
    * Constructor for default language 
    *
    * @param value value of the String
    * @param id XML ID
    * @param lang language
    */
   public LocalizedXsdString(String value, String id, String lang)
   {
      this(value, id, lang, null);
   }

   /**
    * Constructor for default language without tag
    *
    * @param value value of the String
    * @param id XML ID
    */
   public LocalizedXsdString(String value, String id)
   {
      this(value, id, null, null);
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
   public void setLang(String lng)
   {
      lang = lng;
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
      int result = super.hashCode();
      result = prime * result + ((lang == null) ? 0 : lang.hashCode());
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
      if (!super.equals(obj))
      {
         return false;
      }
      if (!(obj instanceof LocalizedXsdString))
      {
         return false;
      }
      LocalizedXsdString other = (LocalizedXsdString) obj;
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
         return "<" + tag + (id == null ? "" : " id=\"" + id + "\"") + (lang == null ? "" : " lang=\"" + lang + "\"")
               + ">" + value + "</" + tag + ">";
   }

   @Override
   public CopyableMetaData copy()
   {
      return new LocalizedXsdString(CopyUtil.cloneString(value), CopyUtil.cloneString(id),
            CopyUtil.cloneString(lang), CopyUtil.cloneString(tag));
   }

}
