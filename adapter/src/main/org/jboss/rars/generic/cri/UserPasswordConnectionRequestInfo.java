/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.rars.generic.cri;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * UserPasswordConnectionRequestInfo.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class UserPasswordConnectionRequestInfo implements ConnectionRequestInfo
{
   /** No user and password */
   public static final UserPasswordConnectionRequestInfo NONE = new UserPasswordConnectionRequestInfo();
   
   /** The user */
   private String user;
   
   /** The password */
   private String password;
   
   /** The hashCode */
   private int cachedHashCode = Integer.MIN_VALUE;
   
   /**
    * Create a new UserPasswordConnectionRequestInfo.
    */
   public UserPasswordConnectionRequestInfo()
   {
   }

   /**
    * Create a new UserPasswordConnectionRequestInfo.
    * 
    * @param user the user
    * @param password the password
    */
   public UserPasswordConnectionRequestInfo(String user, String password)
   {
      this.user = user;
      this.password = password;
   }

   /**
    * Get the user.
    * 
    * @return the user.
    */
   public String getUser()
   {
      return user;
   }

   /**
    * Get the password.
    * 
    * @return the password.
    */
   public String getPassword()
   {
      return password;
   }
   
   public int hashCode()
   {
      if (cachedHashCode == Integer.MIN_VALUE)
      {
         int hashCode = 37;
         if (user != null)
         {
            hashCode = (hashCode + 37) * user.hashCode();
            // This is deliberate, we ignore the password if there is no user
            if (password != null)
               hashCode = (hashCode + 37) * password.hashCode();
         }
         cachedHashCode = hashCode;
      }
      return cachedHashCode;
   }
   
   public boolean equals(Object object)
   {
      if (this == object)
         return true;
      if (object == null || object instanceof UserPasswordConnectionRequestInfo == false)
         return false;
      
      UserPasswordConnectionRequestInfo other = (UserPasswordConnectionRequestInfo) object;
      
      // This is deliberate, we ignore the password if there is no user
      if (user == null && other.user == null)
         return true;
      if (user == null && other.user != null)
         return false;
      if (user.equals(other.user) == false)
         return false;
      if (password == null && other.password == null)
         return true;
      if (password == null && other.password != null)
         return false;
      return password.equals(other.password);
   }
}
