/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2006, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.api.connectionmanager.pool;

import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 * A pool.
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Pool
{
   /**
    * Gets pool name.
    * @return pool name
    */
   public String getName();

   /**
    * Flush idle connections from the pool
    */
   public void flush();

   /**
    * Flush the pool
    * @param kill Kill all connections
    */
   public void flush(boolean kill);

   /**
    * Flush the pool
    * @param mode The flush mode
    */
   public void flush(FlushMode mode);

   /**
    * Test if a connection can be obtained
    * @return True if it was possible to get a connection; otherwise false
    */
   public boolean testConnection();

   /**
    * Test if a connection can be obtained
    * @param cri Optional connection request info object
    * @param subject Optional subject
    * @return True if it was possible to get a connection; otherwise false
    */
   public boolean testConnection(ConnectionRequestInfo cri, Subject subject);

   /**
    * Get the statistics
    * @return The value
    */
   public PoolStatistics getStatistics();

   /**
    * Dump the queued threads
    * @return The strack traces of the queued thread, or empty if none
    */
   public String[] dumpQueuedThreads();
}
