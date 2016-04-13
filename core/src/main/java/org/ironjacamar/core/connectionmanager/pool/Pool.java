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

import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.api.connectionmanager.pool.PoolConfiguration;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;

/**
 * A pool
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Pool extends org.ironjacamar.core.api.connectionmanager.pool.Pool
{
   /**
    * Get the logger
    * @return The value
    */
   public CoreLogger getLogger();

   /**
    * Get the connection manager
    * @return The value
    */
   public ConnectionManager getConnectionManager();
   
   /**
    * Get the pool configuration
    * @return The configuration
    */
   public PoolConfiguration getConfiguration();

   /**
    * Get the statistics implementation instance
    * @return The value
    */
   public PoolStatisticsImpl getInternalStatistics();

   /**
    * Get the semaphore
    * @return The semaphore
    */
   public Semaphore getPermits();
   
   /**
    * Get a connection listener
    * @param credential The credential
    * @return The connection listener
    * @exception ResourceException Thrown if the connection listener cannot be created
    */
   public ConnectionListener getConnectionListener(Credential credential) throws ResourceException;

   /**
    * Return a connection listener
    * @param cl The connection listener
    * @param kill Kill the connection listener
    * @exception ResourceException Thrown if the connection listener cannot be destroed
    */
   public void returnConnectionListener(ConnectionListener cl, boolean kill) throws ResourceException;

   /**
    * Create a connection listener
    * @param credential The credential
    * @param mcp The ManagedCOnnectionPool
    * @return The connection listener
    * @exception ResourceException Thrown if the connection listener cannot be created
    */
   public ConnectionListener createConnectionListener(Credential credential, ManagedConnectionPool mcp)
         throws ResourceException;

   /**
    * Destroy a connection listener
    * @param cl The connection listener
    * @exception ResourceException Thrown if the connection listener cannot be destroyed
    */
   public void destroyConnectionListener(ConnectionListener cl) throws ResourceException;

   /**
    * Create a new managed connection pool instance
    * @param credential The credential
    * @return The instance
    */
   public ManagedConnectionPool createManagedConnectionPool(Credential credential);

   /**
    * Is the pool full
    * @return True if full, otherwise false
    */
   public boolean isFull();

   /**
    * Shutdown the pool
    */
   public void shutdown();

   /**
    * Shutdown the pool in progress
    * @return true id shutdown is in progress
    */
   public boolean isShutdown();

   /**
    * Get prefill credential
    * @return credential used to prefill
    */
   public Credential getPrefillCredential();

   /**
    * Empty a ManagedConnectionPool
    * @param mcp The instance
    */
   public void emptyManagedConnectionPool(ManagedConnectionPool mcp);

   /**
    * Get the flush strategy
    * @return The value
    */
   public FlushStrategy getFlushStrategy();

   /**
    * Enlist
    * @param mc The ManagedConnection
    * @exception ResourceException Thrown in case of an error
    */
   public void enlist(ManagedConnection mc) throws ResourceException;

   /**
    * Delist
    * @param cl The ConnectionListener
    * @exception ResourceException Thrown in case of an error
    */
   public void delist(ConnectionListener cl) throws ResourceException;

   /**
    * Find a ConnectionListener instance
    * @param mc The associated ManagedConnection
    * @param c The connection (optional)
    * @return The ConnectionListener, or <code>null</code>
    */
   public ConnectionListener findConnectionListener(ManagedConnection mc, Object c);

   /**
    * Get the active ConnectionListener instance
    * @param credential The credential
    * @return The ConnectionListener, or <code>null</code>
    */
   public ConnectionListener getActiveConnectionListener(Credential credential);

   /**
    * Remove a ConnectionListener instance
    * @param credential The target credential (optional)
    * @return The ConnectionListener, or <code>null</code>
    */
   public ConnectionListener removeConnectionListener(Credential credential);

   /**
    * Get the capacity policy
    * @return The value
    */
   public Capacity getCapacity();

   /**
    * Set the capacity policy
    * @param c The value
    */
   public void setCapacity(Capacity c);

   /**
    * Is the pool a FIFO or FILO pool
    * @return True if FIFO
    */
   public boolean isFIFO();

   /**
    * Set the janitor
    * @param v The value
    */
   public void setJanitor(Janitor v);
}
