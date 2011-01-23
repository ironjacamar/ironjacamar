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
         out.write("@AdministeredObject");
         writeEol(out);
      }
      out.write("public class " + getClassName(def) + " implements " + 
         def.getAdminObjects().get(numOfAo).getAdminObjectInterface());
      writeLeftCurlyBracket(out, 0);
      writeEol(out);

      int indent = 1;

      writeDefaultConstructor(def, out, indent);
      
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
      if (def.isUseAnnotation())
      {
         out.write("import javax.resource.spi.AdministeredObject;");
         writeEol(out);
         out.write("import javax.resource.spi.ConfigProperty;");
         writeEol(out);
         out.write("import javax.resource.spi.Connector;");
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

}
