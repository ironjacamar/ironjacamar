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

package org.ironjacamar.core.connectionmanager.listener.dflt;

import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.AbstractTransactionalConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;

import javax.resource.spi.ManagedConnection;
import javax.transaction.xa.XAResource;

/**
 * The XATransaction connection listener
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XATransactionConnectionListener extends AbstractTransactionalConnectionListener
{
   /**
    * Constructor
    * @param cm The connection manager
    * @param mc The managed connection
    * @param credential The credential
    * @param xaResource The XAResource
    * @param xaResourceTimeout The timeout for the XAResource instance
    * @param mcp The ManagedConnectionPool
    * @param flushStrategy The FlushStrategy
    */
   public XATransactionConnectionListener(ConnectionManager cm, ManagedConnection mc, Credential credential,
                                          XAResource xaResource, int xaResourceTimeout,
                                          ManagedConnectionPool mcp, FlushStrategy flushStrategy)
   {
      super(cm, mc, credential, xaResource, xaResourceTimeout, mcp, flushStrategy);
   }
}
