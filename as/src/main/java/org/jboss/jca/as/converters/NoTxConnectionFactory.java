/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.as.converters;

import java.util.HashMap;
import java.util.Map;

/**
 * No Tx ConnectionFactory
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public interface NoTxConnectionFactory extends ConnectionFactory
{
   /**
    * Get the rarName.
    *
    * @return the rarName.
    */
   public String getRarName();

   /**
    *
    * get the ConnectionDefinition
    *
    * @return the ConnectionDefinition
    */
   public String getConnectionDefinition();


   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   public Map<String, String> getConfigProperties();
   
   /**
   *
   * A Tag.
   */
   public enum Tag 
   {
      /**
       * always first
       */
      UNKNOWN(null),
      
      //more by ConnectionFactory
      /**
       * rar-name tag
       */
      RAR_NAME("rar-name"),
      /**
       * connection-definition tag
       */
      CONNECTION_DEFINITION("connection-definition"),
      /**
       * config-property tag
       */
      CONFIG_PROPERTY("config-property"),
      
      /**
       * jndiName tag
       */
      JNDI_NAME("jndi-name"),
      /**
       * security-domain tag
       */
      SECURITY_DOMAIN("security-domain"),
      /**
       * security-domain-and-application tag
       */
      SECURITY_DOMAIN_AND_APPLICATION("security-domain-and-application"),
      /**
       * min-pool-size tag
       */
      MIN_POOL_SIZE("min-pool-size"),
      /**
      * maxPoolSize tag
      */
      MAX_POOL_SIZE("max-pool-size"),
      /**
       * blockingTimeoutMillis tag
       */
      BLOCKING_TIMEOUT_MILLIS("blocking-timeout-millis"),
      /**
       * backgroundValidation tag
       */
      BACKGROUND_VALIDATION("background-validation"),
      /**
      * backgroundValidationMillis tag
      */
      BACKGROUND_VALIDATION_MILLIS("background-validation-millis"),
      /**
      * idleTimeoutMinutes tag
      */
      IDLE_TIMEOUT_MINUTES("idle-timeout-minutes"),
      /**
       * allocationRetry tag
       */
      ALLOCATION_RETRY("allocation-retry"),
      /**
      * allocationRetryWaitMillis tag
      */
      ALLOCATION_RETRY_WAIT_MILLIS("allocation-retry-wait-millis"),
      /**
      * prefill tag
      */
      PREFILL("prefill"),
      /**
       * useFastFail tag
       */
      USE_FAST_FAIL("use-fast-fail");

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
