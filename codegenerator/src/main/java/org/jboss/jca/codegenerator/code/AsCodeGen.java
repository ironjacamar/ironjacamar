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
 * A ActivationSpec CodeGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class AsCodeGen extends PropsCodeGen
{

   /**
    * Output class
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeClassBody(Definition def, Writer out) throws IOException
   {
      if (def.isUseAnnotation())
      {
         out.write("@Activation(messageListeners = { " + def.getRaPackage() + 
            ".inflow." + def.getMlClass() + ".class })");
         writeEol(out);
      }
      out.write("public class " + getClassName(def) + " implements ActivationSpec");
      writeLeftCurlyBracket(out, 0);
      writeEol(out);

      int indent = 1;
      writeIndent(out, indent);
      out.write("/** The logger */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private static Logger log = Logger.getLogger(\"" + getClassName(def) + "\");");
      writeEol(out);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/** The resource adapter */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private ResourceAdapter ra;");
      writeEol(out);
      writeEol(out);

      writeConfigPropsDeclare(def, out, indent);
      
      writeDefaultConstructor(def, out, indent);
      
      writeConfigProps(def, out, indent);
      
      writeValidate(def, out, indent);
      writeResourceAdapter(def, out, indent);
      
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
      out.write("package " + def.getRaPackage() + ".inflow;");
      writeEol(out);
      writeEol(out);
      out.write("import java.util.logging.Logger;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.resource.ResourceException;");
      writeEol(out);
      out.write("import javax.resource.spi.Activation;");
      writeEol(out);
      out.write("import javax.resource.spi.ActivationSpec;");
      writeEol(out);
      if (def.isUseAnnotation())
      {
         out.write("import javax.resource.spi.ConfigProperty;");
         writeEol(out);
      }
      out.write("import javax.resource.spi.InvalidPropertyException;");
      writeEol(out);
      out.write("import javax.resource.spi.ResourceAdapter;");
      writeEol(out);
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
      return def.getAsClass();
   }
   
   /**
    * get list of ConfigPropType
    * @param def definition
    * @return List<ConfigPropType> List of ConfigPropType
    */
   @Override
   public List<ConfigPropType> getConfigProps(Definition def)
   {
      return def.getAsConfigProps();
   }
   
   /**
    * Output validate method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeValidate(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * This method may be called by a deployment tool to validate the overall");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * activation configuration information provided by the endpoint deployer.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws InvalidPropertyException indicates invalid onfiguration property settings.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void validate() throws InvalidPropertyException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.finest(\"validate()\");");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output ResourceAdapter method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeResourceAdapter(Definition def, Writer out, int indent) throws IOException
   {      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Get the resource adapter");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return The handle");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public ResourceAdapter getResourceAdapter()");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.finest(\"getResourceAdapter()\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Set the resource adapter");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param ra The handle");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void setResourceAdapter(ResourceAdapter ra)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.finest(\"setResourceAdapter()\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("this.ra = ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
