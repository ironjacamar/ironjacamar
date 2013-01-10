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
 * Java class for connection-definition-propertiesType complex type.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connection-definition-propertiesType", propOrder =
      { "poolParams", "logging", "transactionSupport", "reauthenticationSupport", "properties", "resAuth" })
public class ConnectionDefinitionPropertiesType
{

   @XmlElement(name = "pool-params")
   private PoolParamsType poolParams;

   private LoggingType logging;
   
   @XmlElement(name = "transaction-support")
   private TransactionSupportType transactionSupport;

   @XmlElement(name = "reauthentication-support")
   private Boolean reauthenticationSupport;

   private ConfigPropertiesType properties;

   @XmlElement(name = "res-auth")
   private String resAuth;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private String id;

   /**
    * Gets the value of the poolParams property.
    * 
    * @return
    *     possible object is
    *     {@link PoolParamsType }
    *     
    */
   public PoolParamsType getPoolParams()
   {
      return poolParams;
   }

   /**
    * Sets the value of the poolParams property.
    * 
    * @param value
    *     allowed object is
    *     {@link PoolParamsType }
    *     
    */
   public void setPoolParams(PoolParamsType value)
   {
      this.poolParams = value;
   }

   /**
    * Gets the value of the logging property.
    * 
    * @return
    *     possible object is
    *     {@link LoggingType }
    *     
    */
   public LoggingType getLogging()
   {
      return logging;
   }

   /**
    * Sets the value of the logging property.
    * 
    * @param value
    *     allowed object is
    *     {@link LoggingType }
    *     
    */
   public void setLogging(LoggingType value)
   {
      this.logging = value;
   }

   /**
    * @return the transactionSupport
    */
   public TransactionSupportType getTransactionSupport()
   {
      return transactionSupport;
   }

   /**
    * @param transactionSupport the transactionSupport to set
    */
   public void setTransactionSupport(TransactionSupportType transactionSupport)
   {
      this.transactionSupport = transactionSupport;
   }
   
   /**
    * Gets the value of the reauthenticationSupport property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getReauthenticationSupport()
   {
      return reauthenticationSupport;
   }

   /**
    * Sets the value of the reauthenticationSupport property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setReauthenticationSupport(Boolean value)
   {
      this.reauthenticationSupport = value;
   }

   /**
    * 
    *  The properties element includes one or more property elements, which define name 
    *  and value subelements that apply to the default connections.
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
    * 
    *  Specifies whether to use container- or application-managed security. The values for 
    *  this element can be one of Application or Container. The default value is Container.
    *           
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getResAuth()
   {
      return resAuth;
   }

   /**
    * Sets the value of the resAuth property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setResAuth(String value)
   {
      this.resAuth = value;
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
