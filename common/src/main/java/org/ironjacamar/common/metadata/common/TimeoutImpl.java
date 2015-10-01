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
import org.ironjacamar.common.api.metadata.common.Timeout;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Map;

import org.jboss.logging.Messages;

/**
 *
 * A TimeoutImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class TimeoutImpl extends AbstractMetadata implements Timeout
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /** blockingTimeoutMillis **/
   protected Long blockingTimeoutMillis;

   /** idleTimeoutMinutes **/
   protected Integer idleTimeoutMinutes;

   /** allocationRetry **/
   protected Integer allocationRetry;

   /** allocationRetryWaitMillis **/
   protected Long allocationRetryWaitMillis;

   /** xaResourceTimeout **/
   protected Integer xaResourceTimeout;

   /**
    * Constructor
    *
    * @param blockingTimeoutMillis blockingTimeoutMillis
    * @param idleTimeoutMinutes idleTimeoutMinutes
    * @param allocationRetry allocationRetry
    * @param allocationRetryWaitMillis allocationRetryWaitMillis
    * @param xaResourceTimeout xaResourceTimeout
    * @param expressions expressions
    * @throws ValidateException ValidateException
    */
   public TimeoutImpl(Long blockingTimeoutMillis, Integer idleTimeoutMinutes, Integer allocationRetry,
                      Long allocationRetryWaitMillis, Integer xaResourceTimeout,
                      Map<String, String> expressions) throws ValidateException
   {
      super(expressions);
      this.blockingTimeoutMillis = blockingTimeoutMillis;
      this.idleTimeoutMinutes = idleTimeoutMinutes;
      this.allocationRetry = allocationRetry;
      this.allocationRetryWaitMillis = allocationRetryWaitMillis;
      this.xaResourceTimeout = xaResourceTimeout;
      this.partialCommonValidate();
   }

   /**
    * {@inheritDoc}
    */
   public Long getBlockingTimeoutMillis()
   {
      return blockingTimeoutMillis;
   }

   /**
    * {@inheritDoc}
    */
   public Integer getIdleTimeoutMinutes()
   {
      return idleTimeoutMinutes;
   }

   /**
    * {@inheritDoc}
    */
   public Integer getAllocationRetry()
   {
      return allocationRetry;
   }

   /**
    * {@inheritDoc}
    */
   public Long getAllocationRetryWaitMillis()
   {
      return allocationRetryWaitMillis;
   }

   /**
    * {@inheritDoc}
    */
   public Integer getXaResourceTimeout()
   {
      return xaResourceTimeout;
   }

   private void partialCommonValidate() throws ValidateException
   {
      if (this.allocationRetry != null && this.allocationRetry < 0)
         throw new ValidateException(bundle.invalidNegative(CommonXML.ELEMENT_ALLOCATION_RETRY));

      if (this.blockingTimeoutMillis != null && this.blockingTimeoutMillis < 0)
         throw new ValidateException(bundle.invalidNegative(CommonXML.ELEMENT_BLOCKING_TIMEOUT_MILLIS));

      if (this.allocationRetryWaitMillis != null && this.allocationRetryWaitMillis < 0)
         throw new ValidateException(bundle.invalidNegative(CommonXML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS));

      if (this.idleTimeoutMinutes != null && this.idleTimeoutMinutes < 0)
         throw new ValidateException(bundle.invalidNegative(CommonXML.ELEMENT_IDLE_TIMEOUT_MINUTES));

      if (this.xaResourceTimeout != null && this.xaResourceTimeout < 0)
         throw new ValidateException(bundle.invalidNegative(CommonXML.ELEMENT_XA_RESOURCE_TIMEOUT));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((allocationRetry == null) ? 0 : allocationRetry.hashCode());
      result = prime * result + ((allocationRetryWaitMillis == null) ? 0 : allocationRetryWaitMillis.hashCode());
      result = prime * result + ((blockingTimeoutMillis == null) ? 0 : blockingTimeoutMillis.hashCode());
      result = prime * result + ((idleTimeoutMinutes == null) ? 0 : idleTimeoutMinutes.hashCode());
      result = prime * result + ((xaResourceTimeout == null) ? 0 : xaResourceTimeout.hashCode());
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
      if (!(obj instanceof TimeoutImpl))
         return false;
      TimeoutImpl other = (TimeoutImpl) obj;
      if (allocationRetry == null)
      {
         if (other.allocationRetry != null)
            return false;
      }
      else if (!allocationRetry.equals(other.allocationRetry))
         return false;
      if (allocationRetryWaitMillis == null)
      {
         if (other.allocationRetryWaitMillis != null)
            return false;
      }
      else if (!allocationRetryWaitMillis.equals(other.allocationRetryWaitMillis))
         return false;
      if (blockingTimeoutMillis == null)
      {
         if (other.blockingTimeoutMillis != null)
            return false;
      }
      else if (!blockingTimeoutMillis.equals(other.blockingTimeoutMillis))
         return false;
      if (idleTimeoutMinutes == null)
      {
         if (other.idleTimeoutMinutes != null)
            return false;
      }
      else if (!idleTimeoutMinutes.equals(other.idleTimeoutMinutes))
         return false;
      if (xaResourceTimeout == null)
      {
         if (other.xaResourceTimeout != null)
            return false;
      }
      else if (!xaResourceTimeout.equals(other.xaResourceTimeout))
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<timeout>");

      if (blockingTimeoutMillis != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_BLOCKING_TIMEOUT_MILLIS).append(">");
         sb.append(blockingTimeoutMillis);
         sb.append("</").append(CommonXML.ELEMENT_BLOCKING_TIMEOUT_MILLIS).append(">");
      }

      if (idleTimeoutMinutes != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_IDLE_TIMEOUT_MINUTES).append(">");
         sb.append(idleTimeoutMinutes);
         sb.append("</").append(CommonXML.ELEMENT_IDLE_TIMEOUT_MINUTES).append(">");
      }

      if (allocationRetry != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_ALLOCATION_RETRY).append(">");
         sb.append(allocationRetry);
         sb.append("</").append(CommonXML.ELEMENT_ALLOCATION_RETRY).append(">");
      }

      if (allocationRetryWaitMillis != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS).append(">");
         sb.append(allocationRetryWaitMillis);
         sb.append("</").append(CommonXML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS).append(">");
      }

      if (xaResourceTimeout != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_XA_RESOURCE_TIMEOUT).append(">");
         sb.append(xaResourceTimeout);
         sb.append("</").append(CommonXML.ELEMENT_XA_RESOURCE_TIMEOUT).append(">");
      }

      sb.append("</timeout>");
      
      return sb.toString();
   }
}
