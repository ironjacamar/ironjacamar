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
package org.jboss.jca.common.api.metadata.ds;


import org.jboss.jca.common.api.metadata.ValidatableMetadata;
import org.jboss.jca.common.api.metadata.common.Extension;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * A Validation.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public interface Validation extends org.jboss.jca.common.api.metadata.common.CommonValidation, ValidatableMetadata
{

   /**
    * Get the validConnectionChecker
    *
    * @return the validConnectionChecker
    */
   public Extension getValidConnectionChecker();

   /**
    * Get the checkValidConnectionSql.
    *
    * @return the checkValidConnectionSql.
    */
   public String getCheckValidConnectionSql();

   /**
    * Get the validateOnMatch.
    *
    * @return the validateOnMatch.
    */
   public Boolean isValidateOnMatch();

   /**
    * Get the staleConnectionChecker
    *
    * @return the staleConnectionChecker
    */
   public Extension getStaleConnectionChecker();

   /**
    * Get the exceptionSorter
    *
    * @return the exceptionSorter
    */
   public Extension getExceptionSorter();


   /**
   *
   * A Tag.
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   *
   */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
      * validConnectionCheckerClassName tag
      */
      VALID_CONNECTION_CHECKER("valid-connection-checker"),

      /**
      * checkValidConnectionSql tag
      */
      CHECK_VALID_CONNECTION_SQL("check-valid-connection-sql"),
      /**
      * validateOnMatch tag
      */
      VALIDATE_ON_MATCH("validate-on-match"),
      /**
      * backgroundValidation tag
      */
      BACKGROUND_VALIDATION("background-validation"),
      /**
      * backgroundValidationMillis tag
      */
      BACKGROUND_VALIDATION_MILLIS("background-validation-millis"),
      /**
      * useFastFail tag
      */
      USE_FAST_FAIL("use-fast-fail"),
      /**
      * staleConnectionCheckerClassName tag
      */
      STALE_CONNECTION_CHECKER("stale-connection-checker"),
      /**
      * exceptionSorterClassName tag
      */
      EXCEPTION_SORTER("exception-sorter");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Tag(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }

      /**
       * {@inheritDoc}
       */
      public String toString()
      {
         return name;
      }

      private static final Map<String, Tag> MAP;

      static
      {
         final Map<String, Tag> map = new HashMap<String, Tag>();
         for (Tag element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAP = map;
      }

      /**
       * Set the value
       * @param v The name
       * @return The value
       */
      Tag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName XsdString
      *
      * @param localName a XsdString used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

}
