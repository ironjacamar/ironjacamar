/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.validator.ValidateException;

import org.jboss.logging.Messages;

/**
 *
 * A TimeOutImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class CommonTimeOutImpl implements CommonTimeOut
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 7351813875143571341L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /** blockingTimeoutMillis **/
   protected final Long blockingTimeoutMillis;

   /** idleTimeoutMinutes **/
   protected final Long idleTimeoutMinutes;

   /** allocationRetry **/
   protected final Integer allocationRetry;

   /** allocationRetryWaitMillis **/
   protected final Long allocationRetryWaitMillis;

   /** xaResourceTimeout **/
   protected final Integer xaResourceTimeout;

   /**
    * Create a new STimeOutImpl.
    *
    * @param blockingTimeoutMillis blockingTimeoutMillis
    * @param idleTimeoutMinutes idleTimeoutMinutes
    * @param allocationRetry allocationRetry
    * @param allocationRetryWaitMillis allocationRetryWaitMillis
    * @param xaResourceTimeout xaResourceTimeout
    * @throws ValidateException ValidateException
    */
   public CommonTimeOutImpl(Long blockingTimeoutMillis, Long idleTimeoutMinutes, Integer allocationRetry,
      Long allocationRetryWaitMillis, Integer xaResourceTimeout) throws ValidateException
   {
      super();
      this.blockingTimeoutMillis = blockingTimeoutMillis;
      this.idleTimeoutMinutes = idleTimeoutMinutes;
      this.allocationRetry = allocationRetry;
      this.allocationRetryWaitMillis = allocationRetryWaitMillis;
      this.xaResourceTimeout = xaResourceTimeout;
      this.partialCommonValidate();
   }

   /**
    * Get the blockingTimeoutMillis.
    *
    * @return the blockingTimeoutMillis.
    */
   @Override
   public final Long getBlockingTimeoutMillis()
   {
      return blockingTimeoutMillis;
   }

   /**
    * Get the idleTimeoutMinutes.
    *
    * @return the idleTimeoutMinutes.
    */
   @Override
   public final Long getIdleTimeoutMinutes()
   {
      return idleTimeoutMinutes;
   }

   /**
    * Get the allocationRetry.
    *
    * @return the allocationRetry.
    */
   @Override
   public final Integer getAllocationRetry()
   {
      return allocationRetry;
   }

   /**
    * Get the allocationRetryWaitMillis.
    *
    * @return the allocationRetryWaitMillis.
    */
   @Override
   public final Long getAllocationRetryWaitMillis()
   {
      return allocationRetryWaitMillis;
   }

   /**
    * Get the xaResourceTimeout.
    *
    * @return the xaResourceTimeout.
    */
   @Override
   public Integer getXaResourceTimeout()
   {
      return xaResourceTimeout;
   }

   @Override
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

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof CommonTimeOutImpl))
         return false;
      CommonTimeOutImpl other = (CommonTimeOutImpl) obj;
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

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<timeout>");

      if (blockingTimeoutMillis != null)
      {
         sb.append("<").append(CommonTimeOut.Tag.BLOCKING_TIMEOUT_MILLIS).append(">");
         sb.append(blockingTimeoutMillis);
         sb.append("</").append(CommonTimeOut.Tag.BLOCKING_TIMEOUT_MILLIS).append(">");
      }

      if (idleTimeoutMinutes != null)
      {
         sb.append("<").append(CommonTimeOut.Tag.IDLE_TIMEOUT_MINUTES).append(">");
         sb.append(idleTimeoutMinutes);
         sb.append("</").append(CommonTimeOut.Tag.IDLE_TIMEOUT_MINUTES).append(">");
      }

      if (allocationRetry != null)
      {
         sb.append("<").append(CommonTimeOut.Tag.ALLOCATION_RETRY).append(">");
         sb.append(allocationRetry);
         sb.append("</").append(CommonTimeOut.Tag.ALLOCATION_RETRY).append(">");
      }

      if (allocationRetryWaitMillis != null)
      {
         sb.append("<").append(CommonTimeOut.Tag.ALLOCATION_RETRY_WAIT_MILLIS).append(">");
         sb.append(allocationRetryWaitMillis);
         sb.append("</").append(CommonTimeOut.Tag.ALLOCATION_RETRY_WAIT_MILLIS).append(">");
      }

      if (xaResourceTimeout != null)
      {
         sb.append("<").append(CommonTimeOut.Tag.XA_RESOURCE_TIMEOUT).append(">");
         sb.append(xaResourceTimeout);
         sb.append("</").append(CommonTimeOut.Tag.XA_RESOURCE_TIMEOUT).append(">");
      }

      sb.append("</timeout>");
      
      return sb.toString();
   }

   private void partialCommonValidate() throws ValidateException
   {
      if (this.allocationRetry != null && this.allocationRetry < 0)
         throw new ValidateException(bundle.invalidNegative(Tag.ALLOCATION_RETRY.getLocalName()));

      if (this.blockingTimeoutMillis != null && this.blockingTimeoutMillis < 0)
         throw new ValidateException(bundle.invalidNegative(Tag.BLOCKING_TIMEOUT_MILLIS.getLocalName()));

      if (this.allocationRetryWaitMillis != null && this.allocationRetryWaitMillis < 0)
         throw new ValidateException(bundle.invalidNegative(Tag.ALLOCATION_RETRY_WAIT_MILLIS.getLocalName()));

      if (this.idleTimeoutMinutes != null && this.idleTimeoutMinutes < 0)
         throw new ValidateException(bundle.invalidNegative(Tag.IDLE_TIMEOUT_MINUTES.getLocalName()));

      if (this.xaResourceTimeout != null && this.xaResourceTimeout < 0)
         throw new ValidateException(bundle.invalidNegative(Tag.XA_RESOURCE_TIMEOUT.getLocalName()));
   }
}
