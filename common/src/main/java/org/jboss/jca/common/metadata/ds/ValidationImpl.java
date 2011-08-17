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

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.Iterator;
import java.util.Map;

import org.jboss.logging.Messages;

/**
 *
 * A ValidationImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class ValidationImpl extends org.jboss.jca.common.metadata.common.CommonValidationImpl implements Validation
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 7816717816552118419L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private final Extension validConnectionChecker;

   private final String checkValidConnectionSql;

   private final Boolean validateOnMatch;

   private final Extension staleConnectionChecker;

   private final Extension exceptionSorter;

   /**
    * Create a new ValidationImpl.
    *
    * @param backgroundValidation backgroundValidation
    * @param backgroundValidationMillis backgroundValidationMillis
    * @param useFastFail useFastFail
    * @param validConnectionChecker validConnectionChecker
    * @param checkValidConnectionSql checkValidConnectionSql
    * @param validateOnMatch validateOnMatch
    * @param staleConnectionChecker staleConnectionChecker
    * @param exceptionSorter exceptionSorter
    * @throws ValidateException ValidateException
    */
   public ValidationImpl(Boolean backgroundValidation, Long backgroundValidationMillis, Boolean useFastFail,
      Extension validConnectionChecker, String checkValidConnectionSql, Boolean validateOnMatch,
      Extension staleConnectionChecker, Extension exceptionSorter) throws ValidateException
   {
      super(backgroundValidation, backgroundValidationMillis, useFastFail);
      this.validConnectionChecker = validConnectionChecker;
      this.checkValidConnectionSql = checkValidConnectionSql;
      this.validateOnMatch = validateOnMatch;
      this.staleConnectionChecker = staleConnectionChecker;
      this.exceptionSorter = exceptionSorter;
      this.validate();
   }

   /**
    * Get the checkValidConnectionSql.
    *
    * @return the checkValidConnectionSql.
    */
   @Override
   public final String getCheckValidConnectionSql()
   {
      return checkValidConnectionSql;
   }

   /**
    * Get the validateOnMatch.
    *
    * @return the validateOnMatch.
    */
   @Override
   public final Boolean isValidateOnMatch()
   {
      return validateOnMatch;
   }

   /**
    * Get the validConnectionChecker.
    *
    * @return the validConnectionChecker.
    */
   public final Extension getValidConnectionChecker()
   {
      return validConnectionChecker;
   }

   /**
    * Get the validateOnMatch.
    *
    * @return the validateOnMatch.
    */
   public final Boolean getValidateOnMatch()
   {
      return validateOnMatch;
   }

   /**
    * Get the staleConnectionChecker.
    *
    * @return the staleConnectionChecker.
    */
   public final Extension getStaleConnectionChecker()
   {
      return staleConnectionChecker;
   }

   /**
    * Get the exceptionSorter.
    *
    * @return the exceptionSorter.
    */
   public final Extension getExceptionSorter()
   {
      return exceptionSorter;
   }

   @Override
   public void validate() throws ValidateException
   {
      if (this.backgroundValidationMillis != null && this.backgroundValidationMillis < 0)
         throw new ValidateException(bundle.invalidNegative(Validation.Tag.BACKGROUNDVALIDATIONMILLIS.getLocalName()));

      if (this.validConnectionChecker != null)
         try
         {
            this.validConnectionChecker.validate();
         }
         catch (ValidateException ve)
         {
            throw new ValidateException(bundle.invalidTag(Validation.Tag.VALIDCONNECTIONCHECKER.getLocalName()), ve);
         }
      if (this.exceptionSorter != null)
         try
         {
            this.exceptionSorter.validate();
         }
         catch (ValidateException ve)
         {
            throw new ValidateException(bundle.invalidTag(Validation.Tag.EXCEPTIONSORTER.getLocalName()), ve);
         }
      if (this.staleConnectionChecker != null)
         try
         {
            this.staleConnectionChecker.validate();
         }
         catch (ValidateException ve)
         {
            throw new ValidateException(bundle.invalidTag(Validation.Tag.STALECONNECTIONCHECKER.getLocalName()), ve);
         }
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<validation>");

      if (validConnectionChecker != null)
      {
         sb.append("<").append(Validation.Tag.VALIDCONNECTIONCHECKER);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(validConnectionChecker.getClassName()).append("\"");
         sb.append(">");

         if (validConnectionChecker.getConfigPropertiesMap() != null &&
             validConnectionChecker.getConfigPropertiesMap().size() > 0)
         {
            Iterator<Map.Entry<String, String>> it =
               validConnectionChecker.getConfigPropertiesMap().entrySet().iterator();
            
            while (it.hasNext())
            {
               Map.Entry<String, String> entry = it.next();

               sb.append("<").append(Extension.Tag.CONFIG_PROPERTY);
               sb.append(" name=\"").append(entry.getKey()).append("\">");
               sb.append(entry.getValue());
               sb.append("</").append(Extension.Tag.CONFIG_PROPERTY).append(">");
            }
         }

         sb.append("</").append(Validation.Tag.VALIDCONNECTIONCHECKER).append(">");
      }

      if (checkValidConnectionSql != null)
      {
         sb.append("<").append(Validation.Tag.CHECKVALIDCONNECTIONSQL).append(">");
         sb.append(checkValidConnectionSql);
         sb.append("</").append(Validation.Tag.CHECKVALIDCONNECTIONSQL).append(">");
      }

      if (validateOnMatch != null)
      {
         sb.append("<").append(Validation.Tag.VALIDATEONMATCH).append(">");
         sb.append(validateOnMatch);
         sb.append("</").append(Validation.Tag.VALIDATEONMATCH).append(">");
      }

      if (backgroundValidation != null)
      {
         sb.append("<").append(Validation.Tag.BACKGROUNDVALIDATION).append(">");
         sb.append(backgroundValidation);
         sb.append("</").append(Validation.Tag.BACKGROUNDVALIDATION).append(">");
      }

      if (backgroundValidationMillis != null)
      {
         sb.append("<").append(Validation.Tag.BACKGROUNDVALIDATIONMILLIS).append(">");
         sb.append(backgroundValidationMillis);
         sb.append("</").append(Validation.Tag.BACKGROUNDVALIDATIONMILLIS).append(">");
      }

      if (useFastFail != null)
      {
         sb.append("<").append(Validation.Tag.USEFASTFAIL).append(">");
         sb.append(useFastFail);
         sb.append("</").append(Validation.Tag.USEFASTFAIL).append(">");
      }

      if (staleConnectionChecker != null)
      {
         sb.append("<").append(Validation.Tag.STALECONNECTIONCHECKER);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(staleConnectionChecker.getClassName()).append("\"");
         sb.append(">");

         if (staleConnectionChecker.getConfigPropertiesMap() != null &&
             staleConnectionChecker.getConfigPropertiesMap().size() > 0)
         {
            Iterator<Map.Entry<String, String>> it =
               staleConnectionChecker.getConfigPropertiesMap().entrySet().iterator();
            
            while (it.hasNext())
            {
               Map.Entry<String, String> entry = it.next();

               sb.append("<").append(Extension.Tag.CONFIG_PROPERTY);
               sb.append(" name=\"").append(entry.getKey()).append("\">");
               sb.append(entry.getValue());
               sb.append("</").append(Extension.Tag.CONFIG_PROPERTY).append(">");
            }
         }

         sb.append("</").append(Validation.Tag.STALECONNECTIONCHECKER).append(">");
      }

      if (exceptionSorter != null)
      {
         sb.append("<").append(Validation.Tag.EXCEPTIONSORTER);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(exceptionSorter.getClassName()).append("\"");
         sb.append(">");

         if (exceptionSorter.getConfigPropertiesMap() != null &&
             exceptionSorter.getConfigPropertiesMap().size() > 0)
         {
            Iterator<Map.Entry<String, String>> it =
               exceptionSorter.getConfigPropertiesMap().entrySet().iterator();
            
            while (it.hasNext())
            {
               Map.Entry<String, String> entry = it.next();

               sb.append("<").append(Extension.Tag.CONFIG_PROPERTY);
               sb.append(" name=\"").append(entry.getKey()).append("\">");
               sb.append(entry.getValue());
               sb.append("</").append(Extension.Tag.CONFIG_PROPERTY).append(">");
            }
         }

         sb.append("</").append(Validation.Tag.EXCEPTIONSORTER).append(">");
      }

      sb.append("</validation>");

      return sb.toString();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((checkValidConnectionSql == null) ? 0 : checkValidConnectionSql.hashCode());
      result = prime * result + ((exceptionSorter == null) ? 0 : exceptionSorter.hashCode());
      result = prime * result + ((staleConnectionChecker == null) ? 0 : staleConnectionChecker.hashCode());
      result = prime * result + ((validConnectionChecker == null) ? 0 : validConnectionChecker.hashCode());
      result = prime * result + ((validateOnMatch == null) ? 0 : validateOnMatch.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof ValidationImpl))
         return false;
      ValidationImpl other = (ValidationImpl) obj;
      if (checkValidConnectionSql == null)
      {
         if (other.checkValidConnectionSql != null)
            return false;
      }
      else if (!checkValidConnectionSql.equals(other.checkValidConnectionSql))
         return false;
      if (exceptionSorter == null)
      {
         if (other.exceptionSorter != null)
            return false;
      }
      else if (!exceptionSorter.equals(other.exceptionSorter))
         return false;
      if (staleConnectionChecker == null)
      {
         if (other.staleConnectionChecker != null)
            return false;
      }
      else if (!staleConnectionChecker.equals(other.staleConnectionChecker))
         return false;
      if (validConnectionChecker == null)
      {
         if (other.validConnectionChecker != null)
            return false;
      }
      else if (!validConnectionChecker.equals(other.validConnectionChecker))
         return false;
      if (validateOnMatch == null)
      {
         if (other.validateOnMatch != null)
            return false;
      }
      else if (!validateOnMatch.equals(other.validateOnMatch))
         return false;
      return true;
   }

}
