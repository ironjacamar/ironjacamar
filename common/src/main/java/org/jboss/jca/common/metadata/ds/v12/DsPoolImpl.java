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
package org.jboss.jca.common.metadata.ds.v12;

import org.jboss.jca.common.api.metadata.common.Capacity;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.ds.v12.DsPool;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.Iterator;
import java.util.Map;

/**
 * A pool implementation
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class DsPoolImpl extends org.jboss.jca.common.metadata.ds.v11.DsPoolImpl implements DsPool
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -8705723067326455982L;

   /**
    * initial-pool-size
    */
   protected final Integer initialPoolSize;

   /**
    * capacity
    */
   protected final Capacity capacity;

   /**
    * connection-listener
    */
   protected final Extension connectionListener;

   /**
    * Create a new PoolImpl.
    *
    * @param minPoolSize minPoolSize
    * @param initialPoolSize initialPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param useStrictMin useStrictMin
    * @param flushStrategy flushStrategy
    * @param allowMultipleUsers allowMultipleUsers
    * @param capacity capacity
    * @param connectionListener connectionListener
    * @throws ValidateException ValidateException
    */
   public DsPoolImpl(Integer minPoolSize, Integer initialPoolSize, Integer maxPoolSize, 
                     Boolean prefill, Boolean useStrictMin,
                     FlushStrategy flushStrategy, Boolean allowMultipleUsers,
                     Capacity capacity, Extension connectionListener)
      throws ValidateException
   {
      super(minPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy, allowMultipleUsers);
      this.initialPoolSize = initialPoolSize;
      this.capacity = capacity;
      this.connectionListener = connectionListener;
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

   /**
    * {@inheritDoc}
    */
   @Override
   public Extension getConnectionListener()
   {
      return connectionListener;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((initialPoolSize == null) ? 7 : 7 * initialPoolSize.hashCode());
      result = prime * result + ((capacity == null) ? 7 : 7 * capacity.hashCode());
      result = prime * result + ((connectionListener == null) ? 7 : 7 * connectionListener.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof DsPoolImpl))
         return false;
      if (!super.equals(obj))
         return false;

      DsPoolImpl other = (DsPoolImpl) obj;
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
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<pool>");

      if (minPoolSize != null)
      {
         sb.append("<").append(DsPool.Tag.MIN_POOL_SIZE).append(">");
         sb.append(minPoolSize);
         sb.append("</").append(DsPool.Tag.MIN_POOL_SIZE).append(">");
      }

      if (initialPoolSize != null)
      {
         sb.append("<").append(DsPool.Tag.INITIAL_POOL_SIZE).append(">");
         sb.append(initialPoolSize);
         sb.append("</").append(DsPool.Tag.INITIAL_POOL_SIZE).append(">");
      }

      if (maxPoolSize != null)
      {
         sb.append("<").append(DsPool.Tag.MAX_POOL_SIZE).append(">");
         sb.append(maxPoolSize);
         sb.append("</").append(DsPool.Tag.MAX_POOL_SIZE).append(">");
      }

      if (prefill != null)
      {
         sb.append("<").append(DsPool.Tag.PREFILL).append(">");
         sb.append(prefill);
         sb.append("</").append(DsPool.Tag.PREFILL).append(">");
      }

      if (useStrictMin != null)
      {
         sb.append("<").append(DsPool.Tag.USE_STRICT_MIN).append(">");
         sb.append(useStrictMin);
         sb.append("</").append(DsPool.Tag.USE_STRICT_MIN).append(">");
      }

      if (flushStrategy != null)
      {
         sb.append("<").append(DsPool.Tag.FLUSH_STRATEGY).append(">");
         sb.append(flushStrategy);
         sb.append("</").append(DsPool.Tag.FLUSH_STRATEGY).append(">");
      }

      if (allowMultipleUsers != null && allowMultipleUsers.booleanValue())
      {
         sb.append("<").append(DsPool.Tag.ALLOW_MULTIPLE_USERS).append("/>");
      }

      if (capacity != null)
         sb.append(capacity);

      if (connectionListener != null)
      {
         sb.append("<").append(DsPool.Tag.CONNECTION_LISTENER);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(connectionListener.getClassName()).append("\"");
         sb.append(">");

         if (connectionListener.getConfigPropertiesMap() != null &&
             connectionListener.getConfigPropertiesMap().size() > 0)
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

         sb.append("</").append(DsPool.Tag.CONNECTION_LISTENER).append(">");
      }

      sb.append("</pool>");
      
      return sb.toString();
   }
}
