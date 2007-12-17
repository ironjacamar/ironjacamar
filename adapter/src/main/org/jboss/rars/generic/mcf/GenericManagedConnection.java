/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.rars.generic.mcf;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;
import org.jboss.rars.generic.wrapper.GenericConnection;
import org.jboss.rars.generic.wrapper.GenericHandle;

/**
 * GenericManagedConnection.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class GenericManagedConnection implements ManagedConnection
{
   /** The log */
   private static final Logger log = Logger.getLogger(GenericManagedConnection.class);

   /** The writer */
   private PrintWriter writer;

   /** The managed connection factory */
   private GenericManagedConnectionFactory mcf;
   
   /** The connection */
   private Object realConnection;
   
   /** The request id */
   private Object requestID;
   
   /** Whether the managed connection is destroyed */
   protected AtomicBoolean destroyed = new AtomicBoolean(false);

   /** The associated connection handles */
   private Set<GenericConnection> associated = new CopyOnWriteArraySet<GenericConnection>();
   
   /** The listeners */
   private List<ConnectionEventListener> listeners = new CopyOnWriteArrayList<ConnectionEventListener>(); 

   /** The connections by handle */
   private Map<GenericHandle, GenericConnection> connectionsByHandle = new ConcurrentHashMap<GenericHandle, GenericConnection>();
   
   /**
    * Create a new GenericManagedConnection.
    * 
    * @param mcf the managed connection factory
    * @param requestID the request id
    */
   protected GenericManagedConnection(GenericManagedConnectionFactory mcf, Object requestID)
   {
      this.mcf = mcf;
      this.requestID = requestID;
      log.debug(this + " CREATED");
   }

   /**
    * Get the managed connection factory
    * 
    * @return the managed connection factory
    */
   public GenericManagedConnectionFactory getManagedConnectionFactory()
   {
      return mcf;
   }

   /**
    * Get the real connection.
    * 
    * @return the connection.
    */
   public Object getRealConnection()
   {
      return realConnection;
   }
   
   protected void setRealConnection(Object connection)
   {
      if (realConnection != null)
         throw new IllegalStateException("Connection is already set");
      this.realConnection = connection;
   }
   
   public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      GenericConnection impl = mcf.createGenericConnection(this, subject, cxRequestInfo);
      Class<?>[] interfaces = mcf.getConnectionInterfaces(impl);
      GenericHandle handle = impl.createHandle(interfaces);
      connectionsByHandle.put(handle, impl);
      associateConnection(impl);
      return handle;
   }
   
   public void associateConnection(Object connection) throws ResourceException
   {
      GenericConnection wrapper = connectionsByHandle.get(connection);
      if (wrapper == null)
         throw new ResourceAdapterInternalException("Unknown connnection handle " + connection);
      associateConnection(wrapper);
   }

   /**
    * Associate a connection
    * 
    * @param wrapper the connection
    * @throws ResourceException for any error
    */
   protected void associateConnection(GenericConnection wrapper) throws ResourceException
   {
      wrapper.setManagedConnection(this);
      associated.add(wrapper);
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace(this + " ASSOCIATE " + wrapper);
   }

   /**
    * Disassociate a connection
    * 
    * @param wrapper the connection
    */
   protected void disassociateConnection(GenericConnection wrapper)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace(this + " DISASSOCIATE " + wrapper);
      associated.remove(wrapper);
      wrapper.setManagedConnection(null);
   }
   
   /**
    * Close a wrapper
    * 
    * @param wrapper the connection
    */
   public void close(GenericConnection wrapper)
   {
      if (destroyed.get())
         return;
      disassociateConnection(wrapper);
      Object handle = wrapper.getProxy(); 
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      event.setConnectionHandle(handle);
      broadcastEvent(event);
      connectionsByHandle.remove(handle);
   }

   public void cleanup() throws ResourceException
   {
      for (GenericConnection wrapper : associated)
         disassociateConnection(wrapper);
   }

   public void safeDestroy()
   {
      try
      {
         destroy();
      }
      catch (Throwable ignored)
      {
         log.trace("Ignored", ignored);
      }
   }
   
   public void destroy() throws ResourceException
   {
      log.debug(this + " DESTROY");
      cleanup();
      listeners.clear();
      if (destroyed.getAndSet(true))
      {
         log.debug(this + " already destroyed");
         return;
      }
      mcf.destroyRealConnection(this);
   }
   
   /**
    * Test whether this managed connection matches the request
    * 
    * @param requestID the request id
    * @return true when it matches
    */
   protected boolean matches(Object requestID)
   {
      if (this.requestID == null && requestID == null)
         return true;
      if (this.requestID == null && requestID != null)
         return false;
      return this.requestID.equals(requestID);
   }
   
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      listeners.add(listener);
   }

   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      listeners.remove(listener);
   }

   /**
    * Check whether an error is fatal
    * 
    * @param t the error
    */
   public void checkFatal(Throwable t)
   {
      if (mcf.isFatal(t))
         broadcastError(t);
   }
   
   /**
    * Broadcast an error
    * 
    * @param t the error
    */
   public void broadcastError(Throwable t)
   {
      Exception e;
      if (t instanceof Exception)
         e = (Exception) t;
      else
         e = new ResourceAdapterInternalException("Error ", t);
      
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, e);
      broadcastEvent(event);
      safeDestroy();
   }
   
   /**
    * Broadcast an event
    * 
    * @param event the event
    */
   protected void broadcastEvent(ConnectionEvent event)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         switch (event.getId())
         {
            case ConnectionEvent.CONNECTION_CLOSED:
            {
               log.trace(this + " CONNECTION_CLOSED " + event.getConnectionHandle());
               break;
            }
            case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
            {
               log.trace(this + " CONNECTION_ERROR", event.getException());
               break;
            }
            case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
            {
               log.trace(this + " LOCAL_TRANSACTION_STARTED");
               break;
            }
            case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
            {
               log.trace(this + " LOCAL_TRANSACTION_COMMITTED");
               break;
            }
            case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
            {
               log.trace(this + " LOCAL_TRANSACTION_ROLLEDBACK");
               break;
            }
         }
      }

      if (listeners.size() == 0)
         return;
      
      for (ConnectionEventListener listener : listeners)
      {
         try
         {
            switch (event.getId())
            {
               case ConnectionEvent.CONNECTION_CLOSED:
               {
                  listener.connectionClosed(event);
                  break;
               }
               case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
               {
                  listener.connectionErrorOccurred(event);
                  break;
               }
               case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
               {
                  listener.localTransactionStarted(event);
                  break;
               }
               case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
               {
                  listener.localTransactionCommitted(event);
                  break;
               }
               case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
               {
                  listener.localTransactionRolledback(event);
                  break;
               }
               default:
                  throw new IllegalStateException("Illegal event id : " + event.getId());
            }
         }
         catch (Throwable t)
         {
            log.warn("Ignored error from event listener " + listener + " event=" + event, t);
         }
      }
   }

   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      // @todo getLocalTransaction
      throw new org.jboss.util.NotImplementedException("getLocalTransaction");
   }

   public XAResource getXAResource() throws ResourceException
   {
      // @todo getXAResource
      throw new org.jboss.util.NotImplementedException("getXAResource");
   }

   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      // @todo getMetaData
      throw new org.jboss.util.NotImplementedException("getMetaData");
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      return writer;
   }

   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      this.writer = out;
   }
   
   protected void finalize() throws Throwable
   {
      if (destroyed.get() == false)
      {
         log.warn("Destroying undestroyed managed connection that is being garbage collection " + this);
         safeDestroy();
      }
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(getClass().getSimpleName()).append('@').append(System.identityHashCode(this));
      buffer.append('[').append(getRealConnection()).append(']');
      return buffer.toString();
   }
}
