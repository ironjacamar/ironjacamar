/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.deployers.common;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;

/**
 *
 * A CommonDeployment.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class CommonDeployment
{

   private final URL url;

   private final String deploymentName;

   private final boolean activateDeployment;

   private final ResourceAdapter resourceAdapter;

   private final Object[] cfs;

   private final File destination;

   private final ClassLoader cl;

   private final Logger log;

   private final String[] jndiNames;

   private final URL deployment;

   private final boolean activateDeployment2;

   /**
    * Create a new Deployment.
    *
    * @param url url
    * @param deploymentName deploymentName
    * @param activateDeployment activateDeployment
    * @param resourceAdapter resourceAdapter
    * @param cfs cfs
    * @param destination destination
    * @param cl cl
    * @param log log
    * @param jndiNames jndiNames
    * @param deployment deployment
    * @param activateDeployment2 activateDeployment2
    */
   public CommonDeployment(URL url, String deploymentName, boolean activateDeployment, ResourceAdapter resourceAdapter,
      Object[] cfs, File destination, ClassLoader cl, Logger log, String[] jndiNames, URL deployment,
      boolean activateDeployment2)
   {
      super();
      this.url = url;
      this.deploymentName = deploymentName;
      this.activateDeployment = activateDeployment;
      this.resourceAdapter = resourceAdapter;
      this.cfs = Arrays.copyOf(cfs, cfs.length);
      this.destination = destination;
      this.cl = cl;
      this.log = log;
      this.jndiNames = Arrays.copyOf(jndiNames, jndiNames.length);;
      this.deployment = deployment;
      this.activateDeployment2 = activateDeployment2;
   }

   /**
    * Get the url.
    *
    * @return the url.
    */
   public final URL getURL()
   {
      return url;
   }

   /**
    * Get the deploymentName.
    *
    * @return the deploymentName.
    */
   public final String getDeploymentName()
   {
      return deploymentName;
   }

   /**
    * Get the activateDeployment.
    *
    * @return the activateDeployment.
    */
   public final boolean isActivateDeployment()
   {
      return activateDeployment;
   }

   /**
    * Get the resourceAdapter.
    *
    * @return the resourceAdapter.
    */
   public final ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
   }

   /**
    * Get the cfs.
    *
    * @return the cfs.
    */
   public final Object[] getCfs()
   {
      return Arrays.copyOf(cfs, cfs.length);
   }

   /**
    * Get the destination.
    *
    * @return the destination.
    */
   public final File getDestination()
   {
      return destination;
   }

   /**
    * Get the cl.
    *
    * @return the cl.
    */
   public final ClassLoader getCl()
   {
      return cl;
   }

   /**
    * Get the log.
    *
    * @return the log.
    */
   public final Logger getLog()
   {
      return log;
   }

   /**
    * Get the jndiNames.
    *
    * @return the jndiNames.
    */
   public final String[] getJndiNames()
   {
      return Arrays.copyOf(jndiNames, jndiNames.length);
   }

   /**
    * Get the deployment.
    *
    * @return the deployment.
    */
   public final URL getDeployment()
   {
      return deployment;
   }

   /**
    * Get the activateDeployment2.
    *
    * @return the activateDeployment2.
    */
   public final boolean isActivateDeployment2()
   {
      return activateDeployment2;
   }

}
