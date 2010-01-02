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
 * Represents a map
 */
public class MapType
{
   private List<EntryType> entry;
   private String clazz;
   private String keyClass;
   private String valueClass;
   
   /**
    * Constructor
    */
   public MapType()
   {
      entry = null;
      clazz = null;
      keyClass = null;
      valueClass = null;
   }

   /**
    * Get the entries
    * @return The value
    */
   public List<EntryType> getEntry()
   {
      if (entry == null)
         entry = new ArrayList<EntryType>(1);

      return entry;
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
    * Get the key class
    * @return The value
    */
   public String getKeyClass()
   {
      return keyClass;
   }

   /**
    * Set the key class
    * @param value The value
    */
   public void setKeyClass(String value)
   {
      keyClass = value;
   }

   /**
    * Get the value class
    * @return The value
    */
   public String getValueClass()
   {
      return valueClass;
   }

   /**
    * Set the value class
    * @param value The value
    */
   public void setValueClass(String value)
   {
      valueClass = value;
   }
}
