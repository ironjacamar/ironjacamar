/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.workmanager;

import org.jboss.jca.core.CoreLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.jboss.logging.Logger;

/**
 * Work class loader
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WorkClassLoader extends ClassLoader
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, WorkClassLoader.class.getName());

   /** The resource adapter class loader */
   private ResourceAdapterClassLoader resourceAdapterClassLoader;

   /**
    * Constructor
    * @param cb The class bundle
    */
   public WorkClassLoader(ClassBundle cb)
   {
      this(cb, null);
   }

   /**
    * Constructor
    * @param cb The class bundle
    * @param resourceAdapterClassLoader The resource adapter class loader
    */
   public WorkClassLoader(ClassBundle cb, ResourceAdapterClassLoader resourceAdapterClassLoader)
   {
      super(SecurityActions.getClassLoader(WorkClassLoader.class));

      List<Class<?>> classes = new ArrayList<Class<?>>(cb.getDefinitions().size());
      final boolean trace = log.isTraceEnabled();
      for (ClassDefinition cd : cb.getDefinitions())
      {
         if (trace)
            log.tracef("%s: Defining class=%s", Integer.toHexString(System.identityHashCode(this)), cd.getName());

         Class<?> c = defineClass(cd.getName(), cd.getData(), 0, cd.getData().length);
         classes.add(c);
      }

      for (Class<?> c : classes)
      {
         if (trace)
            log.tracef("%s: Resolving class=%s", Integer.toHexString(System.identityHashCode(this)), c.getName());

         resolveClass(c);
      }

      this.resourceAdapterClassLoader = resourceAdapterClassLoader;
   }

   /**
    * Set the resource adapter class loader
    * @param v The value
    */
   public void setResourceAdapterClassLoader(ResourceAdapterClassLoader v)
   {
      if (log.isTraceEnabled())
         log.tracef("%s: setResourceAdapterClassLoader(%s)", Integer.toHexString(System.identityHashCode(this)), v);

      resourceAdapterClassLoader = v;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      if (log.isTraceEnabled())
         log.tracef("%s: loadClass(%s)", Integer.toHexString(System.identityHashCode(this)), name);

      Class<?> result = super.loadClass(name);

      if (result != null)
         return result;

      if (resourceAdapterClassLoader != null)
      {
         try
         {
            return resourceAdapterClassLoader.loadClass(name);
         }
         catch (ClassNotFoundException cnfe)
         {
            // Default to parent
         }
         catch (NoClassDefFoundError ncdfe)
         {
            // Default to parent
         }
      }

      return loadClass(name, false);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
   {
      return super.loadClass(name, resolve);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> findClass(String name) throws ClassNotFoundException
   {
      if (log.isTraceEnabled())
         log.tracef("%s: findClass(%s)", Integer.toHexString(System.identityHashCode(this)), name);

      if (resourceAdapterClassLoader != null)
      {
         try
         {
            return resourceAdapterClassLoader.findClass(name);
         }
         catch (Throwable t)
         {
            // Default to parent
         }
      }

      return super.findClass(name);
   }

   /**
    * Lookup a class
    * @param name The fully qualified class name
    * @return The class
    * @exception ClassNotFoundException Thrown if the class can't be found
    */
   Class<?> lookup(String name) throws ClassNotFoundException
   {
      return super.findClass(name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public URL getResource(String name)
   {
      URL resource = null;

      if (resourceAdapterClassLoader != null)
         resource = resourceAdapterClassLoader.getResource(name);

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
      InputStream is = null;

      if (resourceAdapterClassLoader != null)
         is = resourceAdapterClassLoader.getResourceAsStream(name);

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

      Enumeration<URL> e = null;

      if (resourceAdapterClassLoader != null)
         e = resourceAdapterClassLoader.getResources(name);

      if (e != null)
      {
         while (e.hasMoreElements())
         {
            v.add(e.nextElement());
         }
      }

      e = super.getResources(name);
      while (e.hasMoreElements())
      {
         v.add(e.nextElement());
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

      if (resourceAdapterClassLoader != null)
         resourceAdapterClassLoader.clearAssertionStatus();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setClassAssertionStatus(String className, boolean enabled)
   {
      if (resourceAdapterClassLoader != null)
         resourceAdapterClassLoader.setClassAssertionStatus(className, enabled);

      super.setClassAssertionStatus(className, enabled);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setDefaultAssertionStatus(boolean enabled)
   {
      if (resourceAdapterClassLoader != null)
         resourceAdapterClassLoader.setDefaultAssertionStatus(enabled);

      super.setDefaultAssertionStatus(enabled);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setPackageAssertionStatus(String packageName, boolean enabled)
   {
      if (resourceAdapterClassLoader != null)
         resourceAdapterClassLoader.setPackageAssertionStatus(packageName, enabled);

      super.setPackageAssertionStatus(packageName, enabled);
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("WorkClassLoader@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[parent=").append(getParent());
      sb.append(" resourceAdapterClassLoader=").append(resourceAdapterClassLoader);
      sb.append("]");

      return sb.toString();
   }
}
