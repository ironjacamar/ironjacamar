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

import org.jboss.jca.common.api.metadata.ds.TimeOut;

/**
 *
 * A TimeOutImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class TimeOutImpl extends org.jboss.jca.common.metadata.common.CommonTimeOutImpl implements TimeOut
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -8797718258493768716L;

   private final Boolean setTxQuertTimeout;

   private final Long queryTimeout;

   private final Long useTryLock;

   /**
    * Create a new TimeOutImpl.
    *
    * @param blockingTimeoutMillis blockingTimeoutMillis
    * @param idleTimeoutMinutes idleTimeoutMinutes
    * @param allocationRetry allocationRetry
    * @param allocationRetryWaitMillis allocationRetryWaitMillis
    * @param xaResourceTimeout xaResourceTimeout
    * @param setTxQuertTimeout setTxQuertTimeout
    * @param queryTimeout queryTimeout
    * @param useTryLock useTryLock
    */
   public TimeOutImpl(Long blockingTimeoutMillis, Long idleTimeoutMinutes, Integer allocationRetry,
      Long allocationRetryWaitMillis, Integer xaResourceTimeout, Boolean setTxQuertTimeout, Long queryTimeout,
      Long useTryLock)
   {
      super(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry, allocationRetryWaitMillis,
            xaResourceTimeout);
      this.setTxQuertTimeout = setTxQuertTimeout;
      this.queryTimeout = queryTimeout;
      this.useTryLock = useTryLock;
   }

   /**
    * Get the setTxQuertTimeout.
    *
    * @return the setTxQuertTimeout.
    */
   @Override
   public final Boolean isSetTxQueryTimeout()
   {
      return setTxQuertTimeout;
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
      result = prime * result + ((setTxQuertTimeout == null) ? 0 : setTxQuertTimeout.hashCode());
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
      if (!(obj instanceof TimeOutImpl))
         return false;
      TimeOutImpl other = (TimeOutImpl) obj;
      if (queryTimeout == null)
      {
         if (other.queryTimeout != null)
            return false;
      }
      else if (!queryTimeout.equals(other.queryTimeout))
         return false;
      if (setTxQuertTimeout == null)
      {
         if (other.setTxQuertTimeout != null)
            return false;
      }
      else if (!setTxQuertTimeout.equals(other.setTxQuertTimeout))
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
      return "TimeOutImpl [setTxQuertTimeout=" + setTxQuertTimeout + ", queryTimeout=" + queryTimeout +
             ", useTryLock=" + useTryLock + "]";
   }

}
