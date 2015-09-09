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

package org.ironjacamar.core.connectionmanager.listener;

import org.ironjacamar.core.api.connectionmanager.ConnectionManager;

import java.util.concurrent.CopyOnWriteArraySet;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;

/**
 * The abstract connection listener
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractConnectionListener implements ConnectionListener
{
   /** The connection manager */
   private ConnectionManager cm;
   
   /** The managed connection */
   private ManagedConnection mc;

   /** Connection handles */
   private CopyOnWriteArraySet<Object> connectionHandles;
   
   /**
    * Constructor
    * @param cm The connection manager
    * @param mc The managed connection
    */
   public AbstractConnectionListener(ConnectionManager cm, ManagedConnection mc)
   {
      this.cm = cm;
      this.mc = mc;
      this.connectionHandles = new CopyOnWriteArraySet<Object>();

      mc.addConnectionEventListener(this);
   }

   /**
    * {@inheritDoc}
    */
   public void connectionClosed(ConnectionEvent event)
   {
      connectionHandles.remove(event.getConnectionHandle());

      if (connectionHandles.size() == 0)
         cm.returnManagedConnection(this, false);
   }
   
   /**
    * {@inheritDoc}
    */
   public void connectionErrorOccurred(ConnectionEvent event)
   {
      connectionHandles.remove(event.getConnectionHandle());

      if (connectionHandles.size() == 0)
         cm.returnManagedConnection(this, true);
   }

   /**
    * {@inheritDoc}
    */
   public void localTransactionStarted(ConnectionEvent event)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void localTransactionCommitted(ConnectionEvent event)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void localTransactionRolledback(ConnectionEvent event)
   {
   }
   
   /**
    * {@inheritDoc}
    */
   public ManagedConnection getManagedConnection()
   {
      return mc;
   }

   /**
    * {@inheritDoc}
    */
   public Object getConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      Object result = mc.getConnection(subject, cri);

      connectionHandles.add(result);

      return result;
   }
   
   /**
    * {@inheritDoc}
    */
   public int compareTo(Object o)
   {
      return 0;
   }
}
