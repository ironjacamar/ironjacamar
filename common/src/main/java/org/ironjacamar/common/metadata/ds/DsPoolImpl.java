/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.metadata.ds;

import org.ironjacamar.common.api.metadata.Defaults;
import org.ironjacamar.common.api.metadata.common.Capacity;
import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.common.api.metadata.ds.DsPool;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Iterator;
import java.util.Map;

/**
 * A pool implementation
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DsPoolImpl extends org.ironjacamar.common.metadata.common.PoolImpl implements DsPool
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /**
    * allow-multiple-users
    */
   protected Boolean allowMultipleUsers;

   /**
    * connection-listener
    */
   protected Extension connectionListener;

   /**
    * Create a new PoolImpl.
    * @param type type
    * @param minPoolSize minPoolSize
    * @param initialPoolSize initialPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param useStrictMin useStrictMin
    * @param flushStrategy flushStrategy
    * @param allowMultipleUsers allowMultipleUsers
    * @param capacity capacity
    * @param connectionListener connectionListener
    * @param expressions expressions
    * @throws ValidateException ValidateException
    */
   public DsPoolImpl(String type, Integer minPoolSize, Integer initialPoolSize, Integer maxPoolSize, 
                     Boolean prefill, Boolean useStrictMin,
                     FlushStrategy flushStrategy, Boolean allowMultipleUsers,
                     Capacity capacity, Extension connectionListener,
                     Map<String, String> expressions)
      throws ValidateException
   {
      super(type, minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin,
            flushStrategy, capacity, expressions);
      this.allowMultipleUsers = allowMultipleUsers;
      this.connectionListener = connectionListener;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isAllowMultipleUsers()
   {
      return allowMultipleUsers;
   }

   /**
    * {@inheritDoc}
    */
   public Extension getConnectionListener()
   {
      return connectionListener;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((allowMultipleUsers == null) ? 0 : allowMultipleUsers.hashCode());
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
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<pool");

      if (type != null)
      {
         sb.append(" ").append(XML.ATTRIBUTE_TYPE).append("=\"");
         sb.append(type);
         sb.append("\"");
      }

      sb.append(">");

      if (minPoolSize != null && !Defaults.MIN_POOL_SIZE.equals(minPoolSize))
      {
         sb.append("<").append(XML.ELEMENT_MIN_POOL_SIZE).append(">");
         sb.append(minPoolSize);
         sb.append("</").append(XML.ELEMENT_MIN_POOL_SIZE).append(">");
      }

      if (initialPoolSize != null)
      {
         sb.append("<").append(XML.ELEMENT_INITIAL_POOL_SIZE).append(">");
         sb.append(initialPoolSize);
         sb.append("</").append(XML.ELEMENT_INITIAL_POOL_SIZE).append(">");
      }

      if (maxPoolSize != null && !Defaults.MAX_POOL_SIZE.equals(maxPoolSize))
      {
         sb.append("<").append(XML.ELEMENT_MAX_POOL_SIZE).append(">");
         sb.append(maxPoolSize);
         sb.append("</").append(XML.ELEMENT_MAX_POOL_SIZE).append(">");
      }

      if (prefill != null && !Defaults.PREFILL.equals(prefill))
      {
         sb.append("<").append(XML.ELEMENT_PREFILL).append(">");
         sb.append(prefill);
         sb.append("</").append(XML.ELEMENT_PREFILL).append(">");
      }

      if (useStrictMin != null && !Defaults.USE_STRICT_MIN.equals(useStrictMin))
      {
         sb.append("<").append(XML.ELEMENT_USE_STRICT_MIN).append(">");
         sb.append(useStrictMin);
         sb.append("</").append(XML.ELEMENT_USE_STRICT_MIN).append(">");
      }

      if (flushStrategy != null && !Defaults.FLUSH_STRATEGY.equals(flushStrategy))
      {
         sb.append("<").append(XML.ELEMENT_FLUSH_STRATEGY).append(">");
         sb.append(flushStrategy);
         sb.append("</").append(XML.ELEMENT_FLUSH_STRATEGY).append(">");
      }

      if (allowMultipleUsers != null && allowMultipleUsers.booleanValue())
      {
         sb.append("<").append(XML.ELEMENT_ALLOW_MULTIPLE_USERS).append("/>");
      }

      if (capacity != null)
         sb.append(capacity);

      if (connectionListener != null)
      {
         sb.append("<").append(XML.ELEMENT_CONNECTION_LISTENER);
         sb.append(" ").append(XML.ATTRIBUTE_CLASS_NAME).append("=\"");
         sb.append(connectionListener.getClassName()).append("\"");
         sb.append(">");

         if (connectionListener.getConfigPropertiesMap().size() > 0)
         {
            Iterator<Map.Entry<String, String>> it = connectionListener.getConfigPropertiesMap().entrySet().iterator();
            
            while (it.hasNext())
            {
               Map.Entry<String, String> entry = it.next();

               sb.append("<").append(XML.ELEMENT_CONFIG_PROPERTY);
               sb.append(" name=\"").append(entry.getKey()).append("\">");
               sb.append(entry.getValue());
               sb.append("</").append(XML.ELEMENT_CONFIG_PROPERTY).append(">");
            }
         }

         sb.append("</").append(XML.ELEMENT_CONNECTION_LISTENER).append(">");
      }

      sb.append("</pool>");
      
      return sb.toString();
   }
}
