/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.validator;

import org.ironjacamar.common.annotations.Annotations;
import org.ironjacamar.common.api.metadata.spec.AdminObject;
import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.common.api.metadata.spec.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.api.metadata.spec.Connector.Version;
import org.ironjacamar.common.api.metadata.spec.MessageListener;
import org.ironjacamar.common.api.metadata.spec.XsdString;
import org.ironjacamar.common.metadata.MetadataFactory;
import org.ironjacamar.common.spi.annotations.repository.AnnotationRepository;
import org.ironjacamar.common.spi.annotations.repository.AnnotationScanner;
import org.ironjacamar.common.spi.annotations.repository.AnnotationScannerFactory;

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

/**
 * Validation of resource adapters
 *
 * @author Jeff Zhang
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Validation
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

      URLClassLoader cl =  null;
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

         cl = SecurityActions.createURLClassLoader(allurls, SecurityActions.getThreadContextClassLoader());
         SecurityActions.setThreadContextClassLoader(cl);

         // Parse metadata
         MetadataFactory metadataFactory = new MetadataFactory();
         Connector cmd = metadataFactory.getStandardMetaData(root);

         // Annotation scanning
         Annotations annotator = new Annotations();
         AnnotationScanner scanner = AnnotationScannerFactory.getAnnotationScanner();
         AnnotationRepository repository = scanner.scan(cl.getURLs(), cl);
         cmd = annotator.merge(cmd, repository, cl);

         File reportDirectory = new File(output);

         if (!reportDirectory.exists() && !reportDirectory.mkdirs())
         {
            throw new IOException("The output directory '" + output + "' can't be created");
         }

         String reportName = url.getFile();

         int lastSlashIndex = reportName.lastIndexOf("/");
         int lastSepaIndex = reportName.lastIndexOf(File.separator);

         int lastIndex = lastSlashIndex > lastSepaIndex ? lastSlashIndex : lastSepaIndex;
         if (lastIndex != -1)
            reportName = reportName.substring(lastIndex + 1);
         reportName += ".log";

         File report = new File(reportDirectory, reportName);

         exitCode = validate(cmd, root, report, cl);
      }
      catch (ValidatorException ve)
      {
         exitCode = FAIL;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         exitCode = OTHER;
      }
      finally
      {
         SecurityActions.closeURLClassLoader(cl);
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
    * validate
    * @param c The spec metadata
    * @param root The root directory of the expanded resource adapter archive
    * @param report The destination of the report; <code>null</code> if an exception should be thrown instead
    * @param cl The class loader
    * @return The system exit code
    * @exception ValidatorException Thrown if a validation error occurs
    * @exception IOException If an I/O error occurs
    */
   public static int validate(Connector c, File root, File report, ClassLoader cl)
      throws ValidatorException, IOException
   {
      int exitCode = SUCCESS;

      ClassLoader oldTCCL = SecurityActions.getThreadContextClassLoader();
      try
      {
         SecurityActions.setThreadContextClassLoader(cl);

         List<Validate> validateClasses = new ArrayList<Validate>();
         List<Failure> failures = new ArrayList<Failure>();

         Validator validator = new Validator();
         validateClasses.addAll(createResourceAdapter(c, failures, validator.getResourceBundle(), cl));
         validateClasses.addAll(createManagedConnectionFactory(c, failures, validator.getResourceBundle(), cl));
         validateClasses.addAll(createActivationSpec(c, failures, validator.getResourceBundle(), cl));
         validateClasses.addAll(createAdminObject(c, failures, validator.getResourceBundle(), cl));

         if (root != null && validateClassesInPackage(root))
         {
            Failure failure = new Failure(Severity.WARNING, "20.2",
                                          validator.getResourceBundle().getString("pak.cip"));
            failures.add(failure);
         }

         List<Failure> classFailures = validator.validate(validateClasses);
         if (classFailures != null && !classFailures.isEmpty())
            failures.addAll(classFailures);

         if (!failures.isEmpty())
         {
            if (report != null)
            {
               FailureHelper fh = new FailureHelper(failures);

               FileWriter fw = null;
               BufferedWriter bw = null;
               try
               {
                  fw = new FileWriter(report);
                  bw = new BufferedWriter(fw, 8192);
                  bw.write(fh.asText(validator.getResourceBundle()));
                  bw.flush();
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
            }
            else
            {
               String eisType = c.getEisType() != null ? c.getEisType().getValue() : "unknown Eis Type";
               throw new ValidatorException(eisType, failures, validator.getResourceBundle());
            }

            exitCode = FAIL;
         }
         else
         {
            exitCode = SUCCESS;
         }
      }
      finally
      {
         SecurityActions.setThreadContextClassLoader(oldTCCL);
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
   private static List<Validate> createResourceAdapter(Connector cmd,
         List<Failure> failures, ResourceBundle rb, ClassLoader cl)
   {
      List<Validate> result = new ArrayList<Validate>();

      if (cmd.getVersion() != Version.V_10 && cmd.getResourceadapter() != null &&
          cmd.getResourceadapter().getResourceadapterClass() != null)
      {
         try
         {
            Class<?> clazz = Class.forName(cmd.getResourceadapter().getResourceadapterClass(),
                                           true, cl);
            List<ConfigProperty> configProperties = cmd.getResourceadapter().getConfigProperties();

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
   private static List<Validate> createManagedConnectionFactory(Connector cmd,
         List<Failure> failures, ResourceBundle rb, ClassLoader cl)
   {
      List<Validate> result = new ArrayList<Validate>();

      if (cmd.getVersion() != Version.V_10 &&
          cmd.getResourceadapter() != null &&
          cmd.getResourceadapter().getOutboundResourceadapter() != null &&
          cmd.getResourceadapter().getOutboundResourceadapter().getConnectionDefinitions() != null)
      {
         List<ConnectionDefinition> cdMetas = cmd.getResourceadapter()
               .getOutboundResourceadapter().getConnectionDefinitions();
         if (!cdMetas.isEmpty())
         {
            for (ConnectionDefinition cdMeta : cdMetas)
            {
               if (cdMeta.getManagedConnectionFactoryClass() != null)
               {
                  try
                  {
                     Class<?> clazz = Class.forName(cdMeta.getManagedConnectionFactoryClass().getValue(), true, cl);
                     List<ConfigProperty> configProperties = cdMeta.getConfigProperties();

                     ValidateClass vc = new ValidateClass(Key.MANAGED_CONNECTION_FACTORY, clazz, configProperties);
                     result.add(vc);
                  }
                  catch (ClassNotFoundException | NoClassDefFoundError e)
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
   private static List<Validate> createActivationSpec(Connector cmd,
         List<Failure> failures, ResourceBundle rb, ClassLoader cl)
   {
      List<Validate> result = new ArrayList<Validate>();

      if (cmd.getVersion() != Version.V_10 &&
          cmd.getResourceadapter() != null &&
          cmd.getResourceadapter().getInboundResourceadapter() != null &&
          cmd.getResourceadapter().getInboundResourceadapter().getMessageadapter() != null &&
          cmd.getResourceadapter().getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null)
      {
         List<MessageListener> mlMetas = cmd.getResourceadapter()
               .getInboundResourceadapter().getMessageadapter()
               .getMessagelisteners();
         if (!mlMetas.isEmpty())
         {
            for (MessageListener mlMeta : mlMetas)
            {
               if (mlMeta.getActivationspec() != null && mlMeta.getActivationspec().getActivationspecClass() != null
                     && !mlMeta.getActivationspec().getActivationspecClass().equals(XsdString.NULL_XSDSTRING))
               {

                  if (mlMeta.getActivationspec().getConfigProperties() != null &&
                          !mlMeta.getActivationspec().getConfigProperties().isEmpty())
                  {
                     try
                     {
                        Class<?> clazz = Class.forName(mlMeta.getActivationspec().getActivationspecClass().getValue(),
                                                       true, cl);
                        List<ConfigProperty> configProperties = mlMeta.getActivationspec().getConfigProperties();

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
   private static List<Validate> createAdminObject(Connector cmd,
         List<Failure> failures, ResourceBundle rb, ClassLoader cl)
   {
      List<Validate> result = new ArrayList<Validate>();

      if (cmd.getVersion() != Version.V_10 &&
          cmd.getResourceadapter() != null &&
          cmd.getResourceadapter().getAdminObjects() != null)
      {
         List<AdminObject> aoMetas = cmd.getResourceadapter().getAdminObjects();
         if (!aoMetas.isEmpty())
         {
            for (AdminObject aoMeta : aoMetas)
            {
               if (aoMeta.getAdminobjectClass() != null
                     && !aoMeta.getAdminobjectClass().equals(XsdString.NULL_XSDSTRING))
               {

                  try
                  {
                     Class<?> clazz = Class.forName(aoMeta.getAdminobjectClass().getValue(), true, cl);
                     List<ConfigProperty> configProperties = aoMeta.getConfigProperties();

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

      JarFile jar = null;
      try
      {
         jar = new JarFile(file);
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
      }
      finally
      {
         try
         {
            if (jar != null)
               jar.close();
         }
         catch (IOException ignore)
         {
            // Ignore
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

   private static boolean validateClassesInPackage(File root)
   {
      boolean hasClass = false;
      File[] files = root.listFiles();
      for (File file : files)
      {
         if (file.getName().lastIndexOf(".class") > 0)
            return true;
         if (file.isDirectory())
            hasClass = validateClassesInPackage(file);
         if (hasClass)
            return true;
      }
      return hasClass;
   }

}
