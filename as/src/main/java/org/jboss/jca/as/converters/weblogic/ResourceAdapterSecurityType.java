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
 * Java class for resource-adapter-securityType complex type.
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource-adapter-securityType", propOrder = { })
public class ResourceAdapterSecurityType
{

   @XmlElement(name = "default-principal-name")
   private AnonPrincipalType defaultPrincipalName;

   @XmlElement(name = "manage-as-principal-name")
   private AnonPrincipalType manageAsPrincipalName;

   @XmlElement(name = "run-as-principal-name")
   private AnonPrincipalCallerType runAsPrincipalName;

   @XmlElement(name = "run-work-as-principal-name")
   private AnonPrincipalCallerType runWorkAsPrincipalName;

   @XmlElement(name = "security-work-context")
   private SecurityWorkContextType securityWorkContext;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private String id;

   /**
    * Gets the value of the defaultPrincipalName property.
    * 
    * @return
    *     possible object is
    *     {@link AnonPrincipalType }
    *     
    */
   public AnonPrincipalType getDefaultPrincipalName()
   {
      return defaultPrincipalName;
   }

   /**
    * Sets the value of the defaultPrincipalName property.
    * 
    * @param value
    *     allowed object is
    *     {@link AnonPrincipalType }
    *     
    */
   public void setDefaultPrincipalName(AnonPrincipalType value)
   {
      this.defaultPrincipalName = value;
   }

   /**
    * Gets the value of the manageAsPrincipalName property.
    * 
    * @return
    *     possible object is
    *     {@link AnonPrincipalType }
    *     
    */
   public AnonPrincipalType getManageAsPrincipalName()
   {
      return manageAsPrincipalName;
   }

   /**
    * Sets the value of the manageAsPrincipalName property.
    * 
    * @param value
    *     allowed object is
    *     {@link AnonPrincipalType }
    *     
    */
   public void setManageAsPrincipalName(AnonPrincipalType value)
   {
      this.manageAsPrincipalName = value;
   }

   /**
    * Gets the value of the runAsPrincipalName property.
    * 
    * @return
    *     possible object is
    *     {@link AnonPrincipalCallerType }
    *     
    */
   public AnonPrincipalCallerType getRunAsPrincipalName()
   {
      return runAsPrincipalName;
   }

   /**
    * Sets the value of the runAsPrincipalName property.
    * 
    * @param value
    *     allowed object is
    *     {@link AnonPrincipalCallerType }
    *     
    */
   public void setRunAsPrincipalName(AnonPrincipalCallerType value)
   {
      this.runAsPrincipalName = value;
   }

   /**
    * Gets the value of the runWorkAsPrincipalName property.
    * 
    * @return
    *     possible object is
    *     {@link AnonPrincipalCallerType }
    *     
    */
   public AnonPrincipalCallerType getRunWorkAsPrincipalName()
   {
      return runWorkAsPrincipalName;
   }

   /**
    * Sets the value of the runWorkAsPrincipalName property.
    * 
    * @param value
    *     allowed object is
    *     {@link AnonPrincipalCallerType }
    *     
    */
   public void setRunWorkAsPrincipalName(AnonPrincipalCallerType value)
   {
      this.runWorkAsPrincipalName = value;
   }

   /**
    * Gets the value of the securityWorkContext property.
    * 
    * @return
    *     possible object is
    *     {@link SecurityWorkContextType }
    *     
    */
   public SecurityWorkContextType getSecurityWorkContext()
   {
      return securityWorkContext;
   }

   /**
    * Sets the value of the securityWorkContext property.
    * 
    * @param value
    *     allowed object is
    *     {@link SecurityWorkContextType }
    *     
    */
   public void setSecurityWorkContext(SecurityWorkContextType value)
   {
      this.securityWorkContext = value;
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
