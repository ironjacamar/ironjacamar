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
package org.jboss.jca.common.api.metadata.common;

import org.jboss.jca.common.api.metadata.JCAMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * A ConnectionDefinition.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public interface CommonConnDef extends JCAMetadata
{

   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   public Map<String, String> getConfigProperties();

   /**
    * Get the className.
    *
    * @return the className.
    */
   public String getClassName();

   /**
    * Get the jndiName.
    *
    * @return the jndiName.
    */
   public String getJndiName();

   /**
    * Get the poolName.
    *
    * @return the poolName.
    */
   public String getPoolName();

   /**
    * Get the enabled.
    *
    * @return the enabled.
    */
   public Boolean isEnabled();

   /**
    * Get the useJavaContext.
    *
    * @return the useJavaContext.
    */
   public Boolean isUseJavaContext();

   /**
    * Get the useCcm.
    *
    * @return the useCcm.
    */
   public Boolean isUseCcm();

   /**
    * Get the sharable
    *
    * @return the value
    */
   public Boolean isSharable();

   /**
    * Get the pool.
    *
    * @return the pool.
    */
   public CommonPool getPool();

   /**
    * Get the timeOut.
    *
    * @return the timeOut.
    */
   public CommonTimeOut getTimeOut();

   /**
    * Get the validation.
    *
    * @return the validation.
    */
   public CommonValidation getValidation();

   /**
    * Get the security.
    *
    * @return the security.
    */
   public CommonSecurity getSecurity();

   /**
    * Return true if this connectionDefnition have defined an XaPool
    *
    * @return true if this connectionDefnition have defined an XaPool
    */

   public Boolean isXa();

   /**
    * Get the recovery settings.
    *
    * @return the recovery settings.
    */
   public Recovery getRecovery();

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
       * config-property tag
       */
      CONFIG_PROPERTY("config-property"),
      /**
       * pool tag
       */
      XA_POOL("xa-pool"),
      /**
       * pool tag
       */
      POOL("pool"),
      /**
       * security tag
       */
      SECURITY("security"),
      /**
       * timeout tag
       */
      TIMEOUT("timeout"),
      /**
       * validation tag
       */
      VALIDATION("validation"),

      /** recovery tag */
      RECOVERY("recovery");

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

   /**
   *
   * A Attribute.
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   *
   */
   public enum Attribute
   {

      /** always first
      *
      */
      UNKNOWN(null),
      /** jndiName attribute
        *
        */
      JNDI_NAME("jndi-name"),

      /** class-name attribute
      *
      */
      CLASS_NAME("class-name"),

      /** pool-name attribute
      *
      */
      POOL_NAME("pool-name"),

      /** enabled attribute
      *
      */
      ENABLED("enabled"),
      /** use-java-context attribute
      *
      */
      USE_JAVA_CONTEXT("use-java-context"),
      /** use-ccm attribute
      *
      */
      USE_CCM("use-ccm"),
      /** sharable attribute
      *
      */
      SHARABLE("sharable");

      private final String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Attribute(final String name)
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

      private static final Map<String, Attribute> MAP;

      static
      {
         final Map<String, Attribute> map = new HashMap<String, Attribute>();
         for (Attribute element : values())
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
      public static Attribute forName(String localName)
      {
         final Attribute element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
      }

   }
}
