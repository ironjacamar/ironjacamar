/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.codegenerator.xml;

import org.ironjacamar.codegenerator.ConfigPropType;
import org.ironjacamar.codegenerator.Definition;

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
    *
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
      out.write("<vendor-name>Red Hat Inc</vendor-name>");
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
         writeConfigPropsXml(def.getRaConfigProps(), out, indent + 1);
      }

      if (def.isSupportOutbound())
      {
         writeOutbound(def, out, indent + 1);
      }
      if (def.isSupportInbound())
      {
         writeInbound(def, out, indent + 1);
      }

      if (def.isGenAdminObject() && def.getAdminObjects().size() > 0)
      {
         for (int i = 0; i < def.getAdminObjects().size(); i++)
         {
            writeIndent(out, indent + 1);
            out.write("<adminobject>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<adminobject-interface>" + def.getRaPackage() + "." +
                  def.getAdminObjects().get(i).getAdminObjectInterface() + "</adminobject-interface>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<adminobject-class>" + def.getRaPackage() + "." +
                  def.getAdminObjects().get(i).getAdminObjectClass() + "</adminobject-class>");
            writeEol(out);
            writeConfigPropsXml(def.getAdminObjects().get(i).getAoConfigProps(), out, indent + 2);
            writeIndent(out, indent + 1);
            out.write("</adminobject>");
            writeEol(out);
         }
      }

      if (def.getSecurityPermissions() != null && def.getSecurityPermissions().size() > 0)
      {
         for (int i = 0; i < def.getSecurityPermissions().size(); i++)
         {
            writeIndent(out, indent + 1);
            out.write("<security-permission>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<security-permission-spec>" + def.getSecurityPermissions().get(i).getPermissionSpec() +
                  "</security-permission-spec>");
            writeEol(out);
            writeIndent(out, indent + 1);
            out.write("</security-permission>");
            writeEol(out);
         }
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
   void writeConnectorVersion(Writer out) throws IOException
   {
      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      writeEol(out);
      writeEol(out);
      out.write("<!--");
      writeEol(out);
      writeHeader(null, out);
      out.write("-->");
      writeEol(out);
      writeEol(out);
      out.write("<connector xmlns=\"http://java.sun.com/xml/ns/j2ee\"");
      writeEol(out);
      out.write("           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
      writeEol(out);
      out.write("           xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee");
      writeEol(out);
      out.write("           http://java.sun.com/xml/ns/j2ee/connector_1_5.xsd\"");
      writeEol(out);
      out.write("           version=\"1.5\">");
      writeEol(out);
      writeEol(out);
   }

   /**
    * Output config props xml part
    *
    * @param props  config properties
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeConfigPropsXml(List<ConfigPropType> props, Writer out, int indent) throws IOException
   {
      if (props == null || props.size() == 0)
         return;
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

   /**
    * Output As config props xml part
    *
    * @param props  config properties
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   abstract void writeAsConfigPropsXml(List<ConfigPropType> props, Writer out, int indent) throws IOException;

   /**
    * Output required config props xml part
    *
    * @param props  config properties
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeRequireConfigPropsXml(List<ConfigPropType> props, Writer out, int indent) throws IOException
   {
      if (props == null || props.size() == 0)
         return;
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

   /**
    * Output inbound xml part
    *
    * @param def    definition
    * @param out    Writer
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
      if (!def.isDefaultPackageInbound())
      {
         out.write("<messagelistener-type>" + def.getMlClass() + "</messagelistener-type>");
      }
      else
      {
         out.write("<messagelistener-type>" + def.getRaPackage() +
               ".inflow." + def.getMlClass() + "</messagelistener-type>");
      }
      writeEol(out);
      writeIndent(out, indent + 3);
      out.write("<activationspec>");
      writeEol(out);
      writeIndent(out, indent + 4);
      out.write("<activationspec-class>" + def.getRaPackage() +
            ".inflow." + def.getAsClass() + "</activationspec-class>");
      writeEol(out);

      writeAsConfigPropsXml(def.getAsConfigProps(), out, indent + 4);
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
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeOutbound(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("<outbound-resourceadapter>");
      writeEol(out);

      for (int num = 0; num < def.getMcfDefs().size(); num++)
      {

         writeIndent(out, indent + 1);
         out.write("<connection-definition>");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("<managedconnectionfactory-class>" + def.getRaPackage() + "." +
               def.getMcfDefs().get(num).getMcfClass() + "</managedconnectionfactory-class>");
         writeEol(out);
         writeConfigPropsXml(def.getMcfDefs().get(num).getMcfConfigProps(), out, indent + 2);

         if (!def.getMcfDefs().get(num).isUseCciConnection())
         {
            writeIndent(out, indent + 2);
            out.write("<connectionfactory-interface>" + def.getRaPackage() + "." +
                  def.getMcfDefs().get(num).getCfInterfaceClass() + "</connectionfactory-interface>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<connectionfactory-impl-class>" + def.getRaPackage() + "." +
                  def.getMcfDefs().get(num).getCfClass() + "</connectionfactory-impl-class>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<connection-interface>" + def.getRaPackage() + "." +
                  def.getMcfDefs().get(num).getConnInterfaceClass() + "</connection-interface>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<connection-impl-class>" + def.getRaPackage() + "." +
                  def.getMcfDefs().get(num).getConnImplClass() + "</connection-impl-class>");
            writeEol(out);
         }
         else
         {
            writeIndent(out, indent + 2);
            out.write("<connectionfactory-interface>javax.resource.cci.ConnectionFactory"
                  + "</connectionfactory-interface>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<connectionfactory-impl-class>" + def.getRaPackage() + "." +
                  def.getMcfDefs().get(num).getCciConnFactoryClass() + "</connectionfactory-impl-class>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<connection-interface>javax.resource.cci.Connection</connection-interface>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<connection-impl-class>" + def.getRaPackage() + "." +
                  def.getMcfDefs().get(num).getCciConnClass() + "</connection-impl-class>");
            writeEol(out);
         }
         writeIndent(out, indent + 1);
         out.write("</connection-definition>");
         writeEol(out);
      }
      writeIndent(out, indent + 1);
      out.write("<transaction-support>" + def.getSupportTransaction() + "</transaction-support>");
      writeEol(out);

      if (def.getAuthenMechanisms() != null && def.getAuthenMechanisms().size() > 0)
      {
         for (int i = 0; i < def.getAuthenMechanisms().size(); i++)
         {
            writeIndent(out, indent + 1);
            out.write("<authentication-mechanism>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<authentication-mechanism-type>" + def.getAuthenMechanisms().get(i).getAuthMechanism() +
                  "</authentication-mechanism-type>");
            writeEol(out);
            writeIndent(out, indent + 2);
            out.write("<credential-interface>");
            if (def.getAuthenMechanisms().get(i).getCredentialInterface().equals("GSSCredential"))
               out.write("org.ietf.jgss.GSSCredential");
            else
               out.write("javax.resource.spi.security." + def.getAuthenMechanisms().get(i).getCredentialInterface());
            out.write("</credential-interface>");
            writeEol(out);
            writeIndent(out, indent + 1);
            out.write("</authentication-mechanism>");
            writeEol(out);
         }
      }

      writeIndent(out, indent + 1);
      out.write("<reauthentication-support>" + def.isSupportReauthen() + "</reauthentication-support>");
      writeEol(out);
      writeIndent(out, indent);
      out.write("</outbound-resourceadapter>");
      writeEol(out);
   }
}
