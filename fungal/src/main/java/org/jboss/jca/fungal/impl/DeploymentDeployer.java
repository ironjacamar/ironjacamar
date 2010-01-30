/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.jca.fungal.deployment.IgnoreCreateType;
import org.jboss.jca.fungal.deployment.IgnoreDestroyType;
import org.jboss.jca.fungal.deployment.IgnoreStartType;
import org.jboss.jca.fungal.deployment.IgnoreStopType;
import org.jboss.jca.fungal.deployment.IncallbackType;
import org.jboss.jca.fungal.deployment.InjectType;
import org.jboss.jca.fungal.deployment.InstallType;
import org.jboss.jca.fungal.deployment.ListType;
import org.jboss.jca.fungal.deployment.MapType;
import org.jboss.jca.fungal.deployment.NullType;
import org.jboss.jca.fungal.deployment.ParameterType;
import org.jboss.jca.fungal.deployment.PropertyType;
import org.jboss.jca.fungal.deployment.SetType;
import org.jboss.jca.fungal.deployment.ThisType;
import org.jboss.jca.fungal.deployment.UncallbackType;
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
            Set<String> ignoreStops = Collections.synchronizedSet(new HashSet<String>(deployment.getBean().size()));
            Set<String> ignoreDestroys = Collections.synchronizedSet(new HashSet<String>(deployment.getBean().size()));

            final CountDownLatch beansLatch = new CountDownLatch(deployment.getBean().size());

            for (BeanType bt : deployment.getBean())
            {
               BeanDeployer deployer = new BeanDeployer(bt, beans, uninstall, ignoreStops, ignoreDestroys, kernel,
                                                        beansLatch, parent);
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
               return new BeanDeployment(url, beans, uninstall, ignoreStops, ignoreDestroys, kernel);
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
      /** Supported types by parameters/properties */
      private static final Set<Class<?>> SUPPORTED_TYPES = new HashSet<Class<?>>();

      /** The bean */
      private BeanType bt;

      /** The bean names */
      private List<String> beans;

      /** Uninstall methods */
      private Map<String, List<Method>> uninstall;

      /** Ignore stop */
      private Set<String> ignoreStops;

      /** Ignore destroy */
      private Set<String> ignoreDestroys;

      /** The kernel */
      private KernelImpl kernel;

      /** The bean latch */
      private CountDownLatch beansLatch;

      /** The classloader */
      private ClassLoader classLoader;

      /** DeployException */
      private DeployException deployException;

      static
      {
         SUPPORTED_TYPES.add(String.class);
         SUPPORTED_TYPES.add(byte.class);
         SUPPORTED_TYPES.add(Byte.class);
         SUPPORTED_TYPES.add(short.class);
         SUPPORTED_TYPES.add(Short.class);
         SUPPORTED_TYPES.add(int.class);
         SUPPORTED_TYPES.add(Integer.class);
         SUPPORTED_TYPES.add(long.class);
         SUPPORTED_TYPES.add(Long.class);
         SUPPORTED_TYPES.add(float.class);
         SUPPORTED_TYPES.add(Float.class);
         SUPPORTED_TYPES.add(double.class);
         SUPPORTED_TYPES.add(Double.class);
         SUPPORTED_TYPES.add(boolean.class);
         SUPPORTED_TYPES.add(Boolean.class);
         SUPPORTED_TYPES.add(char.class);
         SUPPORTED_TYPES.add(Character.class);
         SUPPORTED_TYPES.add(InetAddress.class);
         SUPPORTED_TYPES.add(Class.class);
      }

      /**
       * Constructor
       * @param bt The bean
       * @param beans The list of bean names
       * @param uninstall Uninstall methods for beans
       * @param ignoreStops Ignore stop methods for beans
       * @param ignoreDestroys Ignore destroy methods for beans
       * @param kernel The kernel
       * @param beansLatch The beans latch
       * @param classLoader The class loader
       */
      public BeanDeployer(BeanType bt, 
                          List<String> beans,
                          Map<String, List<Method>> uninstall,
                          Set<String> ignoreStops,
                          Set<String> ignoreDestroys,
                          KernelImpl kernel,
                          CountDownLatch beansLatch,
                          ClassLoader classLoader)
      {
         this.bt = bt;
         this.beans = beans;
         this.uninstall = uninstall;
         this.ignoreStops = ignoreStops;
         this.ignoreDestroys = ignoreDestroys;
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
            result = new HashSet<String>(dts.size());
            for (DependsType dt : dts)
            {
               result.add(dt.getValue());
               kernel.addBeanDependants(bt.getName(), dt.getValue());
            }
         }

         List<PropertyType> pts = bt.getProperty();
         if (pts.size() > 0)
         {
            for (PropertyType pt : pts)
            {
               Object element = pt.getContent().get(0);

               if (element != null && element instanceof InjectType)
               {
                  if (result == null)
                     result = new HashSet<String>(1);

                  InjectType it = (InjectType)element;
                  result.add(it.getBean());
                  kernel.addBeanDependants(bt.getName(), it.getBean());
               }
            }
         }

         ConstructorType ct = bt.getConstructor();
         if (ct != null)
         {
            if (ct.getFactory() != null)
            {
               if (result == null)
                  result = new HashSet<String>(1);

               result.add(ct.getFactory().getBean());
               kernel.addBeanDependants(bt.getName(), ct.getFactory().getBean());
            }
            
            if (ct.getParameter() != null && ct.getParameter().size() > 0)
            {
               for (ParameterType pt : ct.getParameter())
               {
                  Object v = pt.getContent().get(0);
                  if (v instanceof InjectType)
                  {
                     if (result == null)
                        result = new HashSet<String>(1);

                     InjectType it = (InjectType)v;
                     result.add(it.getBean());
                     kernel.addBeanDependants(bt.getName(), it.getBean());
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

            Object factoryObject = null;
            Class<?> factoryClass = null;

            if (ct.getFactory() != null)
            {
               factoryObject = kernel.getBean(ct.getFactory().getBean());
               factoryClass = factoryObject.getClass();
            }
            else
            {
               String fcs = ct.getFactoryClass();

               if (fcs == null)
                  fcs = bt.getClazz();

               factoryClass = Class.forName(fcs, true, cl);
            }

            if (ct.getFactoryMethod() == null)
            {
               if (ct.getParameter() == null || ct.getParameter().size() == 0)
               {
                  instance = factoryClass.newInstance();
                  clz = instance.getClass();
               }
               else
               {
                  Constructor factoryConstructor = findConstructor(factoryClass, ct.getParameter(), cl);
                  Object[] args = getArguments(ct.getParameter(), factoryConstructor.getParameterTypes(), cl);

                  instance = factoryConstructor.newInstance(args);
                  clz = instance.getClass();
               }
            }
            else
            {
               Method factoryMethod = findMethod(factoryClass, ct.getFactoryMethod(), ct.getParameter(), cl);

               if (ct.getParameter() == null || ct.getParameter().size() == 0)
               {
                  instance = factoryMethod.invoke(factoryObject, (Object[])null);
                  clz = instance.getClass();
               }
               else
               {
                  Object[] args = getArguments(ct.getParameter(), factoryMethod.getParameterTypes(), cl);
                  instance = factoryMethod.invoke(factoryObject, args);
                  clz = instance.getClass();
               }
            }
         }

         // Bean properties
         if (bt.getProperty() != null)
         {
            for (PropertyType pt : bt.getProperty())
            {
               setBeanProperty(instance, pt, cl);
            }
         }

         if (bt.getIgnoreCreate() == null)
         {
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
         }

         if (bt.getIgnoreStart() == null)
         {
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
         }

         if (bt.getIgnoreStop() != null)
            ignoreStops.add(bt.getName());

         if (bt.getIgnoreDestroy() != null)
            ignoreDestroys.add(bt.getName());

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

         // Register incallback methods
         if (bt.getIncallback() != null && bt.getIncallback().size() > 0)
         {
            for (IncallbackType it : bt.getIncallback())
            {
               List<Method> candidates = new ArrayList<Method>(1);
               Method[] methods = clz.getMethods();

               for (Method m : methods)
               {
                  if (m.getName().equals(it.getMethod()) && m.getParameterTypes().length == 1)
                     candidates.add(m);
               }

               if (candidates.size() > 0)
               {
                  Method method = candidates.get(0);
                  Class<?> parameter = method.getParameterTypes()[0];

                  Callback cb = new Callback(parameter, method, instance);

                  kernel.registerIncallback(cb);
               }
            }
         }

         // Register uncallback methods
         if (bt.getUncallback() != null && bt.getUncallback().size() > 0)
         {
            for (UncallbackType ut : bt.getUncallback())
            {
               List<Method> candidates = new ArrayList<Method>(1);
               Method[] methods = clz.getMethods();

               for (Method m : methods)
               {
                  if (m.getName().equals(ut.getMethod()) && m.getParameterTypes().length == 1)
                     candidates.add(m);
               }

               if (candidates.size() > 0)
               {
                  Method method = candidates.get(0);
                  Class<?> parameter = method.getParameterTypes()[0];

                  Callback cb = new Callback(parameter, method, instance);

                  kernel.registerUncallback(cb);
               }
            }
         }

         // Register deployer
         if (instance instanceof Deployer)
         {
            ((MainDeployerImpl)kernel.getMainDeployer()).addDeployer((Deployer)instance);
         }

         return instance;
      }

      /**
       * Find constructor
       * @param clz The class
       * @param parameters The list of parameters
       * @param cl The class loader
       * @return The constructor
       * @exception Throwable Thrown if a constructor cannot be found
       */
      @SuppressWarnings("unchecked") 
      private Constructor findConstructor(Class clz, List<ParameterType> parameters, ClassLoader cl)
         throws Throwable
      {
         if (parameters == null || parameters.size() == 0)
         {
            return clz.getConstructor((Class<?>)null);
         }
         else
         {
            Constructor[] constructors = clz.getConstructors();

            for (Constructor c : constructors)
            {
               if (parameters.size() == c.getParameterTypes().length)
               {
                  boolean include = true;

                  for (int i = 0; include && i < parameters.size(); i++)
                  {
                     ParameterType pt = parameters.get(i);
                     Class<?> parameterClass = c.getParameterTypes()[i];

                     if (pt.getClazz() == null)
                     {
                        if ((!(pt.getContent().get(0) instanceof InjectType)) &&
                            (!(pt.getContent().get(0) instanceof NullType)))
                           if (!SUPPORTED_TYPES.contains(parameterClass))
                              include = false;
                     }
                     else
                     {
                        Class<?> pClz = Class.forName(pt.getClazz(), true, cl);

                        if (!parameterClass.equals(pClz))
                           include = false;
                     }
                  }

                  if (include)
                     return c;
               }
            }
         }

         throw new Exception("Unable to find constructor for " + clz.getName());
      }

      /**
       * Find method
       * @param clz The class
       * @param name The method name
       * @param parameters The list of parameters
       * @param cl The class loader
       * @return The constructor
       * @exception Throwable Thrown if a constructor cannot be found
       */
      @SuppressWarnings("unchecked") 
      private Method findMethod(Class clz, String name, List<ParameterType> parameters, ClassLoader cl)
         throws Throwable
      {
         if (parameters == null || parameters.size() == 0)
         {
            return clz.getMethod(name, (Class<?>[])null);
         }
         else
         {
            Method[] methods = clz.getMethods();

            for (Method m : methods)
            {
               if (m.getName().equals(name))
               {
                  if (parameters.size() == m.getParameterTypes().length)
                  {
                     boolean include = true;

                     for (int i = 0; include && i < parameters.size(); i++)
                     {
                        ParameterType pt = parameters.get(i);
                        Class<?> parameterClass = m.getParameterTypes()[i];

                        if (pt.getClazz() == null)
                        {
                           if ((!(pt.getContent().get(0) instanceof InjectType)) &&
                               (!(pt.getContent().get(0) instanceof NullType)))
                              if (!SUPPORTED_TYPES.contains(parameterClass))
                                 include = false;
                        }
                        else
                        {
                           Class<?> pClz = Class.forName(pt.getClazz(), true, cl);
                           
                           if (!parameterClass.equals(pClz))
                              include = false;
                        }
                     }

                     if (include)
                        return m;
                  }
               }
            }
         }

         throw new Exception("Unable to find method (" + name + "[" + parameters + "]) in " + clz.getName());
      }

      /**
       * Get the argument values
       * @param definitions The argument definitions
       * @param types The argument types
       * @param cl The class loader
       * @return The values
       * @exception Throwable Thrown if an error occurs
       */
      private Object[] getArguments(List<ParameterType> definitions, Class<?>[] types, ClassLoader cl)
         throws Throwable
      {
         if (definitions == null || definitions.size() == 0)
            return null;

         Object[] args = new Object[types.length];

         for (int i = 0; i < definitions.size(); i++)
         {
            ParameterType parameter = definitions.get(i);

            Object v = parameter.getContent().get(0);

            if (v instanceof InjectType)
            {
               args[i] = getInjectValue((InjectType)v);
            }
            else if (v instanceof NullType)
            {
               args[i] = null;
            }
            else
            {
               args[i] = getValue((String)v, types[i], cl);
            }
         }

         return args;
      }

      /**
       * Get inject value
       * @param it The inject type
       * @return The value
       * @exception Exception If the injection bean cannot be resolved or if an error occurs
       */
      private Object getInjectValue(InjectType it) throws Exception
      {
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

            return method.invoke(injectionObject, (Object[])null);
         }
         else
         {
            return injectionObject;
         }
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
            parameterValue = getInjectValue((InjectType)element);
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
