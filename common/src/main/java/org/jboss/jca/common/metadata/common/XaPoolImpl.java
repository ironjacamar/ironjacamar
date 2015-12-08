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

import org.jboss.jca.common.api.metadata.common.Capacity;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.XaPool;
import org.jboss.jca.common.api.validator.ValidateException;

/**
 *
 * A XaPoolImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class XaPoolImpl extends PoolImpl implements XaPool
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** is-same-rm-override */
   protected Boolean isSameRmOverride;

   /** interleaving */
   protected Boolean interleaving;

   /** pad-xid */
   protected Boolean padXid;

   /** wrap-xa-resource */
   protected Boolean wrapXaResource;

   /** no-tx-separate-pool */
   protected Boolean noTxSeparatePool;

   /**
    * Create a new XaPoolImpl.
    *
    * @param minPoolSize minPoolSize
    * @param initialPoolSize initialPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param useStrictMin useStrictMin
    * @param flushStrategy flushStrategy
    * @param capacity capacity
    * @param isSameRmOverride isSameRmOverride
    * @param interleaving interleaving
    * @param padXid padXid
    * @param wrapXaResource wrapXaResource
    * @param noTxSeparatePool noTxSeparatePool
    * @param fair fair
    * @throws ValidateException ValidateException
    */
   public XaPoolImpl(Integer minPoolSize, Integer initialPoolSize, Integer maxPoolSize,
                     Boolean prefill, Boolean useStrictMin,
                     FlushStrategy flushStrategy, Capacity capacity,
                     Boolean fair, Boolean isSameRmOverride, Boolean interleaving,
                     Boolean padXid, Boolean wrapXaResource,
                     Boolean noTxSeparatePool) throws ValidateException
   {
      super(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy, capacity, fair);
      this.isSameRmOverride = isSameRmOverride;
      this.interleaving = interleaving;
      this.padXid = padXid;
      this.wrapXaResource = wrapXaResource;
      this.noTxSeparatePool = noTxSeparatePool;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isSameRmOverride()
   {
      return isSameRmOverride;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isInterleaving()
   {
      return interleaving;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isPadXid()
   {
      return padXid;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isWrapXaResource()
   {
      return wrapXaResource;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isNoTxSeparatePool()
   {
      return noTxSeparatePool;
   }

   /**
    * {@inheritDoc}
    */
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

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof XaPoolImpl))
         return false;
      XaPoolImpl other = (XaPoolImpl) obj;
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
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<xa-pool>");

      if (minPoolSize != null)
      {
         sb.append("<").append(XaPool.Tag.MIN_POOL_SIZE).append(">");
         sb.append(minPoolSize);
         sb.append("</").append(XaPool.Tag.MIN_POOL_SIZE).append(">");
      }

      if (initialPoolSize != null)
      {
         sb.append("<").append(XaPool.Tag.INITIAL_POOL_SIZE).append(">");
         sb.append(initialPoolSize);
         sb.append("</").append(XaPool.Tag.INITIAL_POOL_SIZE).append(">");
      }

      if (maxPoolSize != null)
      {
         sb.append("<").append(XaPool.Tag.MAX_POOL_SIZE).append(">");
         sb.append(maxPoolSize);
         sb.append("</").append(XaPool.Tag.MAX_POOL_SIZE).append(">");
      }

      if (prefill != null)
      {
         sb.append("<").append(XaPool.Tag.PREFILL).append(">");
         sb.append(prefill);
         sb.append("</").append(XaPool.Tag.PREFILL).append(">");
      }

      if (fair != null)
      {
         sb.append("<").append(XaPool.Tag.FAIR).append(">");
         sb.append(fair);
         sb.append("</").append(XaPool.Tag.FAIR).append(">");
      }

      if (useStrictMin != null)
      {
         sb.append("<").append(XaPool.Tag.USE_STRICT_MIN).append(">");
         sb.append(useStrictMin);
         sb.append("</").append(XaPool.Tag.USE_STRICT_MIN).append(">");
      }

      if (flushStrategy != null)
      {
         sb.append("<").append(XaPool.Tag.FLUSH_STRATEGY).append(">");
         sb.append(flushStrategy);
         sb.append("</").append(XaPool.Tag.FLUSH_STRATEGY).append(">");
      }

      if (capacity != null)
      {
         sb.append("<").append(XaPool.Tag.CAPACITY).append(">");
         sb.append(capacity);
         sb.append("</").append(XaPool.Tag.CAPACITY).append(">");
      }

      if (isSameRmOverride != null)
      {
         sb.append("<").append(XaPool.Tag.IS_SAME_RM_OVERRIDE).append(">");
         sb.append(isSameRmOverride);
         sb.append("</").append(XaPool.Tag.IS_SAME_RM_OVERRIDE).append(">");
      }

      if (interleaving != null && Boolean.TRUE.equals(interleaving))
      {
         sb.append("<").append(XaPool.Tag.INTERLEAVING).append("/>");
      }

      if (noTxSeparatePool != null && Boolean.TRUE.equals(noTxSeparatePool))
      {
         sb.append("<").append(XaPool.Tag.NO_TX_SEPARATE_POOLS).append("/>");
      }

      if (padXid != null)
      {
         sb.append("<").append(XaPool.Tag.PAD_XID).append(">");
         sb.append(padXid);
         sb.append("</").append(XaPool.Tag.PAD_XID).append(">");
      }

      if (wrapXaResource != null)
      {
         sb.append("<").append(XaPool.Tag.WRAP_XA_RESOURCE).append(">");
         sb.append(wrapXaResource);
         sb.append("</").append(XaPool.Tag.WRAP_XA_RESOURCE).append(">");
      }

      sb.append("</xa-pool>");
      
      return sb.toString();
   }
}

