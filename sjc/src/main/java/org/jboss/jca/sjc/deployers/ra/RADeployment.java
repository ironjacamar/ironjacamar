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

import org.jboss.jca.sjc.deployers.Deployment;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.jboss.logging.Logger;

/**
 * A resource adapter deployment for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RADeployment implements Deployment
{
   private static Logger log = Logger.getLogger(RADeployer.class);
   private static boolean trace = log.isTraceEnabled();

   /** The resource adapter file */
   private File adapter;

   /** The classloader */
   private ClassLoader cl;

   /**
    * Constructor
    * @param adapter The adapter
    * @param cl The classloader for the deployment
    */
   public RADeployment(File adapter, ClassLoader cl)
   {
      this.adapter = adapter;
      this.cl = cl;
   }

   /**
    * Get the name
    * @return The name
    */
   public String getName()
   {
      return adapter.getName();
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
      log.info("Undeploying: " + adapter.getAbsolutePath());

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
   }
}
