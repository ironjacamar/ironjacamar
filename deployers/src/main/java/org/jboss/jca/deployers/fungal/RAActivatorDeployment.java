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

import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.mdr.NotFoundException;
import org.jboss.jca.core.spi.naming.JndiStrategy;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;

import com.github.fungal.spi.deployers.Deployment;

/**
 * A resource adapter activator deployment for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RAActivatorDeployment implements Deployment
{
   /** The logger */
   private Logger log;

   /** The deployment */
   private URL deployment;

   /** The deployment name */
   private String deploymentName;

   /** The resource adapter instance */
   private ResourceAdapter ra;

   /** The JNDI strategy */
   private JndiStrategy jndiStrategy;

   /** The MDR */
   private MetadataRepository mdr;

   /** The connection factories */
   private Object[] cfs;

   /** The JNDI names for the connection factories */
   private String[] cfJndis;

   /** The admin objects */
   private Object[] aos;

   /** The JNDI names for the admin objects */
   private String[] aoJndis;

   /** The classloader */
   private ClassLoader cl;

   /**
    * Constructor
    * @param deployment The deployment
    * @param deploymentName The deployment name
    * @param ra The resource adapter instance if present
    * @param jndiStrategy The JNDI strategy
    * @param metadataRepository The metadata repository
    * @param cfs The connection factories
    * @param cfJndis The JNDI names for the connection factories
    * @param aos The admin objects
    * @param aoJndis The JNDI names for the admin objects
    * @param cl The classloader for the deployment
    * @param log The logger
    */
   public RAActivatorDeployment(URL deployment, String deploymentName, ResourceAdapter ra, JndiStrategy jndiStrategy,
                                MetadataRepository metadataRepository, 
                                Object[] cfs, String[] cfJndis, 
                                Object[] aos, String[] aoJndis, 
                                ClassLoader cl, Logger log)
   {
      this.deployment = deployment;
      this.deploymentName = deploymentName;
      this.ra = ra;
      this.jndiStrategy = jndiStrategy;
      this.mdr = metadataRepository;
      this.cfs = cfs;
      this.cfJndis = cfJndis;
      this.aos = aos;
      this.aoJndis = aoJndis;
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
      log.debug("Undeploying: " + deployment.toExternalForm());

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

      if (cfs != null)
      {
         try
         {
            jndiStrategy.unbindConnectionFactories(deploymentName, cfs);
         }
         catch (Throwable t)
         {
            log.warn("Exception during JNDI unbinding", t);
         }
      }

      if (aos != null)
      {
         try
         {
            jndiStrategy.unbindAdminObjects(deploymentName, aos);
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

      log.info("Undeployed: " + deployment.toExternalForm());
   }
}
