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
package org.ironjacamar.common.metadata.spec;

import org.ironjacamar.common.api.metadata.CopyUtil;
import org.ironjacamar.common.api.metadata.spec.LocalizedXsdString;
import org.ironjacamar.common.api.metadata.spec.SecurityPermission;
import org.ironjacamar.common.api.metadata.spec.XsdString;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Security permission
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class SecurityPermissionImpl extends AbstractMetadata implements SecurityPermission
{
   private static final long serialVersionUID = 1L;

   private ArrayList<LocalizedXsdString> description;

   private XsdString securityPermissionSpec;

   private String id;

   /**
    * Constructor
    * @param description descriptions
    * @param securityPermissionSpec the security permission spec as defined in the xml
    * @param id XML ID
    */
   public SecurityPermissionImpl(List<LocalizedXsdString> description, XsdString securityPermissionSpec, String id)
   {
      super(null);
      if (description != null)
      {
         this.description = new ArrayList<LocalizedXsdString>(description);
         for (LocalizedXsdString d: this.description)
            d.setTag(XML.ELEMENT_DESCRIPTION);
      }
      else
      {
         this.description = new ArrayList<LocalizedXsdString>(0);
      }

      this.securityPermissionSpec = securityPermissionSpec;

      if (!XsdString.isNull(this.securityPermissionSpec))
         this.securityPermissionSpec.setTag(XML.ELEMENT_SECURITY_PERMISSION_SPEC);

      this.id = id;
   }

   /**
    * {@inheritDoc}
    */
   public List<LocalizedXsdString> getDescriptions()
   {
      return Collections.unmodifiableList(description);
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getSecurityPermissionSpec()
   {
      return securityPermissionSpec;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   public SecurityPermission copy()
   {
      return new SecurityPermissionImpl(CopyUtil.cloneList(description), CopyUtil.clone(securityPermissionSpec),
                                        CopyUtil.cloneString(id));
   }

   /**
    * {@inheritDoc}
    */
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
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<security-permission");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
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
}
