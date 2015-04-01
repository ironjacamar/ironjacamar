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
package org.jboss.jca.core.connectionmanager.listener;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ManagedConnection;

import org.jboss.logging.Logger;

/**
 * NoTx Connection Listener.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 * @see AbstractConnectionListener
 */
public class NoTxConnectionListener extends AbstractConnectionListener
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, 
      NoTxConnectionListener.class.getName());

   /**
    * Creates a new no-tx listener.
    * @param cm connection manager
    * @param mc managed connection
    * @param pool pool
    * @param mcp mcp
    * @param flushStrategy flushStrategy
    * @param tracking tracking
    */
   public NoTxConnectionListener(final ConnectionManager cm, final ManagedConnection mc, 
                                 final Pool pool, final ManagedConnectionPool mcp, final FlushStrategy flushStrategy,
                                 final Boolean tracking)
   {
      super(cm, mc, pool, mcp, flushStrategy, tracking);
   }
   
   /**
    * {@inheritDoc}
    */
   protected CoreLogger getLogger()
   {
      return log;
   }

   /**
    * {@inheritDoc}
    */
   public void connectionClosed(ConnectionEvent ce)
   {
      if (getCachedConnectionManager() != null)
      {
         try
         {
            getCachedConnectionManager().unregisterConnection(getConnectionManager(), this, ce.getConnectionHandle());
         }
         catch (Throwable t)
         {
            log.debug("Throwable from unregisterConnection", t);
         }
      }

      getConnectionManager().unregisterAssociation(this, ce.getConnectionHandle());
      
      if (isManagedConnectionFree())
      {
         getConnectionManager().returnManagedConnection(this, false);
      }
   }
}
