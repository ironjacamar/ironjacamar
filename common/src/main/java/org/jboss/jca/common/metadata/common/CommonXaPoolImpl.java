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

   private final boolean isSameRmOverride;

   private final boolean interleaving;

   private final boolean padXid;

   private final boolean wrapXaDataSource;

   private final boolean noTxSeparatePool;



   /**
    * Create a new XaPoolImpl.
    *
    * @param minPoolSize minPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param useStrictMin useStrictMin
    * @param isSameRmOverride isSameRmOverride
    * @param interleaving interleaving
    * @param padXid padXid
    * @param wrapXaDataSource wrapXaDataSource
    * @param noTxSeparatePool noTxSeparatePool
    */
   public CommonXaPoolImpl(Integer minPoolSize, Integer maxPoolSize, boolean prefill, boolean useStrictMin,
      boolean isSameRmOverride, boolean interleaving, boolean padXid, boolean wrapXaDataSource,
      boolean noTxSeparatePool)
   {
      super(minPoolSize, maxPoolSize, prefill, useStrictMin);
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
   public final boolean isSameRmOverride()
   {
      return isSameRmOverride;
   }

   /**
    * Get the interleaving.
    *
    * @return the interleaving.
    */
   @Override
   public final boolean isInterleaving()
   {
      return interleaving;
   }

   /**
    * Get the padXid.
    *
    * @return the padXid.
    */
   @Override
   public final boolean isPadXid()
   {
      return padXid;
   }

   /**
    * Get the wrapXaDataSource.
    *
    * @return the wrapXaDataSource.
    */
   @Override
   public final boolean isWrapXaDataSource()
   {
      return wrapXaDataSource;
   }

   /**
    * Get the noTxSeparatePool.
    *
    * @return the noTxSeparatePool.
    */
   @Override
   public final boolean isNoTxSeparatePool()
   {
      return noTxSeparatePool;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + (interleaving ? 1231 : 1237);
      result = prime * result + (isSameRmOverride ? 1231 : 1237);
      result = prime * result + (noTxSeparatePool ? 1231 : 1237);
      result = prime * result + (padXid ? 1231 : 1237);
      result = prime * result + (wrapXaDataSource ? 1231 : 1237);
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
      if (interleaving != other.interleaving)
         return false;
      if (isSameRmOverride != other.isSameRmOverride)
         return false;
      if (noTxSeparatePool != other.noTxSeparatePool)
         return false;
      if (padXid != other.padXid)
         return false;
      if (wrapXaDataSource != other.wrapXaDataSource)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "XaPoolImpl [isSameRmOverride=" + isSameRmOverride + ", interleaving=" + interleaving + ", padXid=" +
             padXid + ", wrapXaDataSource=" + wrapXaDataSource + ", noTxSeparatePool=" + noTxSeparatePool +
             ", minPoolSize=" + minPoolSize + ", maxPoolSize=" + maxPoolSize + ", prefill=" + prefill +
             ", useStrictMin=" + useStrictMin + "]";
   }



}

