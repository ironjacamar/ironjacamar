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
package org.jboss.jca.codegenerator;

import java.io.IOException;
import java.io.Writer;


/**
 * A managed connection CodeGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class McCodeGen extends AbstractCodeGen
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
      int indent = 1;
      out.write("public class " + getClassName(def) + " implements ManagedConnection");
      writeLeftCurlyBracket(out, 0);
      writeEol(out);

      writeIndent(out, indent);
      out.write("private static Logger log = Logger.getLogger(" + getClassName(def) + ".class);");
      writeEol(out);
      writeEol(out);

      writeDefaultConstructor(def, out, indent);
      
      writeConnection(def, out, indent);
      writeLifecycle(def, out, indent);
      writeConnectionEventListener(def, out, indent);
      writeLogWriter(def, out, indent);
      writeTransaction(def, out, indent);
      writeMetaData(def, out, indent);
      
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
      out.write("import java.io.PrintWriter;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.resource.ResourceException;");
      writeEol(out);
      out.write("import javax.resource.spi.ConnectionEventListener;");
      writeEol(out);
      out.write("import javax.resource.spi.ConnectionDefinition;");
      writeEol(out);
      out.write("import javax.resource.spi.ConnectionRequestInfo;");
      writeEol(out);
      out.write("import javax.resource.spi.LocalTransaction;");
      writeEol(out);
      out.write("import javax.resource.spi.ManagedConnection;");
      writeEol(out);
      out.write("import javax.resource.spi.ManagedConnectionMetaData;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.security.auth.Subject;");
      writeEol(out);
      out.write("import javax.transaction.xa.XAResource;");
      writeEol(out);
      writeEol(out);
      out.write("import org.jboss.logging.Logger;");
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
      return def.getMcClass();
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
      out.write("public Object getConnection(Subject subject,");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("ConnectionRequestInfo cxRequestInfo) throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      out.write("log.debug(\"call getConnection\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void associateConnection(Object connection) throws ResourceException");
      writeLeftCurlyBracket(out, indent);

      writeIndent(out, indent + 1);
      out.write("log.debug(\"call associateConnection\");");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output Lifecycle method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeLifecycle(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("public void cleanup() throws ResourceException");
      writeEol(out);
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call cleanup\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeIndent(out, indent);
      out.write("public void destroy() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call destroy\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output ConnectionEventListener method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeConnectionEventListener(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("public void addConnectionEventListener(ConnectionEventListener listener)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call addConnectionEventListener\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void removeConnectionEventListener(ConnectionEventListener listener)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call removeConnectionEventListener\");");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   /**
    * Output LogWriter method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeLogWriter(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("public PrintWriter getLogWriter() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call getLogWriter\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void setLogWriter(PrintWriter out) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call setLogWriter\");");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output Transaction method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeTransaction(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("public LocalTransaction getLocalTransaction() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call getLocalTransaction\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public XAResource getXAResource() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call getXAResource\");");
      writeEol(out);
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
      out.write("public ManagedConnectionMetaData getMetaData() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.debug(\"call getMetaData\");");
      writeEol(out);
      
      writeIndent(out, indent + 1);
      if (def.isUseCciConnection())
         out.write("return new MyManagedConnectionMetaData();");
      else
         out.write("return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
