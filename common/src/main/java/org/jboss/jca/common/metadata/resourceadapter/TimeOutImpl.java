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
package org.jboss.jca.common.metadata.resourceadapter;

import org.jboss.jca.common.api.metadata.resourceadapter.TimeOut;

/**
 *
 * A TimeOutImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class TimeOutImpl implements TimeOut
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -4482517387845540925L;

   private final Long blockingTimeoutMillis;

   private final Long idleTimeoutMinutes;

   private final Long allocationRetryWaitMillis;

   private final Long backgroundValidationMinutes;

   /**
    * Create a new TimeOutImpl.
    *
    * @param blockingTimeoutMillis blockingTimeoutMillis
    * @param idleTimeoutMinutes idleTimeoutMinutes
    * @param allocationRetryWaitMillis allocationRetryWaitMillis
    * @param backgroundValidationMinutes backgroundValidationMinutes
    */
   public TimeOutImpl(Long blockingTimeoutMillis, Long idleTimeoutMinutes, Long allocationRetryWaitMillis,
         Long backgroundValidationMinutes)
   {
      super();
      this.blockingTimeoutMillis = blockingTimeoutMillis;
      this.idleTimeoutMinutes = idleTimeoutMinutes;
      this.allocationRetryWaitMillis = allocationRetryWaitMillis;
      this.backgroundValidationMinutes = backgroundValidationMinutes;
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
    * Get the backgroundValidationMinutes.
    *
    * @return the backgroundValidationMinutes.
    */
   @Override
   public final Long getBackgroundValidationMinutes()
   {
      return backgroundValidationMinutes;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((allocationRetryWaitMillis == null) ? 0 : allocationRetryWaitMillis.hashCode());
      result = prime * result + ((backgroundValidationMinutes == null) ? 0 : backgroundValidationMinutes.hashCode());
      result = prime * result + ((blockingTimeoutMillis == null) ? 0 : blockingTimeoutMillis.hashCode());
      result = prime * result + ((idleTimeoutMinutes == null) ? 0 : idleTimeoutMinutes.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof TimeOutImpl))
         return false;
      TimeOutImpl other = (TimeOutImpl) obj;
      if (allocationRetryWaitMillis == null)
      {
         if (other.allocationRetryWaitMillis != null)
            return false;
      }
      else if (!allocationRetryWaitMillis.equals(other.allocationRetryWaitMillis))
         return false;
      if (backgroundValidationMinutes == null)
      {
         if (other.backgroundValidationMinutes != null)
            return false;
      }
      else if (!backgroundValidationMinutes.equals(other.backgroundValidationMinutes))
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
      return true;
   }

   @Override
   public String toString()
   {
      return "TimeOutImpl [blockingTimeoutMillis=" + blockingTimeoutMillis + ", idleTimeoutMinutes="
            + idleTimeoutMinutes + ", allocationRetryWaitMillis=" + allocationRetryWaitMillis
            + ", backgroundValidationMinutes=" + backgroundValidationMinutes + "]";
   }
}

