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
package org.jboss.jca.common.metadata.common.v11;

import org.jboss.jca.common.api.metadata.common.Capacity;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.v11.ConnDefPool;
import org.jboss.jca.common.api.validator.ValidateException;

/**
 * A pool for ConnectionDefinition.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConnDefPoolImpl extends org.jboss.jca.common.metadata.common.CommonPoolImpl implements ConnDefPool
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 3L;

   private Integer initialPoolSize;

   private Capacity capacity;

   /**
    * Construcot
    * @param minPoolSize minPoolSize
    * @param initialPoolSize initialPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param useStrictMin useStrictMin
    * @param flushStrategy flushStrategy
    * @param capacity capacity
    * @throws ValidateException ValidateException
    */
   public ConnDefPoolImpl(Integer minPoolSize, Integer initialPoolSize, Integer maxPoolSize, 
                          Boolean prefill, Boolean useStrictMin,
                          FlushStrategy flushStrategy, Capacity capacity)
      throws ValidateException
   {
      super(minPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy);
      this.initialPoolSize = initialPoolSize;
      this.capacity = capacity;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Integer getInitialPoolSize()
   {
      return initialPoolSize;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Capacity getCapacity()
   {
      return capacity;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((initialPoolSize == null) ? 7 : 7 * initialPoolSize.hashCode());
      result = prime * result + ((capacity == null) ? 7 : 7 * capacity.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof ConnDefPoolImpl))
         return false;
      if (!super.equals(obj))
         return false;

      ConnDefPoolImpl other = (ConnDefPoolImpl) obj;
      if (initialPoolSize == null)
      {
         if (other.initialPoolSize != null)
            return false;
      }
      else if (!initialPoolSize.equals(other.initialPoolSize))
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

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<pool>");

      if (minPoolSize != null)
      {
         sb.append("<").append(ConnDefPool.Tag.MIN_POOL_SIZE).append(">");
         sb.append(minPoolSize);
         sb.append("</").append(ConnDefPool.Tag.MIN_POOL_SIZE).append(">");
      }

      if (initialPoolSize != null)
      {
         sb.append("<").append(ConnDefPool.Tag.INITIAL_POOL_SIZE).append(">");
         sb.append(initialPoolSize);
         sb.append("</").append(ConnDefPool.Tag.INITIAL_POOL_SIZE).append(">");
      }

      if (maxPoolSize != null)
      {
         sb.append("<").append(ConnDefPool.Tag.MAX_POOL_SIZE).append(">");
         sb.append(maxPoolSize);
         sb.append("</").append(ConnDefPool.Tag.MAX_POOL_SIZE).append(">");
      }

      if (prefill != null)
      {
         sb.append("<").append(ConnDefPool.Tag.PREFILL).append(">");
         sb.append(prefill);
         sb.append("</").append(ConnDefPool.Tag.PREFILL).append(">");
      }

      if (useStrictMin != null)
      {
         sb.append("<").append(ConnDefPool.Tag.USE_STRICT_MIN).append(">");
         sb.append(useStrictMin);
         sb.append("</").append(ConnDefPool.Tag.USE_STRICT_MIN).append(">");
      }

      if (flushStrategy != null)
      {
         sb.append("<").append(ConnDefPool.Tag.FLUSH_STRATEGY).append(">");
         sb.append(flushStrategy);
         sb.append("</").append(ConnDefPool.Tag.FLUSH_STRATEGY).append(">");
      }

      if (capacity != null)
      {
         sb.append("<").append(ConnDefPool.Tag.CAPACITY).append(">");
         sb.append(capacity);
         sb.append("</").append(ConnDefPool.Tag.CAPACITY).append(">");
      }

      sb.append("</pool>");
      
      return sb.toString();
   }
}
