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

import org.jboss.jca.common.api.metadata.ds.Validation;


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

   private final String validConnectionCheckerClassName;

   private final String checkValidConnectionSql;

   private final Boolean validateOnMatch;

   private final String staleConnectionCheckerClassName;

   private final String exceptionSorterClassName;

   /**
    * Create a new ValidationImpl.
    *
    * @param backgroundValidation backgroundValidation
    * @param backgroundValidationMinutes backgroundValidationMinutes
    * @param useFastFail useFastFail
    * @param validConnectionCheckerClassName validConnectionCheckerClassName
    * @param checkValidConnectionSql checkValidConnectionSql
    * @param validateOnMatch validateOnMatch
    * @param staleConnectionCheckerClassName staleConnectionCheckerClassName
    * @param exceptionSorterClassName exceptionSorterClassName
    */
   public ValidationImpl(Boolean backgroundValidation, Long backgroundValidationMinutes, Boolean useFastFail,
      String validConnectionCheckerClassName, String checkValidConnectionSql, Boolean validateOnMatch,
      String staleConnectionCheckerClassName, String exceptionSorterClassName)
   {
      super(backgroundValidation, backgroundValidationMinutes, useFastFail);
      this.validConnectionCheckerClassName = validConnectionCheckerClassName;
      this.checkValidConnectionSql = checkValidConnectionSql;
      this.validateOnMatch = validateOnMatch;
      this.staleConnectionCheckerClassName = staleConnectionCheckerClassName;
      this.exceptionSorterClassName = exceptionSorterClassName;
   }

   /**
    * Get the validConnectionCheckerClassName.
    *
    * @return the validConnectionCheckerClassName.
    */
   @Override
   public final String getValidConnectionCheckerClassName()
   {
      return validConnectionCheckerClassName;
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
    * Get the staleConnectionCheckerClassName.
    *
    * @return the staleConnectionCheckerClassName.
    */
   @Override
   public final String getStaleConnectionCheckerClassName()
   {
      return staleConnectionCheckerClassName;
   }

   /**
    * Get the exceptionSorterClassName.
    *
    * @return the exceptionSorterClassName.
    */
   @Override
   public final String getExceptionSorterClassName()
   {
      return exceptionSorterClassName;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((checkValidConnectionSql == null) ? 0 : checkValidConnectionSql.hashCode());
      result = prime * result + ((exceptionSorterClassName == null) ? 0 : exceptionSorterClassName.hashCode());
      result = prime * result +
               ((staleConnectionCheckerClassName == null) ? 0 : staleConnectionCheckerClassName.hashCode());
      result = prime * result +
               ((validConnectionCheckerClassName == null) ? 0 : validConnectionCheckerClassName.hashCode());
      result = prime * result + (validateOnMatch ? 1231 : 1237);
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
      if (exceptionSorterClassName == null)
      {
         if (other.exceptionSorterClassName != null)
            return false;
      }
      else if (!exceptionSorterClassName.equals(other.exceptionSorterClassName))
         return false;
      if (staleConnectionCheckerClassName == null)
      {
         if (other.staleConnectionCheckerClassName != null)
            return false;
      }
      else if (!staleConnectionCheckerClassName.equals(other.staleConnectionCheckerClassName))
         return false;
      if (validConnectionCheckerClassName == null)
      {
         if (other.validConnectionCheckerClassName != null)
            return false;
      }
      else if (!validConnectionCheckerClassName.equals(other.validConnectionCheckerClassName))
         return false;
      if (validateOnMatch != other.validateOnMatch)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "ValidationImpl [validConnectionCheckerClassName=" + validConnectionCheckerClassName +
             ", checkValidConnectionSql=" + checkValidConnectionSql + ", validateOnMatch=" + validateOnMatch +
             ", staleConnectionCheckerClassName=" + staleConnectionCheckerClassName +
             ", exceptionSorterClassName=" + exceptionSorterClassName + ", backgroundValidation=" +
             backgroundValidation + ", backgroundValidationMinutes=" + backgroundValidationMinutes +
             ", useFastFail=" + useFastFail + "]";
   }
}

