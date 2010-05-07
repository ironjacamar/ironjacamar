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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.KernelFactory;
import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.configuration.KernelConfiguration;

/**
 * The embedded JBoss JCA container
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class EmbeddedJCA
{
   /** Enable full profile */
   private boolean fullProfile;

   /** Kernel */
   private Kernel kernel;

   /** ShrinkWrap deployments */
   private List<File> shrinkwrapDeployments;

   /**
    * Constructs an embedded JCA environment using
    * the full JCA 1.6 profile
    */
   public EmbeddedJCA()
   {
      this(true);
   }

   /**
    * Constructs an embedded JCA environment. If <code>fullProfile</code>
    * is <code>true</code> then a full JCA 1.6 container is initialized -
    * otherwise only the basic kernel is initialized and services has
    * to be added as deployments
    * @param fullProfile Should a full profile be initialized
    */
   public EmbeddedJCA(boolean fullProfile)
   {
      this.fullProfile = fullProfile;
      this.shrinkwrapDeployments = null;
   }

   /**
    * Startup
    * @exception Throwable If an error occurs
    */
   public void startup() throws Throwable
   {
      KernelConfiguration kernelConfiguration = new KernelConfiguration();
      kernelConfiguration = kernelConfiguration.name("jboss.jca");
      kernelConfiguration = kernelConfiguration.classLoader(ClassLoaderFactory.TYPE_PARENT_FIRST);
      kernelConfiguration = kernelConfiguration.parallelDeploy(false);
      kernelConfiguration = kernelConfiguration.remoteAccess(false);
      kernelConfiguration = kernelConfiguration.hotDeployment(false);
      kernelConfiguration = kernelConfiguration.eventListener(new PreClassLoaderEventListener());
      kernelConfiguration = kernelConfiguration.eventListener(new PostClassLoaderEventListener());

      kernel = KernelFactory.create(kernelConfiguration);
      kernel.startup();

      if (fullProfile)
      {
         deploy(EmbeddedJCA.class.getClassLoader(), "naming.xml");
         deploy(EmbeddedJCA.class.getClassLoader(), "transaction.xml");
         deploy(EmbeddedJCA.class.getClassLoader(), "stdio.xml");
         deploy(EmbeddedJCA.class.getClassLoader(), "jca.xml");
      }
   }

   /**
    * Shutdown
    * @exception Throwable If an error occurs
    */
   public void shutdown() throws Throwable
   {
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
         undeploy(EmbeddedJCA.class.getClassLoader(), "jca.xml");
         undeploy(EmbeddedJCA.class.getClassLoader(), "stdio.xml");
         undeploy(EmbeddedJCA.class.getClassLoader(), "transaction.xml");
         undeploy(EmbeddedJCA.class.getClassLoader(), "naming.xml");
      }

      kernel.shutdown();
   }

   /**
    * Lookup a bean
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

      kernel.getMainDeployer().deploy(url);
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

      InputStream is = raa.as(ZipExporter.class).exportZip();

      File parentDirectory = new File(SecurityActions.getSystemProperty("java.io.tmpdir"));
      File raaFile = new File(parentDirectory, raa.getName());

      if (shrinkwrapDeployments != null && shrinkwrapDeployments.contains(raaFile))
         throw new IOException(raa.getName() + " already deployed");

      if (raaFile.exists())
         recursiveDelete(raaFile);

      FileOutputStream os = new FileOutputStream(raaFile);

      byte[] buffer = new byte[4096];
      int read = 0;
      try
      {
         while ((read = is.read(buffer)) != -1)
         {
            os.write(buffer, 0, read);
         }

         os.flush();
      }
      finally
      {
         if (os != null)
         {
            try
            {
               os.close();
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }

         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }
      }

      if (shrinkwrapDeployments == null)
         shrinkwrapDeployments = new ArrayList<File>(1);

      shrinkwrapDeployments.add(raaFile);
     
      kernel.getMainDeployer().deploy(raaFile.toURI().toURL());
   }

   /**
    * Deploy
    * @param cl The class loader
    * @param name The resource name
    * @exception Throwable If an error occurs
    */
   public void deploy(ClassLoader cl, String name) throws Throwable
   {
      if (cl == null)
         throw new IllegalArgumentException("ClassLoader is null");

      if (name == null)
         throw new IllegalArgumentException("Name is null");

      URL url = cl.getResource(name);
      kernel.getMainDeployer().deploy(url);
   }

   /**
    * Deploy
    * @param clz The class
    * @exception Throwable If an error occurs
    */
   public void deploy(Class<?> clz) throws Throwable
   {
      if (clz == null)
         throw new IllegalArgumentException("Clz is null");      

      String name = clz.getName().replace('.', '/');
      name += "-jboss-beans.xml";

      URL url = clz.getClassLoader().getResource(name);
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

      kernel.getMainDeployer().undeploy(url);
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

      File parentDirectory = new File(SecurityActions.getSystemProperty("java.io.tmpdir"));
      File raaFile = new File(parentDirectory, raa.getName());

      if (!shrinkwrapDeployments.contains(raaFile))
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
   public void undeploy(ClassLoader cl, String name) throws Throwable
   {
      if (cl == null)
         throw new IllegalArgumentException("ClassLoader is null");

      if (name == null)
         throw new IllegalArgumentException("Name is null");

      URL url = cl.getResource(name);
      kernel.getMainDeployer().undeploy(url);
   }

   /**
    * Undeploy
    * @param clz The class
    * @exception Throwable If an error occurs
    */
   public void undeploy(Class<?> clz) throws Throwable
   {
      if (clz == null)
         throw new IllegalArgumentException("Clz is null");      

      String name = clz.getName().replace('.', '/');
      name += "-jboss-beans.xml";

      URL url = clz.getClassLoader().getResource(name);
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
