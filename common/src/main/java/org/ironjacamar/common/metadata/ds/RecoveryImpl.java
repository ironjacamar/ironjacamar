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
import org.ironjacamar.common.api.metadata.ds.Credential;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Map;

/**
 * A datasource recovery
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class RecoveryImpl extends org.ironjacamar.common.metadata.common.RecoveryImpl
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Credential credential;

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
      super(credential, plugin, noRecovery, expressions);
      this.credential = credential;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Credential getCredential()
   {
      return credential;
   }

   /**
    * {@inheritDoc}
    */
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

