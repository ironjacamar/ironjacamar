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

package org.ironjacamar.core.connectionmanager.pool.dflt;

import org.ironjacamar.core.api.connectionmanager.pool.CapacityDecrementer;
import org.ironjacamar.core.api.connectionmanager.pool.FlushMode;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.AbstractManagedConnectionPool;
import org.ironjacamar.core.connectionmanager.pool.CapacityFiller;
import org.ironjacamar.core.connectionmanager.pool.CapacityRequest;
import org.ironjacamar.core.connectionmanager.pool.ConnectionValidator;
import org.ironjacamar.core.connectionmanager.pool.FillRequest;
import org.ironjacamar.core.connectionmanager.pool.IdleConnectionRemover;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPoolUtility;
import org.ironjacamar.core.connectionmanager.pool.PoolFiller;
import org.ironjacamar.core.connectionmanager.pool.capacity.DefaultCapacity;
import org.ironjacamar.core.connectionmanager.pool.capacity.TimedOutDecrementer;
import org.ironjacamar.core.connectionmanager.pool.capacity.TimedOutFIFODecrementer;
import org.ironjacamar.core.tracer.Tracer;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ValidatingManagedConnectionFactory;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROY;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROYED;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.FLUSH;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.FREE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.IN_USE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.TO_POOL;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.VALIDATION;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.ZOMBIE;

/**
 * The default ManagedConnectionPool
 */
public class DefaultManagedConnectionPool extends AbstractManagedConnectionPool
{
   /**
    * The associated pool
    */
   private DefaultPool pool;

   /**
    * The connection listeners
    */
   private ConcurrentLinkedDeque<ConnectionListener> listeners;

