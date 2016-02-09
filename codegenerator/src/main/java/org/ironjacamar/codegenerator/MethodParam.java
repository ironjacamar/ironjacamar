/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.codegenerator;

/**
 * A MethodParam.
 *
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
public class MethodParam
{
   /**
    * name
    */
   private String name;

   /**
    * type
    */
   private String type;

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
    * toString
    *
    * @return name and type
    */
   @Override
   public String toString()
   {
      return name + "," + type;
   }
}
