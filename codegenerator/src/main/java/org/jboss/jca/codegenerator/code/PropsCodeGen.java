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
 * A properties code generator
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public abstract class PropsCodeGen extends AbstractCodeGen
{
   /**
    * Output Configuration Properties Declare
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeConfigPropsDeclare(Definition def, Writer out, int indent) throws IOException
   {
      if (getConfigProps(def) == null)
         return;
      
      for (int i = 0; i < getConfigProps(def).size(); i++)
      {
         writeIndent(out, indent);
         out.write("/** " + getConfigProps(def).get(i).getName() + " */");
         writeEol(out);
         
         if (def.isUseAnnotation())
         {
            writeIndent(out, indent);
            out.write("@ConfigProperty(defaultValue = \"" + getConfigProps(def).get(i).getValue() + "\")");
            writeEol(out);
         }
         writeIndent(out, indent);
         out.write("private " + 
                   getConfigProps(def).get(i).getType() +
                   " " +
                   getConfigProps(def).get(i).getName() +
                   ";");
         writeEol(out);
         writeEol(out);
      }
   }
   
   /**
    * Output Configuration Properties
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   void writeConfigProps(Definition def, Writer out, int indent) throws IOException
   {
      if (getConfigProps(def) == null)
         return;

      for (int i = 0; i < getConfigProps(def).size(); i++)
      {
         String name = getConfigProps(def).get(i).getName();
         String upcaseName = upcaseFirst(name);
         //set
         writeIndent(out, indent);
         out.write("/** ");
         writeEol(out);
         writeIndent(out, indent);
         out.write(" * Set " + name);
         writeEol(out);
         writeIndent(out, indent);
         out.write(" * @param " + name + " The value");
         writeEol(out);
         writeIndent(out, indent);
         out.write(" */");
         writeEol(out);
         
         writeIndent(out, indent);
         out.write("public void set" + 
                   upcaseName +
                   "(" +
                   getConfigProps(def).get(i).getType() +
                   " " +
                   name +
                   ")");
         writeLeftCurlyBracket(out, indent);
         writeIndent(out, indent + 1);
         out.write("this." + name + " = " + name + ";");
         writeRightCurlyBracket(out, indent);
         writeEol(out);
         
         //get
         writeIndent(out, indent);
         out.write("/** ");
         writeEol(out);
         writeIndent(out, indent);
         out.write(" * Get " + name);
         writeEol(out);
         writeIndent(out, indent);
         out.write(" * @return The value");
         writeEol(out);
         writeIndent(out, indent);
         out.write(" */");
         writeEol(out);
         writeIndent(out, indent);
         out.write("public " + 
                   getConfigProps(def).get(i).getType() +
                   " get" +
                   upcaseName +
                   "()");
         writeLeftCurlyBracket(out, indent);
         writeIndent(out, indent + 1);
         out.write("return " + name + ";");
         writeRightCurlyBracket(out, indent);
         writeEol(out);
      }
   }

   /**
    * Output hashCode method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   @Override
   void writeHashCode(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/** ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Returns a hash code value for the object.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return A hash code value for this object.");
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
      out.write("int result = 17;");
      writeEol(out);
      for (int i = 0; i < getConfigProps(def).size(); i++)
      {
         writeIndent(out, indent + 1);
         out.write("if (" + getConfigProps(def).get(i).getName() + " != null)");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("result += 31 * result + 7 * " + getConfigProps(def).get(i).getName() + ".hashCode();");
         writeEol(out);
         writeIndent(out, indent + 1);
         
         out.write("else");
         writeEol(out);
         writeIndent(out, indent + 2);
         out.write("result += 31 * result + 7;");
         writeEol(out);
      }
      writeIndent(out, indent + 1);
      out.write("return result;");
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
   @Override
   void writeEquals(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/** ");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Indicates whether some other object is equal to this one.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param other The reference object with which to compare.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return true if this object is the same as the obj argument, false otherwise.");
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
      out.write("if (other == this)");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("return true;");
      writeEol(out);
      writeIndent(out, indent + 1);
      out.write("if (!(other instanceof " + getClassName(def) + "))");
      writeEol(out);
      writeIndent(out, indent + 2);
      out.write("return false;");
      writeEol(out);
      
      writeIndent(out, indent + 1);
      out.write("boolean result = other.hashCode() == this.hashCode(); ");
      writeEol(out);

      if (getConfigProps(def).size() > 0)
      {
         writeIndent(out, indent + 1);
         out.write(getClassName(def) + " obj = (" + getClassName(def) + ")other;");
         writeEol(out);
      }

      for (int i = 0; i < getConfigProps(def).size(); i++)
      {
         writeIndent(out, indent + 1);
         out.write("if (result)");
         writeLeftCurlyBracket(out, indent + 1);
         writeIndent(out, indent + 2);
         out.write("if (" + getConfigProps(def).get(i).getName() + " == null)");
         writeEol(out);
         writeIndent(out, indent + 3);
         out.write("result = obj.get" + upcaseFirst(getConfigProps(def).get(i).getName()) + "() == null;");
         writeEol(out);
         
         writeIndent(out, indent + 2);
         out.write("else");
         writeEol(out);
         writeIndent(out, indent + 3);
         out.write("result = " + getConfigProps(def).get(i).getName() + ".equals(obj.get" + 
            upcaseFirst(getConfigProps(def).get(i).getName()) + "());");
         writeRightCurlyBracket(out, indent + 1);
      }
      writeIndent(out, indent + 1);
      out.write("return result;");
      writeRightCurlyBracket(out, indent);
      //writeEol(out);
   }

   /**
    * get list of ConfigPropType
    * @param def TODO
    * @return List<ConfigPropType> List of ConfigPropType
    */
   public abstract List<ConfigPropType> getConfigProps(Definition def);
}
