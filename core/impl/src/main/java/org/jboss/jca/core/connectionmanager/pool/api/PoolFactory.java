/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.api;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.strategy.OnePool;
import org.jboss.jca.core.connectionmanager.pool.strategy.PoolByCri;
import org.jboss.jca.core.connectionmanager.pool.strategy.PoolBySubject;
import org.jboss.jca.core.connectionmanager.pool.strategy.PoolBySubjectAndCri;
import org.jboss.jca.core.connectionmanager.pool.strategy.ReauthPool;

import javax.resource.spi.ManagedConnectionFactory;

/**
 * The pool factory. 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PoolFactory
{
   /**
    * Constructor
    */
   public PoolFactory()
   {
   }

   /**
    * Create a pool
    * @param strategy The pool strategy
    * @param mcf The managed connection factory
    * @param pc The pool configuration
    * @param noTxSeparatePools no-tx separate pool
    * @param sharable Are the connections sharable
    * @param mcp ManagedConnectionPool
    * @return The pool instance
    */
   public Pool create(final PoolStrategy strategy,
                      final ManagedConnectionFactory mcf,
                      final PoolConfiguration pc,
                      final boolean noTxSeparatePools,
                      final boolean sharable,
                      final String mcp)
   {
      if (strategy == null)
         throw new IllegalArgumentException("Strategy is null");

      if (mcf == null)
         throw new IllegalArgumentException("MCF is null");

      if (pc == null)
         throw new IllegalArgumentException("PoolConfiguration is null");

      switch (strategy)
      {
         case POOL_BY_CRI:
            return new PoolByCri(mcf, pc, noTxSeparatePools, sharable, mcp);

         case POOL_BY_SUBJECT:
            return new PoolBySubject(mcf, pc, noTxSeparatePools, sharable, mcp);

         case POOL_BY_SUBJECT_AND_CRI:
            return new PoolBySubjectAndCri(mcf, pc, noTxSeparatePools, sharable, mcp);

         case ONE_POOL:
            return new OnePool(mcf, pc, noTxSeparatePools, sharable, mcp);

         case REAUTH:
            return new ReauthPool(mcf, pc, noTxSeparatePools, sharable, mcp);
      }

      throw new IllegalArgumentException("Unknown strategy " + strategy);
   }
}
