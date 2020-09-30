/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.workmanager.support;

import java.io.Serializable;

import com.github.fungal.api.Kernel;
import com.github.fungal.api.remote.Command;
import com.github.fungal.api.remote.Communicator;

/**
 * Crashes the VM once called
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class CrashVM implements Command
{
   private Kernel kernel;
   private Communicator communicator;

   /**
    * Constructor
    */
   public CrashVM()
   {
      this.kernel = null;
      this.communicator = null;
   }

   /**
    * Set the kernel
    * @param v The value
    */
   public void setKernel(Kernel v)
   {
      kernel = v;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return "crash";
   }

   /**
    * {@inheritDoc}
    */
   public Class[] getParameterTypes()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public Serializable invoke(Serializable[] args)
   {
      Runtime.getRuntime().halt(1);
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isPublic()
   {
      return true;
   }

   /**
    * Start
    * @exception Throwable Thrown in case of an error
    */
   public void start() throws Throwable
   {
      communicator = kernel.getBean("Communicator", Communicator.class);

      communicator.registerCommand(this);
   }

   /**
    * Stop
    * @exception Throwable Thrown in case of an error
    */
   public void stop() throws Throwable
   {
      if (communicator != null)
         communicator.unregisterCommand(this);
   }
}
