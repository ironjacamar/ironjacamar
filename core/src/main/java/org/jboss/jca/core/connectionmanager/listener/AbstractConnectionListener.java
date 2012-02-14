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

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
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
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a> 
 */
public abstract class AbstractConnectionListener implements ConnectionListener
{
   private final CoreLogger log;
   
   /** Log trace */
   protected boolean trace;
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** Connection Manager */
   private final ConnectionManager cm;
      
   /** Managed connection */
   private final ManagedConnection managedConnection;
   
   /** Pool for this connection */
   private final Pool pool;
   
   /** Pool internal context */
   private final Object internalManagedPoolContext;

   /** Flush strategy */
   private FlushStrategy flushStrategy;
   
   /** Connection State */
   private ConnectionState state = ConnectionState.NORMAL;
   
   /** Connection handles */
   private final CopyOnWriteArrayList<Object> connectionHandles = new CopyOnWriteArrayList<Object>();
      
   /** Track by transaction or not */
   private final AtomicBoolean trackByTx = new AtomicBoolean(false);
   
   /** Connection last use */
   private long lastUse;
   
   /** Connection last validated time */
   private long lastValidated;
   
   /**
    * Creates a new instance of the listener that is responsible for
    * tracking the owned connection instance.
    * @param cm connection manager
    * @param managedConnection managed connection
    * @param pool pool
    * @param context pool internal context
    * @param flushStrategy flushStrategy
    */
   protected AbstractConnectionListener(ConnectionManager cm, ManagedConnection managedConnection, 
                                        Pool pool, Object context, FlushStrategy flushStrategy)
   {
      this.cm = cm;
      this.managedConnection = managedConnection;
      this.pool = pool;
      this.internalManagedPoolContext = context;
      this.flushStrategy = flushStrategy;
      this.log = getLogger();
      this.trace = log.isTraceEnabled();
      this.lastUse = System.currentTimeMillis();
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
   public void delist() throws ResourceException
   {
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
   public Object getContext()
   {
      return internalManagedPoolContext;
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
      return connectionHandles.isEmpty();
   }

   /**
    * {@inheritDoc}
    */   
   public boolean isTimedOut(long timeout)
   {      
      return lastUse < timeout;
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
      }
      else
      {
         log.unregisteredNullHandleManagedConnection(managedConnection);
      }
      
      if (trace)
      {
         log.trace("unregisterConnection: " + connectionHandles.size() + " handles left");  
      }            
   }
   
   /**
    * Unregister connections.
    */
   public  void unregisterConnections()
   {
      if (getCachedConnectionManager() != null)
      {
         for (Object handle : connectionHandles)
         {
            getCachedConnectionManager().unregisterConnection(getConnectionManager(), handle);
         }
      }

      connectionHandles.clear();
   }
   

   /**
    * {@inheritDoc}
    */   
   public void used()
   {
      lastUse = System.currentTimeMillis();      
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
            Throwable cause = event.getException();
            if (cause == null)
            {
               cause = new Exception("No exception was reported");  
            }
            
            log.connectionErrorOccured(this, cause);
         }
         else
         {
            Throwable cause = new Exception("No exception was reported");
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
      
      getConnectionManager().returnManagedConnection(this, true);      

      if (flushStrategy == FlushStrategy.IDLE_CONNECTIONS)
      {
         pool.flush();
      }
      else if (flushStrategy == FlushStrategy.ENTIRE_POOL)
      {
         pool.flush(true);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public boolean controls(Object connection, ManagedConnection mc)
   {
      if (managedConnection.equals(mc))
      {
         if (connectionHandles.contains(connection))
            return true;
      }

      return false;
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
    * Compare
    * @param o The other object
    * @return 0 if equal; -1 if less than based on lastUse; otherwise 1
    */
   public int compareTo(Object o)
   {
      if (this == o)
         return 0;

      if (!(o instanceof AbstractConnectionListener))
         throw new ClassCastException(bundle.notCorrectTypeWhenClassCast(o.getClass().getName()));

      final AbstractConnectionListener acl = (AbstractConnectionListener)o;

      if (lastUse < acl.lastUse)
         return -1;

      return 1;
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
      buffer.append(" lastUse=").append(lastUse);
      buffer.append(" trackByTx=").append(trackByTx.get());
      buffer.append(" pool=").append(pool);
      buffer.append(" pool internal context=").append(internalManagedPoolContext);
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
