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


import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class ResourceAdapter implements IdDecoratedMetadata
{
   /**
    */
   private static final long serialVersionUID = 4841095296099713549L;

   private final String resourceadapterClass;

   private final List<? extends ConfigProperty> configProperty;

   private final OutboundResourceAdapter outboundResourceadapter;

   private final InboundResourceAdapter inboundResourceadapter;

   private final List<Adminobject> adminobject;

   private final List<SecurityPermission> securityPermission;

   private final String id;

   /**
    * @param resourceadapterClass full qualified name of the class
    * @param configProperty confi properties for this RA
    * @param outboundResourceadapter outbound RA
    * @param inboundResourceadapter inbound RA
    * @param adminobject list of admin objects of this RA
    * @param securityPermission supported security permissions
    * @param id XML ID
    */
   public ResourceAdapter(String resourceadapterClass, List<? extends ConfigProperty> configProperty,
         OutboundResourceAdapter outboundResourceadapter, InboundResourceAdapter inboundResourceadapter,
         List<Adminobject> adminobject, List<SecurityPermission> securityPermission, String id)
   {
      super();
      this.resourceadapterClass = resourceadapterClass;
      this.configProperty = configProperty;
      this.outboundResourceadapter = outboundResourceadapter;
      this.inboundResourceadapter = inboundResourceadapter;
      this.adminobject = adminobject;
      this.securityPermission = securityPermission;
      this.id = id;
   }

   /**
    * @return resourceadapterClass
    */
   public String getResourceadapterClass()
   {
      return resourceadapterClass;
   }

   /**
    * @return configProperty
    */
   public List<? extends ConfigProperty> getConfigProperty()
   {
      return Collections.unmodifiableList(configProperty);
   }

   /**
    * @return outboundResourceadapter
    */
   public OutboundResourceAdapter getOutboundResourceadapter()
   {
      return outboundResourceadapter;
   }

   /**
    * @return inboundResourceadapter
    */
   public InboundResourceAdapter getInboundResourceadapter()
   {
      return inboundResourceadapter;
   }

   /**
    * @return adminobject
    */
   public List<Adminobject> getAdminobject()
   {
      return Collections.unmodifiableList(adminobject);
   }

   /**
    * @return securityPermission
    */
   public List<SecurityPermission> getSecurityPermission()
   {
      return Collections.unmodifiableList(securityPermission);
   }

   /**
    * {@inheritDoc}
    *
    * @see IdDecoratedMetadata#getId()
    */
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
      result = prime * result + ((adminobject == null) ? 0 : adminobject.hashCode());
      result = prime * result + ((configProperty == null) ? 0 : configProperty.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((inboundResourceadapter == null) ? 0 : inboundResourceadapter.hashCode());
      result = prime * result + ((outboundResourceadapter == null) ? 0 : outboundResourceadapter.hashCode());
      result = prime * result + ((resourceadapterClass == null) ? 0 : resourceadapterClass.hashCode());
      result = prime * result + ((securityPermission == null) ? 0 : securityPermission.hashCode());
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
      if (!(obj instanceof ResourceAdapter))
      {
         return false;
      }
      ResourceAdapter other = (ResourceAdapter) obj;
      if (adminobject == null)
      {
         if (other.adminobject != null)
         {
            return false;
         }
      }
      else if (!adminobject.equals(other.adminobject))
      {
         return false;
      }
      if (configProperty == null)
      {
         if (other.configProperty != null)
         {
            return false;
         }
      }
      else if (!configProperty.equals(other.configProperty))
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
      if (securityPermission == null)
      {
         if (other.securityPermission != null)
         {
            return false;
         }
      }
      else if (!securityPermission.equals(other.securityPermission))
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
      return "ResourceAdapter [resourceadapterClass=" + resourceadapterClass + ", configProperty=" + configProperty
            + ", outboundResourceadapter=" + outboundResourceadapter + ", inboundResourceadapter="
            + inboundResourceadapter + ", adminobject=" + adminobject + ", securityPermission=" + securityPermission
            + ", id=" + id + "]";
   }
}
