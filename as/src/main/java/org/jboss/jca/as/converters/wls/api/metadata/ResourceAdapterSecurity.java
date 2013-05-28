/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.as.converters.wls.api.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * A generic ResourceAdapterSecurity.
 *
 * @author <a href="jeff.zhang@ironjacamar.org">Jeff Zhang</a>
 *
 */
public interface ResourceAdapterSecurity extends WlsMetadata
{

   /**
    * Get DefaultPrincipalName
    * 
    * @return the AnonPrincipal
    */
   public AnonPrincipal getDefaultPrincipalName();
   
   /**
    * Get ManageAsPrincipalName
    * 
    * @return the AnonPrincipal
    */
   public AnonPrincipal getManageAsPrincipalName();
   
   /**
    * Get RunAsPrincipalName
    * 
    * @return the AnonPrincipalCaller
    */
   public AnonPrincipalCaller getRunAsPrincipalName();
   
   /**
    * Get RunWorkAsPrincipalName
    * 
    * @return the AnonPrincipalCaller  
    */
   public AnonPrincipalCaller getRunWorkAsPrincipalName();
   
   /**
    * Get SecurityWorkContext
    * 
    * @return the SecurityWorkContext   
    */
   public SecurityWorkContext getSecurityWorkContext();
   
   /**
   *
   * A Tag.
   *
   */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
       * default-principal-name tag
       */
      DEFAULT_PRINCIPAL_NAME("default-principal-name"),

      /**
       * manage-as-principal-name tag
       */
      MANAGE_AS_PRINCIPAL_NAME("manage-as-principal-name"),

      /**
       * run-as-principal-name tag
       */
      RUN_AS_PRINCIPAL_NAME("run-as-principal-name"),

      /**
       * run-work-as-principal-name tag
       */
      RUN_WORK_AS_PRINCIPAL_NAME("run-work-as-principal-name"),

      /**
       * security-work-context tag
       */
      SECURITY_WORK_CONTEXT("security-work-context");

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
