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
         if (!def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         {
            out.write("@ConnectionDefinition(connectionFactory = " + 
               def.getMcfDefs().get(getNumOfMcf()).getCfInterfaceClass() + ".class,");
            writeEol(out);
            writeIndent(out, indent);
            out.write("connectionFactoryImpl = " + def.getMcfDefs().get(getNumOfMcf()).getCfClass() + ".class,");
            writeEol(out);
            writeIndent(out, indent);
            out.write("connection = " + def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass() + ".class,");
            writeEol(out);
            writeIndent(out, indent);
            out.write("connectionImpl = " + def.getMcfDefs().get(getNumOfMcf()).getConnImplClass() + ".class)");
            writeEol(out);
         }
         else
         {
            out.write("@ConnectionDefinition(connectionFactory = ConnectionFactory.class,");
            writeEol(out);
            writeIndent(out, indent);
            out.write("connectionFactoryImpl = " + 
               def.getMcfDefs().get(getNumOfMcf()).getCciConnFactoryClass() + ".class,");
            writeEol(out);
            
            writeIndent(out, indent);
            out.write("connection = Connection.class,");
            writeEol(out);
            writeIndent(out, indent);
            out.write("connectionImpl = " + def.getMcfDefs().get(getNumOfMcf()).getCciConnClass() + ".class)");
            writeEol(out);
         }
      }

      out.write("public class " + getClassName(def) + " implements ManagedConnectionFactory");
      if (def.getMcfDefs().get(getNumOfMcf()).isImplRaAssociation())
      {
         out.write(", ResourceAdapterAssociation");
      }
      writeLeftCurlyBracket(out, 0);
      writeEol(out);

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
      out.write("private static Logger log = Logger.getLogger(\"" + getClassName(def) + "\");");
      writeEol(out);
      writeEol(out);
      if (def.getMcfDefs().get(getNumOfMcf()).isImplRaAssociation())
      {
         writeIndent(out, indent);
         out.write("/** The resource adapter */");
         writeEol(out);
         writeIndent(out, indent);
         if (def.isRaSerial())
         {
            out.write("private ResourceAdapter ra;");
         }
         else
         {
            out.write("private transient ResourceAdapter ra;");
         }
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
      
      writeConfigPropsDeclare(def, out, indent);
      
      writeDefaultConstructor(def, out, indent);
      
      writeConfigProps(def, out, indent);
      writeConnectionFactory(def, out, indent);
      writeManagedConnection(def, out, indent);
      writeLogWriter(def, out, indent);
      if (def.getMcfDefs().get(getNumOfMcf()).isImplRaAssociation())
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
      out.write("import java.util.Iterator;");
      writeEol(out);
      out.write("import java.util.Set;");
      writeEol(out);
      writeEol(out);
      out.write("import java.util.logging.Logger;");
      writeEol(out);
      writeEol(out);

      out.write("import javax.resource.ResourceException;");
      writeEol(out);
      
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
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
   }
   
   /**
    * get this class name
    * @param def definition
    * @return String class name
    */
   @Override
   public String getClassName(Definition def)
   {
      return def.getMcfDefs().get(getNumOfMcf()).getMcfClass();
   }
   
   /**
    * get list of ConfigPropType
    * @param def definition
    * @return List<ConfigPropType> List of ConfigPropType
    */
   @Override
   public List<ConfigPropType> getConfigProps(Definition def)
   {
      return def.getMcfDefs().get(getNumOfMcf()).getMcfConfigProps();
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
      out.write(" * @param cxManager ConnectionManager to be " + 
         "associated with created EIS connection factory instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return EIS-specific Connection Factory instance or " +
         "javax.resource.cci.ConnectionFactory instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.finest(\"createConnectionFactory()\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         out.write("return new " + def.getMcfDefs().get(getNumOfMcf()).getCciConnFactoryClass() + "(cxManager);");
      else
         out.write("return new " + def.getMcfDefs().get(getNumOfMcf()).getCfClass() + "(this, cxManager);");
      
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
      out.write(" * @return EIS-specific Connection Factory instance or " +
         "javax.resource.cci.ConnectionFactory instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException Generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public Object createConnectionFactory() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("throw new ResourceException(\"This resource adapter doesn't support non-managed environments\");");

      writeRightCurlyBracket(out, indent);
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
      out.write(" * @param subject Caller's security information");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param cxRequestInfo Additional resource adapter " +
         "specific connection request information");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return ManagedConnection instance ");
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
      writeIndent(out, indent + 1);
      out.write("log.finest(\"createManagedConnection()\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return new " + def.getMcfDefs().get(getNumOfMcf()).getMcClass() + "(this);");
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
      out.write(" * @param connectionSet Candidate connection set");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param subject Caller's security information");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param cxRequestInfo Additional resource adapter " +
         "specific connection request information");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return ManagedConnection if resource adapter finds an acceptable match otherwise null ");
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
      writeIndent(out, indent + 1);
      out.write("log.finest(\"matchManagedConnections()\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("ManagedConnection result = null;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("Iterator it = connectionSet.iterator();");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("while (result == null && it.hasNext())");
      writeLeftCurlyBracket(out, indent + 1);
      writeIndent(out, indent + 2);
      out.write("ManagedConnection mc = (ManagedConnection)it.next();");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("if (mc instanceof " + def.getMcfDefs().get(getNumOfMcf()).getMcClass() + ")");
      writeLeftCurlyBracket(out, indent + 2);
      writeIndent(out, indent + 3);
      out.write("result = mc;");
      writeRightCurlyBracket(out, indent + 2);
      writeRightCurlyBracket(out, indent + 1);
      writeIndent(out, indent + 1);
      out.write("return result;");

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
      out.write(" * @return PrintWriter");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public PrintWriter getLogWriter() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.finest(\"getLogWriter()\");");
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
      out.write(" * @param out PrintWriter - an out stream for error logging and tracing");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @throws ResourceException generic exception");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void setLogWriter(PrintWriter out) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("log.finest(\"setLogWriter()\");");
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
      out.write("log.finest(\"getResourceAdapter()\");");
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
      out.write("log.finest(\"setResourceAdapter()\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("this.ra = ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
