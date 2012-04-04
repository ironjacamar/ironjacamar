/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.embedded.arquillian;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

/**
 * SecurityActions
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
final class SecurityActions
{
   /**
    * No instantiation
    */
   private SecurityActions()
   {
      throw new UnsupportedOperationException("No instantiation");
   }

   /**
    * Get field which contains a certain annotation
    * @param clz The class
    * @param annotationClass The annotation class
    * @return The list of fields
    */
   public static List<Field> getFieldsWithAnnotation(final Class<?> clz, 
                                                     final Class<? extends Annotation> annotationClass)
   {
      List<Field> declaredAccessableFields = AccessController.doPrivileged(new PrivilegedAction<List<Field>>()
      {
         public List<Field> run()
         {
            List<Field> foundFields = new ArrayList<Field>(1);
            Class<?> nextClz = clz;
            while (nextClz != Object.class)
            {
               for (Field field : nextClz.getDeclaredFields())
               {
                  if (field.isAnnotationPresent(annotationClass))
                  {
                     if (!field.isAccessible())
                     {
                        field.setAccessible(true);
                     }
                     foundFields.add(field);
                  }
               }
               nextClz = nextClz.getSuperclass();
            }
            return foundFields;
         }
      });

      return declaredAccessableFields;
   }
}
