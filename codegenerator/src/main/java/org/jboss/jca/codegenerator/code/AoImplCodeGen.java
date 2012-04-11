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
 * An admin object class CodeGen.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class AoImplCodeGen extends PropsCodeGen
{
   /** admin object order */
   int numOfAo = 0;

   /**
    * constructor 
    */
   public AoImplCodeGen()
   {
   }
   /**
    * constructor 
    * @param num admin object order
    */
   public AoImplCodeGen(int num)
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
      if (def.isUseAnnotation())
      {
         out.write("@AdministeredObject(adminObjectInterfaces = { ");
         out.write(def.getAdminObjects().get(numOfAo).getAdminObjectInterface());
         out.write(".class })");
         writeEol(out);
      }
      out.write("public class " + getClassName(def) + " implements " + 
         def.getAdminObjects().get(numOfAo).getAdminObjectInterface());
      if (def.isAdminObjectImplRaAssociation())
      {
         out.write(",");
         writeEol(out);
         writeIndent(out, 1);
         out.write("ResourceAdapterAssociation, Referenceable, Serializable");
      }
      writeLeftCurlyBracket(out, 0);
      int indent = 1;

      writeIndent(out, indent);
      out.write("/** Serial version uid */");
      writeEol(out);
      writeIndent(out, indent);
      out.write("private static final long serialVersionUID = 1L;");
      writeEol(out);
      writeEol(out);
      
      if (def.isAdminObjectImplRaAssociation())
      {
         writeIndent(out, indent);
         out.write("/** The resource adapter */");
         writeEol(out);
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
         
         writeIndent(out, indent);
         out.write("/** Reference */");
         writeEol(out);
         writeIndent(out, indent);
         out.write("private Reference reference;");
         writeEol(out);
         writeEol(out);
      }
      writeConfigPropsDeclare(def, out, indent);
      
      writeDefaultConstructor(def, out, indent);
      
      writeConfigProps(def, out, indent);

      if (def.isAdminObjectImplRaAssociation())
      {
         writeResourceAdapter(def, out, indent);
         writeReference(def, out, indent);
      }
      writeHashCode(def, out, indent);
      writeEquals(def, out, indent);

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
         out.write("import javax.naming.NamingException;");
         writeEol(out);
         out.write("import javax.naming.Reference;");
         writeEol(out);
         writeEol(out);
         out.write("import javax.resource.Referenceable;");
         writeEol(out);
      }
      if (def.isUseAnnotation())
      {
         out.write("import javax.resource.spi.AdministeredObject;");
         writeEol(out);
         out.write("import javax.resource.spi.ConfigProperty;");
         writeEol(out);
      }
      if (def.isAdminObjectImplRaAssociation())
      {
         out.write("import javax.resource.spi.ResourceAdapter;");
         writeEol(out);
         out.write("import javax.resource.spi.ResourceAdapterAssociation;");
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
      return def.getAdminObjects().get(numOfAo).getAdminObjectClass();
   }
   
   /**
    * get list of ConfigPropType
    * @param def definition
    * @return List<ConfigPropType> List of ConfigPropType
    */
   @Override
   public List<ConfigPropType> getConfigProps(Definition def)
   {
      return def.getAdminObjects().get(numOfAo).getAoConfigProps();
   }

   /**
    * Output ResourceAdapter method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeResourceAdapter(Definition def, Writer out, int indent) throws IOException
   {      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Get the resource adapter");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return The handle");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public ResourceAdapter getResourceAdapter()");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("return ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Set the resource adapter");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param ra The handle");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("public void setResourceAdapter(ResourceAdapter ra)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("this.ra = ra;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
   
   /**
    * Output Reference method
    * @param def definition
    * @param out Writer
    * @param indent space number
    * @throws IOException ioException
    */
   private void writeReference(Definition def, Writer out, int indent) throws IOException
   {
      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Get the Reference instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @return Reference instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @exception NamingException Thrown if a reference can't be obtained");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public Reference getReference() throws NamingException");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("return reference;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);

      writeIndent(out, indent);
      out.write("/**");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * Set the Reference instance.");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" *");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" * @param reference A Reference instance");
      writeEol(out);
      writeIndent(out, indent);
      out.write(" */");
      writeEol(out);
      
      writeIndent(out, indent);
      out.write("@Override");
      writeEol(out);
      writeIndent(out, indent);
      out.write("public void setReference(Reference reference)");
      writeLeftCurlyBracket(out, indent);
      writeIndent(out, indent + 1);
      out.write("this.reference = reference;");
      writeRightCurlyBracket(out, indent);
      writeEol(out);
   }
}
