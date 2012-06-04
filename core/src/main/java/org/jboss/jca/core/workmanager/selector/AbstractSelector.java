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

package org.jboss.jca.core.workmanager.selector;

import org.jboss.jca.core.api.workmanager.DistributableContext;
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.spi.workmanager.notification.NotificationListener;
import org.jboss.jca.core.spi.workmanager.selector.Selector;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;

/**
 * Common base class for selector implementations
 */
public abstract class AbstractSelector implements Selector, NotificationListener
{
   /** Distributed work manager instance */
   protected DistributedWorkManager dwm;

   /** Short running */
   protected Map<String, Integer> shortRunning;

   /** Long running */
   protected Map<String, Integer> longRunning;

   /**
    * Constructor
    */
   public AbstractSelector()
   {
      this.dwm = null;
      this.shortRunning = Collections.synchronizedMap(new HashMap<String, Integer>());
      this.longRunning = Collections.synchronizedMap(new HashMap<String, Integer>());
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
   public void join(String id)
   {
      shortRunning.put(id, null);
      longRunning.put(id, null);
   }

   /**
    * {@inheritDoc}
    */
   public void leave(String id)
   {
      shortRunning.remove(id);
      longRunning.remove(id);
   }

   /**
    * {@inheritDoc}
    */
   public void updateShortRunningFree(String id, int free)
   {
      shortRunning.put(id, Integer.valueOf(free));
   }

   /**
    * {@inheritDoc}
    */
   public void updateLongRunningFree(String id, int free)
   {
      longRunning.put(id, Integer.valueOf(free));
   }

   /**
    * Get explicit work manager override
    * @param work The work instance
    * @return The override, if none return null
    */
   protected String getWorkManager(DistributableWork work)
   {
      if (work != null && work instanceof WorkContextProvider)
      {
         List<WorkContext> contexts = ((WorkContextProvider)work).getWorkContexts();
         if (contexts != null)
         {
            for (WorkContext wc : contexts)
            {
               if (wc instanceof DistributableContext)
               {
                  DistributableContext dc = (DistributableContext)wc;
                  return dc.getWorkManager();
               }
               else if (wc instanceof HintsContext)
               {
                  HintsContext hc = (HintsContext)wc;
                  if (hc.getHints().keySet().contains(DistributableContext.WORKMANAGER))
                  {
                     Serializable value = hc.getHints().get(DistributableContext.WORKMANAGER);
                     if (value != null && value instanceof String)
                     {
                        return (String)value;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   /**
    * Get the data for selection
    * @param work The work instance
    * @return The selection map
    */
   protected Map<String, Integer> getSelectionMap(DistributableWork work)
   {
      Map<String, Integer> sorted = null;
      if (isLongRunning(work))
      {
         sorted = new HashMap<String, Integer>(longRunning);
      }
      else
      {
         sorted = new HashMap<String, Integer>(shortRunning);
      }

      return sorted;
   }

   /**
    * Is a long running work instance
    * @param work The work instance
    * @return True if long running, otherwise false
    */
   private boolean isLongRunning(DistributableWork work)
   {
      if (work != null && work instanceof WorkContextProvider)
      {
         List<WorkContext> contexts = ((WorkContextProvider)work).getWorkContexts();
         if (contexts != null)
         {
            for (WorkContext wc : contexts)
            {
               if (wc instanceof HintsContext)
               {
                  HintsContext hc = (HintsContext)wc;
                  if (hc.getHints().keySet().contains(HintsContext.LONGRUNNING_HINT))
                  {
                     Serializable value = hc.getHints().get(HintsContext.LONGRUNNING_HINT);
                     if (value != null)
                     {
                        if (value instanceof String)
                        {
                           return Boolean.valueOf((String)value);
                        }
                        else if (value instanceof Boolean)
                        {
                           return ((Boolean)value).booleanValue();
                        }
                     }
                     else
                     {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }
}
