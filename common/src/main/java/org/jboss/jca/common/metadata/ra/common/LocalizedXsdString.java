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

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class LocalizedXsdString extends XsdString implements LocalizedMetadata
{
   /**
    */
   private static final long serialVersionUID = -7778684576336929347L;

   private final String lang;

   /**
    * @param value value of the String
    * @param id XML ID
    * @param lang language
    */
   public LocalizedXsdString(String value, String id, String lang)
   {
      super(value, id);
      this.lang = lang;
   }

   /**
    * Constructor for default language "en"
    *
    * @param value value of the String
    * @param id XML ID
    */
   public LocalizedXsdString(String value, String id)
   {
      super(value, id);
      this.lang = "en";
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
      return "LocalizedXsdString [lang=" + lang + ", value=" + value + ", id=" + id + "]";
   }

}
