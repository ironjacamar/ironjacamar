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

import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.FREE;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ManagedConnection;

/**
 * The abstract connection listener
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractConnectionListener implements ConnectionListener
{
   /** The connection manager */
   protected ConnectionManager cm;
   
   /** The managed connection */
   private ManagedConnection mc;

   /** The credential */
   private Credential credential;
   
   /** The state */
   private AtomicInteger state;

   /** Connection handles */
   protected CopyOnWriteArraySet<Object> connectionHandles;
   
   /** Last validated timestamp */
   private long validated;

   /**
    * Constructor
    * @param cm The connection manager
    * @param mc The managed connection
    * @param credential The credential
    */
   public AbstractConnectionListener(ConnectionManager cm, ManagedConnection mc, Credential credential)
   {
      this.cm = cm;
      this.mc = mc;
      this.credential = credential;
      this.state = new AtomicInteger(FREE);
      this.connectionHandles = new CopyOnWriteArraySet<Object>();
      this.validated = System.currentTimeMillis();

      mc.addConnectionEventListener(this);
   }

   /**
    * {@inheritDoc}
    */
   public boolean changeState(int currentState, int newState)
   {
      return state.compareAndSet(currentState, newState);
   }

   /**
    * {@inheritDoc}
    */
   public int getState()
   {
      return state.get();
   }

   /**
    * {@inheritDoc}
    */
   public void setState(int state)
   {
      this.state.set(state);
   }
   
   /**
    * {@inheritDoc}
    */
   public Credential getCredential()
   {
      return credential;
   }

   /**
    * {@inheritDoc}
    */
   public void connectionClosed(ConnectionEvent event)
   {
      Object connection = event.getConnectionHandle();
      if (connectionHandles.remove(connection))
      {
         if (cm.getCachedConnectionManager() != null)
         {
            cm.getCachedConnectionManager().unregisterConnection(cm, this, connection);
         }
      }

      if (connectionHandles.size() == 0 && !isEnlisted())
         cm.returnConnectionListener(this, false);
   }
   
   /**
    * {@inheritDoc}
    */
   public void connectionErrorOccurred(ConnectionEvent event)
   {
      Object connection = event.getConnectionHandle();
      if (connectionHandles.remove(connection))
      {
         if (cm.getCachedConnectionManager() != null)
         {
            cm.getCachedConnectionManager().unregisterConnection(cm, this, connection);
         }
      }

      if (connectionHandles.size() == 0 && !isEnlisted())
         cm.returnConnectionListener(this, true);
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
   public Object getConnection() throws ResourceException
   {
      Object result = mc.getConnection(credential.getSubject(), credential.getConnectionRequestInfo());

      connectionHandles.add(result);

      return result;
   }
   
   /**
    * {@inheritDoc}
    */
   public Set<Object> getConnections()
   {
      return connectionHandles;
   }
   
   /**
    * {@inheritDoc}
    */
   public void addConnection(Object c)
   {
      connectionHandles.add(c);
   }
   
   /**
    * {@inheritDoc}
    */
   public void clearConnections()
   {
      connectionHandles.clear();
   }
   
   /**
    * {@inheritDoc}
    */
   public long getValidated()
   {
      return validated;
   }

   /**
    * {@inheritDoc}
    */
   public void validated()
   {
      validated = System.currentTimeMillis();
   }

   /**
    * {@inheritDoc}
    */
   public int compareTo(Object o)
   {
      return 0;
   }
}
