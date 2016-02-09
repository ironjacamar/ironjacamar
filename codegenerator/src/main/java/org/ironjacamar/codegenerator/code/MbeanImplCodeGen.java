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
import org.ironjacamar.codegenerator.MethodForConnection;
import org.ironjacamar.codegenerator.MethodParam;

import java.io.IOException;
import java.io.Writer;

/**
 * A mbean impl CodeGen.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class MbeanImplCodeGen extends AbstractCodeGen
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
      out.write("public class " + getClassName(def) + " implements " + def.getMbeanInterfaceClass());
      writeLeftCurlyBracket(out, 0);

      writeVars(def, out, indent);
      writeEol(out);
      writeMBeanLifecycle(def, out, indent);
      writeEol(out);
      writeMethods(def, out, indent);
      writeEol(out);
      writeGetConnection(def, out, indent);

      writeRightCurlyBracket(out, 0);
   }

   /**
    * Output Constructor
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeConstructor(Definition def, Writer out, int indent) throws IOException
   {
      writeSimpleMethodSignature(out, indent, " * Default constructor", "public " + getClassName(def) + "()");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1, "this.mbeanServer = null;\n");
      writeWithIndent(out, indent + 1, "this.objectName = \"" + def.getDefaultValue() + ",class=HelloWorld\";\n");
      writeWithIndent(out, indent + 1, "this.registered = false;\n\n");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
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
      out.write("package " + def.getRaPackage() + ".mbean;\n\n");
      out.write("import javax.management.MBeanServer;\n");
      out.write("import javax.management.ObjectName;\n");
      out.write("import javax.naming.InitialContext;\n\n");

      out.write("import " + def.getRaPackage() + "." + def.getMcfDefs().get(0).getConnInterfaceClass() + ";\n");
      out.write("import " + def.getRaPackage() + "." + def.getMcfDefs().get(0).getCfInterfaceClass() + ";\n\n");
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
      return def.getMbeanImplClass();
   }

   /**
    * Output class vars
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeVars(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/** JNDI name */\n");
      writeWithIndent(out, indent,
            "private static final String JNDI_NAME = \"java:/eis/" + def.getDefaultValue() + "\";\n\n");

      writeWithIndent(out, indent, "/** MBeanServer instance */\n");
      writeWithIndent(out, indent, "private MBeanServer mbeanServer;\n\n");

      writeWithIndent(out, indent, "/** Object Name */\n");
      writeWithIndent(out, indent, "private String objectName;\n\n");

      writeWithIndent(out, indent, "/** The actual ObjectName instance */\n");
      writeWithIndent(out, indent, "private ObjectName on;\n\n");

      writeWithIndent(out, indent, "/** Registered */\n");
      writeWithIndent(out, indent, "private boolean registered;\n\n");
   }

   /**
    * Output defined methods
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeMethods(Definition def, Writer out, int indent) throws IOException
   {
      if (def.getMcfDefs().get(0).isDefineMethodInConnection())
      {
         if (def.getMcfDefs().get(0).getMethods().size() > 0)
         {
            for (MethodForConnection method : def.getMcfDefs().get(0).getMethods())
            {
               writeMethodSignature(out, indent, method);

               writeLeftCurlyBracket(out, indent);
               writeIndent(out, indent + 1);
               if (!method.getReturnType().equals("void"))
               {
                  out.write("return ");
               }
               out.write("getConnection()." + method.getMethodName() + "(");
               int paramSize = method.getParams().size();
               for (int i = 0; i < paramSize; i++)
               {
                  MethodParam param = method.getParams().get(i);
                  out.write(param.getName());
                  if (i + 1 < paramSize)
                     out.write(", ");
               }
               out.write(");\n");

               writeRightCurlyBracket(out, indent);
            }
         }
      }
      else
      {
         writeSimpleMethodSignature(out, indent, " * Call me", "public void callMe()");
         writeLeftCurlyBracket(out, indent);
         writeWithIndent(out, indent + 1, "try");
         writeLeftCurlyBracket(out, indent + 1);
         writeWithIndent(out, indent + 2, "getConnection().callMe();");
         writeRightCurlyBracket(out, indent + 1);
         writeWithIndent(out, indent + 1, "catch (Exception e)");
         writeLeftCurlyBracket(out, indent + 1);
         writeRightCurlyBracket(out, indent + 1);
         writeRightCurlyBracket(out, indent);
      }
   }

   /**
    * Output getConnection method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeGetConnection(Definition def, Writer out, int indent) throws IOException
   {
      String connInterface = def.getMcfDefs().get(0).getConnInterfaceClass();
      String cfInterface = def.getMcfDefs().get(0).getCfInterfaceClass();

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * GetConnection\n");
      writeWithIndent(out, indent, " * @return " + connInterface);
      writeEol(out);
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent, "private " + connInterface + " getConnection() throws Exception");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "InitialContext context = new InitialContext();\n");
      writeIndent(out, indent + 1);
      out.write(cfInterface + " factory = (" + cfInterface +
            ")context.lookup(JNDI_NAME);\n");
      writeIndent(out, indent + 1);
      out.write(connInterface + " conn = factory.getConnection();\n");
      writeWithIndent(out, indent + 1, "if (conn == null)");
      writeLeftCurlyBracket(out, indent + 1);
      writeIndent(out, indent + 2);
      out.write("throw new RuntimeException(\"No connection\");");
      writeRightCurlyBracket(out, indent + 1);

      writeWithIndent(out, indent + 1, "return conn;");

      writeRightCurlyBracket(out, indent);
   }

   /**
    * Output mbean lifecycle
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeMBeanLifecycle(Definition def, Writer out, int indent) throws IOException
   {
      //setMBeanServer
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Set the MBean server\n");
      writeWithIndent(out, indent, " * @param v The value\n");
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent, "public void setMBeanServer(MBeanServer v)");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "mbeanServer = v;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      //Start
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Start\n");
      writeWithIndent(out, indent, " * @exception Throwable Thrown in case of an error\n");
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent, "public void start() throws Throwable");
      writeLeftCurlyBracket(out, indent);

      writeWithIndent(out, indent + 1, "if (mbeanServer == null)\n");
      writeIndent(out, indent + 2);
      out.write("throw new IllegalArgumentException(\"MBeanServer is null\");\n");
      writeWithIndent(out, indent + 1, "on = new ObjectName(mbeanServer.getDefaultDomain() + objectName);\n");
      writeWithIndent(out, indent + 1, "mbeanServer.registerMBean(this, on);\n");
      writeWithIndent(out, indent + 1, "registered = true;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      //stop
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Stop\n");
      writeWithIndent(out, indent, " * @exception Throwable Thrown in case of an error\n");
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent, "public void stop() throws Throwable");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "if (registered)\n");
      writeIndent(out, indent + 2);
      out.write("mbeanServer.unregisterMBean(on); ");
      writeRightCurlyBracket(out, indent);
   }
}
