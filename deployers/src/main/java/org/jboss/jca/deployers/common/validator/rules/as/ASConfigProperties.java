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

package org.jboss.jca.deployers.common.validator.rules.as;

import org.jboss.jca.deployers.common.validator.Failure;
import org.jboss.jca.deployers.common.validator.Key;
import org.jboss.jca.deployers.common.validator.Rule;
import org.jboss.jca.deployers.common.validator.Severity;
import org.jboss.jca.deployers.common.validator.Validate;
import org.jboss.jca.deployers.common.validator.ValidateClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.resource.spi.ActivationSpec;

import org.jboss.metadata.rar.spec.ConfigPropertyMetaData;

/**
 * An ActivationSpec must use the valid set of config-property-type
 */
public class ASConfigProperties implements Rule
{
   /** Section */
   private static final String SECTION = "20.7";

   /** Valid types */
   private static final Set<Class> VALID_TYPES;

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
   }

   /**
    * Constructor
    */
   public ASConfigProperties()
   {
   }

   /**
    * Validate
    * @param v The validate object
    * @param rb The resource bundle 
    * @return The list of failures found; <code>null</code> if none
    */
   @SuppressWarnings("unchecked")
   public List<Failure> validate(Validate v, ResourceBundle rb)
   {
      if (v != null && 
          Key.ACTIVATION_SPEC == v.getKey() &&
          v.getClazz() != null &&
          ActivationSpec.class.isAssignableFrom(v.getClazz()))
      {
         ValidateClass vo = (ValidateClass)v;
         if (vo.getConfigProperties() != null && vo.getConfigProperties().size() > 0)
         {
            Class clz = vo.getClazz();
            List<Failure> failures = new ArrayList<Failure>(1);

            for (ConfigPropertyMetaData cpmd : vo.getConfigProperties())
            {
               try
               {
                  String methodName = "get" + cpmd.getName().substring(0, 1).toUpperCase(Locale.US);
                  if (cpmd.getName().length() > 1)
                  {
                     methodName += cpmd.getName().substring(1);
                  }

                  Method method = clz.getMethod(methodName, (Class[])null);

                  if (!VALID_TYPES.contains(method.getReturnType()))
                  {
                     StringBuilder sb = new StringBuilder("Class: " + vo.getClazz().getName());
                     sb = sb.append(" Property: " + cpmd.getName());
                     sb = sb.append(" Type: " + method.getReturnType().getName());

                     Failure failure = new Failure(Severity.WARNING,
                                                   SECTION,
                                                   rb.getString("as.ASConfigProperties"),
                                                   sb.toString());
                     failures.add(failure);
                  }
               }
               catch (Throwable t)
               {
                  try
                  {
                     String methodName = "is" + cpmd.getName().substring(0, 1).toUpperCase(Locale.US);
                     if (cpmd.getName().length() > 1)
                     {
                        methodName += cpmd.getName().substring(1);
                     }
                     
                     Method method = clz.getMethod(methodName, (Class[])null);

                     if (!VALID_TYPES.contains(method.getReturnType()))
                     {
                        StringBuilder sb = new StringBuilder("Class: " + vo.getClazz().getName());
                        sb = sb.append(" Property: " + cpmd.getName());
                        sb = sb.append(" Type: " + method.getReturnType().getName());

                        Failure failure = new Failure(Severity.WARNING,
                                                      SECTION,
                                                      rb.getString("as.ASConfigProperties"),
                                                      sb.toString());
                        failures.add(failure);
                     }
                  }
                  catch (Throwable it)
                  {
                     // Ignore
                  }
               }
            }

            if (failures.size() == 0)
               return null;

            return failures;
         }
      }

      return null;
   }
}
