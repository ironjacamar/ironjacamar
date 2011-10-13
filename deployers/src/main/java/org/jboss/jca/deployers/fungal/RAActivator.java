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
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
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
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public final class RAActivator extends AbstractFungalRADeployer implements DeployerPhases
{
   /** The logger */
   private static DeployersLogger log = Logger.getMessageLogger(DeployersLogger.class, RAActivator.class.getName());

   /** Trace enabled */
   private static boolean trace = log.isTraceEnabled();

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
               log.debug("Ignoring: " + deployment);
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

      if (ra1 instanceof ResourceAdapter10 && ra2 instanceof ResourceAdapter10)
      {
         ResourceAdapter10 ra110 = (ResourceAdapter10)ra1;
         ResourceAdapter10 ra210 = (ResourceAdapter10)ra2;

         return ra110.getManagedConnectionFactoryClass().getValue().
            equals(ra210.getManagedConnectionFactoryClass().getValue());
      }
      else if (ra1 instanceof ResourceAdapter1516 && ra2 instanceof ResourceAdapter1516)
      {
         ResourceAdapter1516 ra11516 = (ResourceAdapter1516)ra1;
         ResourceAdapter1516 ra21516 = (ResourceAdapter1516)ra2;

         Set<String> clzRa1 = new HashSet<String>();
         Set<String> clzMcf1 = new HashSet<String>();
         Set<String> clzAo1 = new HashSet<String>();
         Set<String> clzAS1 = new HashSet<String>();

         if (ra11516.getResourceadapterClass() != null)
            clzRa1.add(ra11516.getResourceadapterClass());

         if (ra11516.getOutboundResourceadapter() != null)
         {
            if (ra11516.getOutboundResourceadapter().getConnectionDefinitions() != null)
            {
               for (ConnectionDefinition cd : ra11516.getOutboundResourceadapter().getConnectionDefinitions())
               {
                  clzMcf1.add(cd.getManagedConnectionFactoryClass().getValue());
               }
            }
         }

         if (ra11516.getAdminObjects() != null)
         {
            for (AdminObject ao : ra11516.getAdminObjects())
            {
               clzAo1.add(ao.getAdminobjectClass().getValue());
            }
         }

         if (ra11516.getInboundResourceadapter() != null &&
             ra11516.getInboundResourceadapter().getMessageadapter() != null &&
             ra11516.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null)
         {
            for (MessageListener ml : ra11516.getInboundResourceadapter().getMessageadapter().getMessagelisteners())
            {
               clzAS1.add(ml.getActivationspec().getActivationspecClass().getValue());
            }
         }

         Set<String> clzRa2 = new HashSet<String>();
         Set<String> clzMcf2 = new HashSet<String>();
         Set<String> clzAo2 = new HashSet<String>();
         Set<String> clzAS2 = new HashSet<String>();

         if (ra21516.getResourceadapterClass() != null)
            clzRa2.add(ra21516.getResourceadapterClass());

         if (ra21516.getOutboundResourceadapter() != null)
         {
            if (ra21516.getOutboundResourceadapter().getConnectionDefinitions() != null)
            {
               for (ConnectionDefinition cd : ra21516.getOutboundResourceadapter().getConnectionDefinitions())
               {
                  clzMcf2.add(cd.getManagedConnectionFactoryClass().getValue());
               }
            }
         }

         if (ra21516.getAdminObjects() != null)
         {
            for (AdminObject ao : ra21516.getAdminObjects())
            {
               clzAo2.add(ao.getAdminobjectClass().getValue());
            }
         }

         if (ra21516.getInboundResourceadapter() != null &&
             ra21516.getInboundResourceadapter().getMessageadapter() != null &&
             ra21516.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null)
         {
            for (MessageListener ml : ra21516.getInboundResourceadapter().getMessageadapter().getMessagelisteners())
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

      return false;
   }

   /**
    * Has a resource adapter instance deployed
    * @param rar The resource adapter repository
    * @param c The connector definition
    */
   private boolean hasResourceAdapter(ResourceAdapterRepository rar, Connector c)
   {
      if (rar != null && c != null &&
          c.getResourceadapter() != null && c.getResourceadapter() instanceof ResourceAdapter1516)
      {
         ResourceAdapter1516 ra = (ResourceAdapter1516)c.getResourceadapter();

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

         if (cmd != null)
            cmd = (Connector)cmd.copy();

         cmd = (new Merger()).mergeConnectorWithCommonIronJacamar(ijmd, cmd);

         CommonDeployment c = createObjectsAndInjectValue(url, deploymentName, root, cl, cmd, ijmd);

         List<ObjectName> ons = registerManagementView(c.getConnector(),
                                                       kernel.getMBeanServer(),
                                                       kernel.getName());

         JndiStrategy jndiStrategy = ((RAConfiguration) getConfiguration()).getJndiStrategy();
         ResourceAdapterRepository resourceAdapterRepository =
            ((RAConfiguration) getConfiguration()).getResourceAdapterRepository();

         return new RAActivatorDeployment(c.getURL(), c.getDeploymentName(), c.getResourceAdapter(), 
                                          c.getResourceAdapterKey(),
                                          jndiStrategy, metadataRepository, resourceAdapterRepository,
                                          c.getCfs(), c.getCfJndiNames(), c.getConnectionManagers(),
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
   protected boolean requireExplicitJndiBindings()
   {
      return false;
   }

   @Override
   protected boolean checkActivation(Connector cmd, IronJacamar ijmd)
   {
      if (cmd == null)
         return false;

      ResourceAdapter ra = cmd.getResourceadapter();

      if (ra == null)
         return false;

      if (ijmd != null)
      {
         if (ra instanceof ResourceAdapter10)
         {
            return true;
         }
         else
         {
            ResourceAdapter1516 ra1516 = (ResourceAdapter1516)ra;

            int mcfs = ijmd.getConnectionDefinitions() != null ? ijmd.getConnectionDefinitions().size() : 0;
            int aos = ijmd.getAdminObjects() != null ? ijmd.getAdminObjects().size() : 0;
            boolean inflow = false;

            if (mcfs == 0)
            {
               if (ra1516.getOutboundResourceadapter() != null)
               {
                  mcfs = ra1516.getOutboundResourceadapter().getConnectionDefinitions() != null ?
                     ra1516.getOutboundResourceadapter().getConnectionDefinitions().size() : 0;
               }
            }

            if (aos == 0)
            {
               aos = ra1516.getAdminObjects() != null ? ra1516.getAdminObjects().size() : 0;
            }

            if (ra1516.getInboundResourceadapter() != null &&
                ra1516.getInboundResourceadapter().getMessageadapter() != null &&
                ra1516.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null &&
                ra1516.getInboundResourceadapter().getMessageadapter().getMessagelisteners().size() > 0)
            {
               inflow = true;
            }

            return mcfs >= 1 || aos >= 1 || inflow;
         }
      }

      if (ra instanceof ResourceAdapter10)
      {
         return true;
      }
      else
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516)ra;
         int mcfs = 0;
         int aos = ra1516.getAdminObjects() != null ? ra1516.getAdminObjects().size() : 0;

         if (ra1516.getOutboundResourceadapter() != null)
         {
            mcfs = ra1516.getOutboundResourceadapter().getConnectionDefinitions() != null ?
               ra1516.getOutboundResourceadapter().getConnectionDefinitions().size() : 0;
         }

         return mcfs <= 1 && aos <= 1;
      }
   }
}
