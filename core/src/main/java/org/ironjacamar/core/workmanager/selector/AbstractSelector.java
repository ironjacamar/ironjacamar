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

package org.ironjacamar.core.workmanager.selector;

import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.spi.workmanager.Address;
import org.ironjacamar.core.spi.workmanager.selector.Selector;
import org.ironjacamar.core.workmanager.WorkManagerUtil;
import org.ironjacamar.core.workmanager.notification.AbstractNotificationListener;

import java.util.HashMap;
import java.util.Map;

import javax.resource.spi.work.DistributableWork;

import org.jboss.logging.Logger;

/**
 * Common base class for selector implementations
 */
public abstract class AbstractSelector extends AbstractNotificationListener implements Selector
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           AbstractSelector.class.getName());

   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();

   /**
    * Constructor
    */
   public AbstractSelector()
   {
   }

   /**
    * Get explicit work manager override
    * @param wmId The work manager identifier
    * @param work The work instance
    * @return The selection map
    */
   protected Map<Address, Long> getSelectionMap(String wmId, DistributableWork work)
   {
      if (trace)
      {
         log.tracef("getSelectionMap(%s, %s)", wmId, work);
         log.tracef("ShortRunning: %s", shortRunning);
         log.tracef("LongRunning: %s", longRunning);
      }

      Map<Address, Long> sorted = null;

      if (WorkManagerUtil.isLongRunning(work) && longRunning.get(wmId) != null)
      {
         sorted = new HashMap<Address, Long>(longRunning.get(wmId));
      }

      if (sorted == null && shortRunning.get(wmId) != null)
         sorted = new HashMap<Address, Long>(shortRunning.get(wmId));

      return sorted;
   }

   /**
    * {@inheritDoc}
    */
   public abstract Address selectDistributedWorkManager(Address own, DistributableWork work);
}
