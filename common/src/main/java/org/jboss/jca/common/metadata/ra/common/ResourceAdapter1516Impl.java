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

import org.jboss.jca.common.api.metadata.MergeUtil;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.InboundResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.OutboundResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.SecurityPermission;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class ResourceAdapter1516Impl implements ResourceAdapter1516
{
   /**
    */
   private static final long serialVersionUID = 4841095296099713549L;

   private final String resourceadapterClass;

   private final ArrayList<ConfigProperty> configProperties;

   private final OutboundResourceAdapter outboundResourceadapter;

   private final InboundResourceAdapter inboundResourceadapter;

   private final ArrayList<AdminObject> adminobjects;

   private final ArrayList<SecurityPermission> securityPermissions;

   private final String id;

   /**
    * @param resourceadapterClass full qualified name of the class
    * @param configProperties confi properties for this RA
    * @param outboundResourceadapter outbound RA
    * @param inboundResourceadapter inbound RA
    * @param adminobjects list of admin objects of this RA
    * @param securityPermissions supported security permissions
    * @param id XML ID
    */
   public ResourceAdapter1516Impl(String resourceadapterClass, List<? extends ConfigProperty> configProperties,
         OutboundResourceAdapter outboundResourceadapter, InboundResourceAdapter inboundResourceadapter,
         List<AdminObject> adminobjects, List<SecurityPermission> securityPermissions, String id)
   {
      super();
      this.resourceadapterClass = resourceadapterClass;
      if (configProperties != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(configProperties.size());
         this.configProperties.addAll(configProperties);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
      this.outboundResourceadapter = outboundResourceadapter;
      this.inboundResourceadapter = inboundResourceadapter;
      if (adminobjects != null)
      {
         this.adminobjects = new ArrayList<AdminObject>(adminobjects.size());
         this.adminobjects.addAll(adminobjects);
      }
      else
      {
         this.adminobjects = new ArrayList<AdminObject>(0);
      }
      if (securityPermissions != null)
      {
         this.securityPermissions = new ArrayList<SecurityPermission>(securityPermissions.size());
         this.securityPermissions.addAll(securityPermissions);
      }
      else
      {
         this.securityPermissions = new ArrayList<SecurityPermission>(0);
      }
      this.id = id;
   }

   /**
    * @return resourceadapterClass
    */
   @Override
   public String getResourceadapterClass()
   {
      return resourceadapterClass;
   }

   /**
    * @return configProperty
    */
   @Override
   public List<? extends ConfigProperty> getConfigProperties()
   {
      return configProperties == null ? null : Collections.unmodifiableList(configProperties);
   }

   /**
    * @return outboundResourceadapter
    */
   @Override
   public OutboundResourceAdapter getOutboundResourceadapter()
   {
      return outboundResourceadapter;
   }

   /**
    * @return inboundResourceadapter
    */
   @Override
   public InboundResourceAdapter getInboundResourceadapter()
   {
      return inboundResourceadapter;
   }

   /**
    * @return adminobject
    */
   @Override
   public List<AdminObject> getAdminobjects()
   {
      return adminobjects == null ? null : Collections.unmodifiableList(adminobjects);
   }

   /**
    * @return securityPermission
    */
   @Override
   public List<SecurityPermission> getSecurityPermissions()
   {
      return securityPermissions == null ? null : Collections.unmodifiableList(securityPermissions);
   }

   @Override
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((adminobjects == null) ? 0 : adminobjects.hashCode());
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((inboundResourceadapter == null) ? 0 : inboundResourceadapter.hashCode());
      result = prime * result + ((outboundResourceadapter == null) ? 0 : outboundResourceadapter.hashCode());
      result = prime * result + ((resourceadapterClass == null) ? 0 : resourceadapterClass.hashCode());
      result = prime * result + ((securityPermissions == null) ? 0 : securityPermissions.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
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
      if (!(obj instanceof ResourceAdapter1516Impl))
      {
         return false;
      }
      ResourceAdapter1516Impl other = (ResourceAdapter1516Impl) obj;
      if (adminobjects == null)
      {
         if (other.adminobjects != null)
         {
            return false;
         }
      }
      else if (!adminobjects.equals(other.adminobjects))
      {
         return false;
      }
      if (configProperties == null)
      {
         if (other.configProperties != null)
         {
            return false;
         }
      }
      else if (!configProperties.equals(other.configProperties))
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
      if (inboundResourceadapter == null)
      {
         if (other.inboundResourceadapter != null)
         {
            return false;
         }
      }
      else if (!inboundResourceadapter.equals(other.inboundResourceadapter))
      {
         return false;
      }
      if (outboundResourceadapter == null)
      {
         if (other.outboundResourceadapter != null)
         {
            return false;
         }
      }
      else if (!outboundResourceadapter.equals(other.outboundResourceadapter))
      {
         return false;
      }
      if (resourceadapterClass == null)
      {
         if (other.resourceadapterClass != null)
         {
            return false;
         }
      }
      else if (!resourceadapterClass.equals(other.resourceadapterClass))
      {
         return false;
      }
      if (securityPermissions == null)
      {
         if (other.securityPermissions != null)
         {
            return false;
         }
      }
      else if (!securityPermissions.equals(other.securityPermissions))
      {
         return false;
      }
      return true;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "ResourceAdapter [resourceadapterClass=" + resourceadapterClass + ", configProperties=" + configProperties
            + ", outboundResourceadapter=" + outboundResourceadapter + ", inboundResourceadapter="
            + inboundResourceadapter + ", adminobjects=" + adminobjects + ", securityPermission=" + securityPermissions
            + ", id=" + id + "]";
   }

   @Override
   public void validate() throws ValidateException
   {
      boolean inboundOrOutbound = false;
      if (this.getOutboundResourceadapter() != null && this.getOutboundResourceadapter().validationAsBoolean())
         inboundOrOutbound = true;
      if (this.getInboundResourceadapter() != null && this.getInboundResourceadapter().validationAsBoolean()
            && this.getResourceadapterClass() != null)
         inboundOrOutbound = true;
      if (!inboundOrOutbound)
         throw new ValidateException("ResourceAdapter metadata should contains inbound or outbound at least");

   }

   @Override
   public ResourceAdapter1516 merge(MergeableMetadata<?> jmd) throws Exception
   {
      if (jmd instanceof ResourceAdapter1516Impl)
      {
         ResourceAdapter1516Impl inputRA = (ResourceAdapter1516Impl) jmd;

         InboundResourceAdapter newInboundResourceadapter = this.inboundResourceadapter == null
               ? inputRA.inboundResourceadapter
               : this.inboundResourceadapter
               .merge(inputRA.inboundResourceadapter);

         OutboundResourceAdapter newOutboundResourceadapter = this.outboundResourceadapter == null
               ? inputRA.outboundResourceadapter
               : this.outboundResourceadapter
               .merge(inputRA.outboundResourceadapter);
         List<SecurityPermission> newSecurityPermission = MergeUtil.mergeList(this.securityPermissions,
               inputRA.securityPermissions);
         List<? extends ConfigProperty> newConfigProperties = MergeUtil.mergeConfigList(
               this.configProperties,
               inputRA.configProperties);
         List<AdminObject> newAdminobjects = MergeUtil.mergeList(this.adminobjects, inputRA.adminobjects);
         String newId = this.id == null ? inputRA.id : this.id;
         String newResourceadapterClass = this.resourceadapterClass == null
               ? inputRA.resourceadapterClass
               : this.resourceadapterClass;
         return new ResourceAdapter1516Impl(newResourceadapterClass, newConfigProperties, newOutboundResourceadapter,
               newInboundResourceadapter, newAdminobjects, newSecurityPermission, newId);
      }
      else
      {
         return this;
      }
   }
}
