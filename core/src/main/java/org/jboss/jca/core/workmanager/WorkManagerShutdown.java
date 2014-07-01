/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.spi.graceful.GracefulCallback;

/**
 * Shutdown a WorkManager
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class WorkManagerShutdown implements Runnable
{
   private WorkManager wm;
   private GracefulCallback cb;

   /**
    * Constructor
    * @param wm The work manager
    * @param cb The callback
    */
   WorkManagerShutdown(WorkManager wm, GracefulCallback cb)
   {
      this.wm = wm;
      this.cb = cb;
   }

   /**
    * {@inheritDoc}
    */
   public void run()
   {
      wm.shutdown();

      if (cb != null)
         cb.done();
   }
}
