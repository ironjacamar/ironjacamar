/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.deployers.fungal;

import org.jboss.jca.deployers.DeployersLogger;

import java.net.URL;
import java.util.List;

import org.jboss.logging.Logger;

import com.github.fungal.spi.deployers.Deployment;

/**
 * Datasource deployments for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DsXmlDeployments implements Deployment
{
   /** The logger */
   private static DeployersLogger log = 
      Logger.getMessageLogger(DeployersLogger.class, DsXmlDeployments.class.getName());

   /** The deployment */
   private URL deployment;

   /** The deployments */
   private List<DsXmlDeployment> deployments;

   /** The classloader */
   private ClassLoader cl;

   /**
    * Constructor
    * @param deployment The deployment
    * @param deployments The deployments
    * @param cl The classloader
    */
   public DsXmlDeployments(URL deployment,
                           List<DsXmlDeployment> deployments,
                           ClassLoader cl)
   {
      this.deployment = deployment;
      this.deployments = deployments;
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
      if (log.isDebugEnabled())
      {
         log.debug("Undeploying: " + deployment.toExternalForm());
      }

      for (DsXmlDeployment dsxml : deployments)
      {
         dsxml.stop();
      }
   }

   /**
    * Destroy
    */
   public void destroy()
   {
      for (DsXmlDeployment dsxml : deployments)
      {
         dsxml.destroy();
      }
   }
}
