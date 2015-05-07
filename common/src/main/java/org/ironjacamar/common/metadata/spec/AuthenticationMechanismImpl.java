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
import org.ironjacamar.common.api.metadata.spec.AuthenticationMechanism;
import org.ironjacamar.common.api.metadata.spec.CredentialInterfaceEnum;
import org.ironjacamar.common.api.metadata.spec.LocalizedXsdString;
import org.ironjacamar.common.api.metadata.spec.XsdString;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An AuthenticationMechanism implementation
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AuthenticationMechanismImpl extends AbstractMetadata implements AuthenticationMechanism
{
   private static final long serialVersionUID = 1L;

   private List<LocalizedXsdString> description;

   private XsdString authenticationMechanismType;

   private CredentialInterfaceEnum credentialInterface;

   private String id;

   private String cIId;

   /**
    * Constructor
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
      this.authenticationMechanismType = authenticationMechanismType;
      if (!XsdString.isNull(this.authenticationMechanismType))
         this.authenticationMechanismType.
            setTag(XML.ELEMENT_AUTHENTICATION_MECHANISM_TYPE);
      this.credentialInterface = credentialInterface;
      this.id = id;
      this.cIId = cid;
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
   public XsdString getAuthenticationMechanismType()
   {
      return authenticationMechanismType;
   }

   /**
    * {@inheritDoc}
    */
   public CredentialInterfaceEnum getCredentialInterface()
   {
      return credentialInterface;
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
   public String getCredentialInterfaceId()
   {
      return cIId;
   }

   /**
    * {@inheritDoc}
    */
   public AuthenticationMechanism copy()
   {
      return new AuthenticationMechanismImpl(CopyUtil.cloneList(description),
            CopyUtil.clone(authenticationMechanismType), credentialInterface, CopyUtil.cloneString(id),
            CopyUtil.cloneString(cIId));
   }

   /**
    * {@inheritDoc}
    */
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
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<authentication-mechanism");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
      sb.append(">");

      if (description != null)
      {
         for (LocalizedXsdString s : description)
            sb.append(s);
      }

      sb.append(authenticationMechanismType);

      sb.append("<").append(XML.ELEMENT_CREDENTIAL_INTERFACE)
            .append(cIId == null ? "" : " id=\"" + cIId + "\"").append(">");
      sb.append(credentialInterface);
      sb.append("</").append(XML.ELEMENT_CREDENTIAL_INTERFACE).append(">");

      sb.append("</authentication-mechanism>");

      return sb.toString();
   }
}
