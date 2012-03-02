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
import org.jboss.jca.codegenerator.code.AoImplCodeGen;
import org.jboss.jca.codegenerator.code.AoInterfaceCodeGen;
import org.jboss.jca.codegenerator.xml.BuildIvyXmlGen;
import org.jboss.jca.codegenerator.xml.BuildXmlGen;
import org.jboss.jca.codegenerator.xml.IronjacamarXmlGen;
import org.jboss.jca.codegenerator.xml.IvySettingsXmlGen;
import org.jboss.jca.codegenerator.xml.IvyXmlGen;
import org.jboss.jca.codegenerator.xml.MbeanXmlGen;
import org.jboss.jca.codegenerator.xml.PackageHtmlGen;
import org.jboss.jca.codegenerator.xml.PomXmlGen;
import org.jboss.jca.codegenerator.xml.RaXmlGen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

/**
 * A BaseProfile.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class BaseProfile implements Profile
{

   /**
    * BaseProfile
    */
   public BaseProfile()
   {
   }
   
  
   /**
    * generate code
    * @param def Definition 
    */
   @Override
   public void generate(Definition def)
   {
      generatePackageHtml(def, def.getOutputDir(), null);
      
      generateRaCode(def);
      generateOutboundCode(def);
      generateInboundCode(def);
      
      generateTestCode(def);

      if (def.getBuild().equals("ivy"))
         generateAntIvyXml(def, def.getOutputDir());
      else if (def.getBuild().equals("maven"))
         generateMavenXml(def, def.getOutputDir());
      else
         generateAntXml(def, def.getOutputDir());

      generateRaXml(def, def.getOutputDir());
      if (def.isSupportOutbound())
         generateIronjacamarXml(def, def.getOutputDir());
      
      if (def.isGenMbean() &&
         def.isSupportOutbound() &&
         !def.getMcfDefs().get(0).isUseCciConnection())
      {
         generateMBeanCode(def);
         generateMbeanXml(def, def.getOutputDir());
      }
   }

   /**
    * generate resource adapter code
    * 
    * @param def Definition 
    */
   void generateRaCode(Definition def)
   {
      if (def.isUseRa())
      {
         generateClassCode(def, "Ra");
         generateClassCode(def, "RaMeta");
      }
      if (def.isGenAdminObject())
      {
         for (int i = 0; i < def.getAdminObjects().size(); i++)
         {
            generateMultiAdminObjectClassCode(def, "AoImpl", i);
            generateMultiAdminObjectClassCode(def, "AoInterface", i);
         }
      }
   }

   /**
    * generate outbound code
    * 
    * @param def Definition 
    */
   void generateOutboundCode(Definition def)
   {
      if (def.isSupportOutbound())
      {
         if (def.getMcfDefs() == null)
            throw new IllegalStateException("Should define at least one mcf class");
         
         for (int num = 0; num < def.getMcfDefs().size(); num++)
         {
            generateMultiMcfClassCode(def, "Mcf", num);
            generateMultiMcfClassCode(def, "Mc", num);
            generateMultiMcfClassCode(def, "McMeta", num);
      
            if (!def.getMcfDefs().get(num).isUseCciConnection())
            {
               generateMultiMcfClassCode(def, "CfInterface", num);
               generateMultiMcfClassCode(def, "Cf", num);
               generateMultiMcfClassCode(def, "ConnInterface", num);
               generateMultiMcfClassCode(def, "ConnImpl", num);
            }
            else
            {
               generateMultiMcfClassCode(def, "CciConn", num);
               generateMultiMcfClassCode(def, "CciConnFactory", num);
               generateMultiMcfClassCode(def, "ConnMeta", num);
               generateMultiMcfClassCode(def, "ConnSpec", num);
            }
         }
      }
   }

   /**
    * generate inbound code
    * 
    * @param def Definition 
    */
   void generateInboundCode(Definition def)
   {
      if (def.isSupportInbound())
      {
         if (def.isDefaultPackageInbound())
            generateClassCode(def, "Ml", "inflow");
         generateClassCode(def, "As", "inflow");
         generateClassCode(def, "Activation", "inflow");
         generatePackageHtml(def, def.getOutputDir(), "inflow");
      }
   }

   /**
    * generate MBean code
    * 
    * @param def Definition 
    */
   void generateMBeanCode(Definition def)
   {
      if (def.isSupportOutbound())
      {
         generateClassCode(def, "MbeanInterface", "mbean");
         generateClassCode(def, "MbeanImpl", "mbean");
         generatePackageHtml(def, def.getOutputDir(), "mbean");
      }
   }
   
   /**
    * generate class code
    * @param def Definition 
    * @param className class name 
    */
   void generateClassCode(Definition def, String className)
   {
      generateClassCode(def, className, null);
   }

   /**
    * generate class code
    * @param def Definition 
    * @param className class name 
    * @param subDir sub-directory name
    */
   void generateClassCode(Definition def, String className, String subDir)
   {
      if (className == null || className.equals(""))
         return;
      
      try
      {
         String clazzName = this.getClass().getPackage().getName() + ".code." + className + "CodeGen";
         String javaFile = (String)Definition.class.getMethod(
            "get" + className + "Class").invoke(def, (Object[])null) + ".java";
         FileWriter fw = null;
         if (subDir == null)
            fw = Utils.createSrcFile(javaFile, def.getRaPackage(), def.getOutputDir());
         else
            fw = Utils.createSrcFile(javaFile, def.getRaPackage() + "." + subDir, def.getOutputDir());

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
    * generate multi mcf class code
    * @param def Definition 
    * @param className class name 
    * @param num number of order
    */
   void generateMultiMcfClassCode(Definition def, String className, int num)
   {
      if (className == null || className.equals(""))
         return;
      if (num < 0 || num + 1 > def.getMcfDefs().size())
         return;
      try
      {

         String clazzName = this.getClass().getPackage().getName() + ".code." + className + "CodeGen";

         String javaFile = (String)McfDef.class.getMethod(
            "get" + className + "Class").invoke(def.getMcfDefs().get(num), (Object[])null) + ".java";
         FileWriter fw = null;
         fw = Utils.createSrcFile(javaFile, def.getRaPackage(), def.getOutputDir());
         
         Class<?> clazz = Class.forName(clazzName, true, Thread.currentThread().getContextClassLoader());
         AbstractCodeGen codeGen = (AbstractCodeGen)clazz.newInstance();
         codeGen.setNumOfMcf(num);
         
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
    * generate multi admin object class code
    * @param def Definition 
    * @param className class name 
    * @param num number of order
    */
   void generateMultiAdminObjectClassCode(Definition def, String className, int num)
   {
      if (className == null || className.equals(""))
         return;
      
      try
      {
         String clazzName = this.getClass().getPackage().getName() + ".code." + className + "CodeGen";

         Class<?> clazz = Class.forName(clazzName, true, Thread.currentThread().getContextClassLoader());
         AbstractCodeGen codeGen = (AbstractCodeGen)clazz.newInstance();
         
         String javaFile = "";
         if (codeGen instanceof AoImplCodeGen)
         {
            ((AoImplCodeGen)codeGen).setNumOfAo(num);
            javaFile = def.getAdminObjects().get(num).getAdminObjectClass() + ".java";
         }
         else if (codeGen instanceof AoInterfaceCodeGen)
         {
            ((AoInterfaceCodeGen)codeGen).setNumOfAo(num);
            javaFile = def.getAdminObjects().get(num).getAdminObjectInterface() + ".java";
         }
         
         FileWriter fw = Utils.createSrcFile(javaFile, def.getRaPackage(), def.getOutputDir());
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
    * @param def Definition
    * @param outputDir output directory
    */
   void generateAntXml(Definition def, String outputDir)
   {
      try
      {
         FileWriter antfw = Utils.createFile("build.xml", outputDir);
         BuildXmlGen bxGen = new BuildXmlGen();
         bxGen.generate(def, antfw);
         antfw.close();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }

   /**
    * generate ant + ivy build.xml and ivy files
    * @param def Definition
    * @param outputDir output directory
    */
   void generateAntIvyXml(Definition def, String outputDir)
   {
      try
      {
         FileWriter antfw = Utils.createFile("build.xml", outputDir);
         BuildIvyXmlGen bxGen = new BuildIvyXmlGen();
         bxGen.generate(def, antfw);
         antfw.close();
         
         FileWriter ivyfw = Utils.createFile("ivy.xml", outputDir);
         IvyXmlGen ixGen = new IvyXmlGen();
         ixGen.generate(def, ivyfw);
         ivyfw.close();
         
         FileWriter ivySettingsfw = Utils.createFile("ivy.settings.xml", outputDir);
         IvySettingsXmlGen isxGen = new IvySettingsXmlGen();
         isxGen.generate(def, ivySettingsfw);
         ivySettingsfw.close();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }
   
   /**
    * generate ant build.xml
    * @param def Definition
    * @param outputDir output directory
    */
   void generateMavenXml(Definition def, String outputDir)
   {
      try
      {
         FileWriter pomfw = Utils.createFile("pom.xml", outputDir);
         PomXmlGen pxGen = new PomXmlGen();
         pxGen.generate(def, pomfw);
         pomfw.close();
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
   void generateRaXml(Definition def, String outputDir)
   {
      if (!def.isUseAnnotation())
      {
         try
         {
            outputDir = outputDir + File.separatorChar + "src" + File.separatorChar + 
               "main" + File.separatorChar + "resources";
            FileWriter rafw = Utils.createFile("ra.xml", outputDir + File.separatorChar + "META-INF");
            RaXmlGen raGen = getRaXmlGen(def);
            raGen.generate(def, rafw);
            rafw.close();
         }
         catch (IOException ioe)
         {
            ioe.printStackTrace();
         }
      }
   }
   
   /**
    * get right profile ra xmlGen
    * @param def Definition
    * @return RaXmlGen profile ra xmlGen
    */
   RaXmlGen getRaXmlGen(Definition def)
   {
      return null;
   }
   
   /**
    * generate ant ironjacamar.xml
    * @param def Definition
    * @param outputDir output directory
    */
   void generateIronjacamarXml(Definition def, String outputDir)
   {
      try
      {
         String resourceDir = outputDir + File.separatorChar + "src" + File.separatorChar + 
            "main" + File.separatorChar + "resources";
         writeIronjacamarXml(def, resourceDir);

         if (def.getBuild().equals("maven"))
         {
            String rarDir = outputDir + File.separatorChar + "src" + File.separatorChar + 
               "main" + File.separatorChar + "rar";
            writeIronjacamarXml(def, rarDir);
         }
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }


   /**
    * writeIronjacamarXml
    * @param def Definition
    * @param outputDir output directory
    * @throws IOException output exception
    */
   private void writeIronjacamarXml(Definition def, String outputDir) throws IOException
   {
      FileWriter ijfw = Utils.createFile("ironjacamar.xml", outputDir + File.separatorChar + "META-INF");
      IronjacamarXmlGen ijxGen = new IronjacamarXmlGen();
      ijxGen.generate(def, ijfw);
      ijfw.close();
   }
   
   /**
    * generate mbean deployment xml
    * @param def Definition
    * @param outputDir output directory
    */
   void generateMbeanXml(Definition def, String outputDir)
   {
      String mbeanName = def.getDefaultValue().toLowerCase(Locale.US);
      if (def.getRaPackage() != null && !def.getRaPackage().equals(""))
      {
         if (def.getRaPackage().indexOf('.') >= 0)
         {
            mbeanName = def.getRaPackage().substring(def.getRaPackage().lastIndexOf('.') + 1);
         }
         else
            mbeanName = def.getRaPackage();
      }
      
      try
      {
         outputDir = outputDir + File.separatorChar + "src" + File.separatorChar + 
            "main" + File.separatorChar + "resources";
         FileWriter mbfw = Utils.createFile(mbeanName + ".xml", outputDir);
         MbeanXmlGen mbGen = new MbeanXmlGen();
         mbGen.generate(def, mbfw);
         mbfw.close();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }
   
   /**
    * generate package.html
    * @param def Definition
    * @param outputDir output directory
    * @param subDir sub-directory
    */
   void generatePackageHtml(Definition def, String outputDir, String subDir)
   {
      try
      {
         FileWriter fw = null;
         if (subDir == null)
            fw = Utils.createSrcFile("package.html", def.getRaPackage(), def.getOutputDir());
         else
            fw = Utils.createSrcFile("package.html", def.getRaPackage() + "." + subDir, def.getOutputDir());
         PackageHtmlGen phGen = new PackageHtmlGen();
         phGen.generate(def, fw);
         fw.close();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
   }
   
   /**
    * generate test code
    * 
    * @param def Definition 
    */
   void generateTestCode(Definition def)
   {
      if (!def.isSupportOutbound())
         return;
      
      try
      {
         String clazzName = this.getClass().getPackage().getName() + ".code.TestCodeGen";
         String javaFile = "ConnectorTestCase.java";
         FileWriter fw = Utils.createTestFile(javaFile, def.getRaPackage(), def.getOutputDir());

         Class<?> clazz = Class.forName(clazzName, true, Thread.currentThread().getContextClassLoader());
         AbstractCodeGen codeGen = (AbstractCodeGen)clazz.newInstance();
         
         codeGen.generate(def, fw);
         
         fw.flush();
         fw.close();
         
         copyTestResourceFiles(def.getOutputDir(), "logging.properties");
         copyTestResourceFiles(def.getOutputDir(), "jndi.properties");
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
    * copy some test resource files
    * 
    * @param outputDir output directory
    * @param filename filename
    * @throws IOException ioException
    */
   private void copyTestResourceFiles(String outputDir, String filename) throws IOException
   {
      String testResourceDir = outputDir + "/src/test/resources";
      FileWriter fw = Utils.createFile(filename, testResourceDir);
      URL buildFile = BaseProfile.class.getResource("/" + filename + ".template");
      String buildString = Utils.readFileIntoString(buildFile);
      fw.write(buildString);
      fw.close();
   }

}
