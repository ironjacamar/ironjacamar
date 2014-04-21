/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.tx.rars.perf;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

/**
 * PerfManagedConnection
 */
public class PerfManagedConnection implements ManagedConnection, LocalTransaction, XAResource
{
   /** The logwriter */
   private PrintWriter logwriter;

   /** ManagedConnectionFactory */
   private PerfManagedConnectionFactory mcf;

   /** Transaction begin duration */
   private long txBeginDuration;

   /** Transaction commit duration */
   private long txCommitDuration;

   /** Listeners */
   private List<ConnectionEventListener> listeners;

   /** Connection */
   private PerfConnectionImpl connection;

   /** Close event */
   private ConnectionEvent closeEvent;

   /** Error event */
   private ConnectionEvent errorEvent;

   /**
    * Default constructor
    * @param mcf mcf
    * @param tb Transaction begin duration
    * @param tc Transaction commit duration
    */
   public PerfManagedConnection(PerfManagedConnectionFactory mcf, long tb, long tc)
   {
      this.mcf = mcf;
      this.txBeginDuration = tb;
      this.txCommitDuration = tc;
      this.logwriter = null;
      this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
      this.connection = new PerfConnectionImpl(this);

      this.closeEvent = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      this.closeEvent.setConnectionHandle(connection);

      this.errorEvent = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, new Exception());
      this.errorEvent.setConnectionHandle(connection);
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

      if (!(connection instanceof PerfConnectionImpl))
         throw new ResourceException("Wrong connection handle");

      this.connection = (PerfConnectionImpl)connection;
   }

   /**
    * Application server calls this method to force any cleanup on the ManagedConnection instance.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void cleanup() throws ResourceException
   {
      // We want to be able to use txState across checkouts
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
    * Removes an already registered connection event listener from the ManagedConnection instance.
    *
    * @param listener already registered connection event listener to be removed
    */
   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("Listener is null");

      listeners.remove(listener);
   }

   /**
    * Close handle
    */
   void closeHandle()
   {
      for (ConnectionEventListener cel : listeners)
      {
         cel.connectionClosed(closeEvent);
      }
   }

   /**
    * Error handle
    */
   void errorHandle()
   {
      for (ConnectionEventListener cel : listeners)
      {
         cel.connectionErrorOccurred(errorEvent);
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
    * Gets the metadata information for this connection's underlying EIS resource manager instance. 
    *
    * @return ManagedConnectionMetaData instance
    * @throws ResourceException generic exception if operation fails
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      return new PerfManagedConnectionMetaData();
   }

   // LocalTransaction

   /**
    * {@inheritDoc}
    */
   public void begin() throws ResourceException
   {
      if (txBeginDuration > 0)
      {
         try
         {
            Thread.sleep(txBeginDuration);
         }
         catch (Exception e)
         {
            // Ignore
         }
      }

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
      if (txCommitDuration > 0)
      {
         try
         {
            Thread.sleep(txCommitDuration);
         }
         catch (Exception e)
         {
            // Ignore
         }
      }

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
      if (txCommitDuration > 0)
      {
         try
         {
            Thread.sleep(txCommitDuration);
         }
         catch (Exception e)
         {
            // Ignore
         }
      }

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
      if (txBeginDuration > 0)
      {
         try
         {
            Thread.sleep(txBeginDuration);
         }
         catch (Exception e)
         {
            // Ignore
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      if (txCommitDuration > 0)
      {
         try
         {
            Thread.sleep(txCommitDuration);
         }
         catch (Exception e)
         {
            // Ignore
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void rollback(Xid xid) throws XAException
   {
      if (txCommitDuration > 0)
      {
         try
         {
            Thread.sleep(txCommitDuration);
         }
         catch (Exception e)
         {
            // Ignore
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void end(Xid xid, int flags) throws XAException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void forget(Xid xid) throws XAException
   {
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSameRM(XAResource xares) throws XAException
   {
      if (this == xares)
         return true;

      return false;
   }

   /**
    * {@inheritDoc}
    */
   public int prepare(Xid xid) throws XAException
   {
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
      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("PerfManagedConnection@").append(Integer.toHexString(System.identityHashCode(this)));

      return sb.toString();
   }
}
