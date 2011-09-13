/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2010, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecovery;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecoveryRegistry;
import org.jboss.jca.deployers.DeployersLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.ResourceAdapter;

import com.github.fungal.api.util.FileUtil;

/**
 * A resource adapter deployment for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RADeployment extends AbstractFungalDeployment
{
   /** The temporary directory */
   private File tmpDirectory;

   /**
    * Constructor
    * @param deployment The deployment
    * @param deploymentName The deployment name
    * @param activator Is this the activator of the deployment
    * @param ra The resource adapter instance if present
    * @param raKey The resource adapter instance key if present
    * @param jndiStrategy The JNDI strategy
    * @param mdr The metadata repository
    * @param resourceAdapterRepository The resource adapter repository
    * @param cfs The connection factories
    * @param cfJndis The JNDI names of the connection factories
    * @param cfCMs The connection managers
    * @param aos The admin objects
    * @param aoJndis The JNDI names of the admin objects
    * @param recoveryModules The recovery modules
    * @param recoveryRegistry The recovery registry
    * @param tmpDirectory The temporary directory
    * @param managementRepository The management repository
    * @param connector The management connector instance
    * @param server The MBeanServer
    * @param objectNames The ObjectNames
    * @param cl The classloader for the deployment
    * @param log The logger
    */
   public RADeployment(URL deployment, String deploymentName, boolean activator, ResourceAdapter ra,
                       String raKey,
                       JndiStrategy jndiStrategy, MetadataRepository mdr, 
                       ResourceAdapterRepository resourceAdapterRepository,
                       Object[] cfs, String[] cfJndis, ConnectionManager[] cfCMs,
                       Object[] aos, String[] aoJndis,
                       XAResourceRecovery[] recoveryModules, XAResourceRecoveryRegistry recoveryRegistry,
                       File tmpDirectory,
                       ManagementRepository managementRepository, Connector connector,
                       MBeanServer server, List<ObjectName> objectNames,
                       ClassLoader cl, DeployersLogger log)
   {
      super(deployment, deploymentName, activator, ra, raKey, jndiStrategy, mdr, resourceAdapterRepository,
            cfs, cfJndis, cfCMs, aos, aoJndis, recoveryModules, recoveryRegistry, managementRepository,
            connector, server, objectNames, cl, log);

      this.tmpDirectory = tmpDirectory;
   }

   /**
    * Stop
    */
   @Override
   public void stop()
   {
      super.stop();

      if (mdr != null)
      {
         try
         {
            mdr.unregisterResourceAdapter(deployment.toExternalForm());
         }
         catch (Throwable t)
         {
            log.warn("Exception during unregistering deployment in the metadata repository", t);
         }
      }
   }

   /**
    * Destroy
    */
   @Override
   public void destroy()
   {
      super.destroy();

      if (tmpDirectory != null && tmpDirectory.exists())
      {
         try
         {
            FileUtil fu = new FileUtil();
            fu.delete(tmpDirectory);
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
   }
}
