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

package org.ironjacamar.core.connectionmanager;

import org.ironjacamar.core.api.connectionmanager.ConnectionManagerConfiguration;
import org.ironjacamar.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.spi.transaction.TransactionIntegration;
import org.ironjacamar.core.spi.transaction.TxUtils;

import javax.resource.ResourceException;
import javax.resource.spi.LazyEnlistableManagedConnection;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * The base class for all transactional connection manager implementations
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractTransactionalConnectionManager extends AbstractConnectionManager
   implements TransactionalConnectionManager
{
   /** The transaction integration */
   protected TransactionIntegration ti;
   
   /**
    * Constructor
    * @param mcf The managed connection factory
    * @param ccm The cached connection manager
    * @param cmc The connection manager configuration
    * @param ti The transaction integration
    */
   public AbstractTransactionalConnectionManager(ManagedConnectionFactory mcf,
                                                 CachedConnectionManager ccm,
                                                 ConnectionManagerConfiguration cmc,
                                                 TransactionIntegration ti)
   {
      super(mcf, ccm, cmc);
      this.ti = ti;
   }

   /**
    * {@inheritDoc}
    */
   public TransactionIntegration getTransactionIntegration()
   {
      return ti;
   }

   /**
    * {@inheritDoc}
    */
   public void transactionStarted(ConnectionListener cl) throws ResourceException
   {
      try
      {
         if (!cl.isEnlisted() &&
             TxUtils.isUncommitted(ti.getTransactionManager().getTransaction()) &&
             shouldEnlist(cl))
            cl.enlist();
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

   /**
    * {@inheritDoc}
    */
   @Override
   public void lazyEnlist(ManagedConnection mc) throws ResourceException
   {
      if (!cmConfiguration.isEnlistment())
         throw new ResourceException();

      if (mc == null || !(mc instanceof LazyEnlistableManagedConnection))
         throw new ResourceException();

      pool.enlist(mc);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected org.ironjacamar.core.connectionmanager.listener.ConnectionListener
      getConnectionListener(Credential credential) throws ResourceException
   {
      org.ironjacamar.core.connectionmanager.listener.ConnectionListener cl =
         super.getConnectionListener(credential);

      try
      {
         if (!cl.isEnlisted() &&
             TxUtils.isUncommitted(ti.getTransactionManager().getTransaction()) &&
             shouldEnlist(cl))
            cl.enlist();
      
         return cl;
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

   /**
    * Should enlist the ConnectionListener
    * @param cl The ConnectionListener
    * @return True if enlist, otherwise false
    */
   private boolean shouldEnlist(ConnectionListener cl)
   {
      if (cmConfiguration.isEnlistment() && cl.getManagedConnection() instanceof LazyEnlistableManagedConnection)
         return false;

      return true;
   }
}
