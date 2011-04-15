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

   private final Boolean isSameRmOverride;

   private final Boolean interleaving;

   private final Boolean padXid;

   private final Boolean wrapXaDataSource;

   private final Boolean noTxSeparatePool;


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
    * @param wrapXaDataSource wrapXaDataSource
    * @param noTxSeparatePool noTxSeparatePool
    * @throws ValidateException ValidateException
    */
   public CommonXaPoolImpl(Integer minPoolSize, Integer maxPoolSize,
                           Boolean prefill, Boolean useStrictMin,
                           FlushStrategy flushStrategy,
                           Boolean isSameRmOverride, Boolean interleaving, 
                           Boolean padXid, Boolean wrapXaDataSource,
                           Boolean noTxSeparatePool) throws ValidateException
   {
      super(minPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy);
      this.isSameRmOverride = isSameRmOverride;
      this.interleaving = interleaving;
      this.padXid = padXid;
      this.wrapXaDataSource = wrapXaDataSource;
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
    * Get the wrapXaDataSource.
    *
    * @return the wrapXaDataSource.
    */
   @Override
   public final Boolean isWrapXaDataSource()
   {
      return wrapXaDataSource;
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
      int result = 1;
      result = prime * result + ((interleaving == null) ? 0 : interleaving.hashCode());
      result = prime * result + ((isSameRmOverride == null) ? 0 : isSameRmOverride.hashCode());
      result = prime * result + ((noTxSeparatePool == null) ? 0 : noTxSeparatePool.hashCode());
      result = prime * result + ((padXid == null) ? 0 : padXid.hashCode());
      result = prime * result + ((wrapXaDataSource == null) ? 0 : wrapXaDataSource.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
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
      if (wrapXaDataSource == null)
      {
         if (other.wrapXaDataSource != null)
            return false;
      }
      else if (!wrapXaDataSource.equals(other.wrapXaDataSource))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "CommonXaPoolImpl [isSameRmOverride=" + isSameRmOverride + ", interleaving=" + interleaving +
             ", padXid=" + padXid + ", wrapXaDataSource=" + wrapXaDataSource + ", noTxSeparatePool=" +
             noTxSeparatePool + "]";
   }
}

