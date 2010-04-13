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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
         System.out.println("Please input ResourceAdapter package name: ");
         String packageName = in.readLine();
         System.out.println("Please input ResourceAdapter class name: ");
         String className = in.readLine();

         URL file = Main.class.getResource("/ResourceAdapter.template");
         Template template = new SimpleTemplate(file);
         Map<String, String> varMap = new HashMap<String, String>();
         varMap.put("package.name", packageName);
         varMap.put("class.name", className);
         
         FileWriter fw = new FileWriter(className + ".java");
         template.process(varMap, fw);
         fw.close();
         System.out.println("Java file wrote");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

}
