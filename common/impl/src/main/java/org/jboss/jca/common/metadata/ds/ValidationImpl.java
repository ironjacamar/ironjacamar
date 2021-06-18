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
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class ValidationImpl extends org.jboss.jca.common.metadata.common.ValidationImpl implements Validation
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private Extension validConnectionChecker;

   private String checkValidConnectionSql;

   private Extension staleConnectionChecker;

   private Extension exceptionSorter;

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
      super(validateOnMatch, backgroundValidation, backgroundValidationMillis, useFastFail);
      this.validConnectionChecker = validConnectionChecker;
      this.checkValidConnectionSql = checkValidConnectionSql;
      this.staleConnectionChecker = staleConnectionChecker;
      this.exceptionSorter = exceptionSorter;
      this.validate();
   }

   /**
    * {@inheritDoc}
    */
   public String getCheckValidConnectionSql()
   {
      return checkValidConnectionSql;
   }

   /**
    * {@inheritDoc}
    */
   public Extension getValidConnectionChecker()
   {
      return validConnectionChecker;
   }

   /**
    * {@inheritDoc}
    */
   public Extension getStaleConnectionChecker()
   {
      return staleConnectionChecker;
   }

   /**
    * {@inheritDoc}
    */
   public Extension getExceptionSorter()
   {
      return exceptionSorter;
   }

   /**
    * {@inheritDoc}
    */
   public void validate() throws ValidateException
   {
      if (this.backgroundValidationMillis != null && this.backgroundValidationMillis < 0)
      {
         throw new
            ValidateException(bundle.invalidNegative(Validation.Tag.BACKGROUND_VALIDATION_MILLIS.getLocalName()));
      }

      if (this.validConnectionChecker != null)
      {
         try
         {
            this.validConnectionChecker.validate();
         }
         catch (ValidateException ve)
         {
            throw new ValidateException(bundle.invalidTag(Validation.Tag.VALID_CONNECTION_CHECKER.getLocalName()), ve);
         }
      }
      if (this.exceptionSorter != null)
      {
         try
         {
            this.exceptionSorter.validate();
         }
         catch (ValidateException ve)
         {
            throw new ValidateException(bundle.invalidTag(Validation.Tag.EXCEPTION_SORTER.getLocalName()), ve);
         }
      }
      if (this.staleConnectionChecker != null)
      {
         try
         {
            this.staleConnectionChecker.validate();
         }
         catch (ValidateException ve)
         {
            throw new ValidateException(bundle.invalidTag(Validation.Tag.STALE_CONNECTION_CHECKER.getLocalName()), ve);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<validation>");

      if (validConnectionChecker != null)
      {
         sb.append("<").append(Validation.Tag.VALID_CONNECTION_CHECKER);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(validConnectionChecker.getClassName()).append("\"");
         sb.append(">");

         if (validConnectionChecker.getConfigPropertiesMap().size() > 0)
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

         sb.append("</").append(Validation.Tag.VALID_CONNECTION_CHECKER).append(">");
      }

      if (checkValidConnectionSql != null)
      {
         sb.append("<").append(Validation.Tag.CHECK_VALID_CONNECTION_SQL).append(">");
         sb.append(checkValidConnectionSql);
         sb.append("</").append(Validation.Tag.CHECK_VALID_CONNECTION_SQL).append(">");
      }

      if (validateOnMatch != null)
      {
         sb.append("<").append(Validation.Tag.VALIDATE_ON_MATCH).append(">");
         sb.append(validateOnMatch);
         sb.append("</").append(Validation.Tag.VALIDATE_ON_MATCH).append(">");
      }

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

      if (staleConnectionChecker != null)
      {
         sb.append("<").append(Validation.Tag.STALE_CONNECTION_CHECKER);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(staleConnectionChecker.getClassName()).append("\"");
         sb.append(">");

         if (staleConnectionChecker.getConfigPropertiesMap().size() > 0)
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

         sb.append("</").append(Validation.Tag.STALE_CONNECTION_CHECKER).append(">");
      }

      if (exceptionSorter != null)
      {
         sb.append("<").append(Validation.Tag.EXCEPTION_SORTER);
         sb.append(" ").append(Extension.Attribute.CLASS_NAME).append("=\"");
         sb.append(exceptionSorter.getClassName()).append("\"");
         sb.append(">");

         if (exceptionSorter.getConfigPropertiesMap().size() > 0)
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

         sb.append("</").append(Validation.Tag.EXCEPTION_SORTER).append(">");
      }

      sb.append("</validation>");

      return sb.toString();
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((checkValidConnectionSql == null) ? 0 : checkValidConnectionSql.hashCode());
      result = prime * result + ((exceptionSorter == null) ? 0 : exceptionSorter.hashCode());
      result = prime * result + ((staleConnectionChecker == null) ? 0 : staleConnectionChecker.hashCode());
      result = prime * result + ((validConnectionChecker == null) ? 0 : validConnectionChecker.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
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
      return true;
   }

}
