/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

import org.jboss.jca.core.CoreLogger;

import org.jboss.logging.Logger;

/**
 * Resource adapter class loader
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapterClassLoader extends ClassLoader
{
   /** The logger */
   private static CoreLogger log =
      Logger.getMessageLogger(CoreLogger.class, ResourceAdapterClassLoader.class.getName());

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
      if (log.isTraceEnabled())
         log.tracef("%s: loadClass(%s)", Integer.toHexString(System.identityHashCode(this)), name);

      try
      {
         return super.loadClass(name);
      }
      catch (Throwable t)
      {
         // Default to delegate
         if (log.isTraceEnabled())
            log.tracef("%s: Failed to load=%s", Integer.toHexString(System.identityHashCode(this)), name);
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
      if (log.isTraceEnabled())
         log.tracef("%s: findClass(%s)", Integer.toHexString(System.identityHashCode(this)), name);

      try
      {
         return getParent().loadClass(name);
      }
      catch (Throwable t)
      {
         // Default to delegate
         if (log.isTraceEnabled())
            log.tracef("%s: Failed to find=%s", Integer.toHexString(System.identityHashCode(this)), name);
      }

      return workClassLoader.lookup(name);
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ResourceAdapterClassLoader@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[parent=").append(getParent());
      sb.append(" workClassLoader=").append(Integer.toHexString(System.identityHashCode(workClassLoader)));
      sb.append("]");

      return sb.toString();
   }
}
