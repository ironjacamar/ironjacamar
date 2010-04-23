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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Code generator main class
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class Main
{

   /**
    * Code generator standalone tool
    * 
    * @param args command line arguments
    */
   public static void main(String[] args)
   {
      try 
      {
         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
         System.out.print("Please input ResourceAdapter package name: ");
         String packageName = in.readLine();
         System.out.print("Please input ResourceAdapter class name: ");
         String className = in.readLine();
         

         Template template = new SimpleTemplate();
         Definition def = new Definition();
         def.setRaPackage(packageName);
         def.setRaClass(className);
         
         List<ConfigPropType> props = new ArrayList<ConfigPropType>();
         while (true)
         {
            System.out.println("Please input config properties [enter to quit]: ");
            System.out.print("    Name: ");
            String name = in.readLine();
            if (name == null || name.equals(""))
               break;
            System.out.print("    Type: ");
            String type = in.readLine();
            System.out.print("    Value: ");
            String value = in.readLine();
            System.out.println();
            
            ConfigPropType config = new ConfigPropType(name, type, value);
            props.add(config);
         }
         def.setRaConfigProps(props);
         
         System.out.print("Please input output directory: ");
         String output = in.readLine();
         
         File outDir = new File(output);

         if (!outDir.mkdirs())
         {
            throw new IOException(output + " can't be created");
         }
         
         File report = new File(outDir, className + ".java");
         FileWriter fw = new FileWriter(report);
         template.process(def, fw);
         fw.close();
         System.out.println("Java file wrote");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

}
