/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.jca.rar.support;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

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
import org.jboss.util.collection.CollectionsFactory;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

/**
 * TestManagedConnection.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.3 $
 */
public class TestManagedConnection implements ManagedConnection
{
   private static final Logger log = Logger.getLogger(TestManagedConnection.class);
   
   protected SynchronizedBoolean destroyed = new SynchronizedBoolean(false);
   
   protected Set handles = CollectionsFactory.createCopyOnWriteSet(); 
   
   protected Set listeners = CollectionsFactory.createCopyOnWriteSet();
   
   public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      TestConnectionImpl handle =  new TestConnectionImpl();
      associateConnection(handle);
      return handle;
   }

   public void associateConnection(Object connection) throws ResourceException
   {
      if (connection == null || connection instanceof TestConnectionImpl == false)
         throw new IllegalArgumentException("Not a TestConnectionImpl");
      TestConnectionImpl handle = (TestConnectionImpl) connection;
      handle.setManagedConnection(this);
      handles.add(handle);
   }

   protected void disassociateConnection(Object connection)
   {
      handles.remove(connection);
   }
   
   public void close(Object handle)
   {
      if (destroyed.get())
         return;
      disassociateConnection(handle);
      ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      event.setConnectionHandle(handle);
      broadcastEvent(event);
   }
   
   public void cleanup() throws ResourceException
   {
      for (Iterator i = handles.iterator(); i.hasNext();)
      {
         TestConnectionImpl handle = (TestConnectionImpl) i.next();
         handle.setManagedConnection(null);
      }
      handles.clear();
   }

   public void destroy() throws ResourceException
   {
      cleanup();
      destroyed.set(true);
      listeners.clear();
   }

   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      if (destroyed.get())
         throw new IllegalStateException("ManagedConnection is destroyed");
      listeners.add(listener);
   }

   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      listeners.remove(listener);
   }

   protected void broadcastEvent(ConnectionEvent event)
   {
      for (Iterator i = listeners.iterator(); i.hasNext();)
      {
         ConnectionEventListener listener = (ConnectionEventListener) i.next();
         try
         {
            switch (event.getId())
            {
               case ConnectionEvent.CONNECTION_CLOSED:
               {
                  listener.connectionClosed(event);
                  break;
               }
               case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
               {
                  listener.connectionErrorOccurred(event);
                  break;
               }
               case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
               {
                  listener.localTransactionStarted(event);
                  break;
               }
               case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
               {
                  listener.localTransactionCommitted(event);
                  break;
               }
               case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
               {
                  listener.localTransactionRolledback(event);
                  break;
               }
               default:
                  throw new IllegalStateException("Illegal event id : " + event.getId());
            }
         }
         catch (Throwable t)
         {
            log.warn("Ignored error from event listener " + listener + " event=" + event, t);
         }
      }
   }
   
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      return new TestManagedConnectionMetaData();
   }

   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI getLocalTransaction");
   }

   public XAResource getXAResource() throws ResourceException
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI getXAResource");
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI getLogWriter");
   }

   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI setLogWriter");
   }
}
