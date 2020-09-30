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
package org.jboss.jca.core.connectionmanager.pool.api;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.PoolStatisticsImpl;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.Transaction;

/**
 * A pool.
 *
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Pool extends org.jboss.jca.core.api.connectionmanager.pool.Pool
{
   /**
    * Sets pool name.
    * @param poolName pool name
    */
   public void setName(String poolName);
   
   /**
    * Is sharable
    * @return The value
    */
   public boolean isSharable();

   /**
    * Retrieve the managed connection factory for this pool.
    * 
    * @return the managed connection factory
    */ 
   public ManagedConnectionFactory getManagedConnectionFactory();

   /**
    * Set the connection manager
    * 
    * @param cm the connection manager
    */
   public void setConnectionManager(ConnectionManager cm);

   /**
    * Get the lock
    * @return The value
    */
   public Semaphore getLock();

   /**
    * Get the capacity policy
    * @return The value
    */
   public Capacity getCapacity();

   /**
    * Is the pool a FIFO or FILO pool
    * @return True if FIFO
    */
   public boolean isFIFO();

   /**
    * Set the capacity policy
    * @param c The value
    */
   public void setCapacity(Capacity c);

   /**
    * Get the interleaving flag
    * @return The value
    */
   public boolean isInterleaving();

   /**
    * Set the interleaving flag
    * @param v The value
    */
   public void setInterleaving(boolean v);

   /**
    * Is the pool idle
    * @return True if idle, otherwise false
    */
   public boolean isIdle();

   /**
    * Is the pool full
    * @return True if full, otherwise false
    */
   public boolean isFull();

   /**
    * Get internal statistics
    * @return The value
    */
   public PoolStatisticsImpl getInternalStatistics();

   /**
    * Get a connection
    * 
    * @param trackByTransaction for transaction stickiness
    * @param subject the subject for connection
    * @param cri the connection request information
    * @return a connection event listener wrapping the connection
    * @throws ResourceException for any error
    */
   public ConnectionListener getConnection(Transaction trackByTransaction, Subject subject, ConnectionRequestInfo cri)
      throws ResourceException;

   /**
    * Find a connection listener
    * @param mc The managed connection
    * @return The connection listener
    */
   public ConnectionListener findConnectionListener(ManagedConnection mc);

   /**
    * Find a connection listener
    * @param mc The managed connection
    * @param connection The connection
    * @return The connection listener
    */
   public ConnectionListener findConnectionListener(ManagedConnection mc, Object connection);

   /**
    * Return a connection
    * 
    * @param cl the connection event listener wrapping the connection
    * @param kill whether to destroy the managed connection
    * @throws ResourceException for any error
    */
   public void returnConnection(ConnectionListener cl, boolean kill) 
      throws ResourceException;

   /**
    * Has an existing connection
    * 
    * @param subject the subject for connection
    * @param cri the connection request information
    * @return <code>true</code> if there is an existing connection enlisted, otherwise <code>false</code>
    */
   public boolean hasConnection(Subject subject, ConnectionRequestInfo cri);

   /**
    * Is shutdown
    * @return The value
    */
   public boolean isShutdown();

   /**
    * Shutdown the pool
    */
   public void shutdown();

   /**
    * Prepare Shutdown
    */
   public void prepareShutdown();

   /**
    * Cancel shutdown
    * @return True if the shutdown was canceled; false otherwise
    */
   public boolean cancelShutdown();

   /**
    * Remove the matching managed connection pool if the pool is empty
    * @param pool The pool
    */
   public void emptyManagedConnectionPool(ManagedConnectionPool pool);

   /**
    * Get the logger
    * @return The value
    */
   public CoreLogger getLogger();
}
