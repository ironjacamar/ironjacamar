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
package org.jboss.jca.core.connectionmanager.rar;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * 
 * A SimpleConnectionRequestInfoImpl.
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class SimpleConnectionRequestInfoImpl implements ConnectionRequestInfo
{

   /**
    * userID
    */
   private String userID = null;

   /**
    * 
    * Create a new SimpleConnectionRequestInfoImpl.
    * 
    * @param userId parameter 
    */
   public SimpleConnectionRequestInfoImpl(String userId)
   {
      setUserID(userId);
   }

   /**
    * 
    * setter
    * 
    * @param userId parameter
    */
   public void setUserID(String userId)
   {
      this.userID = userId;
   }

   /**
    * 
    * getter
    * 
    * @return userID
    */
   public String getUserID()
   {
      return userID;
   }

   @Override
   public boolean equals(Object o)
   {
      boolean equals = true;
      SimpleConnectionRequestInfoImpl other;
      if ((o instanceof SimpleConnectionRequestInfoImpl))
      {
         other = (SimpleConnectionRequestInfoImpl) o;

         if (this.userID == null)
            equals = other.getUserID() == null;
         else
            equals = this.userID.equals(other.getUserID());

      }
      else
         equals = false;

      return equals;

   }

   @Override
   public int hashCode()
   {
      return 43 + (this.userID != null ? this.userID.hashCode() : 0);
   }

   @Override
   public String toString()
   {
      return "Simple CRI userID: " + userID;
   }
}