   /**
    * Constructor
    *
    * @param pool       The pool
    * @param credential The credential
    */
   public DefaultManagedConnectionPool(DefaultPool pool, Credential credential)
   {
      super(pool, credential);
      this.pool = pool;
      this.listeners = new ConcurrentLinkedDeque<ConnectionListener>();

      if (credential.equals(pool.getPrefillCredential()) &&
            pool.getConfiguration().isPrefill() &&
            pool.getConfiguration().getInitialSize() > 0)
      {
         PoolFiller.fillPool(new FillRequest(this, pool.getConfiguration().getInitialSize()));
      }

      if (pool.getConfiguration().isBackgroundValidation()
            && pool.getConfiguration().getBackgroundValidationMillis() > 0)
      {
         //Register validation
         ConnectionValidator.getInstance().registerPool(this, pool.getConfiguration().getBackgroundValidationMillis());
      }
      if (pool.getConfiguration().getIdleTimeoutMinutes() > 0)
      {
         //Register idle connection cleanup
         IdleConnectionRemover.getInstance().registerPool(this,
                                                          pool.getConfiguration().getIdleTimeoutMinutes() * 60 * 1000L);
      }
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener getConnectionListener() throws ResourceException
   {
      if (pool.getLogger().isTraceEnabled())
      {
         synchronized (this)
         {
            pool.getLogger().trace(ManagedConnectionPoolUtility.fullDetails(this,
               "getConnectionListener()",
               pool.getConnectionManager().getManagedConnectionFactory(),
               pool.getConnectionManager(),
               pool, pool.getConfiguration(),
               listeners, pool.getInternalStatistics(),
               credential.getSubject(),
               credential.getConnectionRequestInfo()));
         }
      }
      else if (pool.getLogger().isDebugEnabled())
      {
         pool.getLogger().debug(ManagedConnectionPoolUtility.details("getConnectionListener()",
                                                                     pool.getConfiguration().getId(),
                                                                     getCount(IN_USE, listeners),
                                                                     pool.getConfiguration().getMaxSize()));

      }
      
      long timestamp = System.currentTimeMillis();
      while (System.currentTimeMillis() - timestamp <= pool.getConfiguration().getBlockingTimeout())
      {
         Iterator<ConnectionListener> listenersIterator =
            poolIsFifo ? listeners.iterator() : listeners.descendingIterator();

         while (listenersIterator.hasNext())
         {
            ConnectionListener cl = listenersIterator.next();
            if (cl.changeState(FREE, VALIDATION))
            {
               if (pool.getInternalStatistics().isEnabled())
                  pool.getInternalStatistics().deltaTotalBlockingTime(System.currentTimeMillis() - timestamp);

               if (pool.getConfiguration().isValidateOnMatch())
               {
                  ConnectionListener result = validateConnectionListener(listeners, cl, IN_USE);
                  if (result != null)
                  {
                     if (Tracer.isEnabled())
                        Tracer.getConnectionListener(pool.getConfiguration().getId(),
                                                     this, result, true, false,
                                                     Tracer.isRecordCallstacks() ?
                                                     new Throwable("CALLSTACK") : null);

                     if (pool.getJanitor().isRecording())
                        pool.getJanitor().registerConnectionListener(result);

                     result.fromPool();

                     if (pool.getInternalStatistics().isEnabled())
                     {
                        pool.getInternalStatistics().deltaInUseCount(1);
                        pool.getInternalStatistics().deltaTotalGetTime(result.getFromPool() - timestamp);
                        pool.getInternalStatistics().deltaTotalPoolTime(result.getFromPool() - result.getToPool());
                     }
                     
                     return result;
                  }
                  else
                  {
                     if (Tracer.isEnabled())
                        Tracer.destroyConnectionListener(pool.getConfiguration().getId(),
                                                         this, cl, false, false, true, false, false,
                                                         false, false,
                                                         Tracer.isRecordCallstacks() ?
                                                         new Throwable("CALLSTACK") : null);
                     
                     if (pool.getConfiguration().isUseFastFail())
                        break;
                  }
               }
               else
               {
                  if (cl.changeState(VALIDATION, IN_USE))
                  {
                     if (Tracer.isEnabled())
                        Tracer.getConnectionListener(pool.getConfiguration().getId(),
                                                     this, cl, true, false,
                                                     Tracer.isRecordCallstacks() ?
                                                     new Throwable("CALLSTACK") : null);

                     if (pool.getJanitor().isRecording())
                        pool.getJanitor().registerConnectionListener(cl);

                     cl.fromPool();

                     if (pool.getInternalStatistics().isEnabled())
                     {
                        pool.getInternalStatistics().deltaInUseCount(1);
                        pool.getInternalStatistics().deltaTotalGetTime(cl.getFromPool() - timestamp);
                        pool.getInternalStatistics().deltaTotalPoolTime(cl.getFromPool() - cl.getToPool());
                     }
                     
                     return cl;
                  }
                  else
                  {
                     if (Tracer.isEnabled())
                        Tracer.destroyConnectionListener(pool.getConfiguration().getId(),
                                                         this, cl, false, false, false, false, true,
                                                         false, false,
                                                         Tracer.isRecordCallstacks() ?
                                                         new Throwable("CALLSTACK") : null);
                     
                     destroyAndRemoveConnectionListener(cl, listeners);
                  }
               }
            }
         }

         if (!pool.isFull())
         {
            try
            {
               ConnectionListener cl = pool.createConnectionListener(credential, this);

               if (Tracer.isEnabled())
                  Tracer.createConnectionListener(pool.getConfiguration().getId(),
                                                  this, cl, cl.getManagedConnection(),
                                                  true, false, false,
                                                  Tracer.isRecordCallstacks() ?
                                                  new Throwable("CALLSTACK") : null);

               listeners.addFirst(cl);

            }
            catch (ResourceException re)
            {
               throw re;
            }
            finally
            {
               prefill();
               // Trigger capacity increase
               if (pool.getCapacity().getIncrementer() != null)
                  CapacityFiller.schedule(new CapacityRequest(this));
            }
         }
         else
         {
            if (pool.getInternalStatistics().isEnabled())
               pool.getInternalStatistics().deltaWaitCount();
         }

         Thread.yield();
      }

      if (pool.getInternalStatistics().isEnabled())
         pool.getInternalStatistics().deltaBlockingFailureCount();

      throw new ResourceException("No ConnectionListener");
   }

   /**
    * {@inheritDoc}
    */
   public void returnConnectionListener(ConnectionListener cl, boolean kill) throws ResourceException
   {
      if (pool.getLogger().isTraceEnabled())
      {
         synchronized (this)
         {
            pool.getLogger().trace(ManagedConnectionPoolUtility.fullDetails(this,
               "returnConnectionListener(" + Integer.toHexString(System.identityHashCode(cl)) + ", " + kill + ")",
               pool.getConnectionManager().getManagedConnectionFactory(),
               pool.getConnectionManager(),
               pool, pool.getConfiguration(),
               listeners, pool.getInternalStatistics(),
               credential.getSubject(),
               credential.getConnectionRequestInfo()));
         }
      }
      else if (pool.getLogger().isDebugEnabled())
      {
         pool.getLogger().debug(ManagedConnectionPoolUtility.details(
            "returnConnectionListener(" + Integer.toHexString(System.identityHashCode(cl)) + ", " + kill + ")",
            pool.getConfiguration().getId(),
            getCount(IN_USE, listeners),
            pool.getConfiguration().getMaxSize()));
      }

      if (pool.getJanitor().isRecording())
         pool.getJanitor().unregisterConnectionListener(cl);

      if (cl.getState() != DESTROYED && pool.getInternalStatistics().isEnabled())
      {
         pool.getInternalStatistics().deltaInUseCount(-1);
         pool.getInternalStatistics().deltaTotalUsageTime(System.currentTimeMillis() - cl.getFromPool());
      }

      if (!kill)
      {
         if (cl.changeState(IN_USE, TO_POOL))
         {
            try
            {
               cl.getManagedConnection().cleanup();
               cl.toPool();
               if (!cl.changeState(TO_POOL, FREE))
                  kill = true;
            }
            catch (ResourceException re)
            {
               kill = true;
            }
         }
         else
         {
            kill = true;
         }
      }

      if (kill && cl.getState() != DESTROYED)
      {
         try
         {
            if (Tracer.isEnabled())
               Tracer.destroyConnectionListener(pool.getConfiguration().getId(),
                                                this, cl, true, false, false, false, false,
                                                false, false,
                                                Tracer.isRecordCallstacks() ?
                                                new Throwable("CALLSTACK") : null);
                     
            pool.destroyConnectionListener(cl);
         }
         finally
         {
            listeners.remove(cl);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void shutdown()
   {
      if (pool.getConfiguration().isBackgroundValidation() &&
          pool.getConfiguration().getBackgroundValidationMillis() > 0)
      {
         ConnectionValidator.getInstance().unregisterPool(this);
      }

      if (pool.getConfiguration().getIdleTimeoutMinutes() > 0)
      {
         IdleConnectionRemover.getInstance().unregisterPool(this);
      }

      for (ConnectionListener cl : listeners)
      {
         if (cl.getState() == IN_USE)
         {
            // TODO
         }
         else if (cl.getState() == DESTROY)
         {
            // TODO
         }

         try
         {
            if (Tracer.isEnabled())
               Tracer.clearConnectionListener(pool.getConfiguration().getId(), this, cl);

            pool.destroyConnectionListener(cl);
         }
         catch (ResourceException re)
         {
            // TODO
            cl.setState(ZOMBIE);
         }
      }
      listeners.clear();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void increaseCapacity()
   {
      //Only the prefill credential MCP should be affected by the incrementer
      if (!credential.equals(pool.getPrefillCredential()))
         return;
      // We have already created one connection when this method is scheduled
      int created = 1;
      boolean create = true;

      while (create && !pool.isFull())
      {

         if (pool.isShutdown())
         {
            return;
         }

         int currentSize = listeners.size();

         create = pool.getCapacity().getIncrementer()
               .shouldCreate(currentSize, pool.getConfiguration().getMaxSize(), created);

         if (create)
         {
            try
            {
               ConnectionListener cl = pool.createConnectionListener(credential, this);

               if (Tracer.isEnabled())
                  Tracer.createConnectionListener(pool.getConfiguration().getId(), this, cl, cl.getManagedConnection(),
                        false, false, true, Tracer.isRecordCallstacks() ? new Throwable("CALLSTACK") : null);

               boolean added = false;
               if (listeners.size() < pool.getConfiguration().getMaxSize())
               {
                  if (log.isTraceEnabled())
                     log.trace("Capacity fill: cl=" + cl);

                  listeners.add(cl);
                  created++;
                  added = true;
               }

               if (!added)
               {
                  if (Tracer.isEnabled())
                     Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl, false, false, true,
                           false, false, false, true, Tracer.isRecordCallstacks() ? new Throwable("CALLSTACK") : null);

                  pool.destroyConnectionListener(cl);
                  return;
               }
            }
            catch (ResourceException re)
            {
               log.unableFillPool(pool.getConfiguration().getId(), re);
               return;
            }
         }

      }
   }

   /**
    * Prefill
    */
   @Override public void prefill()
   {
      if (credential.equals(pool.getPrefillCredential()) &&
          pool.getConfiguration().isPrefill() &&
          pool.getConfiguration().getMinSize() > 0 &&
          listeners.size() < pool.getConfiguration().getMinSize())
      {
         PoolFiller.fillPool(new FillRequest(this, pool.getConfiguration().getMinSize()));
      }
   }

   /**
    * Fill to
    *
    * @param size The size
    */
   public void fillTo(int size)
   {
      if (size <= 0)
         return;

      if (pool.getLogger().isTraceEnabled())
      {
         synchronized (this)
         {
            pool.getLogger().trace(ManagedConnectionPoolUtility.fullDetails(this,
               "fillTo(" + size + ")",
               pool.getConnectionManager().getManagedConnectionFactory(),
               pool.getConnectionManager(),
               pool, pool.getConfiguration(),
               listeners, pool.getInternalStatistics(),
               credential.getSubject(),
               credential.getConnectionRequestInfo()));
         }
      }
      else if (pool.getLogger().isDebugEnabled())
      {
         pool.getLogger().debug(ManagedConnectionPoolUtility.details(
            "fillTo(" + size + ")",
            pool.getConfiguration().getId(),
            getCount(IN_USE, listeners),
            pool.getConfiguration().getMaxSize()));
      }

      while (!pool.isFull())
      {
         if (pool.isShutdown())
         {
            return;
         }

         // We already have enough connections
         if (listeners.size() >= size)
         {
            return;
         }

         // Create a connection to fill the pool
         try
         {
            ConnectionListener cl = pool.createConnectionListener(credential, this);

            if (Tracer.isEnabled())
               Tracer.createConnectionListener(pool.getConfiguration().getId(), this, cl,
                                               cl.getManagedConnection(),
                                               false, true, false,
                                               Tracer.isRecordCallstacks() ?
                                               new Throwable("CALLSTACK") : null);

            boolean added = false;

            if (listeners.size() < size)
            {

               listeners.add(cl);
               added = true;
            }

            if (!added)
            {
               if (Tracer.isEnabled())
                  Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl,
                                                   false, false, false, false,
                                                   false, true, false,
                                                   Tracer.isRecordCallstacks() ?
                                                   new Throwable("CALLSTACK") : null);
                     
               pool.destroyConnectionListener(cl);
               return;
            }
         }
         catch (ResourceException re)
         {
            return;
         }

      }
   }

   /**
    * {@inheritDoc}
    */
   public void validateConnections()
   {
      boolean anyDestroyed = false;

      ManagedConnectionFactory mcf = pool.getConnectionManager().getManagedConnectionFactory();

      if (mcf instanceof ValidatingManagedConnectionFactory)
      {
         ValidatingManagedConnectionFactory vcf = (ValidatingManagedConnectionFactory) mcf;
         long timestamp = System.currentTimeMillis();

         for (ConnectionListener cl : listeners)
         {
            if (cl.changeState(FREE, VALIDATION))
            {
               if (cl.getValidated() + pool.getConfiguration().getBackgroundValidationMillis() <= timestamp)
               {
                  ConnectionListener result = validateConnectionListener(listeners, cl, FREE);
                  if (result == null)
                  {
                     if (Tracer.isEnabled())
                        Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl,
                                                         false, false, true,
                                                         false, false, false, false,
                                                         Tracer.isRecordCallstacks() ?
                                                         new Throwable("CALLSTACK") : null);
                     
                     anyDestroyed = true;
                  }
               }
               else
               {
                  if (!cl.changeState(VALIDATION, FREE))
                  {
                     if (Tracer.isEnabled())
                        Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl,
                                                         false, false, false,
                                                         false, true, false, false,
                                                         Tracer.isRecordCallstacks() ?
                                                         new Throwable("CALLSTACK") : null);
                     
                     destroyAndRemoveConnectionListener(cl, listeners);
                  }
               }
            }
         }
      }
      else
      {
         log.validateOnMatchNonCompliantManagedConnectionFactory(mcf.getClass().getName());
      }

      if (anyDestroyed)
         prefill();
   }

   /**
    * {@inheritDoc}
    */
   public void removeIdleConnections()
   {
      long now = System.currentTimeMillis();
      long timeoutSetting = pool.getConfiguration().getIdleTimeoutMinutes() * 1000L * 60;

      CapacityDecrementer decrementer = pool.getCapacity().getDecrementer();

      if (decrementer == null || !credential.equals(pool.getPrefillCredential()))
      {
         decrementer = DefaultCapacity.DEFAULT_DECREMENTER;
      }

      if (TimedOutDecrementer.class.getName().equals(decrementer.getClass().getName()) ||
          TimedOutFIFODecrementer.class.getName().equals(decrementer.getClass().getName()))
      {
         // Allow through each minute
         if (now < (lastIdleCheck + 60000L))
            return;
      }
      else
      {
         // Otherwise, strict check
         if (now < (lastIdleCheck + timeoutSetting))
            return;
      }

      lastIdleCheck = now;

      long timeout = now - timeoutSetting;
      int destroyed = 0;

      if (pool.getLogger().isTraceEnabled())
      {
         synchronized (this)
         {
            pool.getLogger().trace(ManagedConnectionPoolUtility.fullDetails(this,
               "removeIdleConnections(" + timeout + ")",
               pool.getConnectionManager().getManagedConnectionFactory(),
               pool.getConnectionManager(),
               pool, pool.getConfiguration(),
               listeners, pool.getInternalStatistics(),
               credential.getSubject(),
               credential.getConnectionRequestInfo()));
         }
      }
      else if (pool.getLogger().isDebugEnabled())
      {
         pool.getLogger().debug(ManagedConnectionPoolUtility.details(
            "removeIdleConnections(" + timeout + ")",
            pool.getConfiguration().getId(),
            getCount(IN_USE, listeners),
            pool.getConfiguration().getMaxSize()));
      }

      for (ConnectionListener cl : listeners)
      {
         if (cl.changeState(FREE, VALIDATION))
         {
            if (decrementer
                  .shouldDestroy(cl, timeout, listeners.size(), pool.getConfiguration().getMinSize(), destroyed))
            {
               if (Tracer.isEnabled())
                  Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl,
                                                   false, true, false, false, false,
                                                   false, false,
                                                   Tracer.isRecordCallstacks() ?
                                                   new Throwable("CALLSTACK") : null);
                     
               if (pool.getInternalStatistics().isEnabled())
                  pool.getInternalStatistics().deltaTimedOut();
                     
               destroyAndRemoveConnectionListener(cl, listeners);
               destroyed++;
            }
            else
            {
               if (!cl.changeState(VALIDATION, FREE))
               {
                  if (Tracer.isEnabled())
                     Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl,
                                                      false, false, false, false, true,
                                                      false, false,
                                                      Tracer.isRecordCallstacks() ?
                                                      new Throwable("CALLSTACK") : null);
                     
                  if (pool.getInternalStatistics().isEnabled())
                     pool.getInternalStatistics().deltaTimedOut();
                     
                  destroyAndRemoveConnectionListener(cl, listeners);
                  destroyed++;
               }
            }
         }
      }

