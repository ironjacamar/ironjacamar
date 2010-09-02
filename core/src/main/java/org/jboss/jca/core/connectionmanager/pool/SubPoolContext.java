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

package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolFactory;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.tm.TransactionLocal;

/**
 * Sub-pool context. 
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class SubPoolContext
{
   /** Underlying sub-pool */
   private ManagedConnectionPool subPool;

   /** The track by transaction*/
   private TransactionLocal trackByTx;

   /**
    * Create a new SubPoolContext.
    * 
    * @param tm the transaction manager
    * @param mcf the managed connection factory
    * @param clf the connection listener factory
    * @param subject the subject
    * @param cri the connection request info
    * @param pc the pool configuration
    * @param p the pool
    * @param log The logger for the managed connection pool
    * @throws ResourceException for any error
    */
   public SubPoolContext(TransactionManager tm, ManagedConnectionFactory mcf, ConnectionListenerFactory clf, 
                         Subject subject, ConnectionRequestInfo cri, PoolConfiguration pc, Pool p, Logger log)
      throws ResourceException
   {
      try
      {
         ManagedConnectionPoolFactory mcpf = new ManagedConnectionPoolFactory();

         subPool = mcpf.create(mcf, clf, subject, cri, pc, p, this, log);

         if (tm != null)
         {
            trackByTx = new TransactionLocal(tm);  
         }
      }
      catch (Throwable t)
      {
         throw new ResourceException("Exception while creating sub pool", t);
      }
   }

   /**
    * Get the sub pool
    * 
    * @return the sub pool
    */
   public ManagedConnectionPool getSubPool()
   {
      return subPool;
   }

   /**
    * Get the track by transaction
    * 
    * @return the transaction local
    */
   public TransactionLocal getTrackByTx()
   {
      return trackByTx;
   }
}
