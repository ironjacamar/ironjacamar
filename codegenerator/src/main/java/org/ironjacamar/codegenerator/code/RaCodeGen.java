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
package org.ironjacamar.codegenerator.code;

import org.ironjacamar.codegenerator.ConfigPropType;
import org.ironjacamar.codegenerator.Definition;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * A resource adapter code generator
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class RaCodeGen extends PropsCodeGen
{
   /**
    * Output ResourceAdapater class
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeClassBody(Definition def, Writer out) throws IOException
   {
      if (def.isUseAnnotation())
      {
         out.write("@Connector");
         if (def.isSupportOutbound())
         {
            out.write("(\n");
            if (def.getAuthenMechanisms() != null && def.getAuthenMechanisms().size() > 0)
            {
               writeWithIndent(out, 1, "authMechanisms = {\n");
               for (int i = 0; i < def.getAuthenMechanisms().size(); i++)
               {
                  writeWithIndent(out, 2, "@AuthenticationMechanism(");
                  out.write("authMechanism = \"" + def.getAuthenMechanisms().get(i).getAuthMechanism());
                  out.write("\", credentialInterface = CredentialInterface." + def.getAuthenMechanisms().get(i)
                        .getCredentialInterface());
                  if (i + 1 < def.getAuthenMechanisms().size())
                     out.write("),");
                  else
                     out.write(")},");
                  writeEol(out);
               }
            }
            writeIndent(out, 1);
            out.write("reauthenticationSupport = " + def.isSupportReauthen() + ",\n");
            if (def.getSecurityPermissions() != null && def.getSecurityPermissions().size() > 0)
            {
               writeWithIndent(out, 1, "securityPermissions = {\n");

               for (int i = 0; i < def.getSecurityPermissions().size(); i++)
               {
                  writeWithIndent(out, 2, "@SecurityPermission(");
                  out.write("permissionSpec = \"" +
                        def.getSecurityPermissions().get(i).getPermissionSpec() + "\")");
                  if (i + 1 < def.getSecurityPermissions().size())
                     out.write(",");
                  else
                     out.write("},");
                  writeEol(out);
               }
            }
            writeIndent(out, 1);
            out.write("transactionSupport = TransactionSupport.TransactionSupportLevel." +
                  def.getSupportTransaction() + ")");
         }
         writeEol(out);
      }
      out.write("public class " + getClassName(def) + " implements ResourceAdapter");
      if (def.isRaSerial())
      {
         out.write(", java.io.Serializable");
      }
      writeLeftCurlyBracket(out, 0);
      writeEol(out);

      int indent = 1;

      if (def.isRaSerial())
      {
         writeWithIndent(out, indent, "/** The serial version UID */\n");
         writeWithIndent(out, indent, "private static final long serialVersionUID = 1L;\n\n");
      }

      writeWithIndent(out, indent, "/** The logger */\n");

      writeWithIndent(out, indent, "private static Logger log = Logger.getLogger(" + getSelfClassName(def) + ");\n\n");

      if (def.isSupportInbound())
      {
         writeWithIndent(out, indent, "/** The activations by activation spec */\n");
         writeWithIndent(out, indent, "private ");
         if (def.getVersion().equals("1.6") || def.getVersion().equals("1.7"))
         {
            out.write("ConcurrentHash");
         }
         out.write("Map<" + def.getAsClass() + ", " + def.getActivationClass() + "> activations;\n\n");
      }

      writeConfigPropsDeclare(def, out, indent);

      writeSimpleMethodSignature(out, indent, " * Default constructor", "public " + getClassName(def) + "()");
      writeLeftCurlyBracket(out, indent);
      if (def.isSupportInbound())
      {
         if (def.getVersion().equals("1.6") || def.getVersion().equals("1.7"))
         {
            writeWithIndent(out, indent + 1, "this.activations = new ConcurrentHashMap<" +
                  def.getAsClass() + ", " + def.getActivationClass() + ">();\n");
         }
         else
         {
            writeWithIndent(out, indent + 1, "this.activations = Collections.synchronizedMap(new HashMap<" +
                  def.getAsClass() + ", " + def.getActivationClass() + ">());\n");
         }
      }
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeConfigProps(def, out, indent);
      writeEndpointLifecycle(def, out, indent);
      writeLifecycle(def, out, indent);
      writeXAResource(def, out, indent);
      writeHashCode(def, out, indent);
      writeEquals(def, out, indent);

      writeRightCurlyBracket(out, 0);
   }

   /**
    * Output class import
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeImport(Definition def, Writer out) throws IOException
   {
      out.write("package " + def.getRaPackage() + ";\n\n");
      if (def.isSupportInbound())
      {
         out.write("import " + def.getRaPackage() + ".inflow." + def.getActivationClass() + ";\n");
         out.write("import " + def.getRaPackage() + ".inflow." + def.getAsClass() + ";\n\n");
         if (def.getVersion().equals("1.5"))
         {
            out.write("import java.util.Collections;\n");
            out.write("import java.util.HashMap;\n");
            out.write("import java.util.Map;\n");
            writeEol(out);
         }
         else if (def.getVersion().equals("1.6") || def.getVersion().equals("1.7"))
         {
            out.write("import java.util.concurrent.ConcurrentHashMap;\n");
            writeEol(out);
         }
      }
      importLogging(def, out);

      out.write("import javax.resource.ResourceException;\n");
      out.write("import javax.resource.spi.ActivationSpec;\n");
      if (def.isUseAnnotation() &&
            def.getAuthenMechanisms() != null && def.getAuthenMechanisms().size() > 0)
      {
         out.write("import javax.resource.spi.AuthenticationMechanism;\n");
         out.write("import javax.resource.spi.AuthenticationMechanism.CredentialInterface;\n");
      }
      out.write("import javax.resource.spi.BootstrapContext;\n");
      if (def.isUseAnnotation())
      {
         importConfigProperty(def, out);
         out.write("import javax.resource.spi.Connector;\n");
      }
      out.write("import javax.resource.spi.ResourceAdapter;\n");
      out.write("import javax.resource.spi.ResourceAdapterInternalException;\n");
      if (def.isUseAnnotation() &&
            def.getSecurityPermissions() != null && def.getSecurityPermissions().size() > 0)
      {
         out.write("import javax.resource.spi.SecurityPermission;\n");
      }
      if (def.isUseAnnotation() && def.isSupportOutbound())
      {
         out.write("import javax.resource.spi.TransactionSupport;\n");
      }
      out.write("import javax.resource.spi.endpoint.MessageEndpointFactory;\n\n");
      out.write("import javax.transaction.xa.XAResource;\n\n");

   }

   /**
    * Output getXAResources method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeXAResource(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * This method is called by the application server during crash recovery.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param specs An array of ActivationSpec JavaBeans \n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception \n");
      writeWithIndent(out, indent, " * @return An array of XAResource objects\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public XAResource[] getXAResources(ActivationSpec[] specs)\n");
      writeWithIndent(out, indent + 1, "throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getXAResources", "specs.toString()");
      writeWithIndent(out, indent + 1, "return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output Lifecycle method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeLifecycle(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * This is called when a resource adapter instance is bootstrapped.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param ctx A bootstrap context containing references \n");
      writeWithIndent(out, indent, " * @throws ResourceAdapterInternalException indicates bootstrap failure.\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void start(BootstrapContext ctx)\n");
      writeWithIndent(out, indent + 1, "throws ResourceAdapterInternalException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "start", "ctx");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * This is called when a resource adapter instance is undeployed or\n");
      writeWithIndent(out, indent, " * during application server shutdown. \n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void stop()");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "stop");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output EndpointLifecycle method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeEndpointLifecycle(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * This is called during the activation of a message endpoint.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param endpointFactory A message endpoint factory instance.\n");
      writeWithIndent(out, indent, " * @param spec An activation spec JavaBean instance.\n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception \n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void endpointActivation(MessageEndpointFactory endpointFactory,\n");
      writeWithIndent(out, indent + 1, "ActivationSpec spec) throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      if (def.isSupportInbound())
      {
         writeIndent(out, indent + 1);
         out.write(def.getActivationClass() + " activation = new " + def.getActivationClass() +
               "(this, endpointFactory, (" + def.getAsClass() + ")spec);\n");
         writeWithIndent(out, indent + 1, "activations.put((" + def.getAsClass() + ")spec, activation);\n");
         writeWithIndent(out, indent + 1, "activation.start();\n\n");
      }

      writeLogging(def, out, indent + 1, "trace", "endpointActivation", "endpointFactory", "spec");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * This is called when a message endpoint is deactivated. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param endpointFactory A message endpoint factory instance.\n");
      writeWithIndent(out, indent, " * @param spec An activation spec JavaBean instance.\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void endpointDeactivation(MessageEndpointFactory endpointFactory,\n");
      writeWithIndent(out, indent + 1, "ActivationSpec spec)");
      writeLeftCurlyBracket(out, indent);

      if (def.isSupportInbound())
      {
         writeIndent(out, indent + 1);
         out.write(def.getActivationClass() + " activation = activations.remove(spec);\n");
         writeWithIndent(out, indent + 1, "if (activation != null)\n");
         writeWithIndent(out, indent + 2, "activation.stop();\n\n");
      }
      writeLogging(def, out, indent + 1, "trace", "endpointDeactivation", "endpointFactory");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * get this class name
    *
    * @param def definition
    * @return String class name
    */
   @Override
   public String getClassName(Definition def)
   {
      return def.getRaClass();
   }

   /**
    * get list of ConfigPropType
    *
    * @param def definition
    * @return List<ConfigPropType> List of ConfigPropType
    */
   @Override
   public List<ConfigPropType> getConfigProps(Definition def)
   {
      return def.getRaConfigProps();
   }
}
