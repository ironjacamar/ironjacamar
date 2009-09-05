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

import org.jboss.jca.fungal.deployers.Deployer;
import org.jboss.jca.fungal.deployers.Deployment;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The main deployer for JBoss JCA/Fungal
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class MainDeployer
{
   private KernelImpl kernel;
   private List<Deployer> deployers;

   /** Logging */
   private static Object logging;

   static
   {
      initLogging();
   }

   /**
    * Constructor
    * @param kernel The kernel
    */
   public MainDeployer(KernelImpl kernel)
   {
      this.kernel = kernel;
      this.deployers = new ArrayList<Deployer>();
   }

   /**
    * Add deployer
    * @param deployer The deployer
    */
   public void addDeployer(Deployer deployer)
   {
      deployers.add(deployer);
   }

   /**
    * Deploy
    * @param url The URL for the deployment
    * @param classLoader The class loader
    */
   public void deploy(URL url, ClassLoader classLoader)
   {
      boolean done = false;
      try
      {
         for (int i = 0; !done && i < deployers.size(); i++)
         {
            Deployer deployer = deployers.get(i);
            
            Deployment deployment = deployer.deploy(url, classLoader);
            if (deployment != null)
            {
               kernel.registerDeployment(deployment);
               done = true;
            }
         }
      }
      catch (Throwable t)
      {
         error(t.getMessage(), t);
      }
   }

   /**
    * Init logging
    */
   private static void initLogging()
   {
      try
      {
         Class clz = Class.forName("org.jboss.logging.Logger");
         
         Method mGetLogger = clz.getMethod("getLogger", String.class);

         logging = mGetLogger.invoke((Object)null, new Object[] {"org.jboss.jca.fungal.impl.MainDeployer"});
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
}
