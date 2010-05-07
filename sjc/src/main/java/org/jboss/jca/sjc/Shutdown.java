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

package org.jboss.jca.sjc;

import java.io.Serializable;
import java.util.Arrays;

import com.github.fungal.api.remote.Command;

/**
 * Shutdown command
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
class Shutdown implements Command
{
   /** Command name */
   private static final String NAME = "shutdown";

   /**
    * Constructor
    */
   Shutdown()
   {
   }

   /**
    * Get the name of the command
    * @return The name
    */
   public String getName()
   {
      return NAME;
   }

   /**
    * Get the parameter types of the command; <code>null</code> if none
    * @return The types
    */
   public Class[] getParameterTypes()
   {
      return null;
   }

   /**
    * Invoke
    * @param args The arguments
    * @return The return value
    */
   public Serializable invoke(Serializable[] args)
   {
      if (args != null)
         return new IllegalArgumentException("Unsupported argument list: " + Arrays.toString(args));

      System.exit(0);

      return null;
   }

   /**
    * Is it a public command
    * @return True if system-wide; false if internal
    */
   public boolean isPublic()
   {
      return true;
   }
}
