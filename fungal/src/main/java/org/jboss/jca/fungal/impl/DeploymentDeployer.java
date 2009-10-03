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

import org.jboss.jca.fungal.deployers.DeployException;
import org.jboss.jca.fungal.deployers.Deployer;
import org.jboss.jca.fungal.deployers.Deployment;
import org.jboss.jca.fungal.deployment.BeanType;
import org.jboss.jca.fungal.deployment.ConstructorType;
import org.jboss.jca.fungal.deployment.DependsType;
import org.jboss.jca.fungal.deployment.InjectType;
import org.jboss.jca.fungal.deployment.PropertyType;
import org.jboss.jca.fungal.deployment.Unmarshaller;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * The deployment deployer (deploys .xml files)
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class DeploymentDeployer implements Deployer
{
   /** The kernel */
   private KernelImpl kernel;

   /** Bean latch */
   private CountDownLatch beansLatch;

   /** Logging */
   private static Object logging;

   /**
    * Constructor
    * @param kernel The kernel
    */
   public DeploymentDeployer(KernelImpl kernel)
   {
      if (kernel == null)
         throw new IllegalArgumentException("Kernel is null");

      this.kernel = kernel;
      this.beansLatch = null;

      if (logging == null)
         initLogging(kernel.getKernelClassLoader());
   }

   /**
    * Deploy
    * @param url The URL
    * @param parent The parent classloader
    * @return The deployment; or null if no deployment was made
    * @exception DeployException Thrown if an error occurs during deployment
    */
   public Deployment deploy(URL url, ClassLoader parent) throws DeployException
   {
      if (url == null || !url.toString().endsWith(".xml"))
         return null;

      List<String> beans = Collections.synchronizedList(new ArrayList<String>(1));

      try
      {
         Unmarshaller deploymentU = new Unmarshaller();
         org.jboss.jca.fungal.deployment.Deployment deployment = 
            deploymentU.unmarshal(url);

         if (deployment != null)
         {
            beansLatch = new CountDownLatch(deployment.getBean().size());

            for (BeanType bt : deployment.getBean())
            {
               Runnable r = new ServiceRunnable(bt, beans, kernel, beansLatch, parent);
               Future<?> result = kernel.getExecutorService().submit(r);
            }

            beansLatch.await();
         }
      }
      catch (Throwable t)
      {
         error(t.getMessage(), t);
      }

      return new BeanDeployment(url, beans, kernel);
   }

   /**
    * Service runnable
    */
   static class ServiceRunnable implements Runnable
   {
      /** The bean */
      private BeanType bt;

      /** The bean names */
      private List<String> beans;

      /** The kernel */
      private KernelImpl kernel;

      /** The bean latch */
      private CountDownLatch beansLatch;

      /** The classloader */
      private ClassLoader classLoader;

      /**
       * Constructor
       * @param bt The bean
       * @param beans The list of bean names
       * @param kernel The kernel
       * @param beansLatch The beans latch
       * @param classLoader The class loader
       */
      public ServiceRunnable(BeanType bt, 
                             List<String> beans,
                             KernelImpl kernel,
                             CountDownLatch beansLatch,
                             ClassLoader classLoader)
      {
         this.bt = bt;
         this.beans = beans;
         this.kernel = kernel;
         this.beansLatch = beansLatch;
         this.classLoader = classLoader;
      }

      /**
       * Run
       */
      public void run()
      {
         SecurityActions.setThreadContextClassLoader(classLoader);

         try
         {
            if (kernel.getBean(bt.getName()) == null)
            {
               kernel.setBeanStatus(bt.getName(), ServiceLifecycle.NOT_STARTED);

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

               kernel.setBeanStatus(bt.getName(), ServiceLifecycle.STARTING);

               Object bean = createBean(bt, classLoader);

               kernel.addBean(bt.getName(), bean); 
               beans.add(bt.getName());

               kernel.setBeanStatus(bt.getName(), ServiceLifecycle.STARTED);
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

         beansLatch.countDown();
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
               Object element = pt.getContent().get(0);

               if (element != null && element instanceof InjectType)
               {
                  InjectType it = (InjectType)element;
                  result.add(it.getBean());

                  kernel.addBeanDependants(it.getBean(), bt.getName());
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
            ServiceLifecycle dependencyStatus = kernel.getBeanStatus(dependency);
            if (dependencyStatus == null || dependencyStatus != ServiceLifecycle.STARTED)
               count += 1;
         }

         return count;
      }

      /**
       * Create a bean
       * @param bt The bean type definition
       * @param cl The classloader
       * @return The new bean
       * @exception Exception Thrown if an error occurs
       */
      @SuppressWarnings("unchecked") 
      private Object createBean(BeanType bt, ClassLoader cl) throws Exception
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
                           args[i] = 
                              getValue(ct.getParameter().get(i).getValue(), factoryMethod.getParameterTypes()[i]);
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

         // Register deployer
         if (instance instanceof Deployer)
         {
            kernel.getMainDeployer().addDeployer((Deployer)instance);
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
      private Object getValue(String s, Class<?> clz) throws Exception
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
      private void setBeanProperty(Object instance, PropertyType pt, ClassLoader cl) throws Exception
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
         Object element = pt.getContent().get(0);

         if (element == null)
            element = "";

         if (element instanceof InjectType)
         {
            InjectType it = (InjectType)element;

            Object injectionObject = kernel.getBean(it.getBean());

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
      private String getSubstitutionValue(String input)
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

         logging = mGetLogger.invoke((Object)null, new Object[] {"org.jboss.jca.fungal.impl.DeploymentDeployer"});
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
}
