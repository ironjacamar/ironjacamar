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

import org.jboss.jca.sjc.deployers.Deployer;
import org.jboss.jca.sjc.deployers.Deployment;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.metadata.rar.jboss.JBossRA10DefaultNSMetaData;
import org.jboss.metadata.rar.jboss.JBossRA10MetaData;
import org.jboss.metadata.rar.jboss.JBossRAMetaData;
import org.jboss.metadata.rar.spec.ConnectorMetaData;
import org.jboss.metadata.rar.spec.JCA15DTDMetaData;
import org.jboss.metadata.rar.spec.JCA15MetaData;
import org.jboss.metadata.rar.spec.JCA16DefaultNSMetaData;
import org.jboss.metadata.rar.spec.JCA16DTDMetaData;
import org.jboss.metadata.rar.spec.JCA16MetaData;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.resolver.MutableSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;

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
    * @exception Exception Thrown if an error occurs
    */
   public Deployment deploy(File f, ClassLoader parent) throws Exception
   {
      if (f == null || !f.getAbsolutePath().endsWith(".rar"))
         return null;

      log.info("Deploying: " + f.getAbsolutePath());

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

      URL[] urls = getUrls(root);
      URLClassLoader cl = new URLClassLoader(urls, parent);

      ConnectorMetaData cmd = getStandardMetaData(root);
      JBossRAMetaData jrmd = getJBossMetaData(root);

      return new RADeployment(f.getName(), cl);
   }

   /**
    * Get the JCA standard metadata
    * @param root The root of the deployment
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   private ConnectorMetaData getStandardMetaData(File root) throws Exception
   {
      ConnectorMetaData result = null;

      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();

      MutableSchemaResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
      resolver.mapLocationToClass("http://java.sun.com/xml/ns/j2ee/connector_1_6.xsd", JCA16MetaData.class);
      resolver.mapLocationToClass("http://java.sun.com/xml/ns/j2ee/connector_1_5.xsd", JCA15MetaData.class);
      resolver.mapLocationToClass("connector_1_5.dtd", JCA15DTDMetaData.class);
      resolver.mapLocationToClass("connector_1_6.dtd", JCA16DTDMetaData.class);
      resolver.mapLocationToClass("connector", JCA16DefaultNSMetaData.class);

      File metadataFile = new File(root, "/META-INF/ra.xml");

      if (metadataFile.exists())
      {
         String url = metadataFile.getAbsolutePath();
         try
         {
            long start = System.currentTimeMillis();

            result = (ConnectorMetaData)unmarshaller.unmarshal(url, resolver);
            
            log.debug("Total parse for " + url + " took " + (System.currentTimeMillis() - start) + "ms");

            if (trace)
            {
               log.trace("successful parse " + result.getVersion() + " rar package " + result);
            }
               
         }
         catch (Exception e)
         {
            log.error("Error during parsing: " + url, e);
            throw e;
         }
      }
      
      return result;
   }

   /**
    * Get the JBoss specific metadata
    * @param root The root of the deployment
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   private JBossRAMetaData getJBossMetaData(File root) throws Exception
   {
      JBossRAMetaData result = null;

      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();

      MutableSchemaResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
      resolver.mapLocationToClass("http://www.jboss.org/schema/jboss-ra_1_0.xsd", JBossRA10MetaData.class);
      resolver.mapLocationToClass("jboss-ra", JBossRA10DefaultNSMetaData.class);

      File metadataFile = new File(root, "/META-INF/jboss-ra.xml");

      if (metadataFile.exists())
      {
         String url = metadataFile.getAbsolutePath();
         try
         {
            long start = System.currentTimeMillis();

            result = (JBossRAMetaData)unmarshaller.unmarshal(url, resolver);
            
            log.debug("Total parse for " + url + " took " + (System.currentTimeMillis() - start) + "ms");

            if (trace)
               log.trace(result);
         }
         catch (Exception e)
         {
            log.error("Error during parsing: " + url, e);
            throw e;
         }
      }
      
      return result;
   }

   /**
    * Get the URLs for the directory and all libraries located in the directory
    * @param directrory The directory
    * @return The URLs
    * @exception MalformedURLException MalformedURLException
    * @exception IOException IOException
    */
   private static URL[] getUrls(File directory) throws MalformedURLException, IOException
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
