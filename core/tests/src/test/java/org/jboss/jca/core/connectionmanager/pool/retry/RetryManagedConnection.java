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
package org.jboss.jca.core.connectionmanager.pool.retry;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * RetryManagedConnection
 */
public class RetryManagedConnection  implements ManagedConnection
{
   private Logger log = Logger.getLogger(getClass());
   private RetryManagedConnectionFactory mcf;
   private HashSet<RetryConnection> handles = new HashSet<RetryConnection>();
   private HashSet<ConnectionEventListener> listeners = new HashSet<ConnectionEventListener>();

   /**
    * Constructor
    * @param mcf The MCF
    */
   public RetryManagedConnection(RetryManagedConnectionFactory mcf)
   {
      this.mcf = mcf;
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
   public void cleanup() throws ResourceException
   {
      for (RetryConnection c : handles)
      {
         c.setMc(null);
      }

      handles.clear();
   }

   /**
    * {@inheritDoc}
    */
   public Object getConnection(Subject param1, ConnectionRequestInfo param2) throws ResourceException
   {
      RetryConnection c =  new RetryConnection(this);
      handles.add(c);

      return c;
   }

   /**
    * {@inheritDoc}
    */
   public void associateConnection(Object p) throws ResourceException
   {
      if (p instanceof RetryConnection)
      {
         RetryConnection c = (RetryConnection)p;
         c.setMc(this);
         handles.add(c);
      }
      else
      {
         throw new ResourceException("Wrong kind of Connection " + p);
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void addConnectionEventListener(ConnectionEventListener cel)
   {
      listeners.add(cel);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void removeConnectionEventListener(ConnectionEventListener cel)
   {
      listeners.remove(cel);
   }

   /**
    * {@inheritDoc}
    */
   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public XAResource getXAResource() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
   }

   /**
    * {@inheritDoc}
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }

   /**
    * Get listeners
    * @return The value
    */
   public List<ConnectionEventListener> getListeners()
   {
      List<ConnectionEventListener> result = null;

      synchronized (listeners)
      {
         result = new ArrayList<ConnectionEventListener>(listeners);
      }

      return result;
   }

   /**
    * Connection closed
    * @param handle The handle
    */
   void connectionClosed(RetryConnection handle)
   {
      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      ce.setConnectionHandle(handle);

      Collection<ConnectionEventListener> copy = new ArrayList<ConnectionEventListener>(listeners);
      for (ConnectionEventListener cel : copy)
      {
         try
         {
            cel.connectionClosed(ce);
         }
         catch (Throwable ignored)
         {
            log.warn("Ignored", ignored);
         }
      }
      synchronized (this)
      {
         handles.remove(handle);
      }
   }

   /**
    * Connection error
    * @param handle The handle
    * @param e The error
    */
   void connectionError(RetryConnection handle, Exception e)
   {
      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, e);
      ce.setConnectionHandle(handle);

      Collection<ConnectionEventListener> copy = new ArrayList<ConnectionEventListener>(listeners);
      for (ConnectionEventListener cel : copy)
      {
         try
         {
            cel.connectionErrorOccurred(ce);
         }
         catch (Throwable t)
         {
            // Ignore
         }
      }
   }
}
