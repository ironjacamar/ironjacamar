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
 * Java class for context-caseType complex type.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "context-caseType", propOrder =
      { "userName", "groupName", "requestClassName" })
public class ContextCaseType
{

   @XmlElement(name = "user-name")
   private String userName;

   @XmlElement(name = "group-name")
   private GroupNameType groupName;

   @XmlElement(name = "request-class-name", required = true)
   private String requestClassName;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private String id;

   /**
    * Gets the value of the userName property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getUserName()
   {
      return userName;
   }

   /**
    * Sets the value of the userName property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setUserName(String value)
   {
      this.userName = value;
   }

   /**
    * Gets the value of the groupName property.
    * 
    * @return
    *     possible object is
    *     {@link GroupNameType }
    *     
    */
   public GroupNameType getGroupName()
   {
      return groupName;
   }

   /**
    * Sets the value of the groupName property.
    * 
    * @param value
    *     allowed object is
    *     {@link GroupNameType }
    *     
    */
   public void setGroupName(GroupNameType value)
   {
      this.groupName = value;
   }

   /**
    * Gets the value of the requestClassName property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getRequestClassName()
   {
      return requestClassName;
   }

   /**
    * Sets the value of the requestClassName property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setRequestClassName(String value)
   {
      this.requestClassName = value;
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
