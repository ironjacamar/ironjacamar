/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.jca.rar.support;

import java.io.Serializable;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * TestConnectionRequestInfo.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class TestConnectionRequestInfo implements ConnectionRequestInfo, Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1861728676406044442L;

   /** The user */
   protected String userName;

   /** The password */
   protected String password;

   public TestConnectionRequestInfo(String userName, String password)
   {
      this.userName = userName;
      this.password = password;
   }

   /**
    * Get the user.
    * 
    * @return the user.
    */
   public String getUserName()
   {
      return userName;
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

   public boolean equals(Object object)
   {
      if (object == null || object instanceof  TestConnectionRequestInfo == false)
         return false;
      
      TestConnectionRequestInfo other = (TestConnectionRequestInfo) object;
      if (userName == null)
      {
         if (other.userName != null)
            return false;
      }
      else
      {
         if (userName.equals(other.userName) == false)
            return false;
      }
      if (password == null)
      {
         if (other.password != null)
            return false;
      }
      else
      {
         if (password.equals(other.password) == false)
            return false;
      }
      return true;
   }

   public int hashCode()
   {
      return ((userName == null) ? 37 : userName.hashCode()) + 37 * ((password == null) ? 37 : password.hashCode());
   }
}
