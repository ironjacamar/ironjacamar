/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.fungal.api.Kernel;
import org.jboss.jca.fungal.deployers.Deployment;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 * The kernel implementation for JBoss JCA/Fungal
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class KernelImpl implements Kernel
{
   /** Kernel configuration */
   private KernelConfiguration kernelConfiguration;

   /** Startup list */
   private List<String> startup = Collections.synchronizedList(new LinkedList<String>());

   /** Deployments */
   private List<Deployment> deployments = Collections.synchronizedList(new LinkedList<Deployment>());

   /** Services */
   private ConcurrentMap<String, Object> services = new ConcurrentHashMap<String, Object>();

   /** Services status */
   private ConcurrentMap<String, ServiceLifecycle> servicesStatus = new ConcurrentHashMap<String, ServiceLifecycle>();

   /** Executor service */
   private ExecutorService executorService;

   /** The old class loader */
   private ClassLoader oldClassLoader;

   /** Kernel class loader */
   private KernelClassLoader kernelClassLoader;

   /** Main deployer */
   private MainDeployer mainDeployer;

   /** MBeanServer */
   private MBeanServer mbeanServer;

   /** Temporary environment */
   private boolean temporaryEnvironment;

   /** Logging */
   private Object logging;

   /**
    * Constructor
    * @param kc The kernel configuration
    */
   public KernelImpl(KernelConfiguration kc)
   {
      this.kernelConfiguration = kc;
      this.temporaryEnvironment = false;
   }

   /**
    * Get the MBeanServer for the kernel
    * @return The MBeanServer instance
    */
   public MBeanServer getMBeanServer()
   {
      return mbeanServer;
   }

   /**
    * Startup
    */
   public void startup()
   {
      try
      {
         ThreadGroup tg = kernelConfiguration.getThreadGroup();
         if (tg == null)
            tg = new ThreadGroup("jboss");
         ThreadFactory tf = new FungalThreadFactory(tg);
         executorService = Executors.newCachedThreadPool(tf);

         File root = null;

         if (kernelConfiguration.getHome() != null)
         {
            root = new File(kernelConfiguration.getHome().toURI());
            SecurityActions.setSystemProperty("jboss.jca.home", root.getAbsolutePath());
         }
         else
         {
            File tmp = new File(SecurityActions.getSystemProperty("java.io.tmpdir"));
            root = new File(tmp, "jboss-jca");

            if (root.exists())
            {
               try
               {
                  recursiveDelete(root);
               }
               catch (Throwable t)
               {
                  // TODO
                  error(t.getMessage(), t);
               }
            }

            if (!root.mkdirs())
               throw new IOException("Could not create directory " + root.getAbsolutePath());

            SecurityActions.setSystemProperty("jboss.jca.home", root.getAbsolutePath());

            temporaryEnvironment = true;
         }

         File libDirectory = null;
         File configDirectory = null;
         File deployDirectory = null;

         if (root != null)
         {
            libDirectory = new File(root, "/lib/");
            configDirectory = new File(root, "/config/");
            deployDirectory = new File(root, "/deploy/");
         }

         oldClassLoader = SecurityActions.getThreadContextClassLoader();

         URL[] libUrls = getUrls(libDirectory);
         URL[] confUrls = getUrls(configDirectory);

         URL[] urls = mergeUrls(libUrls, confUrls);

         kernelClassLoader = SecurityActions.createKernelClassLoader(urls, oldClassLoader);
         SecurityActions.setThreadContextClassLoader(kernelClassLoader);

         SecurityActions.setSystemProperty("xb.builder.useUnorderedSequence", "true");
         SecurityActions.setSystemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");

         if (kernelConfiguration.getBindAddress() != null)
            SecurityActions.setSystemProperty("jboss.jca.bindaddress", kernelConfiguration.getBindAddress().trim());

         // Init logging
         initLogging(kernelClassLoader);

         // Create MBeanServer
         mbeanServer = MBeanServerFactory.createMBeanServer("jboss.jca");

         // Main deployer
         mainDeployer = new MainDeployer(this);
         ObjectName mainDeployerObjectName = new ObjectName("jboss.jca:name=MainDeployer");
         mbeanServer.registerMBean(mainDeployer, mainDeployerObjectName);

         // Add the deployment deployer
         mainDeployer.addDeployer(new DeploymentDeployer(this));

         // Add the kernel bean reference
         addBean("Kernel", this);
         setBeanStatus("Kernel", ServiceLifecycle.STARTED);

         // Start all URLs defined in bootstrap.xml
         if (configDirectory != null && configDirectory.exists() && configDirectory.isDirectory())
         {
            File bootXml = new File(configDirectory, "bootstrap.xml");
            JAXBContext bootJc = JAXBContext.newInstance("org.jboss.jca.fungal.bootstrap");
            Unmarshaller bootU = bootJc.createUnmarshaller();
            org.jboss.jca.fungal.bootstrap.Bootstrap boot = 
               (org.jboss.jca.fungal.bootstrap.Bootstrap)bootU.unmarshal(bootXml);

            // Boot urls
            if (boot != null)
            {
               for (String url : boot.getUrl())
               {
                  try
                  {
                     URL fullPath = new URL(configDirectory.toURI().toURL().toExternalForm() + url);

                     if (isDebugEnabled())
                        debug("URL=" + fullPath.toString());

                     mainDeployer.deploy(fullPath, kernelClassLoader);
                  }
                  catch (Throwable deployThrowable)
                  {
                     error(deployThrowable.getMessage(), deployThrowable);
                  }
               }
            }
         }

         // Deploy all files in deploy/
         if (deployDirectory != null && deployDirectory.exists() && deployDirectory.isDirectory())
         {
            for (File f : deployDirectory.listFiles())
            {
               try
               {
                  if (isDebugEnabled())
                     debug("URL=" + f.toURI().toURL().toExternalForm());

                  mainDeployer.deploy(f.toURI().toURL(), kernelClassLoader);
               }
               catch (Throwable deployThrowable)
               {
                  error(deployThrowable.getMessage(), deployThrowable);
               }
            }
         }
      }
      catch (Throwable t)
      {
         error(t.getMessage(), t);
      }
   }

   /**
    * Shutdown
    */
   public void shutdown()
   {
      SecurityActions.setThreadContextClassLoader(kernelClassLoader);

      executorService.shutdown();

      List<Deployment> shutdownDeployments = new LinkedList<Deployment>(deployments);
      Collections.reverse(shutdownDeployments);

      for (Deployment deployment : shutdownDeployments)
      {
         try
         {
            Method stopMethod = deployment.getClass().getMethod("stop", (Class[])null);
            stopMethod.invoke(deployment, (Object[])null);
         }
         catch (Exception e)
         {
            // No stop method
         }

         try
         {
            Method destroyMethod = deployment.getClass().getMethod("destroy", (Class[])null);
            destroyMethod.invoke(deployment, (Object[])null);
         }
         catch (Exception e)
         {
            // No destroy method
         }
      }

      List<String> shutdownServices = new LinkedList<String>(startup);
      Collections.reverse(shutdownServices);

      for (String name : shutdownServices)
      {
         setBeanStatus(name, ServiceLifecycle.STOPPING);

         Object service = services.get(name);

         try
         {
            Method stopMethod = service.getClass().getMethod("stop", (Class[])null);
            stopMethod.invoke(service, (Object[])null);
         }
         catch (Exception e)
         {
            // No stop method
         }

         try
         {
            Method destroyMethod = service.getClass().getMethod("destroy", (Class[])null);
            destroyMethod.invoke(service, (Object[])null);
         }
         catch (Exception e)
         {
            // No destroy method
         }

         setBeanStatus(name, ServiceLifecycle.NOT_STARTED);
      }

      // Release MBeanServer
      MBeanServerFactory.releaseMBeanServer(mbeanServer);

      info("Shutdown complete");

      if (temporaryEnvironment)
      {
         File tmp = new File(SecurityActions.getSystemProperty("java.io.tmpdir"));
         File root = new File(tmp, "jboss-jca");

         try
         {
            recursiveDelete(root);
         }
         catch (Throwable t)
         {
            // TODO
            error(t.getMessage(), t);
         }
      }

      if (kernelClassLoader != null && kernelClassLoader instanceof Closeable)
      {
         try
         {
            ((Closeable)kernelClassLoader).close();
         }
         catch (IOException ioe)
         {
            // Swallow
         }
      }

      SecurityActions.setThreadContextClassLoader(oldClassLoader);
   }

   /**
    * Get the kernel class loader
    * @return The class loader
    */
   KernelClassLoader getKernelClassLoader()
   {
      return kernelClassLoader;
   }

   /** 
    * Get the executor service
    * @return The executor service
    */
   ExecutorService getExecutorService()
   {
      return executorService;
   }

   /**
    * Get the bean status
    * @param name The bean name
    * @return The status
    */
   ServiceLifecycle getBeanStatus(String name)
   {
      return servicesStatus.get(name);
   }

   /**
    * Set the bean status
    * @param name The bean name
    * @param status The status
    */
   void setBeanStatus(String name, ServiceLifecycle status)
   {
      servicesStatus.put(name, status);
   }

   /**
    * Add a bean
    * @param name The name of the bean
    * @param bean The bean
    */
   synchronized void addBean(String name, Object bean)
   {
      startup.add(name);
      services.put(name, bean);
   }

   /**
    * Get a bean
    * @param name The name of the bean
    * @return The bean
    */
   public Object getBean(String name)
   {
      return services.get(name);
   }

   /**
    * Register deployment
    * @param deployment The deployment
    */
   void registerDeployment(Deployment deployment)
   {
      deployments.add(deployment);
   }

   /**
    * Get the main deployer
    * @return The main deployer
    */
   public MainDeployer getMainDeployer()
   {
      return mainDeployer;
   }

   /**
    * Get the URLs for the directory and all libraries located in the directory
    * @param directrory The directory
    * @return The URLs
    * @exception MalformedURLException MalformedURLException
    * @exception IOException IOException
    */
   private URL[] getUrls(File directory) throws MalformedURLException, IOException
   {
      if (directory != null && directory.exists() && directory.isDirectory())
      {
         List<URL> list = new LinkedList<URL>();

         // Add directory
         list.add(directory.toURI().toURL());

         // Add the contents of the directory too
         File[] jars = directory.listFiles(new JarFilter());

         if (jars != null)
         {
            for (int j = 0; jars != null && j < jars.length; j++)
            {
               list.add(jars[j].getCanonicalFile().toURI().toURL());
            }
         }
         
         return list.toArray(new URL[list.size()]);      
      }

      return null;
   }

   /**
    * Merge URLs into a single array
    * @param urls The URLs
    * @return The combined list
    */
   private URL[] mergeUrls(URL[]... urls)
   {
      if (urls != null)
      {
         List<URL> list = new LinkedList<URL>();

         for (URL[] u : urls)
         {
            if (u != null)
            {
               for (URL url : u)
               {
                  list.add(url);
               }
            }
         }

         return list.toArray(new URL[list.size()]);      
      }

      return null;
   }

   /**
    * Recursive delete
    * @param f The file handler
    * @exception IOException Thrown if a file could not be deleted
    */
   private void recursiveDelete(File f) throws IOException
   {
      if (f != null && f.exists())
      {
         File[] files = f.listFiles();
         if (files != null)
         {
            for (int i = 0; i < files.length; i++)
            {
               if (files[i].isDirectory())
               {
                  recursiveDelete(files[i]);
               } 
               else
               {
                  if (!files[i].delete())
                     throw new IOException("Could not delete " + files[i]);
               }
            }
         }
         if (!f.delete())
            throw new IOException("Could not delete " + f);
      }
   }

   /**
    * Init logging
    * @param cl The classloader to load from
    */
   private void initLogging(ClassLoader cl)
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

         logging = mGetLogger.invoke((Object)null, new Object[] {"org.jboss.jca.fungal.impl.KernelImpl"});
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
