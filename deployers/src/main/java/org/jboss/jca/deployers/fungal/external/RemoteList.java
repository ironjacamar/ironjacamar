/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.deployers.fungal.external;

import java.io.File;
import java.io.Serializable;

import com.github.fungal.api.remote.Command;

/**
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RemoteList implements Command
{
   private File directory;

   /**
    * Constructor
    * @param directory The directory to use
    */
   public RemoteList(File directory)
   {
      this.directory = directory;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return "remote-list";
   }

   /**
    * {@inheritDoc}
    */
   public Class[] getParameterTypes()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public Serializable invoke(Serializable[] args)
   {
      try
      {
         if (args != null)
            throw new IllegalArgumentException("Invalid number of arguments");

         return directory.list();
      }
      catch (Throwable t)
      {
         return t;
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isPublic()
   {
      return true;
   }
}
