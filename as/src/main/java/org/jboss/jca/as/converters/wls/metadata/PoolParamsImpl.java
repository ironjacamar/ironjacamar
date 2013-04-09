/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.as.converters.wls.metadata;

import org.jboss.jca.as.converters.wls.api.metadata.PoolParams;

/**
*
* A generic PoolParams.
*
* @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
*
*/
public class PoolParamsImpl implements PoolParams
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2654156739973697322L;

   private final Integer initialCapacity;
   private final Integer maxCapacity;
   private final Integer shrinkFrequencySeconds;
   private final Integer connectionCreationRetryFrequencySeconds;
   private final Integer connectionReserveTimeoutSeconds;
   private final Integer testFrequencySeconds;
   
   /**
    * constructor
    * 
    * @param initialCapacity initialCapacity
    * @param maxCapacity maxCapacity
    * @param shrinkFrequencySeconds shrinkFrequencySeconds
    * @param connectionCreationRetryFrequencySeconds connectionCreationRetryFrequencySeconds
    * @param connectionReserveTimeoutSeconds connectionReserveTimeoutSeconds
    * @param testFrequencySeconds testFrequencySeconds
    */
   public PoolParamsImpl(Integer initialCapacity, Integer maxCapacity, Integer shrinkFrequencySeconds,
      Integer connectionCreationRetryFrequencySeconds, Integer connectionReserveTimeoutSeconds, 
      Integer testFrequencySeconds)
   {
      this.initialCapacity = initialCapacity;
      this.maxCapacity = maxCapacity;
      this.shrinkFrequencySeconds = shrinkFrequencySeconds;
      this.connectionCreationRetryFrequencySeconds = connectionCreationRetryFrequencySeconds;
      this.connectionReserveTimeoutSeconds = connectionReserveTimeoutSeconds;
      this.testFrequencySeconds = testFrequencySeconds;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getInitialCapacity()
    */
   @Override
   public Integer getInitialCapacity()
   {
      return initialCapacity;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getMaxCapacity()
    */
   @Override
   public Integer getMaxCapacity()
   {
      return maxCapacity;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getCapacityIncrement()
    */
   @Override
   public Boolean getCapacityIncrement()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getShrinkingEnabled()
    */
   @Override
   public Boolean getShrinkingEnabled()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getShrinkFrequencySeconds()
    */
   @Override
   public Integer getShrinkFrequencySeconds()
   {
      return shrinkFrequencySeconds;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getHighestNumWaiters()
    */
   @Override
   public Integer getHighestNumWaiters()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getHighestNumUnavailable()
    */
   @Override
   public Integer getHighestNumUnavailable()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getConnectionCreationRetryFrequencySeconds()
    */
   @Override
   public Integer getConnectionCreationRetryFrequencySeconds()
   {
      return connectionCreationRetryFrequencySeconds;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getConnectionReserveTimeoutSeconds()
    */
   @Override
   public Integer getConnectionReserveTimeoutSeconds()
   {
      return connectionReserveTimeoutSeconds;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getTestFrequencySeconds()
    */
   @Override
   public Integer getTestFrequencySeconds()
   {
      // TODO Auto-generated method stub
      return testFrequencySeconds;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getTestConnectionsOnCreate()
    */
   @Override
   public Boolean getTestConnectionsOnCreate()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getTestConnectionsOnRelease()
    */
   @Override
   public Boolean getTestConnectionsOnRelease()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getTestConnectionsOnReserve()
    */
   @Override
   public Boolean getTestConnectionsOnReserve()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getProfileHarvestFrequencySeconds()
    */
   @Override
   public Integer getProfileHarvestFrequencySeconds()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getIgnoreInUseConnectionsEnabled()
    */
   @Override
   public Boolean getIgnoreInUseConnectionsEnabled()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getMatchConnectionsSupported()
    */
   @Override
   public Boolean getMatchConnectionsSupported()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.PoolParams#getUseFirstAvailable()
    */
   @Override
   public Boolean getUseFirstAvailable()
   {
      return null;
   }

}
