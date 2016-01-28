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
import org.ironjacamar.core.connectionmanager.pool.ConnectionValidator;
import org.ironjacamar.core.connectionmanager.pool.FillRequest;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;
import org.ironjacamar.core.connectionmanager.pool.PoolFiller;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROY;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.FREE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.IN_USE;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.TO_POOL;
import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.VALIDATION;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ValidatingManagedConnectionFactory;

/**
 * The stable ManagedConnectionPool
 */
public class StableManagedConnectionPool implements ManagedConnectionPool
{
   /**
    * The associated pool
    */
   private StablePool pool;

   /**
    * The credential
    */
   private Credential credential;

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
      this.pool = pool;
      this.credential = credential;
      this.listeners = new ConcurrentLinkedDeque<ConnectionListener>();

      if (this.credential.equals(pool.getPrefillCredential()) &&
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
                     ConnectionListener result = validateConnectionListener(cl, IN_USE);
                     if (result != null)
                     {
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
                     cl.changeState(VALIDATION, IN_USE);
                     return cl;
                  }
               }
            }

            try
            {
               ConnectionListener cl = pool.createConnectionListener(credential);
               cl.setState(IN_USE);
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
                  cl.changeState(TO_POOL, FREE);
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
            if (cl.getState() == DESTROY || cl.changeState(IN_USE, DESTROY) || cl.changeState(TO_POOL, DESTROY))
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
         }
      }
      listeners.clear();
   }

   /**
    * Prefill
    */
   @Override public void prefill()
   {
      if (this.credential.equals(pool.getPrefillCredential()) && pool.getConfiguration().isPrefill()
            && pool.getConfiguration().getMinSize() > 0)
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
               if (cl.getValidated() + pool.getConfiguration().getBackgroundValidationMillis() < timestamp)
               {
                  ConnectionListener result = validateConnectionListener(cl, FREE);
                  if (result == null)
                     anyDestroyed = true;
               }
               else
               {
                  cl.changeState(VALIDATION, FREE);
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
    * Validate a connection listener
    * @param cl The connection listener
    * @param newState The new state
    * @return The validated connection listener, or <code>null</code> if validation failed
    */
   private ConnectionListener validateConnectionListener(ConnectionListener cl, int newState)
   {
      ManagedConnectionFactory mcf = pool.getConnectionManager().getManagedConnectionFactory();

      if (mcf instanceof ValidatingManagedConnectionFactory)
      {
         ValidatingManagedConnectionFactory vcf = (ValidatingManagedConnectionFactory)mcf;
         try
         {
            Set candidateSet = Collections.singleton(cl.getManagedConnection());
            candidateSet = vcf.getInvalidConnections(candidateSet);

            if (candidateSet != null && candidateSet.size() > 0)
            {
               try
               {
                  pool.destroyConnectionListener(cl);
               }
               catch (ResourceException e)
               {
                  // TODO:
               }
               finally
               {
                  listeners.remove(cl);
               }
            }
            else
            {
               cl.validated();
               cl.changeState(VALIDATION, newState);
               return cl;
            }
         }
         catch (ResourceException re)
         {
            try
            {
               pool.destroyConnectionListener(cl);
            }
            catch (ResourceException e)
            {
               // TODO:
            }
            finally
            {
               listeners.remove(cl);
            }
         }
      }
      else
      {
         // TODO: log
         cl.changeState(VALIDATION, newState);
         return cl;
      }

      return null;
   }
}
