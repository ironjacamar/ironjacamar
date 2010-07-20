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

/**
 *
 * A Connector.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public abstract class Connector implements IdDecoratedMetadata
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -2054156739973617322L;

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
    * Create a new Connector.
    *
    * @param vendorName vandor name
    * @param eisType tyeo of EIS
    * @param license license information
    * @param resourceadapter resource adapter instance
    * @param id id attribute in xml file
    */
   protected Connector(XsdString vendorName, XsdString eisType, LicenseType license, ResourceAdapter resourceadapter,
         String id)
   {
      super();
      this.vendorName = vendorName;
      this.eisType = eisType;
      this.license = license;
      this.resourceadapter = resourceadapter;
      this.id = id;
   }

   /**
    * Get the vendorName.
    *
    * @return the vendorName.
    */
   public XsdString getVendorName()
   {
      return vendorName;
   }

   /**
    * Get the eisType.
    *
    * @return the eisType.
    */
   public XsdString getEisType()
   {
      return eisType;
   }

   /**
    * Get the license.
    *
    * @return the license.
    */
   public LicenseType getLicense()
   {
      return license;
   }

   /**
    * Get the resourceadapter.
    *
    * @return the resourceadapter.
    */
   public ResourceAdapter getResourceadapter()
   {
      return resourceadapter;
   }

   /**
    * Get the version.
    *
    * @return the version.
    */
   public abstract String getVersion();

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
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof Connector))
         return false;
      Connector other = (Connector) obj;
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
      return true;
   }

   @Override
   public String toString()
   {
      return "Connector [vendorName=" + vendorName + ", eisType=" + eisType + ", license=" + license
            + ", resourceadapter=" + resourceadapter + ", id=" + id + "]";
   }

}
