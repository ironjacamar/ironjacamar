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

import java.io.Serializable;

import javax.resource.spi.ConnectionManager;

/**
 * TestConnectionFactoryImpl.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class TestConnectionFactoryImpl implements TestConnectionFactory, Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -6670190353441291330L;

   /** The managed connection factory */
   protected TestManagedConnectionFactory mcf;
   
   /** The connection manager */
   protected ConnectionManager cm;
   
   public TestConnectionFactoryImpl()
   {
   }
   
   protected TestConnectionFactoryImpl(TestManagedConnectionFactory mcf, ConnectionManager cm)
   {
      this.mcf = mcf;
      this.cm = cm;
   }

   public TestConnection getConnection() throws Exception
   {
      return (TestConnection) cm.allocateConnection(mcf, null);
   }

   public TestConnection getConnection(String user, String password) throws Exception
   {
      TestConnectionRequestInfo cri = new TestConnectionRequestInfo(user, password);
      return (TestConnection) cm.allocateConnection(mcf, cri);
   }
}
