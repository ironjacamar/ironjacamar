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

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.MergeUtil;
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

   private final XsdString moduleName;

   private ArrayList<XsdString> requiredWorkContexts;

   private final boolean metadataComplete;

   /**
    * @param moduleName name of the module
    * @param vendorName vendor name
    * @param eisType eis type
    * @param resourceadapterVersion version number for the RA
    * @param license license information
    * @param resourceadapter full qualified name of the resource adapter
    * @param requiredWorkContexts list od work context required
    * @param metadataComplete not mandatory boolean value
    * @param description descriptions of this connector
    * @param displayNames name to display for this connecotro
    * @param icons icon representing this connectore
    * @param id XML ID
    */
   public Connector16Impl(String moduleName, XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion,
         LicenseType license, ResourceAdapter1516 resourceadapter, List<String> requiredWorkContexts,
         boolean metadataComplete, List<LocalizedXsdString> description, List<LocalizedXsdString> displayNames,
         List<Icon> icons, String id)
   {
      this(new XsdString(moduleName, null), vendorName, eisType, resourceadapterVersion, license,
            resourceadapter, null, metadataComplete, description, displayNames, icons, id);
      if (requiredWorkContexts != null)
      {
         this.requiredWorkContexts = new ArrayList<XsdString>(requiredWorkContexts.size());
         for (String wc : requiredWorkContexts)
            this.requiredWorkContexts.add(new XsdString(wc, null, Connector16.Tag.REQUIRED_WORK_CONTEXT.toString()));
      }
      else
      {
         this.requiredWorkContexts = new ArrayList<XsdString>(0);
      }
   }

   /**
    * @param moduleName Xsd string as name of the module
    * @param vendorName vendor name
    * @param eisType eis type
    * @param resourceadapterVersion version number for the RA
    * @param license license information
    * @param resourceadapter full qualified name of the resource adapter
    * @param requiredWorkContexts list od work context required
    * @param metadataComplete not mandatory boolean value
    * @param description descriptions of this connector
    * @param displayNames name to display for this connecotro
    * @param icons icon representing this connectore
    * @param id XML ID
    */
   public Connector16Impl(XsdString moduleName, XsdString vendorName, XsdString eisType,
         XsdString resourceadapterVersion, LicenseType license, ResourceAdapter1516 resourceadapter,
         List<XsdString> requiredWorkContexts, boolean metadataComplete, List<LocalizedXsdString> description,
         List<LocalizedXsdString> displayNames, List<Icon> icons, String id)
   {
      super(vendorName, eisType, resourceadapterVersion, license, resourceadapter, description, displayNames,
            icons, id);
      this.moduleName = moduleName;
      if (!XsdString.isNull(this.moduleName))
         this.moduleName.setTag(Connector16.Tag.MODULE_NAME.toString());
      if (requiredWorkContexts != null)
      {
         this.requiredWorkContexts = new ArrayList<XsdString>(requiredWorkContexts.size());
         this.requiredWorkContexts.addAll(requiredWorkContexts);
         for (XsdString wc: this.requiredWorkContexts)
            wc.setTag(Connector16.Tag.REQUIRED_WORK_CONTEXT.toString());
      }
      else
      {
         this.requiredWorkContexts = new ArrayList<XsdString>(0);
      }
      this.metadataComplete = metadataComplete;
   }

   /**
    * @return requiredWorkContext
    */
   @Override
   public List<String> getRequiredWorkContexts()
   {
      if (requiredWorkContexts == null)
         return null;
      else
      {
         List<String> result = new ArrayList<String>(requiredWorkContexts.size());
         for (XsdString wc : requiredWorkContexts)
            result.add(wc.getValue());
         return Collections.unmodifiableList(result);
      }
   }

   /**
    * @return moduleName
    */
   @Override
   public String getModuleName()
   {
      if (XsdString.isNull(moduleName))
         return null;
      return moduleName.getValue();
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
      int result = super.hashCode();
      result = prime * result + (metadataComplete ? 1231 : 1237);
      result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
      result = prime * result + ((requiredWorkContexts == null) ? 0 : requiredWorkContexts.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof Connector16Impl))
         return false;
      Connector16Impl other = (Connector16Impl) obj;
      if (metadataComplete != other.metadataComplete)
         return false;
      if (moduleName == null)
      {
         if (other.moduleName != null)
            return false;
      }
      else if (!moduleName.equals(other.moduleName))
         return false;
      if (requiredWorkContexts == null)
      {
         if (other.requiredWorkContexts != null)
            return false;
      }
      else if (!requiredWorkContexts.equals(other.requiredWorkContexts))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      sb.append("<").append("connector");
      sb.append(" " + Connector16.Attribute.VERSION + "=\"1.6\"");
      sb.append(" " + Connector16.Attribute.METADATA_COMPLETE + "=\"" + metadataComplete + "\"");
      if (id != null)
         sb.append(" " + Connector16.Attribute.ID + "=\"" + id + "\"");
      sb.append(">");

      if (moduleName != null)
         sb.append(moduleName);

      sb.append(super.defaultPropertiesToString());

      if (!XsdString.isNull(resourceadapterVersion))
         sb.append(resourceadapterVersion);

      sb.append(resourceadapter);

      if (requiredWorkContexts != null)
      {
         for (XsdString rwc : requiredWorkContexts)
            sb.append(rwc);
      }

      sb.append("</").append("connector").append(">");

      return sb.toString();
   }

   @Override
   public Connector merge(MergeableMetadata<?> inputMd) throws Exception
   {

      if (inputMd instanceof Connector16Impl)
      {
         Connector16Impl input16 = (Connector16Impl) inputMd;
         XsdString newResourceadapterVersion = XsdString.isNull(this.resourceadapterVersion)
               ? input16.resourceadapterVersion
               : this.resourceadapterVersion;
         XsdString newEisType = XsdString.isNull(this.eisType) ? input16.eisType : this.eisType;
         List<XsdString> newRequiredWorkContexts = MergeUtil.mergeList(this.requiredWorkContexts,
               input16.requiredWorkContexts);
         XsdString newModuleName = this.moduleName == null ? input16.moduleName : this.moduleName;
         List<Icon> newIcons = MergeUtil.mergeList(this.icon, input16.icon);
         boolean newMetadataComplete = this.metadataComplete || input16.metadataComplete;
         LicenseType newLicense = this.license == null ? input16.license : this.license.merge(input16.license);
         List<LocalizedXsdString> newDescriptions = MergeUtil.mergeList(this.description, input16.description);
         List<LocalizedXsdString> newDisplayNames = MergeUtil.mergeList(this.displayName, input16.displayName);
         XsdString newVendorName = XsdString.isNull(this.vendorName) ? input16.vendorName : this.vendorName;;
         ResourceAdapter1516 newResourceadapter = this.resourceadapter == null
               ? (ResourceAdapter1516) input16.resourceadapter
               : ((ResourceAdapter1516) this.resourceadapter).merge((ResourceAdapter1516) input16.resourceadapter);
         return new Connector16Impl(newModuleName, newVendorName, newEisType, newResourceadapterVersion, newLicense,
               newResourceadapter, newRequiredWorkContexts, newMetadataComplete, newDescriptions, newDisplayNames,
               newIcons, null);
      }
      return this;

   }

   @Override
   public CopyableMetaData copy()
   {
      XsdString newResourceadapterVersion = CopyUtil.clone(this.resourceadapterVersion);
      XsdString newEisType = XsdString.isNull(this.eisType) ? null : (XsdString) this.eisType.copy();
      List<XsdString> newRequiredWorkContexts = CopyUtil.cloneList(this.requiredWorkContexts);
      XsdString newModuleName = CopyUtil.clone(this.moduleName);
      List<Icon> newIcons = CopyUtil.cloneList(this.icon);
      boolean newMetadataComplete = this.metadataComplete;
      LicenseType newLicense = CopyUtil.clone(this.license);
      List<LocalizedXsdString> newDescriptions = CopyUtil.cloneList(this.description);
      List<LocalizedXsdString> newDisplayNames = CopyUtil.cloneList(this.displayName);
      XsdString newVendorName = CopyUtil.clone(this.vendorName);
      ResourceAdapter1516 newResourceadapter = CopyUtil.clone((ResourceAdapter1516) this.resourceadapter);
      return new Connector16Impl(newModuleName, newVendorName, newEisType, newResourceadapterVersion, newLicense,
            newResourceadapter, newRequiredWorkContexts, newMetadataComplete, newDescriptions, newDisplayNames,
            newIcons, CopyUtil.cloneString(id));
   }

}
