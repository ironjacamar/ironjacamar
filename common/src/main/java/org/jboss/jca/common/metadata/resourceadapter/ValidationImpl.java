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

import org.jboss.jca.common.api.metadata.resourceadapter.Validation;

/**
 *
 * A ValidationImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class ValidationImpl implements Validation
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 2178081632626071094L;

   private final Integer allocationRetry;

   private final boolean backgroundValidation;

   private final boolean useFastFail;

   /**
    * Create a new ValidationImpl.
    *
    * @param allocationRetry allocationRetry
    * @param backgroundValidation backgroundValidation
    * @param useFastFail useFastFail
    */
   public ValidationImpl(Integer allocationRetry, boolean backgroundValidation, boolean useFastFail)
   {
      super();
      this.allocationRetry = allocationRetry;
      this.backgroundValidation = backgroundValidation;
      this.useFastFail = useFastFail;
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
    * Get the backgroundValidation.
    *
    * @return the backgroundValidation.
    */
   @Override
   public final boolean isBackgroundValidation()
   {
      return backgroundValidation;
   }

   /**
    * Get the useFastFail.
    *
    * @return the useFastFail.
    */
   @Override
   public final boolean isUseFastFail()
   {
      return useFastFail;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((allocationRetry == null) ? 0 : allocationRetry.hashCode());
      result = prime * result + (backgroundValidation ? 1231 : 1237);
      result = prime * result + (useFastFail ? 1231 : 1237);
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof ValidationImpl))
         return false;
      ValidationImpl other = (ValidationImpl) obj;
      if (allocationRetry == null)
      {
         if (other.allocationRetry != null)
            return false;
      }
      else if (!allocationRetry.equals(other.allocationRetry))
         return false;
      if (backgroundValidation != other.backgroundValidation)
         return false;
      if (useFastFail != other.useFastFail)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "ValidationImpl [allocationRetry=" + allocationRetry + ", backgroundValidation=" + backgroundValidation
            + ", useFastFail=" + useFastFail + "]";
   }

}

