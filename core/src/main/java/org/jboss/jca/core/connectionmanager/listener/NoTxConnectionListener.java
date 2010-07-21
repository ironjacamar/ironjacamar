/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ManagedConnection;


/**
 * NoTx Connection Listener.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a> 
 * @see AbstractConnectionListener
 */
public class NoTxConnectionListener extends AbstractConnectionListener
{
   /**
    * Creates a new no-tx listener.
    * @param cm connection manager
    * @param mc managed connection
    * @param pool pool
    * @param context context
    */
   public NoTxConnectionListener(final ConnectionManager cm, final ManagedConnection mc, 
                                 final Pool pool, final Object context)
   {
      super(cm, mc, pool, context);
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
            getCachedConnectionManager().unregisterConnection(getConnectionManager(), ce.getConnectionHandle());
         }
         catch (Throwable t)
         {
            getLog().info("Throwable from unregisterConnection", t);
         }
      }

      getConnectionManager().unregisterAssociation(this, ce.getConnectionHandle());
      
      if (isManagedConnectionFree())
      {
         getConnectionManager().returnManagedConnection(this, false);
      }
   }
}
