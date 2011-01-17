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
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.mdr.NotFoundException;
import org.jboss.jca.core.spi.naming.JndiStrategy;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;

import com.github.fungal.spi.deployers.Deployment;

/**
 * A resource adapter deployment for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public abstract class AbstractFungalDeployment implements Deployment
{
   /** The logger */
   protected Logger log;

   /** The deployment */
   protected URL deployment;

   /** The deployment name */
   protected String deploymentName;

   /** Activator */
   protected boolean activator;

   /** The resource adapter instance */
   protected ResourceAdapter ra;

   /** The JNDI strategy */
   protected JndiStrategy jndiStrategy;

   /** The MDR */
   protected MetadataRepository mdr;

   /** The connection factories */
   protected Object[] cfs;

   /** The JNDI names of the connection factories */
   protected String[] cfJndis;

   /** The admin objects */
   protected Object[] aos;

   /** The JNDI names of the admin objects */
   protected String[] aoJndis;

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
    * @param jndiStrategy The JNDI strategy
    * @param metadataRepository The metadata repository
    * @param cfs The connection factories
    * @param cfJndis The JNDI names of the connection factories
    * @param aos The admin objects
    * @param aoJndis The JNDI names of the admin objects
    * @param managementRepository The management repository
    * @param connector The management connector instance
    * @param server The MBeanServer
    * @param objectNames The ObjectNames
    * @param cl The classloader for the deployment
    * @param log The logger
    */
   public AbstractFungalDeployment(URL deployment, String deploymentName, boolean activator, ResourceAdapter ra,
                                   JndiStrategy jndiStrategy, MetadataRepository metadataRepository, 
                                   Object[] cfs, String[] cfJndis,
                                   Object[] aos, String[] aoJndis,
                                   ManagementRepository managementRepository, Connector connector,
                                   MBeanServer server, List<ObjectName> objectNames,
                                   ClassLoader cl, Logger log)
   {
      this.deployment = deployment;
      this.deploymentName = deploymentName;
      this.activator = activator;
      this.ra = ra;
      this.jndiStrategy = jndiStrategy;
      this.mdr = metadataRepository;
      this.cfs = cfs;
      this.cfJndis = cfJndis;
      this.aos = aos;
      this.aoJndis = aoJndis;
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

                  mdr.unregisterJndiMapping(deployment.toExternalForm(), ao, jndi);
               }
               catch (NotFoundException nfe)
               {
                  log.warn("Exception during unregistering deployment", nfe);
               }
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
