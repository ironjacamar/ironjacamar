/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 * Mock managed connection factory.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @version $Rev$ $Date$
 *
 */
public class MockManagedConnectionFactory implements ManagedConnectionFactory
{
   //Serialize UID
   private static final long serialVersionUID = -5176317097693203493L;

   /**
    * {@inheritDoc}
    */
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {      
      return new MockConnectionFactory(cxManager, this);
   }

   /**
    * {@inheritDoc}
    */

   public Object createConnectionFactory() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */

   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) 
      throws ResourceException
   {
      return new MockManagedConnection();
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
   @SuppressWarnings("unchecked")
   public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, 
                                                    ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      for (Object c : connectionSet)
      {
         if (c instanceof MockManagedConnection)
            return (ManagedConnection)c;
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */

   public void setLogWriter(PrintWriter out) throws ResourceException
   {
   }
}
