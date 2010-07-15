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
package org.jboss.jca.common.metadata.ra.ra16;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class Icon implements IdDecoratedMetadata, LocalizedMetadata
{
   /**
    */
   private static final long serialVersionUID = 7809751095477978996L;

   private Path smallIcon;

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
    * @return smallIcon
    */
   public Path getSmallIcon()
   {
      return smallIcon;
   }

   /**
    * @param smallIcon Sets smallIcon to the specified value.
    */
   public void setSmallIcon(Path smallIcon)
   {
      this.smallIcon = smallIcon;
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
}
