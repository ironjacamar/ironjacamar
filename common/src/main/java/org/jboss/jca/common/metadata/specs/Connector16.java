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
package org.jboss.jca.common.metadata.specs;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public final class Connector16 implements IdDecoratedMetadata
{

   /**
    */
   private static final long serialVersionUID = -6095735191032372517L;

   private final String moduleName;

   private final List<LocalizedXsdString> description;

   private final List<LocalizedXsdString> displayName;

   private final List<Icon> icon;

   private final XsdString vendorName;

   private final XsdString eisType;

   private final XsdString resourceadapterVersion;

   private final LicenseType license;

   private final ResourceAdapter resourceadapter;

   private final List<String> requiredWorkContext;

   private final String version = "1.6";

   private final Boolean metadataComplete;

   private final String id;

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
    * @param requiredWorkContext list od work context required
    * @param metadataComplete not mandatory boolean value
    * @param id XML ID
    */
   public Connector16(String moduleName, List<LocalizedXsdString> description, List<LocalizedXsdString> displayName,
         List<Icon> icon, XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion,
         LicenseType license, ResourceAdapter resourceadapter, List<String> requiredWorkContext,
         Boolean metadataComplete, String id)
   {
      super();
      this.moduleName = moduleName;
      this.description = description;
      this.displayName = displayName;
      this.icon = icon;
      this.vendorName = vendorName;
      this.eisType = eisType;
      this.resourceadapterVersion = resourceadapterVersion;
      this.license = license;
      this.resourceadapter = resourceadapter;
      this.requiredWorkContext = requiredWorkContext;
      this.metadataComplete = metadataComplete;
      this.id = id;
   }

   /**
    * @return resourceadapterVersion
    */
   public XsdString getResourceadapterVersion()
   {
      return resourceadapterVersion;
   }

   /**
    * @return license
    */
   public LicenseType getLicense()
   {
      return license;
   }

   /**
    * @return resourceadapter
    */
   public ResourceAdapter getResourceadapter()
   {
      return resourceadapter;
   }

   /**
    * @return requiredWorkContext
    */
   public List<String> getRequiredWorkContext()
   {
      return Collections.unmodifiableList(requiredWorkContext);
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
   public List<LocalizedXsdString> getDisplayName()
   {
      return Collections.unmodifiableList(displayName);
   }

   /**
    * @return icon
    */
   public List<Icon> getIcon()
   {
      return Collections.unmodifiableList(icon);
   }

   /**
    * @return vendorName
    */
   public XsdString getVendorName()
   {
      return vendorName;
   }

   /**
    * @return eisType
    */
   public XsdString getEisType()
   {
      return eisType;
   }

   /**
    * @return version
    */
   public String getVersion()
   {
      return version;
   }

   /**
    * @return metadataComplete
    */
   public Boolean getMetadataComplete()
   {
      return metadataComplete;
   }

   /**
    * {@inheritDoc}
    *
    * @see IdDecoratedMetadata#getId()
    */
   @Override
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
      result = prime * result + ((eisType == null) ? 0 : eisType.hashCode());
      result = prime * result + ((icon == null) ? 0 : icon.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((license == null) ? 0 : license.hashCode());
      result = prime * result + ((metadataComplete == null) ? 0 : metadataComplete.hashCode());
      result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
      result = prime * result + ((requiredWorkContext == null) ? 0 : requiredWorkContext.hashCode());
      result = prime * result + ((resourceadapter == null) ? 0 : resourceadapter.hashCode());
      result = prime * result + ((resourceadapterVersion == null) ? 0 : resourceadapterVersion.hashCode());
      result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
      result = prime * result + ((version == null) ? 0 : version.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof Connector16))
      {
         return false;
      }
      Connector16 other = (Connector16) obj;
      if (description == null)
      {
         if (other.description != null)
         {
            return false;
         }
      }
      else if (!description.equals(other.description))
      {
         return false;
      }
      if (displayName == null)
      {
         if (other.displayName != null)
         {
            return false;
         }
      }
      else if (!displayName.equals(other.displayName))
      {
         return false;
      }
      if (eisType == null)
      {
         if (other.eisType != null)
         {
            return false;
         }
      }
      else if (!eisType.equals(other.eisType))
      {
         return false;
      }
      if (icon == null)
      {
         if (other.icon != null)
         {
            return false;
         }
      }
      else if (!icon.equals(other.icon))
      {
         return false;
      }
      if (id == null)
      {
         if (other.id != null)
         {
            return false;
         }
      }
      else if (!id.equals(other.id))
      {
         return false;
      }
      if (license == null)
      {
         if (other.license != null)
         {
            return false;
         }
      }
      else if (!license.equals(other.license))
      {
         return false;
      }
      if (metadataComplete == null)
      {
         if (other.metadataComplete != null)
         {
            return false;
         }
      }
      else if (!metadataComplete.equals(other.metadataComplete))
      {
         return false;
      }
      if (moduleName == null)
      {
         if (other.moduleName != null)
         {
            return false;
         }
      }
      else if (!moduleName.equals(other.moduleName))
      {
         return false;
      }
      if (requiredWorkContext == null)
      {
         if (other.requiredWorkContext != null)
         {
            return false;
         }
      }
      else if (!requiredWorkContext.equals(other.requiredWorkContext))
      {
         return false;
      }
      if (resourceadapter == null)
      {
         if (other.resourceadapter != null)
         {
            return false;
         }
      }
      else if (!resourceadapter.equals(other.resourceadapter))
      {
         return false;
      }
      if (resourceadapterVersion == null)
      {
         if (other.resourceadapterVersion != null)
         {
            return false;
         }
      }
      else if (!resourceadapterVersion.equals(other.resourceadapterVersion))
      {
         return false;
      }
      if (vendorName == null)
      {
         if (other.vendorName != null)
         {
            return false;
         }
      }
      else if (!vendorName.equals(other.vendorName))
      {
         return false;
      }
      if (version == null)
      {
         if (other.version != null)
         {
            return false;
         }
      }
      else if (!version.equals(other.version))
      {
         return false;
      }
      return true;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "Connector16 [moduleName=" + moduleName + ", description=" + description + ", displayName=" + displayName
            + ", icon=" + icon + ", vendorName=" + vendorName + ", eisType=" + eisType + ", resourceadapterVersion="
            + resourceadapterVersion + ", license=" + license + ", resourceadapter=" + resourceadapter
            + ", requiredWorkContext=" + requiredWorkContext + ", version=" + version + ", metadataComplete="
            + metadataComplete + ", id=" + id + "]";
   }
}
