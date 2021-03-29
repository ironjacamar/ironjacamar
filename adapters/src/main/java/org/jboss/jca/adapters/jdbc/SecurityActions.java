/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * Privileged Blocks
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class SecurityActions
{
   /**
    * Get the classloader.
    * @param c The class
    * @return The classloader
    */
   static ClassLoader getClassLoader(final Class<?> c)
   {
      if (System.getSecurityManager() == null)
         return c.getClassLoader();

      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return c.getClassLoader();
         }
      });
   }

   static ClassLoader getThreadContextClassLoader()
   {
      if (System.getSecurityManager() == null)
         return Thread.currentThread().getContextClassLoader();

      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return Thread.currentThread().getContextClassLoader();
         }
      });
   }

   /**
    * Set the context classloader.
    * @param cl classloader
    */
   static void setThreadContextClassLoader(final ClassLoader cl)
   {
      if (System.getSecurityManager() == null)
      {
         Thread.currentThread().setContextClassLoader(cl);
      }
      else
      {
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               Thread.currentThread().setContextClassLoader(cl);

               return null;
            }
         });
      }
   }

   static <T> T executeInTccl(ClassLoader classLoader, Producer<T> producer) throws Exception
   {
      ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
      try {
         SecurityActions.setThreadContextClassLoader(classLoader);
         return producer.produce();
      } finally {
         SecurityActions.setThreadContextClassLoader(tccl);
      }
   }

   /**
    * Get a system property
    * @param name The property name
    * @return The property value
    */
   static String getSystemProperty(final String name)
   {
      if (System.getSecurityManager() == null)
         return System.getProperty(name);

      return AccessController.doPrivileged(new PrivilegedAction<String>() 
      {
         public String run()
         {
            return System.getProperty(name);
         }
      });
   }

   /**
    * Get stack trace
    * @param t The thread
    * @return The trace
    */
   static StackTraceElement[] getStackTrace(final Thread t)
   {
      if (System.getSecurityManager() == null)
         return t.getStackTrace();

      return AccessController.doPrivileged(new PrivilegedAction<StackTraceElement[]>() 
      {
         public StackTraceElement[] run()
         {
            return t.getStackTrace();
         }
      });
   }

   /**
    * Get the void return no arguments signature MethodHandle
    * @param c The class
    * @param name Method name
    * @return The method Handle or, null if the virtual method does not exist
    */
   static MethodHandle getMethodHandle(final Class<?> c, final String name)
   {
      if (System.getSecurityManager() == null)
      {
         MethodHandles.Lookup lookup = publicLookup();
         MethodType type = methodType(void.class);
         try
         {
            return lookup.findVirtual(c, name, type);
         } catch (NoSuchMethodException|IllegalAccessException e)
         {
            return null;
         }
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<MethodHandle>()
         {
            public MethodHandle run()
            {
               try
               {
                  MethodHandles.Lookup lookup = publicLookup();
                  MethodType type = methodType(void.class);
                  return lookup.findVirtual(c, name, type);
               } catch (NoSuchMethodException|IllegalAccessException e)
               {
                  return null;
               }
            }
         });
      }
   }

   static interface Producer<T> {
      T produce() throws Exception;
   }
}
