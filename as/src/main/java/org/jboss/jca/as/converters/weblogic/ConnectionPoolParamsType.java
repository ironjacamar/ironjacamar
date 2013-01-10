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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Java class for connection-pool-paramsType complex type.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connection-pool-paramsType", propOrder =
      { "initialCapacity", "maxCapacity", "capacityIncrement", "shrinkingEnabled", "shrinkFrequencySeconds",
      "highestNumWaiters", "highestNumUnavailable", "connectionCreationRetryFrequencySeconds",
      "connectionReserveTimeoutSeconds", "testFrequencySeconds", "testConnectionsOnCreate", "testConnectionsOnRelease",
      "testConnectionsOnReserve", "profileHarvestFrequencySeconds", "ignoreInUseConnectionsEnabled" })
@XmlSeeAlso({ PoolParamsType.class })
public class ConnectionPoolParamsType
{

   @XmlElement(name = "initial-capacity")
   private Integer initialCapacity;

   @XmlElement(name = "max-capacity")
   private Integer maxCapacity;

   @XmlElement(name = "capacity-increment")
   private Boolean capacityIncrement;

   @XmlElement(name = "shrinking-enabled")
   private Boolean shrinkingEnabled;

   @XmlElement(name = "shrink-frequency-seconds")
   private Boolean shrinkFrequencySeconds;

   @XmlElement(name = "highest-num-waiters")
   private Integer highestNumWaiters;

   @XmlElement(name = "highest-num-unavailable")
   private Integer highestNumUnavailable;

   @XmlElement(name = "connection-creation-retry-frequency-seconds")
   private Integer connectionCreationRetryFrequencySeconds;

   @XmlElement(name = "connection-reserve-timeout-seconds")
   private Integer connectionReserveTimeoutSeconds;

   @XmlElement(name = "test-frequency-seconds")
   private Integer testFrequencySeconds;

   @XmlElement(name = "test-connections-on-create")
   private Boolean testConnectionsOnCreate;

   @XmlElement(name = "test-connections-on-release")
   private Boolean testConnectionsOnRelease;

   @XmlElement(name = "test-connections-on-reserve")
   private Boolean testConnectionsOnReserve;

   @XmlElement(name = "profile-harvest-frequency-seconds")
   private Integer profileHarvestFrequencySeconds;

   @XmlElement(name = "ignore-in-use-connections-enabled")
   private Boolean ignoreInUseConnectionsEnabled;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private String id;

   /**
    * Gets the value of the initialCapacity property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getInitialCapacity()
   {
      return initialCapacity;
   }

   /**
    * Sets the value of the initialCapacity property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setInitialCapacity(Integer value)
   {
      this.initialCapacity = value;
   }

   /**
    * Gets the value of the maxCapacity property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getMaxCapacity()
   {
      return maxCapacity;
   }

   /**
    * Sets the value of the maxCapacity property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setMaxCapacity(Integer value)
   {
      this.maxCapacity = value;
   }

   /**
    * Gets the value of the capacityIncrement property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getCapacityIncrement()
   {
      return capacityIncrement;
   }

   /**
    * Sets the value of the capacityIncrement property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setCapacityIncrement(Boolean value)
   {
      this.capacityIncrement = value;
   }

   /**
    * Gets the value of the shrinkingEnabled property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getShrinkingEnabled()
   {
      return shrinkingEnabled;
   }

   /**
    * Sets the value of the shrinkingEnabled property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setShrinkingEnabled(Boolean value)
   {
      this.shrinkingEnabled = value;
   }

   /**
    * Gets the value of the shrinkFrequencySeconds property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getShrinkFrequencySeconds()
   {
      return shrinkFrequencySeconds;
   }

   /**
    * Sets the value of the shrinkFrequencySeconds property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setShrinkFrequencySeconds(Boolean value)
   {
      this.shrinkFrequencySeconds = value;
   }

   /**
    * Gets the value of the highestNumWaiters property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getHighestNumWaiters()
   {
      return highestNumWaiters;
   }

   /**
    * Sets the value of the highestNumWaiters property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setHighestNumWaiters(Integer value)
   {
      this.highestNumWaiters = value;
   }

   /**
    * Gets the value of the highestNumUnavailable property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getHighestNumUnavailable()
   {
      return highestNumUnavailable;
   }

   /**
    * Sets the value of the highestNumUnavailable property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setHighestNumUnavailable(Integer value)
   {
      this.highestNumUnavailable = value;
   }

   /**
    * Gets the value of the connectionCreationRetryFrequencySeconds property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getConnectionCreationRetryFrequencySeconds()
   {
      return connectionCreationRetryFrequencySeconds;
   }

   /**
    * Sets the value of the connectionCreationRetryFrequencySeconds property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setConnectionCreationRetryFrequencySeconds(Integer value)
   {
      this.connectionCreationRetryFrequencySeconds = value;
   }

   /**
    * Gets the value of the connectionReserveTimeoutSeconds property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getConnectionReserveTimeoutSeconds()
   {
      return connectionReserveTimeoutSeconds;
   }

   /**
    * Sets the value of the connectionReserveTimeoutSeconds property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setConnectionReserveTimeoutSeconds(Integer value)
   {
      this.connectionReserveTimeoutSeconds = value;
   }

   /**
    * Gets the value of the testFrequencySeconds property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getTestFrequencySeconds()
   {
      return testFrequencySeconds;
   }

   /**
    * Sets the value of the testFrequencySeconds property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setTestFrequencySeconds(Integer value)
   {
      this.testFrequencySeconds = value;
   }

   /**
    * Gets the value of the testConnectionsOnCreate property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getTestConnectionsOnCreate()
   {
      return testConnectionsOnCreate;
   }

   /**
    * Sets the value of the testConnectionsOnCreate property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setTestConnectionsOnCreate(Boolean value)
   {
      this.testConnectionsOnCreate = value;
   }

   /**
    * Gets the value of the testConnectionsOnRelease property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getTestConnectionsOnRelease()
   {
      return testConnectionsOnRelease;
   }

   /**
    * Sets the value of the testConnectionsOnRelease property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setTestConnectionsOnRelease(Boolean value)
   {
      this.testConnectionsOnRelease = value;
   }

   /**
    * Gets the value of the testConnectionsOnReserve property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getTestConnectionsOnReserve()
   {
      return testConnectionsOnReserve;
   }

   /**
    * Sets the value of the testConnectionsOnReserve property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setTestConnectionsOnReserve(Boolean value)
   {
      this.testConnectionsOnReserve = value;
   }

   /**
    * Gets the value of the profileHarvestFrequencySeconds property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public Integer getProfileHarvestFrequencySeconds()
   {
      return profileHarvestFrequencySeconds;
   }

   /**
    * Sets the value of the profileHarvestFrequencySeconds property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setProfileHarvestFrequencySeconds(Integer value)
   {
      this.profileHarvestFrequencySeconds = value;
   }

   /**
    * Gets the value of the ignoreInUseConnectionsEnabled property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getIgnoreInUseConnectionsEnabled()
   {
      return ignoreInUseConnectionsEnabled;
   }

   /**
    * Sets the value of the ignoreInUseConnectionsEnabled property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setIgnoreInUseConnectionsEnabled(Boolean value)
   {
      this.ignoreInUseConnectionsEnabled = value;
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
