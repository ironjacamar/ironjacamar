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
import java.io.FileWriter;
import java.io.InputStreamReader;

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

         FileWriter fw = new FileWriter(className + ".java");
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
