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

package org.jboss.jca.deployers.fungal.external;

import org.jboss.jca.deployers.fungal.RAActivator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

import com.github.fungal.api.deployer.MainDeployer;
import com.github.fungal.api.remote.Command;

/**
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class RemoteUndeploy implements Command
{
   private MainDeployer mainDeployer;
   private RAActivator activator;
   private File directory;

   /**
    * Constructor
    * @param mainDeployer The main deployer
    * @param activator The RA activator
    * @param directory The directory to use
    */
   public RemoteUndeploy(MainDeployer mainDeployer, RAActivator activator, File directory)
   {
      this.mainDeployer = mainDeployer;
      this.activator = activator;
      this.directory = directory;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return "remote-undeploy";
   }

   /**
    * name
    * @return The class definitions
    */
   public Class[] getParameterTypes()
   {
      return new Class[] {String.class};
   }

   /**
    * {@inheritDoc}
    */
   public Serializable invoke(Serializable[] args)
   {
      boolean activatorEnabled = activator.isEnabled();
      try
      {
         if (args == null || args.length != 1)
            throw new IllegalArgumentException("Invalid number of arguments");

         if (!(args[0] instanceof String))
            throw new IllegalArgumentException("First parameter isn't a String");
         
         String name = (String)args[0];
         
         File deployment = new File(directory, name);

         activator.setEnabled(false);

         if (deployment.exists())
         {
            mainDeployer.undeploy(deployment.toURI().toURL());

            Files.delete(deployment.toPath());
         }
      }
      catch (Throwable t)
      {
         return t;
      }
      finally
      {
         activator.setEnabled(activatorEnabled);
      }

      return Boolean.TRUE;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isPublic()
   {
      return true;
   }
}
