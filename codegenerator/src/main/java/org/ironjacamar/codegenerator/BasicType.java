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
 * A BasicType.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public enum BasicType
{
   /**
    * string
    */
   String,
   /**
    * boolean
    */
   Boolean,
   /**
    * integer
    */
   Integer,
   /**
    * double
    */
   Double,
   /**
    * bye
    */
   Byte,
   /**
    * short
    */
   Short,
   /**
    * long
    */
   Long,
   /**
    * float
    */
   Float,
   /**
    * char
    */
   Character;

   private static String[] primitive = {"int", "boolean", "double", "byte", "short", "long", "float", "char"};

   /**
    * is basic type
    *
    * @param type type string
    * @return boolean true if basic type
    */
   public static boolean isBasicType(String type)
   {
      for (BasicType pt : BasicType.values())
      {
         if (type.equals(pt.toString()))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * is primitive type
    *
    * @param type type string
    * @return boolean true if basic type
    */
   public static boolean isPrimitiveType(String type)
   {
      for (String ptype : primitive)
      {
         if (type.equals(ptype))
            return true;
      }
      return false;
   }

   /**
    * return string include all basic types
    *
    * @return String all types
    */
   public static String allBasicType()
   {
      StringBuilder sb = new StringBuilder();
      BasicType[] types = BasicType.values();
      for (int i = 0; i < types.length; i++)
      {
         sb.append(types[i].toString());
         if (i + 1 < types.length)
            sb.append(", ");
      }
      return sb.toString();
   }

   /**
    * return string include all basic and primitive types
    *
    * @return String all types
    */
   public static String allType()
   {
      StringBuilder sb = new StringBuilder();
      for (String ptype : primitive)
      {
         sb.append(ptype);
         sb.append(", ");
      }
      BasicType[] types = BasicType.values();
      for (int i = 0; i < types.length; i++)
      {
         sb.append(types[i].toString());
         if (i + 1 < types.length)
            sb.append(", ");
      }
      return sb.toString();
   }

   /**
    * return default value string
    *
    * @param type type string
    * @return String all types
    */
   public static String defaultValue(String type)
   {
      switch (type)
      {
         case "String":
            return "null";
         case "boolean":
            return "false";
         case "int":
            return "0";
         case "double":
            return "0.0";
         case "long":
            return "0";
         case "byte":
            return "0";
         case "short":
            return "0";
         case "float":
            return "0.0";
         case "char":
            return "''";
      }
      return "null";
   }
}
