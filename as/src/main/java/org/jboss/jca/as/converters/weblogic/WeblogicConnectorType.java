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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 * The weblogic-connector element is the root element of the WebLogic-specific deployment descriptor
 *  for the deployed resource adapter. 
 */
@XmlRootElement(name = "weblogic-connector")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "weblogic-connectorType", propOrder =
      { "nativeLibdir", "jndiName", "enableAccessOutsideApp", "enableGlobalAccessToClasses", "workManager",
      "connectorWorkManager", "security", "properties", "adminObjects", "outboundResourceAdapter" })
public class WeblogicConnectorType
{

   @XmlElement(name = "native-libdir")
   private String nativeLibdir;

   @XmlElement(name = "jndi-name")
   private String jndiName;

   @XmlElement(name = "enable-access-outside-app")
   private Boolean enableAccessOutsideApp;

   @XmlElement(name = "enable-global-access-to-classes")
   private Boolean enableGlobalAccessToClasses;

   @XmlElement(name = "work-manager")
   private WorkManagerType workManager;

   @XmlElement(name = "connector-work-manager")
   private ConnectorWorkManagerType connectorWorkManager;

   private ResourceAdapterSecurityType security;

   private ConfigPropertiesType properties;

   @XmlElement(name = "admin-objects")
   private AdminObjectsType adminObjects;

   @XmlElement(name = "outbound-resource-adapter")
   private OutboundResourceAdapterType outboundResourceAdapter;

   @XmlAttribute(name = "id")
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   @XmlID
   @XmlSchemaType(name = "ID")
   private java.lang.String id;

   @XmlAttribute(name = "version")
   private java.lang.String version;

   /**
    * Gets the value of the nativeLibdir property.
    * 
    * @return String
    *     
    */
   public String getNativeLibdir()
   {
      return nativeLibdir;
   }

   /**
    * Sets the value of the nativeLibdir property.
    * 
    * @param value String
    *     
    */
   public void setNativeLibdir(String value)
   {
      this.nativeLibdir = value;
   }

   /**
    * 
    * Required only if a resource adapter bean is specified.
    *  Specifies the JNDI name for the resource adapter. The resource adapter bean is registered 
    *  into the JNDI tree with this name. It is not a required element if no resource adapter 
    *  bean is specified. It is not a functional element if a JNDI name is specified for a resource 
    *  adapter without a resource adapter bean.
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
    * Gets the value of the enableAccessOutsideApp property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getEnableAccessOutsideApp()
   {
      return enableAccessOutsideApp;
   }

   /**
    * Sets the value of the enableAccessOutsideApp property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setEnableAccessOutsideApp(Boolean value)
   {
      this.enableAccessOutsideApp = value;
   }

   /**
    * Gets the value of the enableGlobalAccessToClasses property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getEnableGlobalAccessToClasses()
   {
      return enableGlobalAccessToClasses;
   }

   /**
    * Sets the value of the enableGlobalAccessToClasses property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setEnableGlobalAccessToClasses(Boolean value)
   {
      this.enableGlobalAccessToClasses = value;
   }

   /**
    * Gets the value of the workManager property.
    * 
    * @return
    *     possible object is
    *     {@link WorkManagerType }
    *     
    */
   public WorkManagerType getWorkManager()
   {
      return workManager;
   }

   /**
    * Sets the value of the workManager property.
    * 
    * @param value
    *     allowed object is
    *     {@link WorkManagerType }
    *     
    */
   public void setWorkManager(WorkManagerType value)
   {
      this.workManager = value;
   }

   /**
    * Gets the value of the connectorWorkManager property.
    * 
    * @return
    *     possible object is
    *     {@link ConnectorWorkManagerType }
    *     
    */
   public ConnectorWorkManagerType getConnectorWorkManager()
   {
      return connectorWorkManager;
   }

   /**
    * Sets the value of the connectorWorkManager property.
    * 
    * @param value
    *     allowed object is
    *     {@link ConnectorWorkManagerType }
    *     
    */
   public void setConnectorWorkManager(ConnectorWorkManagerType value)
   {
      this.connectorWorkManager = value;
   }

   /**
    * Gets the value of the security property.
    * 
    * @return
    *     possible object is
    *     {@link ResourceAdapterSecurityType }
    *     
    */
   public ResourceAdapterSecurityType getSecurity()
   {
      return security;
   }

   /**
    * Sets the value of the security property.
    * 
    * @param value
    *     allowed object is
    *     {@link ResourceAdapterSecurityType }
    *     
    */
   public void setSecurity(ResourceAdapterSecurityType value)
   {
      this.security = value;
   }

   /**
    * This complex element is used to override any properties that have been specified 
    * for the resource adapter bean in the ra.xml file.
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
    * Gets the value of the adminObjects property.
    * 
    * @return
    *     possible object is
    *     {@link AdminObjectsType }
    *     
    */
   public AdminObjectsType getAdminObjects()
   {
      return adminObjects;
   }

   /**
    * Sets the value of the adminObjects property.
    * 
    * @param value
    *     allowed object is
    *     {@link AdminObjectsType }
    *     
    */
   public void setAdminObjects(AdminObjectsType value)
   {
      this.adminObjects = value;
   }

   /**
    * Gets the value of the outboundResourceAdapter property.
    * 
    * @return
    *     possible object is
    *     {@link OutboundResourceAdapterType }
    *     
    */
   public OutboundResourceAdapterType getOutboundResourceAdapter()
   {
      return outboundResourceAdapter;
   }

   /**
    * Sets the value of the outboundResourceAdapter property.
    * 
    * @param value
    *     allowed object is
    *     {@link OutboundResourceAdapterType }
    *     
    */
   public void setOutboundResourceAdapter(OutboundResourceAdapterType value)
   {
      this.outboundResourceAdapter = value;
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

   /**
    * Gets the value of the version property.
    * 
    * @return
    *     possible object is
    *     {@link java.lang.String }
    *     
    */
   public java.lang.String getVersion()
   {
      return version;
   }

   /**
    * Sets the value of the version property.
    * 
    * @param value
    *     allowed object is
    *     {@link java.lang.String }
    *     
    */
   public void setVersion(java.lang.String value)
   {
      this.version = value;
   }

}
