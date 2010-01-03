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

import org.jboss.jca.fungal.deployers.CloneableDeployer;
import org.jboss.jca.fungal.deployers.DeployException;
import org.jboss.jca.fungal.deployers.Deployer;
import org.jboss.jca.fungal.deployers.Deployment;
import org.jboss.jca.fungal.deployment.BeanType;
import org.jboss.jca.fungal.deployment.ConstructorType;
import org.jboss.jca.fungal.deployment.DependsType;
import org.jboss.jca.fungal.deployment.EntryType;
import org.jboss.jca.fungal.deployment.InjectType;
import org.jboss.jca.fungal.deployment.InstallType;
import org.jboss.jca.fungal.deployment.ListType;
import org.jboss.jca.fungal.deployment.MapType;
import org.jboss.jca.fungal.deployment.NullType;
import org.jboss.jca.fungal.deployment.PropertyType;
import org.jboss.jca.fungal.deployment.SetType;
import org.jboss.jca.fungal.deployment.ThisType;
import org.jboss.jca.fungal.deployment.UninstallType;
import org.jboss.jca.fungal.deployment.Unmarshaller;
import org.jboss.jca.fungal.deployment.ValueType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * The deployment deployer (deploys .xml files)
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public final class DeploymentDeployer implements CloneableDeployer
{
   /** The kernel */
   private KernelImpl kernel;

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

      DeployException deployException = null;
      try
      {
         Unmarshaller deploymentU = new Unmarshaller();
         org.jboss.jca.fungal.deployment.Deployment deployment = 
            deploymentU.unmarshal(url);

         if (deployment != null && deployment.getBean().size() > 0)
         {
            for (BeanType bt : deployment.getBean())
            {
               kernel.setBeanStatus(bt.getName(), ServiceLifecycle.NOT_STARTED);
            }

            kernel.beansRegistered();

            List<BeanDeployer> deployers = new ArrayList<BeanDeployer>(deployment.getBean().size());
            List<String> beans = Collections.synchronizedList(new ArrayList<String>(deployment.getBean().size()));
            Map<String, List<Method>> uninstall = 
               new ConcurrentHashMap<String, List<Method>>(deployment.getBean().size());

            final CountDownLatch beansLatch = new CountDownLatch(deployment.getBean().size());

            for (BeanType bt : deployment.getBean())
            {
               BeanDeployer deployer = new BeanDeployer(bt, beans, uninstall, kernel, beansLatch, parent);
               deployers.add(deployer);

               Future<?> result = kernel.getExecutorService().submit(deployer);
            }

            beansLatch.await();

            Iterator<BeanDeployer> it = deployers.iterator();
            while (deployException == null && it.hasNext())
            {
               BeanDeployer deployer = it.next();
               if (deployer.getDeployException() != null)
                  deployException = deployer.getDeployException();
            }

            if (deployException == null)
               return new BeanDeployment(url, beans, uninstall, kernel);
         }
      }
      catch (Throwable t)
      {
         error(t.getMessage(), t);
         throw new DeployException("Deployment " + url + " failed", t);
      }

      if (deployException != null)
         throw new DeployException("Deployment " + url + " failed", deployException);

      return null;
   }

   /**
    * Bean deployer
    */
   static class BeanDeployer implements Runnable
   {
      /** The bean */
      private BeanType bt;

      /** The bean names */
      private List<String> beans;

      /** Uninstall methods */
      private Map<String, List<Method>> uninstall;

      /** The kernel */
      private KernelImpl kernel;

      /** The bean latch */
      private CountDownLatch beansLatch;

      /** The classloader */
      private ClassLoader classLoader;

      /** DeployException */
      private DeployException deployException;

      /**
       * Constructor
       * @param bt The bean
       * @param beans The list of bean names
       * @param uninstall Uninstall methods for beans
       * @param kernel The kernel
       * @param beansLatch The beans latch
       * @param classLoader The class loader
       */
      public BeanDeployer(BeanType bt, 
                          List<String> beans,
                          Map<String, List<Method>> uninstall,
                          KernelImpl kernel,
                          CountDownLatch beansLatch,
                          ClassLoader classLoader)
      {
         this.bt = bt;
         this.beans = beans;
         this.uninstall = uninstall;
         this.kernel = kernel;
         this.beansLatch = beansLatch;
         this.classLoader = classLoader;
         this.deployException = null;
      }

      /**
       * Run
       */
      public void run()
      {
         SecurityActions.setThreadContextClassLoader(classLoader);

         String beanName = bt.getName();
         try
         {
            if (kernel.getBean(beanName) == null)
            {
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

               kernel.setBeanStatus(beanName, ServiceLifecycle.STARTING);

               Object bean = createBean(bt, classLoader);

               kernel.addBean(beanName, bean); 
               beans.add(beanName);

               kernel.setBeanStatus(beanName, ServiceLifecycle.STARTED);
            }
            else
            {
               warn("Warning: A service with name " + beanName + " already exists");
            }
         }
         catch (Throwable t)
         {
            deployException = new DeployException("Installing bean " + beanName, t);
            kernel.setBeanStatus(beanName, ServiceLifecycle.ERROR);
            error("Installing bean " + beanName, t);
         }

         beansLatch.countDown();
      }

      /**
       * Get deploy exception
       * @return null if no error; otherwise the exception
       */
      public DeployException getDeployException()
      {
         return deployException;
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
         if (dts.size() > 0)
         {
            result = new HashSet<String>();
            for (DependsType dt : dts)
            {
               result.add(dt.getValue());
               kernel.addBeanDependants(bt.getName(), dt.getValue());
            }
         }

         List<PropertyType> pts = bt.getProperty();
         if (pts.size() > 0)
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
                  kernel.addBeanDependants(bt.getName(), it.getBean());
               }
            }
         }

         ConstructorType ct = bt.getConstructor();
         if (ct != null && ct.getFactory() != null)
         {
            if (result == null)
               result = new HashSet<String>();

            result.add(ct.getFactory().getBean());
            kernel.addBeanDependants(bt.getName(), ct.getFactory().getBean());
         }

         return result;
      }

      /**
       * Get the number of services that are not started yet
       * @paran dependencies The dependencies for a service
       * @return The number of not started services
       * @exception DeployException Thrown if an unknown dependency is found
       */
      private int getNotStarted(Set<String> dependencies) throws DeployException
      {
         if (dependencies == null || dependencies.size() == 0)
            return 0;

         int count = 0;
         for (String dependency : dependencies)
         {
            ServiceLifecycle dependencyStatus = kernel.getBeanStatus(dependency);

            if (dependencyStatus == null && kernel.isAllBeansRegistered())
               throw new DeployException("Unknown dependency: " + dependency);

            if (dependencyStatus == null || 
                (dependencyStatus != ServiceLifecycle.STARTED && dependencyStatus != ServiceLifecycle.ERROR))
               count += 1;
         }

         return count;
      }

      /**
       * Create a bean
       * @param bt The bean type definition
       * @param cl The classloader
       * @return The new bean
       * @exception Throwable Thrown if an error occurs
       */
      @SuppressWarnings("unchecked") 
      private Object createBean(BeanType bt, ClassLoader cl) throws Throwable
      {
         Class<?> clz = null;
         Object instance = null;

         if (bt.getClazz() != null && bt.getConstructor() == null)
         {
            clz = Class.forName(bt.getClazz(), true, cl);
            instance = clz.newInstance();
         }
         else
         {
            ConstructorType ct = bt.getConstructor();

            if (ct.getParameter().size() == 0 && ct.getFactory() == null)
            {
               Class factoryClass = Class.forName(ct.getFactoryClass(), true, cl);
               Method factoryMethod = factoryClass.getMethod(ct.getFactoryMethod(), (Class[])null);
               
               instance = factoryMethod.invoke((Object)null, (Object[])null);
               clz = instance.getClass();
            }
            else if (ct.getParameter().size() == 0 && ct.getFactory() != null)
            {
               Object factoryObject = kernel.getBean(ct.getFactory().getBean());
               Method factoryMethod = factoryObject.getClass().getMethod(ct.getFactoryMethod(), (Class[])null);

               instance = factoryMethod.invoke(factoryObject, (Object[])null);
               clz = instance.getClass();
            }
            else
            {
               if (bt.getClazz() != null && ct.getFactoryClass() == null)
               {
                  clz = Class.forName(bt.getClazz(), true, cl);
                  Constructor[] constructors = clz.getConstructors();
                  Constructor constructor = null;
                  Object[] args = null;

                  List<Constructor> candidates = new ArrayList<Constructor>();

                  for (Constructor c : constructors)
                  {
                     if (ct.getParameter().size() == c.getParameterTypes().length)
                     {
                        boolean include = true;
                        for (int i = 0; include && i < c.getParameterTypes().length; i++)
                        {
                           Class<?> parameterClass = c.getParameterTypes()[i];

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
                           candidates.add(c);
                     }
                  }

                  if (candidates.size() == 1)
                  {
                     constructor = candidates.get(0);
                     args = new Object[ct.getParameter().size()];
                     for (int i = 0; i < ct.getParameter().size(); i++)
                     {
                        args[i] = getValue(ct.getParameter().get(i).getValue(), 
                                           constructor.getParameterTypes()[i],
                                           cl);
                     }
                  }
                  else
                  {
                     boolean found = false;
                     Iterator<Constructor> it = candidates.iterator();
                     while (!found && it.hasNext())
                     {
                        try
                        {
                           Constructor c = it.next();
                           args = new Object[ct.getParameter().size()];

                           for (int i = 0; i < ct.getParameter().size(); i++)
                           {
                              args[i] = getValue(ct.getParameter().get(i).getValue(),
                                                 c.getParameterTypes()[i],
                                                 cl);
                           }

                           constructor = c;
                           found = true;
                        }
                        catch (Throwable t)
                        {
                           // ok - not this one...
                        }
                     }
                  }
               
                  instance = constructor.newInstance(args);
               }
               else
               {
                  Class factoryClass = Class.forName(ct.getFactoryClass(), true, cl);
                  Method[] factoryMethods = factoryClass.getMethods();
                  Method factoryMethod = null;
                  Object[] args = null;

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
                        args[i] = getValue(ct.getParameter().get(i).getValue(), 
                                           factoryMethod.getParameterTypes()[i],
                                           cl);
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
                              args[i] = getValue(ct.getParameter().get(i).getValue(),
                                                 factoryMethod.getParameterTypes()[i],
                                                 cl);
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

                  instance = factoryMethod.invoke((Object)null, args);
                  clz = instance.getClass();
               }
            }
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
         catch (NoSuchMethodException nsme)
         {
            // No create method
         }
         catch (InvocationTargetException ite)
         {
            throw ite.getTargetException();
         }

         try
         {
            Method startMethod = clz.getMethod("start", (Class[])null);
            startMethod.invoke(instance, (Object[])null);
         }
         catch (NoSuchMethodException nsme)
         {
            // No start method
         }
         catch (InvocationTargetException ite)
         {
            throw ite.getTargetException();
         }

         // Invoke install methods
         if (bt.getInstall() != null && bt.getInstall().size() > 0)
         {
            for (InstallType it : bt.getInstall())
            {
               try
               {
                  Method method = clz.getMethod(it.getMethod(), (Class[])null);
                  method.invoke(instance, (Object[])null);
               }
               catch (InvocationTargetException ite)
               {
                  throw ite.getTargetException();
               }
            }
         }

         // Register uninstall methods
         if (bt.getUninstall() != null && bt.getUninstall().size() > 0)
         {
            List<Method> methods = new ArrayList<Method>(bt.getUninstall().size());
            for (UninstallType ut : bt.getUninstall())
            {
               try
               {
                  Method method = clz.getMethod(ut.getMethod(), (Class[])null);
                  methods.add(method);
               }
               catch (NoSuchMethodException nsme)
               {
                  throw new Exception("Unknown uninstall method:" + ut.getMethod());
               }
            }
            uninstall.put(bt.getName(), methods);
         }

         // Register deployer
         if (instance instanceof Deployer)
         {
            ((MainDeployerImpl)kernel.getMainDeployer()).addDeployer((Deployer)instance);
         }

         return instance;
      }

      /**
       * Get a value from a string
       * @param s The string representation
       * @param clz The class
       * @param cl The class loader
       * @return The value
       * @exception Exception If the string cant be converted
       */
      private Object getValue(String s, Class<?> clz, ClassLoader cl) throws Exception
      {
         s = getSubstitutionValue(s);

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
         else if (clz.equals(Class.class))
         {
            return Class.forName(s, true, cl);
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
      @SuppressWarnings("unchecked") 
      private void setBeanProperty(Object instance, PropertyType pt, ClassLoader cl) throws Exception
      {
         String name = "set" + pt.getName().substring(0, 1).toUpperCase(Locale.US) + pt.getName().substring(1);
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
                     it.getProperty().substring(0, 1).toUpperCase(Locale.US) + it.getProperty().substring(1);
                  method = injectionObject.getClass().getMethod(getMethodName, (Class[])null);
               }
               catch (NoSuchMethodException nsme)
               {
                  String isMethodName = "is" + 
                     it.getProperty().substring(0, 1).toUpperCase(Locale.US) + it.getProperty().substring(1);
                  method = injectionObject.getClass().getMethod(isMethodName, (Class[])null);
               }

               parameterValue = method.invoke(injectionObject, (Object[])null);
            }
            else
            {
               parameterValue = injectionObject;
            }
         }
         else if (element instanceof MapType)
         {
            MapType mt = (MapType)element;

            Map map = null;
            
            if (mt.getClazz() == null)
            {
               map = new HashMap();
            }
            else
            {
               Class mapClass = Class.forName(mt.getClazz(), true, cl);
               map = (Map)mapClass.newInstance();
            }

            Class keyClass = Class.forName(mt.getKeyClass(), true, cl);
            Class valueClass = Class.forName(mt.getValueClass(), true, cl);

            for (EntryType et : mt.getEntry())
            {
               Object key = getValue(et.getKey().getValue(), keyClass, cl);
               Object value = getValue(et.getValue().getValue(), valueClass, cl);

               map.put(key, value);
            }

            parameterValue = map;
         }
         else if (element instanceof ListType)
         {
            ListType lt = (ListType)element;

            List list = null;
            
            if (lt.getClazz() == null)
            {
               list = new ArrayList();
            }
            else
            {
               Class listClass = Class.forName(lt.getClazz(), true, cl);
               list = (List)listClass.newInstance();
            }

            Class elementClass = Class.forName(lt.getElementClass(), true, cl);

            for (ValueType vt : lt.getValue())
            {
               Object value = getValue(vt.getValue(), elementClass, cl);
               list.add(value);
            }

            parameterValue = list;
         }
         else if (element instanceof SetType)
         {
            SetType st = (SetType)element;

            Set set = null;
            
            if (st.getClazz() == null)
            {
               set = new HashSet();
            }
            else
            {
               Class setClass = Class.forName(st.getClazz(), true, cl);
               set = (Set)setClass.newInstance();
            }

            Class elementClass = Class.forName(st.getElementClass(), true, cl);

            for (ValueType vt : st.getValue())
            {
               Object value = getValue(vt.getValue(), elementClass, cl);
               set.add(value);
            }

            parameterValue = set;
         }
         else if (element instanceof NullType)
         {
            parameterValue = null;
         }
         else if (element instanceof ThisType)
         {
            parameterValue = instance;
         }
         else if (element instanceof ValueType)
         {
            parameterValue = getValue(((ValueType)element).getValue(), parameterClass, cl);
         }
         else
         {
            parameterValue = getValue((String)element, parameterClass, cl);
         }

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

   /**
    * Clone
    * @return The copy of the object
    * @exception CloneNotSupportedException Thrown if a copy can't be created
    */
   public Deployer clone() throws CloneNotSupportedException
   {
      return new DeploymentDeployer(kernel);
   }
}
