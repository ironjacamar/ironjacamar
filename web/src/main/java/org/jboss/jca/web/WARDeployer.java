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

package org.jboss.jca.web;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jboss.logging.Logger;

import com.github.fungal.api.util.FileUtil;
import com.github.fungal.spi.deployers.Context;
import com.github.fungal.spi.deployers.DeployException;
import com.github.fungal.spi.deployers.Deployer;
import com.github.fungal.spi.deployers.Deployment;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * The WAR deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WARDeployer implements Deployer
{
   private static Logger log = Logger.getLogger(WARDeployer.class);
   private static boolean trace = log.isTraceEnabled();

   private WebServer webServer;
       
   /**
    * Constructor
    */
   public WARDeployer()
   {
      this.webServer = null;
   }

   /**
    * Get the web server
    * @return The server
    */
   public WebServer getWebServer()
   {
      return webServer;
   }

   /**
    * Set the web server
    * @param server The server
    */
   public void setWebServer(WebServer server)
   {
      this.webServer = server;
   }

   /**
    * {@inheritDoc}
    */
   public boolean accepts(URL url)
   {
      if (url == null || !(url.toExternalForm().endsWith(".war") || url.toExternalForm().endsWith(".war/")))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public int getOrder()
   {
      return Integer.MIN_VALUE;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized Deployment deploy(URL url, Context context, ClassLoader parent) throws DeployException
   {
      log.debug("Deploying: " + url.toExternalForm());

      try
      {
         String path = url.toExternalForm();

         // Extract context path based on .war name
         String contextPath = "/";
         if (!path.endsWith("/"))
         {
            contextPath += path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
         }
         else
         {
            int lastIndex = path.lastIndexOf('/');

            int index = path.indexOf('/');
            boolean done = false;

            while (!done)
            {
               if (index + 1 <= path.length())
               {
                  int nextIndex = path.indexOf('/', index + 1);
                  if (nextIndex == lastIndex)
                  {
                     done = true;
                  }
                  else
                  {
                     index = nextIndex;
                  }
               }
               else
               {
                  done = true;
               }
            }

            contextPath += path.substring(index + 1, path.lastIndexOf("."));
         }

         String tmpPath = "/web";
         // Map ROOT.war to /
         if ("/ROOT".equalsIgnoreCase(contextPath))
         {
            contextPath = "/";
            tmpPath += "/root"; 
         }
         else
         {
            tmpPath += contextPath;
         }

         // Setup temporary work directory
         File tmp = new File(SecurityActions.getSystemProperty("iron.jacamar.home"), "/tmp/");
         File tmpDeployment = new File(tmp, tmpPath);

         if (tmpDeployment.exists())
         {
            FileUtil fileUtil = new FileUtil();
            fileUtil.delete(tmpDeployment);
         }

         if (!tmpDeployment.mkdirs())
            throw new IOException("Unable to create " + tmpDeployment);

         log.debug("ContextPath=" + contextPath);
         log.debug("TmpPath=" + tmpPath);

         WebAppContext webapp = new WebAppContext();
         webapp.setContextPath(contextPath);
         webapp.setWar(url.toString());
         webapp.setTempDirectory(tmpDeployment);

         ClassLoader webappCL = new WARClassLoader(context.getKernel(), parent);
         webapp.setClassLoader(new WebAppClassLoader(webappCL, webapp));

         webServer.addHandler(webapp);

         log.info("Deployed: " + url.toExternalForm());

         return new WARDeployment(url, webapp, tmpDeployment, webappCL);
      }
      catch (Throwable t)
      {
         throw new DeployException(t.getMessage(), t);
      }
   }
}
