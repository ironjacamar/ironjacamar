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
package org.ironjacamar.rars.test;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

/**
 * TestManagedConnection
 */
public class TestManagedConnection implements ManagedConnection
{
   /** The logwriter */
   private PrintWriter logwriter;

   /** ManagedConnectionFactory */
   private TestManagedConnectionFactory mcf;

   /** Listeners */
   private List<ConnectionEventListener> listeners;

   /** Connections */
   private Set<TestConnectionImpl> connections;

   /**
    * Default constructor
    * @param mcf mcf
    */
   public TestManagedConnection(TestManagedConnectionFactory mcf)
   {
      this.mcf = mcf;
      this.logwriter = null;
      this.listeners = Collections.synchronizedList(new ArrayList<ConnectionEventListener>(1));
      this.connections = new HashSet<TestConnectionImpl>();
   }

   /**
    * {@inheritDoc}
    */
   public Object getConnection(Subject subject,
                               ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      TestConnectionImpl connection = new TestConnectionImpl(this, mcf);
      connections.add(connection);
      return connection;
   }

   /**
    * {@inheritDoc}
    */
   public void associateConnection(Object connection) throws ResourceException
   {
      if (connection == null)
         throw new ResourceException("Null connection handle");

      if (!(connection instanceof TestConnectionImpl))
         throw new ResourceException("Wrong connection handle");

      TestConnectionImpl handle = (TestConnectionImpl)connection;
      handle.setManagedConnection(this);
      connections.add(handle);
   }

   /**
    * {@inheritDoc}
    */
   public void cleanup() throws ResourceException
   {
      for (TestConnectionImpl connection : connections)
      {
         connection.setManagedConnection(null);
      }
      connections.clear();
   }

   /**
    * {@inheritDoc}
    */
   public void destroy() throws ResourceException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("Listener is null");
      listeners.add(listener);
   }

   /**
    * {@inheritDoc}
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
   void closeHandle(TestConnection handle)
   {
      connections.remove((TestConnectionImpl)handle);
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      event.setConnectionHandle(handle);
      for (ConnectionEventListener cel : listeners)
      {
         cel.connectionClosed(event);
      }
   }

   /**
    * {@inheritDoc}
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      return logwriter;
   }

   /**
    * {@inheritDoc}
    */
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      logwriter = out;
   }

   /**
    * {@inheritDoc}
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      throw new NotSupportedException("getLocalTransaction() not supported");
   }

   /**
    * {@inheritDoc}
    */
   public XAResource getXAResource() throws ResourceException
   {
      throw new NotSupportedException("getXAResource() not supported");
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      return new TestManagedConnectionMetaData();
   }

   /**
    * Get failure count
    * @return The value
    */
   int getFailureCount()
   {
      return mcf.getFailureCount().intValue();
   }
}
