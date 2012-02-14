/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.idle.IdleConnectionRemovalSupport;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 * Represents a managed connection pool, which manages all connection listeners
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface ManagedConnectionPool extends IdleConnectionRemovalSupport
{   
   /**
    * Initialize the managed connection pool
    * 
    * @param mcf The managed connection factory
    * @param clf The connection listener factory
    * @param subject The subject
    * @param cri The connection request info
    * @param pc The pool configuration
    * @param p The pool
    */
   public void initialize(ManagedConnectionFactory mcf, ConnectionListenerFactory clf, Subject subject,
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
    * @param connection The connection
    * @param mc The managed connection
    * @return The connection listener; <code>null</code> if the connection listener doesn't belong
    */
   public ConnectionListener findConnectionListener(Object connection, ManagedConnection mc);

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
    * Checks if the pool is running or not
    * @return True if is running; otherwise false
    */
   public boolean isRunning();
   
   /**
    * Reenable a pool 
    */
   public void reenable();
   
   /**
    * Flush idle connections
    */
   public void flush();
   
   /**
    * Flush
    * @param kill Kill all connections
    */
   public void flush(boolean kill);
   
   /**
    * Shutdown
    */
   public void shutdown();
   
   /**
    * Fill to min
    */
   public void fillToMin();
   
   /**
    * Validate connecitons.
    * @throws Exception for exception
    */
   public void validateConnections() throws Exception;

   /**
    * Get statistics
    * @return The module
    */
   public ManagedConnectionPoolStatistics getStatistics();
}
