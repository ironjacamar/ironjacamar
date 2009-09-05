/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.fungal.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Injection utility
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Injection
{

   /**
    * Inject a value into an object property
    * @param propertyType The property type
    * @param propertyName The property name
    * @param propertyValue The property value
    * @param object The object
    * @exception NoSuchMethodException If the property method cannot be found
    * @exception IllegalAccessException If the property method cannot be accessed
    * @exception InvocationTargetException If the property method cannot be executed
    */
   public static void inject(String propertyType, String propertyName, String propertyValue, Object object)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
   {
      if (propertyType == null || propertyType.trim().equals(""))
         throw new IllegalArgumentException("PropertyType is undefined");

      if (propertyName == null || propertyName.trim().equals(""))
         throw new IllegalArgumentException("PropertyName is undefined");

      if (propertyValue == null || propertyValue.trim().equals(""))
         throw new IllegalArgumentException("PropertyValue is undefined");

      if (object == null)
         throw new IllegalArgumentException("Object is null");


      Class parameterClass = null;
      Object parameterValue = null;

      if (propertyType.equals("java.lang.String"))
      {
         parameterClass = String.class;
         parameterValue = getSubstitutionValue(propertyValue);
      }
      else if (propertyType.equals("byte") || propertyType.equals("java.lang.Byte"))
      {
         parameterClass = Byte.class;
         parameterValue = Byte.valueOf(getSubstitutionValue(propertyValue));
      }
      else if (propertyType.equals("short") || propertyType.equals("java.lang.Short"))
      {
         parameterClass = Short.class;
         parameterValue = Short.valueOf(getSubstitutionValue(propertyValue));
      }
      else if (propertyType.equals("int") || propertyType.equals("java.lang.Integer"))
      {
         parameterClass = Integer.class;
         parameterValue = Integer.valueOf(getSubstitutionValue(propertyValue));
      }
      else if (propertyType.equals("long") || propertyType.equals("java.lang.Long"))
      {
         parameterClass = Long.class;
         parameterValue = Long.valueOf(getSubstitutionValue(propertyValue));
      }
      else if (propertyType.equals("float") || propertyType.equals("java.lang.Float"))
      {
         parameterClass = Float.class;
         parameterValue = Float.valueOf(getSubstitutionValue(propertyValue));
      }
      else if (propertyType.equals("double") || propertyType.equals("java.lang.Double"))
      {
         parameterClass = Double.class;
         parameterValue = Double.valueOf(getSubstitutionValue(propertyValue));
      }
      else if (propertyType.equals("boolean") || propertyType.equals("java.lang.Boolean"))
      {
         parameterClass = Boolean.class;
         parameterValue = Boolean.valueOf(getSubstitutionValue(propertyValue));
      }
      else if (propertyType.equals("char") || propertyType.equals("java.lang.Character"))
      {
         parameterClass = Character.class;
         parameterValue = Character.valueOf((getSubstitutionValue(propertyValue)).charAt(0));
      }
      else
      {
         throw new IllegalArgumentException("Unknown property type: " + propertyType + " for " +
                                            "property " + propertyName);
      }

      String methodName = "set" + propertyName.substring(0, 1).toUpperCase();
      if (propertyName.length() > 1)
      {
         methodName += propertyName.substring(1);
      }

      Method method = object.getClass().getMethod(methodName, parameterClass);
      method.invoke(object, new Object[] {parameterValue});
   }

   /**
    * System property substitution
    * @param input The input string
    * @return The output
    */
   private static String getSubstitutionValue(String input)
   {
      if (input == null || input.trim().equals(""))
         return input;

      if (input.indexOf("${") != -1)
      {
         int from = input.indexOf("${");
         int to = input.indexOf("}");
         int dv = input.indexOf(":");
         
         String systemProperty = "";
         String defaultValue = "";
         if (dv == -1)
         {
            systemProperty = SecurityActions.getSystemProperty(input.substring(from + 2, to));
         }
         else
         {
            systemProperty = SecurityActions.getSystemProperty(input.substring(from + 2, dv));
            defaultValue = input.substring(dv + 1, to);
         }
         String prefix = "";
         String postfix = "";

         if (from != 0)
         {
            prefix = input.substring(0, from);
         }
         
         if (to + 1 < input.length() - 1)
         {
            postfix = input.substring(to + 1);
         }

         if (systemProperty != null && !systemProperty.trim().equals(""))
         {
            return prefix + systemProperty + postfix;
         }
         else if (defaultValue != null && !defaultValue.trim().equals(""))
         {
            return prefix + defaultValue + postfix;
         }
      }
      return input;
   }
}
