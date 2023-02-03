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

import javax.naming.NamingException;
import javax.naming.Reference;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.cci.ConnectionSpec;
import jakarta.resource.cci.RecordFactory;
import jakarta.resource.cci.ResourceAdapterMetaData;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;

/**
 * Mock connection factory.
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a> 
 * @version $Rev$ $Date$
 *
 */
public class MockConnectionFactory implements ConnectionFactory
{
   //Serialize UID
   private static final long serialVersionUID = 3835383490257981043L;

   /**Connection manager*/
   private ConnectionManager connectionManager;
   
   /**Connection factory*/
   private ManagedConnectionFactory managedConnectionFactory;
   
   /**
    * Creates an instance.
    * @param connectionManager cm
    * @param managedConnectionFactory mcf
    */
   public MockConnectionFactory(ConnectionManager connectionManager, ManagedConnectionFactory managedConnectionFactory)
   {
      this.connectionManager = connectionManager;
      this.managedConnectionFactory = managedConnectionFactory;
   }
   
   /**
    * {@inheritDoc}
    */
   public Connection getConnection() throws ResourceException
   {      
      return null;
   }

   /**
    * {@inheritDoc}
    */
   
   public Connection getConnection(ConnectionSpec properties) throws ResourceException
   {
      MockConnectionRequestInfo ri = new MockConnectionRequestInfo();
      
      return (Connection)this.connectionManager.allocateConnection(this.managedConnectionFactory, ri);
   }

   /**
    * {@inheritDoc}
    */

   public ResourceAdapterMetaData getMetaData() throws ResourceException
   {
      
      return null;
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

   public void setReference(Reference reference)
   {
      
      
   }

   /**
    * {@inheritDoc}
    */

   public Reference getReference() throws NamingException
   {
      
      return null;
   }

}
