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

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Archive class loader
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
class ArchiveClassLoader extends KernelClassLoader
{
   /** Class loader id */
   private Integer id;

   /** Export packages */
   private Set<String> exportPackages;

   /** Import classloaders */
   private Set<Integer> importClassLoaders;

   /**
    * Constructor
    * @param id The class loader id
    * @param url The URL for JAR archive or directory
    * @param exportPackages The export packages
    */
   ArchiveClassLoader(Integer id, URL url, Set<String> exportPackages)
   {
      super(new URL[] {url}, ClassLoader.getSystemClassLoader());

      this.id = id;
      this.exportPackages = exportPackages;
   }

   /**
    * Get the identifier
    * @return The id
    */
   Integer getId()
   {
      return id;
   }

   /**
    * Add an import classloader
    * @param id The identifier
    */
   void addImportClassLoader(Integer id)
   {
      if (importClassLoaders == null)
         importClassLoaders = new HashSet<Integer>(1);

      importClassLoaders.add(id);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      // Don't call super.loadClass(String) as it is done in ExportClassLoader

      if (isClassRegistered(name))
      {
         return loadClass(name, false);
      }
      else
      {
         try
         {
            return Class.forName(name, true, ClassLoader.getSystemClassLoader());
         }
         catch (ClassNotFoundException cnfe)
         {
            ExportClassLoaderRepository eclr = ExportClassLoaderRepository.getInstance();
            if (importClassLoaders != null)
            {
               for (Integer id : importClassLoaders)
               {
                  ArchiveClassLoader acl = eclr.getClassLoader(id);

                  if (acl != null)
                  {
                     try
                     {
                        return acl.lookup(name);
                     }
                     catch (ClassNotFoundException ignore)
                     {
                        // Ignore
                     }
                  }
               }
            }

            return eclr.getNonExportClassLoader().lookup(name);
         }
      }
   }

   /**
    * Lookup a class
    * @param name The fully qualified class name
    * @return The class
    * @throws ClassNotFoundException If the class could not be found 
    */
   public Class<?> lookup(String name) throws ClassNotFoundException
   {
      return loadClass(name, false);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> findClass(String name) throws ClassNotFoundException
   {
      if (isClassRegistered(name))
         return findClass(name, true);

      throw new ClassNotFoundException("Unable to load class: " + name);
   }

   /**
    * Find a class
    * @param name The fully qualified class name
    * @param fullScan Should a full be performed
    * @return The class
    * @exception ClassNotFoundException Thrown if the class couldn't be found
    */
   public Class<?> findClass(String name, boolean fullScan) throws ClassNotFoundException
   {
      try
      {
         return super.findClass(name);
      }
      catch (Throwable t)
      {
         if (fullScan && importClassLoaders != null)
         {
            ExportClassLoaderRepository eclr = ExportClassLoaderRepository.getInstance();
            for (Integer id : importClassLoaders)
            {
               ArchiveClassLoader acl = eclr.getClassLoader(id);

               if (acl != null)
               {
                  try
                  {
                     return acl.findClass(name, false);
                  }
                  catch (ClassNotFoundException ignore)
                  {
                     // Ignore
                  }
               }
            }
         }
      }
      throw new ClassNotFoundException("Unable to load class: " + name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setClassAssertionStatus(String className, boolean enabled)
   {
      if (isClassRegistered(className))
         super.setClassAssertionStatus(className, enabled);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void setPackageAssertionStatus(String packageName, boolean enabled)
   {
      if (isPackageRegistered(packageName))
         super.setPackageAssertionStatus(packageName, enabled);
   }

   /**
    * Is the class registered for this class loader
    * @param name The fully qualified class name
    * @return True if registered; otherwise false
    */
   private boolean isClassRegistered(String name)
   {
      String packageName = "";
      int lastDot = name.lastIndexOf(".");

      if (lastDot != -1)
         packageName = name.substring(0, lastDot);

      return exportPackages.contains(packageName);
   }

   /**
    * Is the package registered for this class loader
    * @param name The package name
    * @return True if registered; otherwise false
    */
   private boolean isPackageRegistered(String name)
   {
      return exportPackages.contains(name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb = sb.append(ArchiveClassLoader.class.getName());
      sb = sb.append("{");

      sb = sb.append("Id=");
      sb = sb.append(id);
      sb = sb.append(",");

      sb = sb.append("Url=");
      sb = sb.append(super.getURLs()[0]);
      sb = sb.append(",");

      sb = sb.append("ExportPackages=");
      sb = sb.append(exportPackages);
      sb = sb.append(",");
      
      sb = sb.append("ImportClassLoaders=");
      sb = sb.append(importClassLoaders);

      sb = sb.append("}");

      return sb.toString();
   }
}
