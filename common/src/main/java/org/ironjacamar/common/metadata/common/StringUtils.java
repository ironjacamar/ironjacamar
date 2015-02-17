/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.metadata.common;

import java.util.Map;

/**
 * A utility class for String operations
 *
 * @author <a href="vrastseluev@gmail.com">Vladimir Rastseluev</a>
 *
 */
public class StringUtils
{
   /** a tag for map keys */
   private static String startTag = ">?&%<";
   /** a tag for map keys */
   private static String endTag = "<%&?>";

   /**
    * Restores expression with substituted default value
    * @param m a Map with expressions
    * @param key of the Map
    * @param subkey of the Map
    * @param v value for substitution
    * @return restored expression string
    */
   public static String restoreExpression(Map<String, String> m, String key, String subkey, String v)
   {
      String k = key;

      if (subkey != null)
      {
         if (!isIncorrectExpression(subkey) && subkey.startsWith("${"))
         {
            subkey = subkey.substring(2, subkey.length() - 1);

            if (subkey.indexOf(":") != -1)
               subkey = subkey.substring(0, subkey.indexOf(":"));
         }

         k += "|" + subkey;
      }

      return substituteValueInExpression(m.get(k), v);

   }

   /**
    * Substitutes a default value in expression by a new one
    * @param expression to check
    * @param newValue to substitute
    * @return resulting expression
    */
   public static String substituteValueInExpression(String expression, String newValue)
   {
      ExpressionTemplate t = new ExpressionTemplate(expression);
      if (newValue != null && (getExpressionKey(t.getTemplate()) == null ||
            (t.isComplex()  && !newValue.equals(t.getValue()))))
         return newValue;

      String result = t.getSubstitution();
      if (!t.isComplex() && newValue != null)
      {
         int start = result.lastIndexOf(":$");
         start = result.indexOf(":", start + 1);
         int end = result.indexOf("}", start + 1);

         if (start < 0 || end < 0 || start == result.lastIndexOf("${:}") + 2)
            return result;

         result = result.substring(0, start + 1) + newValue +  result.substring(end);
      }
      return result;
   }

   /**
    * Returns true if the parameter is an incorrect expression
    * @param ex expression to check
    * @return boolean
    */
   public static boolean isIncorrectExpression(String ex)
   {
      return isEmptyTrimmed(ex) || ex.equals(transformExpression(ex));
   }

   /**
    * System property substitution utility method
    * @param toTransform The input string
    * @return The output
    */
   public static String transformExpression(String toTransform)
   {
      return new ExpressionTemplate(toTransform).getValue();
   }

   /**
    * Returns true if the string is null or have no symbols after being trimmed
    * @param input string
    * @return boolean
    */
   public static boolean isEmptyTrimmed(String input)
   {
      return input == null || input.trim().equals("");
   }

   /**
    * Returns true if the string is null or have no symbols
    * @param input string
    * @return boolean
    */
   public static boolean isEmpty(String input)
   {
      return input == null || input.length() == 0;
   }

   /**
    * Get an entities map key from the string
    * @param result the string where to find the key
    * @return the key or null if nothing was found
    */
   public static String getExpressionKey(String result)
   {
      if (result == null)
         return null;
      try
      {
         int from = result.indexOf(startTag);
         int to = result.indexOf(endTag, from);
         Integer.parseInt(result.substring(from + 5, to));
         return result.substring(from, to + 5);
      }
      catch (Exception e)
      {
         return null;
      }
   }

   /**
    * Creates a template key based on index
    * @param index for the key
    * @return a key String
    */
   public static String createKey(int index)
   {
      return startTag + index + endTag;
   }
}
