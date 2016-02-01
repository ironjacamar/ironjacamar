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

import org.ironjacamar.core.api.connectionmanager.pool.PoolConfiguration;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;

import javax.resource.ResourceException;

/**
 * A pool
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Pool extends org.ironjacamar.core.api.connectionmanager.pool.Pool
{
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
    * @return The connection listener
    * @exception ResourceException Thrown if the connection listener cannot be created
    */
   public ConnectionListener createConnectionListener(Credential credential) throws ResourceException;

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
    * Prefill the connection pool
    *
    *
    */
   public void prefill();

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
}
