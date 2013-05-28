/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.ra.ra17;

import org.jboss.jca.common.api.metadata.ra.Icon;
import org.jboss.jca.common.api.metadata.ra.LicenseType;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra17.Connector17;
import org.jboss.jca.common.metadata.ra.ra16.Connector16Impl;

import java.util.List;

/**
 * Implementation of Connector17
 *
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
public class Connector17Impl extends Connector16Impl implements Connector17
{
   /** The serial version uid */
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
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
   public Connector17Impl(String moduleName, XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion,
         LicenseType license, ResourceAdapter1516 resourceadapter, List<String> requiredWorkContexts,
         boolean metadataComplete, List<LocalizedXsdString> description, List<LocalizedXsdString> displayNames,
         List<Icon> icons, String id)
   {
      super(moduleName, vendorName, eisType, resourceadapterVersion, license, resourceadapter, requiredWorkContexts,
            metadataComplete, description, displayNames, icons, id);
   }

   /**
    * Constructor
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
   public Connector17Impl(XsdString moduleName, XsdString vendorName, XsdString eisType,
         XsdString resourceadapterVersion, LicenseType license, ResourceAdapter1516 resourceadapter,
         List<XsdString> requiredWorkContexts, boolean metadataComplete, List<LocalizedXsdString> description,
         List<LocalizedXsdString> displayNames, List<Icon> icons, String id)
   {
      super(moduleName, vendorName, eisType, resourceadapterVersion, license, resourceadapter, requiredWorkContexts,
            metadataComplete, description, displayNames, icons, id);
   }

   /**
    * Get the version.
    *
    * @return the version.
    */
   @Override
   public Version getVersion()
   {
      return Version.V_17;
   }

   @Override
   public int hashCode()
   {
      return super.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;

      if (!super.equals(obj))
         return false;

      if (!(obj instanceof Connector17Impl))
         return false;

      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      sb.append("<").append("connector");
      sb.append(" " + Connector17.Attribute.VERSION + "=\"1.7\"");
      sb.append(" " + Connector17.Attribute.METADATA_COMPLETE + "=\"" + metadataComplete + "\"");
      if (id != null)
         sb.append(" " + Connector17.Attribute.ID + "=\"" + id + "\"");
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
}
