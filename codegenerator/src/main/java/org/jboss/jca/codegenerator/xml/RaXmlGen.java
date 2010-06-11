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

import org.jboss.jca.codegenerator.ConfigPropType;
import org.jboss.jca.codegenerator.Definition;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * A BuildXmlGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public abstract class RaXmlGen extends AbstractXmlGen
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
      out.write("<vendor-name>Red Hat Middleware LLC</vendor-name>");
      writeEol(out);
      writeIndent(out, indent);
      out.write("<eis-type>Test RA</eis-type>");
      writeEol(out);
      writeIndent(out, indent);
      out.write("<resourceadapter-version>0.1</resourceadapter-version>");
      writeEol(out);
      writeIndent(out, indent);
      out.write("<resourceadapter>");
      writeEol(out);
      
      if (def.isUseRa())
      {
         writeIndent(out, indent + 1);
         out.write("<resourceadapter-class>" + def.getRaPackage() + "." + 
            def.getRaClass() + "</resourceadapter-class>");
         writeEol(out);
         writeConfigPropsXml(def.getRaConfigProps(), out, indent + 1, false);
      }

      if (def.isSupportOutbound())
      {
         writeOutbound(def, out, indent + 1);
      }
      if (def.isSupportInbound())
      {
         writeInbound(def, out, indent + 1);
      }
      
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
   abstract void writeConnectorVersion(Writer out) throws IOException;
   
   /**
    * Output config props xml part
    * @param props config properties
    * @param out Writer
    * @param indent space number
    * @param required support required
    * @throws IOException ioException
    */
   void writeConfigPropsXml(List<ConfigPropType> props, 
      Writer out, int indent, boolean required) throws IOException
   {
      if (props == null || props.size() == 0)
         return;
      if (required)
      {
         for (ConfigPropType prop : props)
         {
            if (prop.isRequired())
            {
               writeIndent(out, indent);
               out.write("<required-config-property>");
               writeEol(out);
               
               writeIndent(out, indent + 1);
               out.write("<config-property-name>" + prop.getName() + "</config-property-name>");
               writeEol(out);

               writeIndent(out, indent);
               out.write("</required-config-property>");
               writeEol(out);
            }
         }
         writeEol(out);
      }
      else
      {
         for (ConfigPropType prop : props)
         {
            writeIndent(out, indent);
            out.write("<config-property>");
            writeEol(out);
         
            writeIndent(out, indent + 1);
            out.write("<config-property-name>" + prop.getName() + "</config-property-name>");
            writeEol(out);
            writeIndent(out, indent + 1);
            out.write("<config-property-type>java.lang." + prop.getType() + "</config-property-type>");
            writeEol(out);
            writeIndent(out, indent + 1);
            out.write("<config-property-value>" + prop.getValue() + "</config-property-value>");
            writeEol(out);
         
            writeIndent(out, indent);
            out.write("</config-property>");
            writeEol(out);
            writeEol(out);
         }
      }
   }
   
   /**
    * Output inbound xml part
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeInbound(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("<inbound-resourceadapter>");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("<messageadapter>");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("<messagelistener>");
      writeEol(out);
      writeIndent(out, indent + 3);
      out.write("<messagelistener-type>" + def.getRaPackage() + 
         ".inflow." + def.getMlClass() + "</messagelistener-type>");
      writeEol(out);
      writeIndent(out, indent + 3);
      out.write("<activationspec>");
      writeEol(out);
      writeIndent(out, indent + 4);
      out.write("<activationspec-class>" + def.getRaPackage() + 
         ".inflow." + def.getAsClass() + "</activationspec-class>");
      writeEol(out);
      
      writeConfigPropsXml(def.getAsConfigProps(), out, indent + 4, true);
      writeIndent(out, indent + 3);
      out.write("</activationspec>");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("</messagelistener>");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("</messageadapter>");
      writeEol(out);
      writeIndent(out, indent);
      out.write("</inbound-resourceadapter>");
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
      out.write("<outbound-resourceadapter>");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("<connection-definition>");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("<managedconnectionfactory-class>" + def.getRaPackage() + "." + 
         def.getMcfClass() + "</managedconnectionfactory-class>");
      writeEol(out);
      writeConfigPropsXml(def.getMcfConfigProps(), out, indent + 2, false);
      
      if (!def.isUseCciConnection())
      {
         writeIndent(out, indent + 2);
         out.write("<connectionfactory-interface>" + def.getRaPackage() + "." + 
            def.getCfInterfaceClass() + "</connectionfactory-interface>");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("<connectionfactory-impl-class>" + def.getRaPackage() + "." + 
            def.getCfClass() + "</connectionfactory-impl-class>");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("<connection-interface>" + def.getRaPackage() + "." + 
            def.getConnInterfaceClass() + "</connection-interface>");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("<connection-impl-class>" + def.getRaPackage() + "." + 
            def.getConnImplClass() + "</connection-impl-class>");
         writeEol(out);
      }
      else
      {
         writeIndent(out, indent + 2);
         out.write("<connectionfactory-interface>javax.resource.cci.ConnectionFactory</connectionfactory-interface>");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("<connectionfactory-impl-class>" + def.getRaPackage() + "." + 
            def.getCciConnFactoryClass() + "</connectionfactory-impl-class>");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("<connection-interface>javax.resource.cci.Connection</connection-interface>");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("<connection-impl-class>" + def.getRaPackage() + "." + 
            def.getCciConnClass() + "</connection-impl-class>");
         writeEol(out);
      }
      writeIndent(out, indent + 1);
      out.write("</connection-definition>");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("<transaction-support>" + def.getSupportTransaction() + "</transaction-support>");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("<reauthentication-support>false</reauthentication-support>");
      writeEol(out);
      writeIndent(out, indent);
      out.write("</outbound-resourceadapter>");
      writeEol(out);
   }
}
