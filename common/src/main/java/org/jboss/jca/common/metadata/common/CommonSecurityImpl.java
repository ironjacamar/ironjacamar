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

import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.validator.ValidateException;


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
      return "CommonSecurityImpl [securityDomainManaged=" + securityDomainManaged +
             ", securityDomainAndApplicationManaged=" + securityDomainAndApplicationManaged +
             ", applicationManaged=" + applicationManaged + "]";
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
            throw new ValidateException(
                                        "cannot construct a CommonSecurity having set both security-domain-managed "
                                           + "and security-domain-and-application-managed ");
         }
         if (applicationManaged)
         {
            throw new ValidateException(
                                        "cannot construct a CommonSecurity having set both security-domain-managed "
                                           + "and appliction-managed ");
         }
      }
      else
      {
         if (securityDomainAndApplicationManaged != null && !securityDomainAndApplicationManaged.trim().equals(""))
         {
            if (applicationManaged)
            {
               throw new ValidateException(
                                           "cannot construct a CommonSecurity having set "
                                              + "both security-domain-and-application-managed "
                                              + "and appliction-managed ");
            }
         }
         else
         {
            if (!applicationManaged)
            {
               throw new ValidateException(
                                           "cannot construct a CommonSecurity object with security-domain-managed "
                                              + "and security-domain-and-application-managed not set and "
                                              + "appliction-managed not set or false");
            }
         }
      }
   }
}
