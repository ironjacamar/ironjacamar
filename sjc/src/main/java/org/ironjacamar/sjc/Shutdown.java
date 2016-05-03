/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.sjc;

import java.io.Serializable;
import java.util.Arrays;

import com.github.fungal.api.remote.Command;

/**
 * Shutdown command
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
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
