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
 * A Cci connection factory class CodeGen.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class CciConnFactoryCodeGen extends AbstractCodeGen
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

      out.write("public class " + getClassName(def) + " implements ConnectionFactory");
      writeLeftCurlyBracket(out, 0);
      int indent = 1;

      writeWithIndent(out, indent, "private Reference reference;\n\n");

      writeDefaultConstructor(def, out, indent);

      //constructor
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Default constructor\n");
      writeWithIndent(out, indent, " * @param cxManager ConnectionManager\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public " + getClassName(def) + "(ConnectionManager cxManager)");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeConnection(def, out, indent);
      writeMetaData(def, out, indent);
      writeRecordFactory(def, out, indent);
      writeReference(def, out, indent);
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
      out.write("import javax.naming.NamingException;\n");
      out.write("import javax.naming.Reference;\n\n");
      out.write("import javax.resource.ResourceException;\n");
      out.write("import javax.resource.cci.Connection;\n");
      out.write("import javax.resource.cci.ConnectionFactory;\n");
      out.write("import javax.resource.cci.ConnectionSpec;\n");
      out.write("import javax.resource.cci.RecordFactory;\n");
      out.write("import javax.resource.cci.ResourceAdapterMetaData;\n");
      out.write("import javax.resource.spi.ConnectionManager;\n\n");
      if (def.getMcfDefs().size() != 1)
         out.write("import " + def.getRaPackage() + ".*;\n\n");
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
      return def.getMcfDefs().get(getNumOfMcf()).getCciConnFactoryClass();
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
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Gets a connection to an EIS instance. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return Connection instance the EIS instance.\n");
      writeWithIndent(out, indent, " * @throws ResourceException Failed to get a connection to\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public Connection getConnection() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1,
            "return new " + def.getMcfDefs().get(getNumOfMcf()).getCciConnClass() + "(new " + def.getMcfDefs()
                  .get(getNumOfMcf()).getConnSpecClass() + "());");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Gets a connection to an EIS instance. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param connSpec Connection parameters and security information specified as "
            + "ConnectionSpec instance\n");
      writeWithIndent(out, indent, " * @return Connection instance the EIS instance.\n");
      writeWithIndent(out, indent, " * @throws ResourceException Failed to get a connection to\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public Connection getConnection(ConnectionSpec connSpec) throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1,
            "return new " + def.getMcfDefs().get(getNumOfMcf()).getCciConnClass() + "(connSpec);");
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
      writeWithIndent(out, indent, " * Gets metadata for the Resource Adapter. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return ResourceAdapterMetaData instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException Failed to get metadata information \n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public ResourceAdapterMetaData getMetaData() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      if (def.isUseRa())
      {
         out.write("return new " + def.getRaMetaClass() + "();");
      }
      else
      {
         out.write("return null;");
      }
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output RecordFactory method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeRecordFactory(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Gets a RecordFactory instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return RecordFactory instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException Failed to create a RecordFactory\n");
      writeWithIndent(out, indent, " * @throws javax.resource.NotSupportedException Operation not supported\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public RecordFactory getRecordFactory() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1, "return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output Reference method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeReference(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Get the Reference instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return Reference instance\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public Reference getReference() throws NamingException");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "return reference;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Set the Reference instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param reference A Reference instance\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public void setReference(Reference reference)");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "this.reference = reference;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
