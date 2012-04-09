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

package org.jboss.jca.core.workmanager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Utility for class related operations.
 * 
 * @version $Rev$ $Date$
 */
final class ClassUtil
{
   /** Empty class array */
   private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

   /**
    * Private constructor
    */
   private ClassUtil()
   {
   }

   /**
    * Returns true if <b>from</b> is assignable to <b>to</b>, false otherwise.
    * 
    * @param from assignable class
    * @param to assigned class
    * @return true if <b>from</b> is assignable to <b>to</b>, false ow.
    */
   static boolean isClassAssignable(Class<?> from, Class<?> to)
   {
      if (from == null)
         throw new IllegalArgumentException("from is null");

      if (to == null)
         throw new IllegalArgumentException("to is null");

      return to.isAssignableFrom(from);
   }

   /**
    * Returns true if <b>synchronized</b> keyword exists, false otherwise.
    * 
    * @param modifiers member modifieres
    * @return true if <b>synchronized</b> keyword exists, false otherwise
    */
   static boolean modifiersHasSynchronizedKeyword(int modifiers)
   {
      return Modifier.isSynchronized(modifiers);
   }

   /**
    * Gets class' method with given name and parameter types.
    * @param clazz class
    * @param methodName method name
    * @param parameterTypes parameter types
    * @return method
    * @throws NoSuchMethodException if not method exist
    */
   static Method getClassMethod(Class<?> clazz, String methodName, 
                                Class<?>[] parameterTypes) throws NoSuchMethodException
   {
      if (clazz == null)
         throw new IllegalArgumentException("Class is null");


      if (methodName == null || methodName.equals(""))
         throw new IllegalArgumentException("Method name is null or empty");

      if (parameterTypes == null)
         parameterTypes = EMPTY_CLASS_ARRAY;

      return clazz.getMethod(methodName, parameterTypes);
   }
}
