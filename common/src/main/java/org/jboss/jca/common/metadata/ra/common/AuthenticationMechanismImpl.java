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
import org.jboss.jca.common.api.metadata.ra.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.ra.AuthenticationMechanism.Tag;
import org.jboss.jca.common.api.metadata.ra.CredentialInterfaceEnum;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.XsdString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class AuthenticationMechanismImpl implements AuthenticationMechanism
{
   /**
    */
   private static final long serialVersionUID = -1448136517857827148L;

   private final ArrayList<LocalizedXsdString> description;

   private final XsdString authenticationMechanismType;

   private final CredentialInterfaceEnum credentialInterface;

   private final String id;

   private final String cIId;

   /**
    * @param description description attribute in xml
    * @param authenticationMechanismType specifies type of an authentication mechanism.
            The example values are:

            <authentication-mechanism-type>BasicPassword
            </authentication-mechanism-type>

            <authentication-mechanism-type>Kerbv5
            </authentication-mechanism-type>

            Any additional security mechanisms are outside the
            scope of the Connector architecture specification.
    * @param credentialInterface enumeration representing credentialInterface.
    * @param id xml ID
    * @param cid credential interface id
    */
   public AuthenticationMechanismImpl(List<LocalizedXsdString> description, XsdString authenticationMechanismType,
         CredentialInterfaceEnum credentialInterface, String id, String cid)
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
      this.authenticationMechanismType = authenticationMechanismType;
      if (!XsdString.isNull(this.authenticationMechanismType))
         this.authenticationMechanismType.setTag(Tag.AUTHENTICATION_MECHANISM_TYPE.toString());
      this.credentialInterface = credentialInterface;
      this.id = id;
      cIId = cid;
   }

   /**
    * @param description description attribute in xml
    * @param authenticationMechanismType specifies type of an authentication mechanism.
            The example values are:

            <authentication-mechanism-type>BasicPassword
            </authentication-mechanism-type>

            <authentication-mechanism-type>Kerbv5
            </authentication-mechanism-type>

            Any additional security mechanisms are outside the
            scope of the Connector architecture specification.
    * @param credentialInterface enumeration representing credentialInterface.
    * @param id xml ID
    */
   public AuthenticationMechanismImpl(List<LocalizedXsdString> description, XsdString authenticationMechanismType,
         CredentialInterfaceEnum credentialInterface, String id)
   {
      this(description, authenticationMechanismType, credentialInterface, id, null);
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
    * @return authenticationMechanismType
    */
   @Override
   public XsdString getAuthenticationMechanismType()
   {
      return authenticationMechanismType;
   }

   /**
    * @return credentialInterface
    */
   @Override
   public CredentialInterfaceEnum getCredentialInterface()
   {
      return credentialInterface;
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
      result = prime * result + ((authenticationMechanismType == null) ? 0 : authenticationMechanismType.hashCode());
      result = prime * result + ((credentialInterface == null) ? 0 : credentialInterface.hashCode());
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((cIId == null) ? 0 : cIId.hashCode());
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
      if (!(obj instanceof AuthenticationMechanismImpl))
      {
         return false;
      }
      AuthenticationMechanismImpl other = (AuthenticationMechanismImpl) obj;
      if (authenticationMechanismType == null)
      {
         if (other.authenticationMechanismType != null)
         {
            return false;
         }
      }
      else if (!authenticationMechanismType.equals(other.authenticationMechanismType))
      {
         return false;
      }
      if (credentialInterface != other.credentialInterface)
      {
         return false;
      }
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
      if (cIId == null)
      {
         if (other.cIId != null)
         {
            return false;
         }
      }
      else if (!cIId.equals(other.cIId))
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

      sb.append("<authentication-mechanism");
      if (id != null)
         sb.append(" ").append(AuthenticationMechanism.Attribute.ID).append("=\"").append(id).append("\"");
      sb.append(">");

      if (description != null)
      {
         for (LocalizedXsdString s : description)
            sb.append(s);
      }

      sb.append(authenticationMechanismType);

      sb.append("<").append(AuthenticationMechanism.Tag.CREDENTIAL_INTERFACE)
            .append(cIId == null ? "" : " id=\"" + cIId + "\"").append(">");
      sb.append(credentialInterface);
      sb.append("</").append(AuthenticationMechanism.Tag.CREDENTIAL_INTERFACE).append(">");

      sb.append("</authentication-mechanism>");

      return sb.toString();
   }

   @Override
   public CopyableMetaData copy()
   {
      return new AuthenticationMechanismImpl(CopyUtil.cloneList(description),
            CopyUtil.clone(authenticationMechanismType), credentialInterface, CopyUtil.cloneString(id),
            CopyUtil.cloneString(cIId));
   }

}
