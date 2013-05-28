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

package org.jboss.jca.core.workmanager.policy;

import org.jboss.jca.core.api.workmanager.DistributableContext;
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.spi.workmanager.policy.Policy;
import org.jboss.jca.core.workmanager.notification.AbstractNotificationListener;

import java.io.Serializable;
import java.util.List;

import javax.resource.spi.work.DistributableWork;
import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;

/**
 * An abstract policy
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractPolicy extends AbstractNotificationListener implements Policy
{
   /**
    * Constructor
    */
   public AbstractPolicy()
   {
   }

   /**
    * Get should distribute override
    * @param work The work instance
    * @return The override, if none return null
    */
   protected Boolean getShouldDistribute(DistributableWork work)
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
                  return dc.getDistribute();
               }
               else if (wc instanceof HintsContext)
               {
                  HintsContext hc = (HintsContext)wc;
                  if (hc.getHints().keySet().contains(DistributableContext.DISTRIBUTE))
                  {
                     Serializable value = hc.getHints().get(DistributableContext.DISTRIBUTE);
                     if (value != null && value instanceof Boolean)
                     {
                        return (Boolean)value;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public abstract boolean shouldDistribute(DistributedWorkManager dwm, DistributableWork work);
}
