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
import org.ironjacamar.codegenerator.Version;

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
         if (!def.isDefaultPackageInbound())
         {
            out.write("@Activation(messageListeners = { " + def.getMlClass() + ".class })\n");
         }
         else
         {
            out.write("@Activation(messageListeners = { " + def.getRaPackage() +
                  ".inflow." + def.getMlClass() + ".class })\n");
         }
      }
      out.write("public class " + getClassName(def) + " implements ActivationSpec");
      writeLeftCurlyBracket(out, 0);
      writeEol(out);

      int indent = 1;
      writeWithIndent(out, indent, "/** The logger */\n");
      writeWithIndent(out, indent, "private static Logger log = Logger.getLogger(" + getSelfClassName(def) + ");\n\n");

      writeWithIndent(out, indent, "/** The resource adapter */\n");
      writeWithIndent(out, indent, "private ResourceAdapter ra;\n\n");

      writeConfigPropsDeclare(def, out, indent);

      writeDefaultConstructor(def, out, indent);

      writeConfigProps(def, out, indent);

      writeValidate(def, out, indent);
      writeResourceAdapter(def, out, indent);

      writeRightCurlyBracket(out, 0);
   }

   @Override
   void writeDefaultConstructor(Definition def, Writer out, int indent) throws IOException
   {
      if (!def.getVersion().equals(Version.V_15.getLocalName()))
      {
         super.writeDefaultConstructor(def, out, indent);
         return;
      }
      // only for JCA 1.5
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Default constructor\n");
      writeWithIndent(out, indent, " */\n");

      //constructor
      writeWithIndent(out, indent, "public " + getClassName(def) + "()");
      writeLeftCurlyBracket(out, indent);
      writeConfigPropsDefaults(def, out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);

   }

   private void writeConfigPropsDefaults(Definition def, Writer out, int indent) throws IOException
   {
      List<ConfigPropType> configPropTypes = def.getAsConfigProps();
      if (configPropTypes == null)
      {
         return;
      }
      for (ConfigPropType configPropType : configPropTypes)
      {
         String name = configPropType.getName();
         String type = configPropType.getType();
         String value = configPropType.getValue();
         if (value != null && value.length() > 0)
         {
            writeIndent(out, indent);
            writeIndent(out, indent);
            switch (type)
            {
               case "Character":
                  out.write("this." + name + " = " + type + ".valueOf(\'" + value + "\');\n");
                  break;
               case "String":
                  out.write("this." + name + " = \"" + value + "\";\n");
                  break;
               default:
                  out.write("this." + name + " = " + type + ".valueOf(\"" + value + "\");\n");
                  break;
            }
         }
      }
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
      out.write("package " + def.getRaPackage() + ".inflow;\n\n");
      importLogging(def, out);

      if (def.isUseAnnotation())
      {
         out.write("import javax.resource.spi.Activation;");
         writeEol(out);
      }
      out.write("import javax.resource.spi.ActivationSpec;\n");
      if (def.isUseAnnotation())
      {
         importConfigProperty(def, out);
      }
      out.write("import javax.resource.spi.InvalidPropertyException;\n");
      out.write("import javax.resource.spi.ResourceAdapter;\n");

      if (def.isUseAnnotation())
      {
         for (int i = 0; i < getConfigProps(def).size(); i++)
         {
            if (getConfigProps(def).get(i).isRequired())
            {
               out.write("import javax.validation.constraints.NotNull;\n");
               break;
            }
         }
      }
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
      return def.getAsClass();
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
      return def.getAsConfigProps();
   }

   /**
    * Output validate method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeValidate(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * This method may be called by a deployment tool to validate the overall\n");
      writeWithIndent(out, indent, " * activation configuration information provided by the endpoint deployer.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent,
            " * @throws InvalidPropertyException indicates invalid configuration property settings.\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void validate() throws InvalidPropertyException");
      writeLeftCurlyBracket(out, indent);

      writeLogging(def, out, indent + 1, "trace", "validate");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output ResourceAdapter method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeResourceAdapter(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Get the resource adapter\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return The handle\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public ResourceAdapter getResourceAdapter()");
      writeLeftCurlyBracket(out, indent);

      writeLogging(def, out, indent + 1, "trace", "getResourceAdapter");

      writeIndent(out, indent + 1);
      out.write("return ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Set the resource adapter\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param ra The handle\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void setResourceAdapter(ResourceAdapter ra)");
      writeLeftCurlyBracket(out, indent);

      writeLogging(def, out, indent + 1, "trace", "setResourceAdapter", "ra");

      writeIndent(out, indent + 1);
      out.write("this.ra = ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
