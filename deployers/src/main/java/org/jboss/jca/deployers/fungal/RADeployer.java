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

import org.jboss.jca.common.annotations.Annotations;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.metadata.MetadataFactory;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScanner;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScannerFactory;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.naming.JndiStrategy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;

import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.api.util.FileUtil;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.DeployerOrder;
import com.github.fungal.spi.deployers.Deployment;
import com.github.fungal.spi.deployers.MultiStageDeployer;

/**
 * The RA deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 */
public final class RADeployer extends AbstractResourceAdapterDeployer
   implements
      Deployer,
      MultiStageDeployer,
      DeployerOrder
{
   static Logger log = Logger.getLogger(RADeployer.class);

   static boolean trace = log.isTraceEnabled();

   /**
    * Constructor
    */
   public RADeployer()
   {
      super(true);
   }

   /**
    * Deployer order
    * @return The deployment
    */
   @Override
   public int getOrder()
   {
      return Integer.MIN_VALUE;
   }

   /**
    * Deploy
    * @param url The url
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   @SuppressWarnings("rawtypes")
   @Override
   public synchronized Deployment deploy(URL url, ClassLoader parent) throws DeployException
   {
      if (url == null || !(url.toExternalForm().endsWith(".rar") || url.toExternalForm().endsWith(".rar/")))
         return null;

      log.debug("Deploying: " + url.toExternalForm());

      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      try
      {
         File f = new File(url.toURI());

         if (!f.exists())
            throw new IOException("Archive " + url.toExternalForm() + " doesnt exists");

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
         if (getConfiguration().getScopeDeployment())
         {
            cl = ClassLoaderFactory.create(ClassLoaderFactory.TYPE_PARENT_LAST, urls, parent);
         }
         else
         {
            cl = ClassLoaderFactory.create(ClassLoaderFactory.TYPE_PARENT_FIRST, urls, parent);
         }
         SecurityActions.setThreadContextClassLoader(cl);

         // Parse metadata
         MetadataFactory metadataFactory = new MetadataFactory();
         Connector cmd = metadataFactory.getStandardMetaData(root);
         IronJacamar ijmd = metadataFactory.getIronJacamarMetaData(root);

         // Annotation scanning
         Annotations annotator = new Annotations();
         AnnotationScanner scanner = AnnotationScannerFactory.getAnnotationScanner();
         AnnotationRepository repository = scanner.scan(cl.getURLs(), cl);
         cmd = annotator.merge(cmd, repository);

         // Validate metadata
         cmd.validate();

         // Merge metadata
         cmd = (new Merger()).mergeConnectorWithCommonIronJacamar(ijmd, cmd);

         return createObjectsAndInjectValue(url, deploymentName, root, destination, cl, cmd, ijmd, null);

      }
      catch (DeployException de)
      {
         //just rethrow
         throw de;
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
   public Deployment createDeployment(URL deploymentUrl, String deploymentName, boolean activator,
      ResourceAdapter resourceAdapter, JndiStrategy jndiStrategy, MetadataRepository metadataRepository, Object[] cfs,
      File destination, ClassLoader cl, Logger log, String[] jndis, URL deployment, boolean activateDeployment)
   {
      return new RADeployment(deploymentUrl, deploymentName, activateDeployment, resourceAdapter, jndiStrategy,
                              metadataRepository, cfs, jndis, destination, cl, log);
   }

   /**
    * Check if the resource adapter should be activated based on the ironjacamar.xml input
    * @param cmd The connector metadata
    * @param ijmd The IronJacamar metadata
    * @return True if the deployment should be activated; otherwise false
    */
   @Override
   protected boolean checkActivation(Connector cmd, IronJacamar ijmd)
   {
      if (cmd != null && ijmd != null)
      {
         Set<String> raClasses = new HashSet<String>();
         Set<String> ijClasses = new HashSet<String>();

         if (cmd.getVersion() == Version.V_10)
         {
            ResourceAdapter10 ra10 = (ResourceAdapter10) cmd.getResourceadapter();
            raClasses.add(ra10.getManagedConnectionFactoryClass().getValue());
         }
         else
         {
            ResourceAdapter1516 ra = (ResourceAdapter1516) cmd.getResourceadapter();
            if (ra != null && ra.getOutboundResourceadapter() != null &&
                ra.getOutboundResourceadapter().getConnectionDefinitions() != null)
            {
               List<ConnectionDefinition> cdMetas = ra.getOutboundResourceadapter().getConnectionDefinitions();
               if (cdMetas.size() > 0)
               {
                  for (ConnectionDefinition cdMeta : cdMetas)
                  {
                     raClasses.add(cdMeta.getManagedConnectionFactoryClass().getValue());
                  }
               }
            }
         }

         if (raClasses.size() == 0)
            return false;

         if (ijmd.getConnectionDefinitions() != null)
         {
            for (org.jboss.jca.common.api.metadata.common.CommonConnDef def : ijmd.getConnectionDefinitions())
            {
               String clz = def.getClassName();

               if (clz == null && raClasses.size() == 1)
                  return true;

               if (clz != null)
                  ijClasses.add(clz);
            }
         }

         for (String clz : raClasses)
         {
            if (!ijClasses.contains(clz))
               return false;
         }

         return true;
      }

      return false;
   }

}
