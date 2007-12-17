/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.jca.spi.cm;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.resource.ResourceException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;

import org.jboss.jca.spi.ResourceExceptionUtil;
import org.jboss.jca.spi.pool.ManagedConnectionContextPool;
import org.jboss.logging.Logger;
import org.jboss.util.JBossObject;
import org.jboss.util.JBossStringBuilder;

/**
 * A ManagedConnectionContext.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.5 $
 */
public class ManagedConnectionContext extends JBossObject implements ConnectionEventListener
{
   /** The log */
   private static final Logger log = Logger.getLogger(ManagedConnectionContext.class);
   
   /** Initializing state */
   public static final int INITIALIZING = 0;
   
   /** In the pool state */
   public static final int POOL = 1;
   
   /** Cleanup state */
   public static final int CLEANUP = 2;
   
   /** Checked out state */
   public static final int INUSE = 3;
    
   /** Destroying state */
   public static final int DESTROYING = -1;
   
   /** Destroyed state */
   public static final int DESTROYED = -2;
   
   /** The context manager */
   protected ManagedConnectionContextManager contextManager;
   
   /** The managed connection factory */
   protected ManagedConnectionFactory managedConnectionFactory;
   
   /** The managed connection */
   protected ManagedConnection managedConnection;
   
   /** The pool */
   protected ManagedConnectionContextPool pool;
   
   /** The state */
   private AtomicInteger state = new AtomicInteger(INITIALIZING); 
   
   /** The connection handles */
   protected Set<Object> handles = new CopyOnWriteArraySet<Object>(); 
   
   /** Any cached connection set */
   protected Set<ManagedConnection> connectionSet;
   
   /**
    * Safely destroy a managed connecton
    * 
    * @param mc the managed connection
    */
   public static void safeDestroy(ManagedConnection mc)
   {
      if (mc == null)
      {
         log.error("Attempt to destroy null managed connection", new ApplicationServerInternalException("STACKTRACE"));
         return;
      }
      try
      {
         mc.destroy();
      }
      catch (Throwable ignored)
      {
         if (log.isTraceEnabled())
            log.trace("Ignored error destroying managed connection " + mc, ignored);
      }
   }
   
   /**
    * Create a new ManagedConnectionContext.
    * 
    * @param contextManager the context manager
    * @param managedConnectionFactory the managed connection factory
    * @param managedConnection the managed connection
    * @throws ResourceException for any error
    */
   public ManagedConnectionContext(ManagedConnectionContextManager contextManager, ManagedConnectionFactory managedConnectionFactory, ManagedConnection managedConnection) throws ResourceException
   {
      if (contextManager == null)
         throw new IllegalArgumentException("Null context manager");
      if (managedConnectionFactory == null)
         throw new IllegalArgumentException("Null managed connection factory");
      if (managedConnection == null)
         throw new IllegalArgumentException("Null managed connection");
      this.contextManager = contextManager;
      this.managedConnectionFactory = managedConnectionFactory;
      this.managedConnection = managedConnection;
      
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Add ConnectionEventListener: " + this);
      try
      {
         managedConnection.addConnectionEventListener(this);
      }
      catch (Throwable t)
      {
         throw ResourceExceptionUtil.checkResourceAdapterInternal(t);
      }

      changeState(INUSE);
   }
   
   /**
    * Get the state of the connection
    * 
    * @return the state
    */
   public int getState()
   {
      return state.get();
   }
   
   /**
    * Set the context in use
    * 
    * @param pool the pool
    * @throws ResourceException for any error
    */
   public void setInUse(ManagedConnectionContextPool pool) throws ResourceException
   {
      if (getState() != POOL)
         throw new ApplicationServerInternalException("Attempt to set in use when not in the pool " + this);
      if (this.pool != pool)
         throw new ApplicationServerInternalException("Attempt to change pool=" + pool + " context=" + this);
      changeState(INUSE);
   }
   
   /**
    * Set the pool
    * 
    * @param pool the pool
    */
   public void setPool(ManagedConnectionContextPool pool)
   {
      this.pool = pool;
   }
   
   /**
    * Get a connection from the managed connection
    * 
    * @param subject the subject
    * @param cri the connection request info
    * @return the connection
    * @throws ResourceException for any error
    */
   public Object getConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Get Connection context=" + this + " subject=" + subject + " cri=" + cri);
      
      if (getState() != INUSE)
         throw new ApplicationServerInternalException("Attempt to get connection when NOT in use: " + this);
      
