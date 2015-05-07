/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.deployers.fungal;

import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;

import java.net.URL;
import java.util.List;

import com.github.fungal.spi.deployers.Deployment;

/**
 * Activation deployment
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ActivationDeployment implements Deployment
{
   /** The deployment */
   private URL deployment;

   /** The deployments */
   private List<org.ironjacamar.core.api.deploymentrepository.Deployment> deployments;

   /** The deployment repository */
   private DeploymentRepository deploymentRepository;

   /** The classloader */
   private ClassLoader cl;

   /**
    * Constructor
    * @param deployment The deployment
    * @param deployments The deployments
    * @param deploymentRepository The deployment repository
    * @param cl The classloader
    */
   public ActivationDeployment(URL deployment,
                               List<org.ironjacamar.core.api.deploymentrepository.Deployment> deployments,
                               DeploymentRepository deploymentRepository,
                               ClassLoader cl)
   {
      this.deployment = deployment;
      this.deployments = deployments;
      this.deploymentRepository = deploymentRepository;
      this.cl = cl;
   }

   /**
    * Get the unique URL for the deployment
    * @return The URL
    */
   @Override
   public URL getURL()
   {
      return deployment;
   }

   /**
    * Get the classloader
    * @return The classloader
    */
   @Override
   public ClassLoader getClassLoader()
   {
      return cl;
   }

   /**
    * Stop
    */
   public void stop()
   {
      for (org.ironjacamar.core.api.deploymentrepository.Deployment d : deployments)
      {
         // d.deactivate();
         deploymentRepository.unregisterDeployment(d);
      }
   }
}
