/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.security.reauth.ra.cri;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * ReauthManagedConnection
 *
 * @version $Revision: $
 */
public class ReauthManagedConnection implements ManagedConnection
{
   /** The logger */
   private static Logger log = Logger.getLogger(ReauthManagedConnection.class);

   /** The managed connection factory */
   private ReauthManagedConnectionFactory mcf;

   /** The connection listeners */
   private Set<ConnectionEventListener> listeners;

   /** The logwriter */
   private PrintWriter logwriter;

   /** Current connection */
   private ReauthConnectionImpl connection;

   /** The socket */
   private ReauthSocket socket;

   /**
    * Constructor
    * @param mcf The managed connection factory
    * @exception ResourceException Thrown if an error occurs
    */
   public ReauthManagedConnection(ReauthManagedConnectionFactory mcf) throws ResourceException
   {
      log.tracef("constructor(%s)", mcf);

      this.mcf = mcf;
      this.listeners = new HashSet<ConnectionEventListener>(1);
      this.logwriter = null;

      ReauthResourceAdapter rra = (ReauthResourceAdapter)mcf.getResourceAdapter();

      this.socket = new ReauthSocket(rra.getServer(), rra.getPort());
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
      log.tracef("getConnection(%s, %s)", subject, cxRequestInfo);

      if (cxRequestInfo == null)
         throw new ResourceException("ReauthCri is null");

      if (!(cxRequestInfo instanceof ReauthCri))
         throw new ResourceException("Not a ReauthCri instance: " + cxRequestInfo.getClass().getName());

      ReauthCri cri = (ReauthCri)cxRequestInfo;
      boolean auth = true;

      if (connection == null)
      {
         connection = new ReauthConnectionImpl(this, cri);
         log.debugf("Creating a new connection: %s", connection);
      }
      else
      {
         if (connection.getCri().getUserName().equals(cri.getUserName()))
         {
            log.debugf("Existing connection has same credentials: %s", cri.getUserName());
            auth = false;
         }
      }

      if (auth)
      {
         log.debugf("Auth connection: %s", cri.getUserName());
         connection.login(cri.getUserName(), cri.getPassword());
      }

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
      log.tracef("associateConnection(%s)", connection);

      if (!(connection instanceof ReauthConnectionImpl))
         throw new ResourceException("Unable to associate connection: " + connection.getClass().getName());

      this.connection = (ReauthConnectionImpl)connection;
   }

   /**
    * Application server calls this method to force any cleanup on the ManagedConnection instance.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void cleanup() throws ResourceException
   {
      log.tracef("cleanup");

      socket.cleanup();
   }

   /**
    * Destroys the physical connection to the underlying resource manager.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void destroy() throws ResourceException
   {
      log.tracef("destroy");

      socket.destroy();
      socket = null;
   }

   /**
    * Adds a connection event listener to the ManagedConnection instance.
    *
    * @param listener A new ConnectionEventListener to be registered
    */
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      log.tracef("addConnectionEventListener(%s)", listener);

      listeners.add(listener);
   }

   /**
    * Removes an already registered connection event listener from the ManagedConnection instance.
    *
    * @param listener already registered connection event listener to be removed
    */
   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      log.tracef("removeConnectionEventListener(%s)", listener);

      listeners.remove(listener);
   }

   /**
    * Gets the log writer for this ManagedConnection instance.
    *
    * @return Character ourput stream associated with this Managed-Connection instance
    * @throws ResourceException generic exception if operation fails
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      log.tracef("getLogWriter");
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
      log.tracef("setLogWriter(%s)", out);
      this.logwriter = out;
   }

   /**
    * Returns an <code>javax.resource.spi.LocalTransaction</code> instance.
    *
    * @return LocalTransaction instance
    * @throws ResourceException generic exception if operation fails
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      log.tracef("getLocalTransaction()");

      throw new NotSupportedException();
   }

   /**
    * Returns an <code>javax.transaction.xa.XAresource</code> instance. 
    *
    * @return XAResource instance
    * @throws ResourceException generic exception if operation fails
    */
   public XAResource getXAResource() throws ResourceException
   {
      log.tracef("getXAResource()");

      throw new NotSupportedException();
   }

   /**
    * Gets the metadata information for this connection's underlying EIS resource manager instance. 
    *
    * @return ManagedConnectionMetaData instance
    * @throws ResourceException generic exception if operation fails
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      log.tracef("getMetaData()");

      return new ReauthManagedConnectionMetaData(socket);
   }

   /**
    * Get the socket
    * @return The value
    */
   ReauthSocket getSocket()
   {
      return socket;
   }

   /**
    * Close handle
    * @param handle The handle
    */
   void closeHandle(ReauthConnection handle)
   {
      log.tracef("closeHandle(%s)", handle);      

      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      event.setConnectionHandle(handle);

      for (ConnectionEventListener cel : listeners)
      {
         cel.connectionClosed(event);
      }
   }
}
