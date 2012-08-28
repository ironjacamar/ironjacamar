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
package org.jboss.jca.common.metadata.ra.common;

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Icon;
import org.jboss.jca.common.api.metadata.ra.LicenseType;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra10.Connector10.Tag;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.logging.Messages;



/**
 *
 * A Connector.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public abstract class ConnectorAbstractmpl implements Connector
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -2054156739973617322L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
    * vendor name
    */
   protected final XsdString vendorName;

   /**
    * EIS type
    */
   protected final XsdString eisType;

   /**
    * license information
    */
   protected final LicenseType license;

   /**
    * resource adapter
    */
   protected final ResourceAdapter resourceadapter;

   /**
    * id attribute
    */
   protected final String id;

   /**
    * description
    */
   protected final ArrayList<LocalizedXsdString> description;

   /**
    * displayName
    */
   protected final ArrayList<LocalizedXsdString> displayName;

   /**
    * icon
    */
   protected final ArrayList<Icon> icon;

   /**
    * Create a new Connector.
    *
    * @param vendorName vandor name
    * @param eisType tyeo of EIS
    * @param license license information
    * @param resourceadapter resource adapter instance
    * @param description descriptions of this connector
    * @param displayName name to display for this connecotro
    * @param icon icon representing this connectore
    * @param id id attribute in xml file
    */
   protected ConnectorAbstractmpl(XsdString vendorName, XsdString eisType, LicenseType license,
         ResourceAdapter resourceadapter, List<LocalizedXsdString> description, List<LocalizedXsdString> displayName,
         List<Icon> icon, String id)
   {
      super();
      this.vendorName = vendorName;
      if (!XsdString.isNull(this.vendorName))
         this.vendorName.setTag(Tag.VENDOR_NAME.toString());
      
      this.eisType = eisType;
      if (!XsdString.isNull(this.eisType))
         this.eisType.setTag(Tag.EIS_TYPE.toString());
      this.license = license;
      this.resourceadapter = resourceadapter;
      this.id = id;
      if (description != null)
      {
         this.description = new ArrayList<LocalizedXsdString>(description.size());
         this.description.addAll(description);
         for (LocalizedXsdString d: this.description)
            d.setTag(Tag.DESCRIPTION.toString());
      }
      else
      {
         this.description = new ArrayList<LocalizedXsdString>(0);
      }
      if (displayName != null)
      {
         this.displayName = new ArrayList<LocalizedXsdString>(displayName.size());
         this.displayName.addAll(displayName);
         for (LocalizedXsdString d: this.displayName)
            d.setTag(Tag.DISPLAY_NAME.toString());

      }
      else
      {
         this.displayName = new ArrayList<LocalizedXsdString>(0);
      }
      if (icon != null)
      {
         this.icon = new ArrayList<Icon>(icon.size());
         this.icon.addAll(icon);
      }
      else
      {
         this.icon = new ArrayList<Icon>(0);
      }
   }

   /**
    * Get the vendorName.
    *
    * @return the vendorName.
    */
   @Override
   public XsdString getVendorName()
   {
      return vendorName;
   }

   /**
    * Get the eisType.
    *
    * @return the eisType.
    */
   @Override
   public XsdString getEisType()
   {
      return eisType;
   }

   /**
    * Get the license.
    *
    * @return the license.
    */
   @Override
   public LicenseType getLicense()
   {
      return license;
   }

   /**
    * Get the resourceadapter.
    *
    * @return the resourceadapter.
    */
   @Override
   public ResourceAdapter getResourceadapter()
   {
      return resourceadapter;
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
      return displayName == null ? null : Collections.unmodifiableList(displayName);
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
    * Get the id.
    *
    * @return the id.
    */
   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((eisType == null) ? 0 : eisType.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((license == null) ? 0 : license.hashCode());
      result = prime * result + ((resourceadapter == null) ? 0 : resourceadapter.hashCode());
      result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof ConnectorAbstractmpl))
         return false;
      ConnectorAbstractmpl other = (ConnectorAbstractmpl) obj;
      if (eisType == null)
      {
         if (other.eisType != null)
            return false;
      }
      else if (!eisType.equals(other.eisType))
         return false;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      if (license == null)
      {
         if (other.license != null)
            return false;
      }
      else if (!license.equals(other.license))
         return false;
      if (resourceadapter == null)
      {
         if (other.resourceadapter != null)
            return false;
      }
      else if (!resourceadapter.equals(other.resourceadapter))
         return false;
      if (vendorName == null)
      {
         if (other.vendorName != null)
            return false;
      }
      else if (!vendorName.equals(other.vendorName))
         return false;
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
      return true;
   }

   /**
    * Validate specification metadata
    * @exception ValidateException Thrown if an error occurs
    */
   @Override
   public void validate() throws ValidateException
   {
      ResourceAdapter ra = this.getResourceadapter();

      //make sure all need metadata parsered and processed after annotation handle
      if (ra == null)
         throw new ValidateException(bundle.noMetadataForResourceAdapter());

      //make sure ra metadata contains inbound or outbound at least
      ra.validate();
   }

   /**
    * Merge metadatas
    * @param inputMd The metadata to merge with this
    * @exception Exception Thrown if an error occurs
    * @return a new immutable connector instance result of the merging
    */
   @Override
   public Connector merge(MergeableMetadata<?> inputMd) throws Exception
   {
      return this;
   }

   @Override
   public abstract CopyableMetaData copy();

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      for (LocalizedXsdString d:description)
         sb.append(d);

      for (LocalizedXsdString n:displayName)
         sb.append(n);
      
      for (Icon i:icon)
         sb.append(i);

      if (!XsdString.isNull(vendorName))
         sb.append(vendorName);

      if (!XsdString.isNull(eisType))
         sb.append(eisType);
      
      if (license != null)
         sb.append(license);
      
      //id and resourceadapter are in implementing class
      
      return sb.toString();
   }
}
