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
package org.jboss.jca.common.metadata.resourceadapter;

import org.jboss.jca.common.api.metadata.common.SecurityManager;
import org.jboss.jca.common.api.metadata.resourceadapter.Security;

/**
 *
 * A SecurityImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class SecurityImpl implements Security
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1792878989502127046L;

   private final SecurityManager securityManager;

   private final String securityDomain;

   /**
    * Create a new SecurityImpl.
    *
    * @param securityManager securityManager
    * @param securityDomain securityDomain
    */
   public SecurityImpl(SecurityManager securityManager, String securityDomain)
   {
      super();
      this.securityManager = securityManager;
      this.securityDomain = securityDomain;
   }

   /**
    * Get the securityManager.
    *
    * @return the securityManager.
    */
   @Override
   public final SecurityManager getSecurityManager()
   {
      return securityManager;
   }

   /**
    * Get the securityDomain.
    *
    * @return the securityDomain.
    */
   @Override
   public final String getSecurityDomain()
   {
      return securityDomain;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((securityDomain == null) ? 0 : securityDomain.hashCode());
      result = prime * result + ((securityManager == null) ? 0 : securityManager.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof SecurityImpl))
         return false;
      SecurityImpl other = (SecurityImpl) obj;
      if (securityDomain == null)
      {
         if (other.securityDomain != null)
            return false;
      }
      else if (!securityDomain.equals(other.securityDomain))
         return false;
      if (securityManager != other.securityManager)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "SecurityImpl [securityManager=" + securityManager + ", securityDomain=" + securityDomain + "]";
   }
}

