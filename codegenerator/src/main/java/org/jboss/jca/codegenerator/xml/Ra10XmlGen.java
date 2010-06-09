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
package org.jboss.jca.codegenerator.xml;

import org.jboss.jca.codegenerator.Definition;

import java.io.IOException;
import java.io.Writer;

/**
 * A Ra Xml Gen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class Ra10XmlGen extends RaXmlGen
{
   /**
    * Output xml
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeXmlBody(Definition def, Writer out) throws IOException
   {
      writeConnectorVersion(out);
      
      int indent = 1;
      writeIndent(out, indent);
      out.write("<display-name>Display Name</display-name>");
      writeEol(out);
      writeIndent(out, indent);
      out.write("<vendor-name>Red Hat Middleware LLC</vendor-name>");
      writeEol(out);
      writeIndent(out, indent);
      out.write("<spec-version>1.0</spec-version>");
      writeEol(out);
      writeIndent(out, indent);
      out.write("<eis-type>Test RA</eis-type>");
      writeEol(out);
      writeIndent(out, indent);
      out.write("<version>0.1</version>");
      writeEol(out);

      writeIndent(out, indent);
      out.write("<resourceadapter>");
      writeEol(out);

      writeOutbound(def, out, indent + 1);
      
      writeIndent(out, indent);
      out.write("</resourceadapter>");
      writeEol(out);
      out.write("</connector>");
      writeEol(out);
   }
   
   /**
    * write Connector Version
    * 
    * @param out output writer
    * @throws IOException io exception
    */
   @Override
   void writeConnectorVersion(Writer out) throws IOException
   {
      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      writeEol(out);
      writeEol(out);
      out.write("<!DOCTYPE connector PUBLIC");
      writeEol(out);
      out.write(" \"-//Sun Microsystems, Inc.//DTD Connector 1.0//EN\"");
      writeEol(out);
      out.write(" \"http://java.sun.com/dtd/connector_1_0.dtd\">");
      writeEol(out);
      writeEol(out);
      out.write("<connector>");
      writeEol(out);
   }
   
   /**
    * Output outbound xml part
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeOutbound(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("<managedconnectionfactory-class>" + def.getRaPackage() + "." + 
         def.getMcfClass() + "</managedconnectionfactory-class>");
      writeEol(out);
      writeEol(out);

      if (!def.isUseCciConnection())
      {
         writeIndent(out, indent);
         out.write("<connectionfactory-interface>" + def.getRaPackage() + "." + 
            def.getCfInterfaceClass() + "</connectionfactory-interface>");
         writeEol(out);
         writeIndent(out, indent);
         out.write("<connectionfactory-impl-class>" + def.getRaPackage() + "." + 
            def.getCfClass() + "</connectionfactory-impl-class>");
         writeEol(out);
         writeIndent(out, indent);
         out.write("<connection-interface>" + def.getRaPackage() + "." + 
            def.getConnInterfaceClass() + "</connection-interface>");
         writeEol(out);
         writeIndent(out, indent);
         out.write("<connection-impl-class>" + def.getRaPackage() + "." + 
            def.getConnImplClass() + "</connection-impl-class>");
         writeEol(out);
      }
      else
      {
         writeIndent(out, indent);
         out.write("<connectionfactory-interface>javax.resource.cci.ConnectionFactory</connectionfactory-interface>");
         writeEol(out);
         writeIndent(out, indent);
         out.write("<connectionfactory-impl-class>" + def.getRaPackage() + "." + 
            def.getCciConnFactoryClass() + "</connectionfactory-impl-class>");
         writeEol(out);
         writeIndent(out, indent);
         out.write("<connection-interface>javax.resource.cci.Connection</connection-interface>");
         writeEol(out);
         writeIndent(out, indent);
         out.write("<connection-impl-class>" + def.getRaPackage() + "." + 
            def.getCciConnClass() + "</connection-impl-class>");
         writeEol(out);
      }

      writeEol(out);
      writeIndent(out, indent);
      out.write("<transaction-support>NoTransaction</transaction-support>");
      writeEol(out);
      
      writeConfigPropsXml(def.getMcfConfigProps(), out, indent, false);
      
      writeIndent(out, indent);
      out.write("<reauthentication-support>false</reauthentication-support>");
      writeEol(out);
   }
}
