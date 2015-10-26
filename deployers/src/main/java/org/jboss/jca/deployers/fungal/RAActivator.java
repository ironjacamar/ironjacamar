/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2010, Red Hat Inc, and individual contributors
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

import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.spec.AdminObject;
import org.jboss.jca.common.api.metadata.spec.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.MessageListener;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.deployers.DeployersLogger;
import org.jboss.jca.deployers.common.CommonDeployment;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public final class RAActivator extends AbstractFungalRADeployer implements DeployerPhases
{
   /** The logger */
   private static DeployersLogger log = Logger.getMessageLogger(DeployersLogger.class, RAActivator.class.getName());

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
      super(false);
      enabled = true;
      excludeArchives = null;
      deployments = null;
   }

   /**
    * {@inheritDoc}
    */
   protected DeployersLogger getLogger()
   {
      return log;
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
         ResourceAdapterRepository rar = ((RAConfiguration) getConfiguration()).getResourceAdapterRepository();

         Set<String> rarDeployments = mdr.getResourceAdapters();
         Set<String> configuredRars = getConfiguredResourceAdapters(mdr, rar);

         for (String deployment : rarDeployments)
         {
            log.tracef("Processing: %s", deployment);

            boolean include = true;

            if (excludeArchives != null)
            {
               for (String excludedArchive : excludeArchives)
               {
                  if (deployment.endsWith(excludedArchive))
                     include = false;
               }
            }

            if (include && !configuredRars.contains(deployment))
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
    * Get the set of configured resource adapters
    * @param mdr The metadata repository
    * @param rar The resource adapter repository
    * @return The resource adapters
    */
   private Set<String> getConfiguredResourceAdapters(MetadataRepository mdr, ResourceAdapterRepository rar)
   {
      Set<String> configured = new HashSet<String>();

      SortedSet<String> deployments = new TreeSet<String>(new RAActivatorComparator());
      for (String entry : mdr.getResourceAdapters())
      {
         deployments.add(entry);
      }

      for (String deployment : deployments)
      {
         if (deployment.endsWith(".rar"))
         {
            try
            {
               if (mdr.hasJndiMappings(deployment) || hasResourceAdapter(rar, mdr.getResourceAdapter(deployment)))
                  configured.add(deployment);
            }
            catch (Throwable t)
            {
               // Ignore
            }
         }
         else if (deployment.endsWith("-ra.xml"))
         {
            configured.add(deployment);
            try
            {
               Connector raXml = mdr.getResourceAdapter(deployment);

               for (String entry : mdr.getResourceAdapters())
               {
                  if (entry.endsWith(".rar"))
                  {
                     Connector entryXml = mdr.getResourceAdapter(entry);

                     if (sameStructure(raXml, entryXml))
                        configured.add(entry);
                  }
               }
            }
            catch (Throwable t)
            {
               log.debugf(t, "Ignoring: %s", deployment);
            }
         }
      }

      return configured;
   }

   /**
    * Check if two connector defines the same structures
    * @param c1 The first connector
    * @param c2 The second connector
    * @return True if same structure; otherwise false
    */
   private boolean sameStructure(Connector c1, Connector c2)
   {
      if (c1 == null || c1.getResourceadapter() == null)
         return false;

      if (c2 == null || c2.getResourceadapter() == null)
         return false;

      ResourceAdapter ra1 = c1.getResourceadapter();
      ResourceAdapter ra2 = c2.getResourceadapter();


      Set<String> clzRa1 = new HashSet<String>();
      Set<String> clzMcf1 = new HashSet<String>();
      Set<String> clzAo1 = new HashSet<String>();
      Set<String> clzAS1 = new HashSet<String>();

      if (ra1.getResourceadapterClass() != null)
         clzRa1.add(ra1.getResourceadapterClass());

      if (ra1.getOutboundResourceadapter() != null)
      {
         if (ra1.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            for (ConnectionDefinition cd : ra1.getOutboundResourceadapter().getConnectionDefinitions())
            {
               clzMcf1.add(cd.getManagedConnectionFactoryClass().getValue());
            }
         }
      }

      if (ra1.getAdminObjects() != null)
      {
         for (AdminObject ao : ra1.getAdminObjects())
         {
            clzAo1.add(ao.getAdminobjectClass().getValue());
         }
      }

      if (ra1.getInboundResourceadapter() != null &&
          ra1.getInboundResourceadapter().getMessageadapter() != null &&
          ra1.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null)
      {
         for (MessageListener ml : ra1.getInboundResourceadapter().getMessageadapter().getMessagelisteners())
         {
            clzAS1.add(ml.getActivationspec().getActivationspecClass().getValue());
         }
      }

      Set<String> clzRa2 = new HashSet<String>();
      Set<String> clzMcf2 = new HashSet<String>();
      Set<String> clzAo2 = new HashSet<String>();
      Set<String> clzAS2 = new HashSet<String>();

      if (ra2.getResourceadapterClass() != null)
         clzRa2.add(ra2.getResourceadapterClass());

      if (ra2.getOutboundResourceadapter() != null)
      {
         if (ra2.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            for (ConnectionDefinition cd : ra2.getOutboundResourceadapter().getConnectionDefinitions())
            {
               clzMcf2.add(cd.getManagedConnectionFactoryClass().getValue());
            }
         }
      }

      if (ra2.getAdminObjects() != null)
      {
         for (AdminObject ao : ra2.getAdminObjects())
         {
            clzAo2.add(ao.getAdminobjectClass().getValue());
         }
      }

      if (ra2.getInboundResourceadapter() != null &&
          ra2.getInboundResourceadapter().getMessageadapter() != null &&
          ra2.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null)
      {
         for (MessageListener ml : ra2.getInboundResourceadapter().getMessageadapter().getMessagelisteners())
         {
            clzAS2.add(ml.getActivationspec().getActivationspecClass().getValue());
         }
      }

      if (clzRa1.size() != clzRa2.size())
         return false;

      if (clzMcf1.size() != clzMcf2.size())
         return false;

      if (clzAo1.size() != clzAo2.size())
         return false;

      if (clzAS1.size() != clzAS2.size())
         return false;

      for (String s : clzRa1)
      {
         if (!clzRa2.contains(s))
            return false;
      }

      for (String s : clzMcf1)
      {
         if (!clzMcf2.contains(s))
            return false;
      }

      for (String s : clzAo1)
      {
         if (!clzAo2.contains(s))
            return false;
      }

      for (String s : clzAS1)
      {
         if (!clzAS2.contains(s))
            return false;
      }

      return true;
   }

   /**
    * Has a resource adapter instance deployed
    * @param rar The resource adapter repository
    * @param c The connector definition
    */
   private boolean hasResourceAdapter(ResourceAdapterRepository rar, Connector c)
   {
      if (rar != null && c != null && c.getResourceadapter() != null)
      {
         ResourceAdapter ra = c.getResourceadapter();
         
         if (ra.getResourceadapterClass() != null)
         {
            String clz = ra.getResourceadapterClass();

            for (String deployment : rar.getResourceAdapters())
            {
               try
               {
                  javax.resource.spi.ResourceAdapter instance = rar.getResourceAdapter(deployment);
                  if (clz.equals(instance.getClass().getName()))
                     return true;
               }
               catch (Throwable t)
               {
                  // Ignore deployment
               }
            }
         }
      }

      return false;
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

      if (log.isDebugEnabled())
      {
         log.debug("Deploying: " + url.toExternalForm());
      }
      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      try
      {
         File f = new File(url.toURI());

         if (!f.exists())
            return null;

         File root = null;

         if (f.isFile())
         {
            File destination = new File(SecurityActions.getSystemProperty("iron.jacamar.home"), "/tmp/");
            File target = new File(destination, f.getName());
            
            if (!target.exists())
            {
               FileUtil fileUtil = new FileUtil();
               root = fileUtil.extract(f, destination);
            }
            else
            {
               root = target;
            }
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
         Activation activation = metadataRepository.getActivation(url.toExternalForm());

         if (cmd != null)
            cmd = (Connector)cmd.copy();

         cmd = (new Merger()).mergeConnectorWithCommonIronJacamar(activation, cmd);

         CommonDeployment c = createObjectsAndInjectValue(url, deploymentName, root, cl, cmd, activation);

         List<ObjectName> ons = registerManagementView(c.getConnector(),
                                                       kernel.getMBeanServer(),
                                                       kernel.getName());

         JndiStrategy jndiStrategy = ((RAConfiguration) getConfiguration()).getJndiStrategy();
         ResourceAdapterRepository resourceAdapterRepository =
            ((RAConfiguration) getConfiguration()).getResourceAdapterRepository();

         return new RAActivatorDeployment(c.getURL(), c.getDeploymentName(), c.getResourceAdapter(), 
                                          c.getResourceAdapterKey(), c.getBootstrapContextIdentifier(),
                                          jndiStrategy, metadataRepository, resourceAdapterRepository,
                                          c.getCfs(), c.getCfJndiNames(), c.getConnectionManagers(),
                                          c.getAos(), c.getAoJndiNames(),
                                          c.getRecovery(), getTransactionIntegration().getRecoveryRegistry(),
                                          ((RAConfiguration)getConfiguration()).getManagementRepository(), 
                                          c.getConnector(),
                                          kernel.getMBeanServer(), ons,
                                          cl, c.getLog());
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
   protected boolean requireExplicitJndiBindings()
   {
      return false;
   }

   @Override
   protected boolean checkActivation(Connector cmd, Activation activation)
   {
      if (cmd == null)
         return false;

      ResourceAdapter ra = cmd.getResourceadapter();

      if (ra == null)
         return false;

      if (activation != null)
      {
         int mcfs = activation.getConnectionDefinitions() != null ? activation.getConnectionDefinitions().size() : 0;
         int aos = activation.getAdminObjects() != null ? activation.getAdminObjects().size() : 0;
         boolean inflow = false;

         if (mcfs == 0)
         {
            if (ra.getOutboundResourceadapter() != null)
            {
               mcfs = ra.getOutboundResourceadapter().getConnectionDefinitions() != null ?
                  ra.getOutboundResourceadapter().getConnectionDefinitions().size() : 0;
            }
         }

         if (aos == 0)
         {
            aos = ra.getAdminObjects() != null ? ra.getAdminObjects().size() : 0;
         }

         if (ra.getInboundResourceadapter() != null &&
             ra.getInboundResourceadapter().getMessageadapter() != null &&
             ra.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null &&
             ra.getInboundResourceadapter().getMessageadapter().getMessagelisteners().size() > 0)
         {
            inflow = true;
         }

         return mcfs >= 1 || aos >= 1 || inflow;
      }

      int mcfs = 0;
      int aos = ra.getAdminObjects() != null ? ra.getAdminObjects().size() : 0;

      if (ra.getOutboundResourceadapter() != null)
      {
         mcfs = ra.getOutboundResourceadapter().getConnectionDefinitions() != null ?
            ra.getOutboundResourceadapter().getConnectionDefinitions().size() : 0;
      }

      return mcfs <= 1 && aos <= 1;
   }
}
