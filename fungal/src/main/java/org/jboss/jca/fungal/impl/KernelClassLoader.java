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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Kernel class loader
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class KernelClassLoader extends URLClassLoader
{
   /** Simple types */
   private static ConcurrentMap<String, Class<?>> simpleTypes = new ConcurrentHashMap<String, Class<?>>(9);

   static
   {
      simpleTypes.put(void.class.getName(), void.class);
      simpleTypes.put(byte.class.getName(), byte.class);
      simpleTypes.put(short.class.getName(), short.class);
      simpleTypes.put(int.class.getName(), int.class);
      simpleTypes.put(long.class.getName(), long.class);
      simpleTypes.put(char.class.getName(), char.class);
      simpleTypes.put(boolean.class.getName(), boolean.class);
      simpleTypes.put(float.class.getName(), float.class);
      simpleTypes.put(double.class.getName(), double.class);
   }

   /**
    * Constructor
    * @param urls The URLs for JAR archives or directories
    * @param parent The parent class loader
    */
   public KernelClassLoader(URL[] urls, ClassLoader parent)
   {
      super(urls, parent);
   }
   
   /**
    * Load a class
    * @param name The fully qualified class name
    * @return The class
    * @throws ClassNotFoundException If the class could not be found 
    */
   public synchronized Class<?> loadClass(String name) throws ClassNotFoundException
   {
      Class<?> result = simpleTypes.get(name);
      if (result != null)
         return result;

      return super.loadClass(name);
   }
}
