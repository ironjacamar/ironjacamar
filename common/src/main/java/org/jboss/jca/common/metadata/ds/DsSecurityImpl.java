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
package org.jboss.jca.common.metadata.ds;

import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.common.CredentialImpl;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * A DsSecurityImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
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
    * @throws ValidateException in case of validation error
    */
   public DsSecurityImpl(String userName, String password, String securityDomain, Extension reauthPlugin)
      throws ValidateException
   {
      super(userName, password, securityDomain);
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
         sb.append("<").append(DsSecurity.Tag.USERNAME).append(">");
         sb.append(getUserName());
         sb.append("</").append(DsSecurity.Tag.USERNAME).append(">");

         sb.append("<").append(DsSecurity.Tag.PASSWORD).append(">");
         sb.append(getPassword());
         sb.append("</").append(DsSecurity.Tag.PASSWORD).append(">");
      }
      else if (getSecurityDomain() != null)
      {
         sb.append("<").append(DsSecurity.Tag.SECURITY_DOMAIN).append(">");
         sb.append(getSecurityDomain());
         sb.append("</").append(DsSecurity.Tag.SECURITY_DOMAIN).append(">");
      }

      if (getReauthPlugin() != null)
      {
         sb.append("<").append(DsSecurity.Tag.REAUTH_PLUGIN);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(getReauthPlugin().getClassName()).append("\"");
         sb.append(">");

         if (getReauthPlugin().getConfigPropertiesMap() != null &&
             getReauthPlugin().getConfigPropertiesMap().size() > 0)
         {
            Iterator<Map.Entry<String, String>> it = getReauthPlugin().getConfigPropertiesMap().entrySet().iterator();
            
            while (it.hasNext())
            {
               Map.Entry<String, String> entry = it.next();

               sb.append("<").append(Extension.Tag.CONFIG_PROPERTY);
               sb.append(" name=\"").append(entry.getKey()).append("\">");
               sb.append(entry.getValue());
               sb.append("</").append(Extension.Tag.CONFIG_PROPERTY).append(">");
            }
         }

         sb.append("</").append(DsSecurity.Tag.REAUTH_PLUGIN).append(">");
      }

      sb.append("</security>");

      return sb.toString();
   }
}

