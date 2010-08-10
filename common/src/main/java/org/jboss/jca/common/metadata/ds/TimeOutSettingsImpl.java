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

import org.jboss.jca.common.api.metadata.ds.TimeOutSettings;

/**
 *
 * A TimeOutSettingsImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class TimeOutSettingsImpl implements TimeOutSettings
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -8797718258493768716L;

   private final Long blockingTimeoutMillis;

   private final Long idleTimeoutMinutes;

   private final boolean setTxQuertTimeout;

   private final Long queryTimeout;

   private final Long useTryLock;

   private final Long allocationRetry;

   private final Long allocationRetryWaitMillis;

   /**
    * Create a new TimeOutSettingsImpl.
    *
    * @param blockingTimeoutMillis blockingTimeoutMillis
    * @param idleTimeoutMinutes idleTimeoutMinutes
    * @param setTxQuertTimeout setTxQuertTimeout
    * @param queryTimeout queryTimeout
    * @param useTryLock useTryLock
    * @param allocationRetry allocationRetry
    * @param allocationRetryWaitMillis allocationRetryWaitMillis
    */
   public TimeOutSettingsImpl(Long blockingTimeoutMillis, Long idleTimeoutMinutes, boolean setTxQuertTimeout,
         Long queryTimeout, Long useTryLock, Long allocationRetry, Long allocationRetryWaitMillis)
   {
      super();
      this.blockingTimeoutMillis = blockingTimeoutMillis;
      this.idleTimeoutMinutes = idleTimeoutMinutes;
      this.setTxQuertTimeout = setTxQuertTimeout;
      this.queryTimeout = queryTimeout;
      this.useTryLock = useTryLock;
      this.allocationRetry = allocationRetry;
      this.allocationRetryWaitMillis = allocationRetryWaitMillis;
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
    * Get the setTxQuertTimeout.
    *
    * @return the setTxQuertTimeout.
    */
   @Override
   public final boolean isSetTxQuertTimeout()
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

   /**
    * Get the allocationRetry.
    *
    * @return the allocationRetry.
    */
   @Override
   public final Long getAllocationRetry()
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

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((allocationRetry == null) ? 0 : allocationRetry.hashCode());
      result = prime * result + ((allocationRetryWaitMillis == null) ? 0 : allocationRetryWaitMillis.hashCode());
      result = prime * result + ((blockingTimeoutMillis == null) ? 0 : blockingTimeoutMillis.hashCode());
      result = prime * result + ((idleTimeoutMinutes == null) ? 0 : idleTimeoutMinutes.hashCode());
      result = prime * result + ((queryTimeout == null) ? 0 : queryTimeout.hashCode());
      result = prime * result + (setTxQuertTimeout ? 1231 : 1237);
      result = prime * result + ((useTryLock == null) ? 0 : useTryLock.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof TimeOutSettingsImpl))
         return false;
      TimeOutSettingsImpl other = (TimeOutSettingsImpl) obj;
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
      if (queryTimeout == null)
      {
         if (other.queryTimeout != null)
            return false;
      }
      else if (!queryTimeout.equals(other.queryTimeout))
         return false;
      if (setTxQuertTimeout != other.setTxQuertTimeout)
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
      return "TimeOutSettingsImpl [blockingTimeoutMillis=" + blockingTimeoutMillis + ", idleTimeoutMinutes="
            + idleTimeoutMinutes + ", setTxQuertTimeout=" + setTxQuertTimeout + ", queryTimeout=" + queryTimeout
            + ", useTryLock=" + useTryLock + ", allocationRetry=" + allocationRetry + ", allocationRetryWaitMillis="
            + allocationRetryWaitMillis + "]";
   }

}

