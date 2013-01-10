/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.as.converters.weblogic;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 * Java class for outbound-resource-adapterType complex type.
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "outbound-resource-adapterType", propOrder =
      { "defaultConnectionProperties", "connectionDefinitionGroup" })
public class OutboundResourceAdapterType
{

   @XmlElement(name = "default-connection-properties")
   private ConnectionDefinitionPropertiesType defaultConnectionProperties;

   @XmlElement(name = "connection-definition-group")
   private List<ConnectionDefinitionType> connectionDefinitionGroup;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private String id;

   /**
    * 
    * This complex element is used to specify the properties at an global level. At this level, the user is 
    * able to specify parameters that apply to all outbound connection pools in the resource adapter.
    * For subelements, see default-connection-properties.
    *           
    * 
    * @return
    *     possible object is
    *     {@link ConnectionDefinitionPropertiesType }
    *     
    */
   public ConnectionDefinitionPropertiesType getDefaultConnectionProperties()
   {
      return defaultConnectionProperties;
   }

   /**
    * Sets the value of the defaultConnectionProperties property.
    * 
    * @param value
    *     allowed object is
    *     {@link ConnectionDefinitionPropertiesType }
    *     
    */
   public void setDefaultConnectionProperties(ConnectionDefinitionPropertiesType value)
   {
      this.defaultConnectionProperties = value;
   }

   /**
    * Gets the value of the connectionDefinitionGroup property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the connectionDefinitionGroup property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getConnectionDefinitionGroup().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link ConnectionDefinitionType }
    * 
    * @return connectionDefinitionGroup
    */
   public List<ConnectionDefinitionType> getConnectionDefinitionGroup()
   {
      if (connectionDefinitionGroup == null)
      {
         connectionDefinitionGroup = new ArrayList<ConnectionDefinitionType>();
      }
      return this.connectionDefinitionGroup;
   }

   /**
    * Gets the value of the id property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getId()
   {
      return id;
   }

   /**
    * Sets the value of the id property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setId(String value)
   {
      this.id = value;
   }

}
