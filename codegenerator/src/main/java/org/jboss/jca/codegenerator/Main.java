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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Code generator main class
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class Main
{
   private static final int OTHER = 2;

   /**
    * Code generator stand alone tool
    * 
    * @param args command line arguments
    */
   public static void main(String[] args)
   {
      String outputDir = "out"; //default output directory
      int arg = 0;
 
      if (args.length > 0)
      {
         while (args.length > arg + 1)
         {
            if (args[arg].startsWith("-"))
            {
               if (args[arg].equals("-o"))
               {
                  arg++;
                  if (arg >= args.length)
                  {
                     usage();
                     System.exit(OTHER);
                  }
                  outputDir = args[arg];
               }
            } 
            else
            {
               usage();
               System.exit(OTHER);
            }
            arg++;
         }
      }
      try 
      {
         File out = new File(outputDir);
         Utils.recursiveDelete(out);
         
         ResourceBundle dbconf = ResourceBundle.getBundle("codegenerator", Locale.getDefault());

         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
         Definition def = new Definition();
         
         //profile version
         String version = null;
         do
         {
            System.out.print(dbconf.getString("profile.version"));
            version = in.readLine();
            if (version == null || version.equals(""))
               version = "1.6";
         }
         while (!(version.equals("1.6") || version.equals("1.5") || version.equals("1.0")));
         def.setVersion(version);
         
         //by default, support outbound, but not inbound
         def.setSupportOutbound(true);
         def.setSupportInbound(false);
         
         //bound
         if (!version.equals("1.0"))
         {
            System.out.print(dbconf.getString("support.bound"));
            String bound = in.readLine();
            if (bound == null || bound.equals("") || bound.equals("O") || bound.equals("o") || bound.equals("Outbound"))
            {
               //keep default bound 
            }
            else if (bound.equals("I") || bound.equals("i") || bound.equals("Inbound"))
            {
               def.setSupportOutbound(false);
               def.setSupportInbound(true);
            }
            else if (bound.equals("B") || bound.equals("b") || bound.equals("Bidirectional"))
            {
               def.setSupportOutbound(true);
               def.setSupportInbound(true);
            }
         }
         
         //transaction
         if (def.isSupportOutbound())
         {
            System.out.print(dbconf.getString("support.transaction"));
            String trans = in.readLine();
            if (trans == null || trans.equals(""))
               def.setSupportTransaction("NoTransaction");
            else if (trans.equals("L") || trans.equals("l") || trans.equals("LocalTransaction"))
            {
               def.setSupportTransaction("LocalTransaction");
            }
            else if (trans.equals("X") || trans.equals("x") || trans.equals("XATransaction"))
            {
               def.setSupportTransaction("XATransaction");
            }
            else
            {
               def.setSupportTransaction("NoTransaction");
            }
         }
         
         //package name
         System.out.print(dbconf.getString("package.name"));
         String packageName = in.readLine();
         def.setRaPackage(packageName);
         
         //support annotation
         if (version.equals("1.6"))
         {
            System.out.print(dbconf.getString("use.annotation"));
            String useAnnotation = in.readLine();
            if (useAnnotation == null)
               def.setUseAnnotation(true);
            else
            {
               if (useAnnotation.equals("N") || useAnnotation.equals("n") || useAnnotation.equals("No"))
                  def.setUseAnnotation(false);
               else
                  def.setUseAnnotation(true);
            }
         }
         else
         {
            def.setUseAnnotation(false);
         }

         //use resource adapter
         if (def.isSupportOutbound() && !def.isSupportInbound() && (version.equals("1.6") || version.equals("1.5")))
         {
            System.out.print(dbconf.getString("use.ra"));
            String useRa = in.readLine();
            if (useRa == null)
               def.setUseRa(true);
            else
            {
               if (useRa.equals("N") || useRa.equals("n") || useRa.equals("No"))
                  def.setUseRa(false);
               else
                  def.setUseRa(true);
            }
         }
         else if (version.equals("1.0"))
         {
            def.setUseRa(false);
         }
         else
         {
            def.setUseRa(true);
         }
         
         //input ra class name
         if (def.isUseRa() || def.isSupportInbound())
         {
            System.out.print(dbconf.getString("ra.class.name"));
            String raClassName = in.readLine();
            def.setRaClass(raClassName);
            
            List<ConfigPropType> raProps = inputProperties("ra", dbconf, in, false);
            def.setRaConfigProps(raProps);
         }
         
         //outbound
         if (def.isSupportOutbound())
         {
            System.out.print(dbconf.getString("mcf.class.name"));
            String mcfClassName = in.readLine();
            def.setMcfClass(mcfClassName);
   
            List<ConfigPropType> mcfProps = inputProperties("mcf", dbconf, in, false);
            def.setMcfConfigProps(mcfProps);

            if (def.isUseRa())
            {
               System.out.print(dbconf.getString("mcf.impl.raa"));
               String raAssociation = in.readLine();
               if (raAssociation == null)
                  def.setImplRaAssociation(false);
               else
               {
                  if (raAssociation.equals("Y") || raAssociation.equals("y") || raAssociation.equals("Yes"))
                     def.setImplRaAssociation(true);
                  else
                     def.setImplRaAssociation(false);
               }
            }
            
            System.out.print(dbconf.getString("mc.class.name"));
            String mcClassName = in.readLine();
            def.setMcClass(mcClassName);
            
            System.out.print(dbconf.getString("mcf.use.cci"));
            String useCciConnection = in.readLine();
            if (useCciConnection == null)
               def.setUseCciConnection(false);
            else
            {
               if (useCciConnection.equals("Y") || useCciConnection.equals("y") || useCciConnection.equals("Yes"))
                  def.setUseCciConnection(true);
               else
                  def.setUseCciConnection(false);
            }
            
            if (!def.isUseCciConnection())
            {
               System.out.print(dbconf.getString("cf.interface.name"));
               String cfInterfaceName = in.readLine();
               def.setCfInterfaceClass(cfInterfaceName);
               System.out.print(dbconf.getString("cf.class.name"));
               String cfClassName = in.readLine();
               def.setCfClass(cfClassName);
   
               System.out.print(dbconf.getString("conn.interface.name"));
               String connInterfaceName = in.readLine();
               def.setConnInterfaceClass(connInterfaceName);
               System.out.print(dbconf.getString("conn.class.name"));
               String connImplName = in.readLine();
               def.setConnImplClass(connImplName);
               
               System.out.print(dbconf.getString("connection.method.support"));
               String supportMethod = in.readLine();
               if (supportMethod == null)
                  def.setDefineMethodInConnection(false);
               else
               {
                  if (supportMethod.equals("Y") || supportMethod.equals("y") || supportMethod.equals("Yes"))
                     def.setDefineMethodInConnection(true);
                  else
                     def.setDefineMethodInConnection(false);
               }
               if (def.isDefineMethodInConnection())
               {
                  def.setMethods(inputMethod(dbconf, in));
               }
            }
         }
         

         //inbound
         if (def.isSupportInbound())
         {
            System.out.print(dbconf.getString("ml.interface.name"));
            String mlClassName = in.readLine();
            def.setMlClass(mlClassName);
            System.out.print(dbconf.getString("as.class.name"));
            String asClassName = in.readLine();
            def.setAsClass(asClassName);
            List<ConfigPropType> asProps = inputProperties("as", dbconf, in, true);
            def.setAsConfigProps(asProps);
            System.out.print(dbconf.getString("acti.class.name"));
            String actiClassName = in.readLine();
            def.setActivationClass(actiClassName);
         }
         
         def.setOutputDir(outputDir);

         Profile profile;
         if (version.equals("1.6"))
         {
            profile = new JCA16Profile();
         }
         else if (version.equals("1.5"))
         {
            profile = new JCA15Profile();
         }
         else
         {
            profile = new JCA10Profile();
         }
         profile.generate(def);
         
         copyAllJars(outputDir);
         
         System.out.println(dbconf.getString("code.wrote"));
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * copy all jars
    * @param outputDir output directory
    * @throws IOException ioException
    */
   private static void copyAllJars(String outputDir) throws IOException
   {
      File out = new File(outputDir);
      String path = out.getAbsolutePath();
      String targetPath = path + File.separatorChar + "lib";

      String libPath = path + File.separatorChar + ".." + File.separatorChar + ".." + File.separatorChar + 
         ".." + File.separatorChar + "lib";
      Utils.copyFolder(libPath, targetPath, "jar");

      String binPath = path + File.separatorChar + ".." + File.separatorChar + ".." + File.separatorChar + 
         ".." + File.separatorChar + "bin";
      Utils.copyFolder(binPath, targetPath, "jar");
   }

   /**
    * Input Properties
    * @param classname belong to which java class
    * @param dbconf ResourceBundle
    * @param in BufferedReader
    * @param supportRequired need input required property
    * @return List<ConfigPropType> list of properties
    * @throws IOException ioException
    */
   private static List<ConfigPropType> inputProperties(String classname, 
      ResourceBundle dbconf, BufferedReader in, boolean supportRequired) 
      throws IOException
   {
      List<ConfigPropType> props = new ArrayList<ConfigPropType>();
      while (true)
      {
         System.out.println(dbconf.getString(classname + ".config.properties"));
         System.out.print("    " + dbconf.getString("config.properties.name"));
         String name = in.readLine();
         if (name == null || name.equals(""))
            break;
         System.out.print("    " + dbconf.getString("config.properties.type"));
         String type = in.readLine();

         if (!BasicType.isBasicType(type))
         {
            System.out.print(dbconf.getString("config.properties.type.tip") + " [");
            System.out.println(BasicType.allBasicType() + "]");
            continue;
         }
         System.out.print("    " + dbconf.getString("config.properties.value"));
         String value = in.readLine();
         boolean required = false;
         if (supportRequired)
         {
            System.out.print("    " + dbconf.getString("config.properties.required"));
            String propRequired = in.readLine();
            if (propRequired == null)
               required = false;
            else
            {
               if (propRequired.equals("Y") || propRequired.equals("y") || propRequired.equals("Yes"))
                  required = true;
               else
                  required = false;
            }
         }
         System.out.println();
         
         ConfigPropType config = new ConfigPropType(name, type, value, required);
         props.add(config);
      }
      return props;
   }
   
   /**
    * Input Methods
    * @param dbconf ResourceBundle
    * @param in BufferedReader
    * @return List<MethodForConnection> list of properties
    * @throws IOException ioException
    */
   private static List<MethodForConnection> inputMethod(ResourceBundle dbconf, BufferedReader in) 
      throws IOException
   {
      List<MethodForConnection> methods = new ArrayList<MethodForConnection>();
      while (true)
      {
         System.out.print("    " + dbconf.getString("connection.method.name"));
         String methodName = in.readLine();
         if (methodName == null || methodName.equals(""))
            break;
         MethodForConnection method = new MethodForConnection();
         method.setMethodName(methodName);
         System.out.print("    " + dbconf.getString("connection.method.return"));
         String methodReturn = in.readLine();
         if (!(methodReturn == null || methodReturn.equals("")))
            method.setReturnType(methodReturn);
         while (true)
         {
            System.out.print("    " + dbconf.getString("connection.method.param.name"));
            String paramName = in.readLine();
            if (paramName == null || paramName.equals(""))
               break;
            String paramType = null;
            while (true)
            {
               System.out.print("    " + dbconf.getString("connection.method.param.type"));
               paramType = in.readLine();
               if (BasicType.isBasicType(paramType) || BasicType.isPrimitiveType(paramType))
                  break;
               System.out.print(dbconf.getString("config.properties.type.tip") + " [");
               System.out.println(BasicType.allType() + "]");
            }
            
            MethodParam param = method.newParam(paramName, paramType);
            method.getParams().add(param);
         }
         
         while (true)
         {
            System.out.print("    " + dbconf.getString("connection.method.exception"));
            String exceptions = in.readLine();
            if (exceptions == null || exceptions.equals(""))
               break;
            method.getExceptionType().add(exceptions);
         }
         methods.add(method);
      }
      
      return methods;
   
   }

   /**
    * Tool usage
    */
   private static void usage()
   {
      System.out.println("Usage: codegenerator [-o directory]");
   }

}