      Object result = null;
      try
      {
         result = managedConnection.getConnection(subject, cri);
      }
      catch (Throwable t)
      {
         // @todo probably already happened?
         //ConnectionEvent event = new ConnectionEvent(managedConnection, ConnectionEvent.CONNECTION_ERROR_OCCURRED);
         //connectionErrorOccurred(event);
         throw ResourceExceptionUtil.checkResourceAdapterInternal(t);
      }
      if (result == null)
         throw new ResourceAdapterInternalException("No connection returned");
      registerHandle(trace, result);
      if (trace)
         log.trace("Got Connection result=" + result);
      return result;
   }
   
   public void connectionClosed(ConnectionEvent event)
   {
      boolean trace = log.isTraceEnabled();
      Object handle = event.getConnectionHandle();
      if (trace)
         log.trace("Connection Closed " + this + " handle=" + handle);
      
      unregisterHandle(trace, handle);
      
      boolean free = isFree();
      if (trace)
         log.trace("Free=" + free + " " + this);
      if (free)
         returnManagedConnection();
   }

   public void connectionErrorOccurred(ConnectionEvent event)
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI connectionErrorOccurred");
   }

   public void localTransactionCommitted(ConnectionEvent event)
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI localTransactionCommitted");
   }

   public void localTransactionRolledback(ConnectionEvent event)
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI localTransactionRolledback");
   }

   public void localTransactionStarted(ConnectionEvent event)
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI localTransactionStarted");
   }

   /**
    * Match a connection
    * 
    * @param subject the subject
    * @param cri the connection request info
    * @return true when it matches
    */
   public boolean match(Subject subject, ConnectionRequestInfo cri)
   {
      if (connectionSet == null)
         connectionSet = Collections.singleton(managedConnection);
      
      try
      {
         ManagedConnection result = managedConnectionFactory.matchManagedConnections(connectionSet, subject, cri);
         if (result == null || result != managedConnection)
            return false;
         else
            return true;
      }
      catch (Throwable t)
      {
         log.warn("Error matching connection: " + this + " subject=" + subject + " cri=" + cri, t);
         return false;
      }
   }
   
   /**
    * Destroy the managed connection context
    */
   public void destroy()
   {
      contextManager.removeManagedConnectionContext(this);
   }

   protected boolean cacheToString()
   {
      return false;
   }

   protected void toString(JBossStringBuilder buffer)
   {
      buffer.append("mc=").append(managedConnection);
      buffer.append(" state=");
      switch (getState())
      {
         case INITIALIZING:
         {
            buffer.append("INITIALIZING");
            break;
         }
         case POOL:
         {
            buffer.append("POOL");
            break;
         }
         case CLEANUP:
         {
            buffer.append("CLEANUP");
            break;
         }
         case INUSE:
         {
            buffer.append("INUSE");
            break;
         }
         case DESTROYING:
         {
            buffer.append("DESTROYING");
            break;
         }
         case DESTROYED:
         {
            buffer.append("DESTROYED");
            break;
         }
         default:
            buffer.append("???");
      }
      if (pool != null)
         buffer.append(" pool=").append(pool);
      buffer.append(" handles=" + handles.size());
   }
   
   /**
    * Destroy the managed connection context
    */
   protected void destroyContext()
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Destroy " + this);
      
      if (isDestroyed())
         return;
      
      if (pool != null)
      {
         pool.removeManagedConnectonContext(this);
         pool = null;
      }
      
      safeDestroy(managedConnection);
      changeState(DESTROYED);
      if (trace)
         log.trace("Destroyed " + this);
   }

   /**
    * Return the managed connection to the pool
    */
   protected void returnManagedConnection()
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Return ManagedConnection " + this);

      int state = getState();
      if (state == DESTROYING || state == DESTROYED)
         return;
      if (state != INUSE)
      {
         log.warn("Attempt to return connection when NOT in use " + this, new ApplicationServerInternalException("STACKTRACE"));
         return;
      }
      
      changeState(CLEANUP);
      if (trace)
         log.trace("Cleanup " + this);
      try
      {
         managedConnection.cleanup();
      }
      catch (Throwable t)
      {
         log.warn("Error during cleanup " + this, t);
         destroy();
         return;
      }
      changeState(POOL);
      if (trace)
         log.trace("Return to pool " + this);
      pool.returnManagedConnectonContext(this);
   }
   
   /**
    * Cleanup the connection
    * 
    * @throws ResourceException for any error
    */
   protected void cleanup() throws ResourceException
   {
      try
      {
         managedConnection.cleanup();
      }
      catch (Throwable t)
      {
         throw ResourceExceptionUtil.checkResourceAdapterInternal(t);
      }
   }
   
   /**
    * Whether the managed connection is free
    * 
    * @return true when there are no handles
    */
   protected boolean isFree()
   {
      return handles.isEmpty();
   }

   /**
    * Register a handle
    * 
    * @param trace whether trace is enabled
    * @param handle the connection handle
    */
   protected void registerHandle(boolean trace, Object handle)
   {
      if (trace)
         log.trace("Register handle=" + handle + " context=" + this);
      handles.add(handle);
   }
   
   /**
    * Unregister a handle
    * 
    * @param trace whether trace is enabled
    * @param handle the connection handle
    */
   protected void unregisterHandle(boolean trace, Object handle)
   {
      if (trace)
         log.trace("Unregister handle=" + handle + " context=" + this);
      if (handles.remove(handle) == false)
         log.warn("Attempt to unregister handle that was not registered mcc=" + this + " handle=" + handle, new ApplicationServerInternalException("STACKTRACE"));
   }
   
   /**
    * Change the state
    * 
    * @param newState the new state
    * @return the previous value
    */
   private int changeState(int newState)
   {
      return state.getAndSet(newState);
   }
   
   /**
    * Check whether we are already destroyed
    * 
    * @return true if already destroyed/destroying
    */
   private boolean isDestroyed()
   {
      int state = changeState(DESTROYED);
      if (state == DESTROYED)
         changeState(DESTROYED);
      return state == DESTROYING || state == DESTROYED;
   }
}
