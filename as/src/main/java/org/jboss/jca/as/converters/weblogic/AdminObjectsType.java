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
 * <p>Java class for admin-objectsType complex type.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "admin-objectsType", propOrder =
      { "defaultProperties", "adminObjectGroup" })
public class AdminObjectsType
{

   @XmlElement(name = "default-properties")
   private ConfigPropertiesType defaultProperties;

   @XmlElement(name = "admin-object-group")
   private List<AdminObjectGroupType> adminObjectGroup;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private String id;

   /**
    * 
    *  Specifies the default properties that apply to all admin objects (at the global level) in the resource adapter.
    *  The default-properties element can contain one or more property elements, each holding a name and value pair. 
    *  See properties.
    *           
    * 
    * @return
    *     possible object is
    *     {@link ConfigPropertiesType }
    *     
    */
   public ConfigPropertiesType getDefaultProperties()
   {
      return defaultProperties;
   }

   /**
    * Sets the value of the defaultProperties property.
    * 
    * @param value
    *     allowed object is
    *     {@link ConfigPropertiesType }
    *     
    */
   public void setDefaultProperties(ConfigPropertiesType value)
   {
      this.defaultProperties = value;
   }

   /**
    * Gets the value of the adminObjectGroup property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the adminObjectGroup property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getAdminObjectGroup().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link AdminObjectGroupType }
    * 
    * @return adminObjectGroup
    */
   public List<AdminObjectGroupType> getAdminObjectGroup()
   {
      if (adminObjectGroup == null)
      {
         adminObjectGroup = new ArrayList<AdminObjectGroupType>();
      }
      return this.adminObjectGroup;
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
