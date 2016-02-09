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
 * A connection factory class CodeGen.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class CfCodeGen extends AbstractCodeGen
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

      out.write("public class " + getClassName(def) + " implements " +
            def.getMcfDefs().get(getNumOfMcf()).getCfInterfaceClass());
      writeLeftCurlyBracket(out, 0);
      int indent = 1;

      writeWithIndent(out, indent, "/** The serial version UID */\n");
      writeWithIndent(out, indent, "private static final long serialVersionUID = 1L;\n\n");

      writeWithIndent(out, indent, "/** The logger */\n");
      writeWithIndent(out, indent, "private static Logger log = Logger.getLogger(" + getSelfClassName(def) + ");\n\n");

      writeWithIndent(out, indent, "/** Reference */\n");
      writeWithIndent(out, indent, "private Reference reference;\n\n");

      writeWithIndent(out, indent, "/** ManagedConnectionFactory */\n");
      writeWithIndent(out, indent, "private " + def.getMcfDefs().get(getNumOfMcf()).getMcfClass() + " mcf;\n\n");

      writeWithIndent(out, indent, "/** ConnectionManager */\n");
      writeWithIndent(out, indent, "private ConnectionManager connectionManager;\n\n");

      writeDefaultConstructor(def, out, indent);

      //constructor
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Default constructor\n");
      writeWithIndent(out, indent, " * @param mcf ManagedConnectionFactory\n");
      writeWithIndent(out, indent, " * @param cxManager ConnectionManager\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent,
            "public " + getClassName(def) + "(" + def.getMcfDefs().get(getNumOfMcf()).getMcfClass() +
                  " mcf, ConnectionManager cxManager)");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "this.mcf = mcf;\n");
      writeWithIndent(out, indent + 1, "this.connectionManager = cxManager;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeConnection(def, out, indent);
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
      out.write("package " + def.getRaPackage() + ";");

      writeEol(out);
      writeEol(out);
      importLogging(def, out);
      out.write("import javax.naming.NamingException;\n");
      out.write("import javax.naming.Reference;\n\n");
      out.write("import javax.resource.ResourceException;\n");
      out.write("import javax.resource.spi.ConnectionManager;\n\n");
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
      return def.getMcfDefs().get(getNumOfMcf()).getCfClass();
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

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public " + def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass() +
            " getConnection() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getConnection");

      writeWithIndent(out, indent + 1, "return (" + def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass() +
            ")connectionManager.allocateConnection(mcf, null);");
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
      writeWithIndent(out, indent, " * @exception NamingException Thrown if a reference can't be obtained\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public Reference getReference() throws NamingException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getReference");
      writeIndent(out, indent + 1);
      out.write("return reference;");
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
      writeLogging(def, out, indent + 1, "trace", "setReference", "reference");

      writeIndent(out, indent + 1);
      out.write("this.reference = reference;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
