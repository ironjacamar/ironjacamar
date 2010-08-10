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

import org.jboss.jca.common.api.metadata.ds.SecuritySettings;


/**
 *
 * A SecuritySettingsImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class SecuritySettingsImpl implements SecuritySettings
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -5842402120520191086L;

   private final SecurityManager securityManager;

   private final String securityDomain;

   /**
    * Create a new SecuritySettingsImpl.
    *
    * @param securityManager securityManager
    * @param securityDomain securityDomain
    */
   public SecuritySettingsImpl(SecurityManager securityManager, String securityDomain)
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
      if (!(obj instanceof SecuritySettingsImpl))
         return false;
      SecuritySettingsImpl other = (SecuritySettingsImpl) obj;
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
      return "SecuritySettingsImpl [securityManager=" + securityManager + ", securityDomain=" + securityDomain + "]";
   }

}
