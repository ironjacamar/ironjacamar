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
package org.ironjacamar.common.api.metadata.spec;

import org.ironjacamar.common.api.metadata.CopyableMetaData;

import java.util.List;

/**
 *
 * A ConfigProperty.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public interface ConfigProperty extends IdDecoratedMetadata, CopyableMetaData<ConfigProperty>
{
   /**
    * @return description
    */
   public List<LocalizedXsdString> getDescriptions();

   /**
    * @return configPropertyName
    */
   public XsdString getConfigPropertyName();

   /**
    * @return configPropertyType
    */
   public XsdString getConfigPropertyType();

   /**
    * @return configPropertyValue
    */
   public XsdString getConfigPropertyValue();

   /**
    * @return configPropertyIgnore
    */
   public Boolean getConfigPropertyIgnore();

   /**
    * @return configPropertySupportsDynamicUpdates
    */
   public Boolean getConfigPropertySupportsDynamicUpdates();

   /**
    * @return configPropertyConfidential
    */
   public Boolean getConfigPropertyConfidential();

   /**
    *
    * convenient method t verify if the value has already been set
    *
    * @return true if vaue has been set
    */
   public boolean isValueSet();

   /**
    * Is the config property mandatory
    * @return The value
    */
   public boolean isMandatory();

   /**
    * Get the attached classname
    * @return The value
    */
   public String getAttachedClassName();

   /**
    * Get the ignore id
    * @return The value
    */
   public String getConfigPropertyIgnoreId();

   /**
    * Get the supports dynamic id
    * @return The value
    */
   public String getConfigPropertySupportsDynamicUpdatesId();

   /**
    * Get the confidential id
    * @return The value
    */
   public String getConfigPropertyConfidentialId();
}
