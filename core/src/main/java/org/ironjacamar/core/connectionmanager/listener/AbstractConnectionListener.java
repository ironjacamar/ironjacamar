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

import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.core.api.connectionmanager.pool.FlushMode;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;
import org.ironjacamar.core.spi.transaction.ConnectableResourceListener;
import org.ironjacamar.core.tracer.Tracer;

import java.util.HashMap;
import java.util.Map;
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
public abstract class AbstractConnectionListener implements ConnectionListener, ConnectableResourceListener
{
   /** The connection manager */
   protected ConnectionManager cm;
   
   /** The managed connection */
   private ManagedConnection mc;

   /** The managed connection pool*/
   private ManagedConnectionPool mcp;

   /** The credential */
   private Credential credential;
   
   /** The state */
   private AtomicInteger state;

   /** Connection handles */
   protected CopyOnWriteArraySet<Object> connectionHandles;
   
   /** Connection traces */
   protected Map<Object, Exception> connectionTraces;

   /** Last validated timestamp */
   private long validated;

   /** Last fromPool timestamp */
   private long fromPool;

   /** Last toPool timestamp */
   private long toPool;

   /** Flush strategy **/
   private FlushStrategy flushStrategy;

   /**
    * Constructor
    * @param cm The connection manager
    * @param mc The managed connection
    * @param credential The credential
    * @param mcp The ManagedConnectionPool
    * @param flushStrategy The FlushStrategy
    */
   public AbstractConnectionListener(ConnectionManager cm, ManagedConnection mc, Credential credential,
         ManagedConnectionPool mcp, FlushStrategy flushStrategy)
   {
      this.cm = cm;
      this.mc = mc;
      this.mcp = mcp;
      this.flushStrategy = flushStrategy;
      this.credential = credential;
      this.state = new AtomicInteger(FREE);
      this.connectionHandles = new CopyOnWriteArraySet<Object>();

      if (cm.getConnectionManagerConfiguration().isTracking() != null &&
          cm.getConnectionManagerConfiguration().isTracking().booleanValue())
         this.connectionTraces = new HashMap<Object, Exception>();

      long timestamp = System.currentTimeMillis();

      this.validated = timestamp;
      this.fromPool = timestamp;
      this.toPool = timestamp;

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
      if (removeConnection(connection) && cm.getCachedConnectionManager() != null)
      {
         cm.getCachedConnectionManager().unregisterConnection(cm, this, connection);
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
      if (removeConnection(connection) && cm.getCachedConnectionManager() != null)
      {
         cm.getCachedConnectionManager().unregisterConnection(cm, this, connection);
      }

      if (cm.getCachedConnectionManager() != null)
      {
         for (Object c : connectionHandles)
            cm.getCachedConnectionManager().unregisterConnection(cm, this, c);
      }

      clearConnections();
      
      haltCatchFire();
      
      cm.returnConnectionListener(this, true);

      if (flushStrategy == FlushStrategy.FAILING_CONNECTION_ONLY)
      {
         mcp.prefill();
      }
      else if (flushStrategy == FlushStrategy.INVALID_IDLE_CONNECTIONS)
      {
         mcp.flush(FlushMode.INVALID);
      }
      else if (flushStrategy == FlushStrategy.IDLE_CONNECTIONS)
      {
         mcp.flush(FlushMode.IDLE);
      }
      else if (flushStrategy == FlushStrategy.GRACEFULLY)
      {
         mcp.flush(FlushMode.GRACEFULLY);
      }
      else if (flushStrategy == FlushStrategy.ENTIRE_POOL)
      {
         mcp.flush(FlushMode.ALL);
      }
      else if (flushStrategy == FlushStrategy.ALL_INVALID_IDLE_CONNECTIONS)
      {
         cm.getPool().flush(FlushMode.INVALID);
      }
      else if (flushStrategy == FlushStrategy.ALL_IDLE_CONNECTIONS)
      {
         cm.getPool().flush(FlushMode.IDLE);
      }
      else if (flushStrategy == FlushStrategy.ALL_GRACEFULLY)
      {
         cm.getPool().flush(FlushMode.GRACEFULLY);
      }
      else if (flushStrategy == FlushStrategy.ALL_CONNECTIONS)
      {
         cm.getPool().flush(FlushMode.ALL);
      }
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
   public ManagedConnectionPool getManagedConnectionPool()
   {
      return mcp;
   }

   /**
    * {@inheritDoc}
    */
   public Object getConnection() throws ResourceException
   {
      Object result = mc.getConnection(credential.getSubject(), credential.getConnectionRequestInfo());

      addConnection(result);

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
   public boolean addConnection(Object c)
   {
      if (Tracer.isEnabled())
         Tracer.getConnection(cm.getPool().getConfiguration().getId(), mcp, this, c);

      if (connectionTraces != null)
         connectionTraces.put(c, new Exception());

      return connectionHandles.add(c);
   }
   
   /**
    * {@inheritDoc}
    */
   public boolean removeConnection(Object c)
   {
      if (Tracer.isEnabled())
         Tracer.returnConnection(cm.getPool().getConfiguration().getId(), mcp, this, c);

      if (connectionTraces != null)
         connectionTraces.remove(c);

      return connectionHandles.remove(c);
   }
   
   /**
    * {@inheritDoc}
    */
   public void clearConnections()
   {
      if (Tracer.isEnabled())
      {
         for (Object c : connectionHandles)
            Tracer.returnConnection(cm.getPool().getConfiguration().getId(), mcp, this, c);
      }

      if (connectionTraces != null)
         connectionTraces.clear();

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
   public long getFromPool()
   {
      return fromPool;
   }

   /**
    * {@inheritDoc}
    */
   public void fromPool()
   {
      fromPool = System.currentTimeMillis();
   }

   /**
    * {@inheritDoc}
    */
   public long getToPool()
   {
      return toPool;
   }

   /**
    * {@inheritDoc}
    */
   public void toPool() throws ResourceException
   {
      toPool = System.currentTimeMillis();
   }

   /**
    * {@inheritDoc}
    */
   public void handleCreated(Object h)
   {
      addConnection(h);
   }

   /**
    * {@inheritDoc}
    */
   public void handleClosed(Object h)
   {
      // This is never called
      removeConnection(h);
   }

   /**
    * Halt and Catch Fire
    */
   void haltCatchFire()
   {
      // Do nothing by default
   }
   
   /**
    * {@inheritDoc}
    */
   public int compareTo(Object o)
   {
      return 0;
   }
}
