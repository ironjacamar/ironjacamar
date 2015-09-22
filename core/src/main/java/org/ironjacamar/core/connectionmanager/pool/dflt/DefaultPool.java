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

import org.ironjacamar.core.api.connectionmanager.pool.PoolConfiguration;
import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;
import org.ironjacamar.core.connectionmanager.listener.NoTransactionConnectionListener;
import org.ironjacamar.core.connectionmanager.pool.AbstractPool;
import org.ironjacamar.core.connectionmanager.pool.ManagedConnectionPool;

import java.util.concurrent.TimeUnit;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;

/**
 * The default pool
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DefaultPool extends AbstractPool
{
   /**
    * Constructor
    * @param cm The connection manager
    * @param pc The pool configuration
    */
   public DefaultPool(ConnectionManager cm, PoolConfiguration pc)
   {
      super(cm, pc);
   }

   /**
    * {@inheritDoc}
    */
   public ConnectionListener createConnectionListener(Credential credential)
      throws ResourceException
   {
      try
      {
         if (semaphore.tryAcquire(poolConfiguration.getBlockingTimeout(), TimeUnit.MILLISECONDS))
         {
            ManagedConnection mc =
               cm.getManagedConnectionFactory().createManagedConnection(credential.getSubject(),
                                                                        credential.getConnectionRequestInfo());
            return new NoTransactionConnectionListener(cm, mc, credential);
         }
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
         semaphore.release();
      }
   }

   /**
    * {@inheritDoc}
    */
   protected ManagedConnectionPool createManagedConnectionPool(Credential credential)
   {
      return new DefaultManagedConnectionPool(this, credential);
   }
}
