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

package org.ironjacamar.core.connectionmanager.listener;

import org.ironjacamar.core.connectionmanager.Credential;

import java.util.Set;

import javax.resource.ResourceException;

/**
 * The internal connection listener API.
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ConnectionListener extends org.ironjacamar.core.api.connectionmanager.listener.ConnectionListener
{
   /** FREE state */
   public int FREE = 1;

   /** IN_USE state */
   public int IN_USE = 2;

   /** DESTROY state */
   public int DESTROY = 3;

   /** DESTROYED state */
   public int DESTROYED = 4;

   /** TO_POOL state */
   public int TO_POOL = 5;

   /** VALIDATION state */
   public int VALIDATION = 6;

   /** ZOMBIE state */
   public int ZOMBIE = 7;

   /** ZOMBIE state */
   public int FLUSH = 8;

   /**
    * Change the state of the connection listener
    * @param currentState The current state
    * @param newState The new state
    * @return True if the state was changed, otherwise false
    */
   public boolean changeState(int currentState, int newState);

   /**
    * Get the state of the connection listener
    * @return The state
    */
   public int getState();

   /**
    * Set the state of the connection listener
    * @param state The state
    */
   public void setState(int state);

   /**
    * Get the credentials
    * @return The value
    */
   public Credential getCredential();

   /**
    * Is the listener enlisted
    * @return True if enlisted, otherwise false
    */
   public boolean isEnlisted();

   /**
    * Enlist the listener
    * @exception ResourceException Thrown if the listener can't be enlisted
    */
   public void enlist() throws ResourceException;
   
   /**
    * Get a connection
    * @return The connection
    * @exception ResourceException Thrown if a connection can't be obtained
    */
   public Object getConnection() throws ResourceException;

   /**
    * Get the connection handles associated
    * @return The value
    */
   public Set<Object> getConnections();

   /**
    * Add a connection handle
    * @param c The handle
    */
   public void addConnection(Object c);

   /**
    * Clear all connection handles
    */
   public void clearConnections();

   /**
    * Get the last timestamp where the listener was validated
    * @return The value
    */
   public long getValidated();

   /**
    * Mark the listener as validated
    */
   public void validated();

   /**
    * Get the last timestamp where the listener was used
    * @return The value
    */
   public long getFromPool();

   /**
    * Mark the listener as used
    */
   public void fromPool();

   /**
    * Get the last timestamp where the listener was free
    * @return The value
    */
   public long getToPool();

   /**
    * Mark the listener as free
    * @exception ResourceException Thrown in case of an error
    */
   public void toPool() throws ResourceException;
}
