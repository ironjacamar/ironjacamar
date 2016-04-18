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
package org.ironjacamar.common.metadata.common;

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.Defaults;
import org.ironjacamar.common.api.metadata.common.Capacity;
import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.common.api.metadata.common.Pool;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Map;

import org.jboss.logging.Messages;

/**
 *
 * A PoolImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class PoolImpl extends AbstractMetadata implements Pool
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
    * type
    */
   protected String type;

   /**
    * janitor
    */
   protected String janitor;

   /**
    * minPoolSize
    */
   protected Integer minPoolSize;

   /**
    * initial-pool-size
    */
   protected Integer initialPoolSize;

   /**
    * maxPoolSize
    */
   protected Integer maxPoolSize;

   /**
    * prefill
    */
   protected Boolean prefill;

   /**
    * flush-strategy
    */
   protected FlushStrategy flushStrategy;

   /**
    * capacity
    */
   protected Capacity capacity;

   /**
    * Constructor
    *
    * @param type type
    * @param janitor janitor
    * @param minPoolSize minPoolSize
    * @param initialPoolSize initialPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param flushStrategy flushStrategy
    * @param capacity capacity
    * @param expressions expressions
    * @throws ValidateException ValidateException
    */
   public PoolImpl(String type, String janitor, Integer minPoolSize, Integer initialPoolSize, Integer maxPoolSize,
                   Boolean prefill,
                   FlushStrategy flushStrategy, Capacity capacity,
                   Map<String, String> expressions)
      throws ValidateException
   {
      super(expressions);
      this.type = type;
      this.janitor = janitor;
      this.minPoolSize = minPoolSize;
      this.initialPoolSize = initialPoolSize;
      this.maxPoolSize = maxPoolSize;
      this.prefill = prefill;
      this.flushStrategy = flushStrategy;
      this.capacity = capacity;
      this.validate();
   }

   /**
    * {@inheritDoc}
    */
   public String getType()
   {
      return type;
   }

   /**
    * {@inheritDoc}
    */
   public String getJanitor()
   {
      return janitor;
   }

   /**
    * {@inheritDoc}
    */
   public Integer getMinPoolSize()
   {
      return minPoolSize;
   }

   /**
    * {@inheritDoc}
    */
   public Integer getInitialPoolSize()
   {
      return initialPoolSize;
   }

   /**
    * {@inheritDoc}
    */
   public Integer getMaxPoolSize()
   {
      return maxPoolSize;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isPrefill()
   {
      return prefill;
   }

   /**
    * {@inheritDoc}
    */
   public FlushStrategy getFlushStrategy()
   {
      return flushStrategy;
   }

   /**
    * {@inheritDoc}
    */
   public Capacity getCapacity()
   {
      return capacity;
   }

   /**
    * {@inheritDoc}
    */
   public void validate() throws ValidateException
   {
      if (this.maxPoolSize != null && this.maxPoolSize < 0)
         throw new ValidateException(bundle.invalidNegative(CommonXML.ELEMENT_MAX_POOL_SIZE));

      if (this.minPoolSize != null && this.minPoolSize < 0)
         throw new ValidateException(bundle.invalidNegative(CommonXML.ELEMENT_MIN_POOL_SIZE));

      if (this.minPoolSize != null && this.maxPoolSize != null && minPoolSize.intValue() > maxPoolSize.intValue())
      {
         throw new ValidateException(bundle.notValidNumber(minPoolSize.toString(),
                 CommonXML.ELEMENT_MIN_POOL_SIZE));
      }

      if (this.flushStrategy == null)
         throw new ValidateException(bundle.nullValue(CommonXML.ELEMENT_FLUSH_STRATEGY));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      result = prime * result + ((janitor == null) ? 0 : janitor.hashCode());
      result = prime * result + ((minPoolSize == null) ? 0 : minPoolSize.hashCode());
      result = prime * result + ((initialPoolSize == null) ? 0 : initialPoolSize.hashCode());
      result = prime * result + ((maxPoolSize == null) ? 0 : maxPoolSize.hashCode());
      result = prime * result + ((prefill == null) ? 0 : prefill.hashCode());
      result = prime * result + ((flushStrategy == null) ? 0 : flushStrategy.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof PoolImpl))
         return false;
      PoolImpl other = (PoolImpl) obj;
      if (type == null)
      {
         if (other.type != null)
            return false;
      }
      else if (!type.equals(other.type))
         return false;
      if (janitor == null)
      {
         if (other.janitor != null)
            return false;
      }
      else if (!janitor.equals(other.janitor))
         return false;
      if (minPoolSize == null)
      {
         if (other.minPoolSize != null)
            return false;
      }
      else if (!minPoolSize.equals(other.minPoolSize))
         return false;
      if (initialPoolSize == null)
      {
         if (other.initialPoolSize != null)
            return false;
      }
      else if (!initialPoolSize.equals(other.initialPoolSize))
         return false;
      if (maxPoolSize == null)
      {
         if (other.maxPoolSize != null)
            return false;
      }
      else if (!maxPoolSize.equals(other.maxPoolSize))
         return false;
      if (prefill == null)
      {
         if (other.prefill != null)
            return false;
      }
      else if (!prefill.equals(other.prefill))
         return false;
      if (flushStrategy == null)
      {
         if (other.flushStrategy != null)
            return false;
      }
      else if (!flushStrategy.equals(other.flushStrategy))
         return false;
      if (capacity == null)
      {
         if (other.capacity != null)
            return false;
      }
      else if (!capacity.equals(other.capacity))
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
         sb.append(" ").append(CommonXML.ATTRIBUTE_TYPE).append("=\"");
         sb.append(type);
         sb.append("\"");
      }

      if (type != null)
      {
         sb.append(" ").append(CommonXML.ATTRIBUTE_JANITOR).append("=\"");
         sb.append(janitor);
         sb.append("\"");
      }

      sb.append(">");

      if (minPoolSize != null && !Defaults.MIN_POOL_SIZE.equals(minPoolSize))
      {
         sb.append("<").append(CommonXML.ELEMENT_MIN_POOL_SIZE).append(">");
         sb.append(minPoolSize);
         sb.append("</").append(CommonXML.ELEMENT_MIN_POOL_SIZE).append(">");
      }

      if (initialPoolSize != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_INITIAL_POOL_SIZE).append(">");
         sb.append(initialPoolSize);
         sb.append("</").append(CommonXML.ELEMENT_INITIAL_POOL_SIZE).append(">");
      }

      if (maxPoolSize != null && !Defaults.MAX_POOL_SIZE.equals(maxPoolSize))
      {
         sb.append("<").append(CommonXML.ELEMENT_MAX_POOL_SIZE).append(">");
         sb.append(maxPoolSize);
         sb.append("</").append(CommonXML.ELEMENT_MAX_POOL_SIZE).append(">");
      }

      if (prefill != null && !Defaults.PREFILL.equals(prefill))
      {
         sb.append("<").append(CommonXML.ELEMENT_PREFILL).append(">");
         sb.append(prefill);
         sb.append("</").append(CommonXML.ELEMENT_PREFILL).append(">");
      }

      if (flushStrategy != null && !Defaults.FLUSH_STRATEGY.equals(flushStrategy))
      {
         sb.append("<").append(CommonXML.ELEMENT_FLUSH_STRATEGY).append(">");
         sb.append(flushStrategy);
         sb.append("</").append(CommonXML.ELEMENT_FLUSH_STRATEGY).append(">");
      }

      if (capacity != null)
         sb.append(capacity);

      sb.append("</pool>");
      
      return sb.toString();
   }
}
