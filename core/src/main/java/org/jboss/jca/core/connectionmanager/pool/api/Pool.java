/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.pool.api;

import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.pool.ManagedConnectionPool;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.Transaction;

/**
 * A pool.
 *
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface Pool
{
   /**
    * Retrieve the managed connection factory for this pool.
    * 
    * @return the managed connection factory
    */ 
   ManagedConnectionFactory getManagedConnectionFactory();

   /**
    * Set the connection listener factory.
    * 
    * @param clf the connection event listener factory
    */
   void setConnectionListenerFactory(ConnectionListenerFactory clf);

   /**
    * Get a connection
    * 
    * @param trackByTransaction for transaction stickiness
    * @param subject the subject for connection
    * @param cri the connection request information
    * @return a connection event listener wrapping the connection
    * @throws ResourceException for any error
    */
   ConnectionListener getConnection(Transaction trackByTransaction, Subject subject, ConnectionRequestInfo cri)
      throws ResourceException;

   /**
    * Return a connection
    * 
    * @param cl the connection event listener wrapping the connection
    * @param kill whether to destroy the managed connection
    * @throws ResourceException for any error
    */
   void returnConnection(ConnectionListener cl, boolean kill) 
      throws ResourceException;

   /**
    * Shutdown the pool
    */
   void shutdown();

   /**
    * Flush the pool
    */
   void flush();

   /**
    * Remove the matching SubPoolContext if the pool is empty
    * @param pool the internal managed connection pool
    */
   void emptySubPool(ManagedConnectionPool pool);
}
