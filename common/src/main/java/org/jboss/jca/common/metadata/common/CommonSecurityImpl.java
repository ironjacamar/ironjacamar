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
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.validator.ValidateException;

import org.jboss.logging.Messages;

/**
 *
 * A SecurityImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class CommonSecurityImpl implements CommonSecurity
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5842402120520191086L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private final String securityDomainManaged;

   private final String securityDomainAndApplicationManaged;

   private final boolean applicationManaged;

   /**
    * Create a new SecurityImpl.
    *
    * @param securityDomainManaged securityDomainManaged
    * @param securityDomainAndApplicationManaged securityDomainAndApplicationManaged
    * @param applicationManaged applicationManagedS
    * @throws ValidateException ValidateException
    */
   public CommonSecurityImpl(String securityDomainManaged,
      String securityDomainAndApplicationManaged, boolean applicationManaged) throws ValidateException
   {
      super();
      this.securityDomainManaged = securityDomainManaged;
      this.securityDomainAndApplicationManaged = securityDomainAndApplicationManaged;
      this.applicationManaged = applicationManaged;
      this.validate();
   }


   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + (applicationManaged ? 1231 : 1237);
      result = prime * result +
               ((securityDomainAndApplicationManaged == null) ? 0 : securityDomainAndApplicationManaged.hashCode());
      result = prime * result + ((securityDomainManaged == null) ? 0 : securityDomainManaged.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof CommonSecurityImpl))
         return false;
      CommonSecurityImpl other = (CommonSecurityImpl) obj;
      if (applicationManaged != other.applicationManaged)
         return false;
      if (securityDomainAndApplicationManaged == null)
      {
         if (other.securityDomainAndApplicationManaged != null)
            return false;
      }
      else if (!securityDomainAndApplicationManaged.equals(other.securityDomainAndApplicationManaged))
         return false;
      if (securityDomainManaged == null)
      {
         if (other.securityDomainManaged != null)
            return false;
      }
      else if (!securityDomainManaged.equals(other.securityDomainManaged))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<security>");

      if (applicationManaged)
      {
         sb.append("<").append(CommonSecurity.Tag.APPLICATION).append("/>");
      }
      else if (securityDomainManaged != null)
      {
         sb.append("<").append(CommonSecurity.Tag.SECURITY_DOMAIN).append(">");
         sb.append(securityDomainManaged);
         sb.append("</").append(CommonSecurity.Tag.SECURITY_DOMAIN).append(">");
      }
      else if (securityDomainAndApplicationManaged != null)
      {
         sb.append("<").append(CommonSecurity.Tag.SECURITY_DOMAIN_AND_APPLICATION).append(">");
         sb.append(securityDomainAndApplicationManaged);
         sb.append("</").append(CommonSecurity.Tag.SECURITY_DOMAIN_AND_APPLICATION).append(">");
      }

      sb.append("</security>");
      
      return sb.toString();
   }

   /**
    * Get the securityDomainManaged.
    *
    * @return the securityDomainManaged.
    */
   @Override
   public final String getSecurityDomain()
   {
      return securityDomainManaged;
   }

   /**
    * Get the securityDomainAndApplicationManaged.
    *
    * @return the securityDomainAndApplicationManaged.
    */
   @Override
   public final String getSecurityDomainAndApplication()
   {
      return securityDomainAndApplicationManaged;
   }

   /**
    * Get the applicationManaged.
    *
    * @return the applicationManaged.
    */
   @Override
   public final boolean isApplication()
   {
      return applicationManaged;
   }

   @Override
   public void validate() throws ValidateException
   {
      if (securityDomainManaged != null && !securityDomainManaged.trim().equals(""))
      {
         if (securityDomainAndApplicationManaged != null && !securityDomainAndApplicationManaged.trim().equals(""))
         {
            throw new ValidateException(bundle.invalidSecurityConfiguration());
         }
         if (applicationManaged)
         {
            throw new ValidateException(bundle.invalidSecurityConfiguration());
         }
      }
      else
      {
         if (securityDomainAndApplicationManaged != null && !securityDomainAndApplicationManaged.trim().equals(""))
         {
            if (applicationManaged)
            {
               throw new ValidateException(bundle.invalidSecurityConfiguration());
            }
         }
         else
         {
            if (!applicationManaged)
            {
               throw new ValidateException(bundle.invalidSecurityConfiguration());
            }
         }
      }
   }
}
