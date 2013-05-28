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

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.workmanager.WorkManagerUtil;

import javax.resource.spi.work.DistributableWork;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * The always distribute policy
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Always extends AbstractPolicy
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, Always.class.getName());
   
   /** Whether trace is enabled */
   private static boolean trace = log.isTraceEnabled();
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);

   /**
    * Constructor
    */
   public Always()
   {
   }

   /**
    * {@inheritDoc}
    */
   public synchronized boolean shouldDistribute(DistributedWorkManager dwm, DistributableWork work)
   {
      if (trace)
         log.tracef("Work=%s", work);

      Boolean override = WorkManagerUtil.getShouldDistribute(work);
      if (override != null)
         return override.booleanValue();

      return true;
   }
}
