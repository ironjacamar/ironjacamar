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

package org.jboss.jca.fungal.impl;

import org.jboss.jca.fungal.deployers.Deployer;
import org.jboss.jca.fungal.deployers.Deployment;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The main deployer for JBoss JCA/Fungal
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class MainDeployer implements MainDeployerMBean
{
   private KernelImpl kernel;
   private List<Deployer> deployers;

   /**
    * Constructor
    * @param kernel The kernel
    */
   public MainDeployer(KernelImpl kernel)
   {
      if (kernel == null)
         throw new IllegalArgumentException("Kernel is null");

      this.kernel = kernel;
      this.deployers = new ArrayList<Deployer>();
   }

   /**
    * Add deployer
    * @param deployer The deployer
    */
   public void addDeployer(Deployer deployer)
   {
      if (deployer == null)
         throw new IllegalArgumentException("Deployer is null");

      deployers.add(deployer);
   }

   /**
    * Deploy uses the kernel class loader as the parent class loader
    * @param url The URL for the deployment
    * @exception Throwable If an error occurs
    */
   public synchronized void deploy(URL url) throws Throwable
   {
      deploy(url, kernel.getKernelClassLoader());
   }

   /**
    * Deploy
    * @param url The URL for the deployment
    * @param classLoader The parent class loader for the deployment
    * @exception Throwable If an error occurs
    */
   public synchronized void deploy(URL url, ClassLoader classLoader) throws Throwable
   {
      if (url == null)
         throw new IllegalArgumentException("URL is null");

      if (classLoader == null)
         throw new IllegalArgumentException("ClassLoader is null");

      boolean done = false;

      for (int i = 0; !done && i < deployers.size(); i++)
      {
         Deployer deployer = deployers.get(i);
            
         Deployment deployment = deployer.deploy(url, classLoader);
         if (deployment != null)
         {
            kernel.registerDeployment(deployment);
            done = true;
         }
      }
   }

   /**
    * Undeploy
    * @param url The URL for the deployment
    * @exception Throwable If an error occurs
    */
   public synchronized void undeploy(URL url) throws Throwable
   {
      if (url == null)
         throw new IllegalArgumentException("URL is null");

      Deployment deployment = kernel.findDeployment(url);
      if (deployment != null)
         kernel.shutdownDeployment(deployment);
   }
}
