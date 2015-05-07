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
import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.common.api.metadata.spec.LocalizedXsdString;
import org.ironjacamar.common.api.metadata.spec.XsdString;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ConfigProperty.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConfigPropertyImpl extends AbstractMetadata implements ConfigProperty
{
   private static final long serialVersionUID = 1L;

   private List<LocalizedXsdString> description;

   private XsdString configPropertyName;

   private XsdString configPropertyType;

   private XsdString configPropertyValue;

   private Boolean configPropertyIgnore;

   private Boolean configPropertySupportsDynamicUpdates;

   private Boolean configPropertyConfidential;

   private String id;

   private boolean mandatory;

   private String configPropertyIgnoreId;

   private String configPropertySupportsDynamicUpdatesId;

   private String configPropertyConfidentialId;

   private String attachedClassName;

   /**
    * Constructor
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
    * @param mandatory Is the property mandatory
    * @param attachedClassName className of the class where the property is defined by annoptation
    * @param configPropertyIgnoreId ID of configPropertyIgnore element
    * @param configPropertySupportsDynamicUpdatesId ID of configPropertySupportsDynemycUpdates element
    * @param configPropertyConfidentialId ID of configPropertyConfidential element
    */
   public ConfigPropertyImpl(List<LocalizedXsdString> description, XsdString configPropertyName,
                             XsdString configPropertyType, XsdString configPropertyValue, Boolean configPropertyIgnore,
                             Boolean configPropertySupportsDynamicUpdates, Boolean configPropertyConfidential,
                             String id, boolean mandatory,
                             String attachedClassName, String configPropertyIgnoreId,
                             String configPropertySupportsDynamicUpdatesId,
                             String configPropertyConfidentialId)
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
      this.configPropertyName = configPropertyName;
      if (!XsdString.isNull(this.configPropertyName))
         this.configPropertyName.setTag(XML.ELEMENT_CONFIG_PROPERTY_NAME);
      this.configPropertyType = configPropertyType;
      if (!XsdString.isNull(this.configPropertyType))
         this.configPropertyType.setTag(XML.ELEMENT_CONFIG_PROPERTY_TYPE);
      this.configPropertyValue = configPropertyValue;
      if (!XsdString.isNull(this.configPropertyValue))
         this.configPropertyValue.setTag(XML.ELEMENT_CONFIG_PROPERTY_VALUE);
      this.configPropertyIgnore = configPropertyIgnore;
      this.configPropertySupportsDynamicUpdates = configPropertySupportsDynamicUpdates;
      this.configPropertyConfidential = configPropertyConfidential;
      this.id = id;
      this.mandatory = mandatory;
      this.attachedClassName = attachedClassName;
      this.configPropertyIgnoreId = configPropertyIgnoreId;
      this.configPropertyConfidentialId = configPropertyConfidentialId;
      this.configPropertySupportsDynamicUpdatesId = configPropertySupportsDynamicUpdatesId;
   }

   /**
    * {@inheritDoc}
    */
   public String getAttachedClassName()
   {
      return attachedClassName;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean getConfigPropertyIgnore()
   {
      return configPropertyIgnore;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean getConfigPropertySupportsDynamicUpdates()
   {
      return configPropertySupportsDynamicUpdates;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean getConfigPropertyConfidential()
   {
      return configPropertyConfidential;
   }

   /**
    * {@inheritDoc}
    */
   public String getConfigPropertyIgnoreId()
   {
      return configPropertyIgnoreId;
   }

   /**
    * {@inheritDoc}
    */
   public String getConfigPropertyConfidentialId()
   {
      return configPropertyConfidentialId;
   }

   /**
    * {@inheritDoc}
    */
   public String getConfigPropertySupportsDynamicUpdatesId()
   {
      return configPropertySupportsDynamicUpdatesId;
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
   public XsdString getConfigPropertyType()
   {
      return configPropertyType;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getConfigPropertyValue()
   {
      return configPropertyValue;
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
   public boolean isValueSet()
   {
      return (this.getConfigPropertyValue() != null && this.getConfigPropertyValue().getValue() != null && !this
            .getConfigPropertyValue().getValue().trim().equals(""));
   }

   /**
    * Is mandatory
    * @return The value
    */
   public boolean isMandatory()
   {
      return mandatory;
   }

   /**
    * Is the config-property mandatory
    * @param v The value
    */
   public void setMandatory(boolean v)
   {
      mandatory = v;
   }

   /**
    * {@inheritDoc}
    */
   public ConfigProperty copy()
   {
      return new ConfigPropertyImpl(CopyUtil.cloneList(description), CopyUtil.clone(configPropertyName),
                                    CopyUtil.clone(configPropertyType), CopyUtil.clone(configPropertyValue),
                                    configPropertyIgnore, configPropertySupportsDynamicUpdates,
                                    configPropertyConfidential, CopyUtil.cloneString(id), mandatory,
                                    CopyUtil.cloneString(attachedClassName),
                                    CopyUtil.cloneString(configPropertyIgnoreId),
                                    CopyUtil.cloneString(configPropertySupportsDynamicUpdatesId),
                                    CopyUtil.cloneString(configPropertyConfidentialId));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((configPropertyName == null) ? 0 : configPropertyName.hashCode());
      result = prime * result + ((configPropertyType == null) ? 0 : configPropertyType.hashCode());
      result = prime * result + ((configPropertyValue == null) ? 0 : configPropertyValue.hashCode());
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + (mandatory ? 7 : 0);
      result = prime * result + ((attachedClassName == null) ? 0 : attachedClassName.hashCode());
      result = prime * result + ((configPropertyConfidential == null) ? 0 : configPropertyConfidential.hashCode());
      result = prime * result + ((configPropertyIgnore == null) ? 0 : configPropertyIgnore.hashCode());
      result = prime * result +
               ((configPropertySupportsDynamicUpdates == null) ? 0 : configPropertySupportsDynamicUpdates.hashCode());
      result = prime * result + ((configPropertyIgnoreId == null) ? 0 : configPropertyIgnoreId.hashCode());
      result = prime *
               result +
               ((configPropertySupportsDynamicUpdatesId == null) ? 0 : configPropertySupportsDynamicUpdatesId
                  .hashCode());
      result = prime * result + ((configPropertyConfidentialId == null) ? 0 : configPropertyConfidentialId.hashCode());
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

      if (mandatory != other.mandatory)
         return false;

      if (attachedClassName == null)
      {
         if (other.attachedClassName != null)
            return false;
      }
      else if (!attachedClassName.equals(other.attachedClassName))
         return false;
      if (configPropertyConfidential == null)
      {
         if (other.configPropertyConfidential != null)
            return false;
      }
      else if (!configPropertyConfidential.equals(other.configPropertyConfidential))
         return false;
      if (configPropertyIgnore == null)
      {
         if (other.configPropertyIgnore != null)
            return false;
      }
      else if (!configPropertyIgnore.equals(other.configPropertyIgnore))
         return false;
      if (configPropertySupportsDynamicUpdates == null)
      {
         if (other.configPropertySupportsDynamicUpdates != null)
            return false;
      }
      else if (!configPropertySupportsDynamicUpdates.equals(other.configPropertySupportsDynamicUpdates))
         return false;
      if (configPropertyIgnoreId == null)
      {
         if (other.configPropertyIgnoreId != null)
            return false;
      }
      else if (!configPropertyIgnoreId.equals(other.configPropertyIgnoreId))
         return false;
      if (configPropertySupportsDynamicUpdatesId == null)
      {
         if (other.configPropertySupportsDynamicUpdatesId != null)
            return false;
      }
      else if (!configPropertySupportsDynamicUpdatesId.equals(other.configPropertySupportsDynamicUpdatesId))
         return false;
      if (configPropertyConfidentialId == null)
      {
         if (other.configPropertyConfidentialId != null)
            return false;
      }
      else if (!configPropertyConfidentialId.equals(other.configPropertyConfidentialId))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<config-property");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
      sb.append(">");

      for (LocalizedXsdString d : description)
         sb.append(d);

      sb.append(configPropertyName);

      sb.append(configPropertyType);

      if (!XsdString.isNull(configPropertyValue))
         sb.append(configPropertyValue);

      if (configPropertyIgnore != null)
      {
         sb.append("<").append(XML.ELEMENT_CONFIG_PROPERTY_IGNORE)
            .append(configPropertyIgnoreId == null ? "" : " id=\"" + configPropertyIgnoreId + "\"").append(">");
         sb.append(configPropertyIgnore);
         sb.append("</").append(XML.ELEMENT_CONFIG_PROPERTY_IGNORE).append(">");
      }

      if (configPropertySupportsDynamicUpdates != null)
      {
         sb.append("<")
            .append(XML.ELEMENT_CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE)
            .append(
               configPropertySupportsDynamicUpdatesId == null ? "" : " id=\"" + configPropertySupportsDynamicUpdatesId +
                                                                     "\"").append(">");
         sb.append(configPropertySupportsDynamicUpdates);
         sb.append("</").append(XML.ELEMENT_CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE).append(">");
      }

      if (configPropertyConfidential != null)
      {
         sb.append("<").append(XML.ELEMENT_CONFIG_PROPERTY_CONFIDENTIAL)
            .append(configPropertyConfidentialId == null ? "" : " id=\"" + configPropertyConfidentialId + "\"")
            .append(">");
         sb.append(configPropertyConfidential);
         sb.append("</").append(XML.ELEMENT_CONFIG_PROPERTY_CONFIDENTIAL).append(">");
      }

      sb.append("</config-property>");

      return sb.toString();
   }
}
