/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.fungal.deployment;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a set
 */
public class SetType
{
   private List<ValueType> value;
   private String clazz;
   private String elementClass;
   
   /**
    * Constructor
    */
   public SetType()
   {
      value = null;
      clazz = null;
      elementClass = null;
   }

   /**
    * Get the values
    * @return The value
    */
   public List<ValueType> getValue()
   {
      if (value == null)
         value = new ArrayList<ValueType>(1);

      return value;
   }

   /**
    * Get the class
    * @return The value
    */
   public String getClazz()
   {
      return clazz;
   }
   
   /**
    * Set the class
    * @param value The value
    */
   public void setClazz(String value)
   {
      clazz = value;
   }

   /**
    * Get the element class
    * @return The value
    */
   public String getElementClass()
   {
      return elementClass;
   }

   /**
    * Set the element class
    * @param value The value
    */
   public void setElementClass(String value)
   {
      elementClass = value;
   }
}
