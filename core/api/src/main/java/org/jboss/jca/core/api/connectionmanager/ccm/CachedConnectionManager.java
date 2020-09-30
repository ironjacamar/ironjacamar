/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.api.connectionmanager.ccm;

import org.jboss.jca.core.api.connectionmanager.listener.ConnectionCacheListener;
import org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.spi.connectionmanager.ComponentStack;
import org.jboss.jca.core.spi.transaction.usertx.UserTransactionListener;

import java.util.Map;

import javax.transaction.TransactionManager;

/**
 * CacheConnectionManager.
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface CachedConnectionManager extends UserTransactionListener, ComponentStack
{
   /**
    * Gets transaction manager.
    * @return transaction manager
    */
   public TransactionManager getTransactionManager();

   /**
    * Is debug enabled
    * @return True if enabled; otherwise false
    */
   public boolean isDebug();

   /**
    * Set debug flag
    * @param v The value
    */
   public void setDebug(boolean v);

   /**
    * Is error enabled
    * @return True if enabled; otherwise false
    */
   public boolean isError();

   /**
    * Set error flag
    * @param v The value
    */
   public void setError(boolean v);

   /**
    * Is ignore unknown connections on close enabled
    * @return True if enabled; otherwise false
    */
   public boolean isIgnoreUnknownConnections();

   /**
    * Set ignore unknown connections flag
    * @param v The value
    */
   public void setIgnoreUnknownConnections(boolean v);

   /**
    * Register connection.
    * @param cm connection manager
    * @param cl connection listener
    * @param connection connection handle
    */
   public void registerConnection(ConnectionCacheListener cm, ConnectionListener cl,
                                  Object connection);

   /**
    * Unregister connection.
    * @param cm connection manager
    * @param cl connection listener
    * @param connection connection handle
    */
   public void unregisterConnection(ConnectionCacheListener cm, ConnectionListener cl,
                                    Object connection);

   /**
    * Get the number of connections currently in use - if debug is enabled
    * @return The value
    */
   public int getNumberOfConnections();

   /**
    * List the connections in use - if debug is enabled
    *
    * The return value is the connection key, and its allocation stack trace
    * @return The map
    */
   public Map<String, String> listConnections();

   /**
    * Start
    */
   public void start();

   /**
    * Stop
    */
   public void stop();
}
