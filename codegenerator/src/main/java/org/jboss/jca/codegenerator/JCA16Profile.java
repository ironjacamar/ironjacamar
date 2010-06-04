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

import org.jboss.jca.codegenerator.code.AbstractCodeGen;
import org.jboss.jca.codegenerator.xml.BuildXmlGen;
import org.jboss.jca.codegenerator.xml.RaXmlGen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A JCA16Profile.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class JCA16Profile implements Profile
{

   /**
    * JCA16Profile
    */
   public JCA16Profile()
   {
   }
   
  
   /**
    * generate code
    * @param def Definition 
    * @param packageName the writer to output the text to.
    */
   @Override
   public void generate(Definition def, String packageName)
   {
      generateClassCode(def, "Ra");
      generateClassCode(def, "Mcf");
      generateClassCode(def, "Mc");
      generateClassCode(def, "McMeta");
      generateClassCode(def, "Cm");

      if (!def.isUseCciConnection())
      {
         generateClassCode(def, "CfInterface");
         generateClassCode(def, "Cf");
         generateClassCode(def, "ConnInterface");
         generateClassCode(def, "ConnImpl");
      }
      else
      {
         generateClassCode(def, "CciConn");
         generateClassCode(def, "CciConnFactory");
         generateClassCode(def, "ConnMeta");
         generateClassCode(def, "RaMeta");
         generateClassCode(def, "ConnSpec");
      }
      
      if (def.isSupportInbound())
      {
         generateClassCode(def, "Ml");
         generateClassCode(def, "As");
      }
      
      generateAntXml(def.getOutputDir());
      
      if (!def.isUseAnnotation())
         generateRaXml(def, def.getOutputDir());
   }

   /**
    * generate ResourceAdapater code
    * @param def Definition 
    * @param packageName the writer to output the text to.
    */
   private void generateClassCode(Definition def, String className)
   {
      if (className == null || className.equals(""))
         return;
      
      try
      {

         String clazzName = this.getClass().getPackage().getName() + ".code." + className + "CodeGen";
         String javaFile = (String)Definition.class.getMethod(
            "get" + className + "Class").invoke(def, (Object[])null) + ".java";
         FileWriter fw = Utils.createSrcFile(javaFile, def.getRaPackage(), def.getOutputDir());

         Class<?> clazz = Class.forName(clazzName, true, Thread.currentThread().getContextClassLoader());
         AbstractCodeGen codeGen = (AbstractCodeGen)clazz.newInstance();
         
         codeGen.generate(def, fw);
         
         fw.flush();
         fw.close();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * generate ant build.xml
    * @param outputDir output directory
    */
   private void generateAntXml(String outputDir)
   {
      try
      {
         //ant build.xml
         FileWriter antfw = Utils.createFile("build.xml", outputDir);
         BuildXmlGen bxGen = new BuildXmlGen();
         bxGen.generate(null, antfw);
         antfw.close();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }

   /**
    * generate ra.xml
    * @param def Definition
    * @param outputDir output directory
    */
   private void generateRaXml(Definition def, String outputDir)
   {
      try
      {
         FileWriter rafw = Utils.createFile("ra.xml", outputDir + File.separatorChar + "META-INF");
         RaXmlGen raGen = new RaXmlGen();
         raGen.generate(def, rafw);
         rafw.close();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }
}
