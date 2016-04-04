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

package org.ironjacamar.core.connectionmanager;

import org.ironjacamar.core.api.connectionmanager.ConnectionManagerConfiguration;
import org.ironjacamar.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.Pool;
import org.ironjacamar.core.spi.graceful.GracefulShutdown;
import org.ironjacamar.core.spi.security.SubjectFactory;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

/**
 * The internal connection manager API
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ConnectionManager extends org.ironjacamar.core.api.connectionmanager.ConnectionManager,
                                           GracefulShutdown
{
   /**
    * Get the cached connection manager
    * @return The value
    */
   public CachedConnectionManager getCachedConnectionManager();

   /**
    * Get the transaction support level
    * @return The value
    */
   public TransactionSupportLevel getTransactionSupport();

   /**
    * Set the pool
    * @param pool The pool
    */
   public void setPool(Pool pool);

   /**
    * Get the pool
    * @return The pool
    */
   public Pool getPool();

   /**
    * Get the managed connection factory
    * @return The value
    */
   public ManagedConnectionFactory getManagedConnectionFactory();

   /**
    * Get the connection manager configuration
    * @return The value
    */
   public ConnectionManagerConfiguration getConnectionManagerConfiguration();

   /**
    * Transaction started
    * @param cl The connection listener
    * @exception ResourceException Thrown if an enlistment error occurs
    */
   public void transactionStarted(ConnectionListener cl) throws ResourceException;

   /**
    * Is shutdown
    * @return True if shutdown, otherwise false
    */
   public boolean isShutdown();
   
   /**
    * Shutdown the connection manager
    */
   public void shutdown();

   /**
    * set SubjectFactory
    * @param subjectFactory the subjectFactory
    */
   public void setSubjectFactory(SubjectFactory subjectFactory);

   /**
    * get SubjectFactory
    *
    * @return the subjectFactory
    */
   public SubjectFactory getSubjectFactory();
}
