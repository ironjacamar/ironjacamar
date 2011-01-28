/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
 * An admin object interface CodeGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class AoInterfaceCodeGen extends AbstractCodeGen
{
   /** admin object order */
   int numOfAo = 0;

   /**
    * constructor 
    */
   public AoInterfaceCodeGen()
   {
   }
   /**
    * constructor 
    * @param num admin object order
    */
   public AoInterfaceCodeGen(int num)
   {
      numOfAo = num;
   }
   
   /**
    * Get the numOfAo.
    * 
    * @return the numOfAo.
    */
   public int getNumOfAo()
   {
      return numOfAo;
   }

   /**
    * Set the numOfAo.
    * 
    * @param numOfAo The numOfAo to set.
    */
   public void setNumOfAo(int numOfAo)
   {
      this.numOfAo = numOfAo;
   }

   /**
    * Output class
    * @param def definition
    * @param out Writer
    * @throws IOException ioException
    */
   @Override
   public void writeClassBody(Definition def, Writer out) throws IOException
   {
      out.write("public interface " + getClassName(def));
      if (def.isAdminObjectImplRaAssociation())
      {
         out.write(" extends Referenceable, Serializable");
      }
      writeLeftCurlyBracket(out, 0);
      writeEol(out);

      int indent = 1;
      writeConfigProps(def, out, indent);
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
      if (def.isAdminObjectImplRaAssociation())
      {
         out.write("import java.io.Serializable;");
         writeEol(out);
         writeEol(out);
         out.write("import javax.resource.Referenceable;");
         writeEol(out);
      }
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
      return def.getAdminObjects().get(numOfAo).getAdminObjectInterface();
   }
   
   /**
    * Output validate method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeConfigProps(Definition def, Writer out, int indent) throws IOException
   {
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
                   ");");
         writeEol(out);
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
                   "();");
         writeEol(out);
         writeEol(out);
      }
   }
   
   /**
    * get list of ConfigPropType
    * @param def definition
    * @return List<ConfigPropType> List of ConfigPropType
    */
   public List<ConfigPropType> getConfigProps(Definition def)
   {
      return def.getAdminObjects().get(numOfAo).getAoConfigProps();
   }

}
