/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.annotations.repository.jandex;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Privileged Blocks
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class SecurityActions
{ 
   /**
    * Constructor
    */
   private SecurityActions()
   {
   }

   /**
    * Get the declared fields
    * @param c The class
    * @param name The name
    * @return The field
    * @exception NoSuchFieldException If a matching field is not found.
    */
   static Field getDeclaredField(final Class<?> c, final String name)
      throws NoSuchFieldException
   {
      if (System.getSecurityManager() == null)
         return c.getDeclaredField(name);

      Field result = AccessController.doPrivileged(new PrivilegedAction<Field>()
      {
         public Field run()
         {
            try
            {
               return c.getDeclaredField(name);
            }
            catch (NoSuchFieldException e)
            {
               return null;
            }
         }
      });

      if (result != null)
         return result;

      throw new NoSuchFieldException();
   }

   /**
    * Get the method
    * @param c The class
    * @param name The name
    * @param params The parameters
    * @return The method
    * @exception NoSuchMethodException If a matching method is not found.
    */
   static Method getDeclaredMethod(final Class<?> c, final String name, final Class<?>... params)
      throws NoSuchMethodException
   {
      if (System.getSecurityManager() == null)
         return c.getDeclaredMethod(name, params);

      Method result = AccessController.doPrivileged(new PrivilegedAction<Method>()
      {
         public Method run()
         {
            try
            {
               return c.getDeclaredMethod(name, params);
            }
            catch (NoSuchMethodException e)
            {
               return null;
            }
         }
      });

      if (result != null)
         return result;

      throw new NoSuchMethodException();
   }
}
