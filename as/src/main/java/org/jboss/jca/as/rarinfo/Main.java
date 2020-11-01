/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.as.rarinfo;

import org.jboss.jca.common.annotations.Annotations;
import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.Pool;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.Connector.Version;
import org.jboss.jca.common.api.metadata.spec.MessageListener;
import org.jboss.jca.common.api.metadata.spec.RequiredConfigProperty;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.XsdString;
import org.jboss.jca.common.metadata.common.CredentialImpl;
import org.jboss.jca.common.metadata.common.PoolImpl;
import org.jboss.jca.common.metadata.common.SecurityImpl;
import org.jboss.jca.common.metadata.common.XaPoolImpl;
import org.jboss.jca.common.metadata.resourceadapter.AdminObjectImpl;
import org.jboss.jca.common.metadata.resourceadapter.ConnectionDefinitionImpl;
import org.jboss.jca.common.metadata.spec.RaParser;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScanner;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScannerFactory;
import org.jboss.jca.core.util.Injection;
import org.jboss.jca.validator.Validation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * rar info main class
 * 
 * @author Jeff Zhang
 */
public class Main
{
   /** Exit codes */
   private static final int SUCCESS = 0;
   private static final int ERROR = 1;
   private static final int OTHER = 2;
   
   private static final String REPORT_FILE = "-report.txt";
   private static final String RAXML_FILE = "META-INF/ra.xml";

   private static final String tempdir = System.getProperty("java.io.tmpdir");
   private static final String subdir = "/jca/";
   private static Set<Class<?>> validTypes;
   
   private static final String ARGS_CP = "-classpath";
   private static final String ARGS_STDOUT = "--stdout";
   private static final String ARGS_OUT = "-o";
   

   private static File root = null;
   
   static
   {
      validTypes = new HashSet<Class<?>>();

      validTypes.add(boolean.class);
      validTypes.add(Boolean.class);
      validTypes.add(byte.class);
      validTypes.add(Byte.class);
      validTypes.add(short.class);
      validTypes.add(Short.class);
      validTypes.add(int.class);
      validTypes.add(Integer.class);
      validTypes.add(long.class);
      validTypes.add(Long.class);
      validTypes.add(float.class);
      validTypes.add(Float.class);
      validTypes.add(double.class);
      validTypes.add(Double.class);
      validTypes.add(char.class);
      validTypes.add(Character.class);
      validTypes.add(String.class);
   }
   
   private static void loadNativeLibraries(File root)
   {
      if (root != null && root.exists())
      {
         List<String> libs = null;

         if (root.isDirectory())
         {
            if (root.listFiles() != null)
            {
               for (File f : root.listFiles())
               {
                  String fileName = f.getName().toLowerCase(Locale.US);
                  if (fileName.endsWith(".a") || fileName.endsWith(".so") || fileName.endsWith(".dll"))
                  {
                     if (libs == null)
                        libs = new ArrayList<String>();

                     libs.add(f.getAbsolutePath());
                  }
               }
            }
            else
            {
               System.out.println("Root is a directory, but there were an I/O error: " + root.getAbsolutePath());
            }
         }

         if (libs != null)
         {
            for (String lib : libs)
            {
               try
               {
                  SecurityActions.load(lib);
                  System.out.println("Loaded library: " + lib);
               }
               catch (Throwable t)
               {
                  t.printStackTrace(System.err);
                  System.out.println("Unable to load library: " + lib);
               }
            }
         }
      }
   }

