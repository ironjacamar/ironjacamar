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
package org.ironjacamar.common.metadata.ds;

import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.metadata.ds.DsSecurity;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Map;

/**
 *
 * A DsSecurityImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class DsSecurityImpl extends CredentialImpl implements DsSecurity
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -5782260654400841898L;
   private final Extension reauthPlugin;

   /**
    * Create a new DsSecurityImpl.
    *
    * @param userName userName
    * @param password password
    * @param securityDomain securityDomain
    * @param reauthPlugin reauthPlugin
    * @param expressions expressions
    * @throws ValidateException in case of validation error
    */
   public DsSecurityImpl(String userName, String password, String securityDomain, Extension reauthPlugin,
                         Map<String, String> expressions)
      throws ValidateException
   {
      super(userName, password, securityDomain, expressions);
      this.reauthPlugin = reauthPlugin;
   }

   @Override
   public Extension getReauthPlugin()
   {
      return reauthPlugin;
   }

   @Override
   public void validate() throws ValidateException
   {
      //just super.validate(). The reaut-plugin is not mandatory
      super.validate();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((reauthPlugin == null) ? 0 : reauthPlugin.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof DsSecurityImpl))
         return false;
      DsSecurityImpl other = (DsSecurityImpl) obj;
      if (reauthPlugin == null)
      {
         if (other.reauthPlugin != null)
            return false;
      }
      else if (!reauthPlugin.equals(other.reauthPlugin))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<security>");

      if (getUserName() != null)
      {
         sb.append("<").append(XML.ELEMENT_USER_NAME).append(">");
         sb.append(getUserName());
         sb.append("</").append(XML.ELEMENT_USER_NAME).append(">");

         sb.append("<").append(XML.ELEMENT_PASSWORD).append(">");
         sb.append(getPassword());
         sb.append("</").append(XML.ELEMENT_PASSWORD).append(">");
      }
      else if (getSecurityDomain() != null)
      {
         sb.append("<").append(XML.ELEMENT_SECURITY_DOMAIN).append(">");
         sb.append(getSecurityDomain());
         sb.append("</").append(XML.ELEMENT_SECURITY_DOMAIN).append(">");
      }

      if (getReauthPlugin() != null)
      {
         sb.append("<").append(XML.ELEMENT_REAUTH_PLUGIN);
         sb.append(getReauthPlugin().toString());
         sb.append("</").append(XML.ELEMENT_REAUTH_PLUGIN).append(">");
      }

      sb.append("</security>");

      return sb.toString();
   }
}

