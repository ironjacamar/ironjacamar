/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.web;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jboss.logging.Logger;

import com.github.fungal.api.util.FileUtil;
import com.github.fungal.spi.deployers.Deployment;

import org.eclipse.jetty.server.Handler;

/**
 * A web archive deployment for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WARDeployment implements Deployment
{
   private static Logger log = Logger.getLogger(WARDeployer.class);

   /** The web archiive */
   private URL archive;

   /** The handler */
   private Handler handler;

   /** The temporary directory */
   private File tmpDirectory;

   /** The classloader */
   private ClassLoader cl;

   /**
    * Constructor
    * @param archive The archive
    * @param handler The handler
    * @param tmpDirectory The temporary directory
    * @param cl The classloader for the deployment
    */
   public WARDeployment(URL archive, Handler handler, File tmpDirectory, ClassLoader cl)
   {
      this.archive = archive;
      this.handler = handler;
      this.tmpDirectory = tmpDirectory;
      this.cl = cl;
   }

   /**
    * Get the unique URL for the deployment
    * @return The URL
    */
   public URL getURL()
   {
      return archive;
   }

   /**
    * Get the classloader
    * @return The classloader
    */
   public ClassLoader getClassLoader()
   {
      return cl;
   }

   /**
    * Destroy
    */
   public void destroy()
   {
      if (log.isDebugEnabled())
      {
         log.debug("Undeploying: " + archive.toExternalForm());
      }

      /*
        We let Jetty handle this during server.stop()
      if (handler != null)
      {
         try
         {
            handler.stop();
         }
         catch (Throwable t)
         {
            // Ignore
         }

         handler.destroy();
         handler = null;
      }
      */

      if (cl != null && cl instanceof Closeable)
      {
         try
         {
            ((Closeable)cl).close();
         }
         catch (IOException ioe)
         {
            // Swallow
         }
      }

      if (tmpDirectory != null && tmpDirectory.exists())
      {
         try
         {
            FileUtil fu = new FileUtil();
            fu.delete(tmpDirectory);
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }

      log.info("Undeployed: " + archive.toExternalForm());
   }
}
