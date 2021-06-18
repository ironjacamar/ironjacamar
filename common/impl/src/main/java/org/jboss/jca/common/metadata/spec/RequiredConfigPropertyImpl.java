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
import org.jboss.jca.common.api.metadata.spec.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.spec.RequiredConfigProperty;
import org.jboss.jca.common.api.metadata.spec.XsdString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A RequiredConfigProperty implementation
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class RequiredConfigPropertyImpl implements RequiredConfigProperty
{
   private static final long serialVersionUID = 4299927051352998447L;

   private List<LocalizedXsdString> description;

   private XsdString configPropertyName;

   private String id;

   /**
    * Constructor
    * @param description descriptions of this property
    * @param configPropertyName name of the property
    * @param id XML ID
    */
   public RequiredConfigPropertyImpl(List<LocalizedXsdString> description, XsdString configPropertyName, String id)
   {
      if (description != null)
      {
         this.description = new ArrayList<LocalizedXsdString>(description);
         for (LocalizedXsdString d: this.description)
            d.setTag(XML.RequiredConfigPropertyTag.DESCRIPTION.toString());
      }
      else
      {
         this.description = new ArrayList<LocalizedXsdString>(0);
      }
      this.configPropertyName = configPropertyName;
      if (!XsdString.isNull(this.configPropertyName))
         this.configPropertyName.setTag(XML.RequiredConfigPropertyTag.CONFIG_PROPERTY_NAME.toString());
      this.id = id;
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
   public XsdString getConfigPropertyName()
   {
      return configPropertyName;
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
      return new RequiredConfigPropertyImpl(CopyUtil.cloneList(description), CopyUtil.clone(configPropertyName),
                                            CopyUtil.cloneString(id));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((configPropertyName == null) ? 0 : configPropertyName.hashCode());
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
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
      if (!(obj instanceof RequiredConfigPropertyImpl))
      {
         return false;
      }
      RequiredConfigPropertyImpl other = (RequiredConfigPropertyImpl) obj;
      if (configPropertyName == null)
      {
         if (other.configPropertyName != null)
         {
            return false;
         }
      }
      else if (!configPropertyName.equals(other.configPropertyName))
      {
         return false;
      }
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
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<required-config-property");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
      sb.append(">");

      for (LocalizedXsdString d: description)
         sb.append(d);

      if (!XsdString.isNull(configPropertyName))
         sb.append(configPropertyName);

      sb.append("</required-config-property>");

      return sb.toString();
   }
}
