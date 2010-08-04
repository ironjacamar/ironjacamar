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
package org.jboss.jca.common.metadataimpl.ra.common;


import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.XsdString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class AdminObjectImpl implements AdminObject
{
   /**
     */
   private static final long serialVersionUID = -7653991400977178783L;

   private final XsdString adminobjectInterface;

   private final XsdString adminobjectClass;

   private final ArrayList<? extends ConfigProperty> configProperty;

   private final String id;

   /**
    * @param adminobjectInterface full qualified name of the interface
    * @param adminobjectClass full qualified name of the implementation class
    * @param configProperty ArrayList of config propeties
    * @param id xmlid
    */
   public AdminObjectImpl(final XsdString adminobjectInterface, final XsdString adminobjectClass,
         final ArrayList<? extends ConfigProperty> configProperty, final String id)
   {
      super();
      this.adminobjectInterface = adminobjectInterface;
      this.adminobjectClass = adminobjectClass;
      this.configProperty = configProperty;
      this.id = id;
   }

   /**
    * @return adminobjectInterface
    */
   @Override
   public XsdString getAdminobjectInterface()
   {
      return adminobjectInterface;
   }

   /**
    * @return adminobjectClass
    */
   @Override
   public XsdString getAdminobjectClass()
   {
      return adminobjectClass;
   }

   /**
    * @return configProperty
    */
   @Override
   public List<? extends ConfigProperty> getConfigProperties()
   {
      return configProperty == null ? null : Collections.unmodifiableList(configProperty);
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
      result = prime * result + ((adminobjectClass == null) ? 0 : adminobjectClass.hashCode());
      result = prime * result + ((adminobjectInterface == null) ? 0 : adminobjectInterface.hashCode());
      result = prime * result + ((configProperty == null) ? 0 : configProperty.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(final Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof AdminObjectImpl))
      {
         return false;
      }
      final AdminObjectImpl other = (AdminObjectImpl) obj;
      if (adminobjectClass == null)
      {
         if (other.adminobjectClass != null)
         {
            return false;
         }
      }
      else if (!adminobjectClass.equals(other.adminobjectClass))
      {
         return false;
      }
      if (adminobjectInterface == null)
      {
         if (other.adminobjectInterface != null)
         {
            return false;
         }
      }
      else if (!adminobjectInterface.equals(other.adminobjectInterface))
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
      return "Adminobject [adminobjectInterface=" + adminobjectInterface + ", adminobjectClass=" + adminobjectClass
            + ", configProperty=" + configProperty + ", id=" + id + "]";
   }


}
