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
 * Java class for anon-principalType complex type.
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "anon-principalType", propOrder =
      { "useAnonymousIdentity", "principalName" })
public class AnonPrincipalType
{

   @XmlElement(name = "use-anonymous-identity")
   private Boolean useAnonymousIdentity;

   @XmlElement(name = "principal-name")
   private String principalName;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private java.lang.String id;

   /**
    * 
    * Specifies that the anonymous identity should be used.
    *           
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getUseAnonymousIdentity()
   {
      return useAnonymousIdentity;
   }

   /**
    * Sets the value of the useAnonymousIdentity property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setUseAnonymousIdentity(Boolean value)
   {
      this.useAnonymousIdentity = value;
   }

   /**
    * Specifies that the principal name should be used. This should match a defined WebLogic Server user name.
    *           
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getPrincipalName()
   {
      return principalName;
   }

   /**
    * Sets the value of the principalName property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setPrincipalName(String value)
   {
      this.principalName = value;
   }

   /**
    * Gets the value of the id property.
    * 
    * @return
    *     possible object is
    *     {@link java.lang.String }
    *     
    */
   public java.lang.String getId()
   {
      return id;
   }

   /**
    * Sets the value of the id property.
    * 
    * @param value
    *     allowed object is
    *     {@link java.lang.String }
    *     
    */
   public void setId(java.lang.String value)
   {
      this.id = value;
   }

}
