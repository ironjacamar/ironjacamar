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
import org.ironjacamar.common.api.metadata.common.Validation;
import org.ironjacamar.common.api.validator.ValidateException;

import java.util.Map;

import org.jboss.logging.Messages;

/**
 *
 * A ValidationImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class ValidationImpl extends AbstractMetadata implements Validation
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /** validateOnMatch **/
   protected Boolean validateOnMatch;

   /** backgroundValidation **/
   protected Boolean backgroundValidation;

   /** backgroundValidationMillis **/
   protected Long backgroundValidationMillis;

   /** useFastFail **/
   protected Boolean useFastFail;

   /**
    * Constructor
    *
    * @param validateOnMatch validateOnMatch
    * @param backgroundValidation backgroundValidation
    * @param backgroundValidationMillis backgroundValidationMillis
    * @param useFastFail useFastFail
    * @param expressions expressions
    * @throws ValidateException in case of error
    */
   public ValidationImpl(Boolean validateOnMatch,
                         Boolean backgroundValidation, Long backgroundValidationMillis,
                         Boolean useFastFail, Map<String, String> expressions)
      throws ValidateException
   {
      super(expressions);
      this.validateOnMatch = validateOnMatch;
      this.backgroundValidation = backgroundValidation;
      this.backgroundValidationMillis = backgroundValidationMillis;
      this.useFastFail = useFastFail;
      partialCommonValidate();
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isValidateOnMatch()
   {
      return validateOnMatch;
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
         throw new ValidateException(bundle.invalidNegative(CommonXML.ELEMENT_BACKGROUND_VALIDATION_MILLIS));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((validateOnMatch == null) ? 0 : validateOnMatch.hashCode());
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
      if (validateOnMatch == null)
      {
         if (other.validateOnMatch != null)
            return false;
      }
      else if (!validateOnMatch.equals(other.validateOnMatch))
         return false;
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

      if (validateOnMatch != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_VALIDATE_ON_MATCH).append(">");
         sb.append(validateOnMatch);
         sb.append("</").append(CommonXML.ELEMENT_VALIDATE_ON_MATCH).append(">");
      }

      if (backgroundValidation != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_BACKGROUND_VALIDATION).append(">");
         sb.append(backgroundValidation);
         sb.append("</").append(CommonXML.ELEMENT_BACKGROUND_VALIDATION).append(">");
      }

      if (backgroundValidationMillis != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_BACKGROUND_VALIDATION_MILLIS).append(">");
         sb.append(backgroundValidationMillis);
         sb.append("</").append(CommonXML.ELEMENT_BACKGROUND_VALIDATION_MILLIS).append(">");
      }

      if (useFastFail != null)
      {
         sb.append("<").append(CommonXML.ELEMENT_USE_FAST_FAIL).append(">");
         sb.append(useFastFail);
         sb.append("</").append(CommonXML.ELEMENT_USE_FAST_FAIL).append(">");
      }

      sb.append("</validation>");
      
      return sb.toString();
   }
}
