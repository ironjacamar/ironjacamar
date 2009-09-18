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

/**
 * Kernel configuration implementation
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class KernelConfiguration
{
   /** Home */
   private URL home;

   /** Bind address */
   private String bindAddress;

   /** Thread group */
   private ThreadGroup threadGroup;

   /** Remote access */
   private boolean remoteAccess;

   /** Remote port */
   private int remotePort;

   /**
    * Constructor
    */
   public KernelConfiguration()
   {
      home = null;
      bindAddress = null;
      threadGroup = null;
      remoteAccess = true;
      remotePort = 1202;
   }

   /**
    * Set the home
    * @param h The home
    * @return The configuration
    */
   public KernelConfiguration home(URL h)
   {
      this.home = h;

      return this;
   }

   /**
    * Get the home
    * @return The home
    */
   public URL getHome()
   {
      return home;
   }

   /**
    * Set the bind address
    * @param ba The value
    * @return The configuration
    */
   public KernelConfiguration bindAddress(String ba)
   {
      this.bindAddress = ba;

      return this;
   }

   /**
    * Get the bind address
    * @return The value
    */
   public String getBindAddress()
   {
      return bindAddress;
   }

   /**
    * Set the thread group
    * @param tg The value
    * @return The configuration
    */
   public KernelConfiguration threadGroup(ThreadGroup tg)
   {
      this.threadGroup = tg;

      return this;
   }

   /**
    * Get the thread group
    * @return The value
    */
   public ThreadGroup getThreadGroup()
   {
      return threadGroup;
   }

   /**
    * Set the remote access
    * @param v The value
    * @return The configuration
    */
   public KernelConfiguration remoteAccess(boolean v)
   {
      this.remoteAccess = v;

      return this;
   }

   /**
    * Is remote access enabled ?
    * @return The value
    */
   public boolean isRemoteAccess()
   {
      return remoteAccess;
   }

   /**
    * Set the port for remote access
    * @param v The value
    * @return The configuration
    */
   public KernelConfiguration remotePort(int v)
   {
      this.remotePort = v;

      return this;
   }

   /**
    * Get the remote port
    * @return The value
    */
   public int getRemotePort()
   {
      return remotePort;
   }
}
