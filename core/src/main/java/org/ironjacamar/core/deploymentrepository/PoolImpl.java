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

package org.ironjacamar.core.deploymentrepository;

import org.ironjacamar.core.api.connectionmanager.pool.CapacityDecrementer;
import org.ironjacamar.core.api.connectionmanager.pool.CapacityIncrementer;
import org.ironjacamar.core.api.deploymentrepository.Pool;
import org.ironjacamar.core.spi.statistics.StatisticsPlugin;

/**
 * A pool implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PoolImpl implements Pool
{
   /** The pool */
   private org.ironjacamar.core.api.connectionmanager.pool.Pool pool;

   /** The statistics */
   private StatisticsPlugin statistics;

   /**The capacity incrementer */
   private CapacityIncrementer capacityIncrementer;

   /**The capacity decrementer */
   private CapacityDecrementer capacityDecrementer;

   /**
    * Constructor
    * @param pool The pool
    * @param statistics The statistics
    * @param capacityIncrementer the CapacityIncrementer
    * @param capacityDecrementer the CapacityDecrementer
    */
   public PoolImpl(org.ironjacamar.core.api.connectionmanager.pool.Pool pool,
                   StatisticsPlugin statistics,
                   CapacityIncrementer capacityIncrementer,
                   CapacityDecrementer capacityDecrementer)
   {
      this.pool = pool;
      this.statistics = statistics;
      this.capacityIncrementer = capacityIncrementer;
      this.capacityDecrementer = capacityDecrementer;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public org.ironjacamar.core.api.connectionmanager.pool.Pool getPool()
   {
      return pool;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public org.ironjacamar.core.api.connectionmanager.pool.Janitor getJanitor()
   {
      return pool.getJanitor();
   }

   /**
    * {@inheritDoc}
    */
   public StatisticsPlugin getStatistics()
   {
      return statistics;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public CapacityIncrementer getCapacityIncrementer()
   {
      return capacityIncrementer;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public CapacityDecrementer getCapacityDecrementer()
   {
      return capacityDecrementer;
   }
}
