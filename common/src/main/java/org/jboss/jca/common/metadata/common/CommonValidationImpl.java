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
package org.jboss.jca.common.metadata.common;

import org.jboss.jca.common.api.metadata.common.CommonValidation;

/**
 *
 * A SValidationImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class CommonValidationImpl implements CommonValidation
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 2158460908861877316L;

   /** backgroundValidation **/
   protected final boolean backgroundValidation;

   /** backgroundValidationMinutes **/
   protected final Long backgroundValidationMinutes;

   /** useFastFail **/
   protected final boolean useFastFail;

   /**
    * Create a new SValidationImpl.
    *
    * @param backgroundValidation backgroundValidation
    * @param backgroundValidationMinutes backgroundValidationMinutes
    * @param useFastFail useFastFail
    */
   public CommonValidationImpl(boolean backgroundValidation, Long backgroundValidationMinutes, boolean useFastFail)
   {
      super();
      this.backgroundValidation = backgroundValidation;
      this.backgroundValidationMinutes = backgroundValidationMinutes;
      this.useFastFail = useFastFail;
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
    * Get the backgroundValidationMinutes.
    *
    * @return the backgroundValidationMinutes.
    */
   @Override
   public final Long getBackgroundValidationMinutes()
   {
      return backgroundValidationMinutes;
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
      result = prime * result + (backgroundValidation ? 1231 : 1237);
      result = prime * result +
               ((backgroundValidationMinutes == null) ? 0 : backgroundValidationMinutes.hashCode());
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
      if (!(obj instanceof CommonValidationImpl))
         return false;
      CommonValidationImpl other = (CommonValidationImpl) obj;
      if (backgroundValidation != other.backgroundValidation)
         return false;
      if (backgroundValidationMinutes == null)
      {
         if (other.backgroundValidationMinutes != null)
            return false;
      }
      else if (!backgroundValidationMinutes.equals(other.backgroundValidationMinutes))
         return false;
      if (useFastFail != other.useFastFail)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "SValidationImpl [backgroundValidation=" + backgroundValidation + ", backgroundValidationMinutes=" +
             backgroundValidationMinutes + ", useFastFail=" + useFastFail + "]";
   }

}
