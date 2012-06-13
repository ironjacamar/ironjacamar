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

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;

import java.util.Map;

import javax.resource.spi.work.DistributableWork;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * The first available selector
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class FirstAvailable extends AbstractSelector
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, FirstAvailable.class.getName());
   
   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /**
    * Constructor
    */
   public FirstAvailable()
   {
   }

   /**
    * {@inheritDoc}
    */
   public String selectDistributedWorkManager(String ownId, DistributableWork work)
   {
      if (trace)
         log.tracef("OwnId: %s, Work: %s", ownId, work);

      String value = getWorkManager(work);
      if (value != null)
      {
         if (trace)
            log.tracef("WorkManager: %s", value);

         return value;
      }

      Map<String, Integer> selectionMap = getSelectionMap(work);
      // No sorting needed

      if (trace)
         log.tracef("SelectionMap: %s", selectionMap);

      for (Map.Entry<String, Integer> entry : selectionMap.entrySet())
      {
         String id = entry.getKey();
         if (!ownId.equals(id))
         {
            Integer free = entry.getValue();
            if (free != null && free.intValue() > 0)
            {
               if (trace)
                  log.tracef("WorkManager: %s", id);

               return id;
            }
         }
      }

      if (trace)
         log.tracef("WorkManager: None");

      return null;
   }
}
