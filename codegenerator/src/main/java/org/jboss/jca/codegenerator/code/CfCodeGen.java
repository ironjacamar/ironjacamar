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
 * A connection factory class CodeGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class CfCodeGen extends AbstractCodeGen
{
   /**
    * Output class code
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
      
      writeIndent(out, indent);
      out.write("/** The serial version UID */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private static final long serialVersionUID = 1L;");
      writeEol(out);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/** The logger */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private static Logger log = Logger.getLogger(" + getSelfClassName(def) + ");");
      writeEol(out);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/** Reference */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private Reference reference;");
      writeEol(out);
      writeEol(out);

      writeIndent(out, indent);
      out.write("/** ManagedConnectionFactory */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private " + def.getMcfDefs().get(getNumOfMcf()).getMcfClass() + " mcf;");
      writeEol(out);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/** ConnectionManager */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private ConnectionManager connectionManager;");
      writeEol(out);
      writeEol(out);
      
      writeDefaultConstructor(def, out, indent);
      
      //constructor
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Default constructor");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param mcf ManagedConnectionFactory");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param cxManager ConnectionManager");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public " + getClassName(def) + "(" + def.getMcfDefs().get(getNumOfMcf()).getMcfClass() + 
         " mcf, ConnectionManager cxManager)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("this.mcf = mcf;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("this.connectionManager = cxManager;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeConnection(def, out, indent);
      writeReference(def, out, indent);
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
      out.write("package " + def.getRaPackage() + ";");

      writeEol(out);
      writeEol(out);
      out.write("import java.util.logging.Logger;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.naming.NamingException;");
      writeEol(out);
      out.write("import javax.naming.Reference;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.resource.ResourceException;");
      writeEol(out);
      out.write("import javax.resource.spi.ConnectionManager;");
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
      return def.getMcfDefs().get(getNumOfMcf()).getCfClass();
   }
   
   /**
    * Output Connection method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeConnection(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/** ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Get connection from factory");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return " + def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass() + " instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @exception ResourceException Thrown if a connection can't be obtained");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public " + def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass() + 
         " getConnection() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.finest(\"getConnection()\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return (" + def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass() + 
         ")connectionManager.allocateConnection(mcf, null);");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output Reference method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeReference(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Get the Reference instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return Reference instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @exception NamingException Thrown if a reference can't be obtained");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public Reference getReference() throws NamingException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.finest(\"getReference()\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return reference;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Set the Reference instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param reference A Reference instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public void setReference(Reference reference)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.finest(\"setReference()\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("this.reference = reference;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
