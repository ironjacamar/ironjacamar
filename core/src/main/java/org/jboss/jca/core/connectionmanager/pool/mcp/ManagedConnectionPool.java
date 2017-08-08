/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.mcp;

import org.jboss.jca.core.api.connectionmanager.pool.FlushMode;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.idle.IdleConnectionRemovalSupport;

import java.util.Collection;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 * Represents a managed connection pool, which manages all connection listeners
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ManagedConnectionPool extends IdleConnectionRemovalSupport
{   
   /**
    * Get the last used timestamp
    * @return The value
    */
   public long getLastUsed();

   /**
    * Initialize the managed connection pool
    * 
    * @param mcf The managed connection factory
    * @param cm The connection manager
    * @param subject The subject
    * @param cri The connection request info
    * @param pc The pool configuration
    * @param p The pool
    */
   public void initialize(ManagedConnectionFactory mcf, ConnectionManager cm, Subject subject,
                          ConnectionRequestInfo cri, PoolConfiguration pc, Pool p);

   /**
    * Returns a connection listener that wraps managed connection.
    * @param subject subject
    * @param cri connection request info
    * @return connection listener wrapped managed connection
    * @throws ResourceException exception
    */
   public ConnectionListener getConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException;
   
   /**
    * Find a connection listener
    * @param mc The managed connection
    * @return The connection listener; <code>null</code> if the connection listener doesn't belong
    */
   public ConnectionListener findConnectionListener(ManagedConnection mc);

   /**
    * Find a connection listener
    * @param mc The managed connection
    * @param connection The connection
    * @return The connection listener; <code>null</code> if the connection listener doesn't belong
    */
   public ConnectionListener findConnectionListener(ManagedConnection mc, Object connection);

   /**
    * Return connection to the pool.
    * @param cl connection listener
    * @param kill kill connection
    */
   public void returnConnection(ConnectionListener cl, boolean kill);

   /**
    * Checks if the pool is empty or not
    * @return True if is emtpy; otherwise false
    */
   public boolean isEmpty();
   
   /**
    * Is the pool idle ?
    * @return True if idle, otherwise false
    */
   public boolean isIdle();

   /**
    * Checks if the pool is running or not
    * @return True if is running; otherwise false
    */
   public boolean isRunning();

   /**
    * Get number of active connections
    * @return The value
    */
   public int getActive();
   
   /**
    * Prefill
    */
   public void prefill();
   
   /**
    * Flush
    * @param mode The flush mode
    * @param toDestroy list of connection listeners to be destroyed
    */
   public void flush(FlushMode mode, Collection<ConnectionListener> toDestroy);
   
   /**
    * Shutdown
    */
   public void shutdown();
   
   /**
    * Fill to
    * @param size The size
    */
   public void fillTo(int size);
   
   /**
    * Validate connecitons.
    * @throws Exception for exception
    */
   public void validateConnections() throws Exception;

   /**
    * Increase capacity
    * @param subject The subject
    * @param cri The connection request information object
    */
   public void increaseCapacity(Subject subject, ConnectionRequestInfo cri);

   /**
    * Add a connection to the pool
    * @param cl The connection listener
    */
   public void addConnectionListener(ConnectionListener cl);

   /**
    * Remove an idle connection from the pool
    * @return A connection listener; <code>null</code> if no connection listener was available
    */
   public ConnectionListener removeConnectionListener();

   /**
    * Notify that a connection listener belonging to this pool was destroyed.
    */
   public void connectionListenerDestroyed(ConnectionListener cl);
}
