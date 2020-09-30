/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.deployers.common;

import java.lang.reflect.AccessibleObject;
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
    * Get the classloader.
    * @param c The class
    * @return The classloader
    */
   static ClassLoader getClassLoader(final Class<?> c)
   {
      if (System.getSecurityManager() == null)
         return c.getClassLoader();

      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return c.getClassLoader();
         }
      });
   }

   /**
    * Load
    * @param lib The library
    */
   static void load(final String lib)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>() 
      {
         public Object run()
         {
            System.load(lib);
            return null;
         }
      });
   }


   /**
    * Get the methods
    * @param c The class
    * @return The methods
    */
   static Method[] getMethods(final Class<?> c)
   {
      if (System.getSecurityManager() == null)
         return c.getMethods();

      return AccessController.doPrivileged(new PrivilegedAction<Method[]>()
      {
         public Method[] run()
         {
            return c.getMethods();
         }
      });
   }

   /**
    * Set accessible
    * @param ao The object
    */
   static void setAccessible(final AccessibleObject ao)
   {
      if (System.getSecurityManager() == null)
         ao.setAccessible(true);

      AccessController.doPrivileged(new PrivilegedAction<Object>()
      {
         public Object run()
         {
            ao.setAccessible(true);
            return null;
         }
      });
   }
}
