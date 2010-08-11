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

import org.jboss.jca.common.api.metadata.JCAMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * A ValidationSettings.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public interface ValidationSettings extends JCAMetadata
{

   /**
    * Get the validConnectionCheckerClassName.
    *
    * @return the validConnectionCheckerClassName.
    */
   public String getValidConnectionCheckerClassName();

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
   public boolean isValidateOnMatch();

   /**
    * Get the backgroundValidation.
    *
    * @return the backgroundValidation.
    */
   public boolean isBackgroundValidation();

   /**
    * Get the backgroundValidationMinutes.
    *
    * @return the backgroundValidationMinutes.
    */
   public Long getBackgroundValidationMinutes();

   /**
    * Get the useFastFail.
    *
    * @return the useFastFail.
    */
   public boolean isUseFastFail();

   /**
    * Get the staleConnectionCheckerClassName.
    *
    * @return the staleConnectionCheckerClassName.
    */
   public String getStaleConnectionCheckerClassName();

   /**
    * Get the exceptionSorterClassName.
    *
    * @return the exceptionSorterClassName.
    */
   public String getExceptionSorterClassName();

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
      VALIDCONNECTIONCHECKERCLASSNAME("valid-connection-checker-className"),
      /**
      * checkValidConnectionSql tag
      */
      CHECKVALIDCONNECTIONSQL("check-valid-connection-sql"),
      /**
      * validateOnMatch tag
      */
      VALIDATEONMATCH("validate-on-match"),
      /**
      * backgroundValidation tag
      */
      BACKGROUNDVALIDATION("background-validation"),
      /**
      * backgroundValidationMinutes tag
      */
      BACKGROUNDVALIDATIONMINUTES("background-validation-minutes"),
      /**
      * useFastFail tag
      */
      USEFASTFAIL("useFastFail"),
      /**
      * staleConnectionCheckerClassName tag
      */
      STALECONNECTIONCHECKERCLASSNAME("stale-connection-checker-class-name"),
      /**
      * exceptionSorterClassName tag
      */
      EXCEPTIONSORTERCLASSNAME("exception-sorter-class-name");

      private final String name;

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
      *
      * Static method to get enum instance given localName XsdString
      *
      * @param localName a XsdString used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
      }

   }

}
