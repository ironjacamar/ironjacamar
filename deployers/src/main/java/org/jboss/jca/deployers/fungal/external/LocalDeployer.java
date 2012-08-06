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

import org.jboss.logging.Logger;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.deployer.MainDeployer;
import com.github.fungal.api.remote.Communicator;

/**
 * A deployer for local machine deployments
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class LocalDeployer
{
   /** The logger */
   private static DeployersLogger log = Logger.getMessageLogger(DeployersLogger.class, LocalDeployer.class.getName());

   private Kernel kernel;
   private Communicator communicator;

   private LocalDeploy localDeploy;
   private LocalUndeploy localUndeploy;
   private LocalList localList;

   /**
    * Constructor
    */
   public LocalDeployer()
   {
      this.kernel = null;
      this.communicator = null;

      this.localDeploy = null;
      this.localUndeploy = null;
      this.localList = null;
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
    * Start
    * @exception Throwable Thrown in case of an error
    */
   public void start() throws Throwable
   {
      communicator = kernel.getBean("Communicator", Communicator.class);

      MainDeployer mainDeployer = kernel.getMainDeployer();
      RAActivator activator = kernel.getBean("RAActivator", RAActivator.class);

      localList = new LocalList();
      localDeploy = new LocalDeploy(kernel, mainDeployer, activator, localList);
      localUndeploy = new LocalUndeploy(mainDeployer, activator, localList);

      communicator.registerCommand(localList);
      communicator.registerCommand(localDeploy);
      communicator.registerCommand(localUndeploy);
   }

   /**
    * Stop
    * @exception Throwable Thrown in case of an error
    */
   public void stop() throws Throwable
   {
      if (communicator != null)
      {
         if (localUndeploy != null)
            communicator.unregisterCommand(localUndeploy);

         if (localDeploy != null)
            communicator.unregisterCommand(localDeploy);

         if (localList != null)
            communicator.unregisterCommand(localList);
      }
   }
}
