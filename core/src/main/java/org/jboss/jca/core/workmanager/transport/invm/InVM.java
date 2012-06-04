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
import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;
import org.jboss.jca.core.spi.workmanager.policy.Policy;
import org.jboss.jca.core.spi.workmanager.selector.Selector;
import org.jboss.jca.core.spi.workmanager.transport.Transport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.WorkException;

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

   /** Distributed work manager instance */
   protected DistributedWorkManager dwm;

   /** The work manager */
   private Map<String, DistributedWorkManager> workManagers;

   /**
    * Constructor
    */
   public InVM()
   {
      this.dwm = null;
      this.workManagers = Collections.synchronizedMap(new HashMap<String, DistributedWorkManager>());
   }

   /**
    * {@inheritDoc}
    */
   public void setDistributedWorkManager(DistributedWorkManager dwm)
   {
      this.dwm = dwm;
   }

   /**
    * {@inheritDoc}
    */
   public long ping(String dwm)
   {
      if (!workManagers.keySet().contains(dwm))
         return Long.MAX_VALUE;

      return 0L;
   }

   /**
    * {@inheritDoc}
    */
   public void doWork(String id, DistributableWork work) throws WorkException
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         dwm.localDoWork(work);
      }
      else
      {
         // TODO
         throw new WorkException();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void scheduleWork(String id, DistributableWork work) throws WorkException
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         dwm.localScheduleWork(work);
      }
      else
      {
         // TODO
         throw new WorkException();
      }
   }

   /**
    * {@inheritDoc}
    */
   public long startWork(String id, DistributableWork work) throws WorkException
   {
      DistributedWorkManager dwm = workManagers.get(id);
      if (dwm != null)
      {
         return dwm.localStartWork(work);
      }
      else
      {
         // TODO
         throw new WorkException();
      }
   }

   /**
    * Add a distributed work manager
    * @param dwm The work manager
    */
   public void addDistributedWorkManager(DistributedWorkManager dwm)
   {
      if (trace)
         log.tracef("Adding distributed work manager: %s", dwm);

      workManagers.put(dwm.getId(), dwm);

      Policy policy = this.dwm.getPolicy();
      if (policy != null && policy instanceof NotificationListener)
      {
         NotificationListener listener = (NotificationListener)policy;
         listener.join(dwm.getId());

         // TODO
         listener.updateShortRunningFree(dwm.getId(), 10);
         listener.updateLongRunningFree(dwm.getId(), 10);
      }

      Selector selector = this.dwm.getSelector();
      if (selector != null && selector instanceof NotificationListener)
      {
         NotificationListener listener = (NotificationListener)selector;
         listener.join(dwm.getId());

         // TODO
         listener.updateShortRunningFree(dwm.getId(), 10);
         listener.updateLongRunningFree(dwm.getId(), 10);
      }
   }

   /**
    * Remove a distributed work manager
    * @param dwm The work manager
    */
   public void removeDistributedWorkManager(DistributedWorkManager dwm)
   {
      if (trace)
         log.tracef("Removing distributed work manager: %s", dwm);

      workManagers.remove(dwm.getId());

      Policy policy = this.dwm.getPolicy();
      if (policy != null && policy instanceof NotificationListener)
      {
         NotificationListener listener = (NotificationListener)policy;
         listener.leave(dwm.getId());
      }

      Selector selector = this.dwm.getSelector();
      if (selector != null && selector instanceof NotificationListener)
      {
         NotificationListener listener = (NotificationListener)selector;
         listener.leave(dwm.getId());
      }
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
