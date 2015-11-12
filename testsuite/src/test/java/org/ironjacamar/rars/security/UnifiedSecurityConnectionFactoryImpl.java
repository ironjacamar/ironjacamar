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
package org.ironjacamar.rars.security;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

import org.jboss.logging.Logger;

/**
 * UnifiedSecurityConnectionFactoryImpl
 *
 * @version $Revision: $
 */
public class UnifiedSecurityConnectionFactoryImpl implements UnifiedSecurityConnectionFactory
{
   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 1L;

   /**
    * The logger
    */
   private static Logger log = Logger.getLogger(UnifiedSecurityConnectionFactoryImpl.class.getName());

   /**
    * Reference
    */
   private Reference reference;

   /**
    * ManagedConnectionFactory
    */
   private UnifiedSecurityManagedConnectionFactory mcf;

   /**
    * ConnectionManager
    */
   private ConnectionManager connectionManager;

   /**
    * Default constructor
    */
   public UnifiedSecurityConnectionFactoryImpl()
   {

   }

   /**
    * Default constructor
    *
    * @param mcf       ManagedConnectionFactory
    * @param cxManager ConnectionManager
    */
   public UnifiedSecurityConnectionFactoryImpl(UnifiedSecurityManagedConnectionFactory mcf, ConnectionManager cxManager)
   {
      this.mcf = mcf;
      this.connectionManager = cxManager;
   }

   /**
    * Get connection from factory
    *
    * @return UnifiedSecurityConnection instance
    * @throws ResourceException Thrown if a connection can't be obtained
    */
   @Override
   public UnifiedSecurityConnection getConnection() throws ResourceException
   {
      log.trace("getConnection()");
      return (UnifiedSecurityConnection) connectionManager.allocateConnection(mcf, null);
   }

   /**
    * Get connection from factory
    *
    * @param userName The user name
    * @param password The password
    * @return ReauthConnection instance
    * @throws ResourceException Thrown if a connection can't be obtained
    */
   @Override
   public UnifiedSecurityConnection getConnection(String userName, String password) throws ResourceException
   {
      log.tracef("getConnection()");

      UnifiedSecurityCri cri = new UnifiedSecurityCri(userName, password);
      return (UnifiedSecurityConnection) connectionManager.allocateConnection(mcf, cri);
   }

   /**
    * Get the Reference instance.
    *
    * @return Reference instance
    * @throws NamingException Thrown if a reference can't be obtained
    */
   @Override
   public Reference getReference() throws NamingException
   {
      log.trace("getReference()");
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
      log.tracef("setReference(%s)", reference);
      this.reference = reference;
   }

}
