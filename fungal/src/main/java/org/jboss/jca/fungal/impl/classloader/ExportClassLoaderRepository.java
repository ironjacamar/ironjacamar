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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Export class loader repository
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
class ExportClassLoaderRepository
{
   /** Instance */
   private static final ExportClassLoaderRepository INSTANCE = new ExportClassLoaderRepository();

   /** Nob export class loader */
   private static NonExportClassLoader nonExportClassLoader;

   /** Id counter */
   private static AtomicInteger idCounter;

   /** Class loaders */
   private static ConcurrentMap<Integer, ArchiveClassLoader> classLoaders;

   /** Package + Version mapping */
   private static ConcurrentMap<String, SortedMap<String, Set<Integer>>> packages;

   /**
    * Constructor
    */
   private ExportClassLoaderRepository()
   {
      nonExportClassLoader = new NonExportClassLoader();
      idCounter = new AtomicInteger(0);
      classLoaders = new ConcurrentHashMap<Integer, ArchiveClassLoader>();
      packages = new ConcurrentHashMap<String, SortedMap<String, Set<Integer>>>();
   }

   /**
    * Get the singleton
    * @return The instance
    */
   static ExportClassLoaderRepository getInstance()
   {
      return INSTANCE;
   }

   /**
    * Get the non export class loader
    * @return The class loader
    */
   NonExportClassLoader getNonExportClassLoader()
   {
      return nonExportClassLoader;
   }

   /**
    * Get a class loader
    * @param id The identifier
    * @return The class loader
    */
   ArchiveClassLoader getClassLoader(Integer id)
   {
      return classLoaders.get(id);
   }

   /**
    * Get archive class loaders
    * @param clz The fully qualified class name
    * @return The class loader ids; <code>null</code> if no archive class loaders were found
    */
   Set<Integer> getClassLoaders(String clz)
   {
      String pkgName = getPackageName(clz);

      SortedMap<String, Set<Integer>> sm = packages.get(pkgName);

      if (sm != null)
      {
         String lastKey = sm.lastKey();
         return sm.get(lastKey);
      }

      return null;
   }

   /**
    * Get archive class loaders
    * @return The class loaders
    */
   Set<Integer> getClassLoaders()
   {
      Set<Integer> result = new HashSet<Integer>();

      Collection<SortedMap<String, Set<Integer>>> sms = packages.values();

      for (SortedMap<String, Set<Integer>> sm : sms)
      {
         String lastKey = sm.lastKey();
         Set<Integer> values = sm.get(lastKey);

         if (values != null)
            result.addAll(values);
      }

      return result;
   }

