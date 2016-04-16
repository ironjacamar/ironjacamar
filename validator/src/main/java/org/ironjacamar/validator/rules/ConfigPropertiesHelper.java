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

package org.ironjacamar.validator.rules;

import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.validator.Failure;
import org.ironjacamar.validator.Severity;
import org.ironjacamar.validator.ValidateClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A ConfigPropertiesHelper.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class ConfigPropertiesHelper
{
   /** Valid types */
   public static final Set<Class> VALID_TYPES;

   /** Valid types */
   public static final Set<Class> WARNING_TYPES;

   static
   {
      VALID_TYPES = new HashSet<Class>(9);
      VALID_TYPES.add(Boolean.class);
      VALID_TYPES.add(Byte.class);
      VALID_TYPES.add(Character.class);
      VALID_TYPES.add(Double.class);
      VALID_TYPES.add(Float.class);
      VALID_TYPES.add(Integer.class);
      VALID_TYPES.add(Long.class);
      VALID_TYPES.add(Short.class);
      VALID_TYPES.add(String.class);

      WARNING_TYPES = new HashSet<Class>(8);
      WARNING_TYPES.add(boolean.class);
      WARNING_TYPES.add(byte.class);
      WARNING_TYPES.add(char.class);
      WARNING_TYPES.add(double.class);
      WARNING_TYPES.add(float.class);
      WARNING_TYPES.add(int.class);
      WARNING_TYPES.add(long.class);
      WARNING_TYPES.add(short.class);
   }

   /**
    * validate ConfigProperties type
    *
    * @param vo ValidateClass
    * @param section section in the spec document
    * @param failMsg fail or warn message
    * @return list of failures
    */
   public static List<Failure> validateConfigPropertiesType(ValidateClass vo, String section, String failMsg)
   {

      List<Failure> failures = new ArrayList<Failure>(1);

      for (ConfigProperty cpmd : vo.getConfigProperties())
      {
         try
         {
            containGetOrIsMethod(vo, "get", cpmd, section, failMsg, failures);
         }
         catch (Throwable t)
         {
            try
            {
               containGetOrIsMethod(vo, "is", cpmd, section, failMsg, failures);
            }
            catch (Throwable it)
            {
               // Ignore
            }
         }
      }

      if (failures.isEmpty())
         return null;
      return failures;
   }

   /**
    * validated object contain 'get or 'is' Method
    *
    * @param vo ValidateClass
    * @param getOrIs 'get or 'is' String
    * @param cpmd ConfigProperty metadata
    * @param section section in the spec document
    * @param failMsg fail or warn message
    * @param failures list of failures
    * @throws NoSuchMethodException
    */
   private static void containGetOrIsMethod(ValidateClass vo, String getOrIs,
      ConfigProperty cpmd, String section, String failMsg, List<Failure> failures)
      throws NoSuchMethodException
   {
      String methodName = getOrIs + cpmd.getConfigPropertyName().getValue().substring(0, 1).toUpperCase(Locale.US);
      if (cpmd.getConfigPropertyName().getValue().length() > 1)
      {
         methodName += cpmd.getConfigPropertyName().getValue().substring(1);
      }

      Method method = SecurityActions.getMethod(vo.getClazz(), methodName, (Class[])null);

      if (!VALID_TYPES.contains(method.getReturnType()))
      {
         StringBuilder sb = new StringBuilder("Class: " + vo.getClazz().getName());
         sb = sb.append(" Property: " + cpmd.getConfigPropertyName().getValue());
         sb = sb.append(" Type: " + method.getReturnType().getName());

         Failure failure;
         if (WARNING_TYPES.contains(method.getReturnType()))
         {
            failure = new Failure(Severity.WARNING,
                                  section,
                                  failMsg,
                                  sb.toString());
         }
         else
         {
            failure = new Failure(Severity.ERROR,
                  section,
                  failMsg,
                  sb.toString());
         }
         failures.add(failure);
      }
   }
}
