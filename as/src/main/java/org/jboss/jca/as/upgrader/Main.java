/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.as.upgrader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The AS upgrader application - updates the IronJacamar distribution inside
 * the JBoss Application Server
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Main
{
   /** REpository */
   public static final String REPOSITORY =
      "http://repository.jboss.org/nexus/content/groups/public/org/jboss/ironjacamar";

   /**
    * API artifacts
    */
   static final String[] API_ARTIFACTS = {
      "ironjacamar-common-api",
      "ironjacamar-common-spi",
      "ironjacamar-core-api"
   };

   /**
    * Implementation artifacts
    */
   static final String[] IMPLEMENTATION_ARTIFACTS = {
      "ironjacamar-common-impl",
      "ironjacamar-core-impl",
      "ironjacamar-deployers-common",
      "ironjacamar-validator",
   };

   /**
    * JDBC artifacts
    */
   static final String[] JDBC_ARTIFACTS = {
      "ironjacamar-jdbc"
   };

   /**
    * Main
    * 
    * @param args command line arguments
    */
   public static void main(String[] args)
   {
      if (args.length == 2)
      {
         try
         {
            String version = args[0];
            String asPath = args[1];

            File asRoot = new File(asPath);

            if (asRoot.exists() && isValid(asRoot))
            {
               System.out.println("Version: " + version);
               System.out.println("AS installation: " + asPath);
               System.out.println("");

               File downloadDirectory = download(version);

               move(asRoot);

               install(asRoot, version);

               delete(downloadDirectory);

               System.out.println("");
               System.out.println("Upgrade successful");
            }
            else
            {
               System.err.println("AS installation not found: " + asPath);
            }
         }
         catch (Throwable t)
         {
            System.out.println("");
            System.out.println("Upgrade unsuccessful");

            System.err.println("Error: " + t.getMessage());
            t.printStackTrace();
         }
      }
      else
      {
         usage();
      }
   }

   /**
    * Tool usage
    */
   private static void usage()
   {
      System.out.println("Usage: as-upgrader <version> <as-installation>");
   }

   /**
    * Is valid AS installation root
    * @param root The root
    * @return True if valid, otherwise false
    */
   private static boolean isValid(File root)
   {
      File f = new File(root, "modules" + File.separator + "org" + File.separator + "jboss" +
                        File.separator + "ironjacamar");

      return f.exists();
   }

   /**
    * Download
    * @param version The version
    * @return The download directory
    * @exception Throwable If an error occurs
    */
   private static File download(String version) throws Throwable
   {
      File toDirectory = new File(System.getProperty("java.io.tmp"), "ij");

      delete(toDirectory);

      if (!toDirectory.mkdirs())
         throw new IOException("Could not create: " + toDirectory);

      for (String artifact : API_ARTIFACTS)
      {
         downloadArtifact(artifact, version, toDirectory);
      }

      for (String artifact : IMPLEMENTATION_ARTIFACTS)
      {
         downloadArtifact(artifact, version, toDirectory);
      }

      for (String artifact : JDBC_ARTIFACTS)
      {
         downloadArtifact(artifact, version, toDirectory);
      }

      System.out.println("Download: Done");

      return toDirectory;
   }

   /**
    * Download artifact
    * @param name The artifact name
    * @param version The version
    * @param dest The destination
    * @exception Throwable If an error occurs
    */
   private static void downloadArtifact(String name, String version, File destination) throws Throwable
   {
      Http downloader = new Http();

      String fileName = name + "-" + version + ".jar";
      String path = REPOSITORY + "/" + name + "/" + version + "/" + fileName;

      if (!downloader.download(path, new File(destination, fileName)))
         throw new IOException("Could not download: " + path);
   }

   /**
    * Move the old version
    * @param root The root
    * @exception Throwable If an error occurs
    */
   private static void move(File root) throws Throwable
   {
      File ijRoot = new File(root, "modules" + File.separator + "org" + File.separator + "jboss" +
                             File.separator + "ironjacamar");

      File apiRoot = new File(ijRoot, "api");
      File implRoot = new File(ijRoot, "impl");
      File jdbcRoot = new File(ijRoot, "jdbcadapters");

      String oldVersion = getVersion(new File(apiRoot, "main"));

      File apiOld = new File(apiRoot, oldVersion);
      delete(apiOld);

      if (!new File(apiRoot, "main").renameTo(apiOld))
         throw new IOException("Could not rename old api main");

      File implOld = new File(implRoot, oldVersion);
      delete(implOld);

      if (!new File(implRoot, "main").renameTo(implOld))
         throw new IOException("Could not rename old impl main");

      File jdbcOld = new File(jdbcRoot, oldVersion);
      delete(jdbcOld);

      if (!new File(jdbcRoot, "main").renameTo(jdbcOld))
         throw new IOException("Could not rename old JDBC main");

      System.out.println("Moving old installation (" + oldVersion + "): Done");
   }

   /**
    * Get the version of an IronJacamar installation
    * @param root The root
    * @return The version
    * @exception Throwable If an error occurs
    */
   private static String getVersion(File root) throws Throwable
   {
      for (File f : root.listFiles())
      {
         if (f.isFile() && f.getName().startsWith("ironjacamar"))
         {
            String name = f.getName();
            int firstDot = name.indexOf(".");
            int jar = name.indexOf(".jar");

            return name.substring(firstDot - 1, jar);
         }
      }

      throw new IOException("Unable to identify old version");
   }

   /**
    * Recursive delete
    * @param f The file handler
    * @exception IOException Thrown if a file could not be deleted
    */
   private static void delete(File f) throws IOException
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
                  delete(files[i]);
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
    * Copy
    * @param src The source
    * @param dest The destination
    * @exception IOException Thrown if a file could not be deleted
    */
   private static void copy(File src, File dest) throws IOException
   {
      if (src == null)
         throw new IllegalArgumentException("Src is null");

      if (dest == null)
         throw new IllegalArgumentException("Dest is null");

      if (!src.exists())
         throw new IOException("Source doesn't exist: " + src.getAbsolutePath());

      if (!src.canRead())
         throw new IOException("Source can't be read: " + src.getAbsolutePath());

      if (src.isDirectory())
      {
         if (!dest.exists())
         {
            if (!dest.mkdirs())
               throw new IOException("Could not create directory: " + dest.getAbsolutePath());
         }
          
         String list[] = src.list();
         for (int i = 0; i < list.length; i++)
         {
            File srcFile = new File(src, list[i]);
            File destFile = new File(dest, list[i]); 
            copy(srcFile, destFile);
         }
      }
      else
      {
         InputStream in = null;
         OutputStream out = null;

         byte[] buffer = new byte[8192];

         int bytesRead;

         try
         {
            in =  new BufferedInputStream(new FileInputStream(src), 8192);
            out = new BufferedOutputStream(new FileOutputStream(dest), 8192);

            while ((bytesRead = in.read(buffer)) >= 0)
            {
               out.write(buffer, 0, bytesRead);
            }

            out.flush();
         }
         catch (IOException e)
         {
            IOException wrapper = new IOException("Unable to copy file: " +
                                                  src.getAbsolutePath() + " to " + dest.getAbsolutePath());
            wrapper.initCause(e);
            wrapper.setStackTrace(e.getStackTrace());
            throw wrapper;
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

            if (out != null)
            {
               try
               {
                  out.close();
               }
               catch (IOException ioe)
               {
                  // Ignore
               }
            }
         }
      }
   }

   /**
    * Install
    * @param root The root
    * @param version The version
    * @exception Throwable If an error occurs
    */
   private static void install(File root, String version) throws Throwable
   {
      File fromDirectory = new File(System.getProperty("java.io.tmp"), "ij");

      File ijRoot = new File(root, "modules" + File.separator + "org" + File.separator + "jboss" +
                             File.separator + "ironjacamar");
      File apiRoot = new File(ijRoot, "api");
      File implRoot = new File(ijRoot, "impl");
      File jdbcRoot = new File(ijRoot, "jdbcadapters");

      File apiMain = new File(apiRoot, "main");
      if (!apiMain.mkdirs())
         throw new IOException("Could not create: " + apiMain);

      for (String artifact : API_ARTIFACTS)
      {
         installArtifact(fromDirectory, artifact, version, apiMain);
      }

      File apiModuleXml = new File(apiMain, "module.xml");
      FileWriter fw = new FileWriter(apiModuleXml);

      for (String s : ModuleXml.getApi(version))
      {
         fw.write(s);
         fw.write("\n");
      }

      fw.flush();
      fw.close();

      File implMain = new File(implRoot, "main");
      if (!implMain.mkdirs())
         throw new IOException("Could not create: " + implMain);

      for (String artifact : IMPLEMENTATION_ARTIFACTS)
      {
         installArtifact(fromDirectory, artifact, version, implMain);
      }

      File implModuleXml = new File(implMain, "module.xml");
      fw = new FileWriter(implModuleXml);

      for (String s : ModuleXml.getImplementation(version))
      {
         fw.write(s);
         fw.write("\n");
      }

      fw.flush();
      fw.close();

      File jdbcMain = new File(jdbcRoot, "main");
      if (!jdbcMain.mkdirs())
         throw new IOException("Could not create: " + jdbcMain);

      for (String artifact : JDBC_ARTIFACTS)
      {
         installArtifact(fromDirectory, artifact, version, jdbcMain);
      }

      File jdbcModuleXml = new File(jdbcMain, "module.xml");
      fw = new FileWriter(jdbcModuleXml);

      for (String s : ModuleXml.getJdbc(version))
      {
         fw.write(s);
         fw.write("\n");
      }

      fw.flush();
      fw.close();

      System.out.println("Installing new version: Done");
   }

   /**
    * Install artifact
    * @param from The from directory
    * @param artifact The artifact name
    * @param version The version
    * @param to The to directory
    * @exception Throwable If an error occurs
    */
   private static void installArtifact(File from, String artifact, String version, File to) throws Throwable
   {
      File src = new File(from, artifact + "-" + version + ".jar");
      File dest = new File(to, artifact + "-" + version + ".jar");

      copy(src, dest);
   }
}
