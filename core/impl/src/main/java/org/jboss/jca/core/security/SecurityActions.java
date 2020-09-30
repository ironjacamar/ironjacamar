/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.security;

import java.io.InputStream;
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
    * Get the input stream for a resource in the context class loader
    * @param name The name of the resource
    * @return The input stream
    */
   static InputStream getResourceAsStream(final String name)
   {
      if (System.getSecurityManager() == null)
         return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);

      return AccessController.doPrivileged(new PrivilegedAction<InputStream>() 
      {
         public InputStream run()
         {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
         }
      });
   }
}
