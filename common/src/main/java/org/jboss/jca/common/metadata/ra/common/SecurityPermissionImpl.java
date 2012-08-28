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

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.SecurityPermission;
import org.jboss.jca.common.api.metadata.ra.SecurityPermission.Tag;
import org.jboss.jca.common.api.metadata.ra.XsdString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class SecurityPermissionImpl implements SecurityPermission
{
   /**
    */
   private static final long serialVersionUID = -7931009018498254330L;

   private final ArrayList<LocalizedXsdString> description;

   private final XsdString securityPermissionSpec;

   private final String id;

   /**
    * @param description descriptions
    * @param securityPermissionSpec the security permission spec as defined in the xml
    * @param id XML ID
    */
   public SecurityPermissionImpl(List<LocalizedXsdString> description, XsdString securityPermissionSpec, String id)
   {
      super();
      if (description != null)
      {
         this.description = new ArrayList<LocalizedXsdString>(description.size());
         this.description.addAll(description);
         for (LocalizedXsdString d: this.description)
            d.setTag(Tag.DESCRIPTION.toString());

      }
      else
      {
         this.description = new ArrayList<LocalizedXsdString>(0);
      }
      this.securityPermissionSpec = securityPermissionSpec;
      if (!XsdString.isNull(this.securityPermissionSpec))
         this.securityPermissionSpec.setTag(Tag.SECURITY_PERMISSION_SPEC.toString());
      this.id = id;
   }

   /**
    * @return description
    */
   @Override
   public List<LocalizedXsdString> getDescriptions()
   {
      return description == null ? null : Collections.unmodifiableList(description);
   }

   /**
    * @return securityPermissionSpec
    */
   @Override
   public XsdString getSecurityPermissionSpec()
   {
      return securityPermissionSpec;
   }

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
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((securityPermissionSpec == null) ? 0 : securityPermissionSpec.hashCode());
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
      if (!(obj instanceof SecurityPermissionImpl))
      {
         return false;
      }
      SecurityPermissionImpl other = (SecurityPermissionImpl) obj;
      if (description == null)
      {
         if (other.description != null)
         {
            return false;
         }
      }
      else if (!description.equals(other.description))
      {
         return false;
      }
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
      if (securityPermissionSpec == null)
      {
         if (other.securityPermissionSpec != null)
         {
            return false;
         }
      }
      else if (!securityPermissionSpec.equals(other.securityPermissionSpec))
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
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<security-permission");
      if (id != null)
         sb.append(" ").append(SecurityPermission.Attribute.ID).append("=\"").append(id).append("\"");
      sb.append(">");

      if (description != null)
      {
         for (LocalizedXsdString s : description)
            sb.append(s);
      }

      sb.append(securityPermissionSpec);

      sb.append("</security-permission>");
      
      return sb.toString();
   }

   @Override
   public CopyableMetaData copy()
   {
      return new SecurityPermissionImpl(CopyUtil.cloneList(description), CopyUtil.clone(securityPermissionSpec),
            CopyUtil.cloneString(id));
   }

}
