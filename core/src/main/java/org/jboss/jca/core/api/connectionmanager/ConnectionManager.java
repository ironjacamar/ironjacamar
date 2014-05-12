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

package org.jboss.jca.core.api.connectionmanager;

import org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LazyAssociatableConnectionManager;
import javax.resource.spi.LazyEnlistableConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * The JBoss specific connection manager interface.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
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
    * Kill given connection listener wrapped connection instance.
    * @param cl connection listener that wraps connection
    * @param kill kill connection or not
    */
   public void returnManagedConnection(ConnectionListener cl, boolean kill);
}
