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

package org.jboss.jca.deployers.fungal.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import com.github.fungal.api.deployer.MainDeployer;
import com.github.fungal.api.remote.Command;

/**
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RemoteDeploy implements Command
{
   private MainDeployer mainDeployer;
   private File directory;

   /**
    * Constructor
    * @param mainDeployer The main deployer
    * @param directory The directory to use
    */
   public RemoteDeploy(MainDeployer mainDeployer, File directory)
   {
      this.mainDeployer = mainDeployer;
      this.directory = directory;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return "remote-deploy";
   }

   /**
    * name, byte array of deployment
    * @return The class definitions
    */
   public Class[] getParameterTypes()
   {
      return new Class[] {String.class, byte[].class};
   }

   /**
    * {@inheritDoc}
    */
   public Serializable invoke(Serializable[] args)
   {
      try
      {
         if (args == null || args.length != 2)
            throw new IllegalArgumentException("Invalid number of arguments");

         if (!(args[0] instanceof String))
            throw new IllegalArgumentException("First parameter isn't a String");
         
         if (!(args[1] instanceof byte[]))
            throw new IllegalArgumentException("Second parameter isn't a byte[]");
         
         String name = (String)args[0];
         byte[] bytes = (byte[])args[1];
         
         File deployment = new File(directory, name);

         if (deployment.exists())
         {
            mainDeployer.undeploy(deployment.toURI().toURL());
         }

         FileOutputStream fos = null;
         try
         {
            fos = new FileOutputStream(deployment);
            fos.write(bytes);
            fos.flush();
         }
         catch (Throwable ft)
         {
            throw ft;
         }
         finally
         {
            if (fos != null)
            {
               try
               {
                  fos.close();
               }
               catch (IOException ioe)
               {
                  // Ignore
               }
            }
         }

         mainDeployer.deploy(deployment.toURI().toURL());
      }
      catch (Throwable t)
      {
         return t;
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
