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

package org.jboss.jca.core.connectionmanager.notx;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.connectionmanager.AbstractConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionRecord;
import org.jboss.jca.core.connectionmanager.NoTxConnectionManager;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.NoTxConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

import java.util.Collection;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.transaction.SystemException;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 * Non transactional connection manager implementation.
 *
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class NoTxConnectionManagerImpl extends AbstractConnectionManager implements NoTxConnectionManager
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, NoTxConnectionManager.class.getName());

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
 
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /**
    * Default constructor.
    */
   public NoTxConnectionManagerImpl()
   {
   }

   /**
    * Get the logger.
    * @return The value
    */
   protected CoreLogger getLogger()
   {
      return log;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener createConnectionListener(ManagedConnection managedConnection, ManagedConnectionPool mcp)
      throws ResourceException
   {
      ConnectionListener cli = new NoTxConnectionListener(this, managedConnection, getPool(), 
                                                          mcp, getFlushStrategy(), getTracking());
      managedConnection.addConnectionEventListener(cli);

      return cli;
   }

   @Override
   public void transactionStarted(Collection<ConnectionRecord> conns) throws SystemException
   {
      // Doing nothing
   }

   @Override
   public TransactionIntegration getTransactionIntegration()
   {
      return null;
   }

   @Override
   public boolean isTransactional()
   {
      return false;
   }
}
