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

import org.ironjacamar.codegenerator.ConfigPropType;
import org.ironjacamar.codegenerator.Definition;

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
    *
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
                  def.getMcfDefs().get(getNumOfMcf()).getCfInterfaceClass() + ".class,\n");
            writeWithIndent(out, indent,
                  " connectionFactoryImpl = " + def.getMcfDefs().get(getNumOfMcf()).getCfClass() + ".class,\n");
            writeWithIndent(out, indent,
                  " connection = " + def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass() + ".class,\n");
            writeWithIndent(out, indent,
                  " connectionImpl = " + def.getMcfDefs().get(getNumOfMcf()).getConnImplClass() + ".class)\n");
         }
         else
         {
            out.write("@ConnectionDefinition(connectionFactory = ConnectionFactory.class,\n");
            writeWithIndent(out, indent, " connectionFactoryImpl = " +
                  def.getMcfDefs().get(getNumOfMcf()).getCciConnFactoryClass() + ".class,\n");

            writeWithIndent(out, indent, " connection = Connection.class,\n");
            writeWithIndent(out, indent,
                  " connectionImpl = " + def.getMcfDefs().get(getNumOfMcf()).getCciConnClass() + ".class)\n");
         }
      }

      out.write("public class " + getClassName(def) + " implements ManagedConnectionFactory");
      if (def.getMcfDefs().get(getNumOfMcf()).isImplRaAssociation())
      {
         out.write(", ResourceAdapterAssociation");
      }
      writeLeftCurlyBracket(out, 0);
      writeEol(out);

      writeWithIndent(out, indent, "/** The serial version UID */\n");
      writeWithIndent(out, indent, "private static final long serialVersionUID = 1L;\n\n");

      writeWithIndent(out, indent, "/** The logger */\n");
      writeWithIndent(out, indent, "private static Logger log = Logger.getLogger(" + getSelfClassName(def) + ");\n\n");
      if (def.getMcfDefs().get(getNumOfMcf()).isImplRaAssociation())
      {
         writeWithIndent(out, indent, "/** The resource adapter */\n");
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

      writeWithIndent(out, indent, "/** The logwriter */\n");
      writeWithIndent(out, indent, "private PrintWriter logwriter;\n\n");

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
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeImport(Definition def, Writer out) throws IOException
   {
      out.write("package " + def.getRaPackage() + ";\n\n");
      out.write("import java.io.PrintWriter;\n");
      out.write("import java.util.Iterator;\n");
      out.write("import java.util.Set;\n\n");
      importLogging(def, out);

      out.write("import javax.resource.ResourceException;\n");

      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
      {
         out.write("import javax.resource.cci.Connection;\n");
         out.write("import javax.resource.cci.ConnectionFactory;\n");
      }

      if (def.isUseAnnotation())
      {
         importConfigProperty(def, out);
         out.write("import javax.resource.spi.ConnectionDefinition;\n");
      }
      out.write("import javax.resource.spi.ConnectionManager;\n");
      out.write("import javax.resource.spi.ConnectionRequestInfo;\n");
      out.write("import javax.resource.spi.ManagedConnection;\n");
      out.write("import javax.resource.spi.ManagedConnectionFactory;\n");
      out.write("import javax.resource.spi.ResourceAdapter;\n");
      out.write("import javax.resource.spi.ResourceAdapterAssociation;\n\n");
      out.write("import javax.security.auth.Subject;\n\n");
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
      return def.getMcfDefs().get(getNumOfMcf()).getMcfClass();
   }

   /**
    * get list of ConfigPropType
    *
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
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeConnectionFactory(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Creates a Connection Factory instance. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param cxManager ConnectionManager to be "
            + "associated with created EIS connection factory instance\n");
      writeWithIndent(out, indent, " * @return EIS-specific Connection Factory instance or "
            + "javax.resource.cci.ConnectionFactory instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException Generic exception\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent,
            "public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "createConnectionFactory", "cxManager");
      writeIndent(out, indent + 1);
      if (def.getMcfDefs().get(getNumOfMcf()).isUseCciConnection())
         out.write("return new " + def.getMcfDefs().get(getNumOfMcf()).getCciConnFactoryClass() + "(cxManager);");
      else
         out.write("return new " + def.getMcfDefs().get(getNumOfMcf()).getCfClass() + "(this, cxManager);");

      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Creates a Connection Factory instance. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return EIS-specific Connection Factory instance or "
            + "javax.resource.cci.ConnectionFactory instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException Generic exception\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public Object createConnectionFactory() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1,
            "throw new ResourceException(\"This resource adapter doesn't support non-managed environments\");");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output ConnectionFactory method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeManagedConnection(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Creates a new physical connection to the underlying EIS resource manager.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param subject Caller's security information\n");
      writeWithIndent(out, indent,
            " * @param cxRequestInfo Additional resource adapter " + "specific connection request information\n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception\n");
      writeWithIndent(out, indent, " * @return ManagedConnection instance \n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public ManagedConnection createManagedConnection(Subject subject,\n");
      writeIndent(out, indent + 2);
      out.write("ConnectionRequestInfo cxRequestInfo) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "createManagedConnection", "subject", "cxRequestInfo");
      writeWithIndent(out, indent + 1, "return new " + def.getMcfDefs().get(getNumOfMcf()).getMcClass() + "(this);");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Returns a matched connection from the candidate set of connections. \n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param connectionSet Candidate connection set\n");
      writeWithIndent(out, indent, " * @param subject Caller's security information\n");
      writeWithIndent(out, indent,
            " * @param cxRequestInfo Additional resource adapter " + "specific connection request information\n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception\n");
      writeWithIndent(out, indent,
            " * @return ManagedConnection if resource adapter finds an acceptable match otherwise null \n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public ManagedConnection matchManagedConnections(Set connectionSet,\n");
      writeIndent(out, indent + 2);
      out.write("Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "matchManagedConnections", "connectionSet", "subject",
            "cxRequestInfo");
      writeWithIndent(out, indent + 1, "ManagedConnection result = null;\n");
      writeWithIndent(out, indent + 1, "Iterator it = connectionSet.iterator();\n");
      writeWithIndent(out, indent + 1, "while (result == null && it.hasNext())");
      writeLeftCurlyBracket(out, indent + 1);
      writeIndent(out, indent + 2);
      out.write("ManagedConnection mc = (ManagedConnection)it.next();\n");
      writeIndent(out, indent + 2);
      out.write("if (mc instanceof " + def.getMcfDefs().get(getNumOfMcf()).getMcClass() + ")");
      writeLeftCurlyBracket(out, indent + 2);
      writeIndent(out, indent + 3);
      out.write("result = mc;");
      writeRightCurlyBracket(out, indent + 2);
      writeRightCurlyBracket(out, indent + 1);
      writeWithIndent(out, indent + 1, "return result;");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output ResourceAdapter method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeResourceAdapter(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Get the resource adapter\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @return The handle\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public ResourceAdapter getResourceAdapter()");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getResourceAdapter");
      writeWithIndent(out, indent + 1, "return ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Set the resource adapter\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param ra The handle\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void setResourceAdapter(ResourceAdapter ra)");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "setResourceAdapter", "ra");
      writeWithIndent(out, indent + 1, "this.ra = ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
