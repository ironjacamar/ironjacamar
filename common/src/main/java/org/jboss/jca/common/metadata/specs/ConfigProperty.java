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
package org.jboss.jca.common.metadata.specs;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class ConfigProperty implements IdDecoratedMetadata
{
   /**
    */
   private static final long serialVersionUID = -2025203811838727421L;

   private final List<LocalizedXsdString> description;

   private final XsdString configPropertyName;

   private final XsdString configPropertyType;

   private final XsdString configPropertyValue;

   private final Boolean configPropertyIgnore;

   private final Boolean configPropertySupportsDynamicUpdates;

   private final Boolean configPropertyConfidential;

   private final String id;

   /**
    * @param description list of descriptions
    * @param configPropertyName name of the property
    * @param configPropertyType full qualified name of the type of the property
    * @param configPropertyValue value
    * @param configPropertyIgnore not mandatory boolean value The element config-property-ignore is used to specify
    *         whether the configuration tools must ignore considering the
    *         configuration property during auto-discovery of
    *         Configuration properties. See the Connector specification for
    *         more details. If unspecified, the container must not ignore
    *         the configuration property during auto-discovery.
    * @param configPropertySupportsDynamicUpdates not mandatory The element
    *             config-property-supports-dynamic-updates is used to specify
    *             whether the configuration property allows its value to be updated, by
    *             application server's configuration tools, during the lifetime of
    *             the JavaBean instance. See the Connector specification for
    *             more details. If unspecified, the container must not dynamically
    *             reconfigure the property.
    * @param configPropertyConfidential The element config-property-confidential is used to specify
    *  whether the configuration property is confidential and
    *  recommends application server's configuration tools to use special
    *  visual aids for editing them. See the Connector specification for
    *  more details. If unspecified, the container must not treat the
    *  property as confidential.
    * @param id XML ID
    */
   public ConfigProperty(List<LocalizedXsdString> description, XsdString configPropertyName,
         XsdString configPropertyType, XsdString configPropertyValue, Boolean configPropertyIgnore,
         Boolean configPropertySupportsDynamicUpdates, Boolean configPropertyConfidential, String id)
   {
      super();
      this.description = description;
      this.configPropertyName = configPropertyName;
      this.configPropertyType = configPropertyType;
      this.configPropertyValue = configPropertyValue;
      this.configPropertyIgnore = configPropertyIgnore;
      this.configPropertySupportsDynamicUpdates = configPropertySupportsDynamicUpdates;
      this.configPropertyConfidential = configPropertyConfidential;
      this.id = id;
   }

   /**
    * @return description
    */
   public List<LocalizedXsdString> getDescription()
   {
      return Collections.unmodifiableList(description);
   }

   /**
    * @return configPropertyName
    */
   public XsdString getConfigPropertyName()
   {
      return configPropertyName;
   }

   /**
    * @return configPropertyType
    */
   public XsdString getConfigPropertyType()
   {
      return configPropertyType;
   }

   /**
    * @return configPropertyValue
    */
   public XsdString getConfigPropertyValue()
   {
      return configPropertyValue;
   }

   /**
    * @return configPropertyIgnore
    */
   public Boolean getConfigPropertyIgnore()
   {
      return configPropertyIgnore;
   }

   /**
    * @return configPropertySupportsDynamicUpdates
    */
   public Boolean getConfigPropertySupportsDynamicUpdates()
   {
      return configPropertySupportsDynamicUpdates;
   }

   /**
    * @return configPropertyConfidential
    */
   public Boolean getConfigPropertyConfidential()
   {
      return configPropertyConfidential;
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
      result = prime * result + ((configPropertyConfidential == null) ? 0 : configPropertyConfidential.hashCode());
      result = prime * result + ((configPropertyIgnore == null) ? 0 : configPropertyIgnore.hashCode());
      result = prime * result + ((configPropertyName == null) ? 0 : configPropertyName.hashCode());
      result = prime * result
            + ((configPropertySupportsDynamicUpdates == null) ? 0 : configPropertySupportsDynamicUpdates.hashCode());
      result = prime * result + ((configPropertyType == null) ? 0 : configPropertyType.hashCode());
      result = prime * result + ((configPropertyValue == null) ? 0 : configPropertyValue.hashCode());
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
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
      if (!(obj instanceof ConfigProperty))
      {
         return false;
      }
      ConfigProperty other = (ConfigProperty) obj;
      if (configPropertyConfidential == null)
      {
         if (other.configPropertyConfidential != null)
         {
            return false;
         }
      }
      else if (!configPropertyConfidential.equals(other.configPropertyConfidential))
      {
         return false;
      }
      if (configPropertyIgnore == null)
      {
         if (other.configPropertyIgnore != null)
         {
            return false;
         }
      }
      else if (!configPropertyIgnore.equals(other.configPropertyIgnore))
      {
         return false;
      }
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
      if (configPropertySupportsDynamicUpdates == null)
      {
         if (other.configPropertySupportsDynamicUpdates != null)
         {
            return false;
         }
      }
      else if (!configPropertySupportsDynamicUpdates.equals(other.configPropertySupportsDynamicUpdates))
      {
         return false;
      }
      if (configPropertyType == null)
      {
         if (other.configPropertyType != null)
         {
            return false;
         }
      }
      else if (!configPropertyType.equals(other.configPropertyType))
      {
         return false;
      }
      if (configPropertyValue == null)
      {
         if (other.configPropertyValue != null)
         {
            return false;
         }
      }
      else if (!configPropertyValue.equals(other.configPropertyValue))
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
    *
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "ConfigProperty [description=" + description + ", configPropertyName=" + configPropertyName
            + ", configPropertyType=" + configPropertyType + ", configPropertyValue=" + configPropertyValue
            + ", configPropertyIgnore=" + configPropertyIgnore + ", configPropertySupportsDynamicUpdates="
            + configPropertySupportsDynamicUpdates + ", configPropertyConfidential=" + configPropertyConfidential
            + ", id=" + id + "]";
   }
}
