/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.tx.rars.txlog;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;

import javax.security.auth.Subject;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;

/**
 * TxLogManagedConnection
 */
public class TxLogManagedConnection implements ManagedConnection, LocalTransaction, XAResource
{
   /** TX_LOCAL_BEGIN */
   public static final String TX_LOCAL_BEGIN = "0";

   /** TX_LOCAL_COMMIT */
   public static final String TX_LOCAL_COMMIT = "1";

   /** TX_LOCAL_ROLLBACK */
   public static final String TX_LOCAL_ROLLBACK = "2";

   /** TX_XA_START_TMNOFLAGS */
   public static final String TX_XA_START_TMNOFLAGS = "3";

   /** TX_XA_START_TMJOIN */
   public static final String TX_XA_START_TMJOIN = "4";

   /** TX_XA_START_TMRESUME */
   public static final String TX_XA_START_TMRESUME = "5";

   /** TX_XA_START_UNKNOWN */
   public static final String TX_XA_START_UNKNOWN = "6";

   /** TX_XA_PREPARE */
   public static final String TX_XA_PREPARE = "7";

   /** TX_XA_COMMIT */
   public static final String TX_XA_COMMIT = "8";

   /** TX_XA_ROLLBACK */
   public static final String TX_XA_ROLLBACK = "9";

   /** TX_XA_FORGET */
   public static final String TX_XA_FORGET = "A";

   /** TX_XA_END_TMSUCCESS */
   public static final String TX_XA_END_TMSUCCESS = "B";

   /** TX_XA_END_TMFAIL */
   public static final String TX_XA_END_TMFAIL = "C";

   /** TX_XA_END_TMSUSPEND */
   public static final String TX_XA_END_TMSUSPEND = "D";

   /** TX_XA_END_UNKNOWN */
   public static final String TX_XA_END_UNKNOWN = "E";

   /** The logger */
   private static Logger log = Logger.getLogger(TxLogManagedConnection.class.getName());

   /** Transaction states */
   private static Map<String, String> txStates = new HashMap<String, String>();
   
   /** The id */
   private String id;

   /** The logwriter */
   private PrintWriter logwriter;

   /** ManagedConnectionFactory */
   private TxLogManagedConnectionFactory mcf;

   /** Listeners */
   private List<ConnectionEventListener> listeners;

   /** Connection */
   private TxLogConnectionImpl connection;

   /** Transaction timeout */
   private int timeout;

   /** Is in pool */
   private boolean inPool;

   /**
    * Default constructor
    * @param mcf mcf
    */
   public TxLogManagedConnection(TxLogManagedConnectionFactory mcf)
   {
      this.mcf = mcf;
      this.id = Integer.toHexString(System.identityHashCode(this));
      this.logwriter = null;
      this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
      this.connection = null;
      this.inPool = true;
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
      if (connection == null)
         connection = new TxLogConnectionImpl(this, mcf);

      inPool = false;
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

      if (!(connection instanceof TxLogConnectionImpl))
         throw new ResourceException("Wrong connection handle");

      this.connection = (TxLogConnectionImpl)connection;
   }

   /**
    * Application server calls this method to force any cleanup on the ManagedConnection instance.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void cleanup() throws ResourceException
   {
      // We want to be able to use txState across checkouts
      inPool = true;
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
      if (listener == null)
         throw new IllegalArgumentException("Listener is null");

      log.tracef("removeConnectionEventListener(%s)", listener);

      listeners.remove(listener);
   }

   /**
    * Close handle
    *
    * @param handle The handle
    */
   void closeHandle(TxLogConnection handle)
   {
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      event.setConnectionHandle(handle);

      List<ConnectionEventListener> copy = new ArrayList<ConnectionEventListener>(listeners);
      for (ConnectionEventListener cel : copy)
      {
         cel.connectionClosed(event);
      }
   }

   /**
    * Error handle
    *
    * @param handle The handle
    * @param exception The exception
    */
   void errorHandle(TxLogConnection handle, Exception exception)
   {
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, exception);
      event.setConnectionHandle(handle);

      List<ConnectionEventListener> copy = new ArrayList<ConnectionEventListener>(listeners);
      for (ConnectionEventListener cel : copy)
      {
         cel.connectionErrorOccurred(event);
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
      log.trace("getLocalTransaction()");
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
      log.trace("getXAResource()");
      return this;
   }

