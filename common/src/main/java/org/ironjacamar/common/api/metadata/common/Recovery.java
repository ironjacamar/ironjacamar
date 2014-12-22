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
package org.ironjacamar.common.api.metadata.common;

import org.ironjacamar.common.api.metadata.JCAMetadata;
import org.ironjacamar.common.api.metadata.ValidatableMetadata;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Iterator;
import java.util.Map;

/**
 * A Recovery.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
public class Recovery implements JCAMetadata, ValidatableMetadata
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7425365995463321893L;

   private final Credential credential;

   private final Extension recoverPlugin;

   private final Boolean noRecovery;

   /**
    * Create a new Recovery.
    *
    * @param credential credential
    * @param recoverPlugin plugin
    * @param noRecovery niRecovery
    * @throws ValidateException in case of not valid metadata creation
    */
   public Recovery(Credential credential, Extension recoverPlugin, Boolean noRecovery) throws ValidateException
   {
      super();
      this.credential = credential;
      this.recoverPlugin = recoverPlugin;
      this.noRecovery = noRecovery;
      this.validate();
   }

   /**
    * Get the security.
    *
    * @return the security.
    */
   public final Credential getCredential()
   {
      return credential;
   }

   /**
    * Get the plugin.
    *
    * @return the plugin.
    */
   public final Extension getRecoverPlugin()
   {
      return recoverPlugin;
   }

   /**
    * Get the noRecovery.
    *
    * @return the noRecovery.
    */
   public final Boolean getNoRecovery()
   {
      return noRecovery;
   }

   @Override
   public void validate() throws ValidateException
   {
      // the only field not yet validated is a Boolean and all value are fine
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((noRecovery == null) ? 0 : noRecovery.hashCode());
      result = prime * result + ((recoverPlugin == null) ? 0 : recoverPlugin.hashCode());
      result = prime * result + ((credential == null) ? 0 : credential.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof Recovery))
         return false;
      Recovery other = (Recovery) obj;
      if (noRecovery == null)
      {
         if (other.noRecovery != null)
            return false;
      }
      else if (!noRecovery.equals(other.noRecovery))
         return false;
      if (recoverPlugin == null)
      {
         if (other.recoverPlugin != null)
            return false;
      }
      else if (!recoverPlugin.equals(other.recoverPlugin))
         return false;
      if (credential == null)
      {
         if (other.credential != null)
            return false;
      }
      else if (!credential.equals(other.credential))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<recovery");
      if (noRecovery != null)
         sb.append(" ").append("no-recovery").append("=\"").append(noRecovery).append("\"");
      sb.append(">");

      if (credential != null)
      {
         sb.append("<").append("recover-credential").append(">");
         if (credential.getUserName() != null)
         {
            sb.append("<").append("user-name").append(">");
            sb.append(credential.getUserName());
            sb.append("</").append("user-name").append(">");

            sb.append("<").append("password").append(">");
            sb.append(credential.getPassword());
            sb.append("</").append("password").append(">");
         }
         else
         {
            sb.append("<").append("security-domain").append(">");
            sb.append(credential.getSecurityDomain());
            sb.append("</").append("security-domain").append(">");
         }
         sb.append("</").append("recover-credential").append(">");
      }

      if (recoverPlugin != null)
      {
         sb.append("<").append("recover-plugin");
         sb.append(" ").append("class-name").append("=\"");
         sb.append(recoverPlugin.getClassName()).append("\"");
         sb.append(">");

         if (recoverPlugin.getConfigPropertiesMap().size() > 0)
         {
            Iterator<Map.Entry<String, String>> it = recoverPlugin.getConfigPropertiesMap().entrySet().iterator();
            
            while (it.hasNext())
            {
               Map.Entry<String, String> entry = it.next();

               sb.append("<").append("config-property");
               sb.append(" name=\"").append(entry.getKey()).append("\">");
               sb.append(entry.getValue());
               sb.append("</").append("config-property").append(">");
            }
         }

         sb.append("</").append("recover-plugin").append(">");
      }

      sb.append("</recovery>");
      
      return sb.toString();
   }
}

