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
package org.jboss.jca.common.api.metadata.resourceadapter;


import org.jboss.jca.common.api.metadata.JCAMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * A NoTxConnectionFactory.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public interface NoTxConnectionFactory extends JCAMetadata
{

   /**
    * Get the minPoolSize.
    *
    * @return the minPoolSize.
    */
   public Integer getMinPoolSize();

   /**
    * Get the maxPoolSize.
    *
    * @return the maxPoolSize.
    */
   public Integer getMaxPoolSize();

   /**
    * Get the prefill.
    *
    * @return the prefill.
    */
   public boolean isPrefill();

   /**
    * Get the userName.
    *
    * @return the userName.
    */
   public String getUserName();

   /**
    * Get the password.
    *
    * @return the password.
    */
   public String getPassword();

   /**
    * Get the connectionDefinition.
    *
    * @return the connectionDefinition.
    */
   public String getConnectionDefinition();

   /**
    * Get the configProperty.
    *
    * @return the configProperty.
    */
   public HashMap<String, String> getConfigProperty();

   /**
    * Get the security.
    *
    * @return the security.
    */
   public Security getSecurity();

   /**
    * Get the timeOut.
    *
    * @return the timeOut.
    */
   public TimeOut getTimeOut();

   /**
    * Get the validation.
    *
    * @return the validation.
    */
   public Validation getValidation();

   /**
    * Get the poolName.
    *
    * @return the poolName.
    */
   public String getPoolName();

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
    * Get the enabled.
    *
    * @return the enabled.
    */
   public boolean isEnabled();

   /**
    * Get the useJavaContext.
    *
    * @return the useJavaContext.
    */
   public boolean isUseJavaContext();

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
      minPoolSize tag
      */
      MINPOOLSIZE("min-pool-size"),
      /**
      maxPoolSize tag
      */
      MAXPOOLSIZE("max-pool-size"),
      /**
      prefill tag
      */
      PREFILL("prefill"),
      /**
      userName tag
      */
      USERNAME("user-name"),
      /**
      password tag
      */
      PASSWORD("password"),
      /**
      connectionDefinition tag
      */
      CONNECTIONDEFINITION("connection-definition"),
      /**
      configProperty tag
      */
      CONFIGPROPERTY("config-property"),
      /**
      security tag
      */
      SECURITY("security"),
      /**
      timeOut tag
      */
      TIMEOUT("time-out"),
      /**
      validation tag
      */
      VALIDATION("validation");

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

   /**
    *
    * A Attribute.
    *
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
    *
    */
   public enum Attribute
   {

      /** jndiName attribute
       *
       */
      JNDINAME("jndiName"),

      /** class-name attribute
      *
      */
      CLASS_NAME("class-name"),

      /** enabled attribute
      *
      */
      ENABLED("enabled"),
      /** use-java-context attribute
      *
      */
      USEJAVACONTEXT("use-java-context");

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

   }
}
