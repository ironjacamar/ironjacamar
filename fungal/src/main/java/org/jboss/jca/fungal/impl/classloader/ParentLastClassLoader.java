/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.fungal.impl.classloader;

import org.jboss.jca.fungal.api.KernelClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Parent last class loader
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ParentLastClassLoader extends KernelClassLoader
{
   /** Local URLClassLoader */
   private URLClassLoader children;

   /**
    * Constructor
    * @param urls The URLs for JAR archives or directories
    * @param parent The parent class loader
    */
   public ParentLastClassLoader(URL[] urls, ClassLoader parent)
   {
      super(new URL[0], parent);

      this.children = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      Class<?> result = super.loadClass(name);

      if (result != null)
         return result;

      try
      {
         return children.loadClass(name);
      }
      catch (ClassNotFoundException cnfe)
      {
         // Default to parent
      }

      return loadClass(name, false);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public URL getResource(String name)
   {
      URL resource = children.getResource(name);

      if (resource != null)
         return resource;

      return super.getResource(name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public InputStream getResourceAsStream(String name)
   {
      InputStream is = children.getResourceAsStream(name);

      if (is != null)
         return is;

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

      Enumeration<URL> e = children.getResources(name);

      if (e != null)
      {
         while (e.hasMoreElements())
         {
            v.add(e.nextElement());
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

   /**
    * {@inheritDoc}
    */
   @Override 
   public void clearAssertionStatus()
   {
      super.clearAssertionStatus();
      children.clearAssertionStatus();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setClassAssertionStatus(String className, boolean enabled)
   {
      children.setClassAssertionStatus(className, enabled);
      super.setClassAssertionStatus(className, enabled);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setDefaultAssertionStatus(boolean enabled)
   {
      children.setDefaultAssertionStatus(enabled);
      super.setDefaultAssertionStatus(enabled);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setPackageAssertionStatus(String packageName, boolean enabled)
   {
      children.setPackageAssertionStatus(packageName, enabled);
      super.setPackageAssertionStatus(packageName, enabled);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public URL[] getURLs()
   {
      List<URL> result = null;

      URL[] urls = children.getURLs();

      if (urls != null)
      {
         result = new ArrayList<URL>(urls.length);
         for (URL u : urls)
         {
            result.add(u);
         }
      }

      urls = super.getURLs();

      if (urls != null)
      {
         for (URL u : urls)
         {
            result.add(u);
         }
      }

      if (result == null)
         return new URL[0];

      return result.toArray(new URL[result.size()]);
   }
}
