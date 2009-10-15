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

package org.jboss.jca.fungal.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Logger;


/**
 * An utility for JAR type files
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class FileUtil
{
   private static Logger log = Logger.getLogger(FileUtil.class.getName());

   /**
    * Constructor
    */
   private FileUtil()
   {
   }

   /**
    * Compress a directory in a JAR layout to a file
    * @param directory The directory
    * @param target The JAR file
    * @exception IOException Thrown if an error occurs
    */
   public static void compress(File directory, File target) throws IOException
   {
      if (directory == null)
         throw new IllegalArgumentException("Directory is null");

      if (target == null)
         throw new IllegalArgumentException("Target is null");

      if (target.exists())
         recursiveDelete(target);

      Manifest manifest = null;

      File manifestFile = new File(directory, "META-INF/MANIFEST.MF");
      if (manifestFile.exists())
      {
         FileInputStream fis = null;
         try
         {
            fis = new FileInputStream(manifestFile);
            manifest = new Manifest(fis);
         }
         finally
         {
            try
            {
               if (fis != null)
                  fis.close();
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }
      }
      else
      {
         log.fine("No META-INF/MANIFEST.MF found; creating one");
         manifest = new Manifest();
      }

      JarOutputStream jos = null;

      try
      {
         FileOutputStream fos = new FileOutputStream(target);
         jos = new JarOutputStream(fos, manifest);

         int bytesRead;
         byte[] buffer = new byte[4096];

         List<File> entries = findEntries(directory);

         if (entries != null)
         {
            entries.remove(new File("META-INF/MANIFEST.MF"));

            for (File file : entries)
            {
               File f = new File(directory, file.getPath());
               JarEntry entry = new JarEntry(file.getPath());
               jos.putNextEntry(entry);

               FileInputStream in = null;
               try
               {
                  in = new FileInputStream(f);
                  while ((bytesRead = in.read(buffer)) != -1)
                     jos.write(buffer, 0, bytesRead);
               }
               finally
               {
                  if (in != null)
                  {
                     try
                     {
                        in.close(); 
                     }
                     catch (IOException ioe)
                     {
                        // Ignore
                     }
                  }
               }
            }
         }

         jos.flush();
      }
      finally
      {
         try
         {
            if (jos != null)
               jos.close();
         }
         catch (IOException ignore)
         {
            // Ignore
         }
      }
   }

   /**
    * Extract a JAR type file
    * @param file The file
    * @param directory The directory where the file should be extracted
    * @return The root of the extracted JAR file
    * @exception IOException Thrown if an error occurs
    */
   public static File extract(File file, File directory) throws IOException
   {
      if (file == null)
         throw new IllegalArgumentException("File is null");

      if (directory == null)
         throw new IllegalArgumentException("Directory is null");

      File target = new File(directory, file.getName());

      if (target.exists())
         recursiveDelete(target);

      if (!target.mkdirs())
         throw new IOException("Could not create " + target);

      JarFile jar = new JarFile(file);
      Enumeration<JarEntry> entries = jar.entries();

      while (entries.hasMoreElements())
      {
         JarEntry je = entries.nextElement();
         File copy = new File(target, je.getName());

         if (!je.isDirectory())
         {
            InputStream in = null;
            OutputStream out = null;

            try
            {
               in = new BufferedInputStream(jar.getInputStream(je));
               out = new BufferedOutputStream(new FileOutputStream(copy));

               byte[] buffer = new byte[4096];
               for (;;)
               {
                  int nBytes = in.read(buffer);
                  if (nBytes <= 0)
                     break;

                  out.write(buffer, 0, nBytes);
               }
               out.flush();
            }
            finally
            {
               try
               {
                  if (out != null)
                     out.close();
               }
               catch (IOException ignore)
               {
                  // Ignore
               }

               try
               {
                  if (in != null)
                     in.close();
               }
               catch (IOException ignore)
               {
                  // Ignore
               }
            }
         }
         else
         {
            if (!copy.mkdirs())
               throw new IOException("Could not create " + copy);
         }
      }

      return target;
   }

   /**
    * Recursive delete
    * @param f The file handler
    * @exception IOException Thrown if a file could not be deleted
    */
   public static void recursiveDelete(File f) throws IOException
   {
      if (f != null && f.exists())
      {
         File[] files = f.listFiles();
         if (files != null)
         {
            for (int i = 0; i < files.length; i++)
            {
               if (files[i].isDirectory())
               {
                  recursiveDelete(files[i]);
               } 
               else
               {
                  if (!files[i].delete())
                     throw new IOException("Could not delete " + files[i]);
               }
            }
         }
         if (!f.delete())
            throw new IOException("Could not delete " + f);
      }
   }

   /**
    * Find all file entries for a directory
    * @param file The root directory
    * @return The list of files
    */
   private static List<File> findEntries(File root)
   {
      try
      {
         return getListing(root, root);
      }
      catch (Exception e)
      {
         log.severe(e.getMessage());
      }

      return null;
   }

   /**
    * Recursively walk a directory tree and return a list of all files entries found
    * @param root The root directory
    * @param directory The current directory
    * @return The list of files
    * @exception Exception Thrown if an error occurs
    */
   private static List<File> getListing(File root, File directory) throws Exception 
   {
      List<File> result = new ArrayList<File>();

      File[] filesAndDirs = directory.listFiles();

      if (filesAndDirs != null)
      {
         for (File file : filesAndDirs) 
         {
            if (file.isDirectory()) 
            {
               List<File> deeperList = getListing(root, file);
               result.addAll(deeperList);
            }
            else
            {
               String fileName = file.getPath().substring(root.getPath().length() + 1);
               result.add(new File(fileName));
            }
         }
      }

      return result;
   }
}
