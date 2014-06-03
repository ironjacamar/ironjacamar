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
import org.jboss.jca.common.api.metadata.common.Validation;
import org.jboss.jca.common.api.validator.ValidateException;

import org.jboss.logging.Messages;

/**
 *
 * A ValidationImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class ValidationImpl implements Validation
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /** backgroundValidation **/
   protected Boolean backgroundValidation;

   /** backgroundValidationMillis **/
   protected Long backgroundValidationMillis;

   /** useFastFail **/
   protected Boolean useFastFail;

   /**
    * Constructor
    *
    * @param backgroundValidation backgroundValidation
    * @param backgroundValidationMillis backgroundValidationMillis
    * @param useFastFail useFastFail
    * @throws ValidateException in case of error
    */
   public ValidationImpl(Boolean backgroundValidation, Long backgroundValidationMillis, Boolean useFastFail)
      throws ValidateException
   {
      this.backgroundValidation = backgroundValidation;
      this.backgroundValidationMillis = backgroundValidationMillis;
      this.useFastFail = useFastFail;
      partialCommonValidate();
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isBackgroundValidation()
   {
      return backgroundValidation;
   }

   /**
    * {@inheritDoc}
    */
   public Long getBackgroundValidationMillis()
   {
      return backgroundValidationMillis;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isUseFastFail()
   {
      return useFastFail;
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

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((backgroundValidation == null) ? 0 : backgroundValidation.hashCode());
      result = prime * result + ((backgroundValidationMillis == null) ? 0 : backgroundValidationMillis.hashCode());
      result = prime * result + ((useFastFail == null) ? 0 : useFastFail.hashCode());
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
      if (!(obj instanceof ValidationImpl))
         return false;
      ValidationImpl other = (ValidationImpl) obj;
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

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<validation>");

      if (backgroundValidation != null)
      {
         sb.append("<").append(Validation.Tag.BACKGROUND_VALIDATION).append(">");
         sb.append(backgroundValidation);
         sb.append("</").append(Validation.Tag.BACKGROUND_VALIDATION).append(">");
      }

      if (backgroundValidationMillis != null)
      {
         sb.append("<").append(Validation.Tag.BACKGROUND_VALIDATION_MILLIS).append(">");
         sb.append(backgroundValidationMillis);
         sb.append("</").append(Validation.Tag.BACKGROUND_VALIDATION_MILLIS).append(">");
      }

      if (useFastFail != null)
      {
         sb.append("<").append(Validation.Tag.USE_FAST_FAIL).append(">");
         sb.append(useFastFail);
         sb.append("</").append(Validation.Tag.USE_FAST_FAIL).append(">");
      }

      sb.append("</validation>");
      
      return sb.toString();
   }
}
