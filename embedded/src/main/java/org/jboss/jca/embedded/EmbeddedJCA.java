/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.embedded;

import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.KernelFactory;
import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.configuration.DeploymentOrder;
import com.github.fungal.api.configuration.KernelConfiguration;

/**
 * The embedded IronJacamar container
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
class EmbeddedJCA implements Embedded
{
   /** Buffer size */
   private static final int BUFFER_SIZE = 4096;

   /** The logger */
   private static Logger log = Logger.getLogger(EmbeddedJCA.class);

   /** Enable full profile */
   private final boolean fullProfile;

   /** Kernel */
   private Kernel kernel;

   /** ShrinkWrap deployments */
   private List<File> shrinkwrapDeployments;

   /** Started */
   private boolean started;

   /**
    * Constructs an embedded JCA environment. If <code>fullProfile</code>
    * is <code>true</code> then a full JCA 1.6 container is initialized -
    * otherwise only the basic kernel is initialized and services has
    * to be added as deployments
    * @param fullProfile Should a full profile be initialized
    */
   EmbeddedJCA(boolean fullProfile)
   {
      this.fullProfile = fullProfile;
      this.shrinkwrapDeployments = null;
      this.started = false;
   }

   /**
    * Startup
    * @exception Throwable If an error occurs
    */
   public void startup() throws Throwable
   {
      if (started)
         throw new IllegalStateException("Container already started");

      List<String> order = new ArrayList<String>(3);
      order.add(".xml");
      order.add(".rar");
      order.add("-ra.xml");
      order.add("-ds.xml");

      boolean management = 
         Boolean.valueOf(SecurityActions.getSystemProperty("ironjacamar.embedded.management", "false"));

      KernelConfiguration kernelConfiguration = new KernelConfiguration();
      kernelConfiguration = kernelConfiguration.name("iron.jacamar");
      kernelConfiguration = kernelConfiguration.home(null);
      kernelConfiguration = kernelConfiguration.classLoader(ClassLoaderFactory.TYPE_PARENT_FIRST);
      kernelConfiguration = kernelConfiguration.management(management);
      if (management)
         kernelConfiguration = kernelConfiguration.usePlatformMBeanServer(true);
      kernelConfiguration = kernelConfiguration.parallelDeploy(false);
      kernelConfiguration = kernelConfiguration.remoteAccess(false);
      kernelConfiguration = kernelConfiguration.hotDeployment(false);
      kernelConfiguration = kernelConfiguration.eventListener(new PreClassLoaderEventListener());
      kernelConfiguration = kernelConfiguration.eventListener(new PostClassLoaderEventListener());
      kernelConfiguration = kernelConfiguration.deploymentOrder(new DeploymentOrder(order));

      kernel = KernelFactory.create(kernelConfiguration);
      kernel.startup();

      if (fullProfile)
      {
         deploy(EmbeddedJCA.class.getClassLoader(), "naming.xml");
         deploy(EmbeddedJCA.class.getClassLoader(), "transaction.xml");
         deploy(EmbeddedJCA.class.getClassLoader(), "stdio.xml");
         deploy(EmbeddedJCA.class.getClassLoader(), "jca.xml");
         deploy(EmbeddedJCA.class.getClassLoader(), "ds.xml");
      }

      started = true;
   }

   /**
    * Shutdown
    * @exception Throwable If an error occurs
    */
   public void shutdown() throws Throwable
   {
      if (!started)
         throw new IllegalStateException("Container not started");

      if (shrinkwrapDeployments != null && shrinkwrapDeployments.size() > 0)
      {
         List<File> copy = new ArrayList<File>(shrinkwrapDeployments);
         for (File f : copy)
         {
            removeDeployment(f);
         }
      }

      if (fullProfile)
      {
         undeploy(EmbeddedJCA.class.getClassLoader(), "ds.xml");
         undeploy(EmbeddedJCA.class.getClassLoader(), "jca.xml");
         undeploy(EmbeddedJCA.class.getClassLoader(), "stdio.xml");
         undeploy(EmbeddedJCA.class.getClassLoader(), "transaction.xml");
         undeploy(EmbeddedJCA.class.getClassLoader(), "naming.xml");
      }

      kernel.shutdown();

      started = false;
   }

   /**
    * Lookup a bean
    * @param <T> the generic type
    * @param name The bean name
    * @param expectedType The expected type for the bean
    * @return The bean instance
    * @exception Throwable If an error occurs
    */
   public <T> T lookup(String name, Class<T> expectedType) throws Throwable
   {
      if (name == null)
         throw new IllegalArgumentException("Name is null");

      if (expectedType == null)
         throw new IllegalArgumentException("ExpectedType is null");

      if (!started)
         throw new IllegalStateException("Container not started");

      return kernel.getBean(name, expectedType);
   }

   /**
    * Deploy
    * @param url The resource url
    * @exception Throwable If an error occurs
    */
   public void deploy(URL url) throws Throwable
   {
      if (url == null)
         throw new IllegalArgumentException("Url is null");

      if (!started)
         throw new IllegalStateException("Container not started");

      log.debugf("Deploying: %s", url);

      kernel.getMainDeployer().deploy(url);
   }

   /**
    * {@inheritDoc}
    */
   public void deploy(Descriptor descriptor) throws Throwable
   {
      if (descriptor == null)
         throw new IllegalArgumentException("Descriptor is null");

      if (descriptor.getDescriptorName() == null)
         throw new IllegalArgumentException("Descriptor name is null");

      if (!(descriptor instanceof InputStreamDescriptor ||
            descriptor instanceof org.jboss.jca.embedded.dsl.datasources10.api.DatasourcesDescriptor ||
            descriptor instanceof org.jboss.jca.embedded.dsl.datasources11.api.DatasourcesDescriptor ||
            descriptor instanceof org.jboss.jca.embedded.dsl.resourceadapters10.api.ResourceAdaptersDescriptor ||
            descriptor instanceof org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdaptersDescriptor))
         throw new IllegalArgumentException("Unsupported descriptor: " + descriptor.getClass().getName());

      if (!started)
         throw new IllegalStateException("Container not started");

      File parentDirectory = new File(SecurityActions.getSystemProperty("java.io.tmpdir"));
      File descriptorFile = new File(parentDirectory, descriptor.getDescriptorName());

      if (descriptorFile.exists())
         recursiveDelete(descriptorFile);

      FileOutputStream os = new FileOutputStream(descriptorFile);
      BufferedOutputStream bos = new BufferedOutputStream(os, BUFFER_SIZE);
      try
      {
         descriptor.exportTo(bos);
         bos.flush();
      }
      finally
      {
         if (bos != null)
         {
            try
            {
               bos.close();
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }
      }

      log.debugf("Deploying: %s", descriptorFile);

      kernel.getMainDeployer().deploy(descriptorFile.toURI().toURL());
   }

   /**
    * Deploy
    * @param raa The resource adapter archive
    * @exception Throwable If an error occurs
    */
   public void deploy(ResourceAdapterArchive raa) throws Throwable
   {
      if (raa == null)
         throw new IllegalArgumentException("Url is null");

      if (!raa.getName().endsWith(".rar"))
         throw new IllegalArgumentException(raa.getName() + " doesn't end with .rar");

      if (!started)
         throw new IllegalStateException("Container not started");

      File parentDirectory = new File(SecurityActions.getSystemProperty("java.io.tmpdir"));
      File raaFile = new File(parentDirectory, raa.getName());

      if (shrinkwrapDeployments != null && shrinkwrapDeployments.contains(raaFile))
         throw new IOException(raa.getName() + " already deployed");

      if (raaFile.exists())
         recursiveDelete(raaFile);

      raa.as(ZipExporter.class).exportTo(raaFile, true);

      if (shrinkwrapDeployments == null)
         shrinkwrapDeployments = new ArrayList<File>(1);

      shrinkwrapDeployments.add(raaFile);

      log.debugf("Deploying: %s", raaFile);

      kernel.getMainDeployer().deploy(raaFile.toURI().toURL());
   }

   /**
    * Deploy
    * @param cl The class loader
    * @param name The resource name
    * @exception Throwable If an error occurs
    */
   private void deploy(ClassLoader cl, String name) throws Throwable
   {
      if (cl == null)
         throw new IllegalArgumentException("ClassLoader is null");

      if (name == null)
         throw new IllegalArgumentException("Name is null");

      URL url = cl.getResource(name);

      if (url == null)
         throw new IllegalArgumentException("Resource is null");

      log.debugf("Deploying: %s", url);

      kernel.getMainDeployer().deploy(url);
   }

   /**
    * Undeploy
    * @param url The resource url
    * @exception Throwable If an error occurs
    */
   public void undeploy(URL url) throws Throwable
   {
      if (url == null)
         throw new IllegalArgumentException("Url is null");

      if (!started)
         throw new IllegalStateException("Container not started");

      log.debugf("Undeploying: %s", url);

      kernel.getMainDeployer().undeploy(url);
   }

   /**
    * {@inheritDoc}
    */
   public void undeploy(Descriptor descriptor) throws Throwable
   {
      if (descriptor == null)
         throw new IllegalArgumentException("Descriptor is null");

      if (descriptor.getDescriptorName() == null)
         throw new IllegalArgumentException("Descriptor name is null");

      if (!(descriptor instanceof InputStreamDescriptor ||
            descriptor instanceof org.jboss.jca.embedded.dsl.datasources10.api.DatasourcesDescriptor ||
            descriptor instanceof org.jboss.jca.embedded.dsl.datasources11.api.DatasourcesDescriptor ||
            descriptor instanceof org.jboss.jca.embedded.dsl.resourceadapters10.api.ResourceAdaptersDescriptor ||
            descriptor instanceof org.jboss.jca.embedded.dsl.resourceadapters11.api.ResourceAdaptersDescriptor))
         throw new IllegalArgumentException("Unsupported descriptor: " + descriptor.getClass().getName());

      if (!started)
         throw new IllegalStateException("Container not started");

      File parentDirectory = new File(SecurityActions.getSystemProperty("java.io.tmpdir"));
      File descriptorFile = new File(parentDirectory, descriptor.getDescriptorName());

      log.debugf("Undeploying: %s", descriptorFile);

      kernel.getMainDeployer().undeploy(descriptorFile.toURI().toURL());

      recursiveDelete(descriptorFile);
   }

   /**
    * Undeploy
    * @param raa The resource adapter archive
    * @exception Throwable If an error occurs
    */
   public void undeploy(ResourceAdapterArchive raa) throws Throwable
   {
      if (raa == null)
         throw new IllegalArgumentException("Url is null");

      if (!started)
         throw new IllegalStateException("Container not started");

      File parentDirectory = new File(SecurityActions.getSystemProperty("java.io.tmpdir"));
      File raaFile = new File(parentDirectory, raa.getName());

      log.debugf("Undeploying: %s", raaFile);

      if (shrinkwrapDeployments == null || !shrinkwrapDeployments.contains(raaFile))
         throw new IOException(raa.getName() + " not deployed");

      kernel.getMainDeployer().undeploy(raaFile.toURI().toURL());

      removeDeployment(raaFile);
   }

   /**
    * Undeploy
    * @param cl The class loader
    * @param name The resource name
    * @exception Throwable If an error occurs
    */
   private void undeploy(ClassLoader cl, String name) throws Throwable
   {
      if (cl == null)
         throw new IllegalArgumentException("ClassLoader is null");

      if (name == null)
         throw new IllegalArgumentException("Name is null");

      URL url = cl.getResource(name);

      log.debugf("Undeploying: %s", url);

      kernel.getMainDeployer().undeploy(url);
   }

   /**
    * Remove ShrinkWrap deployment
    * @param deployment The deployment
    * @exception IOException Thrown if the deployment cant be removed
    */
   private void removeDeployment(File deployment) throws IOException
   {
      if (deployment == null)
         throw new IllegalArgumentException("Deployment is null");

      if (deployment.exists())
      {
         shrinkwrapDeployments.remove(deployment);

         if (shrinkwrapDeployments.size() == 0)
            shrinkwrapDeployments = null;

         recursiveDelete(deployment);
      }
   }

   /**
    * Recursive delete
    * @param f The file handler
    * @exception IOException Thrown if a file could not be deleted
    */
   private void recursiveDelete(File f) throws IOException
   {
      if (f != null && f.exists())
      {
         File[] files = f.listFiles();
         if (files != null)
         {
            for (int i = 0; i < files.length; i++)
            {
               if (files[i].isDirectory())
               {
                  recursiveDelete(files[i]);
               }
               else
               {
                  if (!files[i].delete())
                     throw new IOException("Could not delete " + files[i]);
               }
            }
         }
         if (!f.delete())
            throw new IOException("Could not delete " + f);
      }
   }
}
