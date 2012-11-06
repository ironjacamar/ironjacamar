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

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.api.workmanager.WorkManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.logging.Logger;

/**
 * Coordinator for WorkManager instances
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WorkManagerCoordinator
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           WorkManagerCoordinator.class.getName());

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

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

         if (trace)
            log.tracef("Registering WorkManager: %s", wm);

         if (!workmanagers.keySet().contains(wm.getName()))
         {
            workmanagers.put(wm.getName(), wm);
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

         if (trace)
            log.tracef("Unregistering WorkManager: %s", wm);

         if (workmanagers.keySet().contains(wm.getName()))
         {
            workmanagers.remove(wm.getName());
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
      if (wm != null)
      {
         if (wm.getName() == null || wm.getName().trim().equals(""))
            throw new IllegalArgumentException("The name of WorkManager is invalid: " + wm);

         if (trace)
            log.tracef("Default WorkManager: %s", wm);

         defaultWorkManager = wm;
         workmanagers.put(wm.getName(), wm);
      }
   }

   /**
    * Get a work manager
    * @param id The id of the work manager
    * @return The value
    */
   public WorkManager getWorkManager(String id)
   {
      if (trace)
      {
         log.tracef("getWorkManager(%s)", id);
         log.tracef("ActiveWorkManagers: %s", activeWorkmanagers);
      }

      return activeWorkmanagers.get(id);
   }

   /**
    * Get a distributed work manager
    * @param id The id of the work manager
    * @return The value
    */
   public DistributedWorkManager getDistributedWorkManager(String id)
   {
      if (trace)
      {
         log.tracef("getDistributedWorkManager(%s)", id);
         log.tracef("ActiveWorkManagers: %s", activeWorkmanagers);
      }

      WorkManager wm = activeWorkmanagers.get(id);

      if (wm instanceof DistributedWorkManager)
         return (DistributedWorkManager)wm;

      return null;
   }

   /**
    * Create a work manager
    * @param id The id of the work manager
    * @return The work manager
    */
   public synchronized WorkManager createWorkManager(String id)
   {
      return createWorkManager(id, null);
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
         if (trace)
            log.tracef("RefCounting WorkManager: %s", id);

         Integer i = refCountWorkmanagers.get(id);
         refCountWorkmanagers.put(id, Integer.valueOf(i.intValue() + 1));

         return activeWorkmanagers.get(id);
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

         activeWorkmanagers.put(id, wm);
         refCountWorkmanagers.put(id, Integer.valueOf(1));

         if (trace)
            log.tracef("Created WorkManager: %s", wm);

         return wm;
      }
      catch (Throwable t)
      {
         throw new IllegalStateException("The WorkManager couldn't be created: " + name);
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
            if (trace)
               log.tracef("Removed WorkManager: %s", id);

            activeWorkmanagers.remove(id);
            refCountWorkmanagers.remove(id);
         }
         else
         {
            if (trace)
               log.tracef("DerefCount WorkManager: %s", id);

            refCountWorkmanagers.put(id, Integer.valueOf(newValue));
         }
      }
   }
}
