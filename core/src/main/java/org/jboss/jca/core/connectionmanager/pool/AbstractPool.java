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

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.api.connectionmanager.pool.PoolStatistics;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolFactory;
import org.jboss.jca.core.connectionmanager.transaction.LockKey;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.RetryableException;
import javax.security.auth.Subject;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.logging.Messages;

/**
 * Abstract pool implementation.
 * <p>
 * It can contains sub-pools according to the semantic of
 * the pool. Concrete implementatins override {@link AbstractPool#getKey(Subject, ConnectionRequestInfo, boolean)}
 * method to create map key object.
 * </p>
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev$
 *
 */
public abstract class AbstractPool implements Pool
{
   /** The logger */
   protected final CoreLogger log;

   /** Is trace enabled */
   private boolean trace;
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** The managed connection pools, maps key --> pool */
   private final ConcurrentMap<Object, ManagedConnectionPool> mcpPools =
      new ConcurrentHashMap<Object, ManagedConnectionPool>();

   /** The managed connection factory for this pool */
   private final ManagedConnectionFactory mcf;

   /** The connection listener factory for this pool*/
   private ConnectionListenerFactory clf;

   /** The pool parameters */
   private final PoolConfiguration poolConfiguration;

   /** Whether to use separate pools for transactional and non-transaction use */
   private final boolean noTxSeparatePools;

   /** The poolName */
   private String poolName;

   /** Statistics */
   private PoolStatistics statistics;

   /**
    * Create a new base pool.
    *
    * @param mcf the managed connection factory
    * @param pc the pool configuration
    * @param noTxSeparatePools noTxSeparatePool
    */
   protected AbstractPool(final ManagedConnectionFactory mcf, final PoolConfiguration pc,
                          final boolean noTxSeparatePools)
   {
      if (mcf == null)
         throw new IllegalArgumentException("MCF is null");

      if (pc == null)
         throw new IllegalArgumentException("PoolConfiguration is null");

      this.mcf = mcf;
      this.poolConfiguration = pc;
      this.noTxSeparatePools = noTxSeparatePools;
      this.log = getLogger();
      this.trace = log.isTraceEnabled();
      this.statistics = new PoolStatisticsImpl(pc.getMaxSize(), mcpPools);
   }

   /**
    * Sets pool name.
    * @param poolName pool name
    */
   public void setName(String poolName)
   {
      this.poolName = poolName;
   }

   /**
    * Gets pool name.
    * @return pool name
    */
   public String getName()
   {
      return poolName;
   }

   /**
    * Retrieve the key for this request.
    *
    * @param subject the subject
    * @param cri the connection request information
    * @param separateNoTx separateNoTx
    * @return the key
    * @throws ResourceException for any error
    */
   protected abstract Object getKey(Subject subject, ConnectionRequestInfo cri,
         boolean separateNoTx) throws ResourceException;

   /**
    * Determine the correct pool for this request,
    * creates a new one when necessary.
    *
    * @param key the key to the pool
    * @param subject the subject of the pool
    * @param cri the connection request info
    * @return the subpool context
    * @throws ResourceException for any error
    */
   protected ManagedConnectionPool getManagedConnectionPool(Object key, Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      try
      {
         ManagedConnectionPool mcp = mcpPools.get(key);
         if (mcp == null)
         {
            ManagedConnectionPoolFactory mcpf = new ManagedConnectionPoolFactory();
            ManagedConnectionPool newMcp = mcpf.create(mcf, clf, subject, cri, poolConfiguration, this);

            mcp = mcpPools.putIfAbsent(key, newMcp);
            if (mcp == null)
            {
               mcp = newMcp;
               initLock();
            }
         }

         return mcp;
      }
      catch (Throwable t)
      {
         throw new ResourceException(bundle.unableGetManagedConnectionPool(), t);
      }
   }

   /**
    * Get any transaction integration associated with the pool.
    *
    * @return the transaction integration
    */
   protected TransactionIntegration getTransactionIntegration()
   {
      if (clf != null)
         return clf.getTransactionIntegration();

      return null;
   }

   /**
    * Get any transaction manager associated with the pool.
    *
    * @return the transaction manager
    */
   protected TransactionManager getTransactionManager()
   {
      if (getTransactionIntegration() != null)
         return getTransactionIntegration().getTransactionManager();

      return null;
   }

   /**
    * Get any transaction synchronization registry associated with the pool.
    * @return The value
    */
   protected TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
   {
      if (getTransactionIntegration() != null)
         return getTransactionIntegration().getTransactionSynchronizationRegistry();

      return null;
   }

   /**
    * Init lock
    * @return The lock
    */
   private synchronized Lock initLock()
   {
      TransactionSynchronizationRegistry tsr = getTransactionSynchronizationRegistry();
      if (tsr != null && tsr.getTransactionKey() != null)
      {
         if (tsr.getResource(LockKey.INSTANCE) == null)
         {
            Lock lock = new ReentrantLock(true);
            tsr.putResource(LockKey.INSTANCE, lock);
            return lock;
         }
         else
         {
            return (Lock)tsr.getResource(LockKey.INSTANCE);
         }
      }

      return null;
   }

