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

package org.jboss.jca.fungal.impl;

import java.lang.reflect.Method;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The hot deployer for JBoss JCA/Fungal
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public final class HotDeployer implements HotDeployerMBean, Runnable
{
   private int interval;
   private File directory;
   private KernelImpl kernel;

   private AtomicBoolean running;
   private List<URL> deployments;
   private Map<URL, Long> modifiedTimestamp;

   /** Logging */
   private static Object logging;

   /**
    * Constructor
    * @param interval The scan interval in seconds
    * @param directory The directory that should be scanned
    * @param kernel The kernel
    */
   public HotDeployer(int interval, File directory, KernelImpl kernel)
   {
      if (interval <= 0)
         throw new IllegalArgumentException("Internal is invalid");

      if (directory == null)
         throw new IllegalArgumentException("Directory is null");

      if (kernel == null)
         throw new IllegalArgumentException("Kernel is null");

      this.interval = interval;
      this.directory = directory;
      this.kernel = kernel;
      this.running = new AtomicBoolean(false);
      this.deployments = new ArrayList<URL>();
      this.modifiedTimestamp = new HashMap<URL, Long>();

      if (logging == null)
         initLogging(kernel.getKernelClassLoader());
   }

   /**
    * Register deployment
    * @param deployment The deployment
    */
   public void register(URL deployment)
   {
      if (deployment == null)
         throw new IllegalArgumentException("Deployment is null");

      deployments.add(deployment);

      try
      {
         File f = new File(deployment.toURI());
         modifiedTimestamp.put(deployment, Long.valueOf(f.lastModified()));
      }
      catch (URISyntaxException use)
      {
         // Ignore
      }
   }

   /**
    * Unregister deployment
    * @param deployment The deployment
    */
   public void unregister(URL deployment)
   {
      if (deployment == null)
         throw new IllegalArgumentException("Deployment is null");

      deployments.remove(deployment);
      modifiedTimestamp.remove(deployment);
   }

   /**
    * Get the interval in seconds
    * @return The value
    */
   public int getInterval()
   {
      return interval;
   }

   /**
    * Set the interval
    * @param value The value in seconds
    */
   public void setInterval(int value)
   {
      this.interval = value;
   }

   /**
    * Start
    */
   public void start()
   {
      running.set(true);
      
      Future<?> f = kernel.getExecutorService().submit(this);
   }

   /**
    * Stop
    */
   public void stop()
   {
      running.set(false);
   }

   /**
    * Run
    */
   public void run()
   {
      while (running.get())
      {
         long start = System.currentTimeMillis();
         try
         {
            List<URL> removeDeployments = new ArrayList<URL>(deployments);
            List<URL> changedDeployments = null;
            List<URL> newDeployments = null;

            File[] files = directory.listFiles();

            for (File f : files)
            {
               URL url = f.toURI().toURL();
               if (removeDeployments.contains(url))
               {
                  long modified = modifiedTimestamp.get(url).longValue();

                  if (f.lastModified() == modified)
                  {
                     removeDeployments.remove(url);
                  }
                  else
                  {
                     if (changedDeployments == null)
                        changedDeployments = new ArrayList<URL>(1);

                     changedDeployments.add(url);
                  }
               }
               else
               {
                  if (newDeployments == null)
                     newDeployments = new ArrayList<URL>(1);

                  newDeployments.add(url);
               }
            }

            if (removeDeployments.size() > 0)
            {
               for (URL url : removeDeployments)
               {
                  try
                  {
                     unregister(url);
                     kernel.getMainDeployer().undeploy(url);
                  }
                  catch (Throwable undeploy)
                  {
                     error(undeploy.getMessage(), undeploy);
                  }
               }
            }

            if (changedDeployments != null)
            {
               for (URL url : changedDeployments)
               {
                  try
                  {
                     unregister(url);
                     kernel.getMainDeployer().undeploy(url);

                     register(url);
                     kernel.getMainDeployer().deploy(url);
                  }
                  catch (Throwable deploy)
                  {
                     error(deploy.getMessage(), deploy);
                  }
               }
            }

            if (newDeployments != null)
            {
               for (URL url : newDeployments)
               {
                  try
                  {
                     register(url);
                     kernel.getMainDeployer().deploy(url);
                  }
                  catch (Throwable deploy)
                  {
                     error(deploy.getMessage(), deploy);
                  }
               }
            }
            
            long took = System.currentTimeMillis() - start;
            long sleep = interval * 1000L - took;

            if (sleep <= 10)
               sleep = 10;

            Thread.currentThread().sleep(sleep);
         }
         catch (Throwable t)
         {
            error(t.getMessage(), t);
         }
      }
   }

   /**
    * Init logging
    */
   @SuppressWarnings("unchecked") 
   private static void initLogging(ClassLoader cl)
   {
      try
      {
         Class clz = Class.forName("org.jboss.logging.Logger", true, cl);
         
         Method mGetLogger = clz.getMethod("getLogger", String.class);

         logging = mGetLogger.invoke((Object)null, new Object[] {"org.jboss.jca.fungal.impl.HotDeployer"});
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
   private void error(String s, Throwable t)
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
   private void warn(String s)
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
   private void info(String s)
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
   private boolean isDebugEnabled()
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
   private void debug(String s)
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
}
