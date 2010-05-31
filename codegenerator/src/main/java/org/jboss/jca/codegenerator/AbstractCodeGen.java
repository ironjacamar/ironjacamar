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
    * generate code
    * @param def Definition 
    * @param out Writer
    * @throws IOException ioException
    */
   public void generate(Definition def, Writer out) throws IOException
   {
      writeheader(def, out);
      writeImport(def, out);
      writeClassComment(def, out);
      writeClassBody(def, out);
   }
   

   /**
    * Output class comment
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   void writeClassComment(Definition def, Writer out) throws IOException
   {
      out.write("/**");
      writeEol(out);
      out.write(" * " + getClassName(def));
      writeEol(out);
      out.write(" *");
      writeEol(out);
      out.write(" * @version $Revision: $");
      writeEol(out);
      out.write(" */");
      writeEol(out);
   }
   
   /**
    * get this class name
    * @param def definition
    * @return String class name
    */
   public abstract String getClassName(Definition def);

   /**
    * Output class import
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   public abstract void writeImport(Definition def, Writer out) throws IOException;


   /**
    * Output class
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   public abstract void writeClassBody(Definition def, Writer out) throws IOException;

   /**
    * Output left curly bracket
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeLeftCurlyBracket(Writer out, int indent) throws IOException
   {
      writeEol(out);
      writeIndent(out, indent);
      out.write("{");
      writeEol(out);
   }

   /**
    * Output right curly bracket
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeRightCurlyBracket(Writer out, int indent) throws IOException
   {
      writeEol(out);
      writeIndent(out, indent);
      out.write("}");
      writeEol(out);
   }

   /**
    * Output Default Constructor
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeDefaultConstructor(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * default constructor");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      //constructor
      writeIndent(out, indent);
      out.write("public " + getClassName(def) + "()");
      writeLeftCurlyBracket(out, indent);
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Upcase first letter
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
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeHashCode(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/** ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Returns a hash code value for the object.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return a hash code value for this object.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public int hashCode()");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("return 42;");

      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }


   /**
    * Output equals method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeEquals(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/** ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Indicates whether some other object is equal to this one.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param   other   the reference object with which to compare.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return true if this object is the same as the obj argument; false otherwise.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public boolean equals(Object other)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("if (other == null)");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("return false;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("return getClass().equals(other.getClass());");
      
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }

}
