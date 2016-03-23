/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.rars.security;

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * UnifiedSecurityManagedConnection
 *
 * @version $Revision: $
 */
public class UnifiedSecurityManagedConnection implements ManagedConnection, LocalTransaction, XAResource
{

   /**
    * The logger
    */
   private static Logger log = Logger.getLogger(UnifiedSecurityManagedConnection.class.getName());

   /**
    * The logwriter
    */
   private PrintWriter logwriter;

   /**
    * ManagedConnectionFactory
    */
   private final UnifiedSecurityManagedConnectionFactory mcf;

   /**
    * Listeners
    */
   private final List<ConnectionEventListener> listeners;

   /**
    * Connection
    */
   private final Set<UnifiedSecurityConnection> connectionSet;

   private final Subject subject;

   private final ConnectionRequestInfo cri;

   private boolean invalid;

   /**
    * Default constructor
    *
    * @param mcf     mcf
    * @param subject subject
    * @param cri     cri
    */
   public UnifiedSecurityManagedConnection(UnifiedSecurityManagedConnectionFactory mcf, Subject subject,
         ConnectionRequestInfo cri)
   {
      this.mcf = mcf;
      this.logwriter = null;
      this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
      this.connectionSet = Collections.synchronizedSet(new HashSet<>());
      this.subject = subject;
      this.cri = cri;
   }

