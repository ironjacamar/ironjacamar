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

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty.Tag;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.XsdString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * A ConfigProperty.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class ConfigPropertyImpl implements ConfigProperty
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 4840388990647778928L;

   /**
    * description
    */
   protected final ArrayList<LocalizedXsdString> description;

   /**
    * configPropertyName
    */
   protected final XsdString configPropertyName;

   /**
    * configPropertyType
    */
   protected final XsdString configPropertyType;

   /**
    * configPropertyValue
    */
   protected final XsdString configPropertyValue;

   /**
    * id
    */
   protected final String id;

   /**
    * Create a new ConfigProperty15.
    *
    * @param description the description
    * @param configPropertyName name of config-property
    * @param configPropertyType type of config-property
    * @param configPropertyValue value of config-property
    * @param id id attribute in xml file
    */
   public ConfigPropertyImpl(List<LocalizedXsdString> description, XsdString configPropertyName,
         XsdString configPropertyType, XsdString configPropertyValue, String id)
   {
      super();
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
      this.configPropertyName = configPropertyName;
      if (!XsdString.isNull(this.configPropertyName))
         this.configPropertyName.setTag(Tag.CONFIG_PROPERTY_NAME.toString());
      this.configPropertyType = configPropertyType;
      if (!XsdString.isNull(this.configPropertyType))
         this.configPropertyType.setTag(Tag.CONFIG_PROPERTY_TYPE.toString());
      this.configPropertyValue = configPropertyValue;
      if (!XsdString.isNull(this.configPropertyValue))
         this.configPropertyValue.setTag(Tag.CONFIG_PROPERTY_VALUE.toString());
      this.id = id;
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
    * @return configPropertyName
    */
   @Override
   public XsdString getConfigPropertyName()
   {
      return configPropertyName;
   }

   /**
    * @return configPropertyType
    */
   @Override
   public XsdString getConfigPropertyType()
   {
      return configPropertyType;
   }

   /**
    * @return configPropertyValue
    */
   @Override
   public XsdString getConfigPropertyValue()
   {
      return configPropertyValue;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public boolean isValueSet()
   {
      return (this.getConfigPropertyValue() != null && this.getConfigPropertyValue().getValue() != null && !this
            .getConfigPropertyValue().getValue().trim().equals(""));
   }

   @Override
   public CopyableMetaData copy()
   {
      return new ConfigPropertyImpl(CopyUtil.cloneList(description), CopyUtil.clone(configPropertyName),
            CopyUtil.clone(configPropertyType), CopyUtil.clone(configPropertyValue), CopyUtil.cloneString(id));
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((configPropertyName == null) ? 0 : configPropertyName.hashCode());
      result = prime * result + ((configPropertyType == null) ? 0 : configPropertyType.hashCode());
      result = prime * result + ((configPropertyValue == null) ? 0 : configPropertyValue.hashCode());
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof ConfigPropertyImpl))
         return false;
      ConfigPropertyImpl other = (ConfigPropertyImpl) obj;
      if (configPropertyName == null)
      {
         if (other.configPropertyName != null)
            return false;
      }
      else if (!configPropertyName.equals(other.configPropertyName))
         return false;
      if (configPropertyType == null)
      {
         if (other.configPropertyType != null)
            return false;
      }
      else if (!configPropertyType.equals(other.configPropertyType))
         return false;
      if (configPropertyValue == null)
      {
         if (other.configPropertyValue != null)
            return false;
      }
      else if (!configPropertyValue.equals(other.configPropertyValue))
         return false;
      if (description == null)
      {
         if (other.description != null)
            return false;
      }
      else if (!description.equals(other.description))
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

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<config-property");
      if (id != null)
         sb.append(" ").append(ConfigProperty.Attribute.ID).append("=\"").append(id).append("\"");
      sb.append(">");

      for (LocalizedXsdString d : description)
         sb.append(d);

      sb.append(configPropertyName);

      sb.append(configPropertyType);

      if (!XsdString.isNull(configPropertyValue))
      {
         sb.append(configPropertyValue);
      }

      sb.append("</config-property>");

      return sb.toString();
   }
}
