/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.api.connectionmanager.pool;

import java.util.Map;

/**
 * The janitor
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Janitor
{
   /**
    * Dump the queued threads
    * @return The strack traces of the queued thread, or empty if none
    */
   public String[] dumpQueuedThreads();

   /**
    * Get a map of connection listener allocations (id, allocation trace)
    * @return The map
    */
   public Map<String, String> getConnectionListeners();

   /**
    * Kill a connection listener
    * @param id The connection listener identifier
    * @return True if scheduled for destruction, otherwise false
    */
   public boolean killConnectionListener(String id);
}
