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

package org.ironjacamar.core.connectionmanager.pool;

import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.core.api.connectionmanager.pool.FlushMode;
import org.ironjacamar.core.api.connectionmanager.pool.PoolConfiguration;
import org.ironjacamar.core.api.connectionmanager.pool.PoolStatistics;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.TransactionalConnectionManager;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.capacity.DefaultCapacity;
import org.ironjacamar.core.connectionmanager.pool.capacity.TimedOutDecrementer;
import org.ironjacamar.core.spi.transaction.ConnectableResource;
import org.ironjacamar.core.spi.transaction.TxUtils;
import org.ironjacamar.core.spi.transaction.local.LocalXAResource;
import org.ironjacamar.core.tracer.Tracer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.security.auth.Subject;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROY;

/**
 * The base class for all pool implementations
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractPool implements Pool
{
   /** The logger */
   private static Logger log = Logger.getLogger(AbstractPool.class);

   /**
    * The connection manager
    */
   protected ConnectionManager cm;

   /**
    * The pool configuration
    */
   protected PoolConfiguration poolConfiguration;

   /**
    * The pools
    */
   protected ConcurrentHashMap<Credential, ManagedConnectionPool> pools;

   /**
    * The transaction map
    */
   protected ConcurrentHashMap<Object, Map<ManagedConnectionPool, ConnectionListener>> transactionMap;

   /**
    * The semaphore
    */
   protected Semaphore semaphore;

   private Credential prefillCredential;

   private FlushStrategy flushStrategy;

   /** The capacity */
   private Capacity capacity;

   /**
    * The statistics
    */
   protected PoolStatisticsImpl statistics;

   /**
    * The janitor
    */
   protected Janitor janitor;
   
   /**
    * Constructor
    *
    * @param cm The connection manager
    * @param pc The pool configuration
    */
   public AbstractPool(ConnectionManager cm, PoolConfiguration pc)
   {
      this.cm = cm;
      this.poolConfiguration = pc;
      this.pools = new ConcurrentHashMap<Credential, ManagedConnectionPool>();
      this.transactionMap = new ConcurrentHashMap<Object, Map<ManagedConnectionPool, ConnectionListener>>();
      this.statistics = new PoolStatisticsImpl(poolConfiguration.getMaxSize());
      this.semaphore = new Semaphore(poolConfiguration.getMaxSize(), statistics);
      this.flushStrategy = poolConfiguration.getFlushStrategy();
      this.capacity = null;
      this.janitor = null;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionManager getConnectionManager()
   {
      return cm;
   }

   /**
    * {@inheritDoc}
    */
   public PoolConfiguration getConfiguration()
   {
      return poolConfiguration;
   }

   /**
    * {@inheritDoc}
    */
   public Semaphore getPermits()
   {
      return semaphore;
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
   public PoolStatisticsImpl getInternalStatistics()
   {
      return statistics;
   }

   /**
    * {@inheritDoc}
    */
   public Janitor getJanitor()
   {
      return janitor;
   }

   /**
    * {@inheritDoc}
    */
   public void setJanitor(Janitor v)
   {
      janitor = v;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener getConnectionListener(Credential credential) throws ResourceException
   {
      log.tracef("getConnectionListener(%s)", credential);

      ConnectionListener cl = null;
      ManagedConnectionPool mcp = getManagedConnectionPool(credential);

      if (isShutdown())
         throw new ResourceException();
      
      if (cm.getTransactionSupport() == TransactionSupportLevel.LocalTransaction
            || cm.getTransactionSupport() == TransactionSupportLevel.XATransaction)
      {
         try
         {
            TransactionalConnectionManager txCM = (TransactionalConnectionManager) cm;
            Transaction tx = txCM.getTransactionIntegration().getTransactionManager().getTransaction();

            if (TxUtils.isUncommitted(tx))
            {
               Object id = txCM.getTransactionIntegration().getTransactionSynchronizationRegistry().getTransactionKey();

               Map<ManagedConnectionPool, ConnectionListener> currentMap = transactionMap.get(id);

               if (currentMap == null)
               {
                  Map<ManagedConnectionPool, ConnectionListener> map = new HashMap<>();

                  currentMap = transactionMap.putIfAbsent(id, map);
                  if (currentMap == null)
                  {
                     currentMap = map;
                  }
               }

               cl = currentMap.get(mcp);

               if (cl == null)
               {
                  if (TxUtils.isActive(tx))
                  {
                     cl = mcp.getConnectionListener();

                     currentMap.put(mcp, cl);

                     txCM.getTransactionIntegration().getTransactionSynchronizationRegistry().
                        registerInterposedSynchronization(new TransactionMapCleanup(id, transactionMap));
                  }
                  else
                  {
                     throw new ResourceException();
                  }
               }
            }
         }
         catch (ResourceException re)
         {
            throw re;
         }
         catch (Exception e)
         {
            throw new ResourceException(e);
         }
      }

      if (cl == null)
         cl = mcp.getConnectionListener();

      return cl;
   }

   /**
    * Get from existing pools or create mcp w/ specified credential
    * It's used during prefill operation
    *
    * @param credential credential used to match
    * @return
    */
   private ManagedConnectionPool getManagedConnectionPool(Credential credential)
   {
      ManagedConnectionPool mcp = pools.get(credential);

      if (mcp == null)
      {
         synchronized (this)
         {
            mcp = pools.get(credential);

            if (mcp == null)
            {
               ManagedConnectionPool newMcp = createManagedConnectionPool(credential);
               mcp = pools.putIfAbsent(credential, newMcp);
               if (mcp == null)
               {
                  mcp = newMcp;

                  if (Tracer.isEnabled())
                     Tracer.createManagedConnectionPool(poolConfiguration.getId(), mcp);
               }
               else
               {
                  newMcp.shutdown();
               }
            }
         }
      }
      return mcp;
   }

   /**
    * {@inheritDoc}
    */
   public void returnConnectionListener(ConnectionListener cl, boolean kill) throws ResourceException
   {
      log.tracef("returnConnectionListener(%s, %s)", cl, kill);

      ManagedConnectionPool mcp = pools.get(cl.getCredential());

      if (!kill)
         kill = cl.getState() == DESTROY;

      if (Tracer.isEnabled())
         Tracer.returnConnectionListener(poolConfiguration.getId(), cl.getManagedConnectionPool(),
                                         cl, kill, false,
                                         Tracer.isRecordCallstacks() ? new Throwable("CALLSTACK") : null);

      mcp.returnConnectionListener(cl, kill);
   }

   /**
    * {@inheritDoc}
    */
   public boolean isFull()
   {
      return semaphore.availablePermits() == 0;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void shutdown()
   {
      for (ManagedConnectionPool mcp : pools.values())
      {
         mcp.shutdown();

         if (Tracer.isEnabled())
            Tracer.destroyManagedConnectionPool(poolConfiguration.getId(), mcp);
      }

      pools.clear();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isShutdown()
   {
      return cm.isShutdown();
   }

   /**
    * Get a LocalXAResource instance
    *
    * @param mc The ManagedConnection
    * @return The instance
    * @throws ResourceException Thrown if an error occurs
    */
   protected LocalXAResource getLocalXAResource(ManagedConnection mc) throws ResourceException
   {
      TransactionalConnectionManager txCM = (TransactionalConnectionManager) cm;
      LocalXAResource xaResource = null;
      String eisProductName = null;
      String eisProductVersion = null;
      String jndiName = cm.getConnectionManagerConfiguration().getJndiName();

      try
      {
         if (mc.getMetaData() != null)
         {
            eisProductName = mc.getMetaData().getEISProductName();
            eisProductVersion = mc.getMetaData().getEISProductVersion();
         }
      }
      catch (ResourceException re)
      {
         // Ignore
      }

      if (eisProductName == null)
         eisProductName = jndiName;

      if (eisProductVersion == null)
         eisProductVersion = jndiName;

      if (cm.getConnectionManagerConfiguration().isConnectable())
      {
         if (mc instanceof org.ironjacamar.core.spi.transaction.ConnectableResource)
         {
            ConnectableResource cr = (ConnectableResource) mc;

            xaResource = txCM.getTransactionIntegration()
                  .createConnectableLocalXAResource(cm, eisProductName, eisProductVersion, jndiName, cr, statistics);
         }
         else if (txCM.getTransactionIntegration().isConnectableResource(mc))
         {
            xaResource = txCM.getTransactionIntegration()
                  .createConnectableLocalXAResource(cm, eisProductName, eisProductVersion, jndiName, mc, statistics);
         }
      }

      if (xaResource == null)
         xaResource = txCM.getTransactionIntegration()
               .createLocalXAResource(cm, eisProductName, eisProductVersion, jndiName, statistics);

      return xaResource;
   }

   /**
    * Get a XAResource instance
    *
    * @param mc The ManagedConnection
    * @return The instance
    * @throws ResourceException Thrown if an error occurs
    */
   protected XAResource getXAResource(ManagedConnection mc) throws ResourceException
   {
      TransactionalConnectionManager txCM = (TransactionalConnectionManager) cm;
      XAResource xaResource = null;

      if (cm.getConnectionManagerConfiguration().isWrapXAResource())
      {
         String eisProductName = null;
         String eisProductVersion = null;
         String jndiName = cm.getConnectionManagerConfiguration().getJndiName();
         boolean padXid = cm.getConnectionManagerConfiguration().isPadXid();
         Boolean isSameRMOverride = cm.getConnectionManagerConfiguration().isIsSameRMOverride();

         try
         {
            if (mc.getMetaData() != null)
            {
               eisProductName = mc.getMetaData().getEISProductName();
               eisProductVersion = mc.getMetaData().getEISProductVersion();
            }
         }
         catch (ResourceException re)
         {
            // Ignore
         }

         if (eisProductName == null)
            eisProductName = jndiName;

         if (eisProductVersion == null)
            eisProductVersion = jndiName;

         if (cm.getConnectionManagerConfiguration().isConnectable())
         {
            if (mc instanceof org.ironjacamar.core.spi.transaction.ConnectableResource)
            {
               ConnectableResource cr = (ConnectableResource) mc;

               xaResource = txCM.getTransactionIntegration()
                     .createConnectableXAResourceWrapper(mc.getXAResource(), padXid, isSameRMOverride, eisProductName,
                           eisProductVersion, jndiName, cr, statistics);
            }
            else if (txCM.getTransactionIntegration().isConnectableResource(mc))
            {
               xaResource = txCM.getTransactionIntegration()
                     .createConnectableXAResourceWrapper(mc.getXAResource(), padXid, isSameRMOverride, eisProductName,
                           eisProductVersion, jndiName, mc, statistics);
            }
         }

         if (xaResource == null)
         {
            XAResource xar = mc.getXAResource();

            if (!(xar instanceof org.ironjacamar.core.spi.transaction.xa.XAResourceWrapper))
            {
               boolean firstResource = txCM.getTransactionIntegration().isFirstResource(mc);

               xaResource = txCM.getTransactionIntegration()
                     .createXAResourceWrapper(xar, padXid, isSameRMOverride, eisProductName, eisProductVersion,
                           jndiName, firstResource, statistics);
            }
            else
            {
               xaResource = xar;
            }
         }
      }
      else
      {
         xaResource = mc.getXAResource();
      }

      return xaResource;
   }

   /**
    * Prefill the connection pool
    */
   @Override
   public void prefill()
   {
      if (isShutdown())
         return;

      if (poolConfiguration.isPrefill())
      {
         ManagedConnectionPool mcp = pools.get(getPrefillCredential());

         if (mcp == null)
         {
            // Trigger the initial-pool-size prefill by creating the ManagedConnectionPool
            getManagedConnectionPool(getPrefillCredential());
         }
         else
         {
            // Standard prefill request
            mcp.prefill();
         }
      }
   }

   /**
    * Get prefill credential
    *
    * @return credential used to prefill
    */
   @Override
   public Credential getPrefillCredential()
   {
      if (this.prefillCredential == null)
      {
         if (cm.getSubjectFactory() == null || cm.getConnectionManagerConfiguration().getSecurityDomain() == null)
         {
            prefillCredential = new Credential(null, null);
         }
         else
         {
            prefillCredential =
               new Credential(SecurityActions.createSubject(cm.getSubjectFactory(),
                                                            cm.getConnectionManagerConfiguration().getSecurityDomain(),
                                                            cm.getManagedConnectionFactory()),
                              null);
         }
      }
      return this.prefillCredential;
   }

   /**
    * {@inheritDoc}
    */
   public void emptyManagedConnectionPool(ManagedConnectionPool mcp)
   {
      if (pools.values().remove(mcp))
      {
         mcp.shutdown();

         if (Tracer.isEnabled())
            Tracer.destroyManagedConnectionPool(poolConfiguration.getId(), mcp);
      }
   }

   /**
    * Get the flush strategy
    * @return The value
    */
   @Override
   public FlushStrategy getFlushStrategy()
   {
      return flushStrategy;
   }


   /**
    * {@inheritDoc}
    */
   @Override
   public void flush()
   {
      flush(FlushMode.IDLE);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized void flush(FlushMode mode)
   {
      if (isShutdown())
         return;

      for (Credential credential : pools.keySet())
      {
         ManagedConnectionPool mcp = pools.get(credential);
         if (mcp != null)
         {
            mcp.flush(mode);

            if (mcp.isEmpty() && !poolConfiguration.isPrefill())
            {
               mcp.shutdown();
               pools.remove(credential);

               if (Tracer.isEnabled())
                  Tracer.destroyManagedConnectionPool(poolConfiguration.getId(), mcp);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean testConnection()
   {
      return internalTestConnection(getPrefillCredential());
   }

   /**
    * {@inheritDoc}
    */
   public boolean testConnection(ConnectionRequestInfo cri, Subject subject)
   {
      return internalTestConnection(new Credential(subject, cri));
   }

   /**
    * Test if a connection can be obtained
    * @param credential The credential
    * @return True if possible; otherwise false
    */
   protected boolean internalTestConnection(Credential credential)
   {
      boolean result = false;
      boolean kill = false;
      ConnectionListener cl = null;

      if (isShutdown())
         return false;

      if (isFull())
         return false;

      try
      {
         ManagedConnectionPool mcp = getManagedConnectionPool(credential);
         cl = mcp.getConnectionListener();
         result = true;
      }
      catch (Throwable t)
      {
         kill = true;
      }
      finally
      {
         if (cl != null)
         {
            try
            {
               returnConnectionListener(cl, kill);
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
    * {@inheritDoc}
    */
   public void enlist(ManagedConnection mc) throws ResourceException
   {
      if (cm.getTransactionSupport() == TransactionSupportLevel.NoTransaction)
         return;

      ConnectionListener cl = findConnectionListener(mc, null);
      if (cl != null)
      {
         if (cl.isEnlisted())
            throw new ResourceException();

         try
         {
            TransactionalConnectionManager txCM = (TransactionalConnectionManager) cm;
            Transaction tx = txCM.getTransactionIntegration().getTransactionManager().getTransaction();

            if (TxUtils.isUncommitted(tx))
            {
               Object id = txCM.getTransactionIntegration().getTransactionSynchronizationRegistry().getTransactionKey();

               Map<ManagedConnectionPool, ConnectionListener> currentMap = transactionMap.get(id);

               if (currentMap == null)
               {
                  Map<ManagedConnectionPool, ConnectionListener> map = new HashMap<>();

                  currentMap = transactionMap.putIfAbsent(id, map);
                  if (currentMap == null)
                  {
                     currentMap = map;
                  }
               }

               ConnectionListener existing = currentMap.get(cl.getManagedConnectionPool());

               if (existing == null)
               {
                  if (TxUtils.isActive(tx))
                  {
                     cl.enlist();
                     
                     currentMap.put(cl.getManagedConnectionPool(), cl);

                     txCM.getTransactionIntegration().getTransactionSynchronizationRegistry().
                        registerInterposedSynchronization(new TransactionMapCleanup(id, transactionMap));
                  }
                  else
                  {
                     throw new ResourceException();
                  }
               }
               else
               {
                  log.tracef("Already a connection listener in the pool tracked by transaction=%s (existing=%s)",
                             id, existing);

                  if (existing.equals(cl))
                  {
                     if (TxUtils.isActive(tx))
                     {
                        cl.enlist();
                     }
                     else
                     {
                        throw new ResourceException();
                     }
                  }
                  else
                  {
                     throw new ResourceException();
                  }
               }
            }
            else
            {
               throw new ResourceException();
            }
         }
         catch (ResourceException re)
         {
            throw re;
         }
         catch (Throwable t)
         {
            throw new ResourceException(t);
         }
      }
      else
      {
         throw new ResourceException();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void delist(ConnectionListener cl) throws ResourceException
   {
      if (cm.getTransactionSupport() == TransactionSupportLevel.NoTransaction)
         return;

      if (cl != null)
      {
         try
         {
            TransactionalConnectionManager txCM = (TransactionalConnectionManager) cm;
            Transaction tx = txCM.getTransactionIntegration().getTransactionManager().getTransaction();

            if (TxUtils.isUncommitted(tx))
            {
               try
               {
                  cl.delist();
               }
               finally
               {
                  Object id = txCM.getTransactionIntegration()
                     .getTransactionSynchronizationRegistry().getTransactionKey();

                  Map<ManagedConnectionPool, ConnectionListener> currentMap = transactionMap.get(id);

                  if (currentMap != null)
                  {
                     ConnectionListener registered = currentMap.remove(cl.getManagedConnectionPool());
                     transactionMap.put(id, currentMap);
                  }
               }
            }
         }
         catch (ResourceException re)
         {
            throw re;
         }
         catch (Exception e)
         {
            throw new ResourceException(e);
         }
      }
      else
      {
         throw new ResourceException();
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public ConnectionListener findConnectionListener(ManagedConnection mc, Object c)
   {
      for (ManagedConnectionPool mcp : pools.values())
      {
         ConnectionListener cl = mcp.findConnectionListener(mc, c);
         if (cl != null)
            return cl;
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener removeConnectionListener(Credential credential)
   {
      if (credential == null)
      {
         // Any free
         for (ManagedConnectionPool mcp : pools.values())
         {
            ConnectionListener cl = mcp.removeConnectionListener(true);
            if (cl != null)
               return cl;
         }
      }
      else
      {
         ManagedConnectionPool mcp = pools.get(credential);
         if (mcp != null)
            return mcp.removeConnectionListener(false);
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener getActiveConnectionListener(Credential credential)
   {
      if (cm.getTransactionSupport() == TransactionSupportLevel.NoTransaction)
         return null;

      try
      {
         TransactionalConnectionManager txCM = (TransactionalConnectionManager) cm;
         Transaction tx = txCM.getTransactionIntegration().getTransactionManager().getTransaction();

         if (TxUtils.isUncommitted(tx))
         {
            Object id = txCM.getTransactionIntegration().getTransactionSynchronizationRegistry().getTransactionKey();
            Map<ManagedConnectionPool, ConnectionListener> currentMap = transactionMap.get(id);
            ManagedConnectionPool key = pools.get(credential);

            return currentMap.get(key);
         }
      }
      catch (Exception e)
      {
         log.tracef(e, "getActiveConnectionListener(%s)", credential);
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   public Capacity getCapacity()
   {
      if (capacity == null)
         return DefaultCapacity.INSTANCE;

      return capacity;
   }

   /**
    * {@inheritDoc}
    */
   public void setCapacity(Capacity c)
   {
      capacity = c;
   }


   /**
    * {@inheritDoc}
    */
   public boolean isFIFO()
   {
      if (capacity == null || capacity.getDecrementer() == null ||
            TimedOutDecrementer.class.getName().equals(capacity.getDecrementer().getClass().getName()))
         return false;

      return true;
   }
}
