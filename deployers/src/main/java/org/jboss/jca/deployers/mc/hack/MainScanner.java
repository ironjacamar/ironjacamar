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

package org.jboss.jca.deployers.mc.hack;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * Main scanner
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 */
public class MainScanner
{
   /** The logger */
   private static Logger log = Logger.getLogger(MainScanner.class);

   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();

   /** The MC main deployer */
   private MainDeployer mainDeployer;

   /** The deploy directory */
   private URL deployDirectory;

   /**
    * Constructor
    */
   public MainScanner()
   {
   }

   /**
    * Set the main deployer instance
    * @param md The instance
    */
   public void setMainDeployer(MainDeployer md)
   {
      mainDeployer = md;
   }

   /**
    * Get the main deployer instance
    * @return The instance
    */
   public MainDeployer getMainDeployer()
   {
      return mainDeployer;
   }

   /**
    * Set the deploy directory
    * @param deploy The instance
    */
   public void setDeployDirectory(String deploy)
   {
      try
      {
         deployDirectory = new URL(deploy);
      }
      catch (MalformedURLException mue)
      {
         // Ignore for now
      }
   }

   /**
    * Get the deploy directory
    * @return The directory
    */
   public String getDeployDirectory()
   {
      return deployDirectory.toString();
   }

   /**
    * Start
    * @exception Exception Thrown if an error occurs
    */
   public void start() throws Exception 
   {
      log.debug("MainScanner starting.");

      if (mainDeployer == null)
         throw new IllegalArgumentException("MainDeployer is null");

      if (deployDirectory == null)
         throw new IllegalArgumentException("DeployDirectory is null");

      ClassLoader tccl = Thread.currentThread().getContextClassLoader();

      Enumeration<URL> urls = tccl.getResources("META-INF/jboss-beans.xml");
      while (urls.hasMoreElements())
      {
         URL u = urls.nextElement();
         URLConnection c = u.openConnection();

         if (!(c instanceof JarURLConnection))
            continue;

         JarURLConnection connection = (JarURLConnection)c;
         URL jarFileURL = connection.getJarFileURL();
         deploy(jarFileURL);
      }

      deployDirectory(deployDirectory);
      
      log.debug("MainScanner started.");
   }

   /**
    * Stop
    * @exception Exception Thrown if an error occurs
    */
   public void stop() throws Exception 
   {
      log.debug("MainScanner stopping.");

      log.debug("MainScanner stopped.");
   }

   /**
    * Deploy
    * @param url The URL
    * @exception DeploymentException Thrown if a deploy error occurs
    * @exception IOException Thrown if an I/O error occurs
    */
   protected void deploy(URL url) throws DeploymentException, IOException
   {
      log.info("Deploying: " + url);

      VirtualFile root = VFS.getRoot(url);
      VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      mainDeployer.deploy(deployment);

      log.info("Deployed: " + url);
   }

   /**
    * Deploy directory
    * @param url The URL
    * @exception DeploymentException Thrown if a deploy error occurs
    * @exception IOException Thrown if an I/O error occurs
    */
   protected void deployDirectory(URL url) throws DeploymentException, IOException
   {
      List<Deployment> deployments = new ArrayList<Deployment>();
      VirtualFile deployDir = VFS.getRoot(url);
      for (VirtualFile child : deployDir.getChildren())
      {
         VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(child);
         log.info("Deploying " + deployment.getName());
         deployments.add(deployment);
      }
      mainDeployer.deploy(deployments.toArray(new Deployment[0]));
   }
}
