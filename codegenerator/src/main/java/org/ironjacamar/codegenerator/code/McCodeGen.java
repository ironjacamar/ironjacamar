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

import org.ironjacamar.codegenerator.BasicType;
import org.ironjacamar.codegenerator.Definition;
import org.ironjacamar.codegenerator.MethodForConnection;

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
    *
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

      writeWithIndent(out, indent, "/** The logger */\n");
      writeWithIndent(out, indent, "private static Logger log = Logger.getLogger(" + getSelfClassName(def) + ");\n\n");

      writeWithIndent(out, indent, "/** The logwriter */\n");
      writeWithIndent(out, indent, "private PrintWriter logwriter;\n\n");

      writeWithIndent(out, indent, "/** ManagedConnectionFactory */\n");
      writeWithIndent(out, indent, "private " + def.getMcfDefs().get(getNumOfMcf()).getMcfClass() + " mcf;\n\n");

      writeWithIndent(out, indent, "/** Listeners */\n");
      writeWithIndent(out, indent, "private List<ConnectionEventListener> listeners;\n\n");

      writeWithIndent(out, indent, "/** Connections */\n");
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         writeWithIndent(out, indent,
               "private Set<" + def.getMcfDefs().get(getNumOfMcf()).getCciConnClass() + "> connections;\n\n");
      else
         writeWithIndent(out, indent,
               "private Set<" + def.getMcfDefs().get(getNumOfMcf()).getConnImplClass() + "> connections;\n\n");

      if (def.isSupportEis())
      {
         writeWithIndent(out, indent, "/** Socket */\n");
         writeWithIndent(out, indent, "private Socket socket;\n\n");
      }

      //constructor
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Default constructor\n");
      writeWithIndent(out, indent, " * @param mcf mcf\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent,
            "public " + getClassName(def) + "(" + def.getMcfDefs().get(getNumOfMcf()).getMcfClass() + " mcf)");
      if (def.isSupportEis())
      {
         out.write(" throws ResourceException");
      }
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "this.mcf = mcf;\n");
      writeWithIndent(out, indent + 1, "this.logwriter = null;\n");
      writeWithIndent(out, indent + 1,
            "this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));\n");
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         writeWithIndent(out, indent + 1,
               "this.connections = new HashSet<" + def.getMcfDefs().get(getNumOfMcf()).getCciConnClass() + ">();");
      else
         writeWithIndent(out, indent + 1,
               "this.connections = new HashSet<" + def.getMcfDefs().get(getNumOfMcf()).getConnImplClass() + ">();");

      if (def.isSupportEis())
      {
         writeEol(out);
         writeWithIndent(out, indent + 1, "this.socket = null; // TODO: Initialize me");
      }

      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeConnection(def, out, indent);
      writeLifecycle(def, out, indent);
      writeConnectionEventListener(def, out, indent);
      writeLogWriter(def, out, indent);
      writeTransaction(def, out, indent);
      writeMetaData(def, out, indent);
      writeMethod(def, out, indent);

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

      if (def.isSupportEis())
      {
         out.write("import java.io.IOException;\n");
      }
      out.write("import java.io.PrintWriter;\n");

      if (def.isSupportEis())
      {
         out.write("import java.net.Socket;\n");
      }

      out.write("import java.util.ArrayList;\n");
      out.write("import java.util.Collections;\n");
      out.write("import java.util.HashSet;\n");
      out.write("import java.util.List;\n");
      out.write("import java.util.Set;\n");
      importLogging(def, out);
      out.write("import javax.resource.NotSupportedException;\n");
      out.write("import javax.resource.ResourceException;\n");
      out.write("import javax.resource.spi.ConnectionEvent;\n");
      out.write("import javax.resource.spi.ConnectionEventListener;\n");
      out.write("import javax.resource.spi.ConnectionRequestInfo;\n");
      out.write("import javax.resource.spi.LocalTransaction;\n");
      out.write("import javax.resource.spi.ManagedConnection;\n");
      out.write("import javax.resource.spi.ManagedConnectionMetaData;\n\n");
      out.write("import javax.security.auth.Subject;\n");
      out.write("import javax.transaction.xa.XAResource;\n\n");
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
      return def.getMcfDefs().get(getNumOfMcf()).getMcClass();
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
      writeWithIndent(out, indent, " * Creates a new connection handle for the underlying physical connection \n");
      writeWithIndent(out, indent, " * represented by the ManagedConnection instance. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param subject Security context as JAAS subject\n");
      writeWithIndent(out, indent, " * @param cxRequestInfo ConnectionRequestInfo instance\n");
      writeWithIndent(out, indent, " * @return generic Object instance representing the connection handle. \n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception if operation fails\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public Object getConnection(Subject subject,\n");
      writeWithIndent(out, indent + 1, "ConnectionRequestInfo cxRequestInfo) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getConnection");
      writeIndent(out, indent + 1);
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         out.write(def.getMcfDefs().get(getNumOfMcf()).getCciConnClass() + " connection = new " +
               def.getMcfDefs().get(getNumOfMcf()).getCciConnClass() + "();\n");
      else
         out.write(def.getMcfDefs().get(getNumOfMcf()).getConnImplClass() + " connection = new " +
               def.getMcfDefs().get(getNumOfMcf()).getConnImplClass() + "(this, mcf);\n");
      writeWithIndent(out, indent + 1, "connections.add(connection);\n");
      writeWithIndent(out, indent + 1, "return connection;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Used by the container to change the association of an \n");
      writeWithIndent(out, indent, " * application-level connection handle with a ManagedConneciton instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param connection Application-level connection handle\n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception if operation fails\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void associateConnection(Object connection) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "associateConnection", "connection\n");
      writeWithIndent(out, indent + 1, "if (connection == null)\n");
      writeWithIndent(out, indent + 2, "throw new ResourceException(\"Null connection handle\");\n\n");
      writeWithIndent(out, indent + 1, "if (!(connection instanceof ");
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         out.write(def.getMcfDefs().get(getNumOfMcf()).getCciConnClass());
      else
         out.write(def.getMcfDefs().get(getNumOfMcf()).getConnImplClass());
      out.write("))\n");
      writeWithIndent(out, indent + 2, "throw new ResourceException(\"Wrong connection handle\");\n\n");
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         writeWithIndent(out, indent + 1, def.getMcfDefs().get(getNumOfMcf()).getCciConnClass());
      else
         writeWithIndent(out, indent + 1, def.getMcfDefs().get(getNumOfMcf()).getConnImplClass());
      out.write(" handle = (");
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         out.write(def.getMcfDefs().get(getNumOfMcf()).getCciConnClass());
      else
         out.write(def.getMcfDefs().get(getNumOfMcf()).getConnImplClass());
      out.write(")connection;\n");
      writeWithIndent(out, indent + 1, "handle.setManagedConnection(this);\n");
      writeWithIndent(out, indent + 1, "connections.add(handle);");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output Lifecycle method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeLifecycle(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent,
            " * Application server calls this method to force any cleanup on the ManagedConnection instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception if operation fails\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void cleanup() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "cleanup");
      writeWithIndent(out, indent + 1, "for (");
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         out.write(def.getMcfDefs().get(getNumOfMcf()).getCciConnClass());
      else
         out.write(def.getMcfDefs().get(getNumOfMcf()).getConnImplClass());
      out.write(" connection : connections)");
      writeLeftCurlyBracket(out, indent + 1);
      writeWithIndent(out, indent + 2, "connection.setManagedConnection(null);");
      writeRightCurlyBracket(out, indent + 1);
      writeWithIndent(out, indent + 1, "connections.clear();\n");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Destroys the physical connection to the underlying resource manager.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception if operation fails\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void destroy() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "destroy");

      if (def.isSupportEis())
      {
         writeEol(out);
         writeWithIndent(out, indent + 1, "if (socket != null)");
         writeLeftCurlyBracket(out, indent + 1);

         writeWithIndent(out, indent + 2, "try");
         writeLeftCurlyBracket(out, indent + 2);
         writeWithIndent(out, indent + 3, "socket.close();");
         writeRightCurlyBracket(out, indent + 2);
         writeWithIndent(out, indent + 2, "catch (IOException ioe)");
         writeLeftCurlyBracket(out, indent + 2);
         writeWithIndent(out, indent + 3, "// Ignore");
         writeRightCurlyBracket(out, indent + 2);
         writeRightCurlyBracket(out, indent + 1);
      }

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output ConnectionEventListener method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeConnectionEventListener(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Adds a connection event listener to the ManagedConnection instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param listener A new ConnectionEventListener to be registered\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void addConnectionEventListener(ConnectionEventListener listener)");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "addConnectionEventListener", "listener");
      writeWithIndent(out, indent + 1, "if (listener == null)\n");
      writeWithIndent(out, indent + 2, "throw new IllegalArgumentException(\"Listener is null\");\n");
      writeWithIndent(out, indent + 1, "listeners.add(listener);");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent,
            " * Removes an already registered connection event listener from the ManagedConnection instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param listener already registered connection event listener to be removed\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void removeConnectionEventListener(ConnectionEventListener listener)");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "removeConnectionEventListener", "listener");
      writeWithIndent(out, indent + 1, "if (listener == null)\n");
      writeWithIndent(out, indent + 2, "throw new IllegalArgumentException(\"Listener is null\");\n");
      writeWithIndent(out, indent + 1, "listeners.remove(listener);");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Close handle\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param handle The handle\n");
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent, "void closeHandle(");
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         out.write(def.getMcfDefs().get(getNumOfMcf()).getCciConnClass() + " handle)");
      else
         out.write(def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass() + " handle)");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "connections.remove((");
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         out.write(def.getMcfDefs().get(getNumOfMcf()).getCciConnClass());
      else
         out.write(def.getMcfDefs().get(getNumOfMcf()).getConnImplClass());
      out.write(")handle);\n");
      writeWithIndent(out, indent + 1,
            "ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);\n");
      writeWithIndent(out, indent + 1, "event.setConnectionHandle(handle);\n");
      writeWithIndent(out, indent + 1, "for (ConnectionEventListener cel : listeners)");
      writeLeftCurlyBracket(out, indent + 1);
      writeWithIndent(out, indent + 2, "cel.connectionClosed(event);");
      writeRightCurlyBracket(out, indent + 1);

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output Transaction method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeTransaction(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Returns an <code>javax.resource.spi.LocalTransaction</code> instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return LocalTransaction instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception if operation fails\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public LocalTransaction getLocalTransaction() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      if (def.getSupportTransaction().equals("NoTransaction"))
      {
         writeWithIndent(out, indent + 1, "throw new NotSupportedException(\"getLocalTransaction() not supported\");");
      }
      else
      {
         writeLogging(def, out, indent + 1, "trace", "getLocalTransaction");
         writeWithIndent(out, indent + 1, "return null;");
      }
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Returns an <code>javax.transaction.xa.XAresource</code> instance. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return XAResource instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception if operation fails\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public XAResource getXAResource() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      if (def.getSupportTransaction().equals("NoTransaction"))
      {
         writeWithIndent(out, indent + 1, "throw new NotSupportedException(\"getXAResource() not supported\");");
      }
      else
      {
         writeLogging(def, out, indent + 1, "trace", "getXAResource");
         writeWithIndent(out, indent + 1, "return null;");
      }

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
      writeWithIndent(out, indent,
            " * Gets the metadata information for this connection's underlying EIS resource manager instance. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return ManagedConnectionMetaData instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception if operation fails\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public ManagedConnectionMetaData getMetaData() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getMetaData");

      writeWithIndent(out, indent + 1, "return new " + def.getMcfDefs().get(getNumOfMcf()).getMcMetaClass() + "();");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output methods
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeMethod(Definition def, Writer out, int indent) throws IOException
   {
      if (def.getMcfDefs().get(getNumOfMcf()).isDefineMethodInConnection())
      {
         if (def.getMcfDefs().get(getNumOfMcf()).getMethods().size() > 0)
         {
            for (MethodForConnection method : def.getMcfDefs().get(getNumOfMcf()).getMethods())
            {
               writeMethodSignature(out, indent, method);
               writeLeftCurlyBracket(out, indent);
               writeLogging(def, out, indent + 1, "trace", method.getMethodName());

               if (!method.getReturnType().equals("void"))
               {
                  writeEol(out);
                  if (BasicType.isPrimitiveType(method.getReturnType()))
                  {
                     writeWithIndent(out, indent + 1, "return " + BasicType.defaultValue(method.getReturnType()) + ";");
                  }
                  else
                  {
                     writeWithIndent(out, indent + 1, "return null;");
                  }
               }

               writeRightCurlyBracket(out, indent);
            }
         }
      }
      else
      {
         writeSimpleMethodSignature(out, indent, " * Call me", "void callMe()");
         writeLeftCurlyBracket(out, indent);
         writeLogging(def, out, indent + 1, "trace", "callMe");
         writeRightCurlyBracket(out, indent);
      }
   }
}
