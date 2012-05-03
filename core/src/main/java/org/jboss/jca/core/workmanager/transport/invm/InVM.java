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

package org.jboss.jca.core.workmanager.transport.invm;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.spi.workmanager.transport.Transport;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * The in-vm transport
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class InVM implements Transport
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, InVM.class.getName());
   
   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /** The work manager */
   private Set<DistributedWorkManager> workManagers;

   /**
    * Constructor
    */
   public InVM()
   {
      this.workManagers = Collections.synchronizedSet(new HashSet<DistributedWorkManager>());
   }

   /**
    * Add a distributed work manager
    * @param dwm The work manager
    */
   public void addDistributedWorkManager(DistributedWorkManager dwm)
   {
      if (trace)
         log.tracef("Adding distributed work manager: %s", dwm);

      workManagers.add(dwm);
   }

   /**
    * Remove a distributed work manager
    * @param dwm The work manager
    */
   public void removeDistributedWorkManager(DistributedWorkManager dwm)
   {
      if (trace)
         log.tracef("Removing distributed work manager: %s", dwm);

      workManagers.remove(dwm);
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("InVM@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[workManagers=").append(workManagers);
      sb.append("]");

      return sb.toString();
   }
}
