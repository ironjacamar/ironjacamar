/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.connectionmanager.pool;

import org.ironjacamar.core.api.connectionmanager.pool.FlushMode;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;

/**
 * ManagedConnectionPool
 */
public interface ManagedConnectionPool
{
   /**
    * Get a connection listener
    * @return The connection listener
    * @exception ResourceException Thrown if the connection listener cannot be created
    */
   public ConnectionListener getConnectionListener() throws ResourceException;

   /**
    * Return a connection listener
    * @param cl The connection listener
    * @param kill Kill the connection listener
    * @exception ResourceException Thrown if the connection listener cannot be destroed
    */
   public void returnConnectionListener(ConnectionListener cl, boolean kill) throws ResourceException;

   /**
    * Get the pool
    * @return The value
    */
   public Pool getPool();

   /**
    * Shutdown the managed connection pool
    */
   public void shutdown();

   /**
    * prefill the pool
    */
   public void prefill();

   /**
    * Fill to
    * @param size The size
    */
   public void fillTo(int size);

   /**
    * Validate connections
    */
   public void validateConnections();


   /**
    * Increase capacity
   */
   public void increaseCapacity();


   /**
    * Remove idle connections
    */
   public void removeIdleConnections();

   /**
    * Checks if the pool is empty or not
    * @return True if is emtpy; otherwise false
    */
   public boolean isEmpty();

   /**
    * Flush
    * @param mode The flush mode
    */
   public void flush(FlushMode mode);

   /**
    * Find a ConnectionListener instance
    * @param mc The associated ManagedConnection
    * @param c The connection (optional)
    * @return The ConnectionListener, or <code>null</code>
    */
   public ConnectionListener findConnectionListener(ManagedConnection mc, Object c);

   /**
    * Remove a ConnectionListener instance
    * @param free True if FREE, false if IN_USE
    * @return The ConnectionListener, or <code>null</code>
    */
   public ConnectionListener removeConnectionListener(boolean free);
}
