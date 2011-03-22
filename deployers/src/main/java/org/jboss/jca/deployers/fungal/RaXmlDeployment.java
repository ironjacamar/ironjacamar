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

package org.jboss.jca.deployers.fungal;

import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.mdr.NotFoundException;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecovery;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecoveryRegistry;

import java.net.URL;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;

/**
 * A -ra.xml deployment for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RaXmlDeployment extends AbstractFungalDeployment
{
   /** The resource adapter deployment */
   private URL raDeployment;

   /**
    * Constructor
    * @param deployment The deployment
    * @param raDeployment The resource adapter deployment
    * @param deploymentName The deployment name
    * @param ra The resource adapter instance if present
    * @param raKey The resource adapter instance key if present
    * @param jndiStrategy The JNDI strategy
    * @param mdr The metadata repository
    * @param resourceAdapterRepository The resource adapter repository
    * @param cfs The connection factories
    * @param cfJndis The JNDI names of the connection factories
    * @param aos The admin objects
    * @param aoJndis The JNDI names of the admin objects
    * @param recoveryModules The recovery modules
    * @param recoveryRegistry The recovery registry
    * @param managementRepository The management repository
    * @param connector The management connector instance
    * @param server The MBeanServer
    * @param objectNames The ObjectNames
    * @param cl The classloader for the deployment
    * @param log The logger
    */
   public RaXmlDeployment(URL deployment, URL raDeployment, String deploymentName, ResourceAdapter ra,
                          String raKey,
                          JndiStrategy jndiStrategy, MetadataRepository mdr, 
                          ResourceAdapterRepository resourceAdapterRepository,
                          Object[] cfs, String[] cfJndis, 
                          Object[] aos, String[] aoJndis, 
                          XAResourceRecovery[] recoveryModules, XAResourceRecoveryRegistry recoveryRegistry,
                          ManagementRepository managementRepository, Connector connector,
                          MBeanServer server, List<ObjectName> objectNames,
                          ClassLoader cl, Logger log)
   {
      super(deployment, deploymentName, true, ra, raKey, jndiStrategy, mdr, resourceAdapterRepository,
            cfs, cfJndis, aos, aoJndis, recoveryModules, recoveryRegistry, managementRepository, 
            connector, server, objectNames, cl, log);

      this.raDeployment = raDeployment;
   }

   /**
    * Stop
    */
   @Override
   public void stop()
   {
      super.stop();

      log.debug("Undeploying: " + deployment.toExternalForm());

      if (mdr != null && cfs != null && cfJndis != null)
      {
         for (int i = 0; i < cfs.length; i++)
         {
            try
            {
               String cf = cfs[i].getClass().getName();
               String jndi = cfJndis[i];

               mdr.unregisterJndiMapping(raDeployment.toExternalForm(), cf, jndi);
            }
            catch (NotFoundException nfe)
            {
               log.warn("Exception during unregistering deployment", nfe);
            }
         }
      }

      if (mdr != null && aos != null && aoJndis != null)
      {
         for (int i = 0; i < aos.length; i++)
         {
            try
            {
               String ao = aos[i].getClass().getName();
               String jndi = aoJndis[i];

               mdr.unregisterJndiMapping(raDeployment.toExternalForm(), ao, jndi);
            }
            catch (NotFoundException nfe)
            {
               log.warn("Exception during unregistering deployment", nfe);
            }
         }
      }
   }
}
