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

import org.ironjacamar.common.api.metadata.Defaults;
import org.ironjacamar.common.api.metadata.common.Capacity;
import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.common.api.metadata.common.XaPool;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Map;

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

   /** pad-xid */
   protected Boolean padXid;

   /** wrap-xa-resource */
   protected Boolean wrapXaResource;

   /**
    * Create a new XaPoolImpl.
    * @param type type
    * @param janitor janitor
    * @param minPoolSize minPoolSize
    * @param initialPoolSize initialPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param flushStrategy flushStrategy
    * @param capacity capacity
    * @param isSameRmOverride isSameRmOverride
    * @param padXid padXid
    * @param wrapXaResource wrapXaResource
    * @param expressions expressions
    * @throws ValidateException ValidateException
    */
   public XaPoolImpl(String type, String janitor, Integer minPoolSize, Integer initialPoolSize, Integer maxPoolSize,
                     Boolean prefill,
                     FlushStrategy flushStrategy, Capacity capacity,
                     Boolean isSameRmOverride,
                     Boolean padXid, Boolean wrapXaResource,
                     Map<String, String> expressions) throws ValidateException
   {
      super(type, janitor, minPoolSize, initialPoolSize, maxPoolSize, prefill,
            flushStrategy, capacity, expressions);
      this.isSameRmOverride = isSameRmOverride;
      this.padXid = padXid;
      this.wrapXaResource = wrapXaResource;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isIsSameRmOverride()
   {
      return isSameRmOverride;
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
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((isSameRmOverride == null) ? 0 : isSameRmOverride.hashCode());
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
      if (isSameRmOverride == null)
      {
         if (other.isSameRmOverride != null)
            return false;
      }
      else if (!isSameRmOverride.equals(other.isSameRmOverride))
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

      sb.append("<xa-pool");

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

      if (isSameRmOverride != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_IS_SAME_RM_OVERRIDE).append(">");
         sb.append(isSameRmOverride);
         sb.append("</").append(CommonXML.ELEMENT_IS_SAME_RM_OVERRIDE).append(">");
      }

      if (padXid != null && !Defaults.PAD_XID.equals(padXid))
      {
         sb.append("<").append(CommonXML.ELEMENT_PAD_XID).append(">");
         sb.append(padXid);
         sb.append("</").append(CommonXML.ELEMENT_PAD_XID).append(">");
      }

      if (wrapXaResource != null && !Defaults.WRAP_XA_RESOURCE.equals(wrapXaResource))
      {
         sb.append("<").append(CommonXML.ELEMENT_WRAP_XA_RESOURCE).append(">");
         sb.append(wrapXaResource);
         sb.append("</").append(CommonXML.ELEMENT_WRAP_XA_RESOURCE).append(">");
      }

      sb.append("</xa-pool>");
      
      return sb.toString();
   }
}

