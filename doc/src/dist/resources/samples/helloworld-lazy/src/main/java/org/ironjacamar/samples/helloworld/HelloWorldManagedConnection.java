/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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
package org.ironjacamar.samples.helloworld;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

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

/**
 * HelloWorldManagedConnection
 */
public class HelloWorldManagedConnection implements ManagedConnection,
                                                    DissociatableManagedConnection
{
   /** The logger */
   private static Logger log = Logger.getLogger("HelloWorldManagedConnection");

   /** Connection manager */
   private ConnectionManager cm;

   /** MCF */
   private HelloWorldManagedConnectionFactory mcf;

   /** Log writer */
   private PrintWriter logWriter;

   /** Listeners */
   private List<ConnectionEventListener> listeners;

   /** Connections */
   private Set<HelloWorldConnectionImpl> connections;

   /**
    * Constructor
    * @param cm The connection manager
    * @param mcf The managed connection factory
    */
   public HelloWorldManagedConnection(ConnectionManager cm,
                                      HelloWorldManagedConnectionFactory mcf)
   {
      this.cm = cm;
      this.mcf = mcf;
      this.logWriter = null;
      this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
      this.connections = new HashSet<HelloWorldConnectionImpl>();
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
   public Object getConnection(Subject subject,
                               ConnectionRequestInfo cxRequestInfo) 
      throws ResourceException
   {
      HelloWorldConnectionImpl connection = new HelloWorldConnectionImpl(this, mcf, cm, cxRequestInfo);

      connections.add(connection);

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
      if (connection == null)
         throw new ResourceException("Null connection handle");

      if (!(connection instanceof HelloWorldConnectionImpl))
         throw new ResourceException("Wrong connection handle");

      HelloWorldConnectionImpl handle = (HelloWorldConnectionImpl)connection;
      handle.setManagedConnection(this);
      connections.add(handle);
   }

   /**
    * This method is called by an application server (that is capable of lazy
    * connection association optimization) in order to dissociate a ManagedConnection
    * instance from all of its connection handles.
    * @exception ResourceException Thrown if an error occurs
    */
   public void dissociateConnections() throws ResourceException
   {
      // We don't call cleanup(), as cleanup() could be doing additional contracts
      // with the physical connection
      
      for (HelloWorldConnectionImpl connection : connections)
      {
         connection.setManagedConnection(null);
      }
      connections.clear();
   }

   /**
    * Application server calls this method to force any cleanup on 
    * the ManagedConnection instance.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void cleanup() throws ResourceException
   {
      for (HelloWorldConnectionImpl connection : connections)
      {
         connection.setManagedConnection(null);
      }
      connections.clear();
   }

   /**
    * Destroys the physical connection to the underlying resource manager.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void destroy() throws ResourceException
   {
   }

   /**
    * Adds a connection event listener to the ManagedConnection instance.
    *
    * @param listener A new ConnectionEventListener to be registered
    */
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("Listener is null");

      listeners.add(listener);
   }

   /**
    * Removes an already registered connection event listener 
    * from the ManagedConnection instance.
    *
    * @param listener Already registered connection event listener to be removed
    */
   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("Listener is null");

      listeners.remove(listener);
   }

   /**
    * Gets the log writer for this ManagedConnection instance.
    *
    * @return Character ourput stream associated with this 
    *         Managed-Connection instance
    * @throws ResourceException generic exception if operation fails
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      return logWriter;
   }

   /**
    * Sets the log writer for this ManagedConnection instance.
    *
    * @param out Character Output stream to be associated
    * @throws ResourceException generic exception if operation fails
    */
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      this.logWriter = out;
   }

   /**
    * Returns an <code>javax.resource.spi.LocalTransaction</code> instance.
    *
    * @return LocalTransaction instance
    * @throws ResourceException generic exception if operation fails
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      throw new NotSupportedException("LocalTransaction not supported");
   }

   /**
    * Returns an <code>javax.transaction.xa.XAresource</code> instance. 
    *
    * @return XAResource instance
    * @throws ResourceException generic exception if operation fails
    */
   public XAResource getXAResource() throws ResourceException
   {
      throw new NotSupportedException("GetXAResource not supported");
   }

   /**
    * Gets the metadata information for this connection's underlying 
    * EIS resource manager instance. 
    *
    * @return ManagedConnectionMetaData instance
    * @throws ResourceException generic exception if operation fails
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      return new HelloWorldManagedConnectionMetaData();
   }

   /**
    * Call helloWorld
    * @param name String name
    * @return String helloworld
    */
   String helloWorld(String name)
   {
      return "Hello World, " + name + " !";
   }

   /**
    * Close handle
    * @param handle The handle
    */
   void closeHandle(HelloWorldConnection handle)
   {
      connections.remove((HelloWorldConnectionImpl)handle);

      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      event.setConnectionHandle(handle);

      for (ConnectionEventListener cel : listeners)
      {
         cel.connectionClosed(event);
      }
   }
}
