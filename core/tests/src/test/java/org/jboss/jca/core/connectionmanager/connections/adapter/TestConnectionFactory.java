/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.connectionmanager.connections.adapter;

import javax.naming.NamingException;
import javax.naming.Reference;
import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.cci.ConnectionSpec;
import jakarta.resource.cci.RecordFactory;
import jakarta.resource.cci.ResourceAdapterMetaData;
import jakarta.resource.spi.ConnectionManager;

/**
 * TestConnectionFactory
 */
public class TestConnectionFactory implements ConnectionFactory, Referenceable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private ConnectionManager cm;

   private TestManagedConnectionFactory mcf;

   private Reference ref;
   
   /**
    * Constructor
    * @param cm The CM
    * @param mcf The MCF
    */
   public TestConnectionFactory(ConnectionManager cm, TestManagedConnectionFactory mcf)
   {
      this.cm = cm;
      this.mcf = mcf;
   }

   /**
    * Set failure
    * @param failure failure
    */
   public void setFailure(String failure)
   {
      mcf.setFailure(failure);
   }

   /**
    * {@inheritDoc}
    */
   public void setReference(Reference ref)
   {
      this.ref = ref;
   }

   /**
    * {@inheritDoc}
    */
   public Reference getReference() throws NamingException
   {
      return ref;
   }

   /**
    * {@inheritDoc}
    */
   public Connection getConnection() throws ResourceException
   {
      return (Connection)cm.allocateConnection(mcf, null);
   }

   /**
    * {@inheritDoc}
    */
   public Connection getConnection(ConnectionSpec ignore) throws ResourceException
   {
      return (Connection)cm.allocateConnection(mcf, null);
   }

   /**
    * Get connection
    * @param failure The failure
    * @return The connection
    * @exception ResourceException If error
    */
   public Connection getConnection(String failure) throws ResourceException
   {
      return (Connection)cm.allocateConnection(mcf, new TestConnectionRequestInfo(failure));
   }

   /**
    * {@inheritDoc}
    */
   public RecordFactory getRecordFactory() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public ResourceAdapterMetaData getMetaData() throws ResourceException
   {
      return null;
   }
}
