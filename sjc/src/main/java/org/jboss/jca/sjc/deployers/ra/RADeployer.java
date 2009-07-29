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

package org.jboss.jca.sjc.deployers.ra;

import org.jboss.jca.sjc.annotationscanner.Annotation;
import org.jboss.jca.sjc.annotationscanner.AnnotationScanner;
import org.jboss.jca.sjc.deployers.DeployException;
import org.jboss.jca.sjc.deployers.Deployer;
import org.jboss.jca.sjc.deployers.Deployment;
import org.jboss.jca.sjc.util.ExtractUtil;
import org.jboss.jca.sjc.util.Injection;
import org.jboss.jca.sjc.util.JarFilter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.metadata.rar.jboss.JBossRAMetaData;
import org.jboss.metadata.rar.spec.ConfigPropertyMetaData;
import org.jboss.metadata.rar.spec.ConnectorMetaData;
import org.jboss.metadata.rar.spec.JCA16DTDMetaData;
import org.jboss.metadata.rar.spec.JCA16DefaultNSMetaData;
import org.jboss.metadata.rar.spec.JCA16MetaData;

/**
 * The RA deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RADeployer implements Deployer
{
   private static Logger log = Logger.getLogger(RADeployer.class);
   private static boolean trace = log.isTraceEnabled();

   /**
    * Constructor
    */
   public RADeployer()
   {
   }

   /**
    * Deploy
    * @param f The file
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   public Deployment deploy(File f, ClassLoader parent) throws DeployException
   {
      if (f == null || !f.getAbsolutePath().endsWith(".rar"))
         return null;

      log.info("Deploying: " + f.getAbsolutePath());

      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      try
      {
         File root = null;

         if (f.isFile())
         {
            File destination = new File(SecurityActions.getSystemProperty("jboss.jca.home"), "/tmp/");
            root = ExtractUtil.extract(f, destination);
         }
         else
         {
            root = f;
         }
      
         // Create classloader
         URL[] urls = getUrls(root);
         URLClassLoader cl = SecurityActions.createURLCLassLoader(urls, parent);
         SecurityActions.setThreadContextClassLoader(cl);

         // Parse metadata
         ConnectorMetaData cmd = Metadata.getStandardMetaData(root);
         JBossRAMetaData jrmd = Metadata.getJBossMetaData(root);
         boolean isMetadataComplete = true;

         // Process annotations
         if (cmd == null || cmd.is16())
         {
            Map<Class, List<Annotation>> annotations = AnnotationScanner.scan(cl.getURLs(), cl);

            isMetadataComplete = false;
            if (cmd != null)
            {
               if (cmd instanceof JCA16MetaData)
               {
                  JCA16MetaData jmd = (JCA16MetaData)cmd;
                  isMetadataComplete = jmd.isMetadataComplete();
               }
               else if (cmd instanceof JCA16DefaultNSMetaData)
               {
                  JCA16DefaultNSMetaData jmd = (JCA16DefaultNSMetaData)cmd;
                  isMetadataComplete = jmd.isMetadataComplete();
               }
               else if (cmd instanceof JCA16DTDMetaData)
               {
                  JCA16DTDMetaData jmd = (JCA16DTDMetaData)cmd;
                  isMetadataComplete = jmd.isMetadataComplete();
               }
            }
            
            if (cmd == null || !isMetadataComplete)
               cmd = Annotations.process(cmd, annotations);
         }
         
         // Validate metadata
         
         // Merge metadata
         cmd = Metadata.merge(cmd, jrmd);
         
         // Create objects
         Object resourceAdapter = null;
         if (cmd != null && cmd.getRa() != null && cmd.getRa().getRaClass() != null)
         {
            Class raClass = Class.forName(cmd.getRa().getRaClass(), true, cl);
            resourceAdapter = raClass.newInstance();
         }

         // Inject values
         if (resourceAdapter != null && cmd != null && cmd.getRa() != null)
         {
            List<ConfigPropertyMetaData> l = cmd.getRa().getConfigProperty();
            if (l != null)
            {
               for (ConfigPropertyMetaData cpmd : l)
               {
                  Injection.inject(cpmd.getType(), cpmd.getName(), cpmd.getValue(), resourceAdapter);
               }
            }
         }

         // Bean validation
         
         // Activate deployment

         return new RADeployment(f.getName(), cl);
      }
      catch (Throwable t)
      {
         throw new DeployException("Deployment " + f.getName() + " failed", t);
      }
      finally
      {
         SecurityActions.setThreadContextClassLoader(oldTCCL);
      }
   }

   /**
    * Get the URLs for the directory and all libraries located in the directory
    * @param directrory The directory
    * @return The URLs
    * @exception MalformedURLException MalformedURLException
    * @exception IOException IOException
    */
   private URL[] getUrls(File directory) throws MalformedURLException, IOException
   {
      List<URL> list = new LinkedList<URL>();

      if (directory.exists() && directory.isDirectory())
      {
         // Add directory
         list.add(directory.toURI().toURL());

         // Add the contents of the directory too
         File[] jars = directory.listFiles(new JarFilter());

         for (int j = 0; jars != null && j < jars.length; j++)
         {
            list.add(jars[j].getCanonicalFile().toURI().toURL());
         }
      }
      return list.toArray(new URL[list.size()]);      
   }
}
