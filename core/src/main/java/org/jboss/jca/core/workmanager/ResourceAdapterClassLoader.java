/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.workmanager;

/**
 * Resource adapter class loader
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ResourceAdapterClassLoader extends ClassLoader
{
   /** The work class loader */
   private WorkClassLoader workClassLoader;

   /**
    * Constructor
    * @param cl The class loader for the resource adapter
    * @param wcl The work class loader
    */
   public ResourceAdapterClassLoader(ClassLoader cl, WorkClassLoader wcl)
   {
      super(cl);
      this.workClassLoader = wcl;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      try
      {
         return super.loadClass(name);
      }
      catch (Throwable t)
      {
         // Default to delegate
      }

      return workClassLoader.loadClass(name, false);
   }

   /**
    * Find a class
    * @param name The fully qualified class name
    * @return The class
    * @throws ClassNotFoundException If the class could not be found 
    */
   @Override
   public Class<?> findClass(String name) throws ClassNotFoundException
   {
      try
      {
         return super.findClass(name);
      }
      catch (Throwable t)
      {
         // Default to delegate
      }

      return workClassLoader.lookup(name);
   }
}
