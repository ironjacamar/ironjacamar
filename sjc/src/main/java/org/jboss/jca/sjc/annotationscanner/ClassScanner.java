/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.sjc.annotationscanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

/**
 * Class scanner
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 */
public class ClassScanner
{
   private static Logger log = Logger.getLogger(ClassScanner.class);
   private static boolean trace = log.isTraceEnabled();

   /**
    * Constructor
    */
   private ClassScanner()
   {
   }

   /**
    * Scan a directory for class files
    * @param root The root directory
    * @return The class names
    */
   public static List<String> scan(File root)
   {
      List<String> result = new ArrayList<String>();

      try
      {
         log.debug("Scanning " + root);

         String rootPath = root.getCanonicalPath();

         List<File> classFiles = ExtensionScanner.scan(root, ".class");
         
         if (classFiles != null)
         {
            for (File classFile : classFiles)
            {
               try
               {
                  String className = classFile.getCanonicalPath();
                  
                  if (className.startsWith(rootPath))
                  {
                     className = className.substring(rootPath.length() + 1);
                  }

                  if (className.endsWith(".class"))
                  {
                     className = className.substring(0, className.lastIndexOf(".class"));
                  }

                  className = className.replace(File.separatorChar, '.');
                  
                  if (trace)
                     log.trace("Class=" + className);

                  result.add(className);
               }
               catch (Exception e)
               {
                  // Nothing we can do
               }
            }
         }
      } 
      catch (Exception e)
      {
         log.error(e);
      }
    
      return result;
   }
}
