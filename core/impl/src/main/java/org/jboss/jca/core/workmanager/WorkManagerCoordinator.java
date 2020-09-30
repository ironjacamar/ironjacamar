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
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.spi.workmanager.Address;
import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.logging.Logger;

/**
 * Coordinator for WorkManager instances
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WorkManagerCoordinator
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           WorkManagerCoordinator.class.getName());

   /** The instance */
   private static final WorkManagerCoordinator INSTANCE = new WorkManagerCoordinator();

   /** The work managers */
   private ConcurrentMap<String, WorkManager> workmanagers;

   /** The default work manager */
   private WorkManager defaultWorkManager;

   /** The activate work managers */
   private Map<String, WorkManager> activeWorkmanagers;

   /** The ref count for activate work managers */
   private Map<String, Integer> refCountWorkmanagers;

   /**
    * Constructor
    */
   private WorkManagerCoordinator()
   {
      this.workmanagers = new ConcurrentHashMap<String, WorkManager>();
      this.defaultWorkManager = null;
      this.activeWorkmanagers = new HashMap<String, WorkManager>();
      this.refCountWorkmanagers = new HashMap<String, Integer>();
   }

   /**
    * Get the instance
    * @return The instance
    */
   public static WorkManagerCoordinator getInstance()
   {
      return INSTANCE;
   }

   /**
    * Register work manager
    * @param wm The work manager
    */
   public void registerWorkManager(WorkManager wm)
   {
      if (wm != null)
      {
         if (wm.getName() == null || wm.getName().trim().equals(""))
            throw new IllegalArgumentException("The name of WorkManager is invalid: " + wm);

         log.tracef("Registering WorkManager: %s", wm);

         if (!workmanagers.keySet().contains(wm.getName()))
         {
            workmanagers.put(wm.getName(), wm);

            // Replay events for distributed work managers
            if (wm instanceof DistributedWorkManager)
            {
               WorkManagerEventQueue wmeq = WorkManagerEventQueue.getInstance();
               List<WorkManagerEvent> events = wmeq.getEvents(wm.getName());

               if (events.size() > 0)
               {
                  log.tracef("%s: Events=%s", wm.getName(), events);

                  for (WorkManagerEvent event : events)
                  {
                     if (event.getType() == WorkManagerEvent.TYPE_JOIN)
                     {
                        DistributedWorkManager dwm = resolveDistributedWorkManager(event.getAddress());

                        if (dwm != null)
                        {
                           Collection<NotificationListener> copy =
                              new ArrayList<NotificationListener>(dwm.getNotificationListeners());
                           for (NotificationListener nl : copy)
                           {
                              nl.join(event.getAddress());
                           }
                        }
                     }
                     else if (event.getType() == WorkManagerEvent.TYPE_LEAVE)
                     {
                        DistributedWorkManager dwm = 
                           (DistributedWorkManager)activeWorkmanagers.get(event.getAddress().getWorkManagerId());

                        if (dwm != null)
                        {
                           Collection<NotificationListener> copy =
                              new ArrayList<NotificationListener>(dwm.getNotificationListeners());
                           for (NotificationListener nl : copy)
                           {
                              nl.leave(event.getAddress());
                           }
                        }

                        removeWorkManager(event.getAddress().getWorkManagerId());
                     }
                     else if (event.getType() == WorkManagerEvent.TYPE_UPDATE_SHORT_RUNNING)
                     {
                        DistributedWorkManager dwm = 
                           (DistributedWorkManager)activeWorkmanagers.get(event.getAddress().getWorkManagerId());

                        if (dwm != null)
                        {
                           Collection<NotificationListener> copy =
                              new ArrayList<NotificationListener>(dwm.getNotificationListeners());
                           for (NotificationListener nl : copy)
                           {
                              nl.updateShortRunningFree(event.getAddress(), event.getValue());
                           }
                        }
                     }
                     else if (event.getType() == WorkManagerEvent.TYPE_UPDATE_LONG_RUNNING)
                     {
                        DistributedWorkManager dwm = 
                           (DistributedWorkManager)activeWorkmanagers.get(event.getAddress().getWorkManagerId());

                        if (dwm != null)
                        {
                           Collection<NotificationListener> copy =
                              new ArrayList<NotificationListener>(dwm.getNotificationListeners());
                           for (NotificationListener nl : copy)
                           {
                              nl.updateLongRunningFree(event.getAddress(), event.getValue());
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Unregister work manager
    * @param wm The work manager
    */
   public void unregisterWorkManager(WorkManager wm)
   {
      if (wm != null)
      {
         if (wm.getName() == null || wm.getName().trim().equals(""))
            throw new IllegalArgumentException("The name of WorkManager is invalid: " + wm);

         log.tracef("Unregistering WorkManager: %s", wm);

         if (workmanagers.keySet().contains(wm.getName()))
         {
            workmanagers.remove(wm.getName());

            // Clear any events
            if (wm instanceof DistributedWorkManager)
            {
               WorkManagerEventQueue wmeq = WorkManagerEventQueue.getInstance();
               List<WorkManagerEvent> events = wmeq.getEvents(wm.getName());
               events.clear();
            }
         }
      }
   }

   /**
    * Get the default work manager
    * @return The work manager
    */
   public WorkManager getDefaultWorkManager()
   {
      return defaultWorkManager;
   }

   /**
    * Set the default work manager
    * @param wm The work manager
    */
   public void setDefaultWorkManager(WorkManager wm)
   {
      log.tracef("Default WorkManager: %s", wm);

      String currentName = null;

      if (defaultWorkManager != null)
         currentName = defaultWorkManager.getName();

      defaultWorkManager = wm;

      if (wm != null)
      {
         workmanagers.put(wm.getName(), wm);
      }
      else if (currentName != null)
      {
         workmanagers.remove(currentName);
      }
   }

   /**
    * Resolve a work manager
    * @param address The work manager address
    * @return The value
    */
   public WorkManager resolveWorkManager(Address address)
   {
      log.tracef("resolveWorkManager(%s)", address);
      log.tracef("  ActiveWorkManagers: %s", activeWorkmanagers);

      WorkManager wm = activeWorkmanagers.get(address.getWorkManagerId());
      if (wm != null)
      {
         log.tracef(" WorkManager: %s", wm);

         return wm;
      }

      try
      {
         // Create a new instance
         WorkManager template = workmanagers.get(address.getWorkManagerName());

         if (template != null)
         {
            wm = template.clone();
            wm.setId(address.getWorkManagerId());

            if (wm instanceof DistributedWorkManager)
            {
               DistributedWorkManager dwm = (DistributedWorkManager)wm;
               dwm.initialize();
            }

            activeWorkmanagers.put(address.getWorkManagerId(), wm);
            refCountWorkmanagers.put(address.getWorkManagerId(), Integer.valueOf(0));

            log.tracef("Created WorkManager: %s", wm);

            return wm;
         }
      }
      catch (Throwable t)
      {
         //throw new IllegalStateException("The WorkManager couldn't be created: " + name);
      }

      return null;
   }

   /**
    * Resolve a distributed work manager
    * @param address The work manager address
    * @return The value
    */
   public DistributedWorkManager resolveDistributedWorkManager(Address address)
   {
      log.tracef("resolveDistributedWorkManager(%s)", address);
      log.tracef("  ActiveWorkManagers: %s", activeWorkmanagers);

      WorkManager wm = activeWorkmanagers.get(address.getWorkManagerId());

      if (wm != null)
      {
         if (wm instanceof DistributedWorkManager)
         {
            log.tracef(" WorkManager: %s", wm);

            return (DistributedWorkManager)wm;
         }
         else
         {
            log.tracef(" WorkManager not distributable: %s", wm);

            return null;
         }
      }

      try
      {
         // Create a new instance
         WorkManager template = workmanagers.get(address.getWorkManagerName());

         if (template != null)
         {
            wm = template.clone();
            wm.setId(address.getWorkManagerId());

            if (wm instanceof DistributedWorkManager)
            {
               DistributedWorkManager dwm = (DistributedWorkManager)wm;
               dwm.initialize();

               activeWorkmanagers.put(address.getWorkManagerId(), dwm);
               refCountWorkmanagers.put(address.getWorkManagerId(), Integer.valueOf(0));
               
               log.tracef("Created WorkManager: %s", dwm);
               
               return dwm;
            }
         }
      }
      catch (Throwable t)
      {
         //throw new IllegalStateException("The WorkManager couldn't be created: " + name);
      }

      return null;
   }

   /**
    * Create a work manager
    * @param id The id of the work manager
    * @param name The name of the work manager; if <code>null</code> default value is used
    * @return The work manager
    */
   public synchronized WorkManager createWorkManager(String id, String name)
   {
      if (id == null || id.trim().equals(""))
         throw new IllegalArgumentException("The id of WorkManager is invalid: " + id);

      // Check for an active work manager
      if (activeWorkmanagers.keySet().contains(id))
      {
         log.tracef("RefCounting WorkManager: %s", id);

         Integer i = refCountWorkmanagers.get(id);
         refCountWorkmanagers.put(id, Integer.valueOf(i.intValue() + 1));

         WorkManager wm = activeWorkmanagers.get(id);
         if (wm instanceof DistributedWorkManager)
         {

            DistributedWorkManager dwm = (DistributedWorkManager)wm;
            if (dwm.getTransport() != null)
               dwm.getTransport().register(new Address(wm.getId(), wm.getName(), dwm.getTransport().getId()));
         }

         return wm;
      }

      try
      {
         // Create a new instance
         WorkManager template = null;
         if (name != null)
         {
            template = workmanagers.get(name);
         }
         else
         {
            template = defaultWorkManager;
         }

         if (template == null)
            throw new IllegalArgumentException("The WorkManager wasn't found: " + name);

         WorkManager wm = template.clone();
         wm.setId(id);

         if (wm instanceof DistributedWorkManager)
         {
            DistributedWorkManager dwm = (DistributedWorkManager)wm;
            dwm.initialize();

            if (dwm.getTransport() != null)
            {
               dwm.getTransport().register(new Address(wm.getId(), wm.getName(), dwm.getTransport().getId()));
            }
            else
            {
               log.debugf("DistributedWorkManager '%s' doesn't have a transport associated", dwm.getName());
            }
         }

         activeWorkmanagers.put(id, wm);
         refCountWorkmanagers.put(id, Integer.valueOf(1));

         log.tracef("Created WorkManager: %s", wm);

         return wm;
      }
      catch (Throwable t)
      {
         throw new IllegalStateException("The WorkManager couldn't be created: " + name, t);
      }
   }

   /**
    * Remove a work manager
    * @param id The id of the work manager
    */
   public synchronized void removeWorkManager(String id)
   {
      if (id == null || id.trim().equals(""))
         throw new IllegalArgumentException("The id of WorkManager is invalid: " + id);

      Integer i = refCountWorkmanagers.get(id);
      if (i != null)
      {
         int newValue = i.intValue() - 1;
         if (newValue == 0)
         {
            log.tracef("Removed WorkManager: %s", id);

            WorkManager wm = activeWorkmanagers.get(id);
            if (wm instanceof DistributedWorkManager)
            {
               DistributedWorkManager dwm = (DistributedWorkManager)wm;
               if (dwm.getTransport() != null)
                  dwm.getTransport().unregister(new Address(wm.getId(), wm.getName(), dwm.getTransport().getId()));
            }

            activeWorkmanagers.remove(id);
            refCountWorkmanagers.remove(id);
         }
         else
         {
            log.tracef("DerefCount WorkManager: %s", id);

            refCountWorkmanagers.put(id, Integer.valueOf(newValue));
         }
      }
   }
}