   /**
    * Gets the metadata information for this connection's underlying EIS resource manager instance. 
    *
    * @return ManagedConnectionMetaData instance
    * @throws ResourceException generic exception if operation fails
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      return new TxLogManagedConnectionMetaData();
   }

   /**
    * Get id
    * @return The id
    */
   String getId()
   {
      return id;
   }

   /**
    * Get state
    * @return The state string
    */
   String getState()
   {
      return getState(id);
   }

   /**
    * Get state
    * @param identifier The identifier
    * @return The state string
    */
   String getState(String identifier)
   {
      String txState = txStates.get(identifier);
      if (txState == null)
         txState = "";

      return txState;
   }

   /**
    * Clear state
    */
   void clearState()
   {
      clearState(id);
   }

   /**
    * Clear state
    * @param identifier The identifier
    */
   void clearState(String identifier)
   {
      txStates.put(identifier, "");
   }

   /**
    * Is in pool
    * @return The value
    */
   boolean isInPool()
   {
      return inPool;
   }

   // LocalTransaction

   /**
    * {@inheritDoc}
    */
   public void begin() throws ResourceException
   {
      log.trace("begin()");

      addTxState(TX_LOCAL_BEGIN);

      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED);
      
      for (ConnectionEventListener cel : listeners)
      {
         cel.localTransactionStarted(ce);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void commit() throws ResourceException
   {
      log.trace("commit()");

      addTxState(TX_LOCAL_COMMIT);

      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
      
      for (ConnectionEventListener cel : listeners)
      {
         cel.localTransactionCommitted(ce);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void rollback() throws ResourceException
   {
      log.trace("rollback()");

      addTxState(TX_LOCAL_ROLLBACK);

      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
      
      for (ConnectionEventListener cel : listeners)
      {
         cel.localTransactionRolledback(ce);
      }
   }

   // XAResource

   /**
    * {@inheritDoc}
    */
   public void start(Xid xid, int flags) throws XAException
   {
      log.tracef("start(%s, %d)", xid, flags);

      if (flags == XAResource.TMNOFLAGS)
      {
         addTxState(TX_XA_START_TMNOFLAGS);
      }
      else if (flags == XAResource.TMJOIN)
      {
         addTxState(TX_XA_START_TMJOIN);
      }
      else if (flags == XAResource.TMRESUME)
      {
         addTxState(TX_XA_START_TMRESUME);
      }
      else
      {
         addTxState(TX_XA_START_UNKNOWN);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      log.tracef("commit(%s, %s)", xid, onePhase);

      addTxState(TX_XA_COMMIT);
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
      log.tracef("rollback(%s)", xid);

      addTxState(TX_XA_ROLLBACK);
   }

   /**
    * {@inheritDoc}
    */
   public void end(Xid xid, int flags) throws XAException
   {
      log.tracef("end(%s, %d)", xid, flags);

      if (flags == XAResource.TMSUCCESS)
      {
         addTxState(TX_XA_END_TMSUCCESS);
      }
      else if (flags == XAResource.TMFAIL)
      {
         addTxState(TX_XA_END_TMFAIL);
      }
      else if (flags == XAResource.TMSUSPEND)
      {
         addTxState(TX_XA_END_TMSUSPEND);
      }
      else
      {
         addTxState(TX_XA_END_UNKNOWN);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void forget(Xid xid) throws XAException
   {
      log.tracef("forget(%s)", xid);

      addTxState(TX_XA_FORGET);
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSameRM(XAResource xares) throws XAException
   {
      log.tracef("isSameRM(%s)", xares);

      if (this == xares)
         return true;

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public int prepare(Xid xid) throws XAException
   {
      log.tracef("prepare(%s)", xid);

      addTxState(TX_XA_PREPARE);

      return XAResource.XA_OK;
   }

   /**
    * {@inheritDoc}
    */
   public Xid[] recover(int flag) throws XAException
   {
      return new Xid[] {};
   }
 
   /**
    * {@inheritDoc}
    */
   public int getTransactionTimeout() throws XAException
   {
      return timeout;
   }

   /**
    * {@inheritDoc}
    */
   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      timeout = seconds;
      return true;
   }

   /**
    * Add a transaction state
    * @param state The state
    */
   private void addTxState(String state)
   {
      String txState = txStates.get(id);
      if (txState == null)
         txState = "";

      txState += state;

      txStates.put(id, txState);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("TxLogManagedConnection@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[txState=").append(txStates.get(id));
      sb.append("]");

      return sb.toString();
   }
}
