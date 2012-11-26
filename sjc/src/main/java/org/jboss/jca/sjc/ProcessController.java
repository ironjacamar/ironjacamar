/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.sjc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The process controller for IronJacamar instances
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ProcessController
{
   /** Singleton */
   private static final ProcessController INSTANCE = new ProcessController();

   /** Java path */
   private static String java; 

   /** Instances */
   private Map<String, Process> instances;

   static
   {
      java = SecurityActions.getSystemProperty("java.home") + File.separator + "bin" + File.separator + "java";
   }

   /**
    * Default constructor
    */
   private ProcessController()
   {
      this.instances = Collections.synchronizedMap(new HashMap<String, Process>());

      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run()
         {
            for (Process p : instances.values())
            {
               try
               {
                  p.destroy();
               }
               catch (Throwable t)
               {
                  // Ignore
               }
            }
         }
      });
   }

   /**
    * Get instance
    * @return The value
    */
   public static ProcessController getInstance()
   {
      return INSTANCE;
   }

   /**
    * Set the java path
    * @param v The java path
    */
   public static void setJava(String v)
   {
      java = v;
   }

   /**
    * Start an instance
    * @param home The home directory
    * @return True if started successfully; otherwise false
    */
   public boolean start(String home)
   {
      return start(home, null);
   }

   /**
    * Start an instance
    * @param home The home directory
    * @param options The options
    * @return True if started successfully; otherwise false
    */
   public boolean start(String home, File options)
   {
      File homeDirectory = new File(home);
      
      if (!homeDirectory.exists())
         return false;

      stop(home);

      try
      {
         List<String> command = new ArrayList<String>();
         command.add(java);
         command.add("-Xmx512m");
         command.add("-Diron.jacamar.home=" + home);

         if (options != null && options.exists())
            command.add("-Diron.jacamar.options=" + options.getAbsolutePath());

         command.add("-Dorg.jboss.logging.Logger.pluginClass=org.jboss.logging.logmanager.LoggerPluginImpl");
         command.add("-Dlog4j.defaultInitOverride=true");
         command.add("-jar");
         command.add(home + "/bin/ironjacamar-sjc.jar");

         ProcessBuilder pb = new ProcessBuilder(command);
         pb.redirectErrorStream(true);

         Map<String, String> environment = pb.environment();
         environment.put("iron.jacamar.home", home);

         Process p = pb.start();

         instances.put(home, p);

         return true;
      }
      catch (Throwable t)
      {
         // Ignore
      }

      return false;
   }

   /**
    * Stop an instance
    * @param home The home directory
    * @return The exit code
    */
   public int stop(String home)
   {
      Process p = instances.get(home);
      if (p != null)
      {
         try
         {
            p.destroy();
            return p.exitValue();

         }
         catch (Throwable t)
         {
            return -1;
         }
         finally
         {
            instances.remove(home);
         }
      }

      return 0;
   }
}
