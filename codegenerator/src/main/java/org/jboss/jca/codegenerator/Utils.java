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
package org.jboss.jca.codegenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * A Utils.
 * @version $Revision: $
 */
public class Utils
{
   /**
    *  Reads the contents of a file into a string variable.
    * 
    * @param input url 
    * @return string of return
    * @throws IOException ioException
    */
   public static String readFileIntoString(URL input) throws IOException
   {
      
      InputStream stream = null;
      InputStreamReader reader = null;
      try
      {
         stream = input.openStream();
         reader = new InputStreamReader(stream);
         return readStreamIntoString(reader);
      }
      finally
      {
         if (reader != null)
            reader.close();
         if (stream != null)
            stream.close();
      }
   }

   /**
    *  Reads the contents of a stream into a string variable.
    * 
    * @param reader url
    * @return string of return
    * @throws IOException ioException
    */
   private static String readStreamIntoString(Reader reader) throws IOException
   {
      StringBuilder s = new StringBuilder();
      char a[] = new char[0x10000];
      while (true)
      {
         int l = reader.read(a);
         if (l == -1)
            break;
         if (l <= 0)
            throw new IOException();
         s.append(a, 0, l);
      }
      return s.toString();
   }
   
   
   /**
    * Create source file
    * @param name The name of the class
    * @param packageName The package name
    * @param outDir output directory
    * @return The file
    * @exception IOException Thrown if an error occurs 
    */
   public static FileWriter createSrcFile(String name, String packageName, String outDir) throws IOException
   {
      String directory = "src" + File.separatorChar + "main" + File.separatorChar + "java";

      return createPackageFile(name, packageName, directory, outDir);
   }
   
   /**
    * Create test file
    * @param name The name of the class
    * @param packageName The package name
    * @param outDir output directory
    * @return The file
    * @exception IOException Thrown if an error occurs 
    */
   public static FileWriter createTestFile(String name, String packageName, String outDir) throws IOException
   {
      String directory = "src" + File.separatorChar + "test" + File.separatorChar + "java";

      return createPackageFile(name, packageName, directory, outDir);
   }
   
   /**
    * Create file in the package
    * @param name The name of the class
    * @param packageName The package name
    * @param directory layout directory
    * @param outDir output directory
    * @return The file
    * @exception IOException Thrown if an error occurs 
    */
   private static FileWriter createPackageFile(String name, String packageName, 
         String directory, String outDir) throws IOException
   {
      if (packageName != null && !packageName.trim().equals(""))
      {
         directory = directory + File.separatorChar +
                     packageName.replace('.', File.separatorChar);
      }

      File path = new File(outDir, directory);
      if (!path.exists())
      {
         if (!path.mkdirs())
            throw new IOException("outdir can't be created");
      }
      
      File file = new File(path.getAbsolutePath() + File.separatorChar + name);

      if (file.exists())
      {
         if (!file.delete())
            throw new IOException("there is exist file, please check");
      }

      return new FileWriter(file);
   }
   
   /**
    * Create file
    * @param name The name of the class
    * @param outDir output directory
    * @return The file
    * @exception IOException Thrown if an error occurs 
    */
   public static FileWriter createFile(String name, String outDir) throws IOException
   {
      File path = new File(outDir);
      if (!path.exists())
      {
         if (!path.mkdirs())
            throw new IOException("outdir can't be created");
      }
      
      File file = new File(path.getAbsolutePath() + File.separatorChar + name);

      if (file.exists())
      {
         if (!file.delete())
            throw new IOException("there is exist file, please check");
      }

      return new FileWriter(file);
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
    * copy folders
    * 
    * @param sourcePath source folder
    * @param targetPath target folder
    * @param filterName filter name
    * @exception IOException Thrown if an error occurs 
    */
   public static void copyFolder(String sourcePath, String targetPath, final String filterName) throws IOException
   {
      File path = new File(targetPath);
      if (!path.exists())
      {
         if (!path.mkdirs())
            throw new IOException("outdir can't be created");
      }
      File a = new File(sourcePath);
      String[] file = a.list(new FilenameFilter()
      {
         public boolean accept(File dir, String fname)
         {
            if (new File(dir, fname).isDirectory())
            {
               return true;
            }
            else
            {
               return fname.endsWith(filterName);
            }
         }

      });
      File jarFile = null;
      for (int i = 0; i < file.length; i++)
      {
         jarFile = new File(sourcePath + File.separator + file[i]);

         if (jarFile.isFile())
         {
            copyFile(jarFile, targetPath);
         }
         if (jarFile.isDirectory())
         {
            copyFolder(sourcePath + File.separator + file[i], targetPath + File.separator + file[i], filterName);
         }
      }
   }

   private static void copyFile(File sourceFile, String targetPath) throws FileNotFoundException, IOException
   {
      FileInputStream from = null;
      FileOutputStream to = null;
      try
      {
         from = new FileInputStream(sourceFile);
         to = new FileOutputStream(targetPath + File.separator + sourceFile.getName());
         byte[] buffer = new byte[4096];
         int bytesRead;

         while ((bytesRead = from.read(buffer)) != -1)
            to.write(buffer, 0, bytesRead);
      }
      finally
      {
         if (from != null)
         {
            try
            {
               from.close();
            }
            catch (IOException e)
            {
               ;
            }
         }
         if (to != null)
         {
            try
            {
               to.close();
            }
            catch (IOException e)
            {
               ;
            }
         }
      }
   }
}
