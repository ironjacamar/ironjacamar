/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.connectionmanager.listener.ConnectionCacheListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.transaction.JTATransactionChecker;

import org.jboss.tm.TransactionTimeoutConfiguration;

/**
 * Internal connection manager contract.
 * <p>
 * <ul>
 *    <li>Responsible for managing cached connections over transactional 
 *    components via {@link ConnectionCacheListener}</li>
 *    <li>Responsible for managing connection instances using event listener 
 *    via {@link ConnectionListenerFactory}</li>
 *    <li>Responsible for managing transaction operations via 
 *    {@link TransactionTimeoutConfiguration} and {@link JTATransactionChecker}</li>.
 * </ul>
 * </p> 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a> 
 */
public interface ConnectionManager extends
   org.jboss.jca.core.api.ConnectionManager,
   ConnectionCacheListener, 
   ConnectionListenerFactory, 
   TransactionTimeoutConfiguration, 
   JTATransactionChecker
{
   /**
    * Set the pool.
    * @param pool the pool
    */
   public void setPool(Pool pool);
   
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
    * Kill given connection listener wrapped connection instance.
    * @param cl connection listener that wraps connection
    * @param kill kill connection or not
    */
   public void returnManagedConnection(ConnectionListener cl, boolean kill);

   /**
    * Unregister association.
    * @param cl connection listener
    * @param c connection
    */
   public void unregisterAssociation(ConnectionListener cl, Object c);
}
