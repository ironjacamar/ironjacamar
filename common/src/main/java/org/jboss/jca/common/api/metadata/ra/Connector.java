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
package org.jboss.jca.common.api.metadata.ra;

import org.jboss.jca.common.api.metadata.CopyableMetaData;

import java.util.List;

/**
 *
 * A Connector.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public interface Connector
      extends
         IdDecoratedMetadata,
         ValidatableMetadata,
         MergeableMetadata<Connector>,
         CopyableMetaData
{

   /**
    * Get the vendorName.
    *
    * @return the vendorName.
    */
   public abstract XsdString getVendorName();

   /**
    * Get the eisType.
    *
    * @return the eisType.
    */
   public abstract XsdString getEisType();

   /**
    * Get the license.
    *
    * @return the license.
    */
   public abstract LicenseType getLicense();

   /**
    * Get the resourceadapter.
    *
    * @return the resourceadapter.
    */
   public abstract ResourceAdapter getResourceadapter();

   /**
    * @return description
    */
   public abstract List<LocalizedXsdString> getDescriptions();

   /**
    * @return displayName
    */
   public abstract List<LocalizedXsdString> getDisplayNames();

   /**
    * @return icon
    */
   public abstract List<Icon> getIcons();

   /**
    * Get the version.
    *
    * @return the version.
    */
   public abstract Version getVersion();

   @Override
   public abstract int hashCode();

   @Override
   public abstract boolean equals(Object obj);

   @Override
   public abstract String toString();

   /**
    *
    * A Version enumeration
    *
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
    *
    */
   public enum Version
   {
      /**
       * 1.0
       */
      V_10,
      /**
       * 1.5
       */
      V_15,
      /**
       * 1.6
       */
      V_16;
   }

}
