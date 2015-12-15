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

package org.ironjacamar.core.api.connectionmanager.ccm;

import org.ironjacamar.core.api.connectionmanager.ConnectionManager;
import org.ironjacamar.core.api.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.spi.transaction.usertx.UserTransactionListener;

import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;

/**
 * CacheConnectionManager
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface CachedConnectionManager extends UserTransactionListener
{
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
    * Push a context
    * @param contextKey The context key
    * @param unsharableResources A set of real jndi names marked as unshareable 
    * @throws ResourceException for any error
    */
   @SuppressWarnings("unchecked")
   public void pushContext(Object contextKey, Set unsharableResources) throws ResourceException;

   /**
    * Pop a context
    * @param unsharableResources A set of real jndi names marked as unshareable 
    * @throws ResourceException for any error
    */
   @SuppressWarnings("unchecked")
   public void popContext(Set unsharableResources) throws ResourceException;
   
   /**
    * Register connection
    * @param cm Connection manager
    * @param cl Connection listener
    * @param connection Connection handle
    */
   public void registerConnection(ConnectionManager cm, ConnectionListener cl,
                                  Object connection);

   /**
    * Unregister connection
    * @param cm Connection manager
    * @param cl Connection listener
    * @param connection Connection handle
    */
   public void unregisterConnection(ConnectionManager cm, ConnectionListener cl,
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
