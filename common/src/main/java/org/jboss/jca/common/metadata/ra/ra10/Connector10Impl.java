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

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.MergeUtil;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Icon;
import org.jboss.jca.common.api.metadata.ra.LicenseType;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra10.Connector10;
import org.jboss.jca.common.api.metadata.ra.ra10.Connector10.Tag;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.metadata.ra.common.ConnectorAbstractmpl;

import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public final class Connector10Impl extends ConnectorAbstractmpl implements Connector10
{

   /**
    */
   private static final long serialVersionUID = -6095735191032372517L;

   private final XsdString version;

   private final XsdString specVersion;

   /**
    * @param description descriptions of this connector
    * @param displayName name to display for this connecotro
    * @param icon icon representing this connectore
    * @param vendorName vendor name
    * @param eisType eis type
    * @param resourceadapterVersion version number for the RA
    * @param license license information
    * @param resourceadapter full qualified name of the resource adapter
    * @param id XML ID
    * @param specVersion parameter
    */
   public Connector10Impl(XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion,
         LicenseType license, ResourceAdapter resourceadapter, List<LocalizedXsdString> description,
         List<LocalizedXsdString> displayName, List<Icon> icon, String id, XsdString specVersion)
   {
      super(vendorName, eisType, license, resourceadapter, description, displayName, icon, id);
      this.version = resourceadapterVersion;
      if (!XsdString.isNull(this.version))
         this.version.setTag(Tag.VERSION.toString());
      this.specVersion = specVersion;
      if (!XsdString.isNull(this.specVersion))
         this.specVersion.setTag(Tag.SPEC_VERSION.toString());
   }

   /**
    * constructor with default spec version 
    * 
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
   public Connector10Impl(XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion,
         LicenseType license, ResourceAdapter resourceadapter, List<LocalizedXsdString> description,
         List<LocalizedXsdString> displayName, List<Icon> icon, String id)
   {
      this(vendorName, eisType, resourceadapterVersion, license, resourceadapter, description, 
            displayName, icon, id, new XsdString("1.0", null));
   }

   /**
    * Get the version.
    *
    * @return the version.
    */
   @Override
   public Version getVersion()
   {
      return Version.V_10;
   }

   /**
    * Get the specVersion.
    *
    * @return the specVersion.
    */
   public XsdString getSpecVersion()
   {
      return specVersion;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((version == null) ? 0 : version.hashCode());
      result = prime * result + ((specVersion == null) ? 0 : specVersion.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof Connector10Impl))
         return false;
      Connector10Impl other = (Connector10Impl) obj;
      if (version == null)
      {
         if (other.version != null)
            return false;
      }
      else if (!version.equals(other.version))
         return false;
      if (specVersion == null)
      {
         if (other.specVersion != null)
            return false;
      }
      else if (!specVersion.equals(other.specVersion))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      sb.append("<").append("connector");
      if (id != null)
         sb.append(" " + Connector10.Attribute.ID + "=\"" + id + "\"");
      sb.append(">");

      sb.append(super.toString());

      if (!XsdString.isNull(version))
         sb.append(version);

      if (!XsdString.isNull(specVersion))
         sb.append(specVersion);

      sb.append(resourceadapter);

      sb.append("</").append("connector").append(">");

      return sb.toString();
   }

   @Override
   public CopyableMetaData copy()
   {
      return new Connector10Impl(CopyUtil.clone(vendorName), CopyUtil.clone(eisType), CopyUtil.clone(version),
            CopyUtil.clone(license), CopyUtil.clone(resourceadapter), CopyUtil.cloneList(description),
            CopyUtil.cloneList(displayName), CopyUtil.cloneList(icon), CopyUtil.cloneString(id),
            CopyUtil.clone(specVersion));
   }

   @Override
   public Connector merge(MergeableMetadata<?> inputMd) throws Exception
   {

      if (inputMd instanceof Connector10Impl)
      {
         Connector10Impl input10 = (Connector10Impl) inputMd;
         XsdString newResourceadapterVersion = XsdString.isNull(this.version) ? input10.version : this.version;
         XsdString newEisType = XsdString.isNull(this.eisType) ? input10.eisType : this.eisType;
         List<Icon> newIcons = MergeUtil.mergeList(this.icon, input10.icon);
         LicenseType newLicense = this.license == null ? input10.license : this.license.merge(input10.license);
         List<LocalizedXsdString> newDescriptions = MergeUtil.mergeList(this.description, input10.description);
         List<LocalizedXsdString> newDisplayNames = MergeUtil.mergeList(this.displayName, input10.displayName);
         XsdString newVendorName = XsdString.isNull(this.vendorName) ? input10.vendorName : this.vendorName;;
         ResourceAdapter10 newResourceadapter = this.resourceadapter == null
               ? (ResourceAdapter10) input10.resourceadapter
               : ((ResourceAdapter10) this.resourceadapter).merge((ResourceAdapter10) input10.resourceadapter);
         return new Connector10Impl(newVendorName, newEisType, newResourceadapterVersion, newLicense,
               newResourceadapter, newDescriptions, newDisplayNames, newIcons, null);
      }
      return this;
   }
}
