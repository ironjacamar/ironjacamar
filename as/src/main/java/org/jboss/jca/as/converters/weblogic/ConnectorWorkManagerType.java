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
 * This complex element is used to specify all the configurable elements for Connector Work Manager 
 * for this adapter module itself.
 * This element provides configurations that are not supported by standard WebLogic Work Manager.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connectorWorkManagerType", propOrder =
      { "maxConcurrentLongRunningRequests" })
public class ConnectorWorkManagerType
{

   @XmlElement(name = "max-concurrent-long-running-requests")
   private Integer maxConcurrentLongRunningRequests;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private String id;

   /**
    * Gets the value of the maxConcurrentLongRunningRequests property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getMaxConcurrentLongRunningRequests()
   {
      return maxConcurrentLongRunningRequests;
   }

   /**
    * Sets the value of the maxConcurrentLongRunningRequests property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setMaxConcurrentLongRunningRequests(Integer value)
   {
      this.maxConcurrentLongRunningRequests = value;
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
