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

package org.ironjacamar.validator.rules.as;

import java.lang.reflect.Constructor;
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
    * Get the constructor
    * @param c The class
    * @param params The parameters
    * @return The constructor
    * @exception NoSuchMethodException If a matching method is not found.
    */
   static Constructor<?> getConstructor(final Class<?> c, final Class<?>... params)
      throws NoSuchMethodException
   {
      if (System.getSecurityManager() == null)
         return c.getConstructor(params);

      Constructor<?> result = AccessController.doPrivileged(new PrivilegedAction<Constructor<?>>()
      {
         public Constructor<?> run()
         {
            try
            {
               return c.getConstructor(params);
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
