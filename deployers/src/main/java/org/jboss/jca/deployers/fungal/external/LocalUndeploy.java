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

import org.jboss.jca.deployers.fungal.RAActivator;

import java.io.Serializable;
import java.net.URL;

import com.github.fungal.api.deployer.MainDeployer;
import com.github.fungal.api.remote.Command;

/**
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class LocalUndeploy implements Command
{
   private MainDeployer mainDeployer;
   private RAActivator activator;
   private LocalList localList;

   /**
    * Constructor
    * @param mainDeployer The main deployer
    * @param activator The RA activator
    * @param localList The local list command
    */
   public LocalUndeploy(MainDeployer mainDeployer, RAActivator activator, LocalList localList)
   {
      this.mainDeployer = mainDeployer;
      this.activator = activator;
      this.localList = localList;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return "local-undeploy";
   }

   /**
    * name
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

         activator.setEnabled(false);
         
         URL deployment = (URL)args[0];
         localList.removeDeployment(deployment);
         mainDeployer.undeploy(deployment);
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
