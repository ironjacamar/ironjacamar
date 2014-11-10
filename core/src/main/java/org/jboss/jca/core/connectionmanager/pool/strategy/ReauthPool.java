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

package org.jboss.jca.core.connectionmanager.pool.strategy;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.AbstractPool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.RetryableUnavailableException;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * Pool implementation that supports reauthentication
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ReauthPool extends AbstractPool
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, ReauthPool.class.getName());

   /** Max pool size */
   private int maxPoolSize;

   /**
    * Creates a new instance.
    * @param mcf managed connection factory
    * @param pc pool configuration
    * @param noTxSeparatePools notx seperate pool
    * @param sharable Are the connections sharable
    */
   public ReauthPool(final ManagedConnectionFactory mcf,
                     final PoolConfiguration pc,
                     final boolean noTxSeparatePools,
                     final boolean sharable)
   {
      super(mcf, pc, noTxSeparatePools, sharable);
      this.maxPoolSize = pc.getMaxSize();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected synchronized Object getKey(Subject subject, ConnectionRequestInfo cri, boolean separateNoTx)
      throws ResourceException
   {
      ReauthKey key = new ReauthKey(subject, cri, separateNoTx);
      int activeCount = getActiveCount();

      if (activeCount >= maxPoolSize)
      {
         ManagedConnectionPool ownMcp = getManagedConnectionPool(key, subject, cri);

         if (!isFull())
         {
            ManagedConnectionPool mcp = getTargetManagedConnectionPool(ownMcp);

            if (mcp == null)
               throw new RetryableUnavailableException();

            ConnectionListener cl = mcp.removeConnectionListener();

            if (cl == null)
               throw new RetryableUnavailableException();

            ownMcp.addConnectionListener(cl);

            if (mcp.isEmpty())
               super.emptyManagedConnectionPool(mcp);
         }
      }

      return key;
   }

   /**
    * There is no reason to empty the managed connection pool for reauth enabled
    * resource adapters, since all managed connections can change
    * its credentials
    * 
    * @param pool the managed connection pool
    */
   @Override
   public void emptyManagedConnectionPool(ManagedConnectionPool pool)
   {
      // No-operation
   }

   /**
    * {@inheritDoc}
    */
   public boolean testConnection()
   {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean testConnection(ConnectionRequestInfo cri, Subject subject)
   {
      return internalTestConnection(cri, subject);
   }

   /**
    * {@inheritDoc}
    */
   public CoreLogger getLogger()
   {
      return log;
   }

   /**
    * Get the active count - use pools directly as statistics could be disabled at pool level
    * @return The number of active connections
    */
   private int getActiveCount()
   {
      int result = 0;

      for (ManagedConnectionPool mcp : getManagedConnectionPools().values())
      {
         result += mcp.getActive();
      }
         
      return result;
   }

   /**
    * Find the oldest managed connection pool with idle connections
    * @param exclude The managed connection pool that should be excluded
    * @return The managed connection pool; <code>null</code> if none
    */
   private ManagedConnectionPool getTargetManagedConnectionPool(ManagedConnectionPool exclude)
   {
      ManagedConnectionPool mcp = null;
      long lastUsed = Long.MAX_VALUE;
      
      for (ManagedConnectionPool m : getManagedConnectionPools().values())
      {
         if (lastUsed > m.getLastUsed() && m.getActive() > 0)
         {
            if (exclude == null || m != exclude)
            {
               mcp = m;
               lastUsed = m.getLastUsed();
            }
         }
      }

      return mcp;
   }
}
