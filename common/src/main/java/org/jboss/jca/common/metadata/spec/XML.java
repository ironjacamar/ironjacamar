/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.spec;

import java.util.HashMap;
import java.util.Map;

/**
 * XML elements / attributes for ra.xml
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 *
 */
public interface XML
{

   // Connector10

   /**
    * A Connector10Tag.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    */
   public enum Connector10Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * vendor-name
       */
      VENDOR_NAME("vendor-name"),

      /**
       * eis-type
       */
      EIS_TYPE("eis-type"),

      /**
       * license
       */
      LICENSE("license"),

      /**
       * resourceadapter
       */
      RESOURCEADAPTER("resourceadapter"),

      /**
       * spec-version
       */
      SPEC_VERSION("spec-version"),

      /**
       * version
       */
      VERSION("version"),

      /**
       * description
       */
      DESCRIPTION("description"),

      /**
       * icon
       */
      ICON("icon"),

      /**
       * display-name
       */
      DISPLAY_NAME("display-name");

      private String name;

      /**
       *
       * Create a new Connector10Tag.
       *
       * @param name a name
       */
      Connector10Tag(final String name)
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

      private static final Map<String, Connector10Tag> MAP;

      static
      {
         final Map<String, Connector10Tag> map = new HashMap<String, Connector10Tag>();
         for (Connector10Tag element : values())
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
      Connector10Tag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Connector10Tag forName(String localName)
      {
         final Connector10Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }

   /**
    * An attribute
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    */
   public enum Connector10Attribute
   {
      /** unknown attribute
       *
       */
      UNKNOWN(null),

      /** id attribute
       *
       */
      ID("id"),

      /**
       * version attribute
       */
      VERSION("version");

      private String name;

      /**
       *
       * Create a new.
       *
       * @param name a name
       */
      Connector10Attribute(final String name)
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

      private static final Map<String, Connector10Attribute> MAP;

      static
      {
         final Map<String, Connector10Attribute> map = new HashMap<String, Connector10Attribute>();
         for (Connector10Attribute element : values())
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
      Connector10Attribute value(String v)
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
      public static Connector10Attribute forName(String localName)
      {
         final Connector10Attribute element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }

   /**
    *
    * A ResourceAdapter10Tag.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   public enum ResourceAdapter10Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * managedconnectionfactory-class TAG
       */
      MANAGEDCONNECTIONFACTORY_CLASS("managedconnectionfactory-class"),

      /**
       * config-property TAG
       */
      CONFIG_PROPERTY("config-property"),

      /**
       * connectionfactory-interface TAG
       */
      CONNECTIONFACTORY_INTERFACE("connectionfactory-interface"),

      /**
       * connectionfactory-impl-class TAG
       */
      CONNECTIONFACTORY_IMPL_CLASS("connectionfactory-impl-class"),

      /**
       * connection-interface TAG
       */
      CONNECTION_INTERFACE("connection-interface"),

      /**
       * connection-impl-class TAG
       */
      CONNECTION_IMPL_CLASS("connection-impl-class"),

      /**
       * transaction-support TAG
       */
      TRANSACTION_SUPPORT("transaction-support"),

      /**
       * authentication-mechanism TAG
       */
      AUTHENTICATION_MECHANISM("authentication-mechanism"),
      /**
       * security-permission TAG
       */
      SECURITY_PERMISSION("security-permission"),
      /**
       * reauthentication-support TAG
       */
      REAUTHENTICATION_SUPPORT("reauthentication-support");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      ResourceAdapter10Tag(final String name)
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

      private static final Map<String, ResourceAdapter10Tag> MAP;

      static
      {
         final Map<String, ResourceAdapter10Tag> map = new HashMap<String, ResourceAdapter10Tag>();
         for (ResourceAdapter10Tag element : values())
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
      ResourceAdapter10Tag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static ResourceAdapter10Tag forName(String localName)
      {
         final ResourceAdapter10Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

   // Connector15
   /**
   *
   * A Connector15Tag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum Connector15Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * vendor-name tag
       */
      VENDOR_NAME("vendor-name"),

      /**
       * eis-type tag
       */
      EIS_TYPE("eis-type"),

      /**
       * resourceadapter-version tag
       */
      RESOURCEADAPTER_VERSION("resourceadapter-version"),

      /**
       * license tag
       */
      LICENSE("license"),

      /**
       * resourceadapter tag
       */
      RESOURCEADAPTER("resourceadapter"),
      /**
       * description tag
       */
      DESCRIPTION("description"),

      /**
       * icon tag
       */
      ICON("icon"),

      /**
       * display-name tag
       */
      DISPLAY_NAME("display-name");

      private String name;

      /**
       *
       * Create a new Connector15Tag.
       *
       * @param name a name
       */
      Connector15Tag(final String name)
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

      private static final Map<String, Connector15Tag> MAP;

      static
      {
         final Map<String, Connector15Tag> map = new HashMap<String, Connector15Tag>();
         for (Connector15Tag element : values())
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
      Connector15Tag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Connector15Tag forName(String localName)
      {
         final Connector15Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

   /**
    *
    * A Connector15Attribute.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   public enum Connector15Attribute
   {
      /** unknown attribute
       *
       */
      UNKNOWN(null),

      /** id attribute
       *
       */
      ID("id"),

      /**
       * version attribute
       */
      VERSION("version");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Connector15Attribute(final String name)
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

      private static final Map<String, Connector15Attribute> MAP;

      static
      {
         final Map<String, Connector15Attribute> map = new HashMap<String, Connector15Attribute>();
         for (Connector15Attribute element : values())
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
      Connector15Attribute value(String v)
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
      public static Connector15Attribute forName(String localName)
      {
         final Connector15Attribute element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }

   // Connector16
   /**
   *
   * A Connector16Tag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum Connector16Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * module-name tag
       */
      MODULE_NAME("module-name"),

      /**
       * vendor-name tag
       */
      VENDOR_NAME("vendor-name"),

      /**
       * eis-type tag
       */
      EIS_TYPE("eis-type"),

      /**
       * resourceadapter-version tag
       */
      RESOURCEADAPTER_VERSION("resourceadapter-version"),

      /**
       * license tag
       */
      LICENSE("license"),

      /**
       * resourceadapter tag
       */
      RESOURCEADAPTER("resourceadapter"),

      /**
       * required-work-context tag
       */
      REQUIRED_WORK_CONTEXT("required-work-context"),

      /**
       * description tag
       */
      DESCRIPTION("description"),

      /**
       * icon tag
       */
      ICON("icon"),

      /**
       * display-name tag
       */
      DISPLAY_NAME("display-name");

      private String name;

      /**
       *
       * Create a new Connector16Tag.
       *
       * @param name a name
       */
      Connector16Tag(final String name)
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

      private static final Map<String, Connector16Tag> MAP;

      static
      {
         final Map<String, Connector16Tag> map = new HashMap<String, Connector16Tag>();
         for (Connector16Tag element : values())
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
      Connector16Tag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Connector16Tag forName(String localName)
      {
         final Connector16Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

   /**
    *
    * A Connector16Attribute.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   public enum Connector16Attribute
   {
      /** unknown attribute
       *
       */
      UNKNOWN(null),

      /** id attribute
       *
       */
      ID("id"),

      /**
       * metadata-complete attribute
       */
      METADATA_COMPLETE("metadata-complete"),

      /**
       * version attribute
       */
      VERSION("version");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Connector16Attribute(final String name)
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

      private static final Map<String, Connector16Attribute> MAP;

      static
      {
         final Map<String, Connector16Attribute> map = new HashMap<String, Connector16Attribute>();
         for (Connector16Attribute element : values())
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
      Connector16Attribute value(String v)
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
      public static Connector16Attribute forName(String localName)
      {
         final Connector16Attribute element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }

   // Connector17
   /**
    * A Connector17Tag.
    *
    * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
    */
   public enum Connector17Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * module-name tag
       */
      MODULE_NAME("module-name"),

      /**
       * vendor-name tag
       */
      VENDOR_NAME("vendor-name"),

      /**
       * eis-type tag
       */
      EIS_TYPE("eis-type"),

      /**
       * resourceadapter-version tag
       */
      RESOURCEADAPTER_VERSION("resourceadapter-version"),

      /**
       * license tag
       */
      LICENSE("license"),

      /**
       * resourceadapter tag
       */
      RESOURCEADAPTER("resourceadapter"),

      /**
       * required-work-context tag
       */
      REQUIRED_WORK_CONTEXT("required-work-context"),

      /**
       * description tag
       */
      DESCRIPTION("description"),

      /**
       * icon tag
       */
      ICON("icon"),

      /**
       * display-name tag
       */
      DISPLAY_NAME("display-name");

      private String name;

      /**
       *
       * Create a new Connector17Tag.
       *
       * @param name a name
       */
      Connector17Tag(final String name)
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

      private static final Map<String, Connector17Tag> MAP;

      static
      {
         final Map<String, Connector17Tag> map = new HashMap<String, Connector17Tag>();
         for (Connector17Tag element : values())
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
      Connector17Tag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Connector17Tag forName(String localName)
      {
         final Connector17Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

   /**
    * An attribute
    *
    * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
    */
   public enum Connector17Attribute
   {
      /** unknown attribute
       *
       */
      UNKNOWN(null),

      /** id attribute
       *
       */
      ID("id"),

      /**
       * metadata-complete attribute
       */
      METADATA_COMPLETE("metadata-complete"),

      /**
       * version attribute
       */
      VERSION("version");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Connector17Attribute(final String name)
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

      private static final Map<String, Connector17Attribute> MAP;

      static
      {
         final Map<String, Connector17Attribute> map = new HashMap<String, Connector17Attribute>();
         for (Connector17Attribute element : values())
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
      Connector17Attribute value(String v)
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
      public static Connector17Attribute forName(String localName)
      {
         final Connector17Attribute element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }

   // ResourceAdapter
   /**
   *
   * A ResourceAdapterTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum ResourceAdapterTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * resourceadapter-class tag
       */
      RESOURCEADAPTER_CLASS("resourceadapter-class"),

      /**
       * config-property tag
       */
      CONFIG_PROPERTY("config-property"),

      /**
       * outbound-resourceadapte TAG
       */
      OUTBOUND_RESOURCEADAPTER("outbound-resourceadapter"),

      /**
       * inbound-resourceadapter TAG
       */
      INBOUND_RESOURCEADAPTER("inbound-resourceadapter"),

      /**
       * adminobject TAG
       */
      ADMINOBJECT("adminobject"),

      /**
       * security-permission TAG
       */
      SECURITY_PERMISSION("security-permission");

      private String name;

      /**
       *
       * Create a new ResourceAdapterTag.
       *
       * @param name a name
       */
      ResourceAdapterTag(final String name)
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

      private static final Map<String, ResourceAdapterTag> MAP;

      static
      {
         final Map<String, ResourceAdapterTag> map = new HashMap<String, ResourceAdapterTag>();
         for (ResourceAdapterTag element : values())
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
      ResourceAdapterTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static ResourceAdapterTag forName(String localName)
      {
         final ResourceAdapterTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

   /**
    *
    * An IdAttribute
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   public enum IdAttribute
   {
      /** unknown attribute
       *
       */
      UNKNOWN(null),

      /** id attribute
       *
       */
      ID("id");

      private String name;

      /**
       * Constructor
       * @param name a name
       */
      IdAttribute(final String name)
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

      private static final Map<String, IdAttribute> MAP;

      static
      {
         final Map<String, IdAttribute> map = new HashMap<String, IdAttribute>();
         for (IdAttribute element : values())
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
      IdAttribute value(String v)
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
      public static IdAttribute forName(String localName)
      {
         final IdAttribute element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }

   // ConfigProperty
   /**
   *
   * A ConfigPropertyTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum ConfigPropertyTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * description-class tag
       */
      DESCRIPTION("description"),

      /**
       * config-property-name TAG
       */
      CONFIG_PROPERTY_NAME("config-property-name"),

      /**
       * config-property-type TAG
       */
      CONFIG_PROPERTY_TYPE("config-property-type"),

      /**
       * config-property-value TAG
       */
      CONFIG_PROPERTY_VALUE("config-property-value"),

      /**
       * config-property-ignore TAG
       */
      CONFIG_PROPERTY_IGNORE("config-property-ignore"),

      /**
       * config-property-supports-dynamic-updates TAG
       */
      CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE("config-property-supports-dynamic-updates"),

      /**
       * config-property-confidential TAG
       */
      CONFIG_PROPERTY_CONFIDENTIAL("config-property-confidential");

      private String name;

      /**
       *
       * Create a new ConfigPropertyTag.
       *
       * @param name a name
       */
      ConfigPropertyTag(final String name)
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

      private static final Map<String, ConfigPropertyTag> MAP;

      static
      {
         final Map<String, ConfigPropertyTag> map = new HashMap<String, ConfigPropertyTag>();
         for (ConfigPropertyTag element : values())
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
      ConfigPropertyTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static ConfigPropertyTag forName(String localName)
      {
         final ConfigPropertyTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }


   // ActivationSpec
   /**
    *
    * A ActivationSpecTag.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   public enum ActivationSpecTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * activationspec-class TAG
       */
      ACTIVATIONSPEC_CLASS("activationspec-class"),

      /**
       * required-config-property TAG
       */
      REQUIRED_CONFIG_PROPERTY("required-config-property"),

      /**
       * config-property TAG
       */
      CONFIG_PROPERTY("config-property");

      private String name;

      /**
       *
       * Create a new ActivationSpecTag.
       *
       * @param name a name
       */
      ActivationSpecTag(final String name)
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

      private static final Map<String, ActivationSpecTag> MAP;

      static
      {
         final Map<String, ActivationSpecTag> map = new HashMap<String, ActivationSpecTag>();
         for (ActivationSpecTag element : values())
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
      ActivationSpecTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static ActivationSpecTag forName(String localName)
      {
         final ActivationSpecTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }


   // AdminObject

   /**
   *
   * A AdminObjectTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum AdminObjectTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * adminobject-interface TAG
       */
      ADMINOBJECT_INTERFACE("adminobject-interface"),

      /**
       * adminobject-class TAG
       */
      ADMINOBJECT_CLASS("adminobject-class"),

      /**
       * config-property TAG
       */
      CONFIG_PROPERTY("config-property");

      private String name;

      /**
       *
       * Create a new AdminObjectTag.
       *
       * @param name a name
       */
      AdminObjectTag(final String name)
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

      private static final Map<String, AdminObjectTag> MAP;

      static
      {
         final Map<String, AdminObjectTag> map = new HashMap<String, AdminObjectTag>();
         for (AdminObjectTag element : values())
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
      AdminObjectTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static AdminObjectTag forName(String localName)
      {
         final AdminObjectTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }


   // SecurityPermission

   /**
   *
   * A SecurityPermissionTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum SecurityPermissionTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * description-class tag
       */
      DESCRIPTION("description"),

      /**
       * security-permission-spec tag
       */
      SECURITY_PERMISSION_SPEC("security-permission-spec");

      private String name;

      /**
       *
       * Create a new SecurityPermissionTag.
       *
       * @param name a name
       */
      SecurityPermissionTag(final String name)
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

      private static final Map<String, SecurityPermissionTag> MAP;

      static
      {
         final Map<String, SecurityPermissionTag> map = new HashMap<String, SecurityPermissionTag>();
         for (SecurityPermissionTag element : values())
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
      SecurityPermissionTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static SecurityPermissionTag forName(String localName)
      {
         final SecurityPermissionTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }


   // RequiredConfigProperty
   /**
   *
   * A RequiredConfigPropertyTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum RequiredConfigPropertyTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * description TAG
       */
      DESCRIPTION("description"),

      /**
       * config-property-name TAG
       */
      CONFIG_PROPERTY_NAME("config-property-name");

      private String name;

      /**
       *
       * Create a new RequiredConfigPropertyTag.
       *
       * @param name a name
       */
      RequiredConfigPropertyTag(final String name)
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

      private static final Map<String, RequiredConfigPropertyTag> MAP;

      static
      {
         final Map<String, RequiredConfigPropertyTag> map = new HashMap<String, RequiredConfigPropertyTag>();
         for (RequiredConfigPropertyTag element : values())
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
      RequiredConfigPropertyTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static RequiredConfigPropertyTag forName(String localName)
      {
         final RequiredConfigPropertyTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }


   // OutboundResourceAdapter

   /**
   *
   * A OutboundResourceAdapterTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum OutboundResourceAdapterTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * connection-definition TAG
       */
      CONNECTION_DEFINITION("connection-definition"),

      /**
       * transaction-support TAG
       */
      TRANSACTION_SUPPORT("transaction-support"),

      /**
       * authentication-mechanism TAG
       */
      AUTHENTICATION_MECHANISM("authentication-mechanism"),

      /**
       * reauthentication-support TAG
       */
      REAUTHENTICATION_SUPPORT("reauthentication-support");

      private String name;

      /**
       *
       * Create a new OutboundResourceAdapterTag.
       *
       * @param name a name
       */
      OutboundResourceAdapterTag(final String name)
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

      private static final Map<String, OutboundResourceAdapterTag> MAP;

      static
      {
         final Map<String, OutboundResourceAdapterTag> map = new HashMap<String, OutboundResourceAdapterTag>();
         for (OutboundResourceAdapterTag element : values())
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
      OutboundResourceAdapterTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static OutboundResourceAdapterTag forName(String localName)
      {
         final OutboundResourceAdapterTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }


   // MessageListener

   /**
   *
   * A MessageListenerTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum MessageListenerTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * messagelistener-type TAG
       */
      MESSAGELISTENER_TYPE("messagelistener-type"),

      /**
       * activationspec TAG
       */
      ACTIVATIONSPEC("activationspec");

      private String name;

      /**
       *
       * Create a new MessageListenerTag.
       *
       * @param name a name
       */
      MessageListenerTag(final String name)
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

      private static final Map<String, MessageListenerTag> MAP;

      static
      {
         final Map<String, MessageListenerTag> map = new HashMap<String, MessageListenerTag>();
         for (MessageListenerTag element : values())
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
      MessageListenerTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static MessageListenerTag forName(String localName)
      {
         final MessageListenerTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

   // MessageAdapter

   /**
   *
   * A MessageAdapterTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum MessageAdapterTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * messagelistener TAG
       */
      MESSAGELISTENER("messagelistener");

      private String name;

      /**
       *
       * Create a new MessageAdapterTag.
       *
       * @param name a name
       */
      MessageAdapterTag(final String name)
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

      private static final Map<String, MessageAdapterTag> MAP;

      static
      {
         final Map<String, MessageAdapterTag> map = new HashMap<String, MessageAdapterTag>();
         for (MessageAdapterTag element : values())
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
      MessageAdapterTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static MessageAdapterTag forName(String localName)
      {
         final MessageAdapterTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }


   // License
   /**
    *
    * A LicenseTag.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   public enum LicenseTag 
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * description tag
       */
      DESCRIPTION("description"),

      /**
       * vendor-name tag
       */
      LICENSE_REQUIRED("license-required");

      private String name;

      /**
       *
       * Create a new LicenseTag.
       *
       * @param name a name
       */
      LicenseTag(final String name)
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

      private static final Map<String, LicenseTag> MAP;

      static
      {
         final Map<String, LicenseTag> map = new HashMap<String, LicenseTag>();
         for (LicenseTag element : values())
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
      LicenseTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static LicenseTag forName(String localName)
      {
         final LicenseTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }


   // InboundResourceAdapter

   /**
   *
   * A InboundResourceAdapterTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum InboundResourceAdapterTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * messageadapter TAG
       */
      MESSAGEADAPTER("messageadapter");

      private String name;

      /**
       *
       * Create a new InboundResourceAdapterTag.
       *
       * @param name a name
       */
      InboundResourceAdapterTag(final String name)
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

      private static final Map<String, InboundResourceAdapterTag> MAP;

      static
      {
         final Map<String, InboundResourceAdapterTag> map = new HashMap<String, InboundResourceAdapterTag>();
         for (InboundResourceAdapterTag element : values())
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
      InboundResourceAdapterTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static InboundResourceAdapterTag forName(String localName)
      {
         final InboundResourceAdapterTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }


   // Icon
   /**
   *
   * A IconTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum IconTag 
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * small-icon tag
       */
      SMALL_ICON("small-icon"),

      /**
       * large-icon tag
       */
      LARGE_ICON("large-icon");

      private String name;

      /**
       *
       * Create a new IconTag.
       *
       * @param name a name
       */
      IconTag(final String name)
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

      private static final Map<String, IconTag> MAP;

      static
      {
         final Map<String, IconTag> map = new HashMap<String, IconTag>();
         for (IconTag element : values())
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
      IconTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static IconTag forName(String localName)
      {
         final IconTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

   /**
    *
    * A IconAttribute.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   public enum IconAttribute 
   {
      /** unknown attribute
       *
       */
      UNKNOWN(null),

      /** id attribute
       *
       */
      ID("id"),

      /**
       * lang atttribute
       */
      LANG("lang");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      IconAttribute(final String name)
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

      private static final Map<String, IconAttribute> MAP;

      static
      {
         final Map<String, IconAttribute> map = new HashMap<String, IconAttribute>();
         for (IconAttribute element : values())
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
      IconAttribute value(String v)
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
      public static IconAttribute forName(String localName)
      {
         final IconAttribute element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }


   // ConnectionDefinition

   /**
   *
   * A ConnectionDefinitionTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum ConnectionDefinitionTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * managedconnectionfactory-class TAG
       */
      MANAGEDCONNECTIONFACTORY_CLASS("managedconnectionfactory-class"),

      /**
       * config-property TAG
       */
      CONFIG_PROPERTY("config-property"),

      /**
       * connectionfactory-interface TAG
       */
      CONNECTIONFACTORY_INTERFACE("connectionfactory-interface"),

      /**
       * connectionfactory-impl-class TAG
       */
      CONNECTIONFACTORY_IMPL_CLASS("connectionfactory-impl-class"),

      /**
       * connection-interface TAG
       */
      CONNECTION_INTERFACE("connection-interface"),

      /**
       * connection-impl-class TAG
       */
      CONNECTION_IMPL_CLASS("connection-impl-class");

      private String name;

      /**
       *
       * Create a new ConnectionDefinitionTag.
       *
       * @param name a name
       */
      ConnectionDefinitionTag(final String name)
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

      private static final Map<String, ConnectionDefinitionTag> MAP;

      static
      {
         final Map<String, ConnectionDefinitionTag> map = new HashMap<String, ConnectionDefinitionTag>();
         for (ConnectionDefinitionTag element : values())
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
      ConnectionDefinitionTag value(String v)
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
      public static ConnectionDefinitionTag forName(String localName)
      {
         final ConnectionDefinitionTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }


   // AuthenticationMechanism

   /**
   *
   * A AuthenticationMechanismTag.
   *
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
   *
   */
   public enum AuthenticationMechanismTag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * description TAG
       */
      DESCRIPTION("description"),

      /**
       * authentication-mechanism-type TAG
       */
      AUTHENTICATION_MECHANISM_TYPE("authentication-mechanism-type"),

      /**
       * credential-interface TAG
       */
      CREDENTIAL_INTERFACE("credential-interface");

      private String name;

      /**
       *
       * Create a new AuthenticationMechanismTag.
       *
       * @param name a name
       */
      AuthenticationMechanismTag(final String name)
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

      private static final Map<String, AuthenticationMechanismTag> MAP;

      static
      {
         final Map<String, AuthenticationMechanismTag> map = new HashMap<String, AuthenticationMechanismTag>();
         for (AuthenticationMechanismTag element : values())
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
      AuthenticationMechanismTag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static AuthenticationMechanismTag forName(String localName)
      {
         final AuthenticationMechanismTag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

}
