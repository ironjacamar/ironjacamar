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
 * A Cci connection factory class CodeGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class CciConnFactoryCodeGen extends AbstractCodeGen
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

      out.write("public class " + getClassName(def) + " implements ConnectionFactory");
      writeLeftCurlyBracket(out, 0);
      int indent = 1;
      
      writeIndent(out, indent);
      out.write("private Reference reference;");
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
      out.write(" * @param cxManager ConnectionManager");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public " + getClassName(def) + "(ConnectionManager cxManager)");
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
      out.write("import javax.naming.NamingException;");
      writeEol(out);
      out.write("import javax.naming.Reference;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.resource.ResourceException;");
      writeEol(out);
      out.write("import javax.resource.cci.Connection;");
      writeEol(out);
      out.write("import javax.resource.cci.ConnectionFactory;");
      writeEol(out);
      out.write("import javax.resource.cci.ConnectionSpec;");
      writeEol(out);
      out.write("import javax.resource.cci.RecordFactory;");
      writeEol(out);
      out.write("import javax.resource.cci.ResourceAdapterMetaData;");
      writeEol(out);
      out.write("import javax.resource.spi.ConnectionManager;");
      writeEol(out);
      writeEol(out);
      if (def.getMcfDefs().size() != 1)
         out.write("import " + def.getRaPackage() + ".*;");
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
      return def.getMcfDefs().get(getNumOfMcf()).getCciConnFactoryClass();
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
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Gets a connection to an EIS instance. ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return Connection instance the EIS instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Failed to get a connection to");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public Connection getConnection() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      out.write("return new " + def.getMcfDefs().get(getNumOfMcf()).getCciConnClass()
         + "(new " + def.getMcfDefs().get(getNumOfMcf()).getConnSpecClass() + "());");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Gets a connection to an EIS instance. ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param connSpec Connection parameters and security information specified as " +
         "ConnectionSpec instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return Connection instance the EIS instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Failed to get a connection to");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public Connection getConnection(ConnectionSpec connSpec) throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      out.write("return new " + def.getMcfDefs().get(getNumOfMcf()).getCciConnClass() + "(connSpec);");
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
      out.write(" * Gets metadata for the Resource Adapter. ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return ResourceAdapterMetaData instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Failed to get metadata information ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public ResourceAdapterMetaData getMetaData() throws ResourceException");
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
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeRecordFactory(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Gets a RecordFactory instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return RecordFactory instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Failed to create a RecordFactory");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws javax.resource.NotSupportedException Operation not supported");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public RecordFactory getRecordFactory() throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      out.write("return null;");
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
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public Reference getReference() throws NamingException");
      writeLeftCurlyBracket(out, indent);
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
      out.write("this.reference = reference;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
