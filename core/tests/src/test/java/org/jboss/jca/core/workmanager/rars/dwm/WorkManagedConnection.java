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
package org.jboss.jca.core.workmanager.rars.dwm;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionMetaData;
import jakarta.resource.spi.work.ExecutionContext;
import jakarta.resource.spi.work.Work;
import jakarta.resource.spi.work.WorkException;
import jakarta.resource.spi.work.WorkListener;
import jakarta.resource.spi.work.WorkManager;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 * WorkManagedConnection
 */
public class WorkManagedConnection implements ManagedConnection
{
   /** The logwriter */
   private PrintWriter logwriter;

   /** ManagedConnectionFactory */
   private WorkManagedConnectionFactory mcf;

   /** Listeners */
   private List<ConnectionEventListener> listeners;

   /** Connection */
   private WorkConnectionImpl connection;

   /**
    * Default constructor
    * @param mcf mcf
    */
   public WorkManagedConnection(WorkManagedConnectionFactory mcf)
   {
      this.mcf = mcf;
      this.logwriter = null;
      this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
      this.connection = null;
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
      ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      connection = new WorkConnectionImpl(this, mcf);
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

      if (!(connection instanceof WorkConnectionImpl))
         throw new ResourceException("Wrong connection handle");

      this.connection = (WorkConnectionImpl)connection;
   }

   /**
    * Application server calls this method to force any cleanup on the ManagedConnection instance.
    *
    * @throws ResourceException generic exception if operation fails
    */
   public void cleanup() throws ResourceException
   {
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
    *
    * @param handle The handle
    */
   void closeHandle(WorkConnection handle)
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
      return new WorkManagedConnectionMetaData();
   }

   /**
    * Get the WorkManager instance
    * @return The value
    */
   WorkManager getWorkManager()
   {
      return ((WorkResourceAdapter)mcf.getResourceAdapter()).getWorkManager();
   }

   /**
    * Call doWork
    * @param work The work instance
    * @throws WorkException WorkException
    */
   void doWork(Work work) throws WorkException
   {
      ((WorkResourceAdapter)mcf.getResourceAdapter()).getWorkManager().doWork(work);
   }

   /**
    * Call doWork
    * @param work The instance
    * @param startTimeout The start timeout
    * @param execContext The execution context
    * @param workListener The work listener
    * @throws WorkException WorkException
    */
   void doWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener)
      throws WorkException
   {
      ((WorkResourceAdapter)mcf.getResourceAdapter()).getWorkManager().doWork(work, startTimeout,
                                                                              execContext, workListener);
   }

   /**
    * Call scheduleWork
    * @param work The work instance
    * @throws WorkException WorkException
    */
   void scheduleWork(Work work) throws WorkException
   {
      ((WorkResourceAdapter)mcf.getResourceAdapter()).getWorkManager().scheduleWork(work);
   }

   /**
    * Call scheduleWork
    * @param work The instance
    * @param startTimeout The start timeout
    * @param execContext The execution context
    * @param workListener The work listener
    * @throws WorkException WorkException
    */
   void scheduleWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener)
      throws WorkException
   {
      ((WorkResourceAdapter)mcf.getResourceAdapter()).getWorkManager().scheduleWork(work, startTimeout,
                                                                                    execContext, workListener);
   }

   /**
    * Call startWork
    * @param work The work instance
    * @return Start delay
    * @throws WorkException WorkException
    */
   long startWork(Work work) throws WorkException
   {
      return ((WorkResourceAdapter)mcf.getResourceAdapter()).getWorkManager().startWork(work);
   }

   /**
    * Call startWork
    * @param work The instance
    * @param startTimeout The start timeout
    * @param execContext The execution context
    * @param workListener The work listener
    * @return Start delay
    * @throws WorkException WorkException
    */
   long startWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener)
      throws WorkException
   {
      return ((WorkResourceAdapter)mcf.getResourceAdapter()).getWorkManager().startWork(work, startTimeout,
                                                                                        execContext, workListener);
   }
}
