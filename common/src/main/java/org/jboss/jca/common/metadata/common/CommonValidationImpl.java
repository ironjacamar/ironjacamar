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

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.validator.ValidateException;

import org.jboss.logging.Messages;

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

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /** backgroundValidation **/
   protected final Boolean backgroundValidation;

   /** backgroundValidationMillis **/
   protected final Long backgroundValidationMillis;

   /** useFastFail **/
   protected final Boolean useFastFail;

   /**
    * Create a new SValidationImpl.
    *
    * @param backgroundValidation backgroundValidation
    * @param backgroundValidationMillis backgroundValidationMillis
    * @param useFastFail useFastFail
    * @throws ValidateException in case of error
    */
   public CommonValidationImpl(Boolean backgroundValidation, Long backgroundValidationMillis, Boolean useFastFail)
      throws ValidateException
   {
      super();
      this.backgroundValidation = backgroundValidation;
      this.backgroundValidationMillis = backgroundValidationMillis;
      this.useFastFail = useFastFail;
      partialCommonValidate();
   }

   /**
    * Get the backgroundValidation.
    *
    * @return the backgroundValidation.
    */
   @Override
   public final Boolean isBackgroundValidation()
   {
      return backgroundValidation;
   }

   /**
    * Get the backgroundValidationMillis.
    *
    * @return the backgroundValidationMillis.
    */
   @Override
   public final Long getBackgroundValidationMillis()
   {
      return backgroundValidationMillis;
   }

   /**
    * Get the useFastFail.
    *
    * @return the useFastFail.
    */
   @Override
   public final Boolean isUseFastFail()
   {
      return useFastFail;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((backgroundValidation == null) ? 0 : backgroundValidation.hashCode());
      result = prime * result + ((backgroundValidationMillis == null) ? 0 : backgroundValidationMillis.hashCode());
      result = prime * result + ((useFastFail == null) ? 0 : useFastFail.hashCode());
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
      if (backgroundValidation == null)
      {
         if (other.backgroundValidation != null)
            return false;
      }
      else if (!backgroundValidation.equals(other.backgroundValidation))
         return false;
      if (backgroundValidationMillis == null)
      {
         if (other.backgroundValidationMillis != null)
            return false;
      }
      else if (!backgroundValidationMillis.equals(other.backgroundValidationMillis))
         return false;
      if (useFastFail == null)
      {
         if (other.useFastFail != null)
            return false;
      }
      else if (!useFastFail.equals(other.useFastFail))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<validation>");

      if (backgroundValidation != null)
      {
         sb.append("<").append(CommonValidation.Tag.BACKGROUND_VALIDATION).append(">");
         sb.append(backgroundValidation);
         sb.append("</").append(CommonValidation.Tag.BACKGROUND_VALIDATION).append(">");
      }

      if (backgroundValidationMillis != null)
      {
         sb.append("<").append(CommonValidation.Tag.BACKGROUND_VALIDATION_MILLIS).append(">");
         sb.append(backgroundValidationMillis);
         sb.append("</").append(CommonValidation.Tag.BACKGROUND_VALIDATION_MILLIS).append(">");
      }

      if (useFastFail != null)
      {
         sb.append("<").append(CommonValidation.Tag.USE_FAST_FAIL).append(">");
         sb.append(useFastFail);
         sb.append("</").append(CommonValidation.Tag.USE_FAST_FAIL).append(">");
      }

      sb.append("</validation>");
      
      return sb.toString();
   }
   /**
    * 
    * Validation 
    * 
    * @throws ValidateException in case of error
    */
   private void partialCommonValidate() throws ValidateException
   {
      if (this.backgroundValidationMillis != null && this.backgroundValidationMillis < 0)
         throw new ValidateException(bundle.invalidNegative(Tag.BACKGROUND_VALIDATION_MILLIS.getLocalName()));
   }
}
