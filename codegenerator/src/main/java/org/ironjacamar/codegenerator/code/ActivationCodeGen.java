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
 * A Activation CodeGen.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class ActivationCodeGen extends AbstractCodeGen
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
      out.write("public class " + getClassName(def));
      writeLeftCurlyBracket(out, 0);
      writeEol(out);

      int indent = 1;

      writeWithIndent(out, indent, "/** The resource adapter */\n");
      writeWithIndent(out, indent, "private " + def.getRaClass() + " ra;\n\n");

      writeWithIndent(out, indent, "/** Activation spec */\n");
      writeWithIndent(out, indent, "private " + def.getAsClass() + " spec;\n\n");

      writeWithIndent(out, indent, "/** The message endpoint factory */\n");
      writeWithIndent(out, indent, "private MessageEndpointFactory endpointFactory;\n\n");

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Default constructor\n");
      writeWithIndent(out, indent, " * @exception ResourceException Thrown if an error occurs\n");
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent, "public " + getClassName(def) + "() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "this(null, null, null);");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      //constructor
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Constructor\n");
      writeWithIndent(out, indent, " * @param ra " + def.getRaClass());
      writeEol(out);
      writeWithIndent(out, indent, " * @param endpointFactory MessageEndpointFactory\n");
      writeWithIndent(out, indent, " * @param spec " + def.getAsClass());
      writeEol(out);
      writeWithIndent(out, indent, " * @exception ResourceException Thrown if an error occurs\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public " + getClassName(def) + "(" + def.getRaClass() + " ra, \n");
      writeWithIndent(out, indent + 1, "MessageEndpointFactory endpointFactory,\n");
      writeWithIndent(out, indent + 1, def.getAsClass() + " spec) throws ResourceException\n");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "this.ra = ra;\n");
      writeWithIndent(out, indent + 1, "this.endpointFactory = endpointFactory;\n");
      writeWithIndent(out, indent + 1, "this.spec = spec;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeGetAs(def, out, indent);
      writeMef(def, out, indent);
      writeStartStop(def, out, indent);

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
      out.write("package " + def.getRaPackage() + ".inflow;\n\n");
      out.write("import " + def.getRaPackage() + "." + def.getRaClass() + ";\n\n");
      out.write("import javax.resource.ResourceException;\n");
      out.write("import javax.resource.spi.endpoint.MessageEndpointFactory;\n\n");
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
      return def.getActivationClass();
   }

   /**
    * Output get activation spec method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeGetAs(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Get activation spec class\n");
      writeWithIndent(out, indent, " * @return Activation spec\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public " + def.getAsClass() + " getActivationSpec()");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "return spec;");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output message endpoint factory method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeMef(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Get message endpoint factory\n");
      writeWithIndent(out, indent, " * @return Message endpoint factory\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public MessageEndpointFactory getMessageEndpointFactory()");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "return endpointFactory;");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output start and stop  method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeStartStop(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Start the activation\n");
      writeWithIndent(out, indent, " * @throws ResourceException Thrown if an error occurs\n");
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent, "public void start() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Stop the activation\n");
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent, "public void stop()");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
