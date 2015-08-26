/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.workmanager;

import org.ironjacamar.core.CoreLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

/**
 * Event queue for WorkManager instances
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WorkManagerEventQueue
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           WorkManagerEventQueue.class.getName());

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /** The instance */
   private static final WorkManagerEventQueue INSTANCE = new WorkManagerEventQueue();

   /** The work managers */
   private Map<String, List<WorkManagerEvent>> events;

   /**
    * Constructor
    */
   private WorkManagerEventQueue()
   {
      this.events = new HashMap<String, List<WorkManagerEvent>>();
   }

   /**
    * Get the instance
    * @return The instance
    */
   public static WorkManagerEventQueue getInstance()
   {
      return INSTANCE;
   }

   /**
    * Add an event
    * @param event The event
    */
   public synchronized void addEvent(WorkManagerEvent event)
   {
      if (trace)
         log.tracef("addEvent(%s)", event);

      List<WorkManagerEvent> e = events.get(event.getAddress().getWorkManagerName());

      if (e == null)
      {
         e = new ArrayList<WorkManagerEvent>();
         events.put(event.getAddress().getWorkManagerName(), e);
      }

      e.add(event);
   }

   /**
    * Get events
    * @param workManagerName The name of the WorkManager
    * @return The list of events
    */
   public synchronized List<WorkManagerEvent> getEvents(String workManagerName)
   {
      List<WorkManagerEvent> result = new ArrayList<WorkManagerEvent>();
      List<WorkManagerEvent> e = events.get(workManagerName);

      if (e != null)
      {
         result.addAll(e);
         e.clear();
      }

      if (trace)
         log.tracef("getEvents(%s): %s", workManagerName, result);

      return result;
   }
}
