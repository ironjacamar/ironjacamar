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

import org.jboss.jca.fungal.deployers.Deployment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A bean deployment for JCA/Fungal
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class BeanDeployment implements Deployment
{
   /** The deployment */
   private URL deployment;

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

   /**
    * Constructor
    * @param deployment The deployment
    * @param beans The list of bean names for the deployment
    * @param uninstall Uninstall methods for beans
    * @param ignoreStops Ignore stop methods for beans
    * @param ignoreDestroys Ignore destroy methods for beans
    * @param kernel The kernel
    */
   public BeanDeployment(URL deployment, 
                         List<String> beans, 
                         Map<String, List<Method>> uninstall,
                         Set<String> ignoreStops,
                         Set<String> ignoreDestroys,
                         KernelImpl kernel)
   {
      if (deployment == null)
         throw new IllegalArgumentException("Deployment is null");

      if (beans == null)
         throw new IllegalArgumentException("Beans is null");

      if (uninstall == null)
         throw new IllegalArgumentException("Uninstall is null");

      if (kernel == null)
         throw new IllegalArgumentException("Kernel is null");

      this.deployment = deployment;
      this.beans = beans;
      this.uninstall = uninstall;
      this.ignoreStops = ignoreStops;
      this.ignoreDestroys = ignoreDestroys;
      this.kernel = kernel;
   }

   /**
    * Get the unique URL for the deployment
    * @return The URL
    */
   public URL getURL()
   {
      return deployment;
   }

   /**
    * Get the classloader
    * @return The classloader
    */
   public ClassLoader getClassLoader()
   {
      return null;
   }

   /**
    * Stop
    * @exception Throwable If the unit cant be stopped
    */
   public void stop() throws Throwable
   {
      Set<String> remaining = new HashSet<String>();
      remaining.addAll(beans);

      for (String bean : beans)
      {
         Set<String> dependants = kernel.getBeanDependants(bean);

         if (dependants != null)
         {
            for (String dependant : dependants)
            {
               remaining.remove(dependant);
            }
         }

         remaining.remove(bean);
      }

      if (remaining.size() > 0)
         throw new Exception("Cannot stop deployment " + deployment + " due to remaining dependants " + remaining);
   }

   /**
    * Destroy
    * @exception Throwable If the unit cant be stopped
    */
   public void destroy() throws Throwable
   {
      List<String> shutdownBeans = new LinkedList<String>(beans);
      Collections.reverse(shutdownBeans);

      for (String name : shutdownBeans)
      {
         kernel.setBeanStatus(name, ServiceLifecycle.STOPPING);

         Object bean = kernel.getBean(name);

         List<Method> l = uninstall.get(name);
         if (l != null)
         {
            for (Method m : l)
            {
               try
               {
                  m.invoke(bean, (Object[])null);
               }
               catch (InvocationTargetException ite)
               {
                  throw ite.getTargetException();
               }
            }
         }

         if (ignoreStops == null || !ignoreStops.contains(name))
         {
            try
            {
               Method stopMethod = bean.getClass().getMethod("stop", (Class[])null);
               stopMethod.invoke(bean, (Object[])null);
            }
            catch (NoSuchMethodException nsme)
            {
               // No stop method
            }
            catch (InvocationTargetException ite)
            {
               throw ite.getTargetException();
            }
         }

         if (ignoreDestroys == null || !ignoreDestroys.contains(name))
         {
            try
            {
               Method destroyMethod = bean.getClass().getMethod("destroy", (Class[])null);
               destroyMethod.invoke(bean, (Object[])null);
            }
            catch (NoSuchMethodException nsme)
            {
               // No destroy method
            }
            catch (InvocationTargetException ite)
            {
               throw ite.getTargetException();
            }
         }

         kernel.removeBean(name);
      }
   }
}
