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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Vector;

import com.github.fungal.api.Kernel;
import com.github.fungal.spi.deployers.Deployment;

/**
 * WAR class loader
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WARClassLoader extends URLClassLoader
{
   /** The kernel */
   private Kernel kernel;

   /**
    * Constructor
    * @param kernel The kernel
    * @param parent The parent class loader
    */
   public WARClassLoader(Kernel kernel, ClassLoader parent)
   {
      super(new URL[0], parent);
      this.kernel = kernel;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      for (Deployment deployment : kernel.getDeployments())
      {
         if (!(deployment instanceof WARDeployment))
         {
            try
            {
               return deployment.getClassLoader().loadClass(name);
            }
            catch (Throwable t)
            {
               // Next
            }
         }
      }

      return super.loadClass(name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public URL getResource(String name)
   {
      URL result = null;

      for (Deployment deployment : kernel.getDeployments())
      {
         if (!(deployment instanceof WARDeployment))
         {
            result = deployment.getClassLoader().getResource(name);

            if (result != null)
               return result;
         }
      }

      return super.getResource(name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public InputStream getResourceAsStream(String name)
   {
      InputStream result = null;

      for (Deployment deployment : kernel.getDeployments())
      {
         if (!(deployment instanceof WARDeployment))
         {
            result = deployment.getClassLoader().getResourceAsStream(name);

            if (result != null)
               return result;
         }
      }

      return super.getResourceAsStream(name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Enumeration<URL> getResources(String name)
      throws IOException
   {
      Vector<URL> v = new Vector<URL>();

      Enumeration<URL> e = null;

      for (Deployment deployment : kernel.getDeployments())
      {
         if (!(deployment instanceof WARDeployment))
         {
            e = deployment.getClassLoader().getResources(name);

            if (e != null)
            {
               while (e.hasMoreElements())
               {
                  v.add(e.nextElement());
               }
            }
         }
      }

      e = super.getResources(name);

      if (e != null)
      {
         while (e.hasMoreElements())
         {
            v.add(e.nextElement());
         }
      }

      return v.elements();
   }
}