   /**
    * Get lock
    * @return The lock
    */
   private Lock getLock()
   {
      Lock result = null;
      TransactionSynchronizationRegistry tsr = getTransactionSynchronizationRegistry();

      if (tsr != null && tsr.getTransactionKey() != null)
      {
         result = (Lock)tsr.getResource(LockKey.INSTANCE);
         if (result == null)
         {
            result = initLock();
         }
      }

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void emptyManagedConnectionPool(ManagedConnectionPool pool)
   {
      log.debug(poolName + ": emptyManagedConnectionPool(" + pool + ")");

      if (pool != null)
      {
         // We only consider removal if there are more than 1 managed connection pool
         if (mcpPools.size() > 1)
         {
            Iterator<ManagedConnectionPool> it = mcpPools.values().iterator();

            while (it.hasNext())
            {
               ManagedConnectionPool other = it.next();
               if (other == pool && pool.isEmpty())
               {
                  pool.shutdown();
                  it.remove();
                  break;
               }
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void flush()
   {
      flush(false);
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void flush(boolean kill)
   {
      log.debug(poolName + ": flush(" + kill + ")");

      Set<ManagedConnectionPool> clearMcpPools = new HashSet<ManagedConnectionPool>();

      Iterator<ManagedConnectionPool> it = mcpPools.values().iterator();
      while (it.hasNext())
      {
         ManagedConnectionPool mcp = it.next();
         mcp.flush(kill);

         if (mcp.isEmpty())
            clearMcpPools.add(mcp);
      }

      if (clearMcpPools.size() > 0)
      {
         for (ManagedConnectionPool mcp : clearMcpPools)
         {
            mcp.shutdown();
            mcpPools.values().remove(mcp);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener getConnection(Transaction trackByTransaction, Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      ConnectionListener cl = null;
      boolean separateNoTx = false;

      if (noTxSeparatePools)
      {
         separateNoTx = clf.isTransactional();
      }

      // Get specific managed connection pool key
      Object key = getKey(subject, cri, separateNoTx);

      // Get managed connection pool
      ManagedConnectionPool mcp = getManagedConnectionPool(key, subject, cri);

      // Are we doing track by transaction ?
      TransactionSynchronizationRegistry tsr = getTransactionSynchronizationRegistry();
      Object transactionKey = tsr != null ? tsr.getTransactionKey() : null;

      if (trackByTransaction == null || transactionKey == null)
      {
         return getSimpleConnection(subject, cri, mcp);
      }

      // Transaction old connections
      if (cl == null)
      {
         cl = getTransactionOldConnection(trackByTransaction, mcp);
      }

      // Creates a new connection with given transaction
      if (cl == null)
      {
         cl = getTransactionNewConnection(trackByTransaction, mcp, subject, cri);
      }

      return cl;
   }

   /**
    * Gets simple connection listener that wraps connection.
    * @param subject Subject instance
    * @param cri Connection request info
    * @param mcp The managed connection pool
    * @return connection listener
    * @throws ResourceException ResourceException
    */
   private ConnectionListener getSimpleConnection(final Subject subject, final ConnectionRequestInfo cri,
                                                  final ManagedConnectionPool mcp)
      throws ResourceException
   {
      ConnectionListener cl = null;

      try
      {
         // Get connection from the managed connection pool
         cl = mcp.getConnection(subject, cri);

         if (trace)
            log.tracef("Got connection from pool: %s", cl);

         return cl;
      }
      catch (ResourceException re)
      {
         if (re instanceof RetryableException)
         {
            if (log.isDebugEnabled())
               log.debug("Got a RetryableException - trying to reinitialize the pool");

            // Make sure that the managed connection pool is running
            if (!mcp.isRunning())
               mcp.reenable();

            //Getting connection from pool
            cl = mcp.getConnection(subject, cri);

            if (trace)
               log.tracef("Got connection from pool (retried): %s", cl);

            return cl;
         }
         else
         {
            throw re;
         }
      }
   }

   /**
    * Gets connection listener instance associated with transaction.
    * This method is package protected beacause it is intended only for test case use.
    * Please don't use it in your production code.
    * @param trackByTransaction transaction instance
    * @param mcp the managed connection pool associated with the desired connection listener
    * @return connection listener instance
    * @throws ResourceException Thrown if an error occurs
    */
   ConnectionListener getTransactionOldConnection(Transaction trackByTransaction, ManagedConnectionPool mcp)
      throws ResourceException
   {
      TransactionSynchronizationRegistry tsr = getTransactionSynchronizationRegistry();
      Lock lock = getLock();
      
      try
      {
         lock.lockInterruptibly();
      }
      catch (InterruptedException ie)
      {
         throw new ResourceException(bundle.unableObtainLock(), ie);
      }
      try
      {
         // Already got one
         ConnectionListener cl = (ConnectionListener)tsr.getResource(mcp);
         if (cl != null)
         {
            if (trace)
               log.tracef("Previous connection tracked by transaction=%s tx=%s", cl, trackByTransaction);
            return cl;
         }

         return null;
      }
      finally
      {
         lock.unlock();
      }
   }

   /**
    * Gets new connection listener if necessary instance with transaction.
    * This method is package protected beacause it is intended only for test case use.
    * Please don't use it in your production code.
    * @param trackByTransaction transaction instance
    * @param mcp pool instance
    * @param subject subject instance
    * @param cri connection request info
    * @return connection listener instance
    * @throws ResourceException ResourceException
    */
   ConnectionListener getTransactionNewConnection(Transaction trackByTransaction, ManagedConnectionPool mcp,
                                                  Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      // Need a new one for this transaction
      // This must be done outside the tx local lock, otherwise
      // the tx timeout won't work and get connection can do a lot of other work
      // with many opportunities for deadlocks.
      // Instead we do a double check after we got the transaction to see
      // whether another thread beat us to the punch.
      ConnectionListener cl = mcp.getConnection(subject, cri);
      if (trace)
         log.tracef("Got connection from pool tracked by transaction=%s tx=%s", cl, trackByTransaction);

      TransactionSynchronizationRegistry tsr = getTransactionSynchronizationRegistry();
      Lock lock = getLock();
      try
      {
         lock.lockInterruptibly();
      }
      catch (InterruptedException ie)
      {
         throw new ResourceException(bundle.unableObtainLock(), ie);
      }
      try
      {
         // Check we weren't racing with another transaction
         ConnectionListener other =
            (ConnectionListener)tsr.getResource(mcp);

         if (other != null)
         {
            mcp.returnConnection(cl, false);

            if (trace)
               log.tracef("Another thread already got a connection tracked by transaction=%s tx=%s",
                       other, trackByTransaction);

            cl = other;
         }

         // This is the connection for this transaction
         cl.setTrackByTx(true);
         tsr.putResource(mcp, cl);

         if (trace)
            log.tracef("Using connection from pool tracked by transaction=%s tx=%s", cl, trackByTransaction);

         return cl;
      }
      finally
      {
         lock.unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnectionFactory getManagedConnectionFactory()
   {
      return mcf;
   }

   /**
    * {@inheritDoc}
    */
   public void returnConnection(ConnectionListener cl, boolean kill) throws ResourceException
   {
      cl.setTrackByTx(false);
      //Get connection listener pool
      ManagedConnectionPool mcp = (ManagedConnectionPool) cl.getContext();

      //Return connection to the pool
      mcp.returnConnection(cl, kill);

      if (trace)
         log.tracef("Returning connection to pool %s", cl);
   }

   /**
    * Get the connection listener factory
    * @return The value
    */
   protected ConnectionListenerFactory getConnectionListenerFactory()
   {
      return clf;
   }

   /**
    * {@inheritDoc}
    */
   public void setConnectionListenerFactory(ConnectionListenerFactory clf)
   {
      this.clf = clf;
   }

   /**
    * {@inheritDoc}
    */
   public void shutdown()
   {
      log.debug(poolName + ": shutdown");

      Iterator<ManagedConnectionPool> it = mcpPools.values().iterator();
      while (it.hasNext())
      {
         ManagedConnectionPool mcp = it.next();
         mcp.shutdown();
      }

      mcpPools.clear();
   }

   /**
    * {@inheritDoc}
    */
   public PoolStatistics getStatistics()
   {
      return statistics;
   }

   /**
    * {@inheritDoc}
    */
   public abstract boolean testConnection();

   /**
    * Test if a connection can be obtained
    * @param subject Optional subject
    * @return True if possible; otherwise false
    */
   protected boolean internalTestConnection(Subject subject)
   {
      boolean result = false;
      ConnectionListener cl = null;
      try
      {
         if (((PoolStatisticsImpl)getStatistics()).getAvailableCount(true) > 0)
         {
            cl = getConnection(null, subject, null);
            result = true;
         }
      }
      catch (Throwable ignored)
      {
         // Ignore
      }
      finally
      {
         if (cl != null)
         {
            try
            {
               returnConnection(cl, false);
            }
            catch (ResourceException ire)
            {
               // Ignore
            }
         }
      }

      return result;
   }

   /**
    * Get the managed connection pools. 
    * This method is package protected beacause it is intended only for test case use.
    * Please don't use it in your production code.
    *
    * @return The managed connection pools
    */
   final ConcurrentMap<Object, ManagedConnectionPool> getManagedConnectionPools()
   {
      return mcpPools;
   }

   /**
    * Get the logger
    * @return The value
    */
   public abstract CoreLogger getLogger();
}
