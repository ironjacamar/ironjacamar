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
package org.jboss.jca.codegenerator.code;

import org.jboss.jca.codegenerator.ConfigPropType;
import org.jboss.jca.codegenerator.Definition;

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
         writeEol(out);
      }
      out.write("public class " + getClassName(def) + " implements ResourceAdapter");
      writeLeftCurlyBracket(out, 0);
      writeEol(out);
      
      int indent = 1;
      writeIndent(out, indent);
      out.write("/** The logger */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("private static Logger log = Logger.getLogger(" + getClassName(def) + ".class);");
      writeEol(out);
      writeEol(out);
      
      if (def.isSupportInbound())
      {
         writeIndent(out, indent);
         out.write("/** The activations by activation spec */");
         writeEol(out);
         writeIndent(out, indent);
         out.write("private HashMap activations = new HashMap();");
         writeEol(out);
         writeEol(out);
      }
      
      writeDefaultConstructor(def, out, indent);
      
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
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeImport(Definition def, Writer out) throws IOException
   {
      out.write("package " + def.getRaPackage() + ";");
      writeEol(out);
      writeEol(out);
      if (def.isSupportInbound())
      {
         out.write("import " + def.getRaPackage() + ".inflow." + def.getActivationClass() + ";");
         writeEol(out);
         out.write("import " + def.getRaPackage() + ".inflow." + def.getAsClass() + ";");
         writeEol(out);
         writeEol(out);
         out.write("import java.util.HashMap;");
         writeEol(out);
         writeEol(out);
      }
      out.write("import javax.resource.ResourceException;");
      writeEol(out);
      out.write("import javax.resource.spi.ActivationSpec;");
      writeEol(out);
      out.write("import javax.resource.spi.BootstrapContext;");
      writeEol(out);
      if (def.isUseAnnotation())
      {
         out.write("import javax.resource.spi.ConfigProperty;");
         writeEol(out);
         out.write("import javax.resource.spi.Connector;");
         writeEol(out);
      }
      out.write("import javax.resource.spi.ResourceAdapter;");
      writeEol(out);
      out.write("import javax.resource.spi.ResourceAdapterInternalException;");
      writeEol(out);
      out.write("import javax.resource.spi.endpoint.MessageEndpointFactory;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.transaction.xa.XAResource;");
      writeEol(out);
      writeEol(out);
      out.write("import org.jboss.logging.Logger;");
      writeEol(out);
      writeEol(out);
   }

   /**
    * Output getXAResources method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeXAResource(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * This method is called by the application server during crash recovery.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param specs an array of ActivationSpec JavaBeans ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException generic exception ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return an array of XAResource objects");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public XAResource[] getXAResources(ActivationSpec[] specs)");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call getXAResources\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output Lifecycle method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeLifecycle(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * This is called when a resource adapter instance is bootstrapped.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param ctx a bootstrap context containing references ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceAdapterInternalException indicates bootstrap failure.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void start(BootstrapContext ctx)");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("throws ResourceAdapterInternalException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call start\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * This is called when a resource adapter instance is undeployed or");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * during application server shutdown. ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void stop()");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call stop\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output EndpointLifecycle method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeEndpointLifecycle(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * This is called during the activation of a message endpoint.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param endpointFactory a message endpoint factory instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param spec an activation spec JavaBean instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException generic exception ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void endpointActivation(MessageEndpointFactory endpointFactory,");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("ActivationSpec spec) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      
      if (def.isSupportInbound())
      {
         writeIndent(out, indent + 1);
         out.write(def.getActivationClass() + " activation = new " + def.getActivationClass() + 
            "(this, endpointFactory, (" + def.getAsClass() + ")spec);");
         writeEol(out);
         writeIndent(out, indent + 1);
         out.write("activations.put(spec, activation);");
         writeEol(out);
         writeEol(out);
      }
      
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call endpointActivation\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * This is called when a message endpoint is deactivated. ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param endpointFactory a message endpoint factory instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param spec an activation spec JavaBean instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void endpointDeactivation(MessageEndpointFactory endpointFactory,");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("ActivationSpec spec)");
      writeLeftCurlyBracket(out, indent);
      
      if (def.isSupportInbound())
      {
         writeIndent(out, indent + 1);
         out.write("activations.remove(spec);");
         writeEol(out);
         writeEol(out);
      }
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call endpointDeactivation\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * get this class name
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
    * @param def definition
    * @return List<ConfigPropType> List of ConfigPropType
    */
   @Override
   public List<ConfigPropType> getConfigProps(Definition def)
   {
      return def.getRaConfigProps();
   }
}