   /**
    * Main
    * @param args args 
    */
   public static void main(String[] args)
   {
      final int argsLength = args.length;
      PrintStream out = null;
      ZipFile zipFile = null;
      URLClassLoader cl = null;
      try
      {
         if (argsLength < 1)
         {
            usage();
            System.exit(OTHER);
         }
         String rarFile = "";
         String[] cps = null;
         boolean stdout = false;
         String reportFile = "";
         
         for (int i = 0; i < argsLength; i++)
         {
            String arg = args[i];
            if (arg.equals(ARGS_CP))
            {
               cps = args[++i].split(System.getProperty("path.separator"));
            }
            else if (arg.equals(ARGS_STDOUT))
            {
               stdout = true;
            }
            else if (arg.equals(ARGS_OUT))
            {
               reportFile = args[++i];
            }
            else if (arg.endsWith("rar"))
            {
               rarFile = arg;
            }
            else
            {
               usage();
               System.exit(OTHER);
            }
         }
         zipFile = new ZipFile(rarFile);

         boolean exsitNativeFile = false;
         Connector connector = null;

         ArrayList<String> names = new ArrayList<String>();
         ArrayList<String> xmls = new ArrayList<String>();
         Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

         while (zipEntries.hasMoreElements())
         {
            ZipEntry ze = (ZipEntry) zipEntries.nextElement();
            String name = ze.getName();
            names.add(name);
            
            if (name.endsWith(".xml") && name.startsWith("META-INF") && !name.endsWith("pom.xml"))
               xmls.add(name);
               
            if (name.endsWith(".so") || name.endsWith(".a") || name.endsWith(".dll"))
               exsitNativeFile = true;

            if (name.equals(RAXML_FILE))
            {
               InputStream raIn = zipFile.getInputStream(ze);
               RaParser parser = new RaParser();
               connector = parser.parse(raIn);
               raIn.close();

            }
         }

         root = getRoot(rarFile);
         loadNativeLibraries(root);
         cl = loadClass(cps);

         // Annotation scanning
         if (scanArchive(connector))
         {
            Annotations annotator = new Annotations();
            AnnotationScanner scanner = AnnotationScannerFactory.getAnnotationScanner();
            AnnotationRepository repository = scanner.scan(cl.getURLs(), cl);
            connector = annotator.merge(connector, repository, cl);
         }

         if (connector == null)
         {
            System.out.println("can't parse ra.xml");
            System.exit(OTHER);
         }

         if (stdout)
         {
            out = System.out;
         }
         else if (!reportFile.isEmpty())
         {
            out = new PrintStream(reportFile);
         }
         else
         {
            out = new PrintStream(rarFile.substring(0, rarFile.length() - 4) + REPORT_FILE);
         }
         
         String archiveFile;
         int sep = rarFile.lastIndexOf(File.separator);
         if (sep > 0)
            archiveFile = rarFile.substring(sep + 1);
         else
            archiveFile = rarFile;
         
         out.println("Archive:\t" + archiveFile);
         
         String version;
         String type = "";
         ResourceAdapter ra = connector.getResourceadapter();
         boolean reauth = false;
         if (connector.getVersion() == Version.V_10)
         {
            version = "1.0";
            type = "OutBound";
            reauth = ra.getOutboundResourceadapter().getReauthenticationSupport();
         }
         else
         {
            if (connector.getVersion() == Version.V_15)
               version = "1.5";
            else if (connector.getVersion() == Version.V_16)
               version = "1.6";
            else
               version = "1.7";
            if (ra.getOutboundResourceadapter() != null)
            {
               reauth = ra.getOutboundResourceadapter().getReauthenticationSupport();
               if (ra.getInboundResourceadapter() != null &&
                   ra.getInboundResourceadapter().getMessageadapter() != null &&
                   ra.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null &&
                   ra.getInboundResourceadapter().getMessageadapter().getMessagelisteners().size() > 0)
                  type = "Bidirectional";
               else
                  type = "OutBound";
            }
            else
            {
               if (ra.getInboundResourceadapter() != null)
                  type = "InBound";
               else
               {
                  out.println("Rar file has problem");
                  System.exit(ERROR);
               }
            }
         }
         out.println("JCA version:\t" + version);
         out.println("Type:\t\t" + type);
         
         if (type.equals("Bidirectional") || type.equals("OutBound"))
         {
            if (ra.getOutboundResourceadapter() != null)
            {
               out.println("Transaction:\t" + ra.getOutboundResourceadapter().getTransactionSupport());
            }
         }
         
         out.print("Reauth:\t\t");
         if (reauth)
            out.println("Yes");
         else
            out.println("No");

         int systemExitCode = Validation.validate(new File(rarFile).toURI().toURL(), ".", cps);
         
         String compliant;
         if (systemExitCode == SUCCESS)
            compliant = "Yes";
         else
            compliant = "No";
         out.println("Compliant:\t" + compliant);

         out.print("Native:\t\t");
         if (exsitNativeFile)
            out.println("Yes");
         else
            out.println("No");
         
         if (cps != null)
         {
            out.print("Extra Classpath: ");
            out.println(Arrays.toString(cps).replaceAll("\\[|\\]", ""));
         }

         Collections.sort(names);
         
         out.println();
         out.println("Structure:");
         out.println("----------");
         for (String name : names)
         {
            out.println(name);
         }
         
         String mcfClassName = "";
         Map<String, String> raConfigProperties = null;
         TransactionSupportEnum transSupport = TransactionSupportEnum.NoTransaction;
         List<AdminObject> adminObjects = null;
         List<ConnectionDefinition> connDefs = null;

         SecurityImpl secImpl = new SecurityImpl("", "", true);
         PoolImpl poolImpl = new PoolImpl(0, null, 10, Defaults.PREFILL, Defaults.USE_STRICT_MIN, 
                                          Defaults.FLUSH_STRATEGY, null, null);
         XaPoolImpl xaPoolImpl = new XaPoolImpl(0, null, 10, Defaults.PREFILL, Defaults.USE_STRICT_MIN, 
                                                Defaults.FLUSH_STRATEGY, null, Defaults.FAIR,
                                                Defaults.IS_SAME_RM_OVERRIDE, Defaults.INTERLEAVING,
                                                Defaults.PAD_XID, Defaults.WRAP_XA_RESOURCE,
                                                Defaults.NO_TX_SEPARATE_POOL);

         Map<String, String> introspected;
            
         if (ra.getResourceadapterClass() != null && !ra.getResourceadapterClass().equals(""))
         {
            out.println();
            out.println("Resource-adapter:");
            out.println("-----------------");
            out.println("Class: " + ra.getResourceadapterClass());
               
            introspected = getIntrospectedProperties(ra.getResourceadapterClass(), cl);

            if (ra.getConfigProperties() != null)
            {
               raConfigProperties = new HashMap<String, String>();
               for (ConfigProperty cp : ra.getConfigProperties())
               {
                  raConfigProperties.put(getValueString(cp.getConfigPropertyName()),
                                         getValueString(cp.getConfigPropertyValue()));
                  
                  removeIntrospectedValue(introspected, getValueString(cp.getConfigPropertyName()));
                  
                  out.println("  Config-property: " + getValueString(cp.getConfigPropertyName()) + " ("
                              + getValueString(cp.getConfigPropertyType()) + ")");
               }
            }

            if (introspected != null && !introspected.isEmpty())
            {
               for (Map.Entry<String, String> entry : introspected.entrySet())
               {
                  out.println("  Introspected Config-property: " + entry.getKey() + " (" + entry.getValue() + ")");
               }
            }

            if (introspected == null)
               out.println("  Unable to resolve introspected config-property's");
            
         }

         int line = 0;
         Set<String> sameClassnameSet = new HashSet<String>();
         boolean needPrint = true;

         if (ra.getOutboundResourceadapter() != null)
         {
            out.println();
            out.println("Managed-connection-factory:");
            out.println("---------------------------");
            
            if (ra.getOutboundResourceadapter().getConnectionDefinitions() != null)
               connDefs = new ArrayList<ConnectionDefinition>();
               
            transSupport = ra.getOutboundResourceadapter().getTransactionSupport();
            for (org.jboss.jca.common.api.metadata.spec.ConnectionDefinition mcf :
                    ra.getOutboundResourceadapter().getConnectionDefinitions())
            {
               mcfClassName = getValueString(mcf.getManagedConnectionFactoryClass());
               if (!sameClassnameSet.contains(mcfClassName))
               {
                  sameClassnameSet.add(mcfClassName);
                  if (line != 0)
                  {
                     out.println();
                  }
                  line++;
                  out.println("Class: " + mcfClassName);
                  needPrint = true;
               }
               else
               {
                  needPrint = false;
               }
                  
               if (needPrint)
               {
                  //ValidatingManagedConnectionFactory
                  hasValidatingMcfInterface(out, mcfClassName, cl);

                  //ResourceAdapterAssociation
                  hasResourceAdapterAssociation(out, mcfClassName, cl);
                  
                  //ManagedConnectionFactory implements javax.resource.spi.TransactionSupport
                  hasMcfTransactionSupport(out, mcfClassName, cl);
                  
                  //DissociatableManagedConnection
                  hasDissociatableMcInterface(out, mcfClassName, cl, mcf.getConfigProperties());
                     
                  //LazyEnlistableManagedConnection
                  hasEnlistableMcInterface(out, mcfClassName, cl, mcf.getConfigProperties());

                  //CCI
                  String cfi = getValueString(mcf.getConnectionFactoryInterface());
                  try
                  {
                     out.print("  CCI: ");
                     Class<?> clazz = Class.forName(cfi, true, cl);
                     if (isCCI(cfi, clazz, mcfClassName, cl))
                     {
                        out.println("Yes");
                     }
                     else
                     {
                        out.println("No");
                        
                        out.println("  ConnectionFactory (" + cfi + "):");
                        outputMethodInfo(out, clazz, cl);
                        
                        Class<?> ci = Class.forName(getValueString(mcf.getConnectionInterface()), true, cl);
                        out.println("  Connection (" + getValueString(mcf.getConnectionInterface()) + "):");
                        outputMethodInfo(out, ci, cl);
                     }
                  }
                  catch (Throwable t)
                  {
                     // Nothing we can do
                     t.printStackTrace(System.err);
                     out.println("Unknown");
                  }
               }
                  
               Map<String, String> configProperty = null;
               if (mcf.getConfigProperties() != null)
                  configProperty = new HashMap<String, String>();

               introspected = getIntrospectedProperties(mcfClassName, cl);

               for (ConfigProperty cp : mcf.getConfigProperties())
               {
                  configProperty.put(getValueString(cp.getConfigPropertyName()), 
                                     getValueString(cp.getConfigPropertyValue()));

                  removeIntrospectedValue(introspected, getValueString(cp.getConfigPropertyName()));
                     
                  if (needPrint)
                     out.println("  Config-property: " + getValueString(cp.getConfigPropertyName()) + " (" +
                                 getValueString(cp.getConfigPropertyType()) + ")");
               }

               if (introspected != null && !introspected.isEmpty())
               {
                  for (Map.Entry<String, String> entry : introspected.entrySet())
                  {
                     if (needPrint)
                        out.println("  Introspected Config-property: " + entry.getKey() + " (" +
                                    entry.getValue() + ")");
                  }
               }

               if (introspected == null)
                  out.println("  Unable to resolve introspected config-property's");

               String poolName = getValueString(mcf.getConnectionInterface()).substring(
                  getValueString(mcf.getConnectionInterface()).lastIndexOf('.') + 1);
               Pool pool = null;
               ConnectionDefinitionImpl connImpl;
               if (transSupport.equals(TransactionSupportEnum.XATransaction))
               {
                  pool = xaPoolImpl;
                  Recovery recovery = new Recovery(new CredentialImpl(null, null, "domain"), null, false);
                  connImpl = new ConnectionDefinitionImpl(configProperty, mcfClassName, "java:jboss/eis/" + poolName,
                                                          poolName, Defaults.ENABLED, Defaults.USE_JAVA_CONTEXT,
                                                          Defaults.USE_CCM, Defaults.SHARABLE, Defaults.ENLISTMENT,
                                                          Defaults.CONNECTABLE, Defaults.TRACKING, Defaults.MCP,
                                                          Defaults.ENLISTMENT_TRACE,
                                                          pool, null, null,
                                                          secImpl, recovery, Boolean.TRUE);
               }
               else
               {
                  pool = poolImpl;
                  connImpl = new ConnectionDefinitionImpl(configProperty, mcfClassName, "java:jboss/eis/" + poolName,
                                                          poolName, Defaults.ENABLED, Defaults.USE_JAVA_CONTEXT,
                                                          Defaults.USE_CCM, Defaults.SHARABLE, Defaults.ENLISTMENT,
                                                          Defaults.CONNECTABLE, Defaults.TRACKING, Defaults.MCP,
                                                          Defaults.ENLISTMENT_TRACE,
                                                          pool, null, null,
                                                          secImpl, null, Boolean.FALSE);
               }
               
               connDefs.add(connImpl);
            }
            
         }

         line = 0;
         sameClassnameSet.clear();

         if (ra.getAdminObjects() != null && ra.getAdminObjects().size() > 0)
         {
            out.println();
            out.println("Admin-object:");
            out.println("-------------");
            adminObjects = new ArrayList<AdminObject>();

            for (org.jboss.jca.common.api.metadata.spec.AdminObject ao : ra.getAdminObjects())
            {
               String aoClassname = getValueString(ao.getAdminobjectClass());
               if (!sameClassnameSet.contains(aoClassname))
               {
                  sameClassnameSet.add(aoClassname);
                  if (line != 0)
                  {
                     out.println();
                  }
                  line++;
                  out.println("Class: " + aoClassname);

                  //ResourceAdapterAssociation
                  hasResourceAdapterAssociation(out, aoClassname, cl);

                  out.println("  Interface: " + getValueString(ao.getAdminobjectInterface()));
                  needPrint = true;
               }
               else
               {
                  needPrint = false;
               }

               String poolName = aoClassname.substring(aoClassname.lastIndexOf('.') + 1);
               Map<String, String> configProperty = null;
               if (ao.getConfigProperties() != null)
                  configProperty = new HashMap<String, String>();
               
               introspected = getIntrospectedProperties(aoClassname, cl);
               
               for (ConfigProperty cp : ao.getConfigProperties())
               {
                  configProperty.put(getValueString(cp.getConfigPropertyName()), 
                                     getValueString(cp.getConfigPropertyValue()));
                  
                  removeIntrospectedValue(introspected, getValueString(cp.getConfigPropertyName()));
                  
                  if (needPrint)
                     out.println("  Config-property: " + getValueString(cp.getConfigPropertyName()) + " (" +
                                 getValueString(cp.getConfigPropertyType()) + ")");
               }

               if (introspected != null && !introspected.isEmpty())
               {
                  for (Map.Entry<String, String> entry : introspected.entrySet())
                  {
                     if (needPrint)
                        out.println("  Introspected Config-property: " + entry.getKey() + " (" +
                                    entry.getValue() + ")");
                  }
               }
                  
               if (introspected == null)
                  out.println("  Unable to resolve introspected config-property's");

               AdminObjectImpl aoImpl = new AdminObjectImpl(configProperty, aoClassname,
                  "java:jboss/eis/ao/" + poolName, poolName, Defaults.ENABLED, Defaults.USE_JAVA_CONTEXT);
               adminObjects.add(aoImpl);
            }
         }
            
         line = 0;
         sameClassnameSet.clear();

         if (ra.getInboundResourceadapter() != null && 
             ra.getInboundResourceadapter().getMessageadapter() != null &&
             ra.getInboundResourceadapter().getMessageadapter().getMessagelisteners() != null &&
             ra.getInboundResourceadapter().getMessageadapter().getMessagelisteners().size() > 0)
         {
            out.println();
            out.println("Activation-spec:");
            out.println("----------------");
            for (MessageListener ml : 
                    ra.getInboundResourceadapter().getMessageadapter().getMessagelisteners())
            {
               String asClassname = getValueString(ml.getActivationspec().getActivationspecClass());
               if (!sameClassnameSet.contains(asClassname))
               {
                  sameClassnameSet.add(asClassname);
                  if (line != 0)
                  {
                     out.println();
                  }
                  line++;
                  out.println("Class: " + asClassname);
                  out.println("  Message-listener: " + getValueString(ml.getMessagelistenerType()));

                  introspected = getIntrospectedProperties(asClassname, cl);

                  if (ml.getActivationspec() != null && 
                      ml.getActivationspec().getRequiredConfigProperties() != null)
                  {
                     for (RequiredConfigProperty cp :  ml.getActivationspec().getRequiredConfigProperties())
                     {
                        removeIntrospectedValue(introspected, getValueString(cp.getConfigPropertyName()));
                           
                        out.println("  Required-config-property: " + getValueString(cp.getConfigPropertyName()));
                     }
                  }

                  if (introspected != null && !introspected.isEmpty())
                  {
                     for (Map.Entry<String, String> entry : introspected.entrySet())
                     {
                        out.println("  Introspected Config-property: " + entry.getKey() + " (" +
                                    entry.getValue() + ")");
                     }
                  }

                  if (introspected == null)
                     out.println("  Unable to resolve introspected config-property's");
               }
            }
         }

         RaImpl raImpl = new RaImpl(archiveFile, transSupport, connDefs, adminObjects, raConfigProperties);
         raImpl.buildResourceAdapterImpl();

         outputMenifest("META-INF/MANIFEST.MF", out);
         
         outputXmlDesc(xmls, out);

         outputRaDesc(raImpl, out);
         
         System.out.println("Done.");
         System.exit(SUCCESS);
      }
      catch (Throwable t)
      {
         System.err.println("Error: " + t.getMessage());
         t.printStackTrace(System.err);
         System.exit(ERROR);
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (Exception ioe)
            {
               // Ignore
            }
         }
         if (cl != null)
         {
            try
            {
               cl.close();
            }
            catch (Exception ioe)
            {
               // Ignore
            }
         }
         if (zipFile != null)
         {
            try
            {
               zipFile.close();
            }
            catch (Exception ioe)
            {
               // Ignore
            }
         }
         cleanupTempFiles();
      }
   }

   private static void outputMethodInfo(PrintStream out, Class<?> clazz, URLClassLoader cl) 
      throws ClassNotFoundException
   {
      Method[] methods = clazz.getMethods();
      for (Method method : methods)
      {
         // Output return type, method name, parameters, exceptions
         out.print("    " + singleName(method.getReturnType().getCanonicalName()) + " " + 
            method.getName() + "(");
         Class<?>[] params = method.getParameterTypes();
         for (int i = 0; i < params.length; i++)
         {
            out.print(singleName(params[i].getCanonicalName()));
            if (i + 1 < params.length)
               out.print(", ");
         }
         out.print(") ");
         Class<?>[] exceptions = method.getExceptionTypes();
         if (exceptions.length > 0)
         {
            out.print("throws ");
            for (int i = 0; i < exceptions.length; i++)
            {
               out.print(singleName(exceptions[i].getCanonicalName()));
               if (i + 1 < exceptions.length)
                  out.print(", ");
            }
         }
         out.println();
      }
   }
   
   private static String singleName(String className)
   {
      int lastPos = className.lastIndexOf(".");
      if (lastPos < 0)
         return className;
      else
         return className.substring(lastPos + 1);
   }

   /**
    * hasValidatingMcfInterface
    * 
    * @param out output stream
    * @param classname classname
    * @param cl classloader
    */
   private static void hasValidatingMcfInterface(PrintStream out, String classname, URLClassLoader cl)
   {
      try
      {
         out.print("  Validating: ");
         Class<?> clazz = Class.forName(classname, true, cl);

         if (hasInterface(clazz, "javax.resource.spi.ValidatingManagedConnectionFactory"))
         {
            out.println("Yes");
         }
         else
         {
            out.println("No");
         }
      }
      catch (Throwable t)
      {
         // Nothing we can do
         t.printStackTrace(System.err);
         out.println("Unknown");
      }
   }
   
   /**
    * hasResourceAdapterAssociation
    *
    * @param out output stream
    * @param classname classname
    * @param cl classloader
    */
   private static void hasResourceAdapterAssociation(PrintStream out, String classname, URLClassLoader cl)
   {
      try
      {
         out.print("  Association: ");
         Class<?> clazz = Class.forName(classname, true, cl);

         if (hasInterface(clazz, "javax.resource.spi.ResourceAdapterAssociation"))
         {
            out.println("Yes");
         }
         else
         {
            out.println("No");
         }
      }
      catch (Throwable t)
      {
         // Nothing we can do
         t.printStackTrace(System.err);
         out.println("Unknown");
      }
   }
   
   /**
    * hasMcfTransactionSupport
    *
    * @param out output stream
    * @param classname classname
    * @param cl classloader
    */
   private static void hasMcfTransactionSupport(PrintStream out, String classname, URLClassLoader cl)
   {
      try
      {
         out.print("  TransactionSupport: ");
         
         Class<?> mcfClz = Class.forName(classname, true, cl);
         ManagedConnectionFactory mcf = (ManagedConnectionFactory)mcfClz.newInstance();

         if (hasInterface(mcf.getClass(),  "javax.resource.spi.TransactionSupport"))
         {
            out.println("Yes");
         } 
         else
         {
            out.println("No");
         }
      }
      catch (Throwable t)
      {
         // Nothing we can do
         t.printStackTrace(System.err);
         out.println("Unknown");
      }
   }

   private static void hasDissociatableMcInterface(PrintStream out, String classname, URLClassLoader cl, 
         List<? extends ConfigProperty> listConfProp)
   {
      hasMcInterface(out, classname, cl, listConfProp, "DissociatableManagedConnection", "Sharable");
   }
   
   private static void hasEnlistableMcInterface(PrintStream out, String classname, URLClassLoader cl,
         List<? extends ConfigProperty> listConfProp)
   {
      hasMcInterface(out, classname, cl, listConfProp, "LazyEnlistableManagedConnection", "Enlistment");
   }
   
   private static void hasMcInterface(PrintStream out, String classname, URLClassLoader cl, 
         List<? extends ConfigProperty> listConfProp, String mcClassName, String tip)
   {
      ManagedConnection mcClz = null;
      try
      {
         out.print("  " + tip + ": ");
         Class<?> mcfClz = Class.forName(classname, true, cl);
         ManagedConnectionFactory mcf = (ManagedConnectionFactory)mcfClz.newInstance();
         
         Injection injector = new Injection(); 
         for (ConfigProperty cp : listConfProp)
         {
            if (!XsdString.isNull(cp.getConfigPropertyValue()))
            {
               injector.inject(mcf, cp.getConfigPropertyName().getValue(), 
                  cp.getConfigPropertyValue().getValue(), cp.getConfigPropertyType().getValue());
            }
         }
         
         mcClz = mcf.createManagedConnection(null, null); 

         if (hasInterface(mcClz.getClass(), "javax.resource.spi." + mcClassName))
         {
            out.println("Yes");
         }
         else
         {
            out.println("No");
         }
      }
      catch (Throwable t)
      {
         // Nothing we can do
         t.printStackTrace(System.err);
         out.println("Unknown");
      }
      finally
      {
         if (mcClz != null)
         {
            try
            {
               mcClz.destroy();
            }
            catch (ResourceException e)
            {
               e.printStackTrace();
            }
         }
      }
   }
   
   private static void outputMenifest(String meniFile, PrintStream out) throws FileNotFoundException, IOException
   {
      out.println();
      out.println(meniFile + ":");
      for (int i = 0; i <= meniFile.length(); i++)
      {
         out.print("-");
      }
      out.println();

      outToFile(meniFile, out);

   }
   
   private static void outputXmlDesc(ArrayList<String> xmls, PrintStream out) throws FileNotFoundException, IOException
   {
      for (String xmlfile : xmls)
      {
         out.println();
         out.println(xmlfile + ":");
         for (int i = 0; i <= xmlfile.length(); i++)
         {
            out.print("-");
         }
         out.println();
         
         outToFile(xmlfile, out);
      }
   }

   private static void outToFile(String fileName, PrintStream out) throws FileNotFoundException, IOException
   {
      Reader in = null;
      try
      {
         in = new FileReader(root.getAbsolutePath() + File.separator + fileName);

         char[] buffer = new char[4096];
         for (;;)
         {
            int nBytes = in.read(buffer);
            if (nBytes <= 0)
               break;

            for (int i = 0; i < nBytes; i++)
            {
               if (buffer[i] != 13)
                  out.print(buffer[i]);
            }
         }
         out.flush();
      }
      finally
      {
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
   
   private static boolean isCCI(String cfi, Class<?> clazz, String mcfClassName, URLClassLoader cl) throws Exception
   { 
      if (cfi.equals("javax.resource.cci.ConnectionFactory"))
         return true;
      
      if (hasInterface(clazz, "javax.resource.cci.ConnectionFactory"))
         return true;
      
      Class mcfClazz = Class.forName(mcfClassName, true, cl);
                     
      Method m = mcfClazz.getMethod("createConnectionFactory", (Class[]) null);
                     
      Class rt = m.getReturnType();
         
      if (rt.isAssignableFrom(javax.resource.cci.ConnectionFactory.class))
         return true;
 
      Object mcfInstance = mcfClazz.newInstance();
      Object result = m.invoke(mcfInstance, (Object[])null);
      if (result instanceof javax.resource.cci.ConnectionFactory)
         return true;
      
      return false;
   }

   /**
    * Output Resource Adapter XML description
    * 
    * @param raImpl RaImpl
    * @param out PrintStream
    * @throws ParserConfigurationException
    * @throws SAXException
    * @throws IOException
    * @throws TransformerFactoryConfigurationError
    * @throws TransformerConfigurationException
    * @throws TransformerException
    */
   private static void outputRaDesc(RaImpl raImpl, PrintStream out) throws ParserConfigurationException, SAXException,
         IOException, TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException
   {
      String raString = "<resource-adapters>" + raImpl.toString() + "</resource-adapters>";

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new InputSource(new StringReader(raString)));
      
      out.println();
      out.println("Deployment descriptor:");
      out.println("----------------------");

      TransformerFactory tfactory = TransformerFactory.newInstance();
      Transformer serializer;

      serializer = tfactory.newTransformer();
      //Setup indenting to "pretty print"
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
      serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      serializer.transform(new DOMSource(doc), new StreamResult(out));
   }

   /**
    * Should the archive be scanned for annotations
    * @param cmd The metadata
    * @return True if scan is needed; otherwise false
    */
   private static boolean scanArchive(Connector cmd)
   {
      if (cmd == null)
         return true;

      if (cmd.getVersion() == Version.V_16 || cmd.getVersion() == Version.V_17 || cmd.getVersion() == Version.V_20)
      {
         if (!cmd.isMetadataComplete())
            return true;
      }

      return false;
   }

   private static File getRoot(String rarFile)
   {
      if (rarFile == null)
         throw new IllegalArgumentException("Rar file name is null");

      try
      {
         File f = new File(rarFile);

         if (f.isFile())
         {
            File destination = new File(tempdir, subdir);
            return extract(f, destination);
         }
         else
         {
            return f;
         }
      }
      catch (Throwable t)
      {
         // Nothing we can do
      }
      return null;
   }

   private static URLClassLoader loadClass(String[] classpath)
   {
      try
      {
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

         return cl;
      }
      catch (Throwable t)
      {
         // Nothing we can do
      }
      return null;
   }

   private static void cleanupTempFiles()
   {
      File destination = new File(tempdir, subdir);
      if (destination.exists())
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
   }

   
   private static boolean hasInterface(Class<?> clazz, String interfaceName)
   {
      for (Class<?> iface : getAllInterfaces(clazz))
      {
         if (iface.getName().equals(interfaceName))
         {
            return true;
         }
      }

      return false;
   }

   private static List<Class<?>> getAllInterfaces(Class<?> cls)
   {
      if (cls == null)
      {
         return null;
      }

      LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<Class<?>>();
      getAllInterfaces(cls, interfacesFound);

      return new ArrayList<Class<?>>(interfacesFound);
   }

   private static void getAllInterfaces(Class<?> cls, HashSet<Class<?>> interfacesFound)
   {
      while (cls != null)
      {
         Class<?>[] interfaces = cls.getInterfaces();

         for (Class<?> i : interfaces)
         {
            if (interfacesFound.add(i))
            {
               getAllInterfaces(i, interfacesFound);
            }
         }

         cls = cls.getSuperclass();
      }
   }
   
   /**
    * Get the introspected properties for a class
    * @param clz The fully qualified class name
    * @param cl classloader
    * @return The properties (name, type)
    */
   private static Map<String, String> getIntrospectedProperties(String clz, URLClassLoader cl)
   {
      Map<String, String> result = null;

      try
      {
         Class<?> c = Class.forName(clz, true, cl);

         result = new TreeMap<String, String>();

         Method[] methods = c.getMethods();

         for (Method m : methods)
         {
            if (m.getName().startsWith("set") && m.getParameterTypes().length == 1
                  && isValidType(m.getParameterTypes()[0]))
            {
               String name = m.getName().substring(3);

               if (name.length() == 1)
               {
                  name = name.toLowerCase(Locale.US);
               }
               else
               {
                  name = name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
               }

               String type = m.getParameterTypes()[0].getName();

               result.put(name, type);
            }
         }
      }
      catch (Throwable t)
      {
         // Nothing we can do
         t.printStackTrace(System.err);
      }
      return result;
   }

   /**
    * Is valid type
    * @param type The type
    * @return True if valid; otherwise false
    */
   private static boolean isValidType(Class<?> type)
   {
      return validTypes.contains(type);
   }

   /**
    * Remove introspected value
    * @param m The map
    * @param name The name
    */
   private static void removeIntrospectedValue(Map<String, String> m, String name)
   {
      if (m != null)
      {
         m.remove(name);

         if (name.length() == 1)
         {
            name = name.toUpperCase(Locale.US);
         }
         else
         {
            name = name.substring(0, 1).toUpperCase(Locale.US) + name.substring(1);
         }

         m.remove(name);

         if (name.length() == 1)
         {
            name = name.toLowerCase(Locale.US);
         }
         else
         {
            name = name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
         }

         m.remove(name);
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
                     {
                        in.close();
                     }
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
    * get correct value string 
    * @param value xsdstring
    * @return correct string
    */
   private static String getValueString(XsdString value)
   {
      if (value == null || value == XsdString.NULL_XSDSTRING)
         return "";
      else
         return value.getValue();
   }

   /**
    * Tool usage
    */
   private static void usage()
   {
      System.out.println("Usage:  ./rar-info.sh [-classpath <lib>[:<lib>]*] [--stdout] [-o <reportFile>] <file>");
   }
}
