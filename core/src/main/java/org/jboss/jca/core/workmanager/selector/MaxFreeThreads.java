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
import org.jboss.jca.core.workmanager.WorkManagerUtil;

import java.util.Map;

import javax.resource.spi.work.DistributableWork;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * The ping time selector
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class MaxFreeThreads extends AbstractSelector
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, MaxFreeThreads.class.getName());

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

    /**
    * Constructor
    */
   public MaxFreeThreads()
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String selectDistributedWorkManager(String ownId, DistributableWork work)
   {
      String value = getWorkManager(work);
      if (value != null)
      {
         if (trace)
            log.tracef("WorkManager: %s", value);

         return value;
      }

      Map<String, Long> selectionMap = getSelectionMap(work);
      String result = null;
      long freeThread = 0L;

      for (Map.Entry<String, Long> entry : selectionMap.entrySet())
      {
         String id = entry.getKey();
         if (!ownId.equals(id))
         {
            Long free = entry.getValue();
            if (free != null && free.intValue() > 0)
            {
               long l = 0L;
               if (WorkManagerUtil.isLongRunning(work))
               {
                  l = dwm.getTransport().getLongRunningFree(id);
               }
               else
               {
                  l = dwm.getTransport().getShortRunningFree(id);
               }

               if (l > freeThread)
               {
                  result = id;
                  freeThread = l;
               }
            }
         }
      }

      if (trace)
         log.tracef("WorkManager: %s (%s)", result, freeThread);

      return result;
   }


}
