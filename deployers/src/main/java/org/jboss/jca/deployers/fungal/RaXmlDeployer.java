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
import org.jboss.jca.common.api.metadata.resourceadapter.Activations;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.common.metadata.resourceadapter.ResourceAdapterParser;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.deployers.DeployersLogger;
import org.jboss.jca.deployers.common.CommonDeployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.jboss.logging.Logger;

import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.DeployerPhases;
import com.github.fungal.spi.deployers.Deployment;

/**
 * The -ra.xml deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class RaXmlDeployer extends AbstractFungalRADeployer
   implements
      Deployer,
      DeployerPhases
{
   private static DeployersLogger log = Logger.getMessageLogger(DeployersLogger.class, RaXmlDeployer.class.getName());

   /** The list of generated deployments */
   private List<Deployment> deployments;

   /**
    * Constructor
    */
   public RaXmlDeployer()
   {
      super(false);
   }

   /**
    * {@inheritDoc}
    */
   protected DeployersLogger getLogger()
   {
      return log;
   }

   /**
    * {@inheritDoc}
    */
   public boolean accepts(URL url)
   {
      if (url == null || url.toExternalForm().startsWith("jar") || !url.toExternalForm().endsWith("-ra.xml"))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public int getOrder()
   {
      return Constants.RAXML_DEPLOYER;
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
    * {@inheritDoc}
    */
   @Override
   public synchronized Deployment deploy(URL url, Context context, ClassLoader parent) throws DeployException
   {
      if (log.isDebugEnabled())
      {
         log.debug("Deploying: " + url.toExternalForm());
      }
      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      InputStream is = null;
      try
      {
         File f = getFileFromUrl(url);

         if (!f.exists())
            throw new IOException("Archive " + url.toExternalForm() + " doesnt exists");

         // Parse metadata
         is = new FileInputStream(f);
         ResourceAdapterParser parser = new ResourceAdapterParser();
         Activations raXmlDeployment = parser.parse(is);

         int size = raXmlDeployment.getActivations().size();
         if (size == 1)
         {
            return doDeploy(url, raXmlDeployment.getActivations().get(0), parent);
         }
         else
         {
            deployments = new ArrayList<Deployment>(size);

            for (org.jboss.jca.common.api.metadata.resourceadapter.Activation activation : raXmlDeployment
               .getActivations())
            {
               Deployment raDeployment = doDeploy(url, activation, parent);
               deployments.add(raDeployment);
               kernel.getMainDeployer().registerDeployment(raDeployment);
            }

            return null;
         }
      }
      catch (DeployException de)
      {
         // Just rethrow
         throw de;
      }
      catch (Throwable t)
      {
         throw new DeployException("Exception during deployment of " + url.toExternalForm(), t);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }
         
         cleanTemporaryFiles(url);
         SecurityActions.setThreadContextClassLoader(oldTCCL);
      }
   }
   
   /**
    * Get File object of URL who locate xml resource
    * @param url url of the archive
    * @return a File object pointing of xml resource
    * @throws URISyntaxException Thrown if the resource adapter cant be found
    * @throws IOException Thrown if the resource adapter cant be accessible
    */
   protected File getFileFromUrl(URL url) throws URISyntaxException, IOException
   {
      return new File(url.toURI());
   }
   
   /**
    * Get IronJacamar Home directory
    * @return a String object
    */
   protected String getIronJacamarHome()
   {
      return SecurityActions.getSystemProperty("iron.jacamar.home");
   }
   
   /**
    * Clean all potential temporary files
    * @param url url of the archive
    * @throws DeployException Thrown if the resource cant be cleaned
    */
   protected void cleanTemporaryFiles(URL url) throws DeployException
   {
   }

   /**
    * Deploy an entry in the -ra.xml deployment
    * @param url The deployment url
    * @param raxml The -ra.xml entry
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   private Deployment doDeploy(URL url, org.jboss.jca.common.api.metadata.resourceadapter.Activation activation,
      ClassLoader parent) throws DeployException
   {
      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      try
      {
         // Find the archive in MDR
         String archive = activation.getArchive();
         URL deployment = null;
         Set<String> deployments = ((RAConfiguration) getConfiguration()).getMetadataRepository().getResourceAdapters();

         for (String s : deployments)
         {
            if (s.endsWith(archive))
               deployment = new URL(s);
         }

         if (deployment == null)
         {
            throw new DeployException("Archive " + archive + " couldn't be resolved in " + url.toExternalForm());
         }

         MetadataRepository mdr = ((RAConfiguration) getConfiguration()).getMetadataRepository();
         Connector cmd = mdr.getResourceAdapter(deployment.toExternalForm());
         File root = mdr.getRoot(deployment.toExternalForm());

         if (cmd != null)
            cmd = (Connector)cmd.copy();

         cmd = (new Merger()).mergeConnectorWithCommonIronJacamar(activation, cmd);
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

         String deploymentName = archive.substring(0, archive.indexOf(".rar"));

         CommonDeployment c = createObjectsAndInjectValue(url, deploymentName, root, cl, cmd, activation);

         List<ObjectName> ons = null;
         if (c.isActivateDeployment())
            ons = registerManagementView(c.getConnector(),
                                         kernel.getMBeanServer(),
                                         kernel.getName());

         JndiStrategy jndiStrategy = ((RAConfiguration) getConfiguration()).getJndiStrategy();
         MetadataRepository metadataRepository = ((RAConfiguration) getConfiguration()).getMetadataRepository();
         ResourceAdapterRepository resourceAdapterRepository =
            ((RAConfiguration) getConfiguration()).getResourceAdapterRepository();

         return new RaXmlDeployment(c.getURL(), deployment, c.getDeploymentName(), c.getResourceAdapter(),
                                    c.getResourceAdapterKey(), c.getBootstrapContextIdentifier(),
                                    jndiStrategy, metadataRepository, resourceAdapterRepository,
                                    c.getCfs(), c.getCfJndiNames(), c.getConnectionManagers(),
                                    c.getAos(), c.getAoJndiNames(), 
                                    c.getRecovery(), getTransactionIntegration().getRecoveryRegistry(),
                                    ((RAConfiguration)getConfiguration()).getManagementRepository(), c.getConnector(),
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
   protected boolean checkActivation(Connector cmd, Activation activation)
   {
      return true;
   }
}
