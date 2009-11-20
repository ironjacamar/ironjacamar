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

import org.jboss.jca.common.api.JBossResourceException;
import org.jboss.jca.core.connectionmanager.exception.RetryableResourceException;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.ManagedConnectionPool;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.tm.TransactionLocal;


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
public  abstract class AbstractPool implements ManagedConnectionPool, PreFillPoolSupport
{
   /** The logger */
   protected final Logger log = Logger.getLogger(getClass());

   /** The subpools, maps key --> pool */
   private final ConcurrentMap<Object, SubPoolContext> subPools = new ConcurrentHashMap<Object, SubPoolContext>();

   /** The managed connection factory for this pool */
   private final ManagedConnectionFactory mcf;
   
   /** The connection listener factory for this pool*/
   private ConnectionListenerFactory clf;

   /** The pool parameters */
   private final PoolParams poolParams;

   /** Whether to use separate pools for transactional and non-transaction use */
   private boolean noTxSeparatePools;

   /** The poolName */
   private String poolName;

   /** Is trace enabled */
   private boolean trace = false;
   
   /**
    * Create a new base pool.
    * 
    * @param mcf the managed connection factory
    * @param poolParams the pooling parameters
    * @param noTxSeparatePools noTxSeparatePool
    */
   protected AbstractPool(final ManagedConnectionFactory mcf, final PoolParams poolParams,
                   final boolean noTxSeparatePools)
   {
      this.mcf = mcf;
      this.poolParams = poolParams;
      this.noTxSeparatePools = noTxSeparatePools;
      this.trace = log.isTraceEnabled();
   }

