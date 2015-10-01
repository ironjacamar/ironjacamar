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

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.ds.Timeout;
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
public class TimeoutImpl extends org.ironjacamar.common.metadata.common.TimeoutImpl implements Timeout
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private final Boolean setTxQueryTimeout;

   private final Long queryTimeout;

   private final Long useTryLock;

   /**
    * Create a new TimeoutImpl.
    *
    * @param blockingTimeoutMillis blockingTimeoutMillis
    * @param idleTimeoutMinutes idleTimeoutMinutes
    * @param allocationRetry allocationRetry
    * @param allocationRetryWaitMillis allocationRetryWaitMillis
    * @param xaResourceTimeout xaResourceTimeout
    * @param setTxQueryTimeout setTxQueryTimeout
    * @param queryTimeout queryTimeout
    * @param useTryLock useTryLock
    * @param expressions expressions
    * @throws ValidateException ValidateException
    */
   public TimeoutImpl(Long blockingTimeoutMillis, Integer idleTimeoutMinutes, Integer allocationRetry,
                      Long allocationRetryWaitMillis, Integer xaResourceTimeout,
                      Boolean setTxQueryTimeout, Long queryTimeout,
                      Long useTryLock, Map<String, String> expressions) throws ValidateException
   {
      super(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry, allocationRetryWaitMillis,
            xaResourceTimeout, expressions);
      this.setTxQueryTimeout = setTxQueryTimeout;
      this.queryTimeout = queryTimeout;
      this.useTryLock = useTryLock;
      this.validate();
   }

   /**
    * Get the setTxQuertTimeout.
    *
    * @return the setTxQuertTimeout.
    */
   @Override
   public final Boolean isSetTxQueryTimeout()
   {
      return setTxQueryTimeout;
   }

   /**
    * Get the queryTimeout.
    *
    * @return the queryTimeout.
    */
   @Override
   public final Long getQueryTimeout()
   {
      return queryTimeout;
   }

   /**
    * Get the useTryLock.
    *
    * @return the useTryLock.
    */
   @Override
   public final Long getUseTryLock()
   {
      return useTryLock;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((queryTimeout == null) ? 0 : queryTimeout.hashCode());
      result = prime * result + ((setTxQueryTimeout == null) ? 0 : setTxQueryTimeout.hashCode());
      result = prime * result + ((useTryLock == null) ? 0 : useTryLock.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof TimeoutImpl))
         return false;
      TimeoutImpl other = (TimeoutImpl) obj;
      if (queryTimeout == null)
      {
         if (other.queryTimeout != null)
            return false;
      }
      else if (!queryTimeout.equals(other.queryTimeout))
         return false;
      if (setTxQueryTimeout == null)
      {
         if (other.setTxQueryTimeout != null)
            return false;
      }
      else if (!setTxQueryTimeout.equals(other.setTxQueryTimeout))
         return false;
      if (useTryLock == null)
      {
         if (other.useTryLock != null)
            return false;
      }
      else if (!useTryLock.equals(other.useTryLock))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<timeout>");

      if (blockingTimeoutMillis != null)
      {
         sb.append("<").append(XML.ELEMENT_BLOCKING_TIMEOUT_MILLIS).append(">");
         sb.append(blockingTimeoutMillis);
         sb.append("</").append(XML.ELEMENT_BLOCKING_TIMEOUT_MILLIS).append(">");
      }

      if (idleTimeoutMinutes != null)
      {
         sb.append("<").append(XML.ELEMENT_IDLE_TIMEOUT_MINUTES).append(">");
         sb.append(idleTimeoutMinutes);
         sb.append("</").append(XML.ELEMENT_IDLE_TIMEOUT_MINUTES).append(">");
      }

      if (setTxQueryTimeout != null && Boolean.TRUE.equals(setTxQueryTimeout))
      {
         sb.append("<").append(XML.ELEMENT_SET_TX_QUERY_TIMEOUT).append("/>");
      }

      if (queryTimeout != null)
      {
         sb.append("<").append(XML.ELEMENT_QUERY_TIMEOUT).append(">");
         sb.append(queryTimeout);
         sb.append("</").append(XML.ELEMENT_QUERY_TIMEOUT).append(">");
      }

      if (useTryLock != null)
      {
         sb.append("<").append(XML.ELEMENT_USE_TRY_LOCK).append(">");
         sb.append(useTryLock);
         sb.append("</").append(XML.ELEMENT_USE_TRY_LOCK).append(">");
      }

      if (allocationRetry != null)
      {
         sb.append("<").append(XML.ELEMENT_ALLOCATION_RETRY).append(">");
         sb.append(allocationRetry);
         sb.append("</").append(XML.ELEMENT_ALLOCATION_RETRY).append(">");
      }

      if (allocationRetryWaitMillis != null)
      {
         sb.append("<").append(XML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS).append(">");
         sb.append(allocationRetryWaitMillis);
         sb.append("</").append(XML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS).append(">");
      }

      if (xaResourceTimeout != null)
      {
         sb.append("<").append(XML.ELEMENT_XA_RESOURCE_TIMEOUT).append(">");
         sb.append(xaResourceTimeout);
         sb.append("</").append(XML.ELEMENT_XA_RESOURCE_TIMEOUT).append(">");
      }

      sb.append("</timeout>");

      return sb.toString();
   }

   @Override
   public void validate() throws ValidateException
   {
      if (this.queryTimeout != null && this.queryTimeout < 0)
         throw new ValidateException(bundle.invalidNegative(XML.ELEMENT_QUERY_TIMEOUT));

      if (this.useTryLock != null && this.useTryLock < 0)
         throw new ValidateException(bundle.invalidNegative(XML.ELEMENT_USE_TRY_LOCK));
   }
}
