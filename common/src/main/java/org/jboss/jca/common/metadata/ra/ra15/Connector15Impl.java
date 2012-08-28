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
package org.jboss.jca.common.metadata.ra.ra15;

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
import org.jboss.jca.common.api.metadata.ra.ra15.Connector15;
import org.jboss.jca.common.api.metadata.ra.ra15.Connector15.Tag;
import org.jboss.jca.common.metadata.ra.common.ConnectorAbstractmpl;

import java.util.List;

/**
 *
 * A Connector15.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class Connector15Impl extends ConnectorAbstractmpl implements Connector15
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 531372312218060928L;

   /**
    * the resource adapter version
    */
   protected final XsdString resourceadapterVersion;

   /**
    * @param vendorName vendor name
    * @param eisType eis type
    * @param resourceadapterVersion version number for the RA
    * @param license license information
    * @param resourceadapter full qualified name of the resource adapter
    * @param description descriptions of this connector
    * @param displayName name to display for this connecotro
    * @param icon icon representing this connectore
    * @param id XML ID
    */
   public Connector15Impl(XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion,
         LicenseType license, ResourceAdapter1516 resourceadapter, List<LocalizedXsdString> description,
         List<LocalizedXsdString> displayName, List<Icon> icon, String id)
   {
      super(vendorName, eisType, license, resourceadapter, description, displayName, icon, id);
      this.resourceadapterVersion = resourceadapterVersion;
      if (!XsdString.isNull(this.resourceadapterVersion))
         this.resourceadapterVersion.setTag(Tag.RESOURCEADPTER_VERSION.toString());
   }

   /**
    * @return resourceadapterVersion
    */
   @Override
   public XsdString getResourceadapterVersion()
   {
      return resourceadapterVersion;
   }

   /**
    * Get the version.
    *
    * @return the version.
    */
   @Override
   public Version getVersion()
   {
      return Version.V_15;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((resourceadapterVersion == null) ? 0 : resourceadapterVersion.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof Connector15Impl))
         return false;
      Connector15Impl other = (Connector15Impl) obj;
      if (resourceadapterVersion == null)
      {
         if (other.resourceadapterVersion != null)
            return false;
      }
      else if (!resourceadapterVersion.equals(other.resourceadapterVersion))
         return false;
      return true;
   }

   /**
    * Xml output of default properties values for inherited classes 
    * @return xml
    */
   protected String defaultPropertiesToString()
   {
      return super.toString();
   }
   
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      sb.append("<").append("connector");
      sb.append(" " + Connector15.Attribute.VERSION + "=\"1.5\"");
      if (id != null)
         sb.append(" " + Connector15.Attribute.ID + "=\"" + id + "\"");
      sb.append(">");

      sb.append(super.toString());

      if (!XsdString.isNull(resourceadapterVersion))
         sb.append(resourceadapterVersion);

      sb.append(resourceadapter);

      sb.append("</").append("connector").append(">");

      return sb.toString();
   }

   @Override
   public CopyableMetaData copy()
   {
      return new Connector15Impl(CopyUtil.clone(vendorName), CopyUtil.clone(eisType),
            CopyUtil.clone(resourceadapterVersion), CopyUtil.clone(license),
            CopyUtil.clone((ResourceAdapter1516) resourceadapter), CopyUtil.cloneList(description),
            CopyUtil.cloneList(displayName),
            CopyUtil.cloneList(icon), CopyUtil.cloneString(id));
   }

   @Override
   public Connector merge(MergeableMetadata<?> inputMd) throws Exception
   {

      if (inputMd instanceof Connector15Impl)
      {
         Connector15Impl input15 = (Connector15Impl) inputMd;
         XsdString newResourceadapterVersion = XsdString.isNull(this.resourceadapterVersion)
               ? input15.resourceadapterVersion : this.resourceadapterVersion;
         XsdString newEisType = XsdString.isNull(this.eisType) ? input15.eisType : this.eisType;
         List<Icon> newIcons = MergeUtil.mergeList(this.icon, input15.icon);
         LicenseType newLicense = this.license == null ? input15.license : this.license.merge(input15.license);
         List<LocalizedXsdString> newDescriptions = MergeUtil.mergeList(this.description,
               input15.description);
         List<LocalizedXsdString> newDisplayNames = MergeUtil.mergeList(this.displayName,
               input15.displayName);
         XsdString newVendorName = XsdString.isNull(this.vendorName)
               ? input15.vendorName : this.vendorName;;
         ResourceAdapter1516 newResourceadapter = this.resourceadapter == null
               ? (ResourceAdapter1516) input15.resourceadapter
               : ((ResourceAdapter1516) this.resourceadapter)
                     .merge((ResourceAdapter1516) input15.resourceadapter);
         return new Connector15Impl(newVendorName, newEisType, newResourceadapterVersion, newLicense,
               newResourceadapter, newDescriptions, newDisplayNames,
               newIcons, null);
      }
      return this;

   }

}
