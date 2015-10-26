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

package org.jboss.jca.core.workmanager.notification;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.workmanager.Address;
import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

/**
 * An abstract notification listener
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractNotificationListener implements NotificationListener
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           AbstractNotificationListener.class.getName());

   /** Short running */
   protected Map<String, Map<Address, Long>> shortRunning;

   /** Long running */
   protected Map<String, Map<Address, Long>> longRunning;

   /**
    * Constructor
    */
   public AbstractNotificationListener()
   {
      this.shortRunning = Collections.synchronizedMap(new HashMap<String, Map<Address, Long>>());
      this.longRunning = Collections.synchronizedMap(new HashMap<String, Map<Address, Long>>());
   }

   /**
    * {@inheritDoc}
    */
   public void join(Address address)
   {
      log.tracef("join(%s)", address);

      Map<Address, Long> sr = shortRunning.get(address.getWorkManagerId());

      if (sr == null)
         sr = Collections.synchronizedMap(new HashMap<Address, Long>());

      sr.put(address, Long.valueOf(0));
      shortRunning.put(address.getWorkManagerId(), sr);

      Map<Address, Long> lr = longRunning.get(address.getWorkManagerId());

      if (lr == null)
         lr = Collections.synchronizedMap(new HashMap<Address, Long>());

      lr.put(address, Long.valueOf(0));
      longRunning.put(address.getWorkManagerId(), lr);
   }

   /**
    * {@inheritDoc}
    */
   public void leave(Address address)
   {
      log.tracef("leave(%s)", address);

      Map<Address, Long> sr = shortRunning.get(address.getWorkManagerId());

      if (sr != null)
      {
         sr.remove(address);

         if (sr.size() > 0)
         {
            shortRunning.put(address.getWorkManagerId(), sr);
         }
         else
         {
            shortRunning.remove(address.getWorkManagerId());
         }
      }

      Map<Address, Long> lr = longRunning.get(address.getWorkManagerId());

      if (lr != null)
      {
         lr.remove(address);

         if (lr.size() > 0)
         {
            longRunning.put(address.getWorkManagerId(), lr);
         }
         else
         {
            longRunning.remove(address.getWorkManagerId());
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateShortRunningFree(Address address, long free)
   {
      log.tracef("updateShortRunningFree(%s, %d)", address, free);

      Map<Address, Long> sr = shortRunning.get(address.getWorkManagerId());

      if (sr != null)
      {
         sr.put(address, Long.valueOf(free));
         shortRunning.put(address.getWorkManagerId(), sr);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void updateLongRunningFree(Address address, long free)
   {
      log.tracef("updateLongRunningFree(%s, %d)", address, free);

      Map<Address, Long> lr = longRunning.get(address.getWorkManagerId());

      if (lr != null)
      {
         lr.put(address, Long.valueOf(free));
         longRunning.put(address.getWorkManagerId(), lr);
      }
   }


   /**
    * {@inheritDoc}
    */
   public void deltaDoWorkAccepted()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void deltaDoWorkRejected()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void deltaStartWorkAccepted()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void deltaStartWorkRejected()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void deltaScheduleWorkAccepted()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void deltaScheduleWorkRejected()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void deltaWorkSuccessful()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void deltaWorkFailed()
   {
   }
}
