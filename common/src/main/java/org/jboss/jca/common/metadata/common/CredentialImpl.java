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
package org.jboss.jca.common.metadata.common;

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.validator.ValidateException;

import org.jboss.logging.Messages;

/**
 * Credential implementation
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class CredentialImpl implements Credential
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5842402120520191086L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private final String userName;

   private final String password;

   private final String securityDomain;

   /**
    * Create a new SecurityImpl.
    *
    * @param userName userName
    * @param password password
    * @param securityDomain securityDomain
    * @throws ValidateException ValidateException
    */
   public CredentialImpl(String userName, String password, String securityDomain) throws ValidateException
   {
      super();
      this.userName = userName;
      this.password = password;
      this.securityDomain = securityDomain;
      this.validate();
   }

   /**
    * Get the userName.
    *
    * @return the userName.
    */
   @Override
   public final String getUserName()
   {
      return userName;
   }

   /**
    * Get the password.
    *
    * @return the password.
    */
   @Override
   public final String getPassword()
   {
      return password;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((password == null) ? 0 : password.hashCode());
      result = prime * result + ((securityDomain == null) ? 0 : securityDomain.hashCode());
      result = prime * result + ((userName == null) ? 0 : userName.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof CredentialImpl))
         return false;
      CredentialImpl other = (CredentialImpl) obj;
      if (password == null)
      {
         if (other.password != null)
            return false;
      }
      else if (!password.equals(other.password))
         return false;
      if (securityDomain == null)
      {
         if (other.securityDomain != null)
            return false;
      }
      else if (!securityDomain.equals(other.securityDomain))
         return false;
      if (userName == null)
      {
         if (other.userName != null)
            return false;
      }
      else if (!userName.equals(other.userName))
         return false;
      return true;
   }

   /**
    * Get the securityDomainManaged.
    *
    * @return the securityDomainManaged.
    */
   @Override
   public final String getSecurityDomain()
   {
      return securityDomain;
   }

   @Override
   public void validate() throws ValidateException
   {
      if (userName != null)
      {
         if (securityDomain != null)
         {
            throw new ValidateException(bundle.invalidSecurityConfiguration());
         }
      }
   }
}
