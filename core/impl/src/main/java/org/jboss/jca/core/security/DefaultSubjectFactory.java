/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.security;

import org.jboss.jca.core.spi.security.SubjectFactory;

import java.security.Principal;

import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

/**
 * Implements a basic subject factory
 */
public class DefaultSubjectFactory implements SubjectFactory
{
   /** The security domain name */
   private String securityDomain;

   /** The user name */
   private String userName;

   /** The password */
   private String password;

   /**
    * Create a new DefaultSubjectFactory.
    */
   public DefaultSubjectFactory()
   {
      this(null, null, null);
   }

   /**
    * Create a new DefaultSubjectFactory.
    *
    * @param securityDomain securityDomain
    * @param userName userName
    * @param password password
    */
   public DefaultSubjectFactory(String securityDomain, String userName, String password)
   {
      this.securityDomain = securityDomain;
      this.userName = userName;
      this.password = password;
   }

   /**
    * Set the security domain
    * @param v The value
    */
   public void setSecurityDomain(String v)
   {
      this.securityDomain = v;
   }

   /**
    * Set the user name
    * @param v The value
    */
   public void setUserName(String v)
   {
      this.userName = v;
   }

   /**
    * Set the password
    * @param v The value
    */
   public void setPassword(String v)
   {
      this.password = v;
   }

   @Override
   public Subject createSubject()
   {
      if (userName == null)
         throw new IllegalStateException("UserName is null");

      if (password == null)
         throw new IllegalStateException("Password is null");

      Subject subject = new Subject();

      Principal p = new SimplePrincipal(userName);
      subject.getPrincipals().add(p);

      PasswordCredential credential = new PasswordCredential(userName, password.toCharArray());
      subject.getPrivateCredentials().add(credential);

      return subject;
   }

   @Override
   public Subject createSubject(String securityDomain)
   {
      //we just ignore atm the arg using a singleton for each security domain
      return createSubject();
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

      if (!(obj instanceof DefaultSubjectFactory))
         return false;

      DefaultSubjectFactory other = (DefaultSubjectFactory) obj;

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

   @Override
   public String toString()
   {
      return "DefaultSubjectFactory [securityDomain=" + securityDomain + ", userName=" + userName + "]";
   }
}
