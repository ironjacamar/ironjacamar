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

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.ResourceAdapter;

import com.github.fungal.spi.deployers.Deployment;

/**
 * A resource adapter deployment for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractFungalDeployment implements Deployment
{
   /** The logger */
   protected DeployersLogger log;

   /** The deployment */
   protected URL deployment;

   /** The deployment name */
   protected String deploymentName;

   /** Activator */
   protected boolean activator;

   /** The resource adapter instance */
   protected ResourceAdapter ra;

   /** The resource adapter instance key */
   protected String raKey;

   /** The JNDI strategy */
   protected JndiStrategy jndiStrategy;

   /** The MDR */
   protected MetadataRepository mdr;

   /** The resource adapter repository */
   protected ResourceAdapterRepository rar;

   /** The connection factories */
   protected Object[] cfs;

   /** The JNDI names of the connection factories */
   protected String[] cfJndis;

   /** The connection managers */
   protected ConnectionManager[] cfCMs;

   /** The admin objects */
   protected Object[] aos;

   /** The JNDI names of the admin objects */
   protected String[] aoJndis;

   /** The recovery modules */
   protected XAResourceRecovery[] recoveryModules;

   /** The recovery registry */
   protected XAResourceRecoveryRegistry recoveryRegistry;

   /** The management repository */
   protected ManagementRepository managementRepository;

   /** The management connector */
   protected Connector connector;

   /** The MBeanServer */
   protected MBeanServer server;

   /** The ObjectName's */
   protected List<ObjectName> objectNames;

   /** The classloader */
   protected ClassLoader cl;

   /**
    * Constructor
    * @param deployment The deployment
    * @param deploymentName The deployment name
    * @param activator Is this the activator of the deployment
    * @param ra The resource adapter instance if present
    * @param raKey The resource adapter instance key if present
    * @param jndiStrategy The JNDI strategy
    * @param metadataRepository The metadata repository
    * @param resourceAdapterRepository The resource adapter repository
    * @param cfs The connection factories
    * @param cfJndis The JNDI names of the connection factories
    * @param cfCMs The connection managers
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
   public AbstractFungalDeployment(URL deployment, String deploymentName, boolean activator, ResourceAdapter ra,
                                   String raKey,
                                   JndiStrategy jndiStrategy, MetadataRepository metadataRepository, 
                                   ResourceAdapterRepository resourceAdapterRepository,
                                   Object[] cfs, String[] cfJndis, ConnectionManager[] cfCMs,
                                   Object[] aos, String[] aoJndis,
                                   XAResourceRecovery[] recoveryModules, XAResourceRecoveryRegistry recoveryRegistry,
                                   ManagementRepository managementRepository, Connector connector,
                                   MBeanServer server, List<ObjectName> objectNames,
                                   ClassLoader cl, DeployersLogger log)
   {
      this.deployment = deployment;
      this.deploymentName = deploymentName;
      this.activator = activator;
      this.ra = ra;
      this.raKey = raKey;
      this.jndiStrategy = jndiStrategy;
      this.mdr = metadataRepository;
      this.rar = resourceAdapterRepository;
      this.cfs = cfs;
      this.cfJndis = cfJndis;
      this.cfCMs = cfCMs;
      this.aos = aos;
      this.aoJndis = aoJndis;
      this.recoveryModules = recoveryModules;
      this.recoveryRegistry = recoveryRegistry;
      this.managementRepository = managementRepository;
      this.connector = connector;
      this.server = server;
      this.objectNames = objectNames;
      this.cl = cl;
      this.log = log;
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
      if (activator)
      {
         log.debug("Undeploying: " + deployment.toExternalForm());
         
         if (server != null && objectNames != null)
         {
            for (ObjectName on : objectNames)
            {
               try
               {
                  server.unregisterMBean(on);
               }
               catch (Throwable t)
               {
                  log.warn("Exception during unregistering deployment", t);
               }
            }
         }

         if (recoveryRegistry != null && recoveryModules != null)
         {
            for (XAResourceRecovery recovery : recoveryModules)
            {
               recoveryRegistry.removeXAResourceRecovery(recovery);
            }
         }

         if (managementRepository != null && connector != null)
            managementRepository.getConnectors().remove(connector);

         if (mdr != null && cfs != null && cfJndis != null)
         {
            for (int i = 0; i < cfs.length; i++)
            {
               try
               {
                  String cf = cfs[i].getClass().getName();
                  String jndi = cfJndis[i];

                  mdr.unregisterJndiMapping(deployment.toExternalForm(), cf, jndi);
               }
               catch (org.jboss.jca.core.spi.mdr.NotFoundException nfe)
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

                  mdr.unregisterJndiMapping(deployment.toExternalForm(), ao, jndi);
               }
               catch (org.jboss.jca.core.spi.mdr.NotFoundException nfe)
               {
                  log.warn("Exception during unregistering deployment", nfe);
               }
            }
         }

         if (cfCMs != null)
         {
            for (ConnectionManager cm : cfCMs)
            {
               cm.shutdown();
            }
         }

         if (cfs != null && cfJndis != null)
         {
            try
            {
               jndiStrategy.unbindConnectionFactories(deploymentName, cfs, cfJndis);

               for (String jndi : cfJndis)
               {
                  log.infof("Unbound connection factory at: %s", jndi);
               }
            }
            catch (Throwable t)
            {
               log.warn("Exception during JNDI unbinding", t);
            }
         }

         if (aos != null && aoJndis != null)
         {
            try
            {
               jndiStrategy.unbindAdminObjects(deploymentName, aos, aoJndis);

               for (String jndi : aoJndis)
               {
                  log.infof("Unbound admin object at: %s", jndi);
               }
            }
            catch (Throwable t)
            {
               log.warn("Exception during JNDI unbinding", t);
            }
         }

         if (raKey != null && rar != null)
         {
            try
            {
               rar.unregisterResourceAdapter(raKey);
            }
            catch (org.jboss.jca.core.spi.rar.NotFoundException nfe)
            {
               log.warn("Exception during unregistering deployment", nfe);
            }
         }

         if (ra != null)
         {
            ra.stop();
            ra = null;
         }
      }
   }

   /**
    * Destroy
    */
   public void destroy()
   {
      if (cl != null && cl instanceof Closeable)
      {
         try
         {
            ((Closeable) cl).close();
         }
         catch (IOException ioe)
         {
            // Swallow
         }
      }

      if (activator)
      {
         log.info("Undeployed: " + deployment.toExternalForm());
      }
   }
}
