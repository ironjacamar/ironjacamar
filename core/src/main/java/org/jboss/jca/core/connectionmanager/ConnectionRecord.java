/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2006, Red Hat Inc, and individual contributors
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

import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * Information about a connection.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
public class ConnectionRecord 
{
   private ConnectionListener connectionListener;
   private final Object connection;
   private final ConnectionRequestInfo cri;
   
   /**
    * Creates a new connection record.
    * @param cl connection listener
    * @param connection connection handle
    * @param cri connection request info
    */
   public ConnectionRecord (final org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener cl, 
                            final Object connection, 
                            final ConnectionRequestInfo cri)
   {
      this.connectionListener = (ConnectionListener)cl;
      this.connection = connection;
      this.cri = cri;
   }

   /**
    * Get the connection listener
    * @return The listener
    */
   public ConnectionListener getConnectionListener()
   {
      return connectionListener;
   }

   /**
    * Set the connection listener
    * @param cl The listener
    */
   void setConnectionListener(ConnectionListener cl)
   {
      this.connectionListener = cl;
   }

   /**
    * Get the connection
    * @return The connection
    */
   public Object getConnection()
   {
      return connection;
   }

   /**
    * Get the connection request info information
    * @return The information
    */
   public ConnectionRequestInfo getCri()
   {
      return cri;
   }
}
