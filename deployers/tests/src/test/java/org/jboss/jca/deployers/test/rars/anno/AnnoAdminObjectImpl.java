/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.deployers.test.rars.anno;

import jakarta.resource.spi.AdministeredObject;
import jakarta.resource.spi.ConfigProperty;

/**
 * AnnoAdminObjectImpl
 *
 * @version $Revision: $
 */
@AdministeredObject(adminObjectInterfaces = { AnnoAdminObject.class })
public class AnnoAdminObjectImpl implements AnnoAdminObject
{
   /** Serial version uid */
   private static final long serialVersionUID = 2L;

   /** first */
   @ConfigProperty(defaultValue = "12345", description = {"1st", "first" }, 
      ignore = true, supportsDynamicUpdates = false, confidential = true)
   private Long first;

   /** second */
   private Boolean second;

   /**
    * Default constructor
    */
   public AnnoAdminObjectImpl()
   {

   }

   /** 
    * Set first
    * @param first The value
    */
   public void setFirst(Long first)
   {
      this.first = first;
   }

   /** 
    * Get first
    * @return The value
    */
   public Long getFirst()
   {
      return first;
   }

   /** 
    * Set second
    * @param second The value
    */
   @ConfigProperty(defaultValue = "false", description =  {"2nd", "second" },
      ignore = false, supportsDynamicUpdates = true, confidential = false)
   public void setSecond(Boolean second)
   {
      this.second = second;
   }

   /** 
    * Get second
    * @return The value
    */
   public Boolean getSecond()
   {
      return second;
   }

   /** 
    * Returns a hash code value for the object.
    * @return A hash code value for this object.
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      if (first != null)
         result += 31 * result + 7 * first.hashCode();
      else
         result += 31 * result + 7;
      if (second != null)
         result += 31 * result + 7 * second.hashCode();
      else
         result += 31 * result + 7;
      return result;
   }

   /** 
    * Indicates whether some other object is equal to this one.
    * @param other The reference object with which to compare.
    * @return true if this object is the same as the obj argument, false otherwise.
    */
   @Override
   public boolean equals(Object other)
   {
      if (other == null)
         return false;
      if (other == this)
         return true;
      if (!(other instanceof AnnoAdminObjectImpl))
         return false;
      boolean result = true;
      AnnoAdminObjectImpl obj = (AnnoAdminObjectImpl)other;
      if (result)
      {
         if (first == null)
            result = obj.getFirst() == null;
         else
            result = first.equals(obj.getFirst());
      }
      if (result)
      {
         if (second == null)
            result = obj.getSecond() == null;
         else
            result = second.equals(obj.getSecond());
      }
      return result;
   }
}
