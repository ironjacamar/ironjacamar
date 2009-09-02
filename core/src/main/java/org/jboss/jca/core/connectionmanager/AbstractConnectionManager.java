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

import org.jboss.jca.core.connectionmanager.listener.ConnectionCacheListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.transaction.JTATransactionChecker;

import java.util.Collection;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.tm.TransactionTimeoutConfiguration;


/**
 * AbstractConnectionManager.
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev$ $Date$
 *
 */
public  abstract class AbstractConnectionManager 
   implements 
   ConnectionCacheListener, 
   ConnectionListenerFactory, 
   TransactionTimeoutConfiguration, 
   JTATransactionChecker

{
   
   /**
    * {@inheritDoc}
    */
   public ConnectionListener createConnectionListener(ManagedConnection managedConnection, Object context) 
      throws ResourceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public TransactionManager getTransactionManagerInstance()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isTransactional()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public long getTimeLeftBeforeTransactionTimeout(boolean arg0) throws RollbackException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public int getTransactionTimeout() throws SystemException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * {@inheritDoc}
    */
   public void checkTransactionActive() throws RollbackException, SystemException
   {
      // TODO Auto-generated method stub
      
   }

   /**
    * {@inheritDoc}
    */
   public void disconnect(Collection<ConnectionRecord> conns, Set<String> unsharableResources) throws ResourceException
   {
      // TODO Auto-generated method stub
      
   }

   /**
    * {@inheritDoc}
    */
   public void reconnect(Collection<ConnectionRecord> conns, Set<String> unsharableResources) throws ResourceException
   {
      // TODO Auto-generated method stub
      
   }

   /**
    * {@inheritDoc}
    */
   public void transactionStarted(Collection<ConnectionRecord> conns) throws SystemException
   {
      // TODO Auto-generated method stub
      
   }

}
