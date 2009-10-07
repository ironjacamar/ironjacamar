/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.connectionmanager;

import org.jboss.jca.core.api.ConnectionManager;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

/**
 * The JBoss specific connection manager interface
 */
public class ConnectionManagerImpl implements ConnectionManager
{
   /**Serial version UID*/
   private static final long serialVersionUID = 2843159124540746692L;
 
   /**Real connection manager*/
   private transient RealConnectionManager realConnectionManager;
   
   /**
    * Creates a new instance of connection manager.
    */
   public ConnectionManagerImpl()
   {
      
   }
   
   /**
    * Allocate a connection
    * @param mcf The managed connection factory
    * @param cxRequestInfo The connection request info
    * @return The connection
    * @throws ResourceException Thrown if an error occurs
    */
   public Object allocateConnection(ManagedConnectionFactory mcf,
                                    ConnectionRequestInfo cxRequestInfo)
      throws ResourceException
   {
      return this.realConnectionManager.allocateConnection(mcf, cxRequestInfo);
   }

   /**
    * {@inheritDoc}
    */
   public void checkTransactionActive() throws RollbackException, SystemException
   {
      this.realConnectionManager.checkTransactionActive();
   }

   /**
    * {@inheritDoc}
    */
   public long getTimeLeftBeforeTransactionTimeout(boolean errorRollback) throws RollbackException
   {
      return this.realConnectionManager.getTimeLeftBeforeTransactionTimeout(errorRollback);
   }

   /**
    * {@inheritDoc}
    */
   public int getTransactionTimeout() throws SystemException
   {
      return this.realConnectionManager.getTransactionTimeout();
   }

   /**
    * {@inheritDoc}
    */
   public RealConnectionManager getRealConnectionManager()
   {
      return this.realConnectionManager;
   }

   /**
    * {@inheritDoc}
    */
   public void setRealConnectionManager(RealConnectionManager realConnectionManager)
   {
      this.realConnectionManager = realConnectionManager;
   }
}
