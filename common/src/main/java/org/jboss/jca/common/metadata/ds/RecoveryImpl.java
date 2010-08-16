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

import org.jboss.jca.common.api.metadata.ds.Recovery;

/**
 *
 * A RecoveryImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class RecoveryImpl implements Recovery
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1704161236644120141L;

   private final boolean noRecover;

   private final String recoverUserName;

   private final String recoverPassword;

   private final String recoverSecurityDomain;

   /**
    * Create a new RecoveryImpl.
    *
    * @param noRecover boolean value from xml tab
    * @param recoverUserName user name for recover
    * @param recoverPassword password
    * @param recoverSecurityDomain security domain used during recover
    */
   public RecoveryImpl(boolean noRecover, String recoverUserName, String recoverPassword,
         String recoverSecurityDomain)
   {
      super();
      this.noRecover = noRecover;
      this.recoverUserName = recoverUserName;
      this.recoverPassword = recoverPassword;
      this.recoverSecurityDomain = recoverSecurityDomain;
   }

   /**
    * Get the noRecover.
    *
    * @return the noRecover.
    */
   @Override
   public final boolean isNoRecover()
   {
      return noRecover;
   }

   /**
    * Get the recoverUserName.
    *
    * @return the recoverUserName.
    */
   @Override
   public final String getRecoverUserName()
   {
      return recoverUserName;
   }

   /**
    * Get the recoverPassword.
    *
    * @return the recoverPassword.
    */
   @Override
   public final String getRecoverPassword()
   {
      return recoverPassword;
   }

   /**
    * Get the recoverSecurityDomain.
    *
    * @return the recoverSecurityDomain.
    */
   @Override
   public final String getRecoverSecurityDomain()
   {
      return recoverSecurityDomain;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + (noRecover ? 1231 : 1237);
      result = prime * result + ((recoverPassword == null) ? 0 : recoverPassword.hashCode());
      result = prime * result + ((recoverSecurityDomain == null) ? 0 : recoverSecurityDomain.hashCode());
      result = prime * result + ((recoverUserName == null) ? 0 : recoverUserName.hashCode());
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
      if (noRecover != other.noRecover)
         return false;
      if (recoverPassword == null)
      {
         if (other.recoverPassword != null)
            return false;
      }
      else if (!recoverPassword.equals(other.recoverPassword))
         return false;
      if (recoverSecurityDomain == null)
      {
         if (other.recoverSecurityDomain != null)
            return false;
      }
      else if (!recoverSecurityDomain.equals(other.recoverSecurityDomain))
         return false;
      if (recoverUserName == null)
      {
         if (other.recoverUserName != null)
            return false;
      }
      else if (!recoverUserName.equals(other.recoverUserName))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "RecoveryImpl [noRecover=" + noRecover + ", recoverUserName=" + recoverUserName
            + ", recoverPassword=" + recoverPassword + ", recoverSecurityDomain=" + recoverSecurityDomain + "]";
   }

}
