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

import org.ironjacamar.codegenerator.Definition;

import java.io.IOException;
import java.io.Writer;

/**
 * A connection factory interface CodeGen.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class CfInterfaceCodeGen extends AbstractCodeGen
{
   /**
    * Output class code
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeClassBody(Definition def, Writer out) throws IOException
   {

      out.write("public interface " + getClassName(def) + " extends Serializable, Referenceable");
      writeLeftCurlyBracket(out, 0);
      int indent = 1;

      writeConnection(def, out, indent);
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
      out.write("import java.io.Serializable;\n\n");
      out.write("import javax.resource.Referenceable;\n");
      out.write("import javax.resource.ResourceException;\n\n");
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
      return def.getMcfDefs().get(getNumOfMcf()).getCfInterfaceClass();
   }

   /**
    * Output Connection method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeConnection(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/** \n");
      writeWithIndent(out, indent, " * Get connection from factory\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent,
            " * @return " + def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass() + " instance\n");
      writeWithIndent(out, indent, " * @exception ResourceException Thrown if a connection can't be obtained\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public " + def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass() +
            " getConnection() throws ResourceException;\n");
   }
}
