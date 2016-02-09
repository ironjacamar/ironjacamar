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
 * A properties code generator
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public abstract class PropsCodeGen extends AbstractCodeGen
{
   /**
    * Output Configuration Properties Declare
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeConfigPropsDeclare(Definition def, Writer out, int indent) throws IOException
   {
      if (getConfigProps(def) == null)
         return;

      for (int i = 0; i < getConfigProps(def).size(); i++)
      {
         writeWithIndent(out, indent, "/** " + getConfigProps(def).get(i).getName() + " */\n");

         if (def.isUseAnnotation())
         {
            writeIndent(out, indent);
            out.write("@ConfigProperty(defaultValue = \"" + getConfigProps(def).get(i).getValue() + "\")");
            if (getConfigProps(def).get(i).isRequired())
            {
               out.write(" @NotNull");
            }
            writeEol(out);
         }
         writeWithIndent(out, indent, "private " +
               getConfigProps(def).get(i).getType() +
               " " +
               getConfigProps(def).get(i).getName() +
               ";\n\n");
      }
   }

   /**
    * Output Configuration Properties
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeConfigProps(Definition def, Writer out, int indent) throws IOException
   {
      if (getConfigProps(def) == null)
         return;

      for (int i = 0; i < getConfigProps(def).size(); i++)
      {
         String name = getConfigProps(def).get(i).getName();
         String upcaseName = upcaseFirst(name);
         //set
         writeWithIndent(out, indent, "/** \n");
         writeWithIndent(out, indent, " * Set " + name);
         writeEol(out);
         writeWithIndent(out, indent, " * @param " + name + " The value\n");
         writeWithIndent(out, indent, " */\n");

         writeWithIndent(out, indent, "public void set" +
               upcaseName +
               "(" +
               getConfigProps(def).get(i).getType() +
               " " +
               name +
               ")");
         writeLeftCurlyBracket(out, indent);
         writeWithIndent(out, indent + 1, "this." + name + " = " + name + ";");
         writeRightCurlyBracket(out, indent);
         writeEol(out);

         //get
         writeWithIndent(out, indent, "/** \n");
         writeWithIndent(out, indent, " * Get " + name);
         writeEol(out);
         writeWithIndent(out, indent, " * @return The value\n");
         writeWithIndent(out, indent, " */\n");
         writeWithIndent(out, indent, "public " +
               getConfigProps(def).get(i).getType() +
               " get" +
               upcaseName +
               "()");
         writeLeftCurlyBracket(out, indent);
         writeWithIndent(out, indent + 1, "return " + name + ";");
         writeRightCurlyBracket(out, indent);
         writeEol(out);
      }
   }

   /**
    * Output hashCode method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   @Override
   void writeHashCode(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/** \n");
      writeIndent(out, indent);
      out.write(" * Returns a hash code value for the object.\n");
      writeIndent(out, indent);
      out.write(" * @return A hash code value for this object.\n");
      writeIndent(out, indent);
      out.write(" */\n");

      writeIndent(out, indent);
      out.write("@Override\n");
      writeIndent(out, indent);
      out.write("public int hashCode()");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "int result = 17;\n");
      for (int i = 0; i < getConfigProps(def).size(); i++)
      {
         writeWithIndent(out, indent + 1, "if (" + getConfigProps(def).get(i).getName() + " != null)\n");
         writeWithIndent(out, indent + 2,
               "result += 31 * result + 7 * " + getConfigProps(def).get(i).getName() + ".hashCode();\n");
         writeIndent(out, indent + 1);

         out.write("else\n");
         writeWithIndent(out, indent + 2, "result += 31 * result + 7;\n");
      }
      writeWithIndent(out, indent + 1, "return result;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output equals method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   @Override
   void writeEquals(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/** \n");
      writeIndent(out, indent);
      out.write(" * Indicates whether some other object is equal to this one.\n");
      writeIndent(out, indent);
      out.write(" * @param other The reference object with which to compare.\n");
      writeIndent(out, indent);
      out.write(" * @return true if this object is the same as the obj argument, false otherwise.\n");
      writeIndent(out, indent);
      out.write(" */\n");

      writeIndent(out, indent);
      out.write("@Override\n");
      writeIndent(out, indent);
      out.write("public boolean equals(Object other)");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "if (other == null)\n");
      writeWithIndent(out, indent + 2, "return false;\n");
      writeWithIndent(out, indent + 1, "if (other == this)\n");
      writeWithIndent(out, indent + 2, "return true;\n");
      writeWithIndent(out, indent + 1, "if (!(other instanceof " + getClassName(def) + "))\n");
      writeWithIndent(out, indent + 2, "return false;\n");

      writeWithIndent(out, indent + 1, "boolean result = true;\n");

      if (getConfigProps(def).size() > 0)
      {
         writeIndent(out, indent + 1);
         out.write(getClassName(def) + " obj = (" + getClassName(def) + ")other;\n");
      }

      for (int i = 0; i < getConfigProps(def).size(); i++)
      {
         writeWithIndent(out, indent + 1, "if (result)");
         writeLeftCurlyBracket(out, indent + 1);
         writeWithIndent(out, indent + 2, "if (" + getConfigProps(def).get(i).getName() + " == null)\n");
         writeWithIndent(out, indent + 3,
               "result = obj.get" + upcaseFirst(getConfigProps(def).get(i).getName()) + "() == null;\n");

         writeWithIndent(out, indent + 2, "else\n");
         writeWithIndent(out, indent + 3, "result = " + getConfigProps(def).get(i).getName() + ".equals(obj.get" +
               upcaseFirst(getConfigProps(def).get(i).getName()) + "());");
         writeRightCurlyBracket(out, indent + 1);
      }
      writeWithIndent(out, indent + 1, "return result;");
      writeRightCurlyBracket(out, indent);
      //writeEol(out);
   }

   /**
    * get list of ConfigPropType
    *
    * @param def definition
    * @return List<ConfigPropType> List of ConfigPropType
    */
   public abstract List<ConfigPropType> getConfigProps(Definition def);

   /**
    * import ConfigProperty
    *
    * @param def definition
    * @param out Writer
    * @throws IOException IOException
    */
   protected void importConfigProperty(Definition def, Writer out) throws IOException
   {
      if (getConfigProps(def).size() > 0)
      {
         out.write("import javax.resource.spi.ConfigProperty;\n");
      }
   }
}
