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

import java.io.Serializable;
import java.net.URL;
import java.util.List;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.deployer.MainDeployer;
import com.github.fungal.api.remote.Command;
import com.github.fungal.spi.deployers.Deployment;

/**
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LocalDeploy implements Command
{
   private Kernel kernel;
   private MainDeployer mainDeployer;
   private RAActivator activator;
   private LocalList localList;

   /**
    * Constructor
    * @param kernel The kernel
    * @param mainDeployer The main deployer
    * @param activator The RA activator
    * @param localList The local list command
    */
   public LocalDeploy(Kernel kernel, MainDeployer mainDeployer, RAActivator activator, LocalList localList)
   {
      this.kernel = kernel;
      this.mainDeployer = mainDeployer;
      this.activator = activator;
      this.localList = localList;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return "local-deploy";
   }

   /**
    * name, byte array of deployment
    * @return The class definitions
    */
   public Class[] getParameterTypes()
   {
      return new Class[] {URL.class};
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

         if (!(args[0] instanceof URL))
            throw new IllegalArgumentException("First parameter isn't an URL");
         
         URL deployment = (URL)args[0];

         activator.setEnabled(false);

         List<Deployment> l = kernel.getDeployments(deployment);
         if (l != null && l.size() > 0)
         {
            localList.removeDeployment(deployment);
            mainDeployer.undeploy(deployment);
         }

         mainDeployer.deploy(deployment);
         localList.addDeployment(deployment);
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
