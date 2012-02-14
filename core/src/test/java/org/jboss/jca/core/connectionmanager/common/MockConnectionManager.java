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
package org.jboss.jca.core.connectionmanager.common;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.connectionmanager.AbstractConnectionManager;
import org.jboss.jca.core.connectionmanager.ConnectionRecord;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

import java.util.Collection;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.SystemException;

import org.jboss.logging.Logger;

/**
 * Mock connection manager.
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev$ $Date$
 *
 */
public class MockConnectionManager extends AbstractConnectionManager
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, MockConnectionManager.class.getName());

   /** Serial version UID */
   private static final long serialVersionUID = 1L;

   /**
    * Creates a new mock connection manager.
    */
   public MockConnectionManager()
   {
   }

   /**
    * {@inheritDoc}
    */
   public CoreLogger getLogger()
   {
      return log;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener createConnectionListener(ManagedConnection managedConnection,
         Object context) throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public boolean dissociateManagedConnection(Object connection, ManagedConnection mc, ManagedConnectionFactory mcf)
      throws ResourceException
   {
      return false;
   }

   @Override
   public void transactionStarted(Collection<ConnectionRecord> conns) throws SystemException
   {
      //doing nothing
   }

   @Override
   public TransactionIntegration getTransactionIntegration()
   {
      return null;
   }

   @Override
   public boolean isTransactional()
   {
      return false;
   }
}
