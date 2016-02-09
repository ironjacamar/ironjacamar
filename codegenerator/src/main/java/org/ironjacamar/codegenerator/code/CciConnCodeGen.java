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
 * A cci connection class CodeGen.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class CciConnCodeGen extends AbstractCodeGen
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

      out.write("public class " + getClassName(def) + " implements Connection");
      writeLeftCurlyBracket(out, 0);
      int indent = 1;

      writeDefaultConstructor(def, out, indent);

      //constructor
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Default constructor\n");
      writeWithIndent(out, indent, " * @param connSpec ConnectionSpec\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public " + getClassName(def) + "(ConnectionSpec connSpec)");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeClose(def, out, indent);
      writeInteraction(def, out, indent);
      writeLocalTransaction(def, out, indent);
      writeMetaData(def, out, indent);
      writeResultSetInfo(def, out, indent);

      writeEol(out);
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Set ManagedConnection\n");
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent,
            "void setManagedConnection(" + def.getMcfDefs().get(getNumOfMcf()).getMcClass() + " mc)");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);

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
      out.write("import javax.resource.cci.Connection;\n");
      out.write("import javax.resource.cci.ConnectionMetaData;\n");
      out.write("import javax.resource.cci.ConnectionSpec;\n");
      out.write("import javax.resource.cci.Interaction;\n");
      out.write("import javax.resource.cci.LocalTransaction;\n");
      out.write("import javax.resource.cci.ResultSetInfo;\n\n");
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
      return def.getMcfDefs().get(getNumOfMcf()).getCciConnClass();
   }

   /**
    * Output close method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeClose(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Initiates close of the connection handle at the application level.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent,
            " * @throws ResourceException Exception thrown if close on a connection handle fails.\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public void close() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output Interaction method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeInteraction(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Creates an Interaction associated with this Connection. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return Interaction instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException Failed to create an Interaction\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public Interaction createInteraction() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1, "return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output LocalTransaction method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeLocalTransaction(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Returns an LocalTransaction instance that enables a component to \n");
      writeWithIndent(out, indent, " * demarcate resource manager local transactions on the Connection.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return LocalTransaction instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException Failed to return a LocalTransaction\n");
      writeWithIndent(out, indent,
            " * @throws javax.resource.NotSupportedException Demarcation of Resource manager \n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public LocalTransaction getLocalTransaction() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1, "return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output MetaData method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeMetaData(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent,
            " * Gets the information on the underlying EIS instance represented through an active connection.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent,
            " * @return ConnectionMetaData instance representing information about the EIS instance\n");
      writeWithIndent(out, indent,
            " * @throws ResourceException Failed to get information about the connected EIS instance. \n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public ConnectionMetaData getMetaData() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1, "return new " + def.getMcfDefs().get(getNumOfMcf()).getConnMetaClass() + "();");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output ResultSetInfo method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeResultSetInfo(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Gets the information on the ResultSet functionality supported by \n");
      writeWithIndent(out, indent, " * a connected EIS instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return ResultSetInfo instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException Failed to get ResultSet related information\n");
      writeWithIndent(out, indent,
            " * @throws javax.resource.NotSupportedException ResultSet functionality is not supported\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public ResultSetInfo getResultSetInfo() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1, "return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
