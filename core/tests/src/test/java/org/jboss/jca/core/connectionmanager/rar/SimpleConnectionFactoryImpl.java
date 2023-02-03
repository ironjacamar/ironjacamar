/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.connectionmanager.rar;

import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;


/**
 * SimpleConnectionFactoryImpl
 *
 * @version $Revision: $
 */
public class SimpleConnectionFactoryImpl implements SimpleConnectionFactory
{
   /** The serial version UID */
   private static final long serialVersionUID = 1L;

   /** The logger */
   private static Logger log = Logger.getLogger("SimpleConnectionFactoryImpl");

   /** Reference */
   private Reference reference;

   /** ManagedConnectionFactory */
   private SimpleManagedConnectionFactory mcf;

   /** ConnectionManager */
   private ConnectionManager connectionManager;

   /**
    * 
    * get ConnectionManager
    * 
    * @return ConnectionManager
    */
   public ConnectionManager getConnectionManager()
   {
      return connectionManager;
   }

   /**
    * 
    * set ConnectionManager
    * 
    * @param connectionManager ConnectionManager
    */
   public void setConnectionManager(ConnectionManager connectionManager)
   {
      this.connectionManager = connectionManager;
   }


   /**
    * Default constructor
    */
   public SimpleConnectionFactoryImpl()
   {

   }

   
   /**
    * Default constructor
    * @param mcf ManagedConnectionFactory
    * @param cxManager ConnectionManager
    */
   public SimpleConnectionFactoryImpl(SimpleManagedConnectionFactory mcf, ConnectionManager cxManager)
   {
      this.mcf = mcf;
      this.connectionManager = cxManager;
   }

   /** 
    * Get connection from factory
    *
    * @return SimpleConnection instance
    * @exception ResourceException Thrown if a connection can't be obtained
    */
   @Override
   public SimpleConnection getConnection() throws ResourceException
   {
      log.finest("getConnection()");
      return (SimpleConnection)connectionManager.allocateConnection(mcf, null);
   }

   /**
    * Get the Reference instance.
    *
    * @return Reference instance
    * @exception NamingException Thrown if a reference can't be obtained
    */
   @Override
   public Reference getReference() throws NamingException
   {
      log.finest("getReference()");
      return reference;
   }

   /**
    * Set the Reference instance.
    *
    * @param reference A Reference instance
    */
   @Override
   public void setReference(Reference reference)
   {
      log.finest("setReference()");
      this.reference = reference;
   }
   
   @Override
   public ManagedConnectionFactory getMCF()
   {
      return mcf;
   }

   @Override
   public SimpleConnection getConnection(String userId) throws ResourceException
   {
      return getConnection();
   }

}
