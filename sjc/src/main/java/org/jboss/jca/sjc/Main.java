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
import org.jboss.jca.sjc.boot.ConstructorType;
import org.jboss.jca.sjc.boot.DependsType;
import org.jboss.jca.sjc.boot.InjectType;
import org.jboss.jca.sjc.boot.PropertyType;
import org.jboss.jca.sjc.deployers.Deployer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

/**
 * The main class for JBoss JCA SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Main
{
   /** Parallel startup */
   private static boolean parallel = false;

   /** Startup list */
   private static List<String> startup = new LinkedList<String>();

   /** Services */
   private static ConcurrentMap<String, Object> services = new ConcurrentHashMap<String, Object>();

   /** Services status */
   private static ConcurrentMap<String, Status> servicesStatus = new ConcurrentHashMap<String, Status>();

   /** Services latch */
   private static CountDownLatch servicesLatch;

   /** Executor service */
   private static ExecutorService executorService;

   /** Container classloader */
   private static URLClassLoader containerClassLoader;

   /** The deploy directory */
   private static File deployDirectory;

   /** Logging */
   private static Object logging;

   /**
    * Status
    */
   enum Status
   {
      /** Services not started */
      NOT_STARTED,

      /** Services starting */
      STARTING,

      /** Services started */
      STARTED,
         
      /** Services stopped */
      STOPPING
   }

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

         if (args != null && args.length > 0)
         {
            for (int i = 0; i < args.length; i++)
            {
               if ("-b".equals(args[i]))
               {
                  SecurityActions.setSystemProperty("jboss.jca.bindaddress", args[++i]);
               }
               else if ("-mt".equals(args[i]))
               {
                  parallel = true;
               }
            }
         }

         if (parallel)
         {
            ThreadFactory tf = new SJCThreadFactory(tg);
            executorService = Executors.newCachedThreadPool(tf);
         }

         File libDirectory = new File(root, "/lib/");
         File configDirectory = new File(root, "/config/");
         deployDirectory = new File(root, "/deploy/");

         ClassLoader parent = SecurityActions.getThreadContextClassLoader();

         URL[] libUrls = getUrls(libDirectory);
         URL[] confUrls = getUrls(configDirectory);

         URL[] urls = mergeUrls(libUrls, confUrls);

         containerClassLoader = SecurityActions.createURLCLassLoader(urls, parent);
         SecurityActions.setThreadContextClassLoader(containerClassLoader);

         SecurityActions.setSystemProperty("xb.builder.useUnorderedSequence", "true");
         SecurityActions.setSystemProperty("jboss.deploy.url", deployDirectory.toURI().toURL().toString());
         SecurityActions.setSystemProperty("jboss.lib.url", libDirectory.toURI().toURL().toString());
         SecurityActions.setSystemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");

         initLogging(containerClassLoader);

         File bootXml = new File(configDirectory, "boot.xml");
         JAXBContext bootJc = JAXBContext.newInstance("org.jboss.jca.sjc.boot");
         Unmarshaller bootU = bootJc.createUnmarshaller();
         org.jboss.jca.sjc.boot.Deployment boot = 
            (org.jboss.jca.sjc.boot.Deployment)bootU.unmarshal(bootXml);

         if (boot != null)
         {
            if (parallel)
               servicesLatch = new CountDownLatch(boot.getBean().size());

            for (BeanType bt : boot.getBean())
            {
               if (!parallel)
               {
                  try
                  {
                     if (services.get(bt.getName()) == null)
                     {
                        setServiceStatus(bt.getName(), Status.STARTING);

                        Object bean = createBean(bt, containerClassLoader, deployDirectory);
                        startup.add(bt.getName());
                        services.put(bt.getName(), bean);

                        setServiceStatus(bt.getName(), Status.STARTED);
                     }
                     else
                     {
                        warn("Warning: A service with name " + bt.getName() + " already exists");
                     }
                  }
                  catch (Throwable t)
                  {
                     error("Installing bean " + bt.getName(), t);
                  }
               }
               else
               {
                  Runnable r = new ServiceRunnable(bt);
                  executorService.execute(r);
               }
            }

            if (parallel)
            {
               servicesLatch.await();
               executorService.shutdown();
            }
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace(System.err);
      }
   }

   /**
    * Set service status
    * @param name The service name
    * @param status The service status
    */
   static void setServiceStatus(String name, Status status)
   {
      servicesStatus.put(name, status);
   }

   /**
    * Service runnable
    */
   static class ServiceRunnable implements Runnable
   {
      /** The bean */
      private BeanType bt;

      /**
       * Constructor
       * @param bt The bean
       */
      public ServiceRunnable(BeanType bt)
      {
         this.bt = bt;
      }

      /**
       * Run
       */
      public void run()
      {
         SecurityActions.setThreadContextClassLoader(containerClassLoader);

         try
         {
            if (services.get(bt.getName()) == null)
            {
               setServiceStatus(bt.getName(), Status.NOT_STARTED);

               Set<String> dependencies = getDependencies(bt);
               int notStarted = getNotStarted(dependencies);

               while (notStarted > 0)
               {
                  try
                  {
                     Thread.sleep(10);
                     notStarted = getNotStarted(dependencies);
                  }
                  catch (InterruptedException ie)
                  {
                     Thread.interrupted();
                  }
               }

               setServiceStatus(bt.getName(), Status.STARTING);

               Object bean = createBean(bt, containerClassLoader, deployDirectory);
               startup.add(bt.getName());
               services.put(bt.getName(), bean);

               setServiceStatus(bt.getName(), Status.STARTED);
            }
            else
            {
               warn("Warning: A service with name " + bt.getName() + " already exists");
            }
         }
         catch (Throwable t)
         {
            error("Installing bean " + bt.getName(), t);
         }

         servicesLatch.countDown();
      }

      /**
       * Get the depedencies for a bean
       * @paran bt The bean type
       * @return The set of dependencies; <code>null</code> if no dependencies
       */
      private Set<String> getDependencies(BeanType bt)
      {
         Set<String> result = null;

         List<DependsType> dts = bt.getDepends();
         if (dts != null)
         {
            result = new HashSet<String>();
            for (DependsType dt : dts)
            {
               result.add(dt.getValue());
            }
         }

         List<PropertyType> pts = bt.getProperty();
         if (pts != null)
         {
            if (result == null)
               result = new HashSet<String>();

            for (PropertyType pt : pts)
            {
               Object e = pt.getContent().get(0);

               if (e != null && e instanceof JAXBElement)
               {
                  Object element = ((JAXBElement)e).getValue();
                  if (element instanceof InjectType)
                  {
                     InjectType it = (InjectType)element;
                     result.add(it.getBean());
                  }
               }
            }
         }

         return result;
      }

      /**
       * Get the number of services that are not started yet
       * @paran dependencies The dependencies for a service
       * @return The number of not started services
       */
      private int getNotStarted(Set<String> dependencies)
      {
         if (dependencies == null || dependencies.size() == 0)
            return 0;

         int count = 0;
         for (String dependency : dependencies)
         {
            Status dependencyStatus = servicesStatus.get(dependency);
            if (dependencyStatus == null || dependencyStatus != Status.STARTED)
               count += 1;
         }

         return count;
      }
   }

   /**
    * A thread factory for JCA/SJC
    */
   static class SJCThreadFactory implements ThreadFactory
   {
      /** The thread group */
      private ThreadGroup tg;

      /**
       * Constructor
       * @param tg The thread group
       */
      public SJCThreadFactory(ThreadGroup tg)
      {
         this.tg = tg;
      }

      /**
       * Create a new thread
       * @param r The runnable
       * @return The thread
       */
      public Thread newThread(Runnable r)
      {
         return new Thread(tg, r);
      }
   }

   /**
    * Shutdown
    */
   private static void shutdown()
   {
      SecurityActions.setThreadContextClassLoader(containerClassLoader);

      List<String> shutdown = new LinkedList<String>(startup);
      Collections.reverse(shutdown);

      for (String name : shutdown)
      {
         setServiceStatus(name, Status.STOPPING);

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

         setServiceStatus(name, Status.NOT_STARTED);
      }

      info("Shutdown complete");
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
      Class<?> clz = null;
      Object instance = null;

      if (bt.getClazz() != null)
      {
         clz = Class.forName(bt.getClazz(), true, cl);
         instance = clz.newInstance();
      }
      else
      {
         ConstructorType ct = bt.getConstructor();
         Class factoryClass = Class.forName(ct.getFactoryClass(), true, cl);
         Method factoryMethod = null;
         Object[] args = null;

         if (ct.getParameter() == null)
         {
            factoryMethod = factoryClass.getMethod(ct.getFactoryMethod(), (Class[])null);
         }
         else
         {
            Method[] factoryMethods = factoryClass.getMethods();
            
            List<Method> candidates = new ArrayList<Method>();

            for (Method m : factoryMethods)
            {
               if (ct.getFactoryMethod().equals(m.getName()))
               {
                  if (ct.getParameter().size() == m.getParameterTypes().length)
                  {
                     boolean include = true;
                     for (int i = 0; include && i < m.getParameterTypes().length; i++)
                     {
                        Class<?> parameterClass = m.getParameterTypes()[i];

                        if (!(parameterClass.equals(String.class) ||
                              parameterClass.equals(byte.class) || parameterClass.equals(Byte.class) ||
                              parameterClass.equals(short.class) || parameterClass.equals(Short.class) ||
                              parameterClass.equals(int.class) || parameterClass.equals(Integer.class) ||
                              parameterClass.equals(long.class) || parameterClass.equals(Long.class) ||
                              parameterClass.equals(float.class) || parameterClass.equals(Float.class) ||
                              parameterClass.equals(double.class) || parameterClass.equals(Double.class) ||
                              parameterClass.equals(boolean.class) || parameterClass.equals(Boolean.class) ||
                              parameterClass.equals(char.class) || parameterClass.equals(Character.class) ||
                              parameterClass.equals(InetAddress.class)))
                        {
                           include = false;
                        }
                     }

                     if (include)
                        candidates.add(m);
                  }
               }
            }

            if (candidates.size() == 1)
            {
               factoryMethod = candidates.get(0);
               args = new Object[ct.getParameter().size()];
               for (int i = 0; i < ct.getParameter().size(); i++)
               {
                  args[i] = getValue(ct.getParameter().get(i).getValue(), factoryMethod.getParameterTypes()[i]);
               }
            }
            else
            {
               boolean found = false;
               Iterator<Method> it = candidates.iterator();
               while (!found && it.hasNext())
               {
                  try
                  {
                     Method m = it.next();
                     args = new Object[ct.getParameter().size()];

                     for (int i = 0; i < ct.getParameter().size(); i++)
                     {
                        args[i] = getValue(ct.getParameter().get(i).getValue(), factoryMethod.getParameterTypes()[i]);
                     }

                     factoryMethod = m;
                     found = true;
                  }
                  catch (Throwable t)
                  {
                     // ok - not this one...
                  }
               }
            }
         }

         instance = factoryMethod.invoke((Object)null, args);
         clz = instance.getClass();
      }

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
            Method deployMethod = clz.getMethod("deploy", new Class[] {File.class, ClassLoader.class});

            for (File f : deployDirectory.listFiles())
            {
               try
               {
                  Object[] parameters = new Object[] {f, cl};
                  org.jboss.jca.sjc.deployers.Deployment deployment = 
                     (org.jboss.jca.sjc.deployers.Deployment)deployMethod.invoke(instance, parameters);
                  if (deployment != null)
                  {
                     if (services.get(deployment.getName()) == null)
                     {
                        startup.add(deployment.getName());
                        services.put(deployment.getName(), deployment);
                     }
                     else
                     {
                        warn("Warning: A deployment with name " + deployment.getName() + " already exists");
                     }
                  }
                  else
                  {
                     warn("Ignoring deployment " + f.getName());
                  }
               }
               catch (Exception de)
               {
                  error("Deployment " + f.getName() + " failed.", de);
               }
            }
         }
         catch (Exception e)
         {
            error("Exception during createBean()", e);
         }
      }

      return instance;
   }

   /**
    * Get a value from a string
    * @param s The string representation
    * @param clz The class
    * @return The value
    * @exception Exception If the string cant be converted
    */
   private static Object getValue(String s, Class<?> clz) throws Exception
   {
      if (clz.equals(String.class))
      {
         return s;
      }
      else if (clz.equals(byte.class) || clz.equals(Byte.class))
      {
         return Byte.valueOf(s);
      }
      else if (clz.equals(short.class) || clz.equals(Short.class))
      {
         return Short.valueOf(s);
      }
      else if (clz.equals(int.class) || clz.equals(Integer.class))
      {
         return Integer.valueOf(s);
      }
      else if (clz.equals(long.class) || clz.equals(Long.class))
      {
         return Long.valueOf(s);
      }
      else if (clz.equals(float.class) || clz.equals(Float.class))
      {
         return Float.valueOf(s);
      }
      else if (clz.equals(double.class) || clz.equals(Double.class))
      {
         return Double.valueOf(s);
      }
      else if (clz.equals(boolean.class) || clz.equals(Boolean.class))
      {
         return Boolean.valueOf(s);
      }
      else if (clz.equals(char.class) || clz.equals(Character.class))
      {
         return Character.valueOf(s.charAt(0));
      }
      else if (clz.equals(InetAddress.class))
      {
         return InetAddress.getByName(s);
      }

      throw new Exception("Unknown class " + clz.getName() + " for " + s);
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
            parameterValue = getSubstitutionValue((String)element);
         }
         else if (parameterClass.equals(byte.class) || parameterClass.equals(Byte.class))
         {
            parameterValue = Byte.valueOf(getSubstitutionValue((String)element));
         }
         else if (parameterClass.equals(short.class) || parameterClass.equals(Short.class))
         {
            parameterValue = Short.valueOf(getSubstitutionValue((String)element));
         }
         else if (parameterClass.equals(int.class) || parameterClass.equals(Integer.class))
         {
            parameterValue = Integer.valueOf(getSubstitutionValue((String)element));
         }
         else if (parameterClass.equals(long.class) || parameterClass.equals(Long.class))
         {
            parameterValue = Long.valueOf(getSubstitutionValue((String)element));
         }
         else if (parameterClass.equals(float.class) || parameterClass.equals(Float.class))
         {
            parameterValue = Float.valueOf(getSubstitutionValue((String)element));
         }
         else if (parameterClass.equals(double.class) || parameterClass.equals(Double.class))
         {
            parameterValue = Double.valueOf(getSubstitutionValue((String)element));
         }
         else if (parameterClass.equals(boolean.class) || parameterClass.equals(Boolean.class))
         {
            parameterValue = Boolean.valueOf(getSubstitutionValue((String)element));
         }
         else if (parameterClass.equals(char.class) || parameterClass.equals(Character.class))
         {
            parameterValue = Character.valueOf((getSubstitutionValue((String)element)).charAt(0));
         }
         else if (parameterClass.equals(InetAddress.class))
         {
            parameterValue = InetAddress.getByName(getSubstitutionValue((String)element));
         }
      }

      if (parameterValue == null)
         throw new Exception("No parameter value assigned for class " + parameterClass.getName() + 
                             " value " + element);

      m.invoke(instance, parameterValue);
   }

   /**
    * System property substitution
    * @param input The input string
    * @return The output
    */
   private static String getSubstitutionValue(String input)
   {
      if (input == null || input.trim().equals(""))
         return input;

      if (input.indexOf("${") != -1)
      {
         int from = input.indexOf("${");
         int to = input.indexOf("}");
         int dv = input.indexOf(":");
         
         String systemProperty = "";
         String defaultValue = "";
         if (dv == -1)
         {
            systemProperty = SecurityActions.getSystemProperty(input.substring(from + 2, to));
         }
         else
         {
            systemProperty = SecurityActions.getSystemProperty(input.substring(from + 2, dv));
            defaultValue = input.substring(dv + 1, to);
         }
         String prefix = "";
         String postfix = "";

         if (from != 0)
         {
            prefix = input.substring(0, from);
         }
         
         if (to + 1 < input.length() - 1)
         {
            postfix = input.substring(to + 1);
         }

         if (systemProperty != null && !systemProperty.trim().equals(""))
         {
            return prefix + systemProperty + postfix;
         }
         else if (defaultValue != null && !defaultValue.trim().equals(""))
         {
            return prefix + defaultValue + postfix;
         }
      }
      return input;
   }

   /**
    * Init logging
    * @param cl The classloader to load from
    */
   private static void initLogging(ClassLoader cl)
   {
      try
      {
         Class clz = Class.forName("org.jboss.logmanager.log4j.BridgeRepositorySelector", true, cl);
         Method mStart = clz.getMethod("start", (Class[])null);

         Object brs = clz.newInstance();

         logging = mStart.invoke(brs, (Object[])null);
      }
      catch (Exception e)
      {
         // Nothing we can do
      }


      try
      {
         Class clz = Class.forName("org.jboss.logging.Logger", true, cl);
         
         Method mGetLogger = clz.getMethod("getLogger", String.class);

         logging = mGetLogger.invoke((Object)null, new Object[] {"org.jboss.jca.sjc.Main"});
      }
      catch (Exception e)
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
         catch (Exception e)
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
         catch (Exception e)
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
         catch (Exception e)
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
         catch (Exception e)
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
         catch (Exception e)
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
         final CountDownLatch latch = new CountDownLatch(1);

         final ThreadGroup threads = new ThreadGroup("jboss");

         Runnable worker = new Runnable()
         {
            public void run()
            {
               try
               {
                  Main.boot(args, threads);
                  latch.countDown();
               }
               catch (Exception e)
               {
                  error("Failed to boot JBoss JCA", e);
               }
            }
         };
         
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

               if (containerClassLoader != null && containerClassLoader instanceof Closeable)
               {
                  try
                  {
                     ((Closeable)containerClassLoader).close();
                  }
                  catch (IOException ioe)
                  {
                     // Swallow
                  }
               }
            }
         });

         if (isDebugEnabled())
         {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            debug("Heap memory: " + memoryBean.getHeapMemoryUsage().toString());
            debug("NonHeap memory: " + memoryBean.getNonHeapMemoryUsage().toString());
         }

         long l2 = System.currentTimeMillis();
         if (!parallel)
         {
            info("Server started in " + (l2 - l1) + "ms");
         }
         else
         {
            info("Server (MT mode) started in " + (l2 - l1) + "ms");
         }
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
