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

package org.jboss.jca.deployers.fungal;

import org.jboss.jca.fungal.deployers.Deployment;
import org.jboss.jca.fungal.util.FileUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;

/**
 * A resource adapter deployment for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RADeployment implements Deployment
{
   private static Logger log = Logger.getLogger(RADeployer.class);

   /** The deployment */
   private URL deployment;

   /** The resource adapter instance */
   private ResourceAdapter ra;

   /** JNDI names for connection factories */
   private List<String> jndiNames;

   /** The temporary directory */
   private File tmpDirectory;

   /** The classloader */
   private ClassLoader cl;

   /**
    * Constructor
    * @param deployment The deployment
    * @param ra The resource adapter instance if present
    * @param jndiNames The JNDI names for connection factories
    * @param tmpDirectory The temporary directory
    * @param cl The classloader for the deployment
    */
   public RADeployment(URL deployment, ResourceAdapter ra, List<String> jndiNames, File tmpDirectory, ClassLoader cl)
   {
      this.deployment = deployment;
      this.ra = ra;
      this.jndiNames = jndiNames;
      this.tmpDirectory = tmpDirectory;
      this.cl = cl;
   }

   /**
    * Get the unique URL for the deployment
    * @return The URL
    */
   public URL getURL()
   {
      return deployment;
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
    * Stop
    */
   public void stop()
   {
      log.debug("Undeploying: " + deployment.toExternalForm());

      if (jndiNames != null)
      {
         Context context = null;
         try
         {
            context = new InitialContext();

            for (String jndiName : jndiNames)
            {
               try
               {
                  Util.unbind(context, jndiName);
               }
               catch (Throwable it)
               {
                  log.warn("Exception during JNDI unbind for: " + jndiName, it);
               }
            }
         }
         catch (Throwable t)
         {
            log.warn("Exception during JNDI initialization", t);
         }
         finally
         {
            if (context != null)
            {
               try
               {
                  context.close();
               }
               catch (NamingException ne)
               {
                  // Ignore
               }
            }
         }
      }

      if (ra != null)
      {
         ra.stop();
         ra = null;
      }
   }

   /**
    * Destroy
    */
   public void destroy()
   {
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
            fu.recursiveDelete(tmpDirectory);
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }

      log.info("Undeployed: " + deployment.toExternalForm());
   }
}
