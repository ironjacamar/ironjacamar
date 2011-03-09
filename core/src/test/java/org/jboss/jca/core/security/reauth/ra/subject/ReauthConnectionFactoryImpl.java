/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.security.reauth.ra.subject;

import javax.naming.NamingException;
import javax.naming.Reference;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;

/**
 * ReauthConnectionFactoryImpl
 *
 * @version $Revision: $
 */
public class ReauthConnectionFactoryImpl implements ReauthConnectionFactory
{
   /** The serial version UID */
   private static final long serialVersionUID = 1L;

   /** The logger */
   private static Logger log = Logger.getLogger(ReauthConnectionFactoryImpl.class);

   /** The managed connection factory */
   private ManagedConnectionFactory mcf;

   /** The connection manager */
   private ConnectionManager connectionManager;

   /** Reference */
   private Reference reference;

   /**
    * Constructor
    * @param mcf The managed connection factory
    * @param cxManager The connection manager
    */
   public ReauthConnectionFactoryImpl(ManagedConnectionFactory mcf,
                                      ConnectionManager cxManager)
   {
      this.mcf = mcf;
      this.connectionManager = cxManager;
      this.reference = null;
   }

   /** 
    * Get connection from factory
    *
    * @return ReauthConnection instance
    * @exception ResourceException Thrown if a connection can't be obtained
    */
   @Override
   public ReauthConnection getConnection() throws ResourceException
   {
      log.tracef("getConnection()");

      return (ReauthConnection)connectionManager.allocateConnection(mcf, null);
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
      log.tracef("getReference()");

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
