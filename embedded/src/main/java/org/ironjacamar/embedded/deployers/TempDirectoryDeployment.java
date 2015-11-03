/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.embedded.deployers;

import java.io.File;
import java.net.URL;

import org.jboss.logging.Logger;

import com.github.fungal.api.util.FileUtil;
import com.github.fungal.spi.deployers.Deployment;

/**
 * A temp directory deployment
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TempDirectoryDeployment implements Deployment
{
   /** The logger */
   private static Logger log = Logger.getLogger(TempDirectoryDeployment.class);
   
   /** The deployment */
   private URL deployment;

   /** The path */
   private File path;

   /**
    * Constructor
    * @param deployment The deployment url
    * @param path The path
    */
   public TempDirectoryDeployment(URL deployment, File path)
   {
      this.deployment = deployment;
      this.path = path;
   }

   /**
    * {@inheritDoc}
    */
   public URL getURL()
   {
      return deployment;
   }

   /**
    * {@inheritDoc}
    */
   public ClassLoader getClassLoader()
   {
      return getClass().getClassLoader();
   }

   /**
    * Stop
    */
   public void stop()
   {
      if (path != null)
      {
         try
         {
            FileUtil fu = new FileUtil();
            fu.delete(path);
         }
         catch (Throwable t)
         {
            log.warn("Exception while deleting extracted deployment", t);
         }
      }
   }
}
