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

import org.jboss.jca.codegenerator.Definition;

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
      
      writeIndent(out, indent);
      out.write("/** The resource adapter */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private " + def.getRaClass() + " ra;");
      writeEol(out);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/** Activation spec */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private " + def.getAsClass() + " spec;");
      writeEol(out);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/** The message endpoint factory */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private MessageEndpointFactory endpointFactory;");
      writeEol(out);
      writeEol(out);

      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Default constructor");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @exception ResourceException Thrown if an error occurs");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public " + getClassName(def) + "() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("this(null, null, null);");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      //constructor
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Constructor");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param ra " + def.getRaClass());
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param endpointFactory MessageEndpointFactory");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param spec " + def.getAsClass());
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @exception ResourceException Thrown if an error occurs");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public " + getClassName(def) + "(" + def.getRaClass() + " ra, ");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("MessageEndpointFactory endpointFactory,");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write(def.getAsClass() + " spec) throws ResourceException");
      writeEol(out);
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("this.ra = ra;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("this.endpointFactory = endpointFactory;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("this.spec = spec;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      

      writeGetAs(def, out, indent);
      writeMef(def, out, indent);
      writeStartStop(def, out, indent);
      
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
      out.write("import " + def.getRaPackage() + "." + def.getRaClass() + ";");
      writeEol(out);
      writeEol(out);
      out.write("import javax.resource.ResourceException;");
      writeEol(out);
      out.write("import javax.resource.spi.endpoint.MessageEndpointFactory;");
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
      return def.getActivationClass();
   }

   /**
    * Output get activation spec method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeGetAs(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Get activation spec class");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return Activation spec");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public " + def.getAsClass() + " getActivationSpec()");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("return spec;");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output message endpoint factory method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeMef(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Get message endpoint factory");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return Message endpoint factory");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public MessageEndpointFactory getMessageEndpointFactory()");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("return endpointFactory;");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output start and stop  method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeStartStop(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Start the activation");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Thrown if an error occurs");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public void start() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Stop the activation");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public void stop()");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
