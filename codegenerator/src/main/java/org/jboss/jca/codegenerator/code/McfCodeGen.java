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

import org.jboss.jca.codegenerator.ConfigPropType;
import org.jboss.jca.codegenerator.Definition;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * A McfCodeGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class McfCodeGen extends PropsCodeGen
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
      if (def.isUseAnnotation())
      {
         if (!def.isUseCciConnection())
         {
            out.write("@ConnectionDefinition(connectionFactory = " + def.getCfInterfaceClass() + ".class,");
            writeEol(out);
            writeIndent(out, indent);
            out.write("connectionFactoryImpl = " + def.getCfClass() + ".class,");
            writeEol(out);
            writeIndent(out, indent);
            out.write("connection = " + def.getConnInterfaceClass() + ".class,");
            writeEol(out);
            writeIndent(out, indent);
            out.write("connectionImpl = " + def.getConnImplClass() + ".class)");
            writeEol(out);
         }
         else
         {
            out.write("@ConnectionDefinition(connectionFactory = ConnectionFactory.class,");
            writeEol(out);
            writeIndent(out, indent);
            out.write("connectionFactoryImpl = " + def.getCciConnFactoryClass() + ".class,");
            writeEol(out);
            
            writeIndent(out, indent);
            out.write("connection = Connection.class,");
            writeEol(out);
            writeIndent(out, indent);
            out.write("connectionImpl = " + def.getCciConnClass() + ".class)");
            writeEol(out);
         }
      }

      out.write("public class " + getClassName(def) + " implements ManagedConnectionFactory");
      if (def.isImplRaAssociation())
      {
         out.write(", ResourceAdapterAssociation");
      }
      writeLeftCurlyBracket(out, 0);
      writeEol(out);

      writeIndent(out, indent);
      out.write("/** The logger */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private static Logger log = Logger.getLogger(\"" + getClassName(def) + "\");");
      writeEol(out);
      writeEol(out);
      if (def.isImplRaAssociation())
      {
         writeIndent(out, indent);
         out.write("/** The resource adapter */");
         writeEol(out);
         writeIndent(out, indent);
         out.write("private ResourceAdapter ra;");
         writeEol(out);
         writeEol(out);
      }
      
      writeIndent(out, indent);
      out.write("/** The logwriter */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private PrintWriter logwriter;");
      writeEol(out);
      writeEol(out);
      
      
      writeDefaultConstructor(def, out, indent);
      
      writeConfigProps(def, out, indent);
      writeConnectionFactory(def, out, indent);
      writeManagedConnection(def, out, indent);
      writeLogWriter(def, out, indent);
      if (def.isImplRaAssociation())
      {
         writeResourceAdapter(def, out, indent);
      }
      
      writeHashCode(def, out, indent);
      writeEquals(def, out, indent);
      
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
      out.write("import java.util.Set;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.resource.ResourceException;");
      writeEol(out);
      
      if (def.isUseCciConnection())
      {
         out.write("import javax.resource.cci.Connection;");
         writeEol(out);
         out.write("import javax.resource.cci.ConnectionFactory;");
         writeEol(out);
      }
      
      if (def.isUseAnnotation())
      {
         out.write("import javax.resource.spi.ConfigProperty;");
         writeEol(out);
         out.write("import javax.resource.spi.ConnectionDefinition;");
         writeEol(out);
      }
      out.write("import javax.resource.spi.ConnectionManager;");
      writeEol(out);
      out.write("import javax.resource.spi.ConnectionRequestInfo;");
      writeEol(out);
      out.write("import javax.resource.spi.ManagedConnection;");
      writeEol(out);
      out.write("import javax.resource.spi.ManagedConnectionFactory;");
      writeEol(out);
      out.write("import javax.resource.spi.ResourceAdapter;");
      writeEol(out);
      out.write("import javax.resource.spi.ResourceAdapterAssociation;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.security.auth.Subject;");
      writeEol(out);
      writeEol(out);
      out.write("import java.util.logging.Logger;");
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
      return def.getMcfClass();
   }
   
   /**
    * get list of ConfigPropType
    * @param def definition
    * @return List<ConfigPropType> List of ConfigPropType
    */
   @Override
   public List<ConfigPropType> getConfigProps(Definition def)
   {
      return def.getMcfConfigProps();
   }
   
   /**
    * Output ConnectionFactory method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeConnectionFactory(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Creates a Connection Factory instance. ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param    cxManager    ConnectionManager to be " + 
         "associated with created EIS connection factory instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return   EIS-specific Connection Factory instance or " +
         "javax.resource.cci.ConnectionFactory instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws   ResourceException Generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      if (def.isImplRaAssociation())
      {
         writeIfRaNull(out, indent);
      }
      writeIndent(out, indent + 1);
      out.write("log.info(\"call createConnectionFactory\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      if (def.isUseCciConnection())
         out.write("return new " + def.getCciConnFactoryClass() + "(cxManager);");
      else
         out.write("return new " + def.getCfClass() + "(cxManager);");
      
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Creates a Connection Factory instance. ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return   EIS-specific Connection Factory instance or " +
         "javax.resource.cci.ConnectionFactory instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws   ResourceException Generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public Object createConnectionFactory() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      if (def.isImplRaAssociation())
      {
         writeIfRaNull(out, indent);
      }

      writeIndent(out, indent + 1);
      if (def.isUseCciConnection())
         out.write("return new " + def.getCciConnFactoryClass() + "(new " + def.getCmClass() + "());");
      else
      {
         out.write("return new " + def.getCfClass() + "(new " + def.getCmClass() + "());");
      }
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output if (ra == null) 
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeIfRaNull(Writer out, int indent) throws IOException
   {
      writeIndent(out, indent + 1);
      out.write("if (ra == null)");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("throw new IllegalStateException(\"RA is null\");");
      writeEol(out);
   }
   
   /**
    * Output ConnectionFactory method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeManagedConnection(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Creates a new physical connection to the underlying EIS resource manager.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param   subject        Caller's security information");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param   cxRequestInfo  Additional resource adapter " +
         "specific connection request information");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws  ResourceException     generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return  ManagedConnection instance ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public ManagedConnection createManagedConnection(Subject subject,");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("ConnectionRequestInfo cxRequestInfo) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      if (def.isImplRaAssociation())
      {
         writeIfRaNull(out, indent);
      }
      writeIndent(out, indent + 1);
      out.write("log.info(\"call createManagedConnection\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return null;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Returns a matched connection from the candidate set of connections. ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param   connectionSet   candidate connection set");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param   subject        Caller's security information");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param   cxRequestInfo  Additional resource adapter " +
         "specific connection request information");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws  ResourceException     generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return  ManagedConnection if resource adapter finds an acceptable match otherwise null ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public ManagedConnection matchManagedConnections(Set connectionSet,");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      if (def.isImplRaAssociation())
      {
         writeIfRaNull(out, indent);
      }
      writeIndent(out, indent + 1);
      out.write("log.info(\"call matchManagedConnections\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return null;");
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
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Get the log writer for this ManagedConnectionFactory instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return  PrintWriter");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws  ResourceException     generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public PrintWriter getLogWriter() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.info(\"call getLogWriter\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return logwriter;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Set the log writer for this ManagedConnectionFactory instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param   out PrintWriter - an out stream for error logging and tracing");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws  ResourceException     generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void setLogWriter(PrintWriter out) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.info(\"call setLogWriter\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("logwriter = out;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output ResourceAdapter method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeResourceAdapter(Definition def, Writer out, int indent) throws IOException
   {      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Get the resource adapter");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return The handle");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public ResourceAdapter getResourceAdapter()");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.info(\"call getResourceAdapter\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Set the resource adapter");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param ra The handle");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void setResourceAdapter(ResourceAdapter ra)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.info(\"call setResourceAdapter\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("this.ra = ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
