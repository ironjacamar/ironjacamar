/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.validator.ValidateException;

import org.jboss.logging.Messages;

/**
 *
 * A PoolImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class CommonPoolImpl implements CommonPool
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -8705723067326455982L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
    * minPoolSize
    */
   protected final Integer minPoolSize;

   /**
   * maxPoolSize
   */
   protected final Integer maxPoolSize;

   /**
   * prefill
   */
   protected final Boolean prefill;

   /**
    * use-strict-min
    */
   protected final Boolean useStrictMin;

   /**
    * flush-strategy
    */
   protected final FlushStrategy flushStrategy;

   /**
    * Create a new PoolImpl.
    *
    * @param minPoolSize minPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param useStrictMin useStrictMin
    * @param flushStrategy flushStrategy
    * @throws ValidateException ValidateException
    */
   public CommonPoolImpl(Integer minPoolSize, Integer maxPoolSize, 
                         Boolean prefill, Boolean useStrictMin,
                         FlushStrategy flushStrategy)
      throws ValidateException
   {
      super();
      this.minPoolSize = minPoolSize;
      this.maxPoolSize = maxPoolSize;
      this.prefill = prefill;
      this.useStrictMin = useStrictMin;
      this.flushStrategy = flushStrategy;
      this.validate();
   }

   /**
    * Get the minPoolSize.
    *
    * @return the minPoolSize.
    */
   @Override
   public final Integer getMinPoolSize()
   {
      return minPoolSize;
   }

   /**
    * Get the maxPoolSize.
    *
    * @return the maxPoolSize.
    */
   @Override
   public final Integer getMaxPoolSize()
   {
      return maxPoolSize;
   }

   /**
    * Get the prefill.
    *
    * @return the prefill.
    */
   @Override
   public final Boolean isPrefill()
   {
      return prefill;
   }

   /**
    * Get the useStrictMin.
    *
    * @return the useStrictMin.
    */
   @Override
   public final Boolean isUseStrictMin()
   {
      return useStrictMin;
   }

   /**
    * Get the flush strategy.
    * @return The value
    */
   @Override
   public final FlushStrategy getFlushStrategy()
   {
      return flushStrategy;
   }

   @Override
   public void validate() throws ValidateException
   {
      if (this.maxPoolSize != null && this.maxPoolSize < 0)
         throw new ValidateException(bundle.invalidNegative(Tag.MAX_POOL_SIZE.getLocalName()));

      if (this.minPoolSize != null && this.minPoolSize < 0)
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

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((minPoolSize == null) ? 0 : minPoolSize.hashCode());
      result = prime * result + ((maxPoolSize == null) ? 0 : maxPoolSize.hashCode());
      result = prime * result + ((prefill == null) ? 0 : prefill.hashCode());
      result = prime * result + ((useStrictMin == null) ? 0 : useStrictMin.hashCode());
      result = prime * result + ((flushStrategy == null) ? 0 : flushStrategy.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof CommonPoolImpl))
         return false;
      CommonPoolImpl other = (CommonPoolImpl) obj;
      if (minPoolSize == null)
      {
         if (other.minPoolSize != null)
            return false;
      }
      else if (!minPoolSize.equals(other.minPoolSize))
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
         sb.append("<").append(CommonPool.Tag.MIN_POOL_SIZE).append(">");
         sb.append(minPoolSize);
         sb.append("</").append(CommonPool.Tag.MIN_POOL_SIZE).append(">");
      }

      if (maxPoolSize != null)
      {
         sb.append("<").append(CommonPool.Tag.MAX_POOL_SIZE).append(">");
         sb.append(maxPoolSize);
         sb.append("</").append(CommonPool.Tag.MAX_POOL_SIZE).append(">");
      }

      if (prefill != null)
      {
         sb.append("<").append(CommonPool.Tag.PREFILL).append(">");
         sb.append(prefill);
         sb.append("</").append(CommonPool.Tag.PREFILL).append(">");
      }

      if (useStrictMin != null)
      {
         sb.append("<").append(CommonPool.Tag.USE_STRICT_MIN).append(">");
         sb.append(useStrictMin);
         sb.append("</").append(CommonPool.Tag.USE_STRICT_MIN).append(">");
      }

      if (flushStrategy != null)
      {
         sb.append("<").append(CommonPool.Tag.FLUSH_STRATEGY).append(">");
         sb.append(flushStrategy);
         sb.append("</").append(CommonPool.Tag.FLUSH_STRATEGY).append(">");
      }

      sb.append("</pool>");
      
      return sb.toString();
   }
}
