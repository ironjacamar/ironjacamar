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

import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.deployers.common.CommonDeployment;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.jboss.logging.Logger;

import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.api.util.FileUtil;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.DeployerPhases;
import com.github.fungal.spi.deployers.Deployment;

/**
 * The RA activator for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public final class RAActivator extends AbstractFungalRADeployer implements DeployerPhases
{
   /** The logger */
   static Logger log = Logger.getLogger(RAActivator.class);

   /** Trace enabled */
   static boolean trace = log.isTraceEnabled();

   /** Enabled */
   private boolean enabled;

   /** The archives that should be excluded for activation */
   private Set<String> excludeArchives;

   /** The list of generated deployments */
   private List<Deployment> deployments;

   /**
    * Constructor
    */
   public RAActivator()
   {
      super(false, log);
      enabled = true;
      excludeArchives = null;
      deployments = null;
   }

   /**
    * Get the exclude archives
    * @return The archives
    */
   public Set<String> getExcludeArchives()
   {
      return excludeArchives;
   }

   /**
    * Set the exclude archives
    * @param archives The archives
    */
   public void setExcludeArchives(Set<String> archives)
   {
      this.excludeArchives = archives;
   }

   /**
    * Is enabled
    * @return True if enabled; otherwise false
    */
   public boolean isEnabled()
   {
      return enabled;
   }

   /**
    * Set the eanbled flag
    * @param value The value
    */
   public void setEnabled(boolean value)
   {
      this.enabled = value;
   }

   /**
    * Pre deploy
    * @exception Throwable Thrown if an error occurs
    */
   @Override
   public void preDeploy() throws Throwable
   {
   }

   /**
    * Post deploy
    * @exception Throwable Thrown if an error occurs
    */
   @Override
   public void postDeploy() throws Throwable
   {
      if (enabled)
      {
         MetadataRepository mdr = ((RAConfiguration) getConfiguration()).getMetadataRepository();

         Set<String> rarDeployments = mdr.getResourceAdapters();

         for (String deployment : rarDeployments)
         {
            if (trace)
               log.trace("Processing: " + deployment);

            boolean include = true;

            if (excludeArchives != null)
            {
               for (String excludedArchive : excludeArchives)
               {
                  if (deployment.endsWith(excludedArchive))
                     include = false;
               }
            }

            if (include && !mdr.hasJndiMappings(deployment))
            {
               // If there isn't any JNDI mappings then the archive isn't active
               // so activate it
               Deployment raDeployment = deploy(new URL(deployment), kernel.getKernelClassLoader());
               if (raDeployment != null)
               {
                  if (deployments == null)
                     deployments = new ArrayList<Deployment>(1);

                  deployments.add(raDeployment);
                  
                  kernel.getMainDeployer().registerDeployment(raDeployment);
               }
            }
         }
      }
   }

   /**
    * Pre undeploy
    * @exception Throwable Thrown if an error occurs
    */
   @Override
   public void preUndeploy() throws Throwable
   {
      if (deployments != null)
      {
         for (Deployment raDeployment : deployments)
         {
            try
            {
               kernel.getMainDeployer().unregisterDeployment(raDeployment);
            }
            catch (Throwable t)
            {
               log.warn("Error during undeployment of " + raDeployment.getURL());
            }
         }

         deployments = null;
      }
   }

   /**
    * Post undeploy
    * @exception Throwable Thrown if an error occurs
    */
   @Override
   public void postUndeploy() throws Throwable
   {
   }

   /**
    * Deploy
    * @param url The url
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   private Deployment deploy(URL url, ClassLoader parent) throws DeployException
   {

      log.debug("Deploying: " + url.toExternalForm());

      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      try
      {
         File f = new File(url.toURI());

         if (!f.exists())
            return null;

         File root = null;
         File destination = null;

         if (f.isFile())
         {
            FileUtil fileUtil = new FileUtil();
            destination = new File(SecurityActions.getSystemProperty("iron.jacamar.home"), "/tmp/");
            root = fileUtil.extract(f, destination);
         }
         else
         {
            root = f;
         }
         String deploymentName = f.getName().substring(0, f.getName().indexOf(".rar"));

         // Create classloader
         URL[] urls = getUrls(root);
         KernelClassLoader cl = null;
         if (((RAConfiguration) getConfiguration()).getScopeDeployment())
         {
            cl = ClassLoaderFactory.create(ClassLoaderFactory.TYPE_PARENT_LAST, urls, parent);
         }
         else
         {
            cl = ClassLoaderFactory.create(ClassLoaderFactory.TYPE_PARENT_FIRST, urls, parent);
         }
         SecurityActions.setThreadContextClassLoader(cl);

         // Get metadata
         MetadataRepository metadataRepository = ((RAConfiguration) getConfiguration()).getMetadataRepository();

         Connector cmd = metadataRepository.getResourceAdapter(url.toExternalForm());
         IronJacamar ijmd = metadataRepository.getIronJacamar(url.toExternalForm());

         cmd = (new Merger()).mergeConnectorWithCommonIronJacamar(ijmd, cmd);

         CommonDeployment c = createObjectsAndInjectValue(url, deploymentName, root, cl, cmd, ijmd);

         List<ObjectName> ons = registerManagementView(c.getConnector(), kernel.getMBeanServer());

         JndiStrategy jndiStrategy = ((RAConfiguration) getConfiguration()).getJndiStrategy();
         ResourceAdapterRepository resourceAdapterRepository =
            ((RAConfiguration) getConfiguration()).getResourceAdapterRepository();

         return new RAActivatorDeployment(c.getURL(), c.getDeploymentName(), c.getResourceAdapter(), 
                                          c.getResourceAdapterKey(),
                                          jndiStrategy, metadataRepository, resourceAdapterRepository,
                                          c.getCfs(), c.getCfJndiNames(), 
                                          c.getAos(), c.getAoJndiNames(),
                                          c.getRecovery(), getTransactionIntegration().getRecoveryRegistry(),
                                          ((RAConfiguration)getConfiguration()).getManagementRepository(), 
                                          c.getConnector(),
                                          kernel.getMBeanServer(), ons,
                                          c.getCl(), c.getLog());
      }
      catch (DeployException de)
      {
         //just rethrow
         throw de;
      }
      catch (org.jboss.jca.deployers.common.DeployException cde)
      {
         throw new DeployException(cde.getMessage(), cde.getCause());
      }
      catch (Throwable t)
      {

         throw new DeployException("Deployment " + url.toExternalForm() + " failed", t);

      }

      finally
      {
         SecurityActions.setThreadContextClassLoader(oldTCCL);
      }
   }

   @Override
   protected boolean checkActivation(Connector cmd, IronJacamar ijmd)
   {
      return true;
   }
}
