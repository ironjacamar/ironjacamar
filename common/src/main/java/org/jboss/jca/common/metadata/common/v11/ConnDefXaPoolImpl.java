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
package org.jboss.jca.common.metadata.common.v11;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.v11.ConnDefXaPool;
import org.jboss.jca.common.api.validator.ValidateException;

/**
 * An XA pool implementation for connection definition
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ConnDefXaPoolImpl extends org.jboss.jca.common.metadata.common.CommonXaPoolImpl implements ConnDefXaPool
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** initial-pool-size */
   protected final Integer initialPoolSize;

   /**
    * Constructor
    *
    * @param minPoolSize minPoolSize
    * @param initialPoolSize initialPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param useStrictMin useStrictMin
    * @param flushStrategy flushStrategy
    * @param isSameRmOverride isSameRmOverride
    * @param interleaving interleaving
    * @param padXid padXid
    * @param wrapXaResource wrapXaResource
    * @param noTxSeparatePool noTxSeparatePool
    * @throws ValidateException ValidateException
    */
   public ConnDefXaPoolImpl(Integer minPoolSize, Integer initialPoolSize, Integer maxPoolSize,
                            Boolean prefill, Boolean useStrictMin,
                            FlushStrategy flushStrategy,
                            Boolean isSameRmOverride, Boolean interleaving, 
                            Boolean padXid, Boolean wrapXaResource,
                            Boolean noTxSeparatePool) throws ValidateException
   {
      super(minPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy,
            isSameRmOverride, interleaving, padXid, wrapXaResource, noTxSeparatePool);
      this.initialPoolSize = initialPoolSize;
   }

   /**
    * Get the initial pool size
    * @return the value
    */
   @Override
   public Integer getInitialPoolSize()
   {
      return initialPoolSize;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((initialPoolSize == null) ? 7 : 7 * initialPoolSize.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof ConnDefXaPoolImpl))
         return false;
      ConnDefXaPoolImpl other = (ConnDefXaPoolImpl) obj;
      if (initialPoolSize == null)
      {
         if (other.initialPoolSize != null)
            return false;
      }
      else if (!initialPoolSize.equals(other.initialPoolSize))
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<xa-pool>");

      if (minPoolSize != null)
      {
         sb.append("<").append(ConnDefXaPool.Tag.MIN_POOL_SIZE).append(">");
         sb.append(minPoolSize);
         sb.append("</").append(ConnDefXaPool.Tag.MIN_POOL_SIZE).append(">");
      }

      if (initialPoolSize != null)
      {
         sb.append("<").append(ConnDefXaPool.Tag.INITIAL_POOL_SIZE).append(">");
         sb.append(initialPoolSize);
         sb.append("</").append(ConnDefXaPool.Tag.INITIAL_POOL_SIZE).append(">");
      }

      if (maxPoolSize != null)
      {
         sb.append("<").append(ConnDefXaPool.Tag.MAX_POOL_SIZE).append(">");
         sb.append(maxPoolSize);
         sb.append("</").append(ConnDefXaPool.Tag.MAX_POOL_SIZE).append(">");
      }

      if (prefill != null)
      {
         sb.append("<").append(ConnDefXaPool.Tag.PREFILL).append(">");
         sb.append(prefill);
         sb.append("</").append(ConnDefXaPool.Tag.PREFILL).append(">");
      }

      if (useStrictMin != null)
      {
         sb.append("<").append(ConnDefXaPool.Tag.USE_STRICT_MIN).append(">");
         sb.append(useStrictMin);
         sb.append("</").append(ConnDefXaPool.Tag.USE_STRICT_MIN).append(">");
      }

      if (flushStrategy != null)
      {
         sb.append("<").append(ConnDefXaPool.Tag.FLUSH_STRATEGY).append(">");
         sb.append(flushStrategy);
         sb.append("</").append(ConnDefXaPool.Tag.FLUSH_STRATEGY).append(">");
      }

      if (isSameRmOverride != null)
      {
         sb.append("<").append(ConnDefXaPool.Tag.IS_SAME_RM_OVERRIDE).append(">");
         sb.append(isSameRmOverride);
         sb.append("</").append(ConnDefXaPool.Tag.IS_SAME_RM_OVERRIDE).append(">");
      }

      if (interleaving != null && Boolean.TRUE.equals(interleaving))
      {
         sb.append("<").append(ConnDefXaPool.Tag.INTERLEAVING).append("/>");
      }

      if (noTxSeparatePool != null && Boolean.TRUE.equals(noTxSeparatePool))
      {
         sb.append("<").append(ConnDefXaPool.Tag.NO_TX_SEPARATE_POOLS).append("/>");
      }

      if (padXid != null)
      {
         sb.append("<").append(ConnDefXaPool.Tag.PAD_XID).append(">");
         sb.append(padXid);
         sb.append("</").append(ConnDefXaPool.Tag.PAD_XID).append(">");
      }

      if (wrapXaResource != null)
      {
         sb.append("<").append(ConnDefXaPool.Tag.WRAP_XA_RESOURCE).append(">");
         sb.append(wrapXaResource);
         sb.append("</").append(ConnDefXaPool.Tag.WRAP_XA_RESOURCE).append(">");
      }

      sb.append("</xa-pool>");
      
      return sb.toString();
   }
}

