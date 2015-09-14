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

import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 * A pool
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Pool
{
   /**
    * Create a connection listener
    * @param subject The subject
    * @param cri The ConnectionRequestInfo object
    * @return The connection listener
    * @exception ResourceException Thrown if the connection listener cannot be created
    */
   public ConnectionListener createConnectionListener(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException;

   /**
    * Destroy a connection listener
    * @param cl The connection listener
    * @exception ResourceException Thrown if the connection listener cannot be destroed
    */
   public void destroyConnectionListener(ConnectionListener cl) throws ResourceException;
}
