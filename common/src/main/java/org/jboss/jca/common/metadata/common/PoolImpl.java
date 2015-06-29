/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.common;

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.common.Capacity;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Pool;
import org.jboss.jca.common.api.validator.ValidateException;

import org.jboss.logging.Messages;

/**
 *
 * A PoolImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class PoolImpl implements Pool
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
    * minPoolSize
    */
   protected Integer minPoolSize;

   /**
    * initial-pool-size
    */
   protected Integer initialPoolSize;

   /**
    * maxPoolSize
    */
   protected Integer maxPoolSize;

   /**
    * prefill
    */
   protected Boolean prefill;

   /**
    * use-strict-min
    */
   protected Boolean useStrictMin;

   /**
    * flush-strategy
    */
   protected FlushStrategy flushStrategy;

   /**
    * capacity
    */
   protected Capacity capacity;

   /**
    * Constructor
    *
    * @param minPoolSize minPoolSize
    * @param initialPoolSize initialPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param useStrictMin useStrictMin
    * @param flushStrategy flushStrategy
    * @param capacity capacity
    * @throws ValidateException ValidateException
    */
   public PoolImpl(Integer minPoolSize, Integer initialPoolSize, Integer maxPoolSize, 
                   Boolean prefill, Boolean useStrictMin,
                   FlushStrategy flushStrategy, Capacity capacity)
      throws ValidateException
   {
      this.minPoolSize = minPoolSize;
      this.initialPoolSize = initialPoolSize;
      this.maxPoolSize = maxPoolSize;
      this.prefill = prefill;
      this.useStrictMin = useStrictMin;
      this.flushStrategy = flushStrategy;
      this.capacity = capacity;
      this.validate();
   }

   /**
    * {@inheritDoc}
    */
   public Integer getMinPoolSize()
   {
      return minPoolSize;
   }

   /**
    * {@inheritDoc}
    */
   public Integer getInitialPoolSize()
   {
      return initialPoolSize;
   }

   /**
    * {@inheritDoc}
    */
   public Integer getMaxPoolSize()
   {
      return maxPoolSize;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isPrefill()
   {
      return prefill;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isUseStrictMin()
   {
      return useStrictMin;
   }

   /**
    * {@inheritDoc}
    */
   public FlushStrategy getFlushStrategy()
   {
      return flushStrategy;
   }

   /**
    * {@inheritDoc}
    */
   public Capacity getCapacity()
   {
      return capacity;
   }

   /**
    * {@inheritDoc}
    */
   public void validate() throws ValidateException
   {
      if (this.maxPoolSize != null && this.maxPoolSize.intValue() < 0)
         throw new ValidateException(bundle.invalidNegative(Tag.MAX_POOL_SIZE.getLocalName()));

      if (this.maxPoolSize != null && this.maxPoolSize.intValue() == 0)
         throw new ValidateException(bundle.invalidZero(Tag.MAX_POOL_SIZE.getLocalName()));

      if (this.minPoolSize != null && this.minPoolSize.intValue() < 0)
         throw new ValidateException(bundle.invalidNegative(Tag.MIN_POOL_SIZE.getLocalName()));

      if (this.minPoolSize != null && this.maxPoolSize != null)
      {
         if (minPoolSize.intValue() > maxPoolSize.intValue())
            throw new ValidateException(bundle.notValidNumber(minPoolSize.toString(),
                                                              Tag.MIN_POOL_SIZE.getLocalName()));
      }

      if (this.flushStrategy == null)
         throw new ValidateException(bundle.nullValue(Tag.FLUSH_STRATEGY.getLocalName()));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((minPoolSize == null) ? 0 : minPoolSize.hashCode());
      result = prime * result + ((initialPoolSize == null) ? 0 : initialPoolSize.hashCode());
      result = prime * result + ((maxPoolSize == null) ? 0 : maxPoolSize.hashCode());
      result = prime * result + ((prefill == null) ? 0 : prefill.hashCode());
      result = prime * result + ((useStrictMin == null) ? 0 : useStrictMin.hashCode());
      result = prime * result + ((flushStrategy == null) ? 0 : flushStrategy.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof PoolImpl))
         return false;
      PoolImpl other = (PoolImpl) obj;
      if (minPoolSize == null)
      {
         if (other.minPoolSize != null)
            return false;
      }
      else if (!minPoolSize.equals(other.minPoolSize))
         return false;
      if (initialPoolSize == null)
      {
         if (other.initialPoolSize != null)
            return false;
      }
      else if (!initialPoolSize.equals(other.initialPoolSize))
         return false;
      if (maxPoolSize == null)
      {
         if (other.maxPoolSize != null)
            return false;
      }
      else if (!maxPoolSize.equals(other.maxPoolSize))
         return false;
      if (prefill == null)
      {
         if (other.prefill != null)
            return false;
      }
      else if (!prefill.equals(other.prefill))
         return false;
      if (useStrictMin == null)
      {
         if (other.useStrictMin != null)
            return false;
      }
      else if (!useStrictMin.equals(other.useStrictMin))
         return false;
      if (flushStrategy == null)
      {
         if (other.flushStrategy != null)
            return false;
      }
      else if (!flushStrategy.equals(other.flushStrategy))
         return false;
      if (capacity == null)
      {
         if (other.capacity != null)
            return false;
      }
      else if (!capacity.equals(other.capacity))
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<pool>");

      if (minPoolSize != null)
      {
         sb.append("<").append(Pool.Tag.MIN_POOL_SIZE).append(">");
         sb.append(minPoolSize);
         sb.append("</").append(Pool.Tag.MIN_POOL_SIZE).append(">");
      }

      if (initialPoolSize != null)
      {
         sb.append("<").append(Pool.Tag.INITIAL_POOL_SIZE).append(">");
         sb.append(initialPoolSize);
         sb.append("</").append(Pool.Tag.INITIAL_POOL_SIZE).append(">");
      }

      if (maxPoolSize != null)
      {
         sb.append("<").append(Pool.Tag.MAX_POOL_SIZE).append(">");
         sb.append(maxPoolSize);
         sb.append("</").append(Pool.Tag.MAX_POOL_SIZE).append(">");
      }

      if (prefill != null)
      {
         sb.append("<").append(Pool.Tag.PREFILL).append(">");
         sb.append(prefill);
         sb.append("</").append(Pool.Tag.PREFILL).append(">");
      }

      if (useStrictMin != null)
      {
         sb.append("<").append(Pool.Tag.USE_STRICT_MIN).append(">");
         sb.append(useStrictMin);
         sb.append("</").append(Pool.Tag.USE_STRICT_MIN).append(">");
      }

      if (flushStrategy != null)
      {
         sb.append("<").append(Pool.Tag.FLUSH_STRATEGY).append(">");
         sb.append(flushStrategy);
         sb.append("</").append(Pool.Tag.FLUSH_STRATEGY).append(">");
      }

      if (capacity != null)
      {
         sb.append("<").append(Pool.Tag.CAPACITY).append(">");
         sb.append(capacity);
         sb.append("</").append(Pool.Tag.CAPACITY).append(">");
      }

      sb.append("</pool>");
      
      return sb.toString();
   }
}
