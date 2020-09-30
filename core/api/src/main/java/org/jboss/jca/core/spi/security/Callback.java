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

package org.jboss.jca.core.spi.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * This SPI interface represents the security inflow contract in
 * the container environment
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Callback extends Serializable
{
   /**
    * Get the domain
    * @return The domain
    */
   public String getDomain();

   /**
    * Is an user mapping required
    * @return The value
    */
   public boolean isMappingRequired();

   /**
    * Get the default principal
    * @return The value; <code>null</code> if no default principal
    */
   public Principal getDefaultPrincipal();

   /**
    * Get the default groups
    * @return The value; <code>null</code> if no default groups
    */
   public String[] getDefaultGroups();

   /**
    * Map a principal
    * @param name The principal name
    * @return The value; <code>null</code> if no mapping could be found
    */
   public Principal mapPrincipal(String name);

   /**
    * Map a group
    * @param name The group name
    * @return The value; <code>null</code> if no mapping could be found
    */
   public String mapGroup(String name);

   /**
    * Applies all mappings in the array of {@code Callback} objects.
    *
    * @param callbacks an array of callbacks that will be mapped
    * @return the new array of mapped callbacks
    */
   public javax.security.auth.callback.Callback[] mapCallbacks(javax.security.auth.callback.Callback[] callbacks);

   /**
    * Start
    * @exception Throwable Thrown if an error occurs
    */
   public void start() throws Throwable;

   /**
    * Stop
    * @exception Throwable Thrown if an error occurs
    */
   public void stop() throws Throwable;
}