   /**
    * Register
    * @param urls The urls
    * @return The identifiers for the classloaders; <code>null</code> if <code>NonExportClassLoader</code> is used
    */
   synchronized Set<Integer> register(URL[] urls)
   {
      if (urls == null)
         return null;

      Set<Integer> result = null;

      // Classloader id -> Version, Packages
      Map<Integer, Map<String, List<String>>> imports = new HashMap<Integer, Map<String, List<String>>>();

      for (URL url : urls)
      {
         boolean added = false;

         if ("file".equals(url.getProtocol()))
         {
            JarFile jarFile = null;
            try
            {
               File f = new File(url.toURI());

               if (f.isFile())
               {
                  jarFile = new JarFile(f);

                  Manifest manifest = jarFile.getManifest();
                  if (manifest != null)
                  {
                     Attributes mainAttributes = manifest.getMainAttributes();

                     String bundleManifestVersion = mainAttributes.getValue("Bundle-ManifestVersion");
                     if (bundleManifestVersion != null)
                     {
                        Integer identifier = Integer.valueOf(idCounter.getAndIncrement());
                        String bundleVersion = mainAttributes.getValue("Bundle-Version");

                        // Export-Package
                        String input = mainAttributes.getValue("Export-Package");

                        boolean semi = false;
                        boolean quote = false;
                        StringBuilder sb = new StringBuilder();

                        Set<String> exportPackages = new HashSet<String>(1);

                        for (int i = 0; i < input.length(); i++)
                        {
                           char c = input.charAt(i);

                           if (c == ',')
                           {
                              if (!quote)
                              {
                                 String exportPackage = sb.toString().trim();

                                 semi = false;
                                 quote = false;
                                 sb = new StringBuilder();

                                 if (!exportPackage.equals(""))
                                 {
                                    exportPackages.add(exportPackage);
                                 }
                              }
                           }
                           else if (c == ';')
                           {
                              semi = true;
                           }
                           else if (c == '\"')
                           {
                              quote = !quote;
                           }
                           else
                           {
                              if (!semi)
                                 sb = sb.append(c);
                           }
                        }

                        // Import-Package
                        input = mainAttributes.getValue("Import-Package");

                        if (input != null)
                        {
                           StringTokenizer st = new StringTokenizer(input, ",");
                           while (st.hasMoreTokens())
                           {
                              String token = st.nextToken().trim();

                              int versionIndex = token.indexOf(";version=");
                              if (versionIndex != -1)
                              {
                                 String pkg = token.substring(0, versionIndex);
                                 String ver = token.substring(versionIndex + 9);
                           
                                 if (pkg.indexOf(";") != -1)
                                    pkg = pkg.substring(0, pkg.indexOf(";"));

                                 if (ver.startsWith("\""))
                                    ver = ver.substring(1);

                                 if (ver.endsWith("\""))
                                    ver = ver.substring(0, ver.length() - 1);

                                 Map<String, List<String>> value = imports.get(identifier);
                           
                                 if (value == null)
                                    value = new HashMap<String, List<String>>();
                              
                                 List<String> l = value.get(ver);

                                 if (l == null)
                                    l = new ArrayList<String>();
                              
                                 l.add(pkg);
                                 value.put(ver, l);
                                 imports.put(identifier, value);
                              }
                           }
                        }
                        // Require-Bundle
                        input = mainAttributes.getValue("Require-Bundle");

                        if (input != null)
                        {
                           StringTokenizer st = new StringTokenizer(input, ",");
                           while (st.hasMoreTokens())
                           {
                              String token = st.nextToken().trim();
                              
                              int delimiter = token.indexOf(";");

                              if (delimiter != -1)
                                 token = token.substring(0, delimiter);

                              Map<String, List<String>> value = imports.get(identifier);
                           
                              if (value == null)
                                 value = new HashMap<String, List<String>>();
                              
                              List<String> l = value.get(null);

                              if (l == null)
                                 l = new ArrayList<String>();
                              
                              l.add(token);
                              value.put(null, l);
                              imports.put(identifier, value);
                           }
                        }
                     
                        for (String s : exportPackages)
                        {
                           SortedMap<String, Set<Integer>> sm = packages.get(s);

                           if (sm == null)
                              sm = new TreeMap<String, Set<Integer>>(new VersionComparator());

                           Set<Integer> cls = sm.get(bundleVersion);

                           if (cls == null)
                              cls = new HashSet<Integer>(1);
                           
                           cls.add(identifier);
                           sm.put(bundleVersion, cls);
                           packages.put(s, sm);
                        }
                     
                        ArchiveClassLoader acl = new ArchiveClassLoader(identifier, url, exportPackages);

                        classLoaders.put(acl.getId(), acl);

                        if (result == null)
                           result = new HashSet<Integer>();

                        result.add(identifier);
                        
                        added = true;
                     }
                  }
               }
            }
            catch (Throwable t)
            {
               System.out.println("REPO: (" + url + ") =");
               t.printStackTrace(System.out);
            }
            finally
            {
               if (jarFile != null)
               {
                  try
                  {
                     jarFile.close();
                  }
                  catch (IOException ioe)
                  {
                     // Ignore
                  }
               }
            }
         }
         
         if (!added)
            nonExportClassLoader.addURL(url);
      }
      
      if (imports.size() > 0)
      {
         Iterator<Map.Entry<Integer, Map<String, List<String>>>> cit = imports.entrySet().iterator();
         while (cit.hasNext())
         {
            Map.Entry<Integer, Map<String, List<String>>> cEntry = cit.next();

            Integer classLoaderId = cEntry.getKey();
            Map<String, List<String>> value = cEntry.getValue();

            ArchiveClassLoader acl = getClassLoader(classLoaderId);

            Iterator<Map.Entry<String, List<String>>> vit = value.entrySet().iterator();
            while (vit.hasNext())
            {
               Map.Entry<String, List<String>> vEntry = vit.next();

               String version = vEntry.getKey();
               List<String> pkgs = vEntry.getValue();

               for (String pkg : pkgs)
               {
                  SortedMap<String, Set<Integer>> sm = packages.get(pkg);
                  
                  if (sm != null)
                  {
                     Set<Integer> clIds = sm.get(version);

                     if (clIds == null)
                        clIds = sm.get(sm.lastKey());

                     for (Integer cid : clIds)
                     {
                        acl.addImportClassLoader(cid);
                        result.add(cid);
                     }
                  }
               }
            }
         }
      }

      return result;
   }

   /**
    * Get the package name for a class
    * @param name The fully qualified class name
    * @return The package name
    */
   private String getPackageName(String name)
   {
      String packageName = "";
      int lastDot = name.lastIndexOf(".");

      if (lastDot != -1)
         packageName = name.substring(0, lastDot);

      return packageName;
   }
}
