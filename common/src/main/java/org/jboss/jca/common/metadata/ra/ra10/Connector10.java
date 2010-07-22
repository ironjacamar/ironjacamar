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
package org.jboss.jca.common.metadata.ra.ra10;

import org.jboss.jca.common.metadata.ra.common.Connector;
import org.jboss.jca.common.metadata.ra.common.LicenseType;
import org.jboss.jca.common.metadata.ra.common.LocalizedXsdString;
import org.jboss.jca.common.metadata.ra.common.ResourceAdapter;
import org.jboss.jca.common.metadata.ra.common.XsdString;
import org.jboss.jca.common.metadata.ra.ra16.Icon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public final class Connector10 extends Connector
{

   /**
    */
   private static final long serialVersionUID = -6095735191032372517L;

   private final String moduleName;

   private final ArrayList<LocalizedXsdString> description;

   private final XsdString displayName;

   private static final String VERSION = "1.0";

   private final String version = Connector10.VERSION;

   private final String specVersion = VERSION;

   private final ArrayList<Icon> icon;

   /**
    * @param moduleName name of the module
    * @param description descriptions of this connector
    * @param displayName name to display for this connecotro
    * @param icon icon representing this connectore
    * @param vendorName vendor name
    * @param eisType eis type
    * @param resourceadapterVersion version number for the RA
    * @param license license information
    * @param resourceadapter full qualified name of the resource adapter
    * @param id XML ID
    */
   public Connector10(String moduleName, ArrayList<LocalizedXsdString> description, XsdString displayName, ArrayList<Icon> icon,
         XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion, LicenseType license,
         ResourceAdapter resourceadapter, String id)
   {
      super(vendorName, eisType, license, resourceadapter, id);
      this.moduleName = moduleName;
      this.description = description;
      this.displayName = displayName;
      this.icon = icon;
   }

   /**
    * @return moduleName
    */
   public String getModuleName()
   {
      return moduleName;
   }

   /**
    * @return description
    */
   public List<LocalizedXsdString> getDescription()
   {
      return Collections.unmodifiableList(description);
   }

   /**
    * @return displayName
    */
   public XsdString getDisplayName()
   {
      return displayName;
   }

   /**
    * @return icon
    */
   public List<Icon> getIcon()
   {
      return Collections.unmodifiableList(icon);
   }

   /**
    * Get the version.
    *
    * @return the version.
    */
   @Override
   public String getVersion()
   {
      return version;
   }

   /**
    * Get the specVersion.
    *
    * @return the specVersion.
    */
   public String getSpecVersion()
   {
      return specVersion;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
      result = prime * result + ((icon == null) ? 0 : icon.hashCode());
      result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
      result = prime * result + ((specVersion == null) ? 0 : specVersion.hashCode());
      result = prime * result + ((version == null) ? 0 : version.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof Connector10))
         return false;
      Connector10 other = (Connector10) obj;
      if (description == null)
      {
         if (other.description != null)
            return false;
      }
      else if (!description.equals(other.description))
         return false;
      if (displayName == null)
      {
         if (other.displayName != null)
            return false;
      }
      else if (!displayName.equals(other.displayName))
         return false;
      if (icon == null)
      {
         if (other.icon != null)
            return false;
      }
      else if (!icon.equals(other.icon))
         return false;
      if (moduleName == null)
      {
         if (other.moduleName != null)
            return false;
      }
      else if (!moduleName.equals(other.moduleName))
         return false;
      if (specVersion == null)
      {
         if (other.specVersion != null)
            return false;
      }
      else if (!specVersion.equals(other.specVersion))
         return false;
      if (version == null)
      {
         if (other.version != null)
            return false;
      }
      else if (!version.equals(other.version))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "Connector10 [moduleName=" + moduleName + ", description=" + description + ", displayName=" + displayName
            + ", version=" + version + ", specVersion=" + specVersion + ", icon=" + icon + ", vendorName=" + vendorName
            + ", eisType=" + eisType + ", license=" + license + ", resourceadapter=" + resourceadapter + ", id=" + id
            + "]";
   }

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
       * module-name tag
       */
      MODULENAME("module-name"),

      /**
       * vendor-name tag
       */
      VENDORNAME("vendor-name"),

      /**
       * eis-type tag
       */
      EIS_TYPE("eis-type"),

      /**
       * resourceadapter-version tag
       */
      RESOURCEADPTER_VERSION("resourceadapter-version"),

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
       * spec-version TAG
       */
      SPEC_VERSION("spec-version"),

      /**
       * version TAG
       */
      VERSION("version"),

      /**
       * display-name tag
       */
      DISPLAY_NAME("display-name");
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
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
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

      /** id attribute
       *
       */
      ID("id"),

      /**
       * version attribute
       */
      VERSION("version");

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
