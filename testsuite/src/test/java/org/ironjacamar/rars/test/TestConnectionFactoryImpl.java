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

import javax.naming.NamingException;
import javax.naming.Reference;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

/**
 * TestConnectionFactoryImpl
 */
public class TestConnectionFactoryImpl implements TestConnectionFactory
{
   /** The serial version UID */
   private static final long serialVersionUID = 1L;

   /** Reference */
   private Reference reference;

   /** ManagedConnectionFactory */
   private TestManagedConnectionFactory mcf;

   /** ConnectionManager */
   private ConnectionManager connectionManager;

   /**
    * Default constructor
    */
   public TestConnectionFactoryImpl()
   {

   }

   /**
    * Default constructor
    * @param mcf ManagedConnectionFactory
    * @param cxManager ConnectionManager
    */
   public TestConnectionFactoryImpl(TestManagedConnectionFactory mcf, ConnectionManager cxManager)
   {
      this.mcf = mcf;
      this.connectionManager = cxManager;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public TestConnection getConnection() throws ResourceException
   {
      return (TestConnection)connectionManager.allocateConnection(mcf, null);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Reference getReference() throws NamingException
   {
      return reference;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setReference(Reference reference)
   {
      this.reference = reference;
   }
}
