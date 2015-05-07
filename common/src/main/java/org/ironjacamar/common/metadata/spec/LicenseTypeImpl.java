/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.metadata.spec;

import org.ironjacamar.common.api.metadata.CopyUtil;
import org.ironjacamar.common.api.metadata.MergeUtil;
import org.ironjacamar.common.api.metadata.spec.LicenseType;
import org.ironjacamar.common.api.metadata.spec.LocalizedXsdString;
import org.ironjacamar.common.api.metadata.spec.MergeableMetadata;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A LicenseType implementation
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LicenseTypeImpl extends AbstractMetadata implements LicenseType
{
   private static final long serialVersionUID = 1L;

   private List<LocalizedXsdString> description;

   private boolean licenseRequired;

   private String id;

   private String licReqId;

   /**
    * Constructor
    * @param description description of the license
    * @param licenseRequired mandatory boolena value
    * @param id XML ID
    * @param lrid id of licenseRequired element
    */
   public LicenseTypeImpl(List<LocalizedXsdString> description, boolean licenseRequired, String id, String lrid)
   {
      super(null);
      if (description != null)
      {
         this.description = new ArrayList<LocalizedXsdString>(description);
         for (LocalizedXsdString d: this.description)
            d.setTag(XML.ELEMENT_DESCRIPTION);
      }
      else
      {
         this.description = new ArrayList<LocalizedXsdString>(0);
      }
      this.licenseRequired = licenseRequired;
      this.id = id;
      this.licReqId = lrid;
   }

   /**
    * {@inheritDoc}
    */
   public List<LocalizedXsdString> getDescriptions()
   {
      return Collections.unmodifiableList(description);
   }

   /**
    * {@inheritDoc}
    */
   public boolean isLicenseRequired()
   {
      return licenseRequired;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   public String getLicReqId()
   {
      return licReqId;
   }

   /**
    * {@inheritDoc}
    */
   public LicenseType merge(MergeableMetadata<?> jmd) throws Exception
   {
      if (jmd instanceof LicenseTypeImpl)
      {
         LicenseTypeImpl inputLicense = (LicenseTypeImpl) jmd;

         List<LocalizedXsdString> newDescription = MergeUtil.mergeList(this.description, inputLicense.description);
         boolean newLicenseRequired = this.licenseRequired || inputLicense.licenseRequired;
         String newId = this.id == null ? inputLicense.id : this.id;
         String newlrid = this.licReqId == null ? inputLicense.licReqId : this.licReqId;
         return new LicenseTypeImpl(newDescription, newLicenseRequired, newId, newlrid);
      }
      else
      {
         return this;
      }
   }

   /**
    * {@inheritDoc}
    */
   public LicenseType copy()
   {
      return new LicenseTypeImpl(CopyUtil.cloneList(description), licenseRequired, CopyUtil.cloneString(id),
                                 CopyUtil.cloneString(licReqId));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + (licenseRequired ? 1231 : 1237);
      result = prime * result + ((licReqId == null) ? 0 : licReqId.hashCode());

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof LicenseTypeImpl))
      {
         return false;
      }
      LicenseTypeImpl other = (LicenseTypeImpl) obj;
      if (description == null)
      {
         if (other.description != null)
         {
            return false;
         }
      }
      else if (!description.equals(other.description))
      {
         return false;
      }
      if (licReqId == null)
      {
         if (other.licReqId != null)
         {
            return false;
         }
      }
      else if (!licReqId.equals(other.licReqId))
      {
         return false;
      }
      if (id == null)
      {
         if (other.id != null)
         {
            return false;
         }
      }
      else if (!id.equals(other.id))
      {
         return false;
      }
      if (licenseRequired != other.licenseRequired)
      {
         return false;
      }
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<license");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
      sb.append(">");

      if (description != null)
      {
         for (LocalizedXsdString s : description)
            sb.append(s);
      }

      sb.append("<").append(XML.ELEMENT_LICENSE_REQUIRED).append(licReqId == null ? "" : " id=\"" + licReqId + "\"")
            .append(">");
      sb.append(licenseRequired);
      sb.append("</").append(XML.ELEMENT_LICENSE_REQUIRED).append(">");

      sb.append("</license>");

      return sb.toString();
   }
}
