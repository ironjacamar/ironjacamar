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
 * Java class for work-manager-shutdown-triggerType complex type.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "work-manager-shutdown-triggerType", propOrder =
      { "maxStuckThreadTime", "stuckThreadCount" })
public class WorkManagerShutdownTriggerType
{

   @XmlElement(name = "max-stuck-thread-time")
   private Integer maxStuckThreadTime;

   @XmlElement(name = "stuck-thread-count", required = true)
   private Integer stuckThreadCount;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private String id;

   /**
    * Gets the value of the maxStuckThreadTime property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getMaxStuckThreadTime()
   {
      return maxStuckThreadTime;
   }

   /**
    * Sets the value of the maxStuckThreadTime property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setMaxStuckThreadTime(Integer value)
   {
      this.maxStuckThreadTime = value;
   }

   /**
    * Gets the value of the stuckThreadCount property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getStuckThreadCount()
   {
      return stuckThreadCount;
   }

   /**
    * Sets the value of the stuckThreadCount property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setStuckThreadCount(Integer value)
   {
      this.stuckThreadCount = value;
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
