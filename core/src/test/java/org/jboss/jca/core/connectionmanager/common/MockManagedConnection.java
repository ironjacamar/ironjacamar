/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.common;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 * Mocked managed connection.
 *
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev$ $Date$
 *
 */
public class MockManagedConnection implements ManagedConnection
{
   private int cleanUpCalled;
   private Set<ConnectionEventListener> listeners;
   private MockHandle handle;

   /**
    * Creates a new instance.
    */
   public MockManagedConnection()
   {
      this.cleanUpCalled = 0;
      this.listeners = new HashSet<ConnectionEventListener>();
      this.handle = new MockHandle();
   }

   /**
    * {@inheritDoc}
    */
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      listeners.add(listener);
   }

   /**
    * {@inheritDoc}
    */

   public void associateConnection(Object connection) throws ResourceException
   {
   }

   /**
    * {@inheritDoc}
    */

   public void cleanup() throws ResourceException
   {
      cleanUpCalled++;
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
   public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      return handle;
   }

   /**
    * {@inheritDoc}
    */

   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      return new MockLocalTransaction();
   }

   /**
    * {@inheritDoc}
    */

   public PrintWriter getLogWriter() throws ResourceException
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

   public XAResource getXAResource() throws ResourceException
   {
      return new MockXAResource();
   }

   /**
    * {@inheritDoc}
    */

   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      listeners.remove(listener);
   }

   /**
    * {@inheritDoc}
    */

   public void setLogWriter(PrintWriter out) throws ResourceException
   {
   }

   /**
    * Get the cleanUpCalled.
    *
    * @return the cleanUpCalled.
    */
   public final int cleanUpCalled()
   {
      return cleanUpCalled;
   }
}
