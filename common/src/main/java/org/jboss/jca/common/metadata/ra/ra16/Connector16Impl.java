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
package org.jboss.jca.common.metadata.ra.ra16;

import org.jboss.jca.common.api.metadata.MergeUtil;
import org.jboss.jca.common.api.metadata.jbossra.JbossRa;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Icon;
import org.jboss.jca.common.api.metadata.ra.LicenseType;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra16.Connector16;
import org.jboss.jca.common.metadata.ra.ra15.Connector15Impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public final class Connector16Impl extends Connector15Impl implements Connector16
{

   /**
    */
   private static final long serialVersionUID = -6095735191032372517L;

   private final String moduleName;

   private final ArrayList<Icon> icons;

   private final ArrayList<String> requiredWorkContexts;

   private final ArrayList<LocalizedXsdString> description;

   private final ArrayList<LocalizedXsdString> displayNames;

   private final boolean metadataComplete;

   /**
    * @param moduleName name of the module
    * @param description descriptions of this connector
    * @param displayNames name to display for this connecotro
    * @param icons icon representing this connectore
    * @param vendorName vendor name
    * @param eisType eis type
    * @param resourceadapterVersion version number for the RA
    * @param license license information
    * @param resourceadapter full qualified name of the resource adapter
    * @param requiredWorkContexts list od work context required
    * @param metadataComplete not mandatory boolean value
    * @param id XML ID
    */
   public Connector16Impl(String moduleName, List<LocalizedXsdString> description,
         List<LocalizedXsdString> displayNames,
         List<Icon> icons, XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion,
         LicenseType license, ResourceAdapter1516 resourceadapter, List<String> requiredWorkContexts,
         boolean metadataComplete, String id)
   {
      super(vendorName, eisType, resourceadapterVersion, license, resourceadapter, id);
      this.moduleName = moduleName;
      if (description != null)
      {
         this.description = new ArrayList<LocalizedXsdString>(description.size());
         this.description.addAll(description);
      }
      else
      {
         this.description = new ArrayList<LocalizedXsdString>(0);
      }
      if (displayNames != null)
      {
         this.displayNames = new ArrayList<LocalizedXsdString>(displayNames.size());
         this.displayNames.addAll(displayNames);
      }
      else
      {
         this.displayNames = new ArrayList<LocalizedXsdString>(0);
      }
      if (icons != null)
      {
         this.icons = new ArrayList<Icon>(icons.size());
         this.icons.addAll(icons);
      }
      else
      {
         this.icons = new ArrayList<Icon>(0);
      }
      if (requiredWorkContexts != null)
      {
         this.requiredWorkContexts = new ArrayList<String>(requiredWorkContexts.size());
         this.requiredWorkContexts.addAll(requiredWorkContexts);
      }
      else
      {
         this.requiredWorkContexts = new ArrayList<String>(0);
      }
      this.metadataComplete = metadataComplete;
   }

   /**
    * @return requiredWorkContext
    */
   @Override
   public List<String> getRequiredWorkContexts()
   {
      return requiredWorkContexts == null ? null : Collections.unmodifiableList(requiredWorkContexts);
   }

   /**
    * @return moduleName
    */
   @Override
   public String getModuleName()
   {
      return moduleName;
   }

   /**
    * @return description
    */
   @Override
   public List<LocalizedXsdString> getDescriptions()
   {
      return description == null ? null : Collections.unmodifiableList(description);
   }

   /**
    * @return displayName
    */
   @Override
   public List<LocalizedXsdString> getDisplayNames()
   {
      return displayNames == null ? null : Collections.unmodifiableList(displayNames);
   }

   /**
    * @return icon
    */
   @Override
   public List<Icon> getIcons()
   {
      return icons == null ? null : Collections.unmodifiableList(icons);
   }

   /**
    * @return metadataComplete
    */
   @Override
   public boolean isMetadataComplete()
   {
      return metadataComplete;
   }

   /**
    * Get the version.
    *
    * @return the version.
    */
   @Override
   public Version getVersion()
   {
      return Version.V_16;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((displayNames == null) ? 0 : displayNames.hashCode());
      result = prime * result + ((eisType == null) ? 0 : eisType.hashCode());
      result = prime * result + ((icons == null) ? 0 : icons.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((license == null) ? 0 : license.hashCode());
      result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
      result = prime * result + ((requiredWorkContexts == null) ? 0 : requiredWorkContexts.hashCode());
      result = prime * result + ((resourceadapter == null) ? 0 : resourceadapter.hashCode());
      result = prime * result + ((resourceadapterVersion == null) ? 0 : resourceadapterVersion.hashCode());
      result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
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
      if (!(obj instanceof Connector16Impl))
      {
         return false;
      }
      Connector16Impl other = (Connector16Impl) obj;
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
      if (displayNames == null)
      {
         if (other.displayNames != null)
         {
            return false;
         }
      }
      else if (!displayNames.equals(other.displayNames))
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
      if (icons == null)
      {
         if (other.icons != null)
         {
            return false;
         }
      }
      else if (!icons.equals(other.icons))
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
      if (metadataComplete != other.metadataComplete)
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
      if (requiredWorkContexts == null)
      {
         if (other.requiredWorkContexts != null)
         {
            return false;
         }
      }
      else if (!requiredWorkContexts.equals(other.requiredWorkContexts))
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
      return "Connector16 [moduleName=" + moduleName + ", description=" + description + ", displayNames="
            + displayNames
            + ", icons=" + icons + ", vendorName=" + vendorName + ", eisType=" + eisType + ", resourceadapterVersion="
            + resourceadapterVersion + ", license=" + license + ", resourceadapter=" + resourceadapter
            + ", requiredWorkContexts=" + requiredWorkContexts + ", version=" + Version.V_15 + ", metadataComplete="
            + metadataComplete + ", id=" + id + "]";
   }

   @Override
   public Connector merge(MergeableMetadata<?> inputMd) throws Exception
   {
      if (inputMd instanceof JbossRa)
      {
         mergeJbossMetaData((JbossRa) inputMd);
         return this;
      }

      if (inputMd instanceof Connector16Impl)
      {
         Connector16Impl input16 = (Connector16Impl) inputMd;
         XsdString newResourceadapterVersion = XsdString.isNull(this.resourceadapterVersion)
               ? input16.resourceadapterVersion : this.resourceadapterVersion;
         XsdString newEisType = XsdString.isNull(this.eisType) ? input16.eisType : this.eisType;
         List<String> newRequiredWorkContexts = MergeUtil.mergeList(this.requiredWorkContexts,
               input16.requiredWorkContexts);
         String newModuleName = this.moduleName == null ? input16.moduleName : this.moduleName;
         List<Icon> newIcons = MergeUtil.mergeList(this.icons, input16.icons);
         boolean newMetadataComplete = this.metadataComplete || input16.metadataComplete;
         LicenseType newLicense = this.license == null ? input16.license : this.license.merge(input16.license);
         List<LocalizedXsdString> newDescriptions = MergeUtil.mergeList(this.description,
               input16.description);
         List<LocalizedXsdString> newDisplayNames = MergeUtil.mergeList(this.displayNames,
               input16.displayNames);
         XsdString newVendorName = XsdString.isNull(this.vendorName)
               ? input16.vendorName : this.vendorName;;
         ResourceAdapter1516 newResourceadapter = this.resourceadapter == null
               ? (ResourceAdapter1516) input16.resourceadapter
               : ((ResourceAdapter1516) this.resourceadapter)
               .merge((ResourceAdapter1516) input16.resourceadapter);
         return new Connector16Impl(newModuleName, newDescriptions, newDisplayNames, newIcons,
               newVendorName, newEisType, newResourceadapterVersion, newLicense, newResourceadapter,
               newRequiredWorkContexts, newMetadataComplete, newModuleName);
      }
      return this;


   }

}
