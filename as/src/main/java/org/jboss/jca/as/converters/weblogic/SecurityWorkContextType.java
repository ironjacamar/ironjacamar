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
 * Java class for security-work-contextType complex type.

 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "security-work-contextType", propOrder =
      { "inboundMappingRequired", "callerPrincipalDefaultMapped", "callerPrincipalMapping", 
      "groupPrincipalDefaultMapped", "groupPrincipalMapping" })
public class SecurityWorkContextType
{

   @XmlElement(name = "inbound-mapping-required", defaultValue = "false")
   private Boolean inboundMappingRequired;

   @XmlElement(name = "caller-principal-default-mapped")
   private AnonPrincipalType callerPrincipalDefaultMapped;

   @XmlElement(name = "caller-principal-mapping")
   private List<InboundCallerPrincipalMappingType> callerPrincipalMapping;

   @XmlElement(name = "group-principal-default-mapped")
   private String groupPrincipalDefaultMapped;

   @XmlElement(name = "group-principal-mapping")
   private List<InboundGroupPrincipalMappingType> groupPrincipalMapping;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private java.lang.String id;

   /**
    * Gets the value of the inboundMappingRequired property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getInboundMappingRequired()
   {
      return inboundMappingRequired;
   }

   /**
    * Sets the value of the inboundMappingRequired property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setInboundMappingRequired(Boolean value)
   {
      this.inboundMappingRequired = value;
   }

   /**
    * Gets the value of the callerPrincipalDefaultMapped property.
    * 
    * @return
    *     possible object is
    *     {@link AnonPrincipalType }
    *     
    */
   public AnonPrincipalType getCallerPrincipalDefaultMapped()
   {
      return callerPrincipalDefaultMapped;
   }

   /**
    * Sets the value of the callerPrincipalDefaultMapped property.
    * 
    * @param value
    *     allowed object is
    *     {@link AnonPrincipalType }
    *     
    */
   public void setCallerPrincipalDefaultMapped(AnonPrincipalType value)
   {
      this.callerPrincipalDefaultMapped = value;
   }

   /**
    * Gets the value of the callerPrincipalMapping property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the callerPrincipalMapping property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getCallerPrincipalMapping().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link InboundCallerPrincipalMappingType }
    * 
    * @return callerPrincipalMapping
    */
   public List<InboundCallerPrincipalMappingType> getCallerPrincipalMapping()
   {
      if (callerPrincipalMapping == null)
      {
         callerPrincipalMapping = new ArrayList<InboundCallerPrincipalMappingType>();
      }
      return this.callerPrincipalMapping;
   }

   /**
    * Gets the value of the groupPrincipalDefaultMapped property.
    * 
    * @return
    *     possible object is
    *     {@link com.sun.java.xml.ns.javaee.String }
    *     
    */
   public String getGroupPrincipalDefaultMapped()
   {
      return groupPrincipalDefaultMapped;
   }

   /**
    * Sets the value of the groupPrincipalDefaultMapped property.
    * 
    * @param value
    *     allowed object is
    *     {@link com.sun.java.xml.ns.javaee.String }
    *     
    */
   public void setGroupPrincipalDefaultMapped(String value)
   {
      this.groupPrincipalDefaultMapped = value;
   }

   /**
    * Gets the value of the groupPrincipalMapping property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the groupPrincipalMapping property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getGroupPrincipalMapping().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link InboundGroupPrincipalMappingType }
    * 
    * @return groupPrincipalMapping
    */
   public List<InboundGroupPrincipalMappingType> getGroupPrincipalMapping()
   {
      if (groupPrincipalMapping == null)
      {
         groupPrincipalMapping = new ArrayList<InboundGroupPrincipalMappingType>();
      }
      return this.groupPrincipalMapping;
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
