/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
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
   private Socket socket;

   /** Input */
   private ObjectInputStream ois;

   /** Output */
   private ObjectOutputStream oos;

   /**
    * Constructor
    * @param mcf The managed connection factory
    * @exception ResourceException Thrown if an error occurs
    */
   public ReauthManagedConnection(ReauthManagedConnectionFactory mcf) throws ResourceException
   {
      try
      {
         this.mcf = mcf;
         this.listeners = new HashSet<ConnectionEventListener>(1);
         this.logwriter = null;

         ReauthResourceAdapter rra = (ReauthResourceAdapter)mcf.getResourceAdapter();

         // Note, that this socket instance *should really* be guarded against concurrent access
         this.socket = new Socket(rra.getServer(), rra.getPort());
         this.ois = null;
         this.oos = null;

         // Connect
         getOutput().writeByte(0);
         getOutput().flush();

         Boolean granted = (Boolean)getInput().readObject();
         
         if (!granted.booleanValue())
            throw new ResourceException("Connection not granted");
      }
      catch (Throwable t)
      {
         throw new ResourceException("Unable to establish a connection", t);
      }
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
         connection = new ReauthConnectionImpl(socket, cri);
      }
      else
      {
         if (connection.getCri().getUserName().equals(cri.getUserName()))
            auth = false;
      }

      if (auth)
      {
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

      // TODO - connection listeners

      try
      {
         // Unauth the interaction
         getOutput().writeByte(4);
         getOutput().flush();

         socket.close();
      }
      catch (Throwable t)
      {
         throw new ResourceException("Error during cleanup", t);
      }
   }

   /**
    * Destroys the physical connection to the underlying resource manager.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void destroy() throws ResourceException
   {
      log.tracef("destroy");

      try
      {
         // Close the interaction
         getOutput().writeByte(1);
         getOutput().flush();

         socket.close();
      }
      catch (Throwable t)
      {
         throw new ResourceException("Error during destroy", t);
      }
   }

   /**
    * Adds a connection event listener to the ManagedConnection instance.
    *
    * @param listener A new ConnectionEventListener to be registered
    */
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      log.tracef("addConnectionEventListener");
      listeners.add(listener);
   }

   /**
    * Removes an already registered connection event listener from the ManagedConnection instance.
    *
    * @param listener already registered connection event listener to be removed
    */
   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      log.tracef("removeConnectionEventListener");
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
    * Get input stream
    * @return The value
    * @exception IOException Thrown in case of an error
    */
   private ObjectInputStream getInput() throws IOException
   {
      if (ois == null)
         ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream(), 8192));
      
      return ois;
   }

   /**
    * Get output stream
    * @return The value
    * @exception IOException Thrown in case of an error
    */
   private ObjectOutputStream getOutput() throws IOException
   {
      if (oos == null)
         oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream(), 8192));
      
      return oos;
   }
}
