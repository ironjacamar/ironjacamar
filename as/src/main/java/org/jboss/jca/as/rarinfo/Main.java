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
package org.jboss.jca.as.rarinfo;

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.RequiredConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.metadata.common.CommonAdminObjectImpl;
import org.jboss.jca.common.metadata.common.CommonConnDefImpl;
import org.jboss.jca.common.metadata.common.CommonPoolImpl;
import org.jboss.jca.common.metadata.common.CommonSecurityImpl;
import org.jboss.jca.common.metadata.common.CommonXaPoolImpl;
import org.jboss.jca.common.metadata.ra.RaParser;
import org.jboss.jca.validator.Validation;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

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
   
   /**
    * Main
    * @param args args 
    */
   public static void main(String[] args)
   {
      PrintStream out = null;
      try
      {
         if (args.length < 1 || args.length == 2 || args.length > 3)
         {
            usage();
            System.exit(OTHER);
         }
         
         String rarFile = "";
         String[] cps = null;
         if (args.length == 1 && args[0].endsWith("rar"))
         {
            rarFile = args[0];
         }
         else if (args.length == 3 && args[0].equals("-classpath") && args[2].endsWith("rar"))
         {
            rarFile = args[2];
            cps = args[1].split(System.getProperty("path.separator"));
         }
         else
         {
            usage();
            System.exit(OTHER);
         }

         ZipFile zipFile = new ZipFile(rarFile);

         boolean hasRaXml = false;
         boolean exsitNativeFile = false;
         Connector connector = null;

         Enumeration zipEntries = zipFile.entries();

         while (zipEntries.hasMoreElements())
         {
            ZipEntry ze = (ZipEntry) zipEntries.nextElement();
            String name = ze.getName();
            if (name.endsWith(".so") || name.endsWith(".a") || name.endsWith(".dll"))
               exsitNativeFile = true;

            if (name.equals(RAXML_FILE))
            {
               hasRaXml = true;
               InputStream raIn = zipFile.getInputStream(ze);
               RaParser parser = new RaParser();
               connector = parser.parse(raIn);
               raIn.close();

            }
         }

         if (!hasRaXml)
         {
            System.out.println("JCA annotations aren't supported");
            System.exit(OTHER);
         }
         if (connector == null)
         {
            System.out.println("can't parse ra.xml");
            System.exit(OTHER);
         }

         out = new PrintStream(rarFile.substring(0, rarFile.length() - 4) + REPORT_FILE);
         out.println("Archive:\t" + rarFile);
         
         String version;
         String type = "";
         ResourceAdapter ra;
         boolean reauth = false;
         if (connector.getVersion() == Version.V_10)
         {
            version = "1.0";
            ra = connector.getResourceadapter();
            type = "OutBound";
            reauth = ((ResourceAdapter10)ra).getReauthenticationSupport();
         }
         else
         {
            if (connector.getVersion() == Version.V_15)
               version = "1.5";
            else
               version = "1.6";
            ResourceAdapter1516 ra1516 = (ResourceAdapter1516)connector.getResourceadapter();
            ra = ra1516;
            if (ra1516.getOutboundResourceadapter() != null)
            {
               reauth = ra1516.getOutboundResourceadapter().getReauthenticationSupport();
               if (ra1516.getInboundResourceadapter() != null)
                  type = "Bidirectional";
               else
                  type = "OutBound";
            }
            else
            {
               if (ra1516.getInboundResourceadapter() != null)
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
         
         String classname = "";
         Map<String, String> raConfigProperties = null;
         TransactionSupportEnum transSupport = TransactionSupportEnum.NoTransaction;
         List<CommonAdminObject> adminObjects = null;
         List<CommonConnDef> connDefs = null;

         CommonSecurityImpl secImpl = new CommonSecurityImpl("", "", true);
         CommonPoolImpl poolImpl = new CommonPoolImpl(0, 0, Defaults.PREFILL, Defaults.USE_STRICT_MIN, 
               Defaults.FLUSH_STRATEGY);
         CommonXaPoolImpl xaPoolImpl = new CommonXaPoolImpl(0, 0, Defaults.PREFILL, Defaults.USE_STRICT_MIN, 
               Defaults.FLUSH_STRATEGY, Defaults.IS_SAME_RM_OVERRIDE, Defaults.INTERLEAVING,
               Defaults.PAD_XID, Defaults.WRAP_XA_RESOURCE, Defaults.NO_TX_SEPARATE_POOL);

         if (connector.getVersion() != Version.V_10)
         {
            ResourceAdapter1516 ra1516 = (ResourceAdapter1516)ra;
            out.println();
            out.println("Resource-adapter:");
            out.println("-----------------");
            out.println("Class: " + ra1516.getResourceadapterClass());
            
            if (ra1516.getConfigProperties() != null)
            {
               raConfigProperties = new HashMap<String, String>();
               for (ConfigProperty cp : ra1516.getConfigProperties())
               {
                  raConfigProperties.put(cp.getConfigPropertyName().toString(), 
                        getValueString(cp.getConfigPropertyValue()));
                  out.println("  Config-property: " + cp.getConfigPropertyName() + " (" +
                        cp.getConfigPropertyType() + ")");
               }
            }
            
            int line = 0;
            String sameClassname = "";
            out.println();
            out.println("Managed-connection-factory:");
            out.println("---------------------------");
            if (ra1516.getOutboundResourceadapter() != null)
            {
               if (ra1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
                  connDefs = new ArrayList<CommonConnDef>();
               
               transSupport = ra1516.getOutboundResourceadapter().getTransactionSupport();
               for (ConnectionDefinition mcf : ra1516.getOutboundResourceadapter().getConnectionDefinitions())
               {
                  classname = mcf.getManagedConnectionFactoryClass().toString();
                  if (!classname.equals(sameClassname))
                  {
                     sameClassname = classname;
                     if (line != 0)
                     {
                        out.println();
                     }
                     line++;
                     out.println("Class: " + classname);
                  }
                  
                  Map<String, String> configProperty = null;
                  if (mcf.getConfigProperties() != null)
                     configProperty = new HashMap<String, String>();
                  for (ConfigProperty cp : mcf.getConfigProperties())
                  {
                     configProperty.put(cp.getConfigPropertyName().toString(), 
                           getValueString(cp.getConfigPropertyValue()));
                     out.println("  Config-property: " + cp.getConfigPropertyName() + " (" +
                           cp.getConfigPropertyType() + ")");
                  }
                  String poolName = mcf.getConnectionInterface().toString().substring(
                        mcf.getConnectionInterface().toString().lastIndexOf('.') + 1);
                  CommonPool pool = null;
                  if (transSupport.equals(TransactionSupportEnum.XATransaction))
                  {
                     pool = poolImpl;
                  }
                  else
                  {
                     pool = xaPoolImpl;
                  }
                  CommonConnDefImpl connImpl = new CommonConnDefImpl(configProperty, classname, 
                        "java:jboss/eis/" + poolName, poolName, 
                        Defaults.ENABLED, Defaults.USE_JAVA_CONTEXT, Defaults.USE_CCM, 
                        pool, null, null, secImpl, null);
                  connDefs.add(connImpl);
               }

            }

            line = 0;
            sameClassname = "";
            out.println();
            out.println("Admin-object:");
            out.println("-------------");
            
            if (ra1516.getAdminObjects() != null)
            {
               adminObjects = new ArrayList<CommonAdminObject>();
            }
            for (AdminObject ao : ra1516.getAdminObjects())
            {
               String aoClassname = ao.getAdminobjectClass().toString();
               if (!aoClassname.equals(sameClassname))
               {
                  sameClassname = aoClassname;
                  if (line != 0)
                  {
                     out.println();
                  }
                  line++;
                  out.println("Class: " + aoClassname);
               }

               String poolName = aoClassname.substring(aoClassname.lastIndexOf('.') + 1);
               Map<String, String> configProperty = null;
               if (ao.getConfigProperties() != null)
                  configProperty = new HashMap<String, String>();
               for (ConfigProperty cp : ao.getConfigProperties())
               {
                  configProperty.put(cp.getConfigPropertyName().toString(), 
                        getValueString(cp.getConfigPropertyValue()));
                  out.println("  Config-property: " + cp.getConfigPropertyName() + " (" +
                        cp.getConfigPropertyType() + ")");
               }
               CommonAdminObjectImpl aoImpl = new CommonAdminObjectImpl(configProperty, aoClassname,
                     "java:jboss/eis/ao/" + poolName, poolName, Defaults.ENABLED, Defaults.USE_JAVA_CONTEXT);
               adminObjects.add(aoImpl);
            }
            
            line = 0;
            sameClassname = "";
            out.println();
            out.println("Activation-spec:");
            out.println("----------------");
            if (ra1516.getInboundResourceadapter() != null && 
               ra1516.getInboundResourceadapter().getMessageadapter() != null)
            {
               for (MessageListener ml : 
                  ra1516.getInboundResourceadapter().getMessageadapter().getMessagelisteners())
               {
                  String asClassname = ml.getActivationspec().getActivationspecClass().toString();
                  if (!asClassname.equals(sameClassname))
                  {
                     sameClassname = asClassname;
                     if (line != 0)
                     {
                        out.println();
                     }
                     line++;
                     out.println("Class: " + asClassname);
                  }
                  if (ml.getActivationspec() != null && 
                     ml.getActivationspec().getRequiredConfigProperties() != null)
                  {
                     for (RequiredConfigProperty cp :  ml.getActivationspec().getRequiredConfigProperties())
                     {
                        out.println("  Required-config-property: " + cp.getConfigPropertyName());
                     }
                  }
               }
            }
         }
         else
         {
            out.println("Managed-connection-factory:");
            out.println("---------------------------");

            ResourceAdapter10 ra10 = (ResourceAdapter10)ra;
            out.println("Class: " + ra10.getManagedConnectionFactoryClass());
            
            classname = ra10.getManagedConnectionFactoryClass().toString();
            transSupport = ra10.getTransactionSupport();
            
            Map<String, String> configProperty = null;
            if (ra10.getConfigProperties() != null)
               configProperty = new HashMap<String, String>();
            for (ConfigProperty cp : ra10.getConfigProperties())
            {
               configProperty.put(cp.getConfigPropertyName().toString(), 
                     getValueString(cp.getConfigPropertyValue()));
               out.println("  Config-property: " + cp.getConfigPropertyName() + " (" +
                     cp.getConfigPropertyType() + ")");
            }
            String poolName = classname.substring(classname.lastIndexOf('.') + 1);
            CommonPool pool = null;
            if (transSupport.equals(TransactionSupportEnum.XATransaction))
            {
               pool = poolImpl;
            }
            else
            {
               pool = xaPoolImpl;
            }
            CommonConnDefImpl connImpl = new CommonConnDefImpl(configProperty, classname, 
                  "java:jboss/eis/" + poolName, poolName, 
                  Defaults.ENABLED, Defaults.USE_JAVA_CONTEXT, Defaults.USE_CCM, 
                  pool, null, null, secImpl, null);
            connDefs = new ArrayList<CommonConnDef>();
            connDefs.add(connImpl);
         }
         

         RaImpl raImpl = new RaImpl(rarFile, transSupport, connDefs, adminObjects, raConfigProperties);
         raImpl.buildResourceAdapterImpl();

         String raString = "<resource-adapters>" + raImpl.toString() + "</resource-adapters>";
         if (!raString.equals(""))
         {

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
         return value.toString();
   }

   /**
    * Tool usage
    */
   private static void usage()
   {
      System.out.println("Usage:  ./rar-info.sh [-classpath <lib>[:<lib>]*] <file>");
   }
}