   /**
    * Sets pool name.
    * @param poolName pool name
    */
   public void setPoolName(String poolName)
   {
      this.poolName = poolName;
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
   protected SubPoolContext getSubPool(Object key, Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      SubPoolContext subPoolContext = subPools.get(key);
      if (subPoolContext == null)
      {
         SubPoolContext newSubPoolContext = new SubPoolContext(getTransactionManager(), mcf, clf, subject, 
                                                         cri, poolParams);
         subPoolContext = subPools.putIfAbsent(key, newSubPoolContext);
         if (subPoolContext == null)
         {
            subPoolContext = newSubPoolContext;
            subPoolContext.initialize();
         }
      }

      return subPoolContext;
   }

   /**
    * Get any transaction manager associated with the pool.
    * 
    * @return the transaction manager
    */
   protected TransactionManager getTransactionManager()
   {
      if (clf != null)
      {
         return clf.getTransactionManager();  
      }
      else
      {
         return null;  
      }
   }
   
   
   /**
    * {@inheritDoc}
    */
   public void emptySubPool(InternalManagedConnectionPool pool)
   {
      if (pool != null)
      {
         Iterator<SubPoolContext> itSubPoolContexts = subPools.values().iterator();
         SubPoolContext other = null;
         while (itSubPoolContexts.hasNext())
         {
            other = itSubPoolContexts.next();
            if (other.getSubPool() == pool && pool.isEmpty())
            {
               pool.shutdown();
               itSubPoolContexts.remove();
               break;
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void flush()
   {
      Iterator<SubPoolContext> itSubPoolContexts = subPools.values().iterator();
      SubPoolContext subPoolContext = null;
      while (itSubPoolContexts.hasNext())
      {
         subPoolContext = itSubPoolContexts.next();
         subPoolContext.getSubPool().flush();
      }

      subPools.clear();
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
      
      //Get specific sub-pool key
      Object key = getKey(subject, cri, separateNoTx);
      
      //Find sub-pool related with key
      SubPoolContext subPoolContext = getSubPool(key, subject, cri);
      
      //Sub-pool internal managed connection pool
      InternalManagedConnectionPool imcp = subPoolContext.getSubPool();

      // Are we doing track by transaction?
      TransactionLocal trackByTx = subPoolContext.getTrackByTx();
      
      if (trackByTransaction == null || trackByTx == null)
      {
         cl = getSimpleConnection(subject, cri, subPoolContext);
      } //end of if trackByTransaction   
      
      //Transaction old connections
      if (cl == null)
      {
         cl = getTransactionOldConnection(trackByTx, trackByTransaction);
      }
      
      if (cl == null)
      {
         //Creats a new connection with given transaction
         cl = getTransactionNewConnection(trackByTx, trackByTransaction, imcp, subject, cri);
      }
      
      return cl;
   }
   
   /**
    * Gets simple connection listener that wraps connection.
    * @param subject subject instance
    * @param cri connection request info
    * @param separateNoTx seperate pool for tx
    * @return connection listener
    * @throws ResourceException
    */
   private ConnectionListener getSimpleConnection(final Subject subject, final ConnectionRequestInfo cri, 
         final SubPoolContext subPoolContext)
      throws ResourceException
   {
      ConnectionListener cl = null;
      InternalManagedConnectionPool imcp = null;
      
      try
      {  
         //Find internal managed pool
         imcp = subPoolContext.getSubPool();
         
         //Get connection from imcp
         cl = imcp.getConnection(subject, cri);
         
         if (trace)
         {
            dump("Got connection from pool : " + cl);
         }

         return cl;

      }
      catch (RetryableResourceException e)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Got a RetryableResourceException - trying to reinitialize the pool");  
         }

         // The IMCP is down - retry
         imcp = subPoolContext.getSubPool();

         // Make sure that IMCP is running
         if (!imcp.isRunning())
         {
            imcp.initialize();  
         }
         
         //Getting connection from pool
         cl = imcp.getConnection(subject, cri);
         if (trace)
         {
            dump("Got connection from pool (retried) " + cl);  
         }

         return cl;            
      } //end of catch
      
   }
   
   /**
    * Gets connection listener instance associated with transaction.
    * @param trackByTx trnasaction local
    * @param trackByTransaction transaction instance
    * @return connection listener instance
    * @throws ResourceException
    */
   private ConnectionListener getTransactionOldConnection(TransactionLocal trackByTx, Transaction trackByTransaction)
      throws ResourceException
   {
      ConnectionListener cl = null;
      
      // Track by transaction
      try
      {
         trackByTx.lock(trackByTransaction);
      }
      catch (Throwable t)
      {
         JBossResourceException.rethrowAsResourceException("Unable to get connection from the pool for tx=" 
               + trackByTransaction, t);
      }
      try
      {
         // Already got one
         cl = (ConnectionListener) trackByTx.get(trackByTransaction);
         if (cl != null)
         {
            if (trace)
            {
               dump("Previous connection tracked by transaction " + cl + " tx=" + trackByTransaction);  
            }
            
            return cl;
         }
      }
      finally
      {
         trackByTx.unlock(trackByTransaction);
      }
      
      return cl;
   }

   /**
    * Gets new connection listener if necessary instance with transaction.
    * @param trackByTx trnasaction local
    * @param trackByTransaction transaction instance
    * @param mcp pool instance
    * @param subject subject instance
    * @param cri connection request info
    * @return connection listener instance
    * @throws ResourceException
    */
   private ConnectionListener getTransactionNewConnection(TransactionLocal trackByTx, Transaction trackByTransaction, 
         InternalManagedConnectionPool mcp, Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      ConnectionListener cl = null;
      
      // Need a new one for this transaction
      // This must be done outside the tx local lock, otherwise
      // the tx timeout won't work and get connection can do a lot of other work
      // with many opportunities for deadlocks.
      // Instead we do a double check after we got the transaction to see
      // whether another thread beat us to the punch.
      cl = mcp.getConnection(subject, cri);
      if (trace)
      {
         dump("Got connection from pool tracked by transaction " + cl + " tx=" + trackByTransaction);  
      }
      
      // Relock and check/set status
      try
      {
         trackByTx.lock(trackByTransaction);
      }
      catch (Throwable t)
      {
         mcp.returnConnection(cl, false);
         if (trace)
         {
            dump("Had to return connection tracked by transaction " + cl + " tx=" + 
                  trackByTransaction + " error=" + t.getMessage());  
         }
         
         JBossResourceException.rethrowAsResourceException("Unable to get connection from the pool for tx=" 
               + trackByTransaction, t);
      }
      try
      {
         // Check we weren't racing with another transaction
         ConnectionListener other = (ConnectionListener) trackByTx.get(trackByTransaction);
         if (other != null)
         {
            mcp.returnConnection(cl, false);
            if (trace)
            {
               dump("Another thread already got a connection tracked by transaction " + 
                     other + " tx=" + trackByTransaction);  
            }
            
            cl = other;
         }
         
         // This is the connection for this transaction
         cl.setTrackByTx(true);
         trackByTx.set(cl);
         
         if (trace)
         {
            dump("Using connection from pool tracked by transaction " + cl + " tx=" + trackByTransaction);  
         }
         
      }
      finally
      {
         trackByTx.unlock(trackByTransaction);
      }
      
      return cl;
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
      InternalManagedConnectionPool mcp = (InternalManagedConnectionPool) cl.getContext();
      
      //Return connection to the pool
      mcp.returnConnection(cl, kill);
      
      if (trace)
      {
         dump("Returning connection to pool " + cl);
      }            
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
      Iterator<SubPoolContext> itSubPoolContexts = subPools.values().iterator();
      SubPoolContext subPoolContext = null;
      while (itSubPoolContexts.hasNext())
      {
         subPoolContext = itSubPoolContexts.next();
         subPoolContext.getSubPool().shutdown();
      }

      subPools.clear();
   }

   /**
    * {@inheritDoc}
    */   
   public void prefill()
   {      
      prefill(null, null, false);
   }

   /**
    * {@inheritDoc}
    */   
   public void prefill(boolean noTxSeperatePool)
   {
      prefill(null, null, noTxSeperatePool);      
   }

   /**
    * {@inheritDoc}
    */   
   public void prefill(Subject subject, ConnectionRequestInfo cri, boolean noTxnSeperatePool)
   {
      if (getPreFill())
      {
         log.debug("Attempting to prefill pool for pool with jndi name" + poolName);

         try
         {
            //Get sub-pool key
            Object key = getKey(subject, cri, noTxnSeperatePool);
            
            //Get sub-pool automatically initializes pool
            getSubPool(key, subject, cri);
         }
         catch (Throwable t)
         {
            //No real need to throw here being that pool remains in the same state as before.
            log.error("Unable to prefill pool with jndi name" + getPoolName(), t);
         }

      }
            
   }

   /**
    * {@inheritDoc}
    */   
   public boolean shouldPreFill()
   {
      return getPreFill();
   }
   
   /**
    * Gets pool name.
    * @return pool name
    */
   public String getPoolName()
   {
      return poolName;
   }
   
   /**
    * Gets prefill flag.
    * @return prefill or not
    */
   public boolean getPreFill()
   {
      return poolParams.isPrefill();
   }
   
   /**
    * Dump the stats to the trace log
    * @param info some context
    */
   private void dump(String info)
   {
      StringBuffer toLog = new StringBuffer(100);
      toLog.append(info);
      /*
         .append(" [InUse/Available/Max]: [");
      toLog.append(getInUseConnectionCount()).append("/");
      toLog.append(getAvailableConnectionCount()).append("/");
      toLog.append(poolParams.getMaxSize());
      toLog.append("]");
      */
      log.trace(toLog);
   }
}
