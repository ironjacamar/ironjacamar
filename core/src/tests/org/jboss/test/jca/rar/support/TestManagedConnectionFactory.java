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
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

/**
 * TestManagedConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class TestManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 4517969739433575464L;

   /** The resource adapter */
   private TestResourceAdapter resourceAdapter;
   
   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      // TODO FIXME
      return new TestManagedConnection();
   }

   @SuppressWarnings("unchecked")
   public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      // TODO FIXME
      return (TestManagedConnection) connectionSet.iterator().next();
   }

   public Object createConnectionFactory() throws ResourceException
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI createConnectionFactory");
   }

   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      return new TestConnectionFactoryImpl(this, cxManager);
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI getLogWriter");
   }

   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      throw new org.jboss.util.NotImplementedException("FIXME NYI setLogWriter");
   }

   public ResourceAdapter getResourceAdapter()
   {
      return resourceAdapter;
   }

   public void setResourceAdapter(ResourceAdapter ra) throws ResourceException
   {
      resourceAdapter = (TestResourceAdapter) ra;
   }
}
