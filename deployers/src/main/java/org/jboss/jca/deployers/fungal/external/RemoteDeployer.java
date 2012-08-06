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

import org.jboss.jca.deployers.DeployersLogger;
import org.jboss.jca.deployers.fungal.RAActivator;

import java.io.File;
import java.io.IOException;

import org.jboss.logging.Logger;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.deployer.MainDeployer;
import com.github.fungal.api.remote.Communicator;

/**
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RemoteDeployer
{
   /** The logger */
   private static DeployersLogger log = Logger.getMessageLogger(DeployersLogger.class, RemoteDeployer.class.getName());

   private Kernel kernel;
   private Communicator communicator;
   private String directory;

   private RemoteDeploy remoteDeploy;
   private RemoteUndeploy remoteUndeploy;
   private RemoteList remoteList;

   /**
    * Constructor
    */
   public RemoteDeployer()
   {
      this.kernel = null;
      this.communicator = null;
      this.directory = null;

      this.remoteDeploy = null;
      this.remoteUndeploy = null;
      this.remoteList = null;
   }

   /**
    * Set the kernel
    * @param v The value
    */
   public void setKernel(Kernel v)
   {
      kernel = v;
   }

   /**
    * Set the remote directory
    * @param v The value
    */
   public void setDirectory(String v)
   {
      directory = v;
   }

   /**
    * Start
    * @exception Throwable Thrown in case of an error
    */
   public void start() throws Throwable
   {
      communicator = kernel.getBean("Communicator", Communicator.class);

      if (directory == null || directory.trim().equals(""))
         throw new IllegalStateException("Directory must be defined");

      File rd = new File(directory);
      if (!rd.exists())
      {
         if (!rd.mkdirs())
            throw new IOException("Directory couldn't be created");
      }

      MainDeployer mainDeployer = kernel.getMainDeployer();
      RAActivator activator = kernel.getBean("RAActivator", RAActivator.class);

      remoteDeploy = new RemoteDeploy(mainDeployer, activator, rd);
      remoteUndeploy = new RemoteUndeploy(mainDeployer, activator, rd);
      remoteList = new RemoteList(rd);

      communicator.registerCommand(remoteDeploy);
      communicator.registerCommand(remoteUndeploy);
      communicator.registerCommand(remoteList);
   }

   /**
    * Stop
    * @exception Throwable Thrown in case of an error
    */
   public void stop() throws Throwable
   {
      if (communicator != null)
      {
         if (remoteList != null)
            communicator.unregisterCommand(remoteList);

         if (remoteUndeploy != null)
            communicator.unregisterCommand(remoteUndeploy);

         if (remoteDeploy != null)
            communicator.unregisterCommand(remoteDeploy);
      }

      // Clean up directory
      File rd = new File(directory);
      if (rd.exists())
      {
         File[] files = rd.listFiles();
         if (files != null)
         {
            MainDeployer mainDeployer = kernel.getMainDeployer();

            for (File deployment : files)
            {
               try
               {
                  mainDeployer.undeploy(deployment.toURI().toURL());
               }
               catch (Throwable t)
               {
                  log.warn("Exception during undeploy: " + t.getMessage(), t);
               }
            }
         }
      }
   }
}
