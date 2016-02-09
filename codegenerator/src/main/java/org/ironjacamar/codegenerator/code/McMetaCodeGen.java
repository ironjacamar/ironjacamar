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
 * A managed connection MetaData class CodeGen.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class McMetaCodeGen extends AbstractCodeGen
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

      out.write("public class " + getClassName(def) + " implements ManagedConnectionMetaData");
      writeLeftCurlyBracket(out, 0);
      int indent = 1;

      writeWithIndent(out, indent, "/** The logger */\n");
      writeWithIndent(out, indent, "private static Logger log = Logger.getLogger(" + getSelfClassName(def) + ");\n\n");

      writeDefaultConstructor(def, out, indent);

      writeEIS(def, out, indent);
      writeMaxConnection(def, out, indent);
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
      importLogging(def, out);
      out.write("import javax.resource.ResourceException;\n\n");
      out.write("import javax.resource.spi.ManagedConnectionMetaData;\n\n");
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
      return def.getMcfDefs().get(getNumOfMcf()).getMcMetaClass();
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
      writeWithIndent(out, indent,
            " * Returns Product name of the underlying EIS instance connected through the ManagedConnection.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return Product name of the EIS instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException Thrown if an error occurs\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public String getEISProductName() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getEISProductName");
      writeWithIndent(out, indent + 1, "return null; //TODO");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent,
            " * Returns Product version of the underlying EIS instance connected through the ManagedConnection.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return Product version of the EIS instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException Thrown if an error occurs\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public String getEISProductVersion() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getEISProductVersion");
      writeWithIndent(out, indent + 1, "return null; //TODO");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output max connection method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeMaxConnection(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Returns maximum limit on number of active concurrent connections \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return Maximum limit for number of active concurrent connections\n");
      writeWithIndent(out, indent, " * @throws ResourceException Thrown if an error occurs\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public int getMaxConnections() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getMaxConnections");
      writeWithIndent(out, indent + 1, "return 0; //TODO");
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
      writeWithIndent(out, indent, " * Returns name of the user associated with the ManagedConnection instance\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return Name of the user\n");
      writeWithIndent(out, indent, " * @throws ResourceException Thrown if an error occurs\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public String getUserName() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getUserName");
      writeWithIndent(out, indent + 1, "return null; //TODO");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
