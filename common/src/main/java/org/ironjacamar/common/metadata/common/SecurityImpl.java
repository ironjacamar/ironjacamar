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

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.common.Security;
import org.ironjacamar.common.api.validator.ValidateException;

import org.jboss.logging.Messages;

/**
 *
 * A SecurityImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class SecurityImpl implements Security
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private String securityDomainManaged;

   private String securityDomainAndApplicationManaged;

   private boolean applicationManaged;

   /**
    * Constructor
    *
    * @param securityDomainManaged securityDomainManaged
    * @param securityDomainAndApplicationManaged securityDomainAndApplicationManaged
    * @param applicationManaged applicationManagedS
    * @throws ValidateException ValidateException
    */
   public SecurityImpl(String securityDomainManaged,
                       String securityDomainAndApplicationManaged, boolean applicationManaged) throws ValidateException
   {
      this.securityDomainManaged = securityDomainManaged;
      this.securityDomainAndApplicationManaged = securityDomainAndApplicationManaged;
      this.applicationManaged = applicationManaged;
      this.validate();
   }

   /**
    * {@inheritDoc}
    */
   public String getSecurityDomain()
   {
      return securityDomainManaged;
   }

   /**
    * {@inheritDoc}
    */
   public String getSecurityDomainAndApplication()
   {
      return securityDomainAndApplicationManaged;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isApplication()
   {
      return applicationManaged;
   }

   /**
    * {@inheritDoc}
    */
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

   /**
    * {@inheritDoc}
    */
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

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof SecurityImpl))
         return false;
      SecurityImpl other = (SecurityImpl) obj;
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

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<security>");

      if (applicationManaged)
      {
         sb.append("<").append(CommonXML.ELEMENT_APPLICATION).append("/>");
      }
      else if (securityDomainManaged != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_SECURITY_DOMAIN).append(">");
         sb.append(securityDomainManaged);
         sb.append("</").append(CommonXML.ELEMENT_SECURITY_DOMAIN).append(">");
      }
      else if (securityDomainAndApplicationManaged != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_SECURITY_DOMAIN_AND_APPLICATION).append(">");
         sb.append(securityDomainAndApplicationManaged);
         sb.append("</").append(CommonXML.ELEMENT_SECURITY_DOMAIN_AND_APPLICATION).append(">");
      }

      sb.append("</security>");
      
      return sb.toString();
   }
}
