/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

import org.ironjacamar.core.connectionmanager.Credential;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;

import static org.ironjacamar.core.connectionmanager.listener.ConnectionListener.VALIDATION;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ValidatingManagedConnectionFactory;

/**
 * The base class for all ManagedConnectionPool implementations
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractManagedConnectionPool implements ManagedConnectionPool
{
   /** The pool */
   protected Pool pool;

   /** The credential */
   protected Credential credential;

   /**
    * Constructor
    * @param pool The pool
    * @param credential The credential
    */
   public AbstractManagedConnectionPool(Pool pool, Credential credential)
   {
      this.pool = pool;
      this.credential = credential;
   }

   /**
    * Validate a connection listener
    * @param listeners The listeners
    * @param cl The connection listener
    * @param newState The new state
    * @return The validated connection listener, or <code>null</code> if validation failed
    */
   protected ConnectionListener validateConnectionListener(Collection<ConnectionListener> listeners,
                                                           ConnectionListener cl,
                                                           int newState)
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
