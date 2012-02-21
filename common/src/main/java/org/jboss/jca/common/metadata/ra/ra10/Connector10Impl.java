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

   private final XsdString resourceadapterVersion;

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
    */
   public Connector10Impl(XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion,
         LicenseType license, ResourceAdapter resourceadapter, List<LocalizedXsdString> description,
         List<LocalizedXsdString> displayName,
         List<Icon> icon, String id)
   {
      super(vendorName, eisType, license, resourceadapter, description, displayName, icon, id);
      this.resourceadapterVersion = resourceadapterVersion;
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
   public Version getSpecVersion()
   {
      return Version.V_10;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
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

      return super.equals((Connector10Impl) obj);
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      sb.append("<").append("connector").append(">");

      // description, displayName, icon

      if (!XsdString.isNull(vendorName))
      {
         sb.append("<" + Connector10.Tag.VENDOR_NAME + ">");
         sb.append(vendorName);
         sb.append("</" + Connector10.Tag.VENDOR_NAME + ">");
      }

      sb.append("<" + Connector10.Tag.SPEC_VERSION + ">");
      sb.append("1.0");
      sb.append("</" + Connector10.Tag.SPEC_VERSION + ">");

      if (!XsdString.isNull(eisType))
      {
         sb.append("<" + Connector10.Tag.EIS_TYPE + ">");
         sb.append(eisType);
         sb.append("</" + Connector10.Tag.EIS_TYPE + ">");
      }

      sb.append("<" + Connector10.Tag.VERSION + ">");
      sb.append(resourceadapterVersion);
      sb.append("</" + Connector10.Tag.VERSION + ">");

      if (license != null)
         sb.append(license);

      sb.append(resourceadapter);

      sb.append("</").append("connector").append(">");

      return sb.toString();
   }

   @Override
   public CopyableMetaData copy()
   {
      return new Connector10Impl(CopyUtil.clone(vendorName),
            CopyUtil.clone(eisType), CopyUtil.clone(resourceadapterVersion), CopyUtil.clone(license),
            CopyUtil.clone(resourceadapter),
            CopyUtil.cloneList(description), CopyUtil.cloneList(displayName), CopyUtil.cloneList(icon),
            CopyUtil.cloneString(id));
   }

   @Override
   public Connector merge(MergeableMetadata<?> inputMd) throws Exception
   {

      if (inputMd instanceof Connector10Impl)
      {
         Connector10Impl input10 = (Connector10Impl) inputMd;
         XsdString newResourceadapterVersion = XsdString.isNull(this.resourceadapterVersion)
               ? input10.resourceadapterVersion : this.resourceadapterVersion;
         XsdString newEisType = XsdString.isNull(this.eisType) ? input10.eisType : this.eisType;
         List<Icon> newIcons = MergeUtil.mergeList(this.icon, input10.icon);
         LicenseType newLicense = this.license == null ? input10.license : this.license.merge(input10.license);
         List<LocalizedXsdString> newDescriptions = MergeUtil.mergeList(this.description,
               input10.description);
         List<LocalizedXsdString> newDisplayNames = MergeUtil.mergeList(this.displayName,
               input10.displayName);
         XsdString newVendorName = XsdString.isNull(this.vendorName)
               ? input10.vendorName : this.vendorName;;
         ResourceAdapter10 newResourceadapter = this.resourceadapter == null
               ? (ResourceAdapter10) input10.resourceadapter
               : ((ResourceAdapter10) this.resourceadapter)
                     .merge((ResourceAdapter10) input10.resourceadapter);
         return new Connector10Impl(newVendorName, newEisType, newResourceadapterVersion, newLicense,
               newResourceadapter, newDescriptions, newDisplayNames, newIcons, null);
      }
      return this;
   }
}
