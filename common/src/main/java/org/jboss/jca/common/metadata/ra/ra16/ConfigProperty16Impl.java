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
package org.jboss.jca.common.metadata.ra.ra16;

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16;
import org.jboss.jca.common.metadata.ra.common.ConfigPropertyImpl;

import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class ConfigProperty16Impl extends ConfigPropertyImpl implements ConfigProperty16
{
   /**
    */
   private static final long serialVersionUID = -2025203811838727421L;

   private final Boolean configPropertyIgnore;

   private final Boolean configPropertySupportsDynamicUpdates;

   private final Boolean configPropertyConfidential;

   private final String ignoreId;

   private final String updatesId;

   private final String confidId;

   private final String attachedClassName;

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
   public ConfigProperty16Impl(List<LocalizedXsdString> description, XsdString configPropertyName,
         XsdString configPropertyType, XsdString configPropertyValue, Boolean configPropertyIgnore,
         Boolean configPropertySupportsDynamicUpdates, Boolean configPropertyConfidential, String id)
   {
      this(description, configPropertyName, configPropertyType, configPropertyValue, configPropertyIgnore,
            configPropertySupportsDynamicUpdates, configPropertyConfidential, id, null);
   }

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
    * @param attachedClassName className of the class where the property is defined by annoptation
    */
   public ConfigProperty16Impl(List<LocalizedXsdString> description, XsdString configPropertyName,
         XsdString configPropertyType, XsdString configPropertyValue, Boolean configPropertyIgnore,
         Boolean configPropertySupportsDynamicUpdates, Boolean configPropertyConfidential, String id,
         String attachedClassName)
   {
      this(description, configPropertyName, configPropertyType, configPropertyValue, configPropertyIgnore,
            configPropertySupportsDynamicUpdates, configPropertyConfidential, id, attachedClassName, null, null, null);
   }

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
    * @param attachedClassName className of the class where the property is defined by annoptation
    * @param ignoreId ID of configPropertyIgnore element
    * @param updatesId ID of configPropertySupportsDynemycUpdates element
    * @param confidId ID of configPropertyConfidential element
    */
   public ConfigProperty16Impl(List<LocalizedXsdString> description, XsdString configPropertyName,
         XsdString configPropertyType, XsdString configPropertyValue, Boolean configPropertyIgnore,
         Boolean configPropertySupportsDynamicUpdates, Boolean configPropertyConfidential, String id,
         String attachedClassName, String ignoreId, String updatesId, String confidId)
   {
      super(description, configPropertyName, configPropertyType, configPropertyValue, id);
      this.configPropertyIgnore = configPropertyIgnore;
      this.configPropertySupportsDynamicUpdates = configPropertySupportsDynamicUpdates;
      this.configPropertyConfidential = configPropertyConfidential;
      this.attachedClassName = attachedClassName;
      this.ignoreId = ignoreId;
      this.confidId = confidId;
      this.updatesId = updatesId;
   }

   /**
    * Get the attachedClassName.
    *
    * @return the attachedClassName.
    */
   public final String getAttachedClassName()
   {
      return attachedClassName;
   }

   /**
    * @return configPropertyIgnore
    */
   @Override
   public Boolean getConfigPropertyIgnore()
   {
      return configPropertyIgnore;
   }

   /**
    * @return configPropertySupportsDynamicUpdates
    */
   @Override
   public Boolean getConfigPropertySupportsDynamicUpdates()
   {
      return configPropertySupportsDynamicUpdates;
   }

   /**
    * @return configPropertyConfidential
    */
   @Override
   public Boolean getConfigPropertyConfidential()
   {
      return configPropertyConfidential;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((attachedClassName == null) ? 0 : attachedClassName.hashCode());
      result = prime * result + ((configPropertyConfidential == null) ? 0 : configPropertyConfidential.hashCode());
      result = prime * result + ((configPropertyIgnore == null) ? 0 : configPropertyIgnore.hashCode());
      result = prime * result
            + ((configPropertySupportsDynamicUpdates == null) ? 0 : configPropertySupportsDynamicUpdates.hashCode());
      result = prime * result + ((ignoreId == null) ? 0 : ignoreId.hashCode());
      result = prime * result + ((updatesId == null) ? 0 : updatesId.hashCode());
      result = prime * result + ((confidId == null) ? 0 : confidId.hashCode());

      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof ConfigProperty16Impl))
         return false;
      ConfigProperty16Impl other = (ConfigProperty16Impl) obj;
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
      if (ignoreId == null)
      {
         if (other.ignoreId != null)
            return false;
      }
      else if (!ignoreId.equals(other.ignoreId))
         return false;
      if (updatesId == null)
      {
         if (other.updatesId != null)
            return false;
      }
      else if (!updatesId.equals(other.updatesId))
         return false;
      if (confidId == null)
      {
         if (other.confidId != null)
            return false;
      }
      else if (!confidId.equals(other.confidId))
         return false;

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
      StringBuilder sb = new StringBuilder();

      sb.append("<config-property");
      if (id != null)
         sb.append(" ").append(ConfigProperty16.Attribute.ID).append("=\"").append(id).append("\"");
      sb.append(">");

      for (LocalizedXsdString d : description)
         sb.append(d);

      sb.append(configPropertyName);

      sb.append(configPropertyType);

      if (!XsdString.isNull(configPropertyValue))
         sb.append(configPropertyValue);

      if (configPropertyIgnore != null)
      {
         sb.append("<").append(ConfigProperty16.Tag.CONFIG_PROPERTY_IGNORE)
               .append(ignoreId == null ? "" : " id=\"" + ignoreId + "\"").append(">");
         sb.append(configPropertyIgnore);
         sb.append("</").append(ConfigProperty16.Tag.CONFIG_PROPERTY_IGNORE).append(">");
      }

      if (configPropertySupportsDynamicUpdates != null)
      {
         sb.append("<").append(ConfigProperty16.Tag.CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE)
               .append(updatesId == null ? "" : " id=\"" + updatesId + "\"").append(">");
         sb.append(configPropertySupportsDynamicUpdates);
         sb.append("</").append(ConfigProperty16.Tag.CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE).append(">");
      }

      if (configPropertyConfidential != null)
      {
         sb.append("<").append(ConfigProperty16.Tag.CONFIG_PROPERTY_CONFIDENTIAL)
               .append(confidId == null ? "" : " id=\"" + confidId + "\"").append(">");
         sb.append(configPropertyConfidential);
         sb.append("</").append(ConfigProperty16.Tag.CONFIG_PROPERTY_CONFIDENTIAL).append(">");
      }

      sb.append("</config-property>");

      return sb.toString();
   }

   @Override
   public CopyableMetaData copy()
   {
      return new ConfigProperty16Impl(CopyUtil.cloneList(description), CopyUtil.clone(configPropertyName),
            CopyUtil.clone(configPropertyType), CopyUtil.clone(configPropertyValue), configPropertyIgnore,
            configPropertySupportsDynamicUpdates, configPropertyConfidential, CopyUtil.cloneString(id),
            CopyUtil.cloneString(attachedClassName), CopyUtil.cloneString(ignoreId), CopyUtil.cloneString(updatesId),
            CopyUtil.cloneString(confidId));
   }

}
