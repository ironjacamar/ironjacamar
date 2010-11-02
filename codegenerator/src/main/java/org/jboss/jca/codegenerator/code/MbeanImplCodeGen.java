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

      writeIndent(out, indent);
      out.write("private static final String JNDI_NAME = \"java:/eis/" + def.getDefaultValue() + "\";");
      writeEol(out);
      writeEol(out);

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
               out.write(" * call " + method.getMethodName());
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
         out.write(" * call me");
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

      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * getConnection");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private " + def.getConnInterfaceClass() + " getConnection() throws Exception");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      
      out.write("InitialContext context = new InitialContext();");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write(def.getCfInterfaceClass() + " factory = (" + def.getCfInterfaceClass() + 
         ")context.lookup(JNDI_NAME);");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write(def.getConnInterfaceClass() + " conn = factory.getConnection();");
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
      out.write("package " + def.getRaPackage() + ".mbean;");
      writeEol(out);
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
}
