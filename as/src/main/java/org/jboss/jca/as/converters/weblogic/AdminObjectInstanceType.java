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
 * Java class for admin-object-instanceType complex type.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "admin-object-instanceType", propOrder =
      { "jndiName", "properties" })
public class AdminObjectInstanceType
{

   @XmlElement(name = "jndi-name", required = true)
   private String jndiName;

   private ConfigPropertiesType properties;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private String id;

   /**
    * 
    *  Specify the JNDI name for the admin object instance. 
    *           
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Sets the value of the jndiName property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setJndiName(String value)
   {
      this.jndiName = value;
   }

   /**
    * 
    * Defines all the properties that apply to the admin object instance.
    * The admin-object-properties element can contain one or more property elements, 
    * each holding a name and value pair. See properties..
    *           
    * 
    * @return
    *     possible object is
    *     {@link ConfigPropertiesType }
    *     
    */
   public ConfigPropertiesType getProperties()
   {
      return properties;
   }

   /**
    * Sets the value of the properties property.
    * 
    * @param value
    *     allowed object is
    *     {@link ConfigPropertiesType }
    *     
    */
   public void setProperties(ConfigPropertiesType value)
   {
      this.properties = value;
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
