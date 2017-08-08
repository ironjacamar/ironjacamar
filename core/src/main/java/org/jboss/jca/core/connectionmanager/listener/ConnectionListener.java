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
package org.jboss.jca.core.connectionmanager.listener;

import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.transaction.SystemException;

/**
 * Connection listener.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="wprice@redhat.com">Weston Price</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ConnectionListener extends org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener
{
   /**
    * Get number of connection handles
    * @return The value
    */
   public int getNumberOfConnections();

   /**
    * Retrieve the pool for this listener.
    * 
    * @return the pool
    */
   public Pool getPool();

   /**
    * Tidyup
    * <p>
    * Invoked just before returning the connection to the pool when the
    * connection is not being destroyed.
    * 
    * @throws ResourceException for any error
    */
   public void tidyup() throws ResourceException;

   /**
    * Get the managed connection pool
    * @return The value
    */
   public ManagedConnectionPool getManagedConnectionPool();

   /**
    * Retrieve the state of this connection.
    * 
    * @return the state
    */
   public ConnectionState getState();

   /**
    * Set the state of this connection.
    * 
    * @param newState new state
    */
   public void setState(ConnectionState newState);

   /**
    * Has the connection timed out?
    * 
    * @param timeout the timeout
    * @return true for timed out, false otherwise
    */
   public boolean isTimedOut(long timeout);

   /**
    * Mark the connection as in pool
    */
   public void toPool();

   /**
    * Register a new connection
    * 
    * @param handle the connection handle
    */
   public void registerConnection(Object handle);

   /**
    * Unregister a connection
    * 
    * @param handle the connection handle
    */
   public void unregisterConnection(Object handle);

   /**
    * Unregister all connections
    */
   public void unregisterConnections();

   /**
    * Is the managed connection free?
    * 
    * @return true when it is free
    */
   public boolean isManagedConnectionFree();

   /**
    * Is enlisted
    * @return True if enlisted, otherwise false
    */
   public boolean isEnlisted();

   /**
    * Enlist the managed connection
    * 
    * @throws SystemException for errors
    */
   public void enlist() throws SystemException;

   /**
    * Delist the managed connection
    * @return <code>True</code> if the listener was delisted succesfully, otherwise <code>false</code>
    * @throws ResourceException if exception occurs
    */
   public boolean delist() throws ResourceException;

   /**
    * Get whether the listener is track by transaction
    * 
    * @return true for track by transaction, false otherwise
    */
   public boolean isTrackByTx();

   /**
    * Set whether the listener is track by transaction
    * 
    * @param trackByTx true for track by transaction, false otherwise
    */
   public void setTrackByTx(boolean trackByTx);

   /**
    * Retrieve the last time this connection was validated.
    * 
    * @return the last time the connection was validated
    */
   public long getLastValidatedTime();

   /**
    * Set the last time, in milliseconds, that this connection was validated.
    * 
    * @param lastValidated the last time the connection was validated in
    *           milliseconds.
    */
   public void setLastValidatedTime(long lastValidated);

   /**
    * Retrieve the last time this connection was returned to the pool
    * 
    * @return the last time the connection was returned to the pool
    */
   public long getLastReturnedTime();

   /**
    * Retrieve the last time this connection was obtained from the pool
    * 
    * @return the last time the connection was obtained from the pool
    */
   public long getLastCheckedOutTime();

   /**
    * Set the last time this connection was obtained from the pool
    *
    * @param v The value
    */
   public void setLastCheckedOutTime(long v);

   /**
    * Get exception
    * @return The exception that occured, or <code>null</code>
    */
   public Exception getException();

   /**
    * Controls the managed connection / connection pair
    * @param mc The managed connection
    * @param connection The connection; optional
    * @return True if the connection listener controls the pair, otherwise false
    */
   public boolean controls(ManagedConnection mc, Object connection);

   /**
    * Dissociate the connection listener
    * @throws ResourceException if exception occurs
    */
   public void dissociate() throws ResourceException;

   /**
    * Supports lazy association
    * @return <code>True</code> if supported, otherwise <code>false</code>
    */
   public boolean supportsLazyAssociation();

   /**
    * Supports lazy enlistment
    * @return <code>True</code> if supported, otherwise <code>false</code>
    */
   public boolean supportsLazyEnlistment();

   /**
    * Destroys this connection listener and its managed connection.
    */
   public void destroy();
}
