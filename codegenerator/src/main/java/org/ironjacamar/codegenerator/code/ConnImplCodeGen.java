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
 * A connection impl class CodeGen.
 *
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class ConnImplCodeGen extends AbstractCodeGen
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

      out.write("public class " + getClassName(def) + " implements " +
            def.getMcfDefs().get(getNumOfMcf()).getConnInterfaceClass());
      writeLeftCurlyBracket(out, 0);
      int indent = 1;
      writeWithIndent(out, indent, "/** The logger */\n");
      writeWithIndent(out, indent, "private static Logger log = Logger.getLogger(" + getSelfClassName(def) + ");\n\n");

      writeWithIndent(out, indent, "/** ManagedConnection */\n");
      writeWithIndent(out, indent, "private " + def.getMcfDefs().get(getNumOfMcf()).getMcClass() + " mc;\n\n");

      writeWithIndent(out, indent, "/** ManagedConnectionFactory */\n");
      writeWithIndent(out, indent, "private " + def.getMcfDefs().get(getNumOfMcf()).getMcfClass() + " mcf;\n\n");

      //constructor
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Default constructor\n");
      writeWithIndent(out, indent, " * @param mc " + def.getMcfDefs().get(getNumOfMcf()).getMcClass());
      writeEol(out);
      writeWithIndent(out, indent, " * @param mcf " + def.getMcfDefs().get(getNumOfMcf()).getMcfClass());
      writeEol(out);
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public " + getClassName(def) + "(" +
            def.getMcfDefs().get(getNumOfMcf()).getMcClass() + " mc, " +
            def.getMcfDefs().get(getNumOfMcf()).getMcfClass() + " mcf)");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "this.mc = mc;\n");
      writeWithIndent(out, indent + 1, "this.mcf = mcf;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

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
      out.write("package " + def.getRaPackage() + ";");

      writeEol(out);
      writeEol(out);
      importLogging(def, out);
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
      return def.getMcfDefs().get(getNumOfMcf()).getConnImplClass();
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
               writeIndent(out, indent + 1);

               if (!method.getReturnType().equals("void"))
               {
                  out.write("return ");
               }
               out.write("mc." + method.getMethodName() + "(");
               int paramSize = method.getParams().size();
               for (int i = 0; i < paramSize; i++)
               {
                  MethodParam param = method.getParams().get(i);
                  out.write(param.getName());
                  if (i + 1 < paramSize)
                     out.write(", ");
               }
               out.write(");");

               writeRightCurlyBracket(out, indent);
               writeEol(out);
            }
         }
      }
      else
      {
         writeSimpleMethodSignature(out, indent, " * Call me", "public void callMe()");
         writeLeftCurlyBracket(out, indent);
         writeWithIndent(out, indent + 1, "if (mc != null)\n");
         writeWithIndent(out, indent + 2, "mc.callMe();");
         writeRightCurlyBracket(out, indent);
      }

      writeEol(out);
      writeSimpleMethodSignature(out, indent, " * Close", "public void close()");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "if (mc != null)\n");
      writeLeftCurlyBracket(out, indent + 1);
      writeWithIndent(out, indent + 2, "mc.closeHandle(this);\n");
      writeIndent(out, indent + 2);
      out.write("mc = null;");
      writeRightCurlyBracket(out, indent + 1);
      writeRightCurlyBracket(out, indent);

      writeEol(out);
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Set ManagedConnection\n");
      writeWithIndent(out, indent, " */\n");
      writeWithIndent(out, indent,
            "void setManagedConnection(" + def.getMcfDefs().get(getNumOfMcf()).getMcClass() + " mc)");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "this.mc = mc;");
      writeRightCurlyBracket(out, indent);
   }
}
