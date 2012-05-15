/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.deployers.spec.rars.lazy;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.DissociatableManagedConnection;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * LazyManagedConnection
 *
 * @version $Revision: $
 */
public class LazyManagedConnection implements ManagedConnection, DissociatableManagedConnection
{
   /** The logger */
   private static Logger log = Logger.getLogger(LazyManagedConnection.class);

   /** The logwriter */
   private PrintWriter logwriter;

   /** Connection manager */
   private ConnectionManager cm;

   /** ManagedConnectionFactory */
   private LazyManagedConnectionFactory mcf;

   /** Listeners */
   private List<ConnectionEventListener> listeners;

   /** Connection */
   private LazyConnectionImpl connection;

   /** Local transaction support */
   private boolean localTransaction;

   /** XA transaction support */
   private boolean xaTransaction;

   /** Lazy local transaction */
   private LazyLocalTransaction lazyLocalTransaction;

   /** Lazy XAResource */
   private LazyXAResource lazyXAResource;

   /**
    * Default constructor
    * @param localTransaction Support local transaction
    * @param xaTransaction Support XA transaction
    * @param mcf The managed connection factory
    * @param cm The connection manager
    */
   public LazyManagedConnection(boolean localTransaction, boolean xaTransaction,
                                LazyManagedConnectionFactory mcf, ConnectionManager cm)
   {
      this.localTransaction = localTransaction;
      this.xaTransaction = xaTransaction;
      this.mcf = mcf;
      this.cm = cm;
      this.logwriter = null;
      this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
      this.connection = null;
      this.lazyLocalTransaction = null;
      this.lazyXAResource = null;

      if (localTransaction)
         this.lazyLocalTransaction = new LazyLocalTransaction();

      if (xaTransaction)
         this.lazyXAResource = new LazyXAResource();
   }

   /**
    * Creates a new connection handle for the underlying physical connection 
    * represented by the ManagedConnection instance. 
    *
    * @param subject Security context as JAAS subject
    * @param cxRequestInfo ConnectionRequestInfo instance
    * @return generic Object instance representing the connection handle. 
    * @throws ResourceException generic exception if operation fails
    */
   public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.trace("getConnection()");

      if (connection != null)
         connection.setManagedConnection(null);
         
      connection = new LazyConnectionImpl(this, mcf, cm, cxRequestInfo);
      return connection;
   }

   /**
    * Used by the container to change the association of an 
    * application-level connection handle with a ManagedConneciton instance.
    *
    * @param connection Application-level connection handle
    * @throws ResourceException generic exception if operation fails
    */
   public void associateConnection(Object connection) throws ResourceException
   {
      log.trace("associateConnection()");

      if (this.connection != null)
         this.connection.setManagedConnection(null);

      if (connection != null)
      {
         if (!(connection instanceof LazyConnectionImpl))
            throw new ResourceException("Connection isn't LazyConnectionImpl: " + connection.getClass().getName());

         this.connection = (LazyConnectionImpl)connection;
         this.connection.setManagedConnection(this);
      }
      else
      {
         this.connection = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public void dissociateConnections() throws ResourceException
   {
      log.trace("dissociateConnections()");

      if (connection != null)
      {
         connection.setManagedConnection(null);
         connection = null;
      }
   }

   /**
    * Application server calls this method to force any cleanup on the ManagedConnection instance.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void cleanup() throws ResourceException
   {
      log.trace("cleanup()");

      if (connection != null)
      {
         connection.setManagedConnection(null);
         connection = null;
      }
   }

   /**
    * Destroys the physical connection to the underlying resource manager.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void destroy() throws ResourceException
   {
      log.trace("destroy()");

      if (connection != null)
      {
         connection.setManagedConnection(null);
         connection = null;
      }
   }

   /**
    * Adds a connection event listener to the ManagedConnection instance.
    *
    * @param listener A new ConnectionEventListener to be registered
    */
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      log.trace("addConnectionEventListener()");
      if (listener == null)
         throw new IllegalArgumentException("Listener is null");
      listeners.add(listener);
   }

   /**
    * Removes an already registered connection event listener from the ManagedConnection instance.
    *
    * @param listener already registered connection event listener to be removed
    */
   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      log.trace("removeConnectionEventListener()");
      if (listener == null)
         throw new IllegalArgumentException("Listener is null");
      listeners.remove(listener);
   }

   /**
    * Close handle
    *
    * @param handle The handle
    */
   public void closeHandle(LazyConnection handle)
   {
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      event.setConnectionHandle(handle);
      for (ConnectionEventListener cel : listeners)
      {
         cel.connectionClosed(event);
      }
   }

   /**
    * Gets the log writer for this ManagedConnection instance.
    *
    * @return Character ourput stream associated with this Managed-Connection instance
    * @throws ResourceException generic exception if operation fails
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      log.trace("getLogWriter()");
      return logwriter;
   }

   /**
    * Sets the log writer for this ManagedConnection instance.
    *
    * @param out Character Output stream to be associated
    * @throws ResourceException  generic exception if operation fails
    */
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      log.trace("setLogWriter()");
      logwriter = out;
   }

   /**
    * Returns an <code>javax.resource.spi.LocalTransaction</code> instance.
    *
    * @return LocalTransaction instance
    * @throws ResourceException generic exception if operation fails
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      if (!localTransaction || xaTransaction)
      {
         throw new NotSupportedException("LocalTransaction not supported");
      }
      else
      {
         return lazyLocalTransaction;
      }
   }

   /**
    * Returns an <code>javax.transaction.xa.XAresource</code> instance. 
    *
    * @return XAResource instance
    * @throws ResourceException generic exception if operation fails
    */
   public XAResource getXAResource() throws ResourceException
   {
      if (!xaTransaction || localTransaction)
      {
         throw new NotSupportedException("GetXAResource not supported not supported");
      }
      else
      {
         return lazyXAResource;
      }
   }

   /**
    * Gets the metadata information for this connection's underlying EIS resource manager instance. 
    *
    * @return ManagedConnectionMetaData instance
    * @throws ResourceException generic exception if operation fails
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      log.trace("getMetaData()");
      return new LazyManagedConnectionMetaData();
   }
}
