/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.codegenerator;

/**
 * A ConfigPropType.
 * 
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
public class ConfigPropType
{
   /** name */
   private String name;
   /** type */
   private String type;
   /** value */
   private String value;
   /** required */
   private boolean required;

   /**
    * Set the name.
    * 
    * @param name The name to set.
    * @param type The type to set.
    * @param value The value to set.
    * @param required The required to set.
    */
   public ConfigPropType(String name, String type, String value, boolean required)
   {
   
      this.name = name;
      this.type = type;
      this.value = value;
      this.required = required;
   }
   
   /**
    * Set the name.
    * 
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Get the name.
    * 
    * @return the name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set the type.
    * 
    * @param type The type to set.
    */
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * Get the type.
    * 
    * @return the type.
    */
   public String getType()
   {
      return type;
   }

   /**
    * Set the value.
    * 
    * @param value The value to set.
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * Get the value.
    * 
    * @return the value.
    */
   public String getValue()
   {
      return value;
   }

   /**
    * Set the required.
    * 
    * @param required The required to set.
    */
   public void setRequired(boolean required)
   {
      this.required = required;
   }

   /**
    * Get the required.
    * 
    * @return the required.
    */
   public boolean isRequired()
   {
      return required;
   }
}
