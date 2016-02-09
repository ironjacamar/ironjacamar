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
 * A connection MetaData class CodeGen.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class ConnMetaCodeGen extends AbstractCodeGen
{
   /**
    * Output Metadata class
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeClassBody(Definition def, Writer out) throws IOException
   {

      out.write("public class " + getClassName(def) + " implements ConnectionMetaData");
      writeLeftCurlyBracket(out, 0);
      int indent = 1;

      writeDefaultConstructor(def, out, indent);

      writeEIS(def, out, indent);
      writeUsername(def, out, indent);

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
      out.write("import javax.resource.ResourceException;\n\n");
      out.write("import javax.resource.cci.ConnectionMetaData;\n\n");
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
      return def.getMcfDefs().get(getNumOfMcf()).getConnMetaClass();
   }

   /**
    * Output eis info method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeEIS(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Returns product name of the underlying EIS instance connected\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return Product name of the EIS instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException  Failed to get the information\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public String getEISProductName() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "return null; //TODO");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Returns product version of the underlying EIS instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return Product version of the EIS instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException  Failed to get the information\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public String getEISProductVersion() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "return null; //TODO");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output username method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeUsername(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent,
            " * Returns the user name for an active connection as known to the underlying EIS instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return String representing the user name\n");
      writeWithIndent(out, indent, " * @throws ResourceException  Failed to get the information\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public String getUserName() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1, "return null; //TODO");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
