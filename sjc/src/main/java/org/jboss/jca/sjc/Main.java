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

package org.jboss.jca.sjc;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.KernelFactory;
import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.configuration.KernelConfiguration;

/**
 * The main class for JBoss JCA SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Main
{
   /** Kernel */
   private static Kernel kernel;

   /** Logging */
   private static Object logging;

   /**
    * Default constructor
    */
   private Main()
   {
   }

   /**
    * Boot
    * @param args The arguments
    * @param tg The thread group used
    */
   private static void boot(final String[] args, final ThreadGroup tg)
   {
      try
      {
         KernelConfiguration kernelConfiguration = new KernelConfiguration();
         kernelConfiguration = kernelConfiguration.name("jboss.jca");
         kernelConfiguration = kernelConfiguration.classLoader(ClassLoaderFactory.TYPE_PARENT_FIRST);
         kernelConfiguration = kernelConfiguration.parallelDeploy(false);
         kernelConfiguration = kernelConfiguration.remoteAccess(true);
         kernelConfiguration = kernelConfiguration.eventListener(new PreClassLoaderEventListener());
         kernelConfiguration = kernelConfiguration.eventListener(new PostClassLoaderEventListener());
         kernelConfiguration = kernelConfiguration.command(new Shutdown());

         String home = SecurityActions.getSystemProperty("jboss.jca.home");
         if (home != null)
         {
            kernelConfiguration.home(new File(home).toURI().toURL());
         }
         else
         {
            home = new File(".").toURI().toURL().toString();
            File root = new File(new URI(home.substring(0, home.lastIndexOf("bin"))));
            kernelConfiguration.home(root.toURI().toURL());
         }

         if (args != null && args.length > 0)
         {
            for (int i = 0; i < args.length; i++)
            {
               if ("-b".equals(args[i]))
               {
                  kernelConfiguration.bindAddress(args[++i]);
               }
            }
         }

         if (tg != null)
            kernelConfiguration.threadGroup(tg);

         kernel = KernelFactory.create(kernelConfiguration);
         kernel.startup();

         initLogging(SecurityActions.getThreadContextClassLoader());
      }
      catch (Throwable t)
      {
         error(t.getMessage(), t);
      }
   }

   /**
    * Init logging
    * @param cl The classloader to load from
    */
   @SuppressWarnings("unchecked") 
   private static void initLogging(ClassLoader cl)
   {
      try
      {
         Class clz = Class.forName("org.jboss.logmanager.log4j.BridgeRepositorySelector", true, cl);
         Method mStart = clz.getMethod("start", (Class[])null);

         Object brs = clz.newInstance();

         logging = mStart.invoke(brs, (Object[])null);
      }
      catch (Throwable t)
      {
         // Nothing we can do
      }


      try
      {
         Class clz = Class.forName("org.jboss.logging.Logger", true, cl);
         
         Method mGetLogger = clz.getMethod("getLogger", String.class);

         logging = mGetLogger.invoke((Object)null, new Object[] {"org.jboss.jca.sjc.Main"});
      }
      catch (Throwable t)
      {
         // Nothing we can do
      }
   }

   /**
    * Logging: ERROR
    * @param s The string
    * @param t The throwable
    */
   @SuppressWarnings("unchecked") 
   private static void error(String s, Throwable t)
   {
      if (logging != null)
      {
         try
         {
            Class clz = logging.getClass();
            Method mError = clz.getMethod("error", Object.class, Throwable.class);
            mError.invoke(logging, new Object[] {s, t});
         }
         catch (Throwable th)
         {
            // Nothing we can do
         }
      }
      else
      {
         System.out.println(s);
         t.printStackTrace(System.out);
      }
   }

   /**
    * Logging: WARN
    * @param s The string
    */
   @SuppressWarnings("unchecked") 
   private static void warn(String s)
   {
      if (logging != null)
      {
         try
         {
            Class clz = logging.getClass();
            Method mWarn = clz.getMethod("warn", Object.class);
            mWarn.invoke(logging, new Object[] {s});
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      else
      {
         System.out.println(s);
      }
   }

   /**
    * Logging: INFO
    * @param s The string
    */
   @SuppressWarnings("unchecked") 
   private static void info(String s)
   {
      if (logging != null)
      {
         try
         {
            Class clz = logging.getClass();
            Method mInfo = clz.getMethod("info", Object.class);
            mInfo.invoke(logging, new Object[] {s});
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      else
      {
         System.out.println(s);
      }
   }

   /**
    * Logging: Is DEBUG enabled
    * @return True if debug is enabled; otherwise false
    */
   @SuppressWarnings("unchecked") 
   private static boolean isDebugEnabled()
   {
      if (logging != null)
      {
         try
         {
            Class clz = logging.getClass();
            Method mIsDebugEnabled = clz.getMethod("isDebugEnabled", (Class[])null);
            return ((Boolean)mIsDebugEnabled.invoke(logging, (Object[])null)).booleanValue();
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      return true;
   }

   /**
    * Logging: DEBUG
    * @param s The string
    */
   @SuppressWarnings("unchecked") 
   private static void debug(String s)
   {
      if (logging != null)
      {
         try
         {
            Class clz = logging.getClass();
            Method mDebug = clz.getMethod("debug", Object.class);
            mDebug.invoke(logging, new Object[] {s});
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      else
      {
         System.out.println(s);
      }
   }

   /**
    * Main
    * @param args The arguments
    */
   public static void main(final String[] args)
   {
      long l1 = System.currentTimeMillis();
      try
      {
         final ThreadGroup threads = new ThreadGroup("jboss-jca");

         Main.boot(args, threads);

         LifeThread lifeThread = new LifeThread(threads);
         lifeThread.start();

         Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
               long l1 = System.currentTimeMillis();
               try
               {
                  if (kernel != null)
                     kernel.shutdown();
               }
               catch (Throwable t)
               {
                  error(t.getMessage(), t);
               }
               long l2 = System.currentTimeMillis();
               info("Shutdown complete in " + (l2 - l1) + " ms");
            }
         });

         long l2 = System.currentTimeMillis();
         info("Server started in " + (l2 - l1) + "ms");
      }
      catch (Exception e)
      {
         error("Exception during main()", e);
      }
   }

   /** 
    * A simple thread that keeps the vm alive in the event there are no
    * other threads started.
    */
   private static class LifeThread extends Thread
   {
      private Object lock = new Object();

      LifeThread(ThreadGroup tg)
      {
         super(tg, "JBossLifeThread");
      }

      public void run()
      {
         synchronized (lock)
         {
            try
            {
               lock.wait();
            }
            catch (InterruptedException ignore)
            {
               //
            }
         }
      }
   }
}
