/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.security.reauth.ra.cri;

import java.io.Serializable;

import jakarta.resource.spi.ConnectionRequestInfo;

import org.jboss.logging.Logger;

/**
 * A simple connection request info object
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class ReauthCri implements ConnectionRequestInfo, Serializable
{
   private static final long serialVersionUID = 1L;
   
   private static Logger log = Logger.getLogger(ReauthCri.class);

   private final String userName;
   private final String password;

   /**
    * Constructor
    * @param userName The user name
    * @param password The password
    */
   public ReauthCri(final String userName, final String password)
   {
      if (userName == null)
         throw new IllegalArgumentException("UserName is null");

      if (password == null)
         throw new IllegalArgumentException("UserName is null");

      this.userName = userName;
      this.password = password;
   }

   /**
    * Get the user name
    * @return The value
    */
   public String getUserName()
   {
      return userName;
   }

   /**
    * Get the password
    * @return The value
    */
   public String getPassword()
   {
      return password;
   }

   /**
    * Hash code
    * @return The value
    */
   public int hashCode()
   {
      int hashCode = 7;

      if (userName != null)
         hashCode += 7 * userName.hashCode();

      if (password != null)      
         hashCode += 7 * password.hashCode();

      return hashCode;
   }

   /**
    * Equals
    * @param obj The other object
    * @return True if equal; otherwise false
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;

      if (obj == null || !(obj instanceof ReauthCri))
         return false;

      ReauthCri rCri = (ReauthCri)obj;

      boolean result = true;
      if (result)
      {
         if (userName == null)
         {
            result = rCri.getUserName() == null;
         }
         else
         {
            result = userName.equals(rCri.getUserName());
         }
      }

      if (result)
      {
         if (password == null)
         {
            result = rCri.getPassword() == null;
         }
         else
         {
            result = password.equals(rCri.getPassword());
         }
      }

      return result;
   }

   /**
    * String representation
    * @return The value
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ReauthCri@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[userName=").append(userName);
      sb.append(" password=").append(password);
      sb.append("]");

      return sb.toString();
   }
}
