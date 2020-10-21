/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.api.metadata.spec;

import org.ironjacamar.common.api.metadata.CopyableMetaData;
import org.ironjacamar.common.api.metadata.ValidatableMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * A Connector.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public interface Connector
      extends
         IdDecoratedMetadata,
         ValidatableMetadata,
         MergeableMetadata<Connector>,
         CopyableMetaData<Connector>
{

   /**
    * Get the vendorName.
    *
    * @return the vendorName.
    */
   public XsdString getVendorName();

   /**
    * Get the eisType.
    *
    * @return the eisType.
    */
   public XsdString getEisType();

   /**
    * Get the license.
    *
    * @return the license.
    */
   public LicenseType getLicense();

   /**
    * Get the resourceadapter.
    *
    * @return the resourceadapter.
    */
   public ResourceAdapter getResourceadapter();

   /**
    * @return resourceadapterVersion
    */
   public XsdString getResourceadapterVersion();

   /**
    * @return description
    */
   public List<LocalizedXsdString> getDescriptions();

   /**
    * @return displayName
    */
   public List<LocalizedXsdString> getDisplayNames();

   /**
    * @return icon
    */
   public List<Icon> getIcons();

   /**
    * Get the version.
    *
    * @return the version.
    */
   public Version getVersion();

   /**
    * @return requiredWorkContext
    */
   public List<String> getRequiredWorkContexts();

   /**
    * @return moduleName
    */
   public String getModuleName();

   /**
    * @return metadataComplete
    */
   public boolean isMetadataComplete();

   /**
    *
    * A Version enumeration
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   public enum Version
   {
      /**
       * Unknown
       */
      UNKNOWN(null),
      /**
       * 1.0
       */
      V_10("1.0"),
      /**
       * 1.5
       */
      V_15("1.5"),
      /**
       * 1.6
       */
      V_16("1.6"),
      /**
       * 1.7
       */
      V_17("1.7"),
      /**
       * 2.0
       */
      V_20("2.0");

      private String name;

      /**
       * Constructor
       * @param name a name
       */
      Version(String name)
      {
         this.name = name;
      }

      /**
       * Get the name
       * @return The value
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

      private static final Map<String, Version> MAP;

      static
      {
         final Map<String, Version> map = new HashMap<String, Version>();
         for (Version element : values())
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
      Version value(String v)
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
      public static Version forName(String localName)
      {
         final Version element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }
   }
}
