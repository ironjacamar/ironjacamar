/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.rhq.embed.core;

import org.jboss.jca.core.api.management.ManagementRepository;

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.jca.rhq.core.Discover;
import org.jboss.jca.rhq.core.Lifecycle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.logging.Logger;

/**
 * EmbeddedJcaDiscover
 * Discover implement by embedded JCA
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 */
public class EmbeddedJcaDiscover implements Discover, Lifecycle
{
   /** log */
   private static final Logger logger = Logger.getLogger(EmbeddedJcaDiscover.class);
   
   /** instance of EmbeddedRHQ */
   private static EmbeddedJcaDiscover instance;
   
   /** embedJCA */
   private Embedded embedJCA;

   /** stopped */
   private boolean stopped = true;
   
   /** ManagementRepository */
   ManagementRepository mr = null;
   
   /** 
    * singleton getInstance
    * 
    * @return EmbeddedJCAContainer EmbeddedJCAContainer
    */
   public static synchronized EmbeddedJcaDiscover getInstance()
   {
      if (null == instance)
      {
         instance = new EmbeddedJcaDiscover();
      }
      return instance;
   }
   
   /** 
    * default constructor
    */
   private EmbeddedJcaDiscover()
   {
   }
   
   /** 
    * start jca container
    */
   @Override
   public void start()
   {
      try
      {
         embedJCA = EmbeddedFactory.create(true);
         embedJCA.startup();
         logger.info("embedded JCA container started");
         
         //embedJCA.deploy(EmbeddedJcaDiscover.class.getResource("h2-ds.xml"));
         
         deployFile("/xa.rar");
         logger.debug("xa.rar deployed");
         
         stopped = false;
      }
      catch (Throwable e)
      {
         throw new IllegalStateException("Something wrong when starting Embedded JCA container", e);
      }

   }
   
   /** 
    * deploy file into container
    * 
    * @param fileName file name
    */
   private void deployFile(String fileName)
   {
      URL url = EmbeddedJcaDiscover.class.getResource(fileName);
      try
      {
         String tmpPath = System.getProperty("java.io.tmpdir");
         File outputFile = new File(tmpPath, fileName);
         copyURLToFile(url, outputFile);
         URL finalURL = outputFile.toURI().toURL();
         embedJCA.deploy(finalURL);
         outputFile.deleteOnExit();
      }
      catch (Throwable e)
      {
         throw new IllegalStateException("Can not deploy resource: " + url, e);
      }
   }
   
   /** 
    * copyURLToFile
    * @param url URL
    * @param outputFile File
    * @throws Exception
    */
   private void copyURLToFile(URL url, File outputFile) throws Exception
   {
      InputStream from = null;
      FileOutputStream to = null;
      try
      {
         from = url.openStream();
         to = new FileOutputStream(outputFile);
         byte[] buffer = new byte[4096];
         int bytesRead;

         while ((bytesRead = from.read(buffer)) != -1)
            to.write(buffer, 0, bytesRead);
      }
      finally
      {
         if (from != null)
            try
            {
               from.close();
            }
            catch (IOException e)
            {
               ;
            }
         if (to != null)
            try
            {
               to.close();
            }
            catch (IOException e)
            {
               ;
            }
      }
   }
   
   /** 
    * getManagementRepository
    * 
    * @return ManagementRepository 
    */
   @Override
   public synchronized ManagementRepository getManagementRepository()
   {
      if (mr != null)
         return mr;

      try
      {
         if (stopped)
         {
            return null;
         }
         mr = embedJCA.lookup("ManagementRepository", ManagementRepository.class);
         return mr;
      }
      catch (Throwable e)
      {
         throw new IllegalStateException("Can not get the beanManagementRepository", e);
      }
   }
   
   /**
    * stop the embedded jca container.
    */
   @Override
   public void stop()
   {
      try
      {
         embedJCA.shutdown();
         mr = null;
         stopped = true;
      }
      catch (Throwable e)
      {
         throw new IllegalStateException("Can not shutdown the Embedded JCA container", e);
      }
   }
   /**
    * Get plugin environment
    * 
    * @return String in which container
    */
   @Override
   public String getPluginEnv()
   {
      return Discover.EMBEDDED;
   }
}
