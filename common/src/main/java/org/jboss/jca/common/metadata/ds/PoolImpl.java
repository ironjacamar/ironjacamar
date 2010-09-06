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
package org.jboss.jca.common.metadata.ds;

import org.jboss.jca.common.api.metadata.ds.Pool;

/**
 *
 * A PoolImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class PoolImpl implements Pool
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -8705723067326455982L;

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
   protected final boolean prefill;

   /**
    * use-strict-min
    */
   protected final boolean useStrictMin;

   /**
    * Create a new PoolImpl.
    *
    * @param minPoolSize minPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param useStrictMin useStrictMin
    */
   public PoolImpl(Integer minPoolSize, Integer maxPoolSize, boolean prefill, boolean useStrictMin)
   {
      super();
      this.minPoolSize = minPoolSize;
      this.maxPoolSize = maxPoolSize;
      this.prefill = prefill;
      this.useStrictMin = useStrictMin;
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
   public final boolean isPrefill()
   {
      return prefill;
   }

   /**
    * Get the useStrictMin.
    *
    * @return the useStrictMin.
    */
   @Override
   public final boolean isUseStrictMin()
   {
      return useStrictMin;
   }

}
