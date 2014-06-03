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

import org.jboss.jca.common.annotations.Annotations;
import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.spec.AdminObject;
import org.jboss.jca.common.api.metadata.spec.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.metadata.MetadataFactory;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScanner;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScannerFactory;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.naming.JndiStrategy;
import org.jboss.jca.core.spi.rar.ResourceAdapterRepository;
import org.jboss.jca.deployers.DeployersLogger;
import org.jboss.jca.deployers.common.CommonDeployment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.jboss.logging.Logger;

import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.classloading.KernelClassLoader;
import com.github.fungal.api.util.FileUtil;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;

/**
 * The RA deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a>
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 */
public final class RADeployer extends AbstractFungalRADeployer implements Deployer
{
   /** The logger */
   private static DeployersLogger log = Logger.getMessageLogger(DeployersLogger.class, RADeployer.class.getName());

   /**
    * Constructor
    */
   public RADeployer()
   {
      super(true);
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
      if (url == null || !(url.toExternalForm().endsWith(".rar") || url.toExternalForm().endsWith(".rar/")))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public int getOrder()
   {
      return Constants.RA_DEPLOYER;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized com.github.fungal.spi.deployers.Deployment deploy(URL url, Context context, ClassLoader parent)
      throws DeployException
   {
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
         if (((RAConfiguration) getConfiguration()).getScopeDeployment())
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
         Activation activation = metadataFactory.getIronJacamarMetaData(root);

         // Annotation scanning
         if (scanArchive(cmd))
         {
            Annotations annotator = new Annotations();
            AnnotationScanner scanner = AnnotationScannerFactory.getAnnotationScanner();
            AnnotationRepository repository = scanner.scan(cl.getURLs(), cl);
            cmd = annotator.merge(cmd, repository, cl);
         }

         // Validate metadata
         cmd.validate();

         // Merge metadata
         cmd = (new Merger()).mergeConnectorWithCommonIronJacamar(activation, cmd);

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

         return new RADeployment(c.getURL(), c.getDeploymentName(), c.isActivateDeployment(), c.getResourceAdapter(),
                                 c.getResourceAdapterKey(), c.getBootstrapContextIdentifier(),
                                 jndiStrategy, metadataRepository, resourceAdapterRepository,
                                 c.getCfs(), c.getCfJndiNames(), c.getConnectionManagers(),
                                 c.getAos(), c.getAoJndiNames(), 
                                 c.getRecovery(), getTransactionIntegration().getRecoveryRegistry(),
                                 destination, 
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

   /**
    * Check if the resource adapter should be activated based on the ironjacamar.xml input
    * @param cmd The connector metadata
    * @param activation The activation metadata
    * @return True if the deployment should be activated; otherwise false
    */
   @Override
   protected boolean checkActivation(Connector cmd, Activation activation)
   {
      if (cmd != null)
      {
         Set<String> raMcfClasses = new HashSet<String>();
         Set<String> raAoClasses = new HashSet<String>();

         ResourceAdapter ra = cmd.getResourceadapter();
         if (ra != null && ra.getOutboundResourceadapter() != null &&
             ra.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            List<ConnectionDefinition> cdMetas = ra.getOutboundResourceadapter().getConnectionDefinitions();
            if (cdMetas.size() > 0)
            {
               for (ConnectionDefinition cdMeta : cdMetas)
               {
                  raMcfClasses.add(cdMeta.getManagedConnectionFactoryClass().getValue());
               }
            }
         }

         if (ra != null && ra.getAdminObjects() != null)
         {
            List<AdminObject> aoMetas = ra.getAdminObjects();
            if (aoMetas.size() > 0)
            {
               for (AdminObject aoMeta : aoMetas)
               {
                  raAoClasses.add(aoMeta.getAdminobjectClass().getValue());
               }
            }
         }

         // Pure inflow
         if (raMcfClasses.size() == 0 && raAoClasses.size() == 0)
            return true;

         if (activation != null)
         {
            if (activation.getConnectionDefinitions() != null)
            {
               for (org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition def :
                       activation.getConnectionDefinitions())
               {
                  String clz = def.getClassName();
                  
                  if (raMcfClasses.contains(clz))
                     return true;
               }
               if (raMcfClasses.size() == 1 && activation.getConnectionDefinitions().size() == 1)
               {
                  return true;
               }
            }

            if (activation.getAdminObjects() != null)
            {
               for (org.jboss.jca.common.api.metadata.resourceadapter.AdminObject def : activation.getAdminObjects())
               {
                  String clz = def.getClassName();

                  if (raAoClasses.contains(clz))
                     return true;
               }
               if (raAoClasses.size() == 1 && activation.getAdminObjects().size() == 1)
               {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