      if (!pool.isShutdown())
      {
         boolean emptyManagedConnectionPool = false;

         if (credential.equals(pool.getPrefillCredential()) && pool.getConfiguration().isPrefill())
         {
            if (pool.getConfiguration().getMinSize() > 0)
            {
               prefill();
            }
            else
            {
               emptyManagedConnectionPool = true;
            }
         }
         else
         {
            emptyManagedConnectionPool = true;
         }

         // Empty pool
         if (emptyManagedConnectionPool && listeners.isEmpty())
            pool.emptyManagedConnectionPool(this);
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isEmpty()
   {
      return listeners.isEmpty();
   }

   /**
    * {@inheritDoc}
    */
   public void flush(FlushMode mode)
   {

      // Destroy connections in the pool
      for (ConnectionListener cl : listeners)
      {
         switch (mode)
         {
            case ALL:
            {
               if (Tracer.isEnabled())
                  Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl,
                                                   false, false, false, true, false,
                                                   false, false,
                                                   Tracer.isRecordCallstacks() ?
                                                   new Throwable("CALLSTACK") : null);
                     
               destroyAndRemoveConnectionListener(cl, listeners);
               break;
            }

            case INVALID:
            {
               if (cl.changeState(FREE, VALIDATION))
               {
                  validateConnectionListener(listeners, cl, FREE);
               }
               break;
            }
            case IDLE:
            {
               if (cl.changeState(FREE, FLUSH))
               {
                  if (Tracer.isEnabled())
                     Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl,
                                                      false, false, false, true, false,
                                                      false, false,
                                                      Tracer.isRecordCallstacks() ?
                                                      new Throwable("CALLSTACK") : null);
                     
                  destroyAndRemoveConnectionListener(cl, listeners);
               }
               break;
            }
            case GRACEFULLY:
            {
               if (cl.changeState(FREE, FLUSH))
               {
                  if (Tracer.isEnabled())
                     Tracer.destroyConnectionListener(pool.getConfiguration().getId(), this, cl,
                                                      false, false, false, true, false,
                                                      false, false,
                                                      Tracer.isRecordCallstacks() ?
                                                      new Throwable("CALLSTACK") : null);
                  
                  destroyAndRemoveConnectionListener(cl, listeners);
               }
               else if (cl.getState() == IN_USE || cl.getState() == TO_POOL || cl.getState() == VALIDATION)
               {
                  cl.setState(DESTROY);
               }
               break;
            }
         }
      }

      // Trigger prefill
      prefill();
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener findConnectionListener(ManagedConnection mc, Object c)
   {
      return findConnectionListener(mc, c, listeners);
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener removeConnectionListener(boolean free)
   {
      return removeConnectionListener(free, listeners);
   }

}
