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

/**
 * A localized string
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LocalizedXsdString extends XsdString implements LocalizedMetadata
{
   private static final long serialVersionUID = 1L;

   private String lang;

   /**
    * Constructor
    *
    * @param value value of the String
    * @param id XML ID
    */
   public LocalizedXsdString(String value, String id)
   {
      this(value, id, null, null);
   }

   /**
    * Constructor
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
    * Constructor
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
    * Get the language
    * @return The value
    */
   public String getLang()
   {
      return lang;
   }

   /**
    * Set the language
    * @param lng The value
    */
   public void setLang(String lng)
   {
      lang = lng;
   }

   /**
    * {@inheritDoc}
    */
   public LocalizedXsdString copy()
   {
      return new LocalizedXsdString(CopyUtil.cloneString(value), CopyUtil.cloneString(id),
                                    CopyUtil.cloneString(lang), CopyUtil.cloneString(tag));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((lang == null) ? 0 : lang.hashCode());
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
    */
   public String toString()
   {
      if (tag == null)
         return value;
      else
         return "<" + tag + (id == null ? "" : " id=\"" + id + "\"") + (lang == null ? "" : " lang=\"" + lang + "\"")
               + ">" + value + "</" + tag + ">";
   }
}
