/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.sjc;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * Privileged Blocks
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class SecurityActions
{ 
   /**
    * Constructor
    */
   private SecurityActions()
   {
   }

   /**
    * Get the thread context class loader
    * @return The class loader
    */
   static ClassLoader getThreadContextClassLoader()
   {
      return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction<Object>() 
      {
         public Object run()
         {
            return Thread.currentThread().getContextClassLoader();
         }
      });
   }

   /**
    * Set the thread context class loader
    * @param cl The class loader
    */
   static void setThreadContextClassLoader(final ClassLoader cl)
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

   /**
    * Get the system properties
    * @return The properties
    */
   static Properties getSystemProperties()
   {
      return (Properties)AccessController.doPrivileged(new PrivilegedAction<Object>() 
      {
         public Object run()
         {
            return System.getProperties();
         }
      });
   }

   /**
    * Get a system property
    * @param name The property name
    * @return The property value
    */
   static String getSystemProperty(final String name)
   {
      return (String)AccessController.doPrivileged(new PrivilegedAction<Object>() 
      {
         public Object run()
         {
            return System.getProperty(name);
         }
      });
   }

   /**
    * Set a system property
    * @param name The property name
    * @param value The property value
    */
   static void setSystemProperty(final String name, final String value)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>() 
      {
         public Object run()
         {
            System.setProperty(name, value);
            return null;
         }
      });
   }

   /**
    * Create an URLClassLoader
    * @param urls The urls
    * @param parent The parent class loader
    * @return The class loader
    */
   static URLClassLoader createURLCLassLoader(final URL[] urls, final ClassLoader parent)
   {
      return (URLClassLoader)AccessController.doPrivileged(new PrivilegedAction<Object>() 
      {
         public Object run()
         {
            return new URLClassLoader(urls, parent);
         }
      });
   }
}
