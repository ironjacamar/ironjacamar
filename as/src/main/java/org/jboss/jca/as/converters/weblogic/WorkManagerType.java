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
 * Java class for work-managerType complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "work-managerType", propOrder =
      { "name", "responseTimeRequestClass", "fairShareRequestClass", "contextRequestClass", "requestClassName",
      "minThreadsConstraint", "minThreadsConstraintName", "maxThreadsConstraint", "maxThreadsConstraintName",
      "capacity", "capacityName", "workManagerShutdownTrigger", "ignoreStuckThreads" })
public class WorkManagerType
{

   @XmlElement(required = true)
   private DispatchPolicyType name;

   @XmlElement(name = "response-time-request-class")
   private ResponseTimeRequestClassType responseTimeRequestClass;

   @XmlElement(name = "fair-share-request-class")
   private FairShareRequestClassType fairShareRequestClass;

   @XmlElement(name = "context-request-class")
   private ContextRequestClassType contextRequestClass;

   @XmlElement(name = "request-class-name")
   private String requestClassName;

   @XmlElement(name = "min-threads-constraint")
   private MinThreadsConstraintType minThreadsConstraint;

   @XmlElement(name = "min-threads-constraint-name")
   private String minThreadsConstraintName;

   @XmlElement(name = "max-threads-constraint")
   private MaxThreadsConstraintType maxThreadsConstraint;

   @XmlElement(name = "max-threads-constraint-name")
   private String maxThreadsConstraintName;

   private CapacityType capacity;

   @XmlElement(name = "capacity-name")
   private String capacityName;

   @XmlElement(name = "work-manager-shutdown-trigger")
   private WorkManagerShutdownTriggerType workManagerShutdownTrigger;

   @XmlElement(name = "ignore-stuck-threads")
   private Boolean ignoreStuckThreads;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private String id;

   /**
    * Gets the value of the name property.
    * 
    * @return
    *     possible object is
    *     {@link DispatchPolicyType }
    *     
    */
   public DispatchPolicyType getName()
   {
      return name;
   }

   /**
    * Sets the value of the name property.
    * 
    * @param value
    *     allowed object is
    *     {@link DispatchPolicyType }
    *     
    */
   public void setName(DispatchPolicyType value)
   {
      this.name = value;
   }

   /**
    * Gets the value of the responseTimeRequestClass property.
    * 
    * @return
    *     possible object is
    *     {@link ResponseTimeRequestClassType }
    *     
    */
   public ResponseTimeRequestClassType getResponseTimeRequestClass()
   {
      return responseTimeRequestClass;
   }

   /**
    * Sets the value of the responseTimeRequestClass property.
    * 
    * @param value
    *     allowed object is
    *     {@link ResponseTimeRequestClassType }
    *     
    */
   public void setResponseTimeRequestClass(ResponseTimeRequestClassType value)
   {
      this.responseTimeRequestClass = value;
   }

   /**
    * Gets the value of the fairShareRequestClass property.
    * 
    * @return
    *     possible object is
    *     {@link FairShareRequestClassType }
    *     
    */
   public FairShareRequestClassType getFairShareRequestClass()
   {
      return fairShareRequestClass;
   }

   /**
    * Sets the value of the fairShareRequestClass property.
    * 
    * @param value
    *     allowed object is
    *     {@link FairShareRequestClassType }
    *     
    */
   public void setFairShareRequestClass(FairShareRequestClassType value)
   {
      this.fairShareRequestClass = value;
   }

   /**
    * Gets the value of the contextRequestClass property.
    * 
    * @return
    *     possible object is
    *     {@link ContextRequestClassType }
    *     
    */
   public ContextRequestClassType getContextRequestClass()
   {
      return contextRequestClass;
   }

   /**
    * Sets the value of the contextRequestClass property.
    * 
    * @param value
    *     allowed object is
    *     {@link ContextRequestClassType }
    *     
    */
   public void setContextRequestClass(ContextRequestClassType value)
   {
      this.contextRequestClass = value;
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
    * Gets the value of the minThreadsConstraint property.
    * 
    * @return
    *     possible object is
    *     {@link MinThreadsConstraintType }
    *     
    */
   public MinThreadsConstraintType getMinThreadsConstraint()
   {
      return minThreadsConstraint;
   }

   /**
    * Sets the value of the minThreadsConstraint property.
    * 
    * @param value
    *     allowed object is
    *     {@link MinThreadsConstraintType }
    *     
    */
   public void setMinThreadsConstraint(MinThreadsConstraintType value)
   {
      this.minThreadsConstraint = value;
   }

   /**
    * Gets the value of the minThreadsConstraintName property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getMinThreadsConstraintName()
   {
      return minThreadsConstraintName;
   }

   /**
    * Sets the value of the minThreadsConstraintName property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setMinThreadsConstraintName(String value)
   {
      this.minThreadsConstraintName = value;
   }

   /**
    * Gets the value of the maxThreadsConstraint property.
    * 
    * @return
    *     possible object is
    *     {@link MaxThreadsConstraintType }
    *     
    */
   public MaxThreadsConstraintType getMaxThreadsConstraint()
   {
      return maxThreadsConstraint;
   }

   /**
    * Sets the value of the maxThreadsConstraint property.
    * 
    * @param value
    *     allowed object is
    *     {@link MaxThreadsConstraintType }
    *     
    */
   public void setMaxThreadsConstraint(MaxThreadsConstraintType value)
   {
      this.maxThreadsConstraint = value;
   }

   /**
    * Gets the value of the maxThreadsConstraintName property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getMaxThreadsConstraintName()
   {
      return maxThreadsConstraintName;
   }

   /**
    * Sets the value of the maxThreadsConstraintName property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setMaxThreadsConstraintName(String value)
   {
      this.maxThreadsConstraintName = value;
   }

   /**
    * Gets the value of the capacity property.
    * 
    * @return
    *     possible object is
    *     {@link CapacityType }
    *     
    */
   public CapacityType getCapacity()
   {
      return capacity;
   }

   /**
    * Sets the value of the capacity property.
    * 
    * @param value
    *     allowed object is
    *     {@link CapacityType }
    *     
    */
   public void setCapacity(CapacityType value)
   {
      this.capacity = value;
   }

   /**
    * Gets the value of the capacityName property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getCapacityName()
   {
      return capacityName;
   }

   /**
    * Sets the value of the capacityName property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setCapacityName(String value)
   {
      this.capacityName = value;
   }

   /**
    * Gets the value of the workManagerShutdownTrigger property.
    * 
    * @return
    *     possible object is
    *     {@link WorkManagerShutdownTriggerType }
    *     
    */
   public WorkManagerShutdownTriggerType getWorkManagerShutdownTrigger()
   {
      return workManagerShutdownTrigger;
   }

   /**
    * Sets the value of the workManagerShutdownTrigger property.
    * 
    * @param value
    *     allowed object is
    *     {@link WorkManagerShutdownTriggerType }
    *     
    */
   public void setWorkManagerShutdownTrigger(WorkManagerShutdownTriggerType value)
   {
      this.workManagerShutdownTrigger = value;
   }

   /**
    * Gets the value of the ignoreStuckThreads property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getIgnoreStuckThreads()
   {
      return ignoreStuckThreads;
   }

   /**
    * Sets the value of the ignoreStuckThreads property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setIgnoreStuckThreads(Boolean value)
   {
      this.ignoreStuckThreads = value;
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
