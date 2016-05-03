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

package org.ironjacamar.sjc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.KernelFactory;
import com.github.fungal.api.classloading.ClassLoaderFactory;
import com.github.fungal.api.configuration.DeploymentOrder;
import com.github.fungal.api.configuration.KernelConfiguration;

/**
 * The main class for IronJacamar SJC
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Main
{
   /** Default name */
   private static final String DEFAULT_NAME = "ironjacamar";

   /** ironjacamar.properties */
   private static final String IRONJACAMAR_PROPERTIES = "ironjacamar.properties";

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
         Properties properties = loadProperties();

         List<String> order = new ArrayList<String>(5);
         order.add(".xml");
         order.add(".rar");
         order.add("-ra.xml");
         order.add("-ds.xml");
         order.add(".war");

         KernelConfiguration kernelConfiguration = new KernelConfiguration();
         String configurationName = configurationString(properties, "name", DEFAULT_NAME);

         kernelConfiguration = kernelConfiguration.name(configurationName);
         kernelConfiguration = kernelConfiguration.classLoader(ClassLoaderFactory.TYPE_PARENT_FIRST);

         kernelConfiguration =
            kernelConfiguration.management(configurationBoolean(properties, "management", true));

         kernelConfiguration = 
            kernelConfiguration.parallelDeploy(configurationBoolean(properties, "parallel.deploy", true));

         kernelConfiguration =
            kernelConfiguration.remoteAccess(configurationBoolean(properties, "remote.access", true));

         kernelConfiguration =
            kernelConfiguration.remotePort(configurationInteger(properties, "remote.port", 1202));

         kernelConfiguration = kernelConfiguration.eventListener(new PreClassLoaderEventListener());
         kernelConfiguration = kernelConfiguration.eventListener(new PostClassLoaderEventListener());
         kernelConfiguration = kernelConfiguration.command(new Shutdown());
         kernelConfiguration = kernelConfiguration.deploymentOrder(new DeploymentOrder(order));

         kernelConfiguration = 
            kernelConfiguration.remoteJmxAccess(configurationBoolean(properties, "remote.jmx.access", true));

         kernelConfiguration = 
            kernelConfiguration.usePlatformMBeanServer(configurationBoolean(properties, "use.platform.mbeanserver",
                                                                            true));

         kernelConfiguration = 
            kernelConfiguration.beanManagement(configurationBoolean(properties, "bean.management", true));

         applySystemProperties(properties);

         String home = SecurityActions.getSystemProperty(configurationName + ".home");
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
         Class<?> clz = Class.forName("org.jboss.logging.Logger", true, cl);
         
         Method mGetLogger = clz.getMethod("getLogger", String.class);

         logging = mGetLogger.invoke((Object)null, new Object[] {"org.ironjacamar.sjc.Main"});
      }
      catch (Throwable t)
      {
         // Nothing we can do
      }
   }

   /**
    * Logging: ERROR
    * @param o The object
    * @param t The throwable
    */
   @SuppressWarnings("unchecked") 
   private static void error(Object o, Throwable t)
   {
      if (logging != null)
      {
         try
         {
            Class<?> clz = logging.getClass();
            Method mError = clz.getMethod("error", Object.class, Throwable.class);
            mError.invoke(logging, new Object[] {o, t});
         }
         catch (Throwable th)
         {
            // Nothing we can do
         }
      }
      else
      {
         if (o != null)
            System.out.println(o.toString());

         if (t != null)
            t.printStackTrace(System.out);
      }
   }

   /**
    * Logging: WARN
    * @param o The object
    */
   @SuppressWarnings("unchecked") 
   private static void warn(Object o)
   {
      if (logging != null)
      {
         try
         {
            Class<?> clz = logging.getClass();
            Method mWarn = clz.getMethod("warn", Object.class);
            mWarn.invoke(logging, new Object[] {o});
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      else
      {
         if (o != null)
            System.out.println(o.toString());
      }
   }

   /**
    * Logging: INFO
    * @param o The object
    */
   @SuppressWarnings("unchecked") 
   private static void info(Object o)
   {
      if (logging != null)
      {
         try
         {
            Class<?> clz = logging.getClass();
            Method mInfo = clz.getMethod("info", Object.class);
            mInfo.invoke(logging, new Object[] {o});
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      else
      {
         if (o != null)
            System.out.println(o.toString());
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
            Class<?> clz = logging.getClass();
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
    * @param o The object
    */
   @SuppressWarnings("unchecked") 
   private static void debug(Object o)
   {
      if (logging != null)
      {
         try
         {
            Class<?> clz = logging.getClass();
            Method mDebug = clz.getMethod("debug", Object.class);
            mDebug.invoke(logging, new Object[] {o});
         }
         catch (Throwable t)
         {
            // Nothing we can do
         }
      }
      else
      {
         if (o != null)
            System.out.println(o.toString());
      }
   }

   /**
    * Load configuration values specified from either a file or the classloader
    * @return The properties
    */
   private static Properties loadProperties()
   {
      Properties properties = new Properties();
      boolean loaded = false;

      String sysProperty = SecurityActions.getSystemProperty("ironjacamar.options");
      if (sysProperty != null && !sysProperty.equals(""))
      {
         File file = new File(sysProperty);
         if (file.exists())
         {
            FileInputStream fis = null;
            try
            {
               fis = new FileInputStream(file);
               properties.load(fis);
               loaded = true;
            }
            catch (Throwable t)
            {
               // Ignore
            }
            finally
            {
               if (fis != null)
               {
                  try
                  {
                     fis.close();
                  }
                  catch (IOException ioe)
                  {
                     //No op
                  }
               }
            }
         }
      }

      if (!loaded)
      {
         File file = new File(IRONJACAMAR_PROPERTIES);
         if (file.exists())
         {
            FileInputStream fis = null;
            try
            {
               fis = new FileInputStream(file);
               properties.load(fis);
               loaded = true;
            }
            catch (Throwable t)
            {
               // Ignore
            }
            finally
            {
               if (fis != null)
               {
                  try
                  {
                     fis.close();
                  }
                  catch (IOException ioe)
                  {
                     //No op
                  }
               }
            }
         }
      }

      if (!loaded)
      {
         InputStream is = null;
         try
         {
            ClassLoader cl = Main.class.getClassLoader();
            is = cl.getResourceAsStream(IRONJACAMAR_PROPERTIES);
            properties.load(is);
            loaded = true;
         }
         catch (Throwable t)
         {
            // Ignore
         }
         finally
         {
            if (is != null)
            {
               try
               {
                  is.close();
               }
               catch (IOException ioe)
               {
                  // Nothing to do
               }
            }
         }
      }

      return properties;
   }

   /**
    * Get configuration string
    * @param properties The properties
    * @param key The key
    * @param defaultValue The default value
    * @return The value
    */
   private static String configurationString(Properties properties, String key, String defaultValue)
   {
      if (properties != null)
      {
         return properties.getProperty(key, defaultValue);
      }

      return defaultValue;
   }

   /**
    * Get configuration boolean
    * @param properties The properties
    * @param key The key
    * @param defaultValue The default value
    * @return The value
    */
   private static boolean configurationBoolean(Properties properties, String key, boolean defaultValue)
   {
      if (properties != null)
      {
         if (properties.containsKey(key))
            return Boolean.valueOf(properties.getProperty(key));
      }

      return defaultValue;
   }

   /**
    * Get configuration integer
    * @param properties The properties
    * @param key The key
    * @param defaultValue The default value
    * @return The value
    */
   private static int configurationInteger(Properties properties, String key, int defaultValue)
   {
      if (properties != null)
      {
         if (properties.containsKey(key))
            return Integer.valueOf(properties.getProperty(key));
      }

      return defaultValue;
   }

   /**
    * Apply any defined system properties
    * @param properties The properties
    */
   private static void applySystemProperties(Properties properties)
   {
      if (properties != null)
      {
         for (Map.Entry<Object, Object> entry : properties.entrySet())
         {
            String key = (String)entry.getKey();
            if (key.startsWith("system.property."))
            {
               key = key.substring(16);
               SecurityActions.setSystemProperty(key, (String)entry.getValue());
            }
         }
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
         final ThreadGroup threads = new ThreadGroup("ironjacamar");

         Main.boot(args, threads);

         LifeThread lifeThread = new LifeThread(threads);
         lifeThread.start();

         Runtime.getRuntime().addShutdownHook(new Thread()
         {
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
               info("Server stopped in " + (l2 - l1) + "ms");
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
         super(tg, "IronJacamarLifeThread");
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
