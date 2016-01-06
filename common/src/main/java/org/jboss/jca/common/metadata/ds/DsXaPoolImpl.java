/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.Capacity;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.ds.DsXaPool;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.Iterator;
import java.util.Map;

/**
 * An XA pool implementation
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DsXaPoolImpl extends org.jboss.jca.common.metadata.common.XaPoolImpl implements DsXaPool
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** allow-multiple-users */
   protected final Boolean allowMultipleUsers;

   /**
    * connection-listener
    */
   protected final Extension connectionListener;

   /**
    * Create a new XaPoolImpl.
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
    * @param allowMultipleUsers allowMultipleUsers
    * @param capacity capacity
    * @param fair fair
    * @param connectionListener connectionListener
    * @throws ValidateException ValidateException
    */
   public DsXaPoolImpl(Integer minPoolSize, Integer initialPoolSize, Integer maxPoolSize,
                       Boolean prefill, Boolean useStrictMin,
                       FlushStrategy flushStrategy,
                       Boolean isSameRmOverride, Boolean interleaving, 
                       Boolean padXid, Boolean wrapXaResource,
                       Boolean noTxSeparatePool,
                       Boolean allowMultipleUsers,
                       Capacity capacity, Boolean fair, Extension connectionListener) throws ValidateException
   {
      super(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy, capacity, fair,
            isSameRmOverride, interleaving, padXid, wrapXaResource, noTxSeparatePool);

      this.allowMultipleUsers = allowMultipleUsers;
      this.connectionListener = connectionListener;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public final Boolean isAllowMultipleUsers()
   {
      return allowMultipleUsers;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Extension getConnectionListener()
   {
      return connectionListener;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((allowMultipleUsers == null) ? 0 : allowMultipleUsers.hashCode());
      result = prime * result + ((connectionListener == null) ? 7 : 7 * connectionListener.hashCode());
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
      if (connectionListener == null)
      {
         if (other.connectionListener != null)
            return false;
      }
      else if (!connectionListener.equals(other.connectionListener))
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

      if (initialPoolSize != null)
      {
         sb.append("<").append(DsXaPool.Tag.INITIAL_POOL_SIZE).append(">");
         sb.append(initialPoolSize);
         sb.append("</").append(DsXaPool.Tag.INITIAL_POOL_SIZE).append(">");
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

      if (fair != null && !fair.equals(Defaults.FAIR))
      {
         sb.append("<").append(DsXaPool.Tag.FAIR).append(">");
         sb.append(fair);
         sb.append("</").append(DsXaPool.Tag.FAIR).append(">");
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

      if (capacity != null)
         sb.append(capacity);

      if (connectionListener != null)
      {
         sb.append("<").append(DsXaPool.Tag.CONNECTION_LISTENER);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(connectionListener.getClassName()).append("\"");
         sb.append(">");

         if (connectionListener.getConfigPropertiesMap().size() > 0)
         {
            Iterator<Map.Entry<String, String>> it = connectionListener.getConfigPropertiesMap().entrySet().iterator();
            
            while (it.hasNext())
            {
               Map.Entry<String, String> entry = it.next();

               sb.append("<").append(Extension.Tag.CONFIG_PROPERTY);
               sb.append(" name=\"").append(entry.getKey()).append("\">");
               sb.append(entry.getValue());
               sb.append("</").append(Extension.Tag.CONFIG_PROPERTY).append(">");
            }
         }

         sb.append("</").append(DsXaPool.Tag.CONNECTION_LISTENER).append(">");
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
