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

package org.ironjacamar.core.connectionmanager.pool.stable;

import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.AbstractManagedConnectionPool;
import org.ironjacamar.core.connectionmanager.pool.ConnectionValidator;
import org.ironjacamar.core.connectionmanager.pool.FillRequest;
import org.ironjacamar.core.connectionmanager.pool.IdleConnectionRemover;
import org.ironjacamar.core.connectionmanager.pool.PoolFiller;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROY;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.FREE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.IN_USE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.TO_POOL;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.VALIDATION;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.ZOMBIE;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ValidatingManagedConnectionFactory;

/**
 * The stable ManagedConnectionPool
 */
public class StableManagedConnectionPool extends AbstractManagedConnectionPool
{
   /**
    * The associated pool
    */
   private StablePool pool;

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
   public StableManagedConnectionPool(StablePool pool, Credential credential)
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

      if (pool.getConfiguration().isBackgroundValidation() &&
          pool.getConfiguration().getBackgroundValidationMillis() > 0)
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
      // Use request semaphore, as a fair queue across all credentials
      try
      {
         if (pool.getRequestSemaphore().tryAcquire(pool.getConfiguration().getBlockingTimeout(), TimeUnit.MILLISECONDS))
         {
            for (ConnectionListener cl : listeners)
            {
               if (cl.changeState(FREE, VALIDATION))
               {
                  if (pool.getConfiguration().isValidateOnMatch())
                  {
                     ConnectionListener result = validateConnectionListener(listeners, cl, IN_USE);
                     if (result != null)
                     {
                        result.fromPool();
                        return result;
                     }
                     else
                     {
                        if (pool.getConfiguration().isUseFastFail())
                           break;
                     }
                  }
                  else
                  {
                     if (cl.changeState(VALIDATION, IN_USE))
                     {
                        cl.fromPool();
                        return cl;
                     }
                     else
                     {
                        destroyAndRemoveConnectionListener(cl, listeners);
                     }
                  }
               }
            }

            try
            {
               ConnectionListener cl = pool.createConnectionListener(credential);
               cl.setState(IN_USE);
               cl.fromPool();
               listeners.addLast(cl);
               return cl;
            }
            catch (ResourceException re)
            {
            }
            finally
            {
               prefill();
            }
         }
      }
      catch (Exception e)
      {
         // TODO
      }

      throw new ResourceException("No ConnectionListener");
   }

   /**
    * {@inheritDoc}
    */
   public void returnConnectionListener(ConnectionListener cl, boolean kill) throws ResourceException
   {
      try
      {
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

         if (kill)
         {
            try
            {
               pool.destroyConnectionListener(cl);
            }
            finally
            {
               listeners.remove(cl);
            }
         }
      }
      finally
      {
         pool.getRequestSemaphore().release();
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
    * Prefill
    */
   @Override
   public void prefill()
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


      //TODO: trace and debug here

      while (!pool.isFull())
      {
         // Get a permit - avoids a race when the pool is nearly full
         // Also avoids unnessary fill checking when all connections are checked out
         try
         {
            //TODO:statistics
            if (pool.getRequestSemaphore()
                  .tryAcquire(pool.getConfiguration().getBlockingTimeout(), TimeUnit.MILLISECONDS))
            {
               try
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
                     ConnectionListener cl = pool.createConnectionListener(credential);

                     //TODO:Trace
                     boolean added = false;

                     if (listeners.size() < size)
                     {

                        listeners.add(cl);
                        added = true;
                     }

                     if (!added)
                     {
                        //TODO: Trace
                        pool.destroyConnectionListener(cl);
                        return;
                     }
                  }
                  catch (ResourceException re)
                  {
                     return;
                  }
               }
               finally
               {
                  pool.getRequestSemaphore().release();
               }
            }
         }
         catch (InterruptedException ignored)
         {
            Thread.interrupted();
            //TODO:trace
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
         ValidatingManagedConnectionFactory vcf = (ValidatingManagedConnectionFactory)mcf;
         long timestamp = System.currentTimeMillis();

         for (ConnectionListener cl : listeners)
         {
            if (cl.changeState(FREE, VALIDATION))
            {
               if (cl.getValidated() + pool.getConfiguration().getBackgroundValidationMillis() <= timestamp)
               {
                  ConnectionListener result = validateConnectionListener(listeners, cl, FREE);
                  if (result == null)
                     anyDestroyed = true;
               }
               else
               {
                  if (!cl.changeState(VALIDATION, FREE))
                  {
                     destroyAndRemoveConnectionListener(cl, listeners);
                  }
               }
            }
         }
      }
      else
      {
         // TODO: log
      }

      if (anyDestroyed)
         prefill();
   }

   /**
    * {@inheritDoc}
    */
   public void removeIdleConnections()
   {
      long timeout = System.currentTimeMillis() - pool.getConfiguration().getIdleTimeoutMinutes() * 1000L * 60;

      for (ConnectionListener cl : listeners)
      {
         if (cl.changeState(FREE, VALIDATION))
         {
            if (cl.getToPool() < timeout)
            {
               destroyAndRemoveConnectionListener(cl, listeners);
            }
            else
            {
               if (!cl.changeState(VALIDATION, FREE))
               {
                  destroyAndRemoveConnectionListener(cl, listeners);
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
         if (emptyManagedConnectionPool && listeners.size() == 0)
            pool.emptyManagedConnectionPool(this);
      }
   }
}
