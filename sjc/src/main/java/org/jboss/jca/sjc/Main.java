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

import org.jboss.jca.sjc.boot.BeanType;
import org.jboss.jca.sjc.boot.InjectType;
import org.jboss.jca.sjc.boot.PropertyType;
import org.jboss.jca.sjc.deployers.Deployer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

/**
 * The main class for JBoss JCA SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Main
{
   /** Startup list */
   private static List<String> startup = new LinkedList<String>();

   /** Services */
   private static Map<String, Object> services = new HashMap<String, Object>();

   /**
    * Default constructor
    */
   private Main()
   {
   }

   /**
    * Boot
    * @param args The arguments
    */
   private static void boot(String[] args)
   {
      try
      {
         String home = SecurityActions.getSystemProperty("jboss.jca.home");
         File root = null;

         if (home != null)
         {
            root = new File(new URI(home));
         }
         else
         {
            home = new File(".").toURI().toURL().toString();
            root = new File(new URI(home.substring(0, home.lastIndexOf("bin"))));
            SecurityActions.setSystemProperty("jboss.jca.home", root.getAbsolutePath());
         }

         File libDirectory = new File(root, "/lib/");
         File configDirectory = new File(root, "/config/");
         File deployDirectory = new File(root, "/deploy/");

         ClassLoader parent = SecurityActions.getThreadContextClassLoader();

         URL[] libUrls = getUrls(libDirectory);
         URL[] confUrls = getUrls(configDirectory);

         URL[] urls = mergeUrls(libUrls, confUrls);

         URLClassLoader classLoader = new URLClassLoader(urls, parent);
         SecurityActions.setThreadContextClassLoader(classLoader);

         SecurityActions.setSystemProperty("xb.builder.useUnorderedSequence", "true");
         SecurityActions.setSystemProperty("jboss.deploy.url", deployDirectory.toURI().toURL().toString());
         SecurityActions.setSystemProperty("jboss.lib.url", libDirectory.toURI().toURL().toString());
         SecurityActions.setSystemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");

         File bootXml = new File(configDirectory, "boot.xml");
         JAXBContext bootJc = JAXBContext.newInstance("org.jboss.jca.sjc.boot");
         Unmarshaller bootU = bootJc.createUnmarshaller();
         org.jboss.jca.sjc.boot.Deployment boot = 
            (org.jboss.jca.sjc.boot.Deployment)bootU.unmarshal(bootXml);

         if (boot != null)
         {
            for (BeanType bt : boot.getBean())
            {
               if (services.get(bt.getName()) == null)
               {
                  Object bean = createBean(bt, classLoader, deployDirectory);
                  startup.add(bt.getName());
                  services.put(bt.getName(), bean);
               }
               else
               {
                  System.out.println("Warning: A service with name " + bt.getName() + " already exists");
               }
            }
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace(System.err);
      }
   }

   /**
    * Shutdown
    */
   private static void shutdown()
   {
      List<String> shutdown = new LinkedList<String>(startup);
      Collections.reverse(shutdown);

      for (String name : shutdown)
      {
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
      }

      System.out.println("Shutdown complete");
   }

   /**
    * Get the URLs for the directory and all libraries located in the directory
    * @param directrory The directory
    * @return The URLs
    * @exception MalformedURLException MalformedURLException
    * @exception IOException IOException
    */
   private static URL[] getUrls(File directory) throws MalformedURLException, IOException
   {
      List<URL> list = new LinkedList<URL>();

      if (directory.exists() && directory.isDirectory())
      {
         // Add directory
         list.add(directory.toURI().toURL());

         // Add the contents of the directory too
         File[] jars = directory.listFiles(new JarFilter());

         for (int j = 0; jars != null && j < jars.length; j++)
         {
            list.add(jars[j].getCanonicalFile().toURI().toURL());
         }
      }
      return list.toArray(new URL[list.size()]);      
   }

   /**
    * Merge URLs into a single array
    * @param urls The URLs
    * @return The combined list
    */
   private static URL[] mergeUrls(URL[]... urls)
   {
      List<URL> list = new LinkedList<URL>();

      for (URL[] all : urls)
      {
         for (URL url : all)
         {
            list.add(url);
         }
      }

      return list.toArray(new URL[list.size()]);      
   }

   /**
    * Create a bean
    * @param bt The bean type definition
    * @param cl The classloader
    * @param deployDirectory The deploy directory
    * @return The new bean
    * @exception Exception Thrown if an error occurs
    */
   private static Object createBean(BeanType bt, ClassLoader cl, File deployDirectory) throws Exception
   {
      Class<?> clz = Class.forName(bt.getClazz(), true, cl);
      Object instance = clz.newInstance();

      if (bt.getProperty() != null)
      {
         for (PropertyType pt : bt.getProperty())
         {
            setBeanProperty(instance, pt, cl);
         }
      }

      try
      {
         Method createMethod = clz.getMethod("create", (Class[])null);
         createMethod.invoke(instance, (Object[])null);
      }
      catch (Exception e)
      {
         // No create method
      }

      try
      {
         Method startMethod = clz.getMethod("start", (Class[])null);
         startMethod.invoke(instance, (Object[])null);
      }
      catch (Exception e)
      {
         // No start method
      }

      if (instance instanceof Deployer)
      {
         try
         {
            Method deployMethod = clz.getMethod("deploy", new Class[] {File.class});

            for (File f : deployDirectory.listFiles())
            {
               org.jboss.jca.sjc.deployers.Deployment deployment = 
                  (org.jboss.jca.sjc.deployers.Deployment)deployMethod.invoke(instance, new Object[] {f});
               if (deployment != null)
               {
                  if (services.get(deployment.getName()) == null)
                  {
                     startup.add(deployment.getName());
                     services.put(deployment.getName(), deployment);
                  }
                  else
                  {
                     System.out.println("Warning: A deployment with name " + deployment.getName() + " already exists");
                  }
               }
            }
         }
         catch (Exception e)
         {
            e.printStackTrace(System.err);
         }
      }

      return instance;
   }

   /**
    * Set a property on an object instance
    * @param instance The object instance
    * @param pt The property type definition
    * @param cl The classloader
    * @exception Exception Thrown if an error occurs
    */
   private static void setBeanProperty(Object instance, PropertyType pt, ClassLoader cl) throws Exception
   {
      String name = "set" + pt.getName().substring(0, 1).toUpperCase() + pt.getName().substring(1);
      Method m = null;

      if (pt.getClazz() == null)
      {
         Method[] ms = instance.getClass().getMethods();
         if (ms != null)
         {
            boolean found = false;

            for (int i = 0; !found && i < ms.length; i++)
            {
               if (ms[i].getName().equals(name) &&
                   ms[i].getParameterTypes() != null &&
                   ms[i].getParameterTypes().length == 1)
               {
                  m = ms[i];
                  found = true;
               }
            }
         }
      }
      else
      {
         Class clz = Class.forName(pt.getClazz(), true, cl);
         m = instance.getClass().getMethod(name, clz);
      }

      if (m == null)
         throw new Exception("Property " + pt.getName() + " not found on " + instance.getClass().getName());

      Class parameterClass = m.getParameterTypes()[0];

      Object parameterValue = null;
      Object e = pt.getContent().get(0);
      Object element = null;

      if (e != null && e instanceof JAXBElement)
      {
         element = ((JAXBElement)e).getValue();
      }
      else
      {
         if (e == null)
            e = "";

         element = e;
      }

      if (element instanceof InjectType)
      {
         InjectType it = (InjectType)element;

         Object injectionObject = services.get(it.getBean());

         if (injectionObject == null)
            throw new Exception("Injection depedency " + it.getBean() + " not found");

         if (it.getProperty() != null)
         {
            Method method = null;
            try
            {
               String getMethodName = "get" + 
                  it.getProperty().substring(0, 1).toUpperCase() + it.getProperty().substring(1);
               method = injectionObject.getClass().getMethod(getMethodName, (Class[])null);
            }
            catch (NoSuchMethodException nsme)
            {
               String isMethodName = "is" + 
                  it.getProperty().substring(0, 1).toUpperCase() + it.getProperty().substring(1);
               method = injectionObject.getClass().getMethod(isMethodName, (Class[])null);
            }

            parameterValue = method.invoke(injectionObject, (Object[])null);
         }
         else
         {
            parameterValue = injectionObject;
         }
      }
      else
      {
         if (parameterClass.equals(String.class))
         {
            String s = (String)element;

            if (s.indexOf("${") != -1)
            {
               int from = s.indexOf("${");
               int to = s.indexOf("}");

               String systemProperty = SecurityActions.getSystemProperty(s.substring(from + 2, to));
               String prefix = "";
               String postfix = "";

               if (from != 0)
               {
                  prefix = s.substring(0, from);
               }

               if (to + 1 < s.length() - 1)
               {
                  postfix = s.substring(to + 1);
               }

               s = prefix + systemProperty + postfix;
            }

            parameterValue = s;
         }
         else if (parameterClass.equals(int.class) || parameterClass.equals(Integer.class))
         {
            parameterValue = Integer.valueOf((String)element);
         }
         else if (parameterClass.equals(long.class) || parameterClass.equals(Long.class))
         {
            parameterValue = Long.valueOf((String)element);
         }
         else if (parameterClass.equals(boolean.class) || parameterClass.equals(Boolean.class))
         {
            parameterValue = Boolean.valueOf((String)element);
         }
         else if (parameterClass.equals(InetAddress.class))
         {
            parameterValue = InetAddress.getByName((String)element);
         }
      }

      if (parameterValue == null)
         throw new Exception("No parameter value assigned for class " + parameterClass.getName() + 
                             " value " + element);

      m.invoke(instance, parameterValue);
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
         final CountDownLatch latch = new CountDownLatch(1);

         Runnable worker = new Runnable()
         {
            public void run()
            {
               try
               {
                  Main.boot(args);
                  latch.countDown();
               }
               catch (Exception e)
               {
                  System.err.println("Failed to boot JBoss JCA:");
                  e.printStackTrace();
               }
            }
         };
         
         ThreadGroup threads = new ThreadGroup("jboss");
         Thread bootThread = new Thread(threads, worker, "main");
         bootThread.start();

         latch.await();

         LifeThread lifeThread = new LifeThread(threads);
         lifeThread.start();

         Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
               Main.shutdown();
            }
         });

         long l2 = System.currentTimeMillis();
         System.out.println("Server started in " + (l2 - l1) + "ms");
      }
      catch (Exception e)
      {
         e.printStackTrace(System.err);
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
