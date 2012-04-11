/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.metadata.ds.v11;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.ds.v11.DsXaPool;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.common.CommonXaPoolImpl;

/**
 * An XA pool implementation
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class DsXaPoolImpl extends CommonXaPoolImpl implements DsXaPool
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 3261597366235425250L;

   private final Boolean allowMultipleUsers;

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
    * @param allowMultipleUsers allowMultipleUsers
    * @throws ValidateException ValidateException
    */
   public DsXaPoolImpl(Integer minPoolSize, Integer maxPoolSize,
                       Boolean prefill, Boolean useStrictMin,
                       FlushStrategy flushStrategy,
                       Boolean isSameRmOverride, Boolean interleaving, 
                       Boolean padXid, Boolean wrapXaResource,
                       Boolean noTxSeparatePool,
                       Boolean allowMultipleUsers) throws ValidateException
   {
      super(minPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy,
            isSameRmOverride, interleaving, padXid, wrapXaResource, noTxSeparatePool);

      this.allowMultipleUsers = allowMultipleUsers;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public final Boolean isAllowMultipleUsers()
   {
      return interleaving;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((allowMultipleUsers == null) ? 0 : allowMultipleUsers.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof DsXaPoolImpl))
         return false;
      if (!super.equals(obj))
         return false;

      DsXaPoolImpl other = (DsXaPoolImpl) obj;
      if (allowMultipleUsers == null)
      {
         if (other.allowMultipleUsers != null)
            return false;
      }
      else if (!allowMultipleUsers.equals(other.allowMultipleUsers))
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
         sb.append("<").append(DsXaPool.Tag.MIN_POOL_SIZE).append(">");
         sb.append(minPoolSize);
         sb.append("</").append(DsXaPool.Tag.MIN_POOL_SIZE).append(">");
      }

      if (maxPoolSize != null)
      {
         sb.append("<").append(DsXaPool.Tag.MAX_POOL_SIZE).append(">");
         sb.append(maxPoolSize);
         sb.append("</").append(DsXaPool.Tag.MAX_POOL_SIZE).append(">");
      }

      if (prefill != null)
      {
         sb.append("<").append(DsXaPool.Tag.PREFILL).append(">");
         sb.append(prefill);
         sb.append("</").append(DsXaPool.Tag.PREFILL).append(">");
      }

      if (useStrictMin != null)
      {
         sb.append("<").append(DsXaPool.Tag.USE_STRICT_MIN).append(">");
         sb.append(useStrictMin);
         sb.append("</").append(DsXaPool.Tag.USE_STRICT_MIN).append(">");
      }

      if (flushStrategy != null)
      {
         sb.append("<").append(DsXaPool.Tag.FLUSH_STRATEGY).append(">");
         sb.append(flushStrategy);
         sb.append("</").append(DsXaPool.Tag.FLUSH_STRATEGY).append(">");
      }

      if (allowMultipleUsers != null && allowMultipleUsers.booleanValue())
      {
         sb.append("<").append(DsXaPool.Tag.ALLOW_MULTIPLE_USERS).append("/>");
      }

      if (isSameRmOverride != null)
      {
         sb.append("<").append(DsXaPool.Tag.IS_SAME_RM_OVERRIDE).append(">");
         sb.append(isSameRmOverride);
         sb.append("</").append(DsXaPool.Tag.IS_SAME_RM_OVERRIDE).append(">");
      }

      if (interleaving != null && Boolean.TRUE.equals(interleaving))
      {
         sb.append("<").append(DsXaPool.Tag.INTERLEAVING).append("/>");
      }

      if (noTxSeparatePool != null && Boolean.TRUE.equals(noTxSeparatePool))
      {
         sb.append("<").append(DsXaPool.Tag.NO_TX_SEPARATE_POOLS).append("/>");
      }

      if (padXid != null)
      {
         sb.append("<").append(DsXaPool.Tag.PAD_XID).append(">");
         sb.append(padXid);
         sb.append("</").append(DsXaPool.Tag.PAD_XID).append(">");
      }

      if (wrapXaResource != null)
      {
         sb.append("<").append(DsXaPool.Tag.WRAP_XA_RESOURCE).append(">");
         sb.append(wrapXaResource);
         sb.append("</").append(DsXaPool.Tag.WRAP_XA_RESOURCE).append(">");
      }

      sb.append("</xa-pool>");
      
      return sb.toString();
   }
}
