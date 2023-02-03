/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.connectionmanager.rar;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionMetaData;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 * SimpleManagedConnection
 *
 * @version $Revision: $
 */
public class SimpleManagedConnection1 implements ManagedConnection
{

   /** The logger */
   private static Logger log = Logger.getLogger("SimpleManagedConnection1");

   /** The logwriter */
   private PrintWriter logwriter;

   /** ManagedConnectionFactory */
   private SimpleManagedConnectionFactory1 mcf;

   /** Listeners */
   private List<ConnectionEventListener> listeners;

   /** Connection */
   private SimpleConnectionImpl1 connection;

   /** ConnectionRequestInfo */
   private ConnectionRequestInfo cri;
 
   /** destroyed */
   private boolean destroyed;

   /**
    * Default constructor
    * @param mcf mcf
    * @param cri cri
    */
   public SimpleManagedConnection1(SimpleManagedConnectionFactory1 mcf, ConnectionRequestInfo cri)
   {
      this.mcf = mcf;
      this.logwriter = null;
      this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
      this.connection = null;
      if (cri == null || cri instanceof SimpleConnectionRequestInfoImpl)
         this.cri = cri;
      else
         throw new RuntimeException("CRI of wrong type:" + cri);
      destroyed = false;
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
      log.info("getConnection()");
      if (cxRequestInfo == null ||
          (cxRequestInfo instanceof SimpleConnectionRequestInfoImpl && cxRequestInfo.equals(cri)))
         connection = new SimpleConnectionImpl1(this, mcf, cxRequestInfo);
      else
         throw new ResourceException("CRI is wrong:" + cxRequestInfo);
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
      log.info("associateConnection()");

      if (connection == null)
         throw new ResourceException("Null connection handle");

      if (!(connection instanceof SimpleConnectionImpl1))
         throw new ResourceException("Wrong connection handle");

      this.connection = (SimpleConnectionImpl1) connection;
   }

   /**
    * Application server calls this method to force any cleanup on the ManagedConnection instance.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void cleanup() throws ResourceException
   {
      log.finest("cleanup()");
   }

   /**
    * Destroys the physical connection to the underlying resource manager.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void destroy() throws ResourceException
   {
      log.finest("destroy()");
      destroyed = true;
   }

   /**
    * Adds a connection event listener to the ManagedConnection instance.
    *
    * @param listener A new ConnectionEventListener to be registered
    */
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      log.finest("addConnectionEventListener()");
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
      log.finest("removeConnectionEventListener()");
      if (listener == null)
         throw new IllegalArgumentException("Listener is null");
      listeners.remove(listener);
   }

   /**
    * Close handle
    *
    * @param handle The handle
    */
   void closeHandle(SimpleConnection handle)
   {
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      event.setConnectionHandle(handle);
      for (int i = 0; i < listeners.size(); i++)
      {
         ConnectionEventListener cel = listeners.get(i);
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
      log.finest("getLogWriter()");
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
      log.finest("setLogWriter()");
      logwriter = out;
   }

   /**
    * Returns an <code>jakarta.resource.spi.LocalTransaction</code> instance.
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
      throw new NotSupportedException("GetXAResource not supported not supported");
   }

   /**
    * Gets the metadata information for this connection's underlying EIS resource manager instance. 
    *
    * @return ManagedConnectionMetaData instance
    * @throws ResourceException generic exception if operation fails
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      log.finest("getMetaData()");
      return new SimpleManagedConnectionMetaData();
   }

   /**
    * Call me
    */
   void callMe()
   {
      log.finest("callMe()");
   }

   /**
    * Fail handle
    *
    * @param handle The handle
    */
   void failHandle(SimpleConnection handle)
   {
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED);
      event.setConnectionHandle(handle);
      for (int i = 0; i < listeners.size(); i++)
      {
         ConnectionEventListener cel = listeners.get(i);
         cel.connectionErrorOccurred(event);
      }
      log.info("/////FAILListeners on Exit:" + listeners);
   }

   /**
    * 
    * getter
    * 
    * @return cri cri
    */
   public ConnectionRequestInfo getCri()
   {
      return cri;
   }

   /**
    * 
    * getter
    * 
    * @return true if connection was destroyed
    */
   public boolean isDestroyed()
   {
      return destroyed;
   }

}
