/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.spec;

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.spec.Icon;
import org.jboss.jca.common.api.metadata.spec.XsdString;

/**
 * An Icon implementation
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class IconImpl implements Icon
{
   private static final long serialVersionUID = 1L;

   private XsdString smallIcon;

   private XsdString largeIcon;

   private String lang;

   private String id;

   /**
    * Constructor
    * @param smallIcon .
    * @param largeIcon .
    * @param lang .
    * @param id .
    */
   public IconImpl(XsdString smallIcon, XsdString largeIcon, String lang, String id)
   {
      this.smallIcon = smallIcon;
      if (!XsdString.isNull(this.smallIcon))
         this.smallIcon.setTag(XML.IconTag.SMALL_ICON.toString());
      this.largeIcon = largeIcon;
      if (!XsdString.isNull(this.largeIcon))
         this.largeIcon.setTag(XML.IconTag.LARGE_ICON.toString());
      this.lang = lang;
      this.id = id;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getSmallIcon()
   {
      return smallIcon;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getLargeIcon()
   {
      return largeIcon;
   }

   /**
    * {@inheritDoc}
    */
   public String getLang()
   {
      return lang;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * Set the lang
    * @param lng The value
    */
   public void setLang(String lng)
   {
      lang = lng;
   }

   /**
    * {@inheritDoc}
    */
   public CopyableMetaData copy()
   {
      return new IconImpl(CopyUtil.clone(smallIcon), CopyUtil.clone(largeIcon), 
                          CopyUtil.cloneString(lang), CopyUtil.cloneString(id));
   }

   /**
    * {@inheritDoc}
    */
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
      if (!(obj instanceof IconImpl))
      {
         return false;
      }
      IconImpl other = (IconImpl) obj;
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
    */
   public String toString()
   {
      return "<icon" + (id == null ? "" : " id=\"" + id + "\"") + (lang == null ? "" : " lang=\"" + lang + "\"") + ">"
            + (smallIcon == null ? "" : smallIcon) + (largeIcon == null ? "" : largeIcon) + "</icon>";
   }
}
