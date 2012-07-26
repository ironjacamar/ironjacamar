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

import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.validator.ValidateException;

/**
 *
 * A XaPoolImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class CommonXaPoolImpl extends CommonPoolImpl implements CommonXaPool
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 3261597366235425250L;

   /** is-same-rm-override */
   protected final Boolean isSameRmOverride;

   /** interleaving */
   protected final Boolean interleaving;

   /** pad-xid */
   protected final Boolean padXid;

   /** wrap-xa-resource */
   protected final Boolean wrapXaResource;

   /** no-tx-separate-pool */
   protected final Boolean noTxSeparatePool;

   /**
    * Create a new XaPoolImpl.
    *
    * @param minPoolSize minPoolSize
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
   public CommonXaPoolImpl(Integer minPoolSize, Integer maxPoolSize,
                           Boolean prefill, Boolean useStrictMin,
                           FlushStrategy flushStrategy,
                           Boolean isSameRmOverride, Boolean interleaving, 
                           Boolean padXid, Boolean wrapXaResource,
                           Boolean noTxSeparatePool) throws ValidateException
   {
      super(minPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy);
      this.isSameRmOverride = isSameRmOverride;
      this.interleaving = interleaving;
      this.padXid = padXid;
      this.wrapXaResource = wrapXaResource;
      this.noTxSeparatePool = noTxSeparatePool;
   }

   /**
    * Get the isSameRmOverride.
    *
    * @return the isSameRmOverride.
    */
   @Override
   public final Boolean isSameRmOverride()
   {
      return isSameRmOverride;
   }

   /**
    * Get the interleaving.
    *
    * @return the interleaving.
    */
   @Override
   public final Boolean isInterleaving()
   {
      return interleaving;
   }

   /**
    * Get the padXid.
    *
    * @return the padXid.
    */
   @Override
   public final Boolean isPadXid()
   {
      return padXid;
   }

   /**
    * Get the wrapXaResource.
    *
    * @return the wrapXaResource.
    */
   @Override
   public final Boolean isWrapXaResource()
   {
      return wrapXaResource;
   }

   /**
    * Get the noTxSeparatePool.
    *
    * @return the noTxSeparatePool.
    */
   @Override
   public final Boolean isNoTxSeparatePool()
   {
      return noTxSeparatePool;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((interleaving == null) ? 0 : interleaving.hashCode());
      result = prime * result + ((isSameRmOverride == null) ? 0 : isSameRmOverride.hashCode());
      result = prime * result + ((noTxSeparatePool == null) ? 0 : noTxSeparatePool.hashCode());
      result = prime * result + ((padXid == null) ? 0 : padXid.hashCode());
      result = prime * result + ((wrapXaResource == null) ? 0 : wrapXaResource.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof CommonXaPoolImpl))
         return false;
      CommonXaPoolImpl other = (CommonXaPoolImpl) obj;
      if (interleaving == null)
      {
         if (other.interleaving != null)
            return false;
      }
      else if (!interleaving.equals(other.interleaving))
         return false;
      if (isSameRmOverride == null)
      {
         if (other.isSameRmOverride != null)
            return false;
      }
      else if (!isSameRmOverride.equals(other.isSameRmOverride))
         return false;
      if (noTxSeparatePool == null)
      {
         if (other.noTxSeparatePool != null)
            return false;
      }
      else if (!noTxSeparatePool.equals(other.noTxSeparatePool))
         return false;
      if (padXid == null)
      {
         if (other.padXid != null)
            return false;
      }
      else if (!padXid.equals(other.padXid))
         return false;
      if (wrapXaResource == null)
      {
         if (other.wrapXaResource != null)
            return false;
      }
      else if (!wrapXaResource.equals(other.wrapXaResource))
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
         sb.append("<").append(CommonXaPool.Tag.MIN_POOL_SIZE).append(">");
         sb.append(minPoolSize);
         sb.append("</").append(CommonXaPool.Tag.MIN_POOL_SIZE).append(">");
      }

      if (maxPoolSize != null)
      {
         sb.append("<").append(CommonXaPool.Tag.MAX_POOL_SIZE).append(">");
         sb.append(maxPoolSize);
         sb.append("</").append(CommonXaPool.Tag.MAX_POOL_SIZE).append(">");
      }

      if (prefill != null)
      {
         sb.append("<").append(CommonXaPool.Tag.PREFILL).append(">");
         sb.append(prefill);
         sb.append("</").append(CommonXaPool.Tag.PREFILL).append(">");
      }

      if (useStrictMin != null)
      {
         sb.append("<").append(CommonXaPool.Tag.USE_STRICT_MIN).append(">");
         sb.append(useStrictMin);
         sb.append("</").append(CommonXaPool.Tag.USE_STRICT_MIN).append(">");
      }

      if (flushStrategy != null)
      {
         sb.append("<").append(CommonXaPool.Tag.FLUSH_STRATEGY).append(">");
         sb.append(flushStrategy);
         sb.append("</").append(CommonXaPool.Tag.FLUSH_STRATEGY).append(">");
      }

      if (isSameRmOverride != null)
      {
         sb.append("<").append(CommonXaPool.Tag.IS_SAME_RM_OVERRIDE).append(">");
         sb.append(isSameRmOverride);
         sb.append("</").append(CommonXaPool.Tag.IS_SAME_RM_OVERRIDE).append(">");
      }

      if (interleaving != null && Boolean.TRUE.equals(interleaving))
      {
         sb.append("<").append(CommonXaPool.Tag.INTERLEAVING).append("/>");
      }

      if (noTxSeparatePool != null && Boolean.TRUE.equals(noTxSeparatePool))
      {
         sb.append("<").append(CommonXaPool.Tag.NO_TX_SEPARATE_POOLS).append("/>");
      }

      if (padXid != null)
      {
         sb.append("<").append(CommonXaPool.Tag.PAD_XID).append(">");
         sb.append(padXid);
         sb.append("</").append(CommonXaPool.Tag.PAD_XID).append(">");
      }

      if (wrapXaResource != null)
      {
         sb.append("<").append(CommonXaPool.Tag.WRAP_XA_RESOURCE).append(">");
         sb.append(wrapXaResource);
         sb.append("</").append(CommonXaPool.Tag.WRAP_XA_RESOURCE).append(">");
      }

      sb.append("</xa-pool>");
      
      return sb.toString();
   }
}

