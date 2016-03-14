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
package org.ironjacamar.common.metadata.common;

import org.ironjacamar.common.api.metadata.common.Credential;
import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.metadata.common.Recovery;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Map;

/**
 * A Recovery
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
public class RecoveryImpl extends AbstractMetadata implements Recovery
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7425365995463321893L;

   /** The credential */
   private Credential credential;

   /** The extension */
   protected Extension plugin;

   /** No recovery */
   protected Boolean noRecovery;

   /**
    * Create a new Recovery.
    *
    * @param credential credential
    * @param plugin plugin
    * @param noRecovery niRecovery
    * @param expressions expressions
    * @throws ValidateException in case of not valid metadata creation
    */
   public RecoveryImpl(Credential credential, Extension plugin, Boolean noRecovery,
                       Map<String, String> expressions) throws ValidateException
   {
      super(expressions);
      this.credential = credential;
      this.plugin = plugin;
      this.noRecovery = noRecovery;
      this.validate();
   }

   /**
    * Get the security.
    *
    * @return the security.
    */
   public Credential getCredential()
   {
      return credential;
   }

   /**
    * Get the plugin.
    *
    * @return the plugin.
    */
   public Extension getPlugin()
   {
      return plugin;
   }

   /**
    * Get the noRecovery.
    *
    * @return the noRecovery.
    */
   public Boolean isNoRecovery()
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
      result = prime * result + ((plugin == null) ? 0 : plugin.hashCode());
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
      if (!(obj instanceof RecoveryImpl))
         return false;
      RecoveryImpl other = (RecoveryImpl) obj;
      if (noRecovery == null)
      {
         if (other.noRecovery != null)
            return false;
      }
      else if (!noRecovery.equals(other.noRecovery))
         return false;
      if (plugin == null)
      {
         if (other.plugin != null)
            return false;
      }
      else if (!plugin.equals(other.plugin))
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
         sb.append("<").append("recovery-credential").append(">");

         sb.append("<").append("security-domain").append(">");
         sb.append(credential.getSecurityDomain());
         sb.append("</").append("security-domain").append(">");

         sb.append("</").append("recovery-credential").append(">");
      }

      if (plugin != null)
      {
         sb.append("<").append("recovery-plugin");
         sb.append(plugin.toString());
         sb.append("</").append("recovery-plugin").append(">");
      }

      sb.append("</recovery>");
      
      return sb.toString();
   }
}

