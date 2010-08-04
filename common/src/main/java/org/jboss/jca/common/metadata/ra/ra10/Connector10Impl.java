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

import org.jboss.jca.common.api.metadata.ra.Icon;
import org.jboss.jca.common.api.metadata.ra.LicenseType;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra10.Connector10;
import org.jboss.jca.common.metadata.ra.common.ConnectorAbstractmpl;

import java.util.List;
import java.util.Collections;
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

   private final String moduleName;

   private final List<LocalizedXsdString> description;

   private final XsdString displayName;

   private final List<Icon> icon;

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
   public Connector10Impl(String moduleName, List<LocalizedXsdString> description, XsdString displayName,
         List<Icon> icon, XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion,
         LicenseType license, ResourceAdapter resourceadapter, String id)
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
   public XsdString getDisplayName()
   {
      return displayName;
   }

   /**
    * @return icon
    */
   @Override
   public List<Icon> getIcons()
   {
      return icon == null ? null : Collections.unmodifiableList(icon);
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
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
      result = prime * result + ((icon == null) ? 0 : icon.hashCode());
      result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
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

      return true;
   }

   @Override
   public String toString()
   {
      return "Connector10 [moduleName=" + moduleName + ", description=" + description + ", displayName=" + displayName
            + ", version=" + Version.V_10 + ", specVersion=" + Version.V_10 + ", icon=" + icon + ", vendorName="
            + vendorName
            + ", eisType=" + eisType + ", license=" + license + ", resourceadapter=" + resourceadapter + ", id=" + id
            + "]";
   }



}