   /**
    * Creates a new connectionSet handle for the underlying physical connectionSet
    * represented by the ManagedConnection instance.
    *
    * @param subject       Security context as JAAS subject
    * @param cxRequestInfo ConnectionRequestInfo instance
    * @return generic Object instance representing the connectionSet handle.
    * @throws ResourceException generic exception if operation fails
    */
   public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.trace("getConnection()");
      UnifiedSecurityConnectionImpl connection = new UnifiedSecurityConnectionImpl(this, mcf);
      connectionSet.add(connection);
      return connection;
   }

   /**
    * Used by the container to change the association of an
    * application-level connectionSet handle with a ManagedConneciton instance.
    *
    * @param connection Application-level connectionSet handle
    * @throws ResourceException generic exception if operation fails
    */
   public void associateConnection(Object connection) throws ResourceException
   {
      log.tracef("associateConnection(%s)", connection);

      if (connection == null)
         throw new ResourceException("Null connection handle");

      if (!(connection instanceof UnifiedSecurityConnectionImpl))
         throw new ResourceException("Wrong connection handle");

      if (!this.connectionSet.add((UnifiedSecurityConnectionImpl) connection))
         throw new ResourceException("connection already associated");

      ((UnifiedSecurityConnectionImpl) connection).setManagedConnection(this);
   }

   /**
    * Application server calls this method to force any cleanup on the ManagedConnection instance.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void cleanup() throws ResourceException
   {
      log.trace("cleanup()");
      this.connectionSet.clear();

   }

   /**
    * Destroys the physical connectionSet to the underlying resource manager.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void destroy() throws ResourceException
   {
      log.trace("destroy()");

   }

   /**
    * Adds a connectionSet event listener to the ManagedConnection instance.
    *
    * @param listener A new ConnectionEventListener to be registered
    */
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      log.tracef("addConnectionEventListener(%s)", listener);
      if (listener == null)
         throw new IllegalArgumentException("Listener is null");
      listeners.add(listener);
   }

   /**
    * Removes an already registered connectionSet event listener from the ManagedConnection instance.
    *
    * @param listener already registered connectionSet event listener to be removed
    */
   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      log.tracef("removeConnectionEventListener(%s)", listener);
      if (listener == null)
         throw new IllegalArgumentException("Listener is null");
      listeners.remove(listener);
   }

   /**
    * Close handle
    *
    * @param handle The handle
    */
   void closeHandle(UnifiedSecurityConnection handle)
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
    * @return Character output stream associated with this Managed-Connection instance
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
    * @throws ResourceException generic exception if operation fails
    */
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      log.tracef("setLogWriter(%s)", out);
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
      return this;
   }

   /**
    * Returns an <code>javax.transaction.xa.XAresource</code> instance.
    *
    * @return XAResource instance
    * @throws ResourceException generic exception if operation fails
    */
   public XAResource getXAResource() throws ResourceException
   {
      return this;
   }

   /**
    * Gets the metadata information for this connectionSet's underlying EIS resource manager instance.
    *
    * @return ManagedConnectionMetaData instance
    * @throws ResourceException generic exception if operation fails
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      log.trace("getMetaData()");
      return new UnifiedSecurityManagedConnectionMetaData(this);
   }


   /**
    * get subject
    *
    * @return subject
    */
   Subject getSubject()
   {
      return subject;
   }

   /**
    * get Connection Request Info
    *
    * @return cri
    */
   ConnectionRequestInfo getCri()
   {
      return cri;
   }

   @Override
   public void begin() throws ResourceException
   {

   }

   @Override
   public void commit() throws ResourceException
   {

   }

   @Override
   public void rollback() throws ResourceException
   {

   }

   @Override
   public void commit(Xid xid, boolean b) throws XAException
   {

   }

   @Override
   public void end(Xid xid, int i) throws XAException
   {

   }

   @Override
   public void forget(Xid xid) throws XAException
   {

   }

   @Override
   public int getTransactionTimeout() throws XAException
   {
      return 0;
   }

   @Override
   public boolean isSameRM(XAResource xaResource) throws XAException
   {
      return this == xaResource;
   }

   @Override
   public int prepare(Xid xid) throws XAException
   {
      return 0;
   }

   @Override
   public Xid[] recover(int i) throws XAException
   {
      return new Xid[0];
   }

   @Override
   public void rollback(Xid xid) throws XAException
   {

   }

   @Override
   public boolean setTransactionTimeout(int i) throws XAException
   {
      return false;
   }

   @Override
   public void start(Xid xid, int i) throws XAException
   {

   }

   /**
    * get the user name
    * @return the user name
    */
   String getUserName()
   {
      if (cri != null && cri instanceof UnifiedSecurityCri)
      {
         return ((UnifiedSecurityCri) cri).getUserName();
      }
      if (subject != null)
      {
         PasswordCredential pc = getPasswordCredential(subject);
         return pc.getUserName();
      }

      return null;
   }

   /**
    * get the password
    * @return the password
    */
   String getPassword()
   {
      if (cri != null && cri instanceof UnifiedSecurityCri)
      {
         return ((UnifiedSecurityCri) cri).getPassword();
      }
      if (subject != null)
      {
         PasswordCredential pc = getPasswordCredential(subject);
         return pc.getPassword().toString();
      }

      return null;
   }

   private PasswordCredential getPasswordCredential(Subject s)
   {
      Set<PasswordCredential> credentials = this.getPasswordCredentials(s);
      assertNotNull(credentials);
      assertFalse(credentials.isEmpty());
      return credentials.iterator().next();
   }

   /**
    * Get the PasswordCredential from the Subject
    *
    * @param subject The subject
    * @return The instances
    */
   private Set<PasswordCredential> getPasswordCredentials(final Subject subject)
   {
      if (System.getSecurityManager() == null)
         return subject.getPrivateCredentials(PasswordCredential.class);

      return AccessController.doPrivileged(
            (PrivilegedAction<Set<PasswordCredential>>) () -> subject.getPrivateCredentials(PasswordCredential.class));
   }

   /**
    * Error handle
    *
    * @param handle The handle
    * @param exception The exception
    */
   void errorHandle(UnifiedSecurityConnection handle, Exception exception)
   {
      connectionSet.remove(handle);

      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, exception);
      event.setConnectionHandle(handle);

      List<ConnectionEventListener> copy = new ArrayList<ConnectionEventListener>(listeners);
      for (ConnectionEventListener cel : copy)
      {
         cel.connectionErrorOccurred(event);
      }
   }

   /**
    * get Listeners
    * @return listeners
    */
   public List<ConnectionEventListener> getListeners()
   {
      return listeners;
   }

   /**
    * check if connection is invalid
    * @return true if invalid
    */
   public boolean isInvalid()
   {
      return invalid;
   }

   /**
    * invalidate connection
    */
   void invalidate()
   {
      this.invalid = Boolean.TRUE;
   }
}
