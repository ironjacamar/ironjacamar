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

package org.jboss.jca.fungal.api;

import java.lang.reflect.Constructor;
import java.net.URL;

/**
 * Class loader factory
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ClassLoaderFactory
{
   /** Type: Parent first */
   public static final int TYPE_PARENT_FIRST = 0;

   /** Type: Parent last */
   public static final int TYPE_PARENT_LAST = 1;

   /** Type: Export */
   public static final int TYPE_EXPORT = 2;

   /** ClassLoader: Parent first */
   private static final String CLASSLOADER_PARENT_FIRST = 
      "org.jboss.jca.fungal.impl.classloader.ParentFirstClassLoader";

   /** ClassLoader: Parent last */
   private static final String CLASSLOADER_PARENT_LAST =
      "org.jboss.jca.fungal.impl.classloader.ParentLastClassLoader";

   /** ClassLoader: Export */
   private static final String CLASSLOADER_EXPORT =
      "org.jboss.jca.fungal.impl.classloader.ExportClassLoader";

   /**
    * Constructor
    */
   private ClassLoaderFactory()
   {
   }

   /**
    * Create a class loader
    * @param type The class loader type
    * @param urls The resource URLs
    * @param parent The parent class loader
    * @return The kernel class loader
    * @exception IllegalArgumentException Thrown if unknown type is passed
    * @exception IllegalStateException Thrown if a classloader can't be created
    */
   public static synchronized KernelClassLoader create(int type, URL[] urls, ClassLoader parent)
      throws IllegalArgumentException, IllegalStateException
   {
      if (type == TYPE_PARENT_FIRST)
      {
         try
         {
            Class<?> clz = Class.forName(CLASSLOADER_PARENT_FIRST, true, ClassLoaderFactory.class.getClassLoader());
            Constructor<?> constructor = clz.getDeclaredConstructor(URL[].class, ClassLoader.class);

            return (KernelClassLoader)constructor.newInstance(urls, parent);
         }
         catch (Throwable t)
         {
            throw new IllegalStateException("Unable to create parent first classloader", t);
         }
      }
      else if (type == TYPE_PARENT_LAST)
      {
         try
         {
            Class<?> clz = Class.forName(CLASSLOADER_PARENT_LAST, true, ClassLoaderFactory.class.getClassLoader());
            Constructor<?> constructor = clz.getDeclaredConstructor(URL[].class, ClassLoader.class);

            return (KernelClassLoader)constructor.newInstance(urls, parent);
         }
         catch (Throwable t)
         {
            throw new IllegalStateException("Unable to create parent last classloader", t);
         }
      }
      else if (type == TYPE_EXPORT)
      {
         try
         {
            Class<?> clz = Class.forName(CLASSLOADER_EXPORT, true, ClassLoaderFactory.class.getClassLoader());
            Constructor<?> constructor = clz.getDeclaredConstructor(URL[].class, ClassLoader.class);

            return (KernelClassLoader)constructor.newInstance(urls, parent);
         }
         catch (Throwable t)
         {
            throw new IllegalStateException("Unable to create export classloader", t);
         }
      }

      throw new IllegalArgumentException("Unknown type: " + type);
   }
}
