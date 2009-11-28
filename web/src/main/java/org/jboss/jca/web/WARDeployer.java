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

import org.jboss.jca.fungal.deployers.DeployException;
import org.jboss.jca.fungal.deployers.Deployer;
import org.jboss.jca.fungal.deployers.Deployment;
import org.jboss.jca.fungal.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jboss.logging.Logger;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;


/**
 * The WAR deployer for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WARDeployer implements Deployer
{
   private static Logger log = Logger.getLogger(WARDeployer.class);
   private static boolean trace = log.isTraceEnabled();

   private Server server;
   
   /**
    * Constructor
    */
   public WARDeployer()
   {
      this.server = null;
   }

   /**
    * Get the server
    * @return The server
    */
   public Server getServer()
   {
      return server;
   }

   /**
    * Set the server
    * @param server The server
    */
   public void setServer(Server server)
   {
      this.server = server;
   }

   /**
    * Deploy
    * @param url The url
    * @param parent The parent classloader
    * @return The deployment
    * @exception DeployException Thrown if an error occurs during deployment
    */
   public synchronized Deployment deploy(URL url, ClassLoader parent) throws DeployException
   {
      if (url == null || !(url.toExternalForm().endsWith(".war") || url.toExternalForm().endsWith(".war/")))
         return null;

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

         // Setup temporary work directory
         File tmp = new File(SecurityActions.getSystemProperty("jboss.jca.home"), "/tmp/");
         File tmpDeployment = new File(tmp, "/web" + contextPath);

         if (tmpDeployment.exists())
         {
            FileUtil fileUtil = new FileUtil();
            fileUtil.recursiveDelete(tmpDeployment);
         }

         if (!tmpDeployment.mkdirs())
            throw new IOException("Unable to create " + tmpDeployment);

         // Map ROOT.war to /
         if ("/ROOT".equalsIgnoreCase(contextPath))
            contextPath = "/";

         log.debug("ContextPath=" + contextPath);

         WebAppContext webapp = new WebAppContext();
         webapp.setServer(server);
         webapp.setContextPath(contextPath);
         webapp.setWar(url.toString());
         webapp.setTempDirectory(tmpDeployment); 

         server.stop();
         server.addHandler(webapp);
         server.start();

         log.info("Deployed: " + url.toExternalForm());

         return new WARDeployment(url, webapp, parent);
      }
      catch (Exception e)
      {
         throw new DeployException(e.getMessage(), e);
      }
   }
}
