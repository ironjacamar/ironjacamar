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
package org.jboss.jca.core.connectionmanager;

import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.connectionmanager.listener.ConnectionCacheListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.spi.graceful.GracefulShutdown;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;

/**
 * Internal connection manager contract.
 * <p>
 * <ul>
 *    <li>Responsible for managing cached connections over transactional 
 *    components via {@link ConnectionCacheListener}</li>
 *    <li>Responsible for managing connection instances using event listener</li>
 * </ul>
 * </p> 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
public interface ConnectionManager extends
   org.jboss.jca.core.api.connectionmanager.ConnectionManager,
   ConnectionCacheListener,
   GracefulShutdown
{
   /**
    * Get the pool.
    * @return the pool
    */
   public Pool getPool();

   /**
    * Gets cached connection manager
    * @return The cached connection manager
    */
   public CachedConnectionManager getCachedConnectionManager();

   /**
    * Get the number of allocation retries
    * @return The number of retries
    */
   public int getAllocationRetry();

   /**
    * Get the wait time between each allocation retry
    * @return The millis
    */
   public long getAllocationRetryWaitMillis();

   /**
    * Get the JNDI name
    * @return The value
    */
   public String getJndiName();

   /**
    * Set the JNDI name
    * @param value The value
    */
   public void setJndiName(String value);

   /**
    * Get the security domain.
    * @return The value
    */
   public String getSecurityDomain();

   /**
    * Get the subject factory
    * @return The value
    */
   public SubjectFactory getSubjectFactory();

   /**
    * Unregister association.
    * @param cl connection listener
    * @param c connection
    */
   public void unregisterAssociation(ConnectionListener cl, Object c);

   /**
    * Create a managed connection listener for the managed connection.
    * 
    * @param managedConnection the managed connection
    * @param mcp the managed connection pool
    * @return a new connection event listener
    * @throws ResourceException for any error
    */
   public ConnectionListener createConnectionListener(ManagedConnection managedConnection, ManagedConnectionPool mcp)
      throws ResourceException;

   /**
    * Determine whether there connection is a transactional.
    *
    * @return whether it is a transactional or not
    */
   public boolean isTransactional();

   /**
    * Get the transaction integration.
    * 
    * @return the transaction integration
    */
   public TransactionIntegration getTransactionIntegration();

   /**
    * Is sharable
    * @return The value
    */
   public boolean isSharable();

   /**
    * Is enlistment
    * @return The value
    */
   public boolean isEnlistment();
}
