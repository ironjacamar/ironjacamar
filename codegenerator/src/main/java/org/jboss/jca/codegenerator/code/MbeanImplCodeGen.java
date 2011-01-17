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
import org.jboss.jca.codegenerator.MethodForConnection;
import org.jboss.jca.codegenerator.MethodParam;

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
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeConstructor(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Default constructor");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      //constructor
      writeIndent(out, indent);
      out.write("public " + getClassName(def) + "()");
      writeLeftCurlyBracket(out, indent);
      
      writeIndent(out, indent + 1);
      out.write("this.mbeanServer = null;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("this.objectName = \"" + def.getDefaultValue() + ",class=HelloWorld\";");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("this.registered = false;");
      writeEol(out);
      writeEol(out);
      
      writeRightCurlyBracket(out, indent);
      writeEol(out);
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
      out.write("package " + def.getRaPackage() + ".mbean;");
      writeEol(out);
      writeEol(out);
      out.write("import javax.management.MBeanServer;");
      writeEol(out);
      out.write("import javax.management.NotCompliantMBeanException;");
      writeEol(out);
      out.write("import javax.management.ObjectName;");
      writeEol(out);
      out.write("import javax.naming.InitialContext;");
      writeEol(out);
      writeEol(out);

      out.write("import " + def.getRaPackage() + "." + def.getConnInterfaceClass() + ";");
      writeEol(out);
      out.write("import " + def.getRaPackage() + "." + def.getCfInterfaceClass() + ";");
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
      return def.getMbeanImplClass();
   }
   
   /**
    * Output class vars
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeVars(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/** JNDI name */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private static final String JNDI_NAME = \"java:/eis/" + def.getDefaultValue() + "\";");
      writeEol(out);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/** MBeanServer instance */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private MBeanServer mbeanServer;");
      writeEol(out);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/** Object Name */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private String objectName;");
      writeEol(out);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/** The actual ObjectName instance */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private ObjectName on;");
      writeEol(out);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/** Registered */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private boolean registered;");
      writeEol(out);
      writeEol(out);
   }

   /**
    * Output defined methods
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeMethods(Definition def, Writer out, int indent) throws IOException
   {
      if (def.isDefineMethodInConnection())
      {
         if (def.getMethods().size() > 0)
         {
            for (MethodForConnection method : def.getMethods())
            {
               writeIndent(out, indent);
               out.write("/**");
               writeEol(out);
               writeIndent(out, indent);
               out.write(" * Call " + method.getMethodName());
               writeEol(out);
               for (MethodParam param : method.getParams())
               {
                  writeIndent(out, indent);
                  out.write(" * @param " + param.getName() + " " + param.getName());
                  writeEol(out);
               }
               if (!method.getReturnType().equals("void"))
               {
                  writeIndent(out, indent);
                  out.write(" * @return " + method.getReturnType());
                  writeEol(out);
               }
               writeIndent(out, indent);
               out.write(" * @throws Exception");
               writeEol(out);

               writeIndent(out, indent);
               out.write(" */");
               writeEol(out);
               
               writeIndent(out, indent);
               out.write("public " + method.getReturnType() + " " +
                  method.getMethodName() + "(");
               int paramSize = method.getParams().size();
               for (int i = 0; i < paramSize; i++)
               {
                  MethodParam param = method.getParams().get(i);
                  out.write(param.getType());
                  out.write(" ");
                  out.write(param.getName());
                  if (i + 1 < paramSize)
                     out.write(", ");
               }
               out.write(") throws Exception");

               writeLeftCurlyBracket(out, indent);
               writeIndent(out, indent + 1);
               if (!method.getReturnType().equals("void"))
               {
                  out.write("return ");
               }
               out.write("getConnection()." + method.getMethodName() + "(");
               for (int i = 0; i < paramSize; i++)
               {
                  MethodParam param = method.getParams().get(i);
                  out.write(param.getName());
                  if (i + 1 < paramSize)
                     out.write(", ");
               }
               out.write(");");
               writeEol(out);

               writeRightCurlyBracket(out, indent);
            }
         }
      }
      else
      {
         writeIndent(out, indent);
         out.write("/**");
         writeEol(out);
         writeIndent(out, indent);
         out.write(" * Call me");
         writeEol(out);
         writeIndent(out, indent);
         out.write(" */");
         writeEol(out);
         writeIndent(out, indent);
         out.write("@Override");
         writeEol(out);
         writeIndent(out, indent);
         out.write("public void callMe() throws Exception");
         writeLeftCurlyBracket(out, indent);
         writeIndent(out, indent + 1);
         out.write("getConnection().callMe();");

         writeRightCurlyBracket(out, indent);
      }
   }

   /**
    * Output getConnection method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeGetConnection(Definition def, Writer out, int indent) throws IOException
   {
      String connInterface = def.getConnInterfaceClass();
      String cfInterface = def.getCfInterfaceClass();
      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * GetConnection");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return " + connInterface);
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private " + connInterface + " getConnection() throws Exception");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      
      out.write("InitialContext context = new InitialContext();");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write(cfInterface + " factory = (" + cfInterface + 
         ")context.lookup(JNDI_NAME);");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write(connInterface + " conn = factory.getConnection();");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("if (conn == null)");
      writeLeftCurlyBracket(out, indent + 1);
      writeIndent(out, indent + 2);
      out.write("throw new RuntimeException(\"No connection\");");
      writeRightCurlyBracket(out, indent + 1);

      writeIndent(out, indent + 1);
      out.write("return conn;");
      
      writeRightCurlyBracket(out, indent);
   }
   
   /**
    * Output mbean lifecycle
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeMBeanLifecycle(Definition def, Writer out, int indent) throws IOException
   {
      //setMBeanServer
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Set the MBean server");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param v The value");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public void setMBeanServer(MBeanServer v)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("mbeanServer = v;");
      writeRightCurlyBracket(out, indent);
      
      //Start
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Start");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @exception Throwable Thrown in case of an error");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public void start() throws Throwable");
      writeLeftCurlyBracket(out, indent);
      
      writeIndent(out, indent + 1);
      out.write("if (mbeanServer == null)");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("throw new IllegalArgumentException(\"MBeanServer is null\");");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("on = new ObjectName(mbeanServer.getDefaultDomain() + objectName);");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("mbeanServer.registerMBean(this, on);");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("registered = true;");
      writeRightCurlyBracket(out, indent);

      //stop
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Stop");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @exception Throwable Thrown in case of an error");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public void stop() throws Throwable");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("if (registered)");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("mbeanServer.unregisterMBean(on); ");
      writeRightCurlyBracket(out, indent);
   }
}
