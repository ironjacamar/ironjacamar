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

import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.metadata.ra.RaParser;
import org.jboss.jca.validator.Validation;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
   
   private static final String REPORT_FILE = "file-report.txt";
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
         if (args.length < 1 && args[0].endsWith("rar"))
         {
            usage();
            System.exit(OTHER);
         }

         String rarFile = args[0];
         ZipFile zipFile = new ZipFile(rarFile);

         boolean hasRaXml = false;
         boolean exsitNativeFile = false;
         Connector connector = null;
         Node raNode = null;

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
               
               DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
               DocumentBuilder db = dbf.newDocumentBuilder();
               Document doc = db.parse(new InputSource(zipFile.getInputStream(ze)));
               NodeList raList = doc.getElementsByTagName("resourceadapter");
               if (raList != null && raList.getLength() > 0)
                  raNode = raList.item(0);
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

         out = new PrintStream(REPORT_FILE);
         out.println("Archive:\t" + rarFile);
         
         String version;
         String type = "";
         ResourceAdapter ra;
         if (connector.getVersion() == Version.V_10)
         {
            version = "1.0";
            ra = connector.getResourceadapter();
            type = "OutBound";
         }
         else
         {
            if (connector.getVersion() == Version.V_15)
               version = "1.5";
            else
               version = "1.6";
            ResourceAdapter1516 ra1516 = (ResourceAdapter1516)connector.getResourceadapter();
            ra = ra1516;
            if (ra1516.getInboundResourceadapter() != null)
            {
               if (ra1516.getOutboundResourceadapter() != null)
                  type = "Bidirect";
               else
                  type = "InBound";
            }
            else
            {
               if (ra1516.getOutboundResourceadapter() != null)
                  type = "OutBound";
               else
               {
                  out.println("Rar file has problem");
                  System.exit(ERROR);
               }
            }
         }
         out.println("JCA version:\t" + version);
         out.println("Type:\t\t" + type);
         
         int systemExitCode = Validation.validate(new File(rarFile).toURI().toURL(), ".");
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
         
         if (connector.getVersion() != Version.V_10)
         {
            ResourceAdapter1516 ra1516 = (ResourceAdapter1516)ra;
            out.println();
            out.println("Resource-adapter:");
            out.println("  Class: " + ra1516.getResourceadapterClass());

            out.println();
            out.println("Managed-connection-factory:");
            if (ra1516.getOutboundResourceadapter() != null)
            {
               for (ConnectionDefinition mcf : ra1516.getOutboundResourceadapter().getConnectionDefinitions())
               {
                  out.println("  Class: " + mcf.getManagedConnectionFactoryClass());
               }
            }
            
            out.println();
            out.println("Admin-object:");
            for (AdminObject ao : ra1516.getAdminObjects())
            {
               out.println("  Class: " + ao.getAdminobjectClass());
            }
            
            out.println();
            out.println("Activation-spec:");
            if (ra1516.getInboundResourceadapter() != null && 
               ra1516.getInboundResourceadapter().getMessageadapter() != null)
            {
               for (MessageListener ml : ra1516.getInboundResourceadapter().getMessageadapter().getMessagelisteners())
               {
                  out.println("  Class: " + ml.getActivationspec().getActivationspecClass());
               }
            }
         }
         else
         {
            out.println("Managed-connection-factory:");

            ResourceAdapter10 ra10 = (ResourceAdapter10)ra;
            out.println("  Class: " + ra10.getManagedConnectionFactoryClass());
         }
         
         if (raNode != null)
         {
            out.println();

            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer serializer;

            serializer = tfactory.newTransformer();
            //Setup indenting to "pretty print"
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            serializer.transform(new DOMSource(raNode), new StreamResult(out));
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
    * Tool usage
    */
   private static void usage()
   {
      System.out.println("Usage: ./rar-info.sh file.rar");
   }
}
