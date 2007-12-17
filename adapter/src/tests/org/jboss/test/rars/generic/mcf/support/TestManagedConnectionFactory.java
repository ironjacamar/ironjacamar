/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.rars.generic.mcf.support;

import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

import org.jboss.rars.generic.cf.GenericConnectionFactory;
import org.jboss.rars.generic.mcf.GenericManagedConnection;
import org.jboss.rars.generic.mcf.direct.DirectGenericManagedConnectionFactory;

/**
 * TestManagedConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class TestManagedConnectionFactory extends DirectGenericManagedConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1155075489153266482L;
   
   private static final Class<?> CONNECTION_FACTORY_CLASS = TestConnectionFactory.class;

   protected void addConnectionFactoryInterfaces(GenericConnectionFactory impl, Set<Class<?>> interfaces)
   {
      interfaces.add(CONNECTION_FACTORY_CLASS);
   }

   protected Object createRealConnection(GenericManagedConnection mc, Subject subject, ConnectionRequestInfo cxRequestInfo, Object requestID) throws ResourceException
   {
      return new TestConnectionImpl();
   }

   protected void destroyRealConnection(GenericManagedConnection mc) throws ResourceException
   {
      TestConnectionImpl connection = (TestConnectionImpl) mc.getRealConnection();
      connection.close();
   }
}
