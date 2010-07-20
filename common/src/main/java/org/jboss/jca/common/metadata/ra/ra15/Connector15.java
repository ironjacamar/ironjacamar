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

import org.jboss.jca.common.metadata.ra.common.Connector;
import org.jboss.jca.common.metadata.ra.common.LicenseType;
import org.jboss.jca.common.metadata.ra.common.ResourceAdapter;
import org.jboss.jca.common.metadata.ra.common.XsdString;

/**
 *
 * A Connector15.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class Connector15 extends Connector
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 531372312218060928L;

   private final String version = "1.5";

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
    * @param id XML ID
    */
   public Connector15(XsdString vendorName, XsdString eisType, XsdString resourceadapterVersion,
         LicenseType license, ResourceAdapter resourceadapter, String id)
   {
      super(vendorName, eisType, license, resourceadapter, id);
      this.resourceadapterVersion = resourceadapterVersion;
   }

   /**
    * @return resourceadapterVersion
    */
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
   public String getVersion()
   {
      return version;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((resourceadapterVersion == null) ? 0 : resourceadapterVersion.hashCode());
      result = prime * result + ((version == null) ? 0 : version.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof Connector15))
         return false;
      Connector15 other = (Connector15) obj;
      if (resourceadapterVersion == null)
      {
         if (other.resourceadapterVersion != null)
            return false;
      }
      else if (!resourceadapterVersion.equals(other.resourceadapterVersion))
         return false;
      if (version == null)
      {
         if (other.version != null)
            return false;
      }
      else if (!version.equals(other.version))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "Connector15 [version=" + version + ", resourceadapterVersion=" + resourceadapterVersion + ", vendorName="
            + vendorName + ", eisType=" + eisType + ", license=" + license + ", resourceadapter=" + resourceadapter
            + ", id=" + id + "]";
   }
}
