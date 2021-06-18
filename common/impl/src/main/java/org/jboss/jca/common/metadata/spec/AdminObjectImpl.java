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
package org.jboss.jca.common.metadata.spec;

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.spec.AdminObject;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.XsdString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An admin object implementation
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AdminObjectImpl implements AdminObject
{
   private static final long serialVersionUID = 1L;

   private XsdString adminobjectInterface;

   private XsdString adminobjectClass;

   private List<ConfigProperty> configProperties;

   private String id;

   /**
    * @param adminobjectInterface full qualified name of the interface
    * @param adminobjectClass full qualified name of the implementation class
    * @param configProperty List of config propeties
    * @param id xmlid
    */
   public AdminObjectImpl(XsdString adminobjectInterface, XsdString adminobjectClass,
                          List<ConfigProperty> configProperty, String id)
   {
      this.adminobjectInterface = adminobjectInterface;
      if (!XsdString.isNull(this.adminobjectInterface))
         this.adminobjectInterface.setTag(XML.AdminObjectTag.ADMINOBJECT_INTERFACE.toString());
      this.adminobjectClass = adminobjectClass;
      if (!XsdString.isNull(this.adminobjectClass))
         this.adminobjectClass.setTag(XML.AdminObjectTag.ADMINOBJECT_CLASS.toString());
      if (configProperty != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(configProperty);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
      this.id = id;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getAdminobjectInterface()
   {
      return adminobjectInterface;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getAdminobjectClass()
   {
      return adminobjectClass;
   }

   /**
    * {@inheritDoc}
    */
   public List<ConfigProperty> getConfigProperties()
   {
      return Collections.unmodifiableList(configProperties);
   }

   /**
    * Force configProperties with new content.
    * This method is thread safe
    *
    * @param newContents the list of new properties
    */
   public synchronized void forceConfigProperties(List<ConfigProperty> newContents)
   {
      if (newContents != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(newContents);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
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
   public CopyableMetaData copy()
   {
      return new AdminObjectImpl(CopyUtil.clone(adminobjectInterface), CopyUtil.clone(adminobjectClass),
                                 CopyUtil.cloneList(configProperties), CopyUtil.cloneString(id));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((adminobjectClass == null) ? 0 : adminobjectClass.hashCode());
      result = prime * result + ((adminobjectInterface == null) ? 0 : adminobjectInterface.hashCode());
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof AdminObjectImpl))
         return false;
      AdminObjectImpl other = (AdminObjectImpl) obj;
      if (adminobjectClass == null)
      {
         if (other.adminobjectClass != null)
            return false;
      }
      else if (!adminobjectClass.equals(other.adminobjectClass))
         return false;
      if (adminobjectInterface == null)
      {
         if (other.adminobjectInterface != null)
            return false;
      }
      else if (!adminobjectInterface.equals(other.adminobjectInterface))
         return false;
      if (configProperties == null)
      {
         if (other.configProperties != null)
            return false;
      }
      else if (!configProperties.equals(other.configProperties))
         return false;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<adminobject");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
      sb.append(">");

      sb.append(adminobjectInterface);

      sb.append(adminobjectClass);

      if (configProperties != null)
      {
         for (ConfigProperty cp : configProperties)
         {
            sb.append(cp);
         }
      }

      sb.append("</adminobject>");
      
      return sb.toString();
   }
}
