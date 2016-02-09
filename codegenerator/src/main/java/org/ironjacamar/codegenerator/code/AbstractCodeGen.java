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

import org.ironjacamar.codegenerator.BaseGen;
import org.ironjacamar.codegenerator.Definition;
import org.ironjacamar.codegenerator.MethodForConnection;
import org.ironjacamar.codegenerator.MethodParam;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * Abstract CodeGenerator.
 *
 * @author Jeff Zhang
 * @version $Revision:$
 */
public abstract class AbstractCodeGen extends BaseGen
{
   /**
    * num of mcf
    */
   private int numOfMcf = 0;

   /**
    * generate code
    *
    * @param def Definition
    * @param out Writer
    * @throws IOException ioException
    */
   public void generate(Definition def, Writer out) throws IOException
   {
      writeHeader(def, out);
      writeImport(def, out);
      writeClassComment(def, out);
      writeClassBody(def, out);
   }

   /**
    * Output class comment
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   void writeClassComment(Definition def, Writer out) throws IOException
   {
      out.write("/**\n");
      out.write(" * " + getClassName(def));
      writeEol(out);
      out.write(" *\n");
      out.write(" * @version $Revision: $\n");
      out.write(" */\n");
   }

   /**
    * get this class name
    *
    * @param def definition
    * @return String class name
    */
   public abstract String getClassName(Definition def);

   /**
    * write a simple method signature
    * @param out the writer
    * @param indent indent
    * @param javadoc javadoc strinf
    * @param signature signatore of the method
    * @throws IOException excption
    */
   protected void writeSimpleMethodSignature(Writer out, int indent, String javadoc, String signature)
         throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeIndent(out, indent);
      out.write(javadoc);
      writeEol(out);
      writeWithIndent(out, indent, " */\n");

      writeIndent(out, indent);
      out.write(signature);
   }

   /**
    * Write method signature for given @MethodForConnection
    * @param out the writer
    * @param indent indent
    * @param method method metadata
    * @throws IOException exception
    */
   protected void writeMethodSignature(Writer out, int indent, MethodForConnection method) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * " + method.getMethodName());
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
      for (String ex : method.getExceptionType())
      {
         writeIndent(out, indent);
         out.write(" * @throws " + ex + " " + ex);
         writeEol(out);
      }

      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public " + method.getReturnType() + " " +
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
      out.write(")");
      int exceptionSize = method.getExceptionType().size();
      for (int i = 0; i < exceptionSize; i++)
      {
         if (i == 0)
            out.write(" throws ");
         String ex = method.getExceptionType().get(i);
         out.write(ex);
         if (i + 1 < exceptionSize)
            out.write(", ");
      }
   }

   /**
    * Output class import
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   public abstract void writeImport(Definition def, Writer out) throws IOException;

   /**
    * Output class
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   public abstract void writeClassBody(Definition def, Writer out) throws IOException;

   /**
    * Output left curly bracket
    *
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeLeftCurlyBracket(Writer out, int indent) throws IOException
   {
      writeEol(out);
      writeWithIndent(out, indent, "{\n");
   }

   /**
    * Output right curly bracket
    *
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeRightCurlyBracket(Writer out, int indent) throws IOException
   {
      writeEol(out);
      writeWithIndent(out, indent, "}\n");
   }

   /**
    * Output Default Constructor
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeDefaultConstructor(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Default constructor\n");
      writeWithIndent(out, indent, " */\n");

      //constructor
      writeWithIndent(out, indent, "public " + getClassName(def) + "()");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Upcase first letter
    *
    * @param name string
    * @return String name string
    */
   String upcaseFirst(String name)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(name.substring(0, 1).toUpperCase(Locale.ENGLISH));
      sb.append(name.substring(1));
      return sb.toString();
   }

   /**
    * Output hashCode method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeHashCode(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/** \n");
      writeWithIndent(out, indent, " * Returns a hash code value for the object.\n");
      writeWithIndent(out, indent, " * @return A hash code value for this object.\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public int hashCode()");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "return 42;");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Output equals method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeEquals(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/** \n");
      writeWithIndent(out, indent, " * Indicates whether some other object is equal to this one.\n");
      writeWithIndent(out, indent, " * @param other The reference object with which to compare.\n");
      writeWithIndent(out, indent,
            " * @return true If this object is the same as the obj argument, false otherwise.\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "@Override\n");
      writeWithIndent(out, indent, "public boolean equals(Object other)");
      writeLeftCurlyBracket(out, indent);
      writeWithIndent(out, indent + 1, "if (other == null)\n");
      writeWithIndent(out, indent + 2, "return false;\n");
      writeWithIndent(out, indent + 1, "return getClass().equals(other.getClass());");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

   /**
    * Set the numOfMcf.
    *
    * @param numOfMcf The numOfMcf to set.
    */
   public void setNumOfMcf(int numOfMcf)
   {
      this.numOfMcf = numOfMcf;
   }

   /**
    * Get the numOfMcf.
    *
    * @return the numOfMcf.
    */
   public int getNumOfMcf()
   {
      return numOfMcf;
   }

   /**
    * get self classname
    *
    * @param def definition
    * @return classname of self
    */
   protected String getSelfClassName(Definition def)
   {
      return getClassName(def) + ".class.getName()";
   }

   /**
    * import logging
    *
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   protected void importLogging(Definition def, Writer out) throws IOException
   {
      if (def.isSupportJbossLogging())
      {
         out.write("import org.jboss.logging.Logger;");
         writeEol(out);
         writeEol(out);
      }
      else
      {
         out.write("import java.util.logging.Logger;");
         writeEol(out);
         writeEol(out);
      }
   }

   /**
    * output logging
    *
    * @param def     definition
    * @param out     Writer
    * @param indent  indent
    * @param level   logging level
    * @param content logging content
    * @param params  logging params
    * @throws IOException ioException
    */
   protected void writeLogging(Definition def, Writer out, int indent, String level, String content, String... params)
         throws IOException
   {
      writeIndent(out, indent);
      if (def.isSupportJbossLogging())
      {
         out.write("log.trace");
         int size = params.length;
         if (size > 0)
            out.write("f");
         out.write("(\"" + content + "(");
         for (int i = 0; i < size; i++)
         {
            out.write("%s");
            if (i < size - 1)
               out.write(", ");
         }
         out.write(")\"");
         for (int i = 0; i < size; i++)
         {
            out.write(", ");
            out.write(params[i]);
         }
         out.write(");");
      }
      else
      {
         out.write("log.finest(\"" + content + "()\");");
      }
      writeEol(out);
   }

   /**
    * Output LogWriter method
    *
    * @param def    definition
    * @param out    Writer
    * @param indent space number
    * @throws IOException ioException
    */
   protected void writeLogWriter(Definition def, Writer out, int indent) throws IOException
   {
      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Gets the log writer for this ManagedConnection instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent,
            " * @return Character output stream associated with this Managed-Connection instance\n");
      writeWithIndent(out, indent, " * @throws ResourceException generic exception if operation fails\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public PrintWriter getLogWriter() throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "getLogWriter");
      writeWithIndent(out, indent + 1, "return logwriter;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeWithIndent(out, indent, "/**\n");
      writeWithIndent(out, indent, " * Sets the log writer for this ManagedConnection instance.\n");
      writeWithIndent(out, indent, " *\n");
      writeWithIndent(out, indent, " * @param out Character Output stream to be associated\n");
      writeWithIndent(out, indent, " * @throws ResourceException  generic exception if operation fails\n");
      writeWithIndent(out, indent, " */\n");

      writeWithIndent(out, indent, "public void setLogWriter(PrintWriter out) throws ResourceException");
      writeLeftCurlyBracket(out, indent);
      writeLogging(def, out, indent + 1, "trace", "setLogWriter", "out");
      writeWithIndent(out, indent + 1, "logwriter = out;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
