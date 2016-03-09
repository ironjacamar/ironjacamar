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

package org.ironjacamar.core.api.connectionmanager;

import org.ironjacamar.core.api.connectionmanager.listener.ConnectionListener;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LazyAssociatableConnectionManager;
import javax.resource.spi.LazyEnlistableConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * A connection manager
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ConnectionManager extends javax.resource.spi.ConnectionManager,
                                           LazyAssociatableConnectionManager,
                                           LazyEnlistableConnectionManager
{
   /**
    * Associate a managed connection to a logical connection
    *
    * @param connection The connection
    * @param mcf The managed connection factory
    * @param cri The connection request information
    * @return The managed connection
    * @exception ResourceException Thrown if an error occurs
    */
   public ManagedConnection associateManagedConnection(Object connection, ManagedConnectionFactory mcf,
                                                       ConnectionRequestInfo cri)
      throws ResourceException;

   /**
    * Dissociate a managed connection from a logical connection. The return value
    * of this method will indicate if the managed connection has more connections
    * attached (false), or if it was return to the pool (true).
    *
    * If the managed connection is return to the pool its <code>cleanup</code> method
    * will be called
    *
    * @param connection The connection
    * @param mc The managed connection
    * @param mcf The managed connection factory
    * @return True if the managed connection was freed; otherwise false
    * @exception ResourceException Thrown if an error occurs
    */
   public boolean dissociateManagedConnection(Object connection, ManagedConnection mc, ManagedConnectionFactory mcf)
      throws ResourceException;

   /**
    * Return the connection listener
    * @param cl The connection listener
    * @param kill Should the connection listener be destroyed
    */
   public void returnConnectionListener(ConnectionListener cl, boolean kill);
}
