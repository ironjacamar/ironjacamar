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
package org.jboss.jca.common.api.metadata.ra.ra10;

import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ra.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.SecurityPermission;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * A ResourceAdapter10.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public interface ResourceAdapter10 extends ResourceAdapter, MergeableMetadata<ResourceAdapter10>
{

   /**
    * Get the managedConnectionFactoryClass.
    *
    * @return the managedConnectionFactoryClass.
    */
   public abstract XsdString getManagedConnectionFactoryClass();

   /**
    * Get the connectionFactoryInterface.
    *
    * @return the connectionFactoryInterface.
    */
   public abstract XsdString getConnectionFactoryInterface();

   /**
    * Get the connectionFactoryImplClass.
    *
    * @return the connectionFactoryImplClass.
    */
   public abstract XsdString getConnectionFactoryImplClass();

   /**
    * Get the connectionInterface.
    *
    * @return the connectionInterface.
    */
   public abstract XsdString getConnectionInterface();

   /**
    * Get the connectionImplClass.
    *
    * @return the connectionImplClass.
    */
   public abstract XsdString getConnectionImplClass();

   /**
    * Get the transactionSupport.
    *
    * @return the transactionSupport.
    */
   public abstract TransactionSupportEnum getTransactionSupport();

   /**
    * Get the authenticationMechanism.
    *
    * @return the authenticationMechanism.
    */
   public abstract List<AuthenticationMechanism> getAuthenticationMechanisms();


   /**
    * Get the reauthenticationSupport.
    *
    * @return the reauthenticationSupport.
    */
   public abstract Boolean getReauthenticationSupport();

   /**
    * Get the securityPermission.
    *
    * @return the securityPermission.
    */
   public abstract List<SecurityPermission> getSecurityPermissions();

   /**
    * Get the id.
    *
    * @return the id.
    */
   @Override
   public abstract String getId();

   @Override
   public abstract int hashCode();

   @Override
   public abstract boolean equals(Object obj);

   @Override
   public abstract String toString();

   @Override
   public abstract void validate() throws ValidateException;

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
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
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
       * Set the value
       * @param v The name
       * @return The value
       */
      Attribute value(String v)
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
      public static Attribute forName(String localName)
      {
         final Attribute element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }
}
