/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.validator.cli;

import org.ironjacamar.validator.Validation;

import java.io.File;

import java.net.MalformedURLException;

/**
 * Main class
 * 
 * @author Jeff Zhang
 */
public class Main
{
   private static final int SUCCESS = 0;
   private static final int FAIL = 1;
   private static final int OTHER = 2;


   /**
    * Validator standalone tool
    * 
    * @param args command line arguments
    */
   public static void main(String[] args)
   {
      boolean quiet = false;
      String outputDir = "."; //put report into current directory by default
      int arg = 0;
      String[] classpath = null;
      
      if (args.length > 0)
      {
         while (args.length > arg + 1)
         {
            if (args[arg].startsWith("-"))
            {
               if (args[arg].endsWith("quiet"))
               {
                  quiet = true;
               }
               else if (args[arg].endsWith("output"))
               {
                  arg++;
                  if (arg + 1 >= args.length)
                  {
                     usage();
                     System.exit(OTHER);
                  }
                  outputDir = args[arg];
               }
               else if (args[arg].endsWith("classpath"))
               {
                  arg++;
                  classpath = args[arg].split(System.getProperty("path.separator"));

               }
            }
            else
            {
               usage();
               System.exit(OTHER);
            }
            arg++;
         }

         try
         {
            int systemExitCode = Validation.validate(new File(args[arg]).toURI().toURL(), outputDir, classpath);
            
            if (!quiet)
            {
               if (systemExitCode == SUCCESS)
               {
                  System.out.println("Validation sucessful");
               }
               else if (systemExitCode == FAIL)
               {
                  System.out.println("Validation errors");
               }
               else if (systemExitCode == OTHER)
               {
                  System.out.println("Validation unknown");
               }
            }

            System.exit(systemExitCode);
         }
         catch (ArrayIndexOutOfBoundsException oe)
         {
            usage();
            System.exit(OTHER);
         }
         catch (MalformedURLException e)
         {
            e.printStackTrace();
         }
      }
      else
      {
         usage();
      }
         
      System.exit(SUCCESS);
   }

   /**
    * Tool usage
    */
   private static void usage()
   {
      System.out.println("Usage: validator [-quiet] [-output directory] [-classpath thirdparty.jar] <file>");
   }
}
