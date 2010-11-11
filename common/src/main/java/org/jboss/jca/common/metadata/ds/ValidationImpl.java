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

import org.jboss.jca.common.api.metadata.ds.JdbcAdapterExtension;
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
   @Override
   public String toString()
   {
      return "ValidationImpl [validConnectionChecker=" + validConnectionChecker + ", checkValidConnectionSql=" +
             checkValidConnectionSql + ", validateOnMatch=" + validateOnMatch + ", staleConnectionChecker=" +
             staleConnectionChecker + ", exceptionSorter=" + exceptionSorter + "]";
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

   /** The serialVersionUID */
   private static final long serialVersionUID = 7816717816552118419L;

   private final JdbcAdapterExtension validConnectionChecker;

   private final String checkValidConnectionSql;

   private final Boolean validateOnMatch;

   private final JdbcAdapterExtension staleConnectionChecker;

   private final JdbcAdapterExtension exceptionSorter;

   /**
    * Create a new ValidationImpl.
    *
    * @param backgroundValidation backgroundValidation
    * @param backgroundValidationMinutes backgroundValidationMinutes
    * @param useFastFail useFastFail
    * @param validConnectionChecker validConnectionChecker
    * @param checkValidConnectionSql checkValidConnectionSql
    * @param validateOnMatch validateOnMatch
    * @param staleConnectionChecker staleConnectionChecker
    * @param exceptionSorter exceptionSorter
    */
   public ValidationImpl(Boolean backgroundValidation, Long backgroundValidationMinutes, Boolean useFastFail,
      JdbcAdapterExtension validConnectionChecker, String checkValidConnectionSql, Boolean validateOnMatch,
      JdbcAdapterExtension staleConnectionChecker, JdbcAdapterExtension exceptionSorter)
   {
      super(backgroundValidation, backgroundValidationMinutes, useFastFail);
      this.validConnectionChecker = validConnectionChecker;
      this.checkValidConnectionSql = checkValidConnectionSql;
      this.validateOnMatch = validateOnMatch;
      this.staleConnectionChecker = staleConnectionChecker;
      this.exceptionSorter = exceptionSorter;
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
   public final JdbcAdapterExtension getValidConnectionChecker()
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
   public final JdbcAdapterExtension getStaleConnectionChecker()
   {
      return staleConnectionChecker;
   }

   /**
    * Get the exceptionSorter.
    *
    * @return the exceptionSorter.
    */
   public final JdbcAdapterExtension getExceptionSorter()
   {
      return exceptionSorter;
   }

}

