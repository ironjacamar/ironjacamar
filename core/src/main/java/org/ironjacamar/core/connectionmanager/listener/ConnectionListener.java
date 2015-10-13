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
    * Get a connection
    * @return The connection
    * @exception ResourceException Thrown if a connection can't be obtained
    */
   public Object getConnection() throws ResourceException;
}
