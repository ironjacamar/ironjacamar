/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.embedded.junit4;

import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.Embedded;
import org.ironjacamar.embedded.EmbeddedFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * IronJacamar JUnit 4 runner
 */
public class IronJacamar extends BlockJUnit4ClassRunner
{
   /** Embedded instance */
   private Embedded embedded;
   
   /** Static deployments */
   private List<Object> staticDeployments;
   
   /** Deployments */
   private List<Object> deployments;
   
   /**
    * Constructor
    * @param clz The class
    * @exception InitializationError If the test can't be initiazed
    */
   public IronJacamar(Class<?> clz) throws InitializationError
   {
      super(clz);
      this.embedded = null;
      this.staticDeployments = new ArrayList<>();
      this.deployments = new ArrayList<>();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected Statement withBefores(final FrameworkMethod method, final Object target, final Statement statement)
   {
      final Statement befores = super.withBefores(method, target, new NoopStatement());
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            TestClass tc = getTestClass();

            // Non-static @Deployment
            List<FrameworkMethod> fms = tc.getAnnotatedMethods(Deployment.class);
            if (fms != null && !fms.isEmpty())
            {
               Collection<FrameworkMethod> filtered = filterAndSort(fms, false);
               for (FrameworkMethod fm : filtered)
               {
                  SecurityActions.setAccessible(fm.getMethod());

                  Class<?> returnType = fm.getReturnType();
                  if (URL.class.isAssignableFrom(returnType))
                  {
                     Object[] parameters = getParameters(fm);
                     URL result = (URL)fm.invokeExplosively(target, parameters);
                     embedded.deploy(result);
                     deployments.add(result);
                  }
                  else if (ResourceAdapterArchive.class.isAssignableFrom(returnType))
                  {
                     Object[] parameters = getParameters(fm);
                     ResourceAdapterArchive result = (ResourceAdapterArchive)fm.invokeExplosively(target, parameters);
                     embedded.deploy(result);
                     deployments.add(result);
                  }
                  else if (Descriptor.class.isAssignableFrom(returnType))
                  {
                     Object[] parameters = getParameters(fm);
                     Descriptor result = (Descriptor)fm.invokeExplosively(target, parameters);
                     embedded.deploy(result);
                     deployments.add(result);
                  }
                  else
                  {
                     throw new Exception("Unsupported deployment type: " + returnType.getName());
                  }
               }
            }

            // Non-static @Inject / @Named
            List<FrameworkField> fields = tc.getAnnotatedFields(javax.inject.Inject.class);
            if (fields != null && !fields.isEmpty())
            {
               for (FrameworkField f : fields)
               {
                  if (!Modifier.isStatic(f.getField().getModifiers()))
                  {
                     SecurityActions.setAccessible(f.getField());
                     if (Embedded.class.equals(f.getType()))
                     {
                        f.getField().set(target, embedded);
                     }
                     else
                     {
                        javax.inject.Named name = f.getAnnotation(javax.inject.Named.class);
                        if (name != null && name.value() != null)
                        {
                           Object value = embedded.lookup(name.value(), f.getType());
                           f.getField().set(target, value);
                        }
                        else
                        {
                           Object value = embedded.lookup(f.getType().getSimpleName(), f.getType());
                           f.getField().set(target, value);
                        }
                     }
                  }
               }
            }

            // Non-static @Resource
            fields = tc.getAnnotatedFields(Resource.class);
            if (fields != null && !fields.isEmpty())
            {
               Context context = createContext();
               for (FrameworkField f : fields)
               {
                  SecurityActions.setAccessible(f.getField());
                  if (!Modifier.isStatic(f.getField().getModifiers()))
                  {
                     Resource resource = (Resource)f.getAnnotation(Resource.class);
                     String name = null;
                     
                     if (resource.mappedName() != null)
                     {
                        name = resource.mappedName();
                     }
                     else if (resource.name() != null)
                     {
                        name = resource.name();
                     }
                     else if (resource.lookup() != null)
                     {
                        name = resource.lookup();
                     }
              
                     f.getField().set(target, context.lookup(name));
                  }
               }
               context.close();
            }
        
            befores.evaluate();
            statement.evaluate();
         }
      };
   }
  
   /**
    * {@inheritDoc}
    */
   @Override
   protected Statement withAfters(final FrameworkMethod method, final Object target, final Statement statement)
   {
      final Statement afters = super.withAfters(method, target, new NoopStatement());
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            statement.evaluate();
            afters.evaluate();

            TestClass tc = getTestClass();
            
            // Non-static @Resource
            List<FrameworkField> fields = tc.getAnnotatedFields(Resource.class);
            if (fields != null && !fields.isEmpty())
            {
               for (FrameworkField f : fields)
               {
                  SecurityActions.setAccessible(f.getField());
                  if (!Modifier.isStatic(f.getField().getModifiers()) && !f.getField().getDeclaringClass().isPrimitive())
                  {
                     f.getField().set(target, null);
                  }
               }
            }

            // Non-static @Inject / @Named
            fields = tc.getAnnotatedFields(javax.inject.Inject.class);
            if (fields != null && !fields.isEmpty())
            {
               for (FrameworkField f : fields)
               {
                  SecurityActions.setAccessible(f.getField());
                  if (!Modifier.isStatic(f.getField().getModifiers()) && !f.getField().getDeclaringClass().isPrimitive())
                  {
                     f.getField().set(target, null);
                  }
               }
            }

            // Non-static @Deployment
            if (!deployments.isEmpty())
            {
               for (int i = deployments.size() - 1; i >= 0; i--)
               {
                  Object deployment = deployments.get(i);
                  if (deployment instanceof URL)
                  {
                     embedded.undeploy((URL)deployment);
                  }
                  else if (deployment instanceof ResourceAdapterArchive)
                  {
                     embedded.undeploy((ResourceAdapterArchive)deployment);
                  }
                  else if (deployment instanceof Descriptor)
                  {
                     embedded.undeploy((Descriptor)deployment);
                  }
               }
            }
            deployments.clear();
         }
      };
   }
  
   /**
    * {@inheritDoc}
    */
   @Override
   protected Statement withBeforeClasses(final Statement statement)
   {
      final Statement beforeClasses = super.withBeforeClasses(new NoopStatement());
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            TestClass tc = getTestClass();

            boolean fullProfile = true;

            Configuration configuration = tc.getAnnotation(Configuration.class);
            if (configuration != null)
               fullProfile = configuration.full();

            extensionStart(tc);
            
            embedded = EmbeddedFactory.create(fullProfile);
            embedded.startup();

            Initializer initializer = tc.getAnnotation(Initializer.class);
            if (initializer != null && initializer.clazz() != null)
            {
               try
               {
                  Class<? extends Beans> iClz = initializer.clazz();
                  Beans bC = iClz.newInstance();
                  bC.execute(new EmbeddedJCAResolver(embedded));
               }
               catch (Exception e)
               {
                  throw new Exception("Initializer error from: " + initializer.clazz(), e);
               }
            }
            
            PreCondition preCondition = tc.getAnnotation(PreCondition.class);
            if (preCondition != null && preCondition.condition() != null)
            {
               try
               {
                  Class<? extends Condition> pCClz = preCondition.condition();
                  Condition pC = pCClz.newInstance();
                  pC.verify(new EmbeddedJCAResolver(embedded));
               }
               catch (Exception e)
               {
                  throw new ConditionException("PreCondition error from: " + preCondition.condition(), e);
               }
            }
            
            // Static @Deployment
            List<FrameworkMethod> fms = tc.getAnnotatedMethods(Deployment.class);
            if (fms != null && !fms.isEmpty())
            {
               Collection<FrameworkMethod> filtered = filterAndSort(fms, true);
               for (FrameworkMethod fm : filtered)
               {
                  SecurityActions.setAccessible(fm.getMethod());

                  Class<?> returnType = fm.getReturnType();
                  if (URL.class.isAssignableFrom(returnType))
                  {
                     Object[] parameters = getParameters(fm);
                     URL result = (URL)fm.invokeExplosively(null, parameters);
                     embedded.deploy(result);
                     staticDeployments.add(result);
                  }
                  else if (ResourceAdapterArchive.class.isAssignableFrom(returnType))
                  {
                     Object[] parameters = getParameters(fm);
                     ResourceAdapterArchive result = (ResourceAdapterArchive)fm.invokeExplosively(null, parameters);
                     embedded.deploy(result);
                     staticDeployments.add(result);
                  }
                  else if (Descriptor.class.isAssignableFrom(returnType))
                  {
                     Object[] parameters = getParameters(fm);
                     Descriptor result = (Descriptor)fm.invokeExplosively(null, parameters);
                     embedded.deploy(result);
                     staticDeployments.add(result);
                  }
                  else
                  {
                     throw new Exception("Unsupported deployment type: " + returnType.getName());
                  }
               }
            }

            // Static @Inject / @Named
            List<FrameworkField> fields = tc.getAnnotatedFields(javax.inject.Inject.class);
            if (fields != null && !fields.isEmpty())
            {
               for (FrameworkField f : fields)
               {
                  if (Modifier.isStatic(f.getField().getModifiers()))
                  {
                     SecurityActions.setAccessible(f.getField());
                     if (Embedded.class.equals(f.getType()))
                     {
                        f.getField().set(null, embedded);
                     }
                     else
                     {
                        javax.inject.Named name = f.getAnnotation(javax.inject.Named.class);
                        if (name != null && name.value() != null)
                        {
                           Object value = embedded.lookup(name.value(), f.getType());
                           f.getField().set(null, value);
                        }
                        else
                        {
                           Object value = embedded.lookup(f.getType().getSimpleName(), f.getType());
                           f.getField().set(null, value);
                        }
                     }
                  }
               }
            }

            // Static @Resource
            fields = tc.getAnnotatedFields(Resource.class);
            if (fields != null && !fields.isEmpty())
            {
               Context context = createContext();
               for (FrameworkField f : fields)
               {
                  SecurityActions.setAccessible(f.getField());
                  if (Modifier.isStatic(f.getField().getModifiers()))
                  {
                     Resource resource = (Resource)f.getAnnotation(Resource.class);
                     String name = null;

                     if (resource.mappedName() != null)
                     {
                        name = resource.mappedName();
                     }
                     else if (resource.name() != null)
                     {
                        name = resource.name();
                     }
                     else if (resource.lookup() != null)
                     {
                        name = resource.lookup();
                     }
              
                     f.getField().set(null, context.lookup(name));
                  }
               }
               context.close();
            }
        
            beforeClasses.evaluate();
            statement.evaluate();
         }
      };
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected Statement withAfterClasses(final Statement statement)
   {
      final Statement afterClasses = super.withAfterClasses(new NoopStatement());
      return new Statement() 
      {
         @Override
         public void evaluate() throws Throwable
         {
            statement.evaluate();
            afterClasses.evaluate();

            TestClass tc = getTestClass();

            // Static @Resource
            List<FrameworkField> fields = tc.getAnnotatedFields(Resource.class);
            if (fields != null && !fields.isEmpty())
            {
               for (FrameworkField f : fields)
               {
                  SecurityActions.setAccessible(f.getField());
                  if (Modifier.isStatic(f.getField().getModifiers()) && !f.getField().getDeclaringClass().isPrimitive())
                  {
                     f.getField().set(null, null);
                  }
               }
            }

            // Static @Inject / @Named
            fields = tc.getAnnotatedFields(javax.inject.Inject.class);
            if (fields != null && !fields.isEmpty())
            {
               for (FrameworkField f : fields)
               {
                  SecurityActions.setAccessible(f.getField());
                  if (Modifier.isStatic(f.getField().getModifiers()) && !f.getField().getDeclaringClass().isPrimitive())
                  {
                     f.getField().set(null, null);
                  }
               }
            }

            // Static @Deployment
            if (!staticDeployments.isEmpty())
            {
               for (int i = staticDeployments.size() - 1; i >= 0; i--)
               {
                  Object deployment = staticDeployments.get(i);
                  if (deployment instanceof URL)
                  {
                     embedded.undeploy((URL)deployment);
                  }
                  else if (deployment instanceof ResourceAdapterArchive)
                  {
                     embedded.undeploy((ResourceAdapterArchive)deployment);
                  }
                  else if (deployment instanceof Descriptor)
                  {
                     embedded.undeploy((Descriptor)deployment);
                  }
               }
            }
            staticDeployments.clear();

            PostCondition postCondition = tc.getAnnotation(PostCondition.class);
            if (postCondition != null && postCondition.condition() != null)
            {
               try
               {
                  Class<? extends Condition> pCClz = postCondition.condition();
                  Condition pC = pCClz.newInstance();
                  pC.verify(new EmbeddedJCAResolver(embedded));
               }
               catch (Exception e)
               {
                  throw new ConditionException("PostCondition error from: " + postCondition.condition(), e);
               }
            }
            
            Finalizer finalizer = tc.getAnnotation(Finalizer.class);
            if (finalizer != null && finalizer.clazz() != null)
            {
               try
               {
                  Class<? extends Beans> fClz = finalizer.clazz();
                  Beans fC = fClz.newInstance();
                  fC.execute(new EmbeddedJCAResolver(embedded));
               }
               catch (Exception e)
               {
                  throw new Exception("Finalizer error from: " + finalizer.clazz(), e);
               }
            }
            
            embedded.shutdown();
            embedded = null;

            extensionStop(tc);
         }
      };
   }

   /**
    * Extension start
    * @param tc The test class
    * @exception Exception Thrown in case of an error
    */
   public void extensionStart(TestClass tc) throws Exception
   {
   }
   
   /**
    * Extension stop
    * @param tc The test class
    * @exception Exception Thrown in case of an error
    */
   public void extensionStop(TestClass tc) throws Exception
   {
   }
   
   /**
    * Filter and sort
    * @param fms The FrameworkMethods
    * @param isStatic Filter static
    * @return The filtered and sorted FrameworkMethods
    * @exception Exception If an order definition is incorrect
    */
   private Collection<FrameworkMethod> filterAndSort(List<FrameworkMethod> fms, boolean isStatic) throws Exception
   {
      SortedMap<Integer, FrameworkMethod> m = new TreeMap<>();

      for (FrameworkMethod fm : fms)
      {
         SecurityActions.setAccessible(fm.getMethod());

         if (Modifier.isStatic(fm.getMethod().getModifiers()) == isStatic)
         {
            Deployment deployment = (Deployment)fm.getAnnotation(Deployment.class);
            int order = deployment.order();

            if (order <= 0 || m.containsKey(Integer.valueOf(order)))
               throw new Exception("Incorrect order definition '" + order + "' on " +
                                   fm.getDeclaringClass().getName() + "#" + fm.getName());
            
            m.put(Integer.valueOf(order), fm);
         }
      }

      return m.values();
   }

  
   /**
    * Get parameter values for a method
    * @param fm The FrameworkMethod
    * @return The resolved parameters
    */
   private Object[] getParameters(FrameworkMethod fm)
   {
      Method m = fm.getMethod();
      SecurityActions.setAccessible(m);
      
      Class<?>[] parameters = m.getParameterTypes();
      Annotation[][] parameterAnnotations = m.getParameterAnnotations();
      Object[] result = new Object[parameters.length];

      for (int i = 0; i < parameters.length; i++)
      {
         Annotation[] parameterAnnotation = parameterAnnotations[i];
         boolean inject = false;
         String name = null;

         for (int j = 0; j < parameterAnnotation.length; j++)
         {
            Annotation a = parameterAnnotation[j];
            if (javax.inject.Inject.class.equals(a.annotationType()))
            {
               inject = true;
            }
            else if (javax.inject.Named.class.equals(a.annotationType()))
            {
               name = ((javax.inject.Named)a).value();
            }
         }

         if (inject)
         {
            result[i] = resolveBean(name != null ? name : parameters[i].getSimpleName(), parameters[i]);
         }
         else
         {
            result[i] = null;
         }
      }
      
      return result;
   }
   
   /**
    * Resolve a bean
    * @param name The name
    * @param type The type
    * @return The value
    */
   private Object resolveBean(String name, Class<?> type)
   {
      try
      {
         return embedded.lookup(name, type);
      }
      catch (Throwable t)
      {
         return null;
      }
   }
  
   /**
    * Create a context
    * @return The context
    * @exception Exception Thrown if an error occurs
    */
   private Context createContext() throws Exception
   {
      Properties properties = new Properties();
      properties.setProperty("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      properties.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      return new InitialContext(properties);
   }

   /**
    * Do nothing
    */
   private static class NoopStatement extends Statement
   {
      /**
       * {@inheritDoc}
       */
      @Override
      public void evaluate() throws Throwable
      {
      }
   }
}
