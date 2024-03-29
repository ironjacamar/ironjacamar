/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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
 * A cci connection class CodeGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class CciConnCodeGen extends AbstractCodeGen
{
   /**
    * Output ResourceAdapater class
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
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Default constructor");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param connSpec ConnectionSpec");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public " + getClassName(def) + "(ConnectionSpec connSpec)");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeClose(def, out, indent);
      writeInteraction(def, out, indent);
      writeLocalTransaction (def, out, indent);
      writeMetaData(def, out, indent);
      writeResultSetInfo(def, out, indent);
      
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
      out.write("import jakarta.resource.ResourceException;");
      writeEol(out);
      writeEol(out);
      out.write("import jakarta.resource.cci.Connection;");
      writeEol(out);
      out.write("import jakarta.resource.cci.ConnectionMetaData;");
      writeEol(out);
      out.write("import jakarta.resource.cci.ConnectionSpec;");
      writeEol(out);
      out.write("import jakarta.resource.cci.Interaction;");
      writeEol(out);
      out.write("import jakarta.resource.cci.LocalTransaction;");
      writeEol(out);
      out.write("import jakarta.resource.cci.ResultSetInfo;");
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
      return def.getMcfDefs().get(getNumOfMcf()).getCciConnClass();
   }
   
   /**
    * Output close method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeClose(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Initiates close of the connection handle at the application level.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Exception thrown if close on a connection handle fails.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public void close() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output Interaction method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeInteraction(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Creates an Interaction associated with this Connection. ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return Interaction instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Failed to create an Interaction");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public Interaction createInteraction() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      out.write("return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output LocalTransaction method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeLocalTransaction(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Returns an LocalTransaction instance that enables a component to ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * demarcate resource manager local transactions on the Connection.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return LocalTransaction instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Failed to return a LocalTransaction");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws jakarta.resource.NotSupportedException Demarcation of Resource manager ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public LocalTransaction getLocalTransaction() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      out.write("return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output MetaData method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeMetaData(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Gets the information on the underlying EIS instance represented through an active connection.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return ConnectionMetaData instance representing information about the EIS instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Failed to get information about the connected EIS instance. ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public ConnectionMetaData getMetaData() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      out.write("return new " + def.getMcfDefs().get(getNumOfMcf()).getConnMetaClass() + "();");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   
   /**
    * Output ResultSetInfo method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeResultSetInfo(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Gets the information on the ResultSet functionality supported by ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * a connected EIS instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return ResultSetInfo instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Failed to get ResultSet related information");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws jakarta.resource.NotSupportedException ResultSet functionality is not supported");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public ResultSetInfo getResultSetInfo() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      out.write("return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
