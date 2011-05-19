/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.deployers.spec;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

/**
 *
 * A ArquillianJCATestUtils.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public final class ArquillianJCATestUtils
{

   /**
    * Build a shrinkwrap rar adding all necessary classes
    *
    * @param archiveName the archhive name
    * @param packageName the package name
    * @return the shrinkwrapped rar
    * @throws Exception in case of error creating the archive
    */
   public static ResourceAdapterArchive buidShrinkwrapRa(String archiveName, String packageName) throws Exception
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, archiveName);

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addClasses(getClasses(packageName));

      raa.addAsLibrary(ja);

      return raa;
   }

   /**
    * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
    *
    * @param packageName The base package
    * @return The classes
    * @throws ClassNotFoundException
    * @throws IOException
    */
   private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String path = packageName.replace('.', '/');
      Enumeration<URL> resources = classLoader.getResources(path);
      List<File> dirs = new ArrayList<File>();
      while (resources.hasMoreElements())
      {
         URL resource = resources.nextElement();
         dirs.add(new File(resource.getFile()));
      }
      ArrayList<Class> classes = new ArrayList<Class>();
      for (File directory : dirs)
      {
         classes.addAll(findClasses(directory, packageName));
      }
      return classes.toArray(new Class[classes.size()]);
   }

   /**
    * Recursive method used to find all classes in a given directory and subdirs.
    *
    * @param directory   The base directory
    * @param packageName The package name for classes found inside the base directory
    * @return The classes
    * @throws ClassNotFoundException
    */
   private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException
   {
      List<Class> classes = new ArrayList<Class>();
      if (!directory.exists())
      {
         return classes;
      }
      File[] files = directory.listFiles();
      for (File file : files)
      {
         if (file.isDirectory())
         {
            assert !file.getName().contains(".");
            classes.addAll(findClasses(file, packageName + "." + file.getName()));
         }
         else if (file.getName().endsWith(".class"))
         {
            classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
         }
      }
      return classes;
   }
}
