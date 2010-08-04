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


import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.IdDecoratedMetadata;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.XsdString;

import java.util.List;
import java.util.Collections;
import java.util.List;

/**
 *
 * A ConfigProperty.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class ConfigPropertyImpl implements IdDecoratedMetadata, ConfigProperty
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 4840388990647778928L;

   /**
    * description
    */
   protected final List<LocalizedXsdString> description;

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
      this.description = description;
      this.configPropertyName = configPropertyName;
      this.configPropertyType = configPropertyType;
      this.configPropertyValue = configPropertyValue;
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

   @Override
   public boolean isValueSet()
   {
      return (this.getConfigPropertyValue() != null && this.getConfigPropertyValue().getValue() != null && !this
            .getConfigPropertyValue().getValue().trim().equals(""));
   }

}
