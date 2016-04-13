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

import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.api.connectionmanager.pool.PoolConfiguration;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.TransactionalConnectionManager;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.connectionmanager.listener.stable.LocalTransactionConnectionListener;
import org.ironjacamar.core.connectionmanager.listener.stable.NoTransactionConnectionListener;
import org.ironjacamar.core.connectionmanager.listener.stable.XATransactionConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.AbstractPool;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;

import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.transaction.Transaction;

import org.jboss.logging.Logger;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.DESTROYED;

/**
 * The stable pool
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class StablePool extends AbstractPool
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, 
                                                           StablePool.class.getName());

   /** Request semaphore */
   private Semaphore requestSemaphore;

   /**
    * Constructor
    * @param cm The connection manager
    * @param pc The pool configuration
    */
   public StablePool(ConnectionManager cm, PoolConfiguration pc)
   {
      super(cm, pc);
      this.requestSemaphore = new Semaphore(pc.getMaxSize(), true);
   }

   /**
    * {@inheritDoc}
    */
   public String getType()
   {
      return "stable";
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
   public ConnectionListener createConnectionListener(Credential credential, ManagedConnectionPool mcp)
      throws ResourceException
   {
      try
      {
         if (semaphore.tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
         {
            long start = getInternalStatistics().isEnabled() ? System.currentTimeMillis() : 0L;

            ManagedConnection mc =
               cm.getManagedConnectionFactory().createManagedConnection(credential.getSubject(),
                                                                        credential.getConnectionRequestInfo());

            if (getInternalStatistics().isEnabled())
            {
               getInternalStatistics().deltaCreatedCount();
               getInternalStatistics().deltaTotalCreationTime(System.currentTimeMillis() - start);
            }

            if (cm.getTransactionSupport() == TransactionSupportLevel.NoTransaction)
            {
               return new NoTransactionConnectionListener(cm, mc, credential, mcp, cm.getPool().getFlushStrategy());
            }
            else if (cm.getTransactionSupport() == TransactionSupportLevel.LocalTransaction)
            {
               return new LocalTransactionConnectionListener(cm, mc, credential, getLocalXAResource(mc), mcp,
                     cm.getPool().getFlushStrategy());
            }
            else
            {
               return new XATransactionConnectionListener(cm, mc, credential, getXAResource(mc),
                     cm.getConnectionManagerConfiguration().getXAResourceTimeout(), mcp,
                     cm.getPool().getFlushStrategy());
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

      throw new ResourceException("No ConnectionListener");
   }

   /**
    * {@inheritDoc}
    */
   public void destroyConnectionListener(ConnectionListener cl) throws ResourceException
   {
      if (getInternalStatistics().isEnabled())
         getInternalStatistics().deltaDestroyedCount();

      try
      {
         cl.getManagedConnection().destroy();
      }
      catch (Exception e)
      {
         throw new ResourceException(e);
      }
      finally
      {
         cl.setState(DESTROYED);
         semaphore.release();
      }
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnectionPool createManagedConnectionPool(Credential credential)
   {
      return new StableManagedConnectionPool(this, credential);
   }

   /**
    * Verify if a connection listener is already in use
    * @param cl The connection listener
    * @return The transaction object if in use, or null if not
    * @exception ResourceException Thrown in case of an error
    */
   public synchronized Object verifyConnectionListener(ConnectionListener cl) throws ResourceException
   {
      for (Map.Entry<Object, Map<ManagedConnectionPool, ConnectionListener>> entry : transactionMap.entrySet())
      {
         if (entry.getValue().values().contains(cl))
         {
            try
            {
               TransactionalConnectionManager txCM = (TransactionalConnectionManager)cm;
               Transaction tx = txCM.getTransactionIntegration().getTransactionManager().getTransaction();
               Object id = txCM.getTransactionIntegration().getTransactionSynchronizationRegistry().getTransactionKey();

               if (!id.equals(entry.getKey()))
                  return entry.getKey();
            }
            catch (Exception e)
            {
               throw new ResourceException(e);
            }
         }
      }

      return null;
   }

   /**
    * Get the request semaphore
    * @return The value
    */
   Semaphore getRequestSemaphore()
   {
      return requestSemaphore;
   }
}
