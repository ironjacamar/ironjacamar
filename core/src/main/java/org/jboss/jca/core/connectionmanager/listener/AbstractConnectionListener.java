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

import org.jboss.jca.core.connectionmanager.AbstractConnectionManager;
import org.jboss.jca.core.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.ManagedConnectionPool;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ManagedConnection;
import javax.transaction.SystemException;

import org.jboss.logging.Logger;

/**
 * Abstract implementation of the {@link ConnectionListener} interface
 * contract.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @version $Rev: $
 *
 */
public abstract class AbstractConnectionListener implements ConnectionListener
{
   private Logger log = Logger.getLogger(getClass());
   
   /**Wraps managed connection instance*/
   private final ManagedConnection managedConnection;
   
   /**Managed connection pool for this connection*/
   private final ManagedConnectionPool managedConnectionPool;
   
   /**Pool internal context*/
   private final Object internalManagedPoolContext;
   
   /**Connection State*/
   private ConnectionState state = ConnectionState.NORMAL;
   
   /**Connection handles*/
   private final CopyOnWriteArrayList<Object> connectionHandles = new CopyOnWriteArrayList<Object>();
      
   /**Track by transaction or not*/
   private AtomicBoolean trackByTx = new AtomicBoolean(false);
   
   /**Connection permit*/
   private boolean permit;
   
   /**Connection last use*/
   private long lastUse;
   
   /**Connection last validated time*/
   private long lastValidated;
   
   /**Log trace*/
   protected boolean trace;
   
   /**Connection Manager*/
   private AbstractConnectionManager cm;
      
   /**
    * Creates a new instance of the listener that is responsible for
    * tracking the owned connection instance.
    * @param managedConnection managed connection
    * @param managedConnectionPool managed connection pool
    * @param context pool internal context
    * @param cm connection manager
    */
   protected AbstractConnectionListener(AbstractConnectionManager cm, ManagedConnection managedConnection, 
         ManagedConnectionPool managedConnectionPool, Object context)
   {
      this.cm = cm;
      this.managedConnection = managedConnection;
      this.managedConnectionPool = managedConnectionPool;
      this.internalManagedPoolContext = context;
      trace = this.log.isTraceEnabled();
      lastUse = System.currentTimeMillis();
   }
   
   /**
    * Gets cached connection manager
    * @return cached connection manager
    */
   protected CachedConnectionManager getCachedConnectionManager()
   {
      return this.cm.getCachedConnectionManager();
   }
   
   /**
    * Gets connection manager.
    * @return connection manager
    */
   protected AbstractConnectionManager getConnectionManager()
   {
      return this.cm;
   }
   
   /**
    * Gets logger.
    * @return logger instance
    */
   protected Logger getLog()
   {
      return this.log;
   }
   
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
      return this.internalManagedPoolContext;
   }

   /**
    * {@inheritDoc}
    */   
   public long getLastValidatedTime()
   {      
      return this.lastValidated;
   }

   /**
    * {@inheritDoc}
    */   
   public ManagedConnection getManagedConnection()
   {      
      return this.managedConnection;
   }

   /**
    * {@inheritDoc}
    */   
   public ManagedConnectionPool getManagedConnectionPool()
   {      
      return this.managedConnectionPool;
   }

   /**
    * {@inheritDoc}
    */   
   public ConnectionState getState()
   {      
      return this.state;
   }

   /**
    * {@inheritDoc}
    */   
   public void grantPermit(boolean value)
   {      
      this.permit = value;
   }

   /**
    * {@inheritDoc}
    */   
   public boolean hasPermit()
   {      
      return this.permit;
   }

   /**
    * {@inheritDoc}
    */   
   public boolean isManagedConnectionFree()
   {      
      return this.connectionHandles.isEmpty();
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
      return this.trackByTx.get();
   }

   /**
    * {@inheritDoc}
    */   
   public void registerConnection(Object handle)
   {
      this.connectionHandles.add(handle);      
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
      if (!this.connectionHandles.remove(handle))
      {
         log.info("Unregistered handle that was not registered! " + handle + " for managedConnection: " + 
               this.managedConnection);
      }
      
      if (trace)
      {
         log.trace("unregisterConnection: " + this.connectionHandles.size() + " handles left");  
      }            
   }
   
   /**
    * Unregister connections.
    */
   protected  void unregisterConnections()
   {
      try
      {
         Iterator<Object> itHandles = this.connectionHandles.iterator();
         
         while (itHandles.hasNext())
         {
            Object handle = itHandles.next();
            this.log.info("Unregister connection handle : " + handle + " from Cached connection manager");
            //getCcm().unregisterConnection(BaseConnectionManager2.this, i.next());
         }
      }
      finally
      {
         this.connectionHandles.clear();
      }
   }
   

   /**
    * {@inheritDoc}
    */   
   public void used()
   {
      this.lastUse = System.currentTimeMillis();      
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
            
            log.warn("Connection error occured: " + this, cause);
         }
         else
         {
            Throwable cause = new Exception("No exception was reported");
            log.warn("Unknown Connection error occured: " + this, cause);
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
         log.warn("Notified of error on a different managed connection?");  
      }
      
      getConnectionManager().returnManagedConnection(this, true);      
      
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
   // For debugging
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
      buffer.append(" managed connection=").append(this.managedConnection);
      buffer.append(" connection handles=").append(this.connectionHandles.size());
      buffer.append(" lastUse=").append(lastUse);
      buffer.append(" permit=").append(permit);
      buffer.append(" trackByTx=").append(trackByTx.get());
      buffer.append(" managed connection pool=").append(this.managedConnectionPool);
      buffer.append(" pool internal context=").append(this.internalManagedPoolContext);
      toString(buffer);
      buffer.append(']');
      
      return buffer.toString();
   }
   
   /**
    * Add specific properties.
    * @param buffer buffer instance
    */
   // For debugging
   protected void toString(StringBuffer buffer)
   {
      
   }
      
}
