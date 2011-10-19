/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import java.util.Set;

/**
 * This SPI interface represents the users, their passwords and roles in
 * the container environment
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface Callback extends Serializable
{
   /**
    * Get the domain
    * @return The domain
    */
   public String getDomain();

   /**
    * Get the users
    * @return A set of user names
    */
   public Set<String> getUsers();

   /**
    * Get the credential for an user
    * @param user The user name
    * @return The credential; <code>null</code> if user doesn't exists
    */
   public char[] getCredential(String user);

   /**
    * Get the roles for an user
    * @param user The user name
    * @return A set of roles; <code>null</code> if user doesn't exists or no roles
    */
   public String[] getRoles(String user);

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
