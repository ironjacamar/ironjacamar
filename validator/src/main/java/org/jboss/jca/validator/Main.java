/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.validator;

import org.jboss.jca.common.Annotations;
import org.jboss.jca.common.Metadata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.metadata.rar.spec.AdminObjectMetaData;
import org.jboss.metadata.rar.spec.ConfigPropertyMetaData;
import org.jboss.metadata.rar.spec.ConnectionDefinitionMetaData;
import org.jboss.metadata.rar.spec.ConnectorMetaData;
import org.jboss.metadata.rar.spec.MessageListenerMetaData;

/**
 * A Main.
 * 
 * @author Jeff Zhang</a>
 * @version $Revision: $
 */
public class Main
{
   private static final int SUCCESS = 0;
   private static final int FAIL = 1;
   private static final int OTHER = 2;

   /**
    * validate
    * @param url The url
    * @param output directory of output
    * @return The system exit code
    */
   public static int validate(URL url, String output)
   {
      return validate(url, output, null);
   }

   /**
    * validate
    * @param url The url
    * @param output directory of output
    * @param classpath classpath of including
    * @return The system exit code
    */
   public static int validate(URL url, String output, String[] classpath)
   {
      if (url == null || !(url.toExternalForm().endsWith(".rar") || url.toExternalForm().endsWith(".rar/")))
         return FAIL;

      int exitCode = SUCCESS;
      File destination = null;
     
      try
      {
         File f = new File(url.toURI());
      
         if (!f.exists())
            throw new IOException("Archive " + url.toExternalForm() + " doesnt exists");

         File root = null;
 
         if (f.isFile())
         {
            destination = new File(SecurityActions.getSystemProperty("java.io.tmpdir"), "/tmp/");
            root = extract(f, destination);
         }
         else
         {
            root = f;
         }
      
         // Create classloader
         URL[] allurls;
         URL[] urls = getUrls(root);
         if (classpath != null && classpath.length > 0)
         {
            List<URL> listUrl = new ArrayList<URL>();
            for (URL u : urls)
               listUrl.add(u);
            for (String jar : classpath)
            {
               if (jar.endsWith(".jar"))
                  listUrl.add(new File(jar).toURI().toURL());
            }
            allurls = listUrl.toArray(new URL[listUrl.size()]);
         }
         else
            allurls = urls;
                  
         URLClassLoader cl = SecurityActions.createURLCLassLoader(allurls, 
            SecurityActions.getThreadContextClassLoader());
         SecurityActions.setThreadContextClassLoader(cl);

         // Parse metadata
         Metadata metadataHandler = new Metadata();
         ConnectorMetaData cmd = metadataHandler.getStandardMetaData(root);


         // Annotation scanning
         Annotations annotator = new Annotations();
         cmd = annotator.scan(cmd, cl.getURLs(), cl);


         List<Validate> validateClasses = new ArrayList<Validate>();
         List<Failure> failures = new ArrayList<Failure>();
         
         Validator validator = new Validator();
         validateClasses.addAll(createResourceAdapter(cmd, failures, validator.getResourceBundle(), cl));
         validateClasses.addAll(createManagedConnectionFactory(cmd, failures, validator.getResourceBundle(), cl));
         validateClasses.addAll(createActivationSpec(cmd, failures, validator.getResourceBundle(), cl));
         validateClasses.addAll(createAdminObject(cmd, failures, validator.getResourceBundle(), cl));

         List<Failure> classFailures = validator.validate(validateClasses); 
         if (classFailures != null && classFailures.size() > 0)
            failures.addAll(classFailures);

         if (failures != null && failures.size() > 0)
         {
            FailureHelper fh = new FailureHelper(failures);
            File reportDirectory = new File(output);

            if (!reportDirectory.mkdirs())
            {
               throw new IOException(output + " can't be created");
            }

            String reportName = url.getFile();
            int lastIndex = reportName.lastIndexOf(File.separator);
            if (lastIndex != -1)
               reportName = reportName.substring(lastIndex + 1);
            reportName += ".log";

            File report = new File(reportDirectory, reportName);
            FileWriter fw = null;
            BufferedWriter bw = null;
            try
            {
               fw = new FileWriter(report);
               bw = new BufferedWriter(fw, 8192);
               bw.write(fh.asText(validator.getResourceBundle()));
               bw.flush();
            }
            catch (IOException ioe)
            {
               ioe.printStackTrace();
            }
            finally
            {
               try
               {
                  if (bw != null)
                     bw.close();
                  if (fw != null)
                     fw.close();
               }
               catch (IOException ignore)
               {
                  // Ignore
               }
            }

            exitCode = FAIL;
         }
         exitCode = SUCCESS;
      } 
      catch (Exception e)
      {
         exitCode = OTHER;
      }
      
      if (destination != null)
      {
         try
         {
            recursiveDelete(destination);
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
      
      return exitCode;
   }
   
   /**
    * createResourceAdapter
    * @param cmd connector metadata
    * @param failures list of failures
    * @param rb ResourceBundle
    * @param cl classloador
    * @return list of validate objects
    */
   private static List<Validate> createResourceAdapter(ConnectorMetaData cmd, 
      List<Failure> failures, ResourceBundle rb, ClassLoader cl)
   {
      List<Validate> result = new ArrayList<Validate>();

      if (cmd.getRa() != null && cmd.getRa().getRaClass() != null)
      {
         try
         {
            Class<?> clazz = Class.forName(cmd.getRa().getRaClass(), true, cl);
            List<ConfigPropertyMetaData> configProperties = cmd.getRa().getConfigProperty();
            
            ValidateClass vc = new ValidateClass(Key.RESOURCE_ADAPTER, clazz, configProperties);
            result.add(vc);
         }
         catch (ClassNotFoundException e)
         {
            Failure failure = new Failure(Severity.ERROR,
                                          rb.getString("uncategorized"),
                                          rb.getString("ra.cnfe"),
                                          e.getMessage());
            failures.add(failure);
         }
      }
      return result;
   }

   /**
    * createManagedConnectionFactory
    * @param cmd connector metadata
    * @param failures list of failures
    * @param rb ResourceBundle
    * @param cl classloador
    * @return list of validate objects
    */
   private static List<Validate> createManagedConnectionFactory(ConnectorMetaData cmd, 
      List<Failure> failures, ResourceBundle rb, ClassLoader cl)
   {
      List<Validate> result = new ArrayList<Validate>();

      if (cmd.getRa() != null && cmd.getRa().getOutboundRa() != null
            && cmd.getRa().getOutboundRa().getConDefs() != null)
      {
         List<ConnectionDefinitionMetaData> cdMetas = cmd.getRa().getOutboundRa().getConDefs();
         if (cdMetas.size() > 0)
         {
            for (ConnectionDefinitionMetaData cdMeta : cdMetas)
            {
               if (cdMeta.getManagedConnectionFactoryClass() != null)
               {
                  try
                  {
                     Class<?> clazz = Class.forName(cdMeta.getManagedConnectionFactoryClass(), true, cl);
                     List<ConfigPropertyMetaData> configProperties = cdMeta.getConfigProps();

                     ValidateClass vc = new ValidateClass(Key.MANAGED_CONNECTION_FACTORY, clazz, configProperties);
                     result.add(vc);
                  }
                  catch (ClassNotFoundException e)
                  {
                     Failure failure = new Failure(Severity.ERROR, 
                                                   rb.getString("uncategorized"),
                                                   rb.getString("mcf.cnfe"),
                                                   e.getMessage());
                     failures.add(failure);
                  }
               }

            }
         }
      }

      return result;
   }
   
   /**
    * createActivationSpec
    * @param cmd connector metadata
    * @param failures list of failures
    * @param rb ResourceBundle
    * @param cl classloador
    * @return list of validate objects
    */
   private static List<Validate> createActivationSpec(ConnectorMetaData cmd, 
      List<Failure> failures, ResourceBundle rb, ClassLoader cl)
   {
      List<Validate> result = new ArrayList<Validate>();

      if (cmd.getRa() != null &&
            cmd.getRa().getInboundRa() != null &&
            cmd.getRa().getInboundRa().getMessageAdapter() != null &&
            cmd.getRa().getInboundRa().getMessageAdapter().getMessageListeners() != null)
      {
         List<MessageListenerMetaData> mlMetas = cmd.getRa().getInboundRa().
            getMessageAdapter().getMessageListeners();
         if (mlMetas.size() > 0)
         {
            for (MessageListenerMetaData mlMeta : mlMetas)
            {
               if (mlMeta.getActivationSpecType() != null && mlMeta.getActivationSpecType().getAsClass() != null)
               {
      
                  try
                  {
                     Class<?> clazz = Class.forName(mlMeta.getActivationSpecType().getAsClass(), true, cl);
                     List<ConfigPropertyMetaData> configProperties = mlMeta.getActivationSpecType().getConfigProps();
                     
                     ValidateClass vc = new ValidateClass(Key.ACTIVATION_SPEC, clazz, configProperties);
                     result.add(vc);
                  }
                  catch (ClassNotFoundException e)
                  {
                     Failure failure = new Failure(Severity.ERROR,
                                                   rb.getString("uncategorized"),
                                                   rb.getString("as.cnfe"),
                                                   e.getMessage());
                     failures.add(failure);
                  }
               }
            }
         }
      }
      return result;
   }
   
   /**
    * createAdminObject
    * @param cmd connector metadata
    * @param failures list of failures
    * @param rb ResourceBundle
    * @param cl classloador
    * @return list of validate objects
    */
   private static List<Validate> createAdminObject(ConnectorMetaData cmd, 
      List<Failure> failures, ResourceBundle rb, ClassLoader cl)
   {
      List<Validate> result = new ArrayList<Validate>();

      if (cmd.getRa() != null &&
          cmd.getRa().getAdminObjects() != null)
      {
         List<AdminObjectMetaData> aoMetas = cmd.getRa().getAdminObjects();
         if (aoMetas.size() > 0)
         {
            for (AdminObjectMetaData aoMeta : aoMetas)
            {
               if (aoMeta.getAdminObjectImplementationClass() != null)
               {
   
                  try
                  {
                     Class<?> clazz = Class.forName(aoMeta.getAdminObjectImplementationClass(), true, cl);
                     List<ConfigPropertyMetaData> configProperties = aoMeta.getConfigProps();
                     
                     ValidateClass vc = new ValidateClass(Key.ADMIN_OBJECT, clazz, configProperties);
                     result.add(vc);
                  }
                  catch (ClassNotFoundException e)
                  {
                     Failure failure = new Failure(Severity.ERROR,
                                                   rb.getString("uncategorized"),
                                                   rb.getString("ao.cnfe"),
                                                   e.getMessage());
                     failures.add(failure);
                  }
               }
            }
         }
      }
      return result;
   }
   
   /**
    * Validator standalone tool
    * 
    * @param args command line arguments
    */
   public static void main(String[] args)
   {
      boolean quite = false;
      String outputDir = "."; //put report into current directory by default
      int arg = 0;
      String[] classpath = null;
      
      if (args.length > 0)
      {
         while (args.length > arg + 1)
         {
            if (args[arg].startsWith("-"))
            {
               if (args[arg].endsWith("quite"))
               {
                  quite = true;
               }
               else if (args[arg].endsWith("output"))
               {
                  arg++;
                  if (arg + 1 >= args.length)
                  {
                     usage();
                     System.exit(OTHER);
                  }
                  outputDir = args[arg];
               }
               else if (args[arg].endsWith("classpath"))
               {
                  arg++;
                  classpath = args[arg].split(System.getProperty("path.separator"));

               }
            }
            else
            {
               usage();
               System.exit(OTHER);
            }
            arg++;
         }

         try
         {
            int systemExitCode = validate(new File(args[arg]).toURI().toURL(), outputDir, classpath);
            
            if (!quite && systemExitCode == FAIL)
            {
               System.out.println("Validation errors");
            }
            System.exit(systemExitCode);
         }
         catch (ArrayIndexOutOfBoundsException oe)
         {
            usage();
            System.exit(OTHER);
         }
         catch (MalformedURLException e)
         {
            e.printStackTrace();
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
      System.out.println("Usage: validator [-quite] [-output directory] [-classpath thirdparty.jar] <file>");
   }


   /**
    * Extract a JAR type file
    * @param file The file
    * @param directory The directory where the file should be extracted
    * @return The root of the extracted JAR file
    * @exception IOException Thrown if an error occurs
    */
   private static File extract(File file, File directory) throws IOException
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
            
            // Make sure that the directory is _really_ there
            if (copy.getParentFile() != null && !copy.getParentFile().exists())
            {
               if (!copy.getParentFile().mkdirs())
                  throw new IOException("Could not create " + copy.getParentFile());
            }

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
            if (!copy.exists())
            {
               if (!copy.mkdirs())
                  throw new IOException("Could not create " + copy);
            }
            else
            {
               if (!copy.isDirectory())
                  throw new IOException(copy + " isn't a directory");
            }
         }
      }

      return target;
   }


   /**
    * Recursive delete
    * @param f The file handler
    * @exception IOException Thrown if a file could not be deleted
    */
   private static void recursiveDelete(File f) throws IOException
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
    * Get the URLs for the directory and all libraries located in the directory
    * @param directory The directory
    * @return The URLs
    * @exception MalformedURLException MalformedURLException
    * @exception IOException IOException
    */
   private static URL[] getUrls(File directory) throws MalformedURLException, IOException
   {
      List<URL> list = new LinkedList<URL>();

      if (directory.exists() && directory.isDirectory())
      {
         // Add directory
         list.add(directory.toURI().toURL());

         // Add the contents of the directory too
         File[] jars = directory.listFiles(new FilenameFilter()
         {
            /**
             * Accept
             * @param dir The directory
             * @param name The name
             * @return True if accepts; otherwise false
             */
            public boolean accept(File dir, String name)
            {
               return name.endsWith(".jar");
            }
         });

         if (jars != null)
         {
            for (int j = 0; j < jars.length; j++)
            {
               list.add(jars[j].getCanonicalFile().toURI().toURL());
            }
         }
      }
      return list.toArray(new URL[list.size()]);      
   }
}
