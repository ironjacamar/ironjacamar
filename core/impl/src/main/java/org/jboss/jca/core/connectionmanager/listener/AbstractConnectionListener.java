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
import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.api.connectionmanager.pool.FlushMode;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.spi.transaction.ConnectableResourceListener;
import org.jboss.jca.core.tracer.Tracer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ManagedConnection;
import javax.transaction.SystemException;

import org.jboss.logging.Messages;

/**
 * Abstract implementation of the {@link ConnectionListener} interface
 * contract.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
public abstract class AbstractConnectionListener implements ConnectionListener, ConnectableResourceListener
{
   private final CoreLogger log;

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** Connection Manager */
   private final ConnectionManager cm;
      
   /** Managed connection */
   private final ManagedConnection managedConnection;
   
   /** Pool for this connection */
   private final Pool pool;
   
   /** Managed connection pool */
   private final ManagedConnectionPool managedConnectionPool;

   /** Flush strategy */
   private FlushStrategy flushStrategy;
   
   /** Connection State */
   private ConnectionState state = ConnectionState.NORMAL;
   
   /** Connection handles */
   protected CopyOnWriteArraySet<Object> connectionHandles = new CopyOnWriteArraySet<Object>();

   /** Connection traces */
   protected Map<Object, Exception> connectionTraces;
      
   /** Track by transaction or not */
   private final AtomicBoolean trackByTx = new AtomicBoolean(false);
   
   /** Connection last returned */
   private long lastReturned;
   
   /** Connection last validated time */
   private long lastValidated;

   /** Connection check out time */
   private long lastCheckedOut;

   /** Enlisted */
   private boolean enlisted;

   /** Tracking */
   protected Boolean tracking;

   /** The reported exception */
   private Exception reportedException;
   
   /**
    * Creates a new instance of the listener that is responsible for
    * tracking the owned connection instance.
    * @param cm connection manager
    * @param managedConnection managed connection
    * @param pool pool
    * @param mcp managed connection pool
    * @param flushStrategy flushStrategy
    * @param tracking tracking
    */
   protected AbstractConnectionListener(ConnectionManager cm, ManagedConnection managedConnection, 
                                        Pool pool, ManagedConnectionPool mcp, FlushStrategy flushStrategy,
                                        Boolean tracking)
   {
      this.cm = cm;
      this.managedConnection = managedConnection;
      this.pool = pool;
      this.managedConnectionPool = mcp;
      this.flushStrategy = flushStrategy;
      this.log = getLogger();
      this.enlisted = false;

      long createdTime = System.currentTimeMillis();
      this.lastReturned = createdTime;
      this.lastValidated = createdTime;
      this.lastCheckedOut = createdTime;

      this.tracking = tracking;

      if (tracking != null && tracking.booleanValue())
         this.connectionTraces = new HashMap<Object, Exception>();

      this.reportedException = null;
   }

   /**
    * Gets cached connection manager
    * @return cached connection manager
    */
   protected CachedConnectionManager getCachedConnectionManager()
   {
      return cm.getCachedConnectionManager();
   }
   
   /**
    * Gets connection manager.
    * @return connection manager
    */
   protected ConnectionManager getConnectionManager()
   {
      return cm;
   }
   
   /**
    * Get the logger
    * @return The value
    */
   protected abstract CoreLogger getLogger();
   
   /**
    * {@inheritDoc}
    */
   public int getNumberOfConnections()
   {
      return connectionHandles.size();
   }

   /**
    * {@inheritDoc}
    */   
   public boolean isEnlisted()
   {
      return enlisted;
   }

   /**
    * Set the enlisted flag
    * @param v The value
    */   
   void setEnlisted(boolean v)
   {
      enlisted = v;
   }

   /**
    * {@inheritDoc}
    */
   public boolean delist() throws ResourceException
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */   
   public void enlist() throws SystemException
   {
   }

   /**
    * {@inheritDoc}
    */   
   public ManagedConnectionPool getManagedConnectionPool()
   {
      return managedConnectionPool;
   }

   /**
    * {@inheritDoc}
    */
   public long getLastValidatedTime()
   {      
      return lastValidated;
   }

   /**
    * {@inheritDoc}
    */
   public long getLastReturnedTime()
   {
      return lastReturned;
   }

   /**
    * {@inheritDoc}
    */
   public long getLastCheckedOutTime()
   {
      return lastCheckedOut;
   }

   /**
    * {@inheritDoc}
    */
   public void setLastCheckedOutTime(long v)
   {
      lastCheckedOut = v;
   }

   /**
    * {@inheritDoc}
    */   
   public ManagedConnection getManagedConnection()
   {      
      return managedConnection;
   }

   /**
    * {@inheritDoc}
    */   
   public Pool getPool()
   {      
      return pool;
   }

   /**
    * {@inheritDoc}
    */   
   public ConnectionState getState()
   {      
      return state;
   }

   /**
    * {@inheritDoc}
    */   
   public boolean isManagedConnectionFree()
   {
      if (log.isTraceEnabled())
         log.tracef("[%s] isManagedConnectionFree: %s", getIdentifier(), connectionHandles.isEmpty());

      return connectionHandles.isEmpty();
   }

   /**
    * {@inheritDoc}
    */   
   public boolean isTimedOut(long timeout)
   {      
      return lastReturned < timeout;
   }

   /**
    * {@inheritDoc}
    */   
   public boolean isTrackByTx()
   {      
      return trackByTx.get();
   }

   /**
    * {@inheritDoc}
    */   
   public void registerConnection(Object handle)
   {
      if (handle != null)
      {
         connectionHandles.add(handle);

         if (Tracer.isEnabled())
            Tracer.getConnection(pool != null ? pool.getName() : null, managedConnectionPool, this, handle);

         if (log.isTraceEnabled())
            log.tracef("[%s] registerConnection: %s [size=%s] (%s)", getIdentifier(), handle,
                       connectionHandles.size(), connectionHandles);

         if (tracking != null && tracking.booleanValue())
            connectionTraces.put(handle, new Exception());
      }
      else
      {
         log.registeredNullHandleManagedConnection(managedConnection);
      }
   }

   /**
    * {@inheritDoc}
    */   
   public void setLastValidatedTime(long lastValidated)
   {
      this.lastValidated = lastValidated;      
   }

   /**
    * {@inheritDoc}
    */   
   public void setState(ConnectionState newState)
   {
      this.state = newState;      
   }

   /**
    * {@inheritDoc}
    */   
   public void setTrackByTx(boolean trackByTx)
   {      
      this.trackByTx.set(trackByTx);
   }

   /**
    * {@inheritDoc}
    */   
   public void tidyup() throws ResourceException
   {
   }

   /**
    * {@inheritDoc}
    */   
   public void unregisterConnection(Object handle)
   {
      if (handle != null)
      {
         if (!connectionHandles.remove(handle))
         {
            log.unregisteredHandleNotRegistered(handle, managedConnection);
         }

         if (Tracer.isEnabled())
            Tracer.returnConnection(pool != null ? pool.getName() : null, managedConnectionPool, this, handle);

         if (tracking != null && tracking.booleanValue())
            connectionTraces.remove(handle);
      }
      else
      {
         log.unregisteredNullHandleManagedConnection(managedConnection);
      }
      
      if (log.isTraceEnabled())
         log.tracef("[%s] unregisterConnection: " + connectionHandles.size() + " handles left (%s)",
                    getIdentifier(), connectionHandles);
   }
   
   /**
    * Unregister connections.
    */
   public  void unregisterConnections()
   {
      if (log.isTraceEnabled())
         log.tracef("[%s] unregisterConnections", getIdentifier());

      if (getCachedConnectionManager() != null)
      {
         for (Object handle : connectionHandles)
         {
            getCachedConnectionManager().unregisterConnection(getConnectionManager(), this, handle);
         }
      }

      if (Tracer.isEnabled())
      {
         for (Object handle : connectionHandles)
         {
            Tracer.returnConnection(pool != null ? pool.getName() : null, managedConnectionPool, this, handle);
         }
      }

      connectionHandles.clear();

      if (tracking != null && tracking.booleanValue())
         connectionTraces.clear();
   }
   

   /**
    * {@inheritDoc}
    */   
   public void toPool()
   {
      lastReturned = System.currentTimeMillis();
   }

   /**
    * {@inheritDoc}
    */   
   public void connectionClosed(ConnectionEvent event)
   {
   }

   /**
    * {@inheritDoc}
    */   
   public void connectionErrorOccurred(ConnectionEvent event)
   {
      if (state == ConnectionState.NORMAL)
      {
         if (event != null)
         {
            Exception cause = event.getException();
            if (cause == null)
            {
               cause = new Exception("No exception was reported");  
            }
            else
            {
               reportedException = cause;
            }
            
            log.connectionErrorOccured(this, cause);
         }
         else
         {
            Exception cause = new Exception("No exception was reported");
            log.unknownConnectionErrorOccured(this, cause);
         }
      }
      
      try
      {
         unregisterConnections();
      }
      catch (Throwable t)
      {
         //ignore, it wasn't checked out.
      }
      
      if (event != null && event.getSource() != getManagedConnection())
      {
         log.notifiedErrorDifferentManagedConnection();
      }

      haltCatchFire();
      
      getConnectionManager().returnManagedConnection(this, true);      

      if (flushStrategy == FlushStrategy.FAILING_CONNECTION_ONLY)
      {
         managedConnectionPool.prefill();
      }
      else if (flushStrategy == FlushStrategy.INVALID_IDLE_CONNECTIONS)
      {
         Collection<ConnectionListener> toDestroy = new ArrayList<ConnectionListener>();
         managedConnectionPool.flush(FlushMode.INVALID, toDestroy);
         for (ConnectionListener connectionListener: toDestroy)
         {
            connectionListener.destroy();
         }
      }
      else if (flushStrategy == FlushStrategy.IDLE_CONNECTIONS)
      {
         Collection<ConnectionListener> toDestroy = new ArrayList<ConnectionListener>();
         managedConnectionPool.flush(FlushMode.IDLE, toDestroy);
         for (ConnectionListener connectionListener: toDestroy)
         {
            connectionListener.destroy();
         }
      }
      else if (flushStrategy == FlushStrategy.GRACEFULLY)
      {
         Collection<ConnectionListener> toDestroy = new ArrayList<ConnectionListener>();
         managedConnectionPool.flush(FlushMode.GRACEFULLY, toDestroy);
         for (ConnectionListener connectionListener: toDestroy)
         {
            connectionListener.destroy();
         }
      }
      else if (flushStrategy == FlushStrategy.ENTIRE_POOL)
      {
         Collection<ConnectionListener> toDestroy = new ArrayList<ConnectionListener>();
         managedConnectionPool.flush(FlushMode.ALL, toDestroy);
         for (ConnectionListener connectionListener: toDestroy)
         {
            connectionListener.destroy();
         }
      }
      else if (flushStrategy == FlushStrategy.ALL_INVALID_IDLE_CONNECTIONS)
      {
         pool.flush(FlushMode.INVALID);
      }
      else if (flushStrategy == FlushStrategy.ALL_IDLE_CONNECTIONS)
      {
         pool.flush(FlushMode.IDLE);
      }
      else if (flushStrategy == FlushStrategy.ALL_GRACEFULLY)
      {
         pool.flush(FlushMode.GRACEFULLY);
      }
      else if (flushStrategy == FlushStrategy.ALL_CONNECTIONS)
      {
         pool.flush(FlushMode.ALL);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public Exception getException()
   {
      return reportedException;
   }

   /**
    * {@inheritDoc}
    */
   public boolean controls(ManagedConnection mc, Object connection)
   {
      if (managedConnection.equals(mc))
      {
         if (connection == null || connectionHandles.contains(connection))
            return true;
      }

      return false;
   }
   
   /**
    * {@inheritDoc}
    */
   public void dissociate() throws ResourceException
   {
      // Nothing by default
   }

   /**
    * {@inheritDoc}
    */
   public boolean supportsLazyAssociation()
   {
      return managedConnection instanceof javax.resource.spi.DissociatableManagedConnection;
   }

   /**
    * {@inheritDoc}
    */
   public boolean supportsLazyEnlistment()
   {
      return managedConnection instanceof javax.resource.spi.LazyEnlistableManagedConnection;
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
   public void localTransactionStarted(ConnectionEvent event)
   {
   }

   /**
    * {@inheritDoc}
    */   
   public void handleCreated(Object h)
   {
      registerConnection(h);

      if (getCachedConnectionManager() != null)
      {
         getCachedConnectionManager().registerConnection(getConnectionManager(), this, h);
      }
   }

   /**
    * {@inheritDoc}
    */   
   public void handleClosed(Object h)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void destroy()
   {
      if (getState() == ConnectionState.DESTROYED)
      {
         log.tracef("ManagedConnection is already destroyed %s", this);
         return;
      }

      getManagedConnectionPool().connectionListenerDestroyed(this);

      setState(ConnectionState.DESTROYED);

      final ManagedConnection mc = getManagedConnection();
      try
      {
         mc.destroy();
      }
      catch (Throwable t)
      {
         if (log.isDebugEnabled())
            log.debug("Exception destroying ManagedConnection " + this, t);
      }

      mc.removeConnectionEventListener(this);
   }

   /**
    * Halt and Catch Fire
    */
   void haltCatchFire()
   {
      // Do nothing by default
   }
   
   /**
    * Compare
    * @param o The other object
    * @return 0 if equal; -1 if less than based on lastReturned; otherwise 1
    */
   public int compareTo(Object o)
   {
      if (this == o)
         return 0;

      if (!(o instanceof AbstractConnectionListener))
         throw new ClassCastException(bundle.notCorrectTypeWhenClassCast(o.getClass().getName()));

      final AbstractConnectionListener acl = (AbstractConnectionListener)o;

      if (lastReturned < acl.lastReturned)
         return -1;

      return 1;
   }

   /**
    * Get string identifier
    * @return The value
    */
   private String getIdentifier()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(getClass().getSimpleName()).append('@').append(Integer.toHexString(System.identityHashCode(this)));
      return buffer.toString();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(100);
      buffer.append(getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[state=");
      
      if (state.equals(ConnectionState.NORMAL))
      {
         buffer.append("NORMAL");  
      }
      else if (state.equals(ConnectionState.DESTROY))
      {
         buffer.append("DESTROY");  
      }
      else if (state.equals(ConnectionState.DESTROYED))
      {
         buffer.append("DESTROYED");  
      }
      else
      {
         buffer.append("UNKNOWN?");  
      }
      buffer.append(" managed connection=").append(managedConnection);
      buffer.append(" connection handles=").append(connectionHandles.size());
      buffer.append(" lastReturned=").append(lastReturned);
      buffer.append(" lastValidated=").append(lastValidated);
      buffer.append(" lastCheckedOut=").append(lastCheckedOut);
      buffer.append(" trackByTx=").append(trackByTx.get());
      buffer.append(" pool=").append(pool);
      buffer.append(" mcp=").append(managedConnectionPool);
      toString(buffer);
      buffer.append(']');
      
      return buffer.toString();
   }
   
   /**
    * Add specific properties.
    * @param buffer buffer instance
    */
   protected void toString(StringBuffer buffer)
   {
      
   }
}
