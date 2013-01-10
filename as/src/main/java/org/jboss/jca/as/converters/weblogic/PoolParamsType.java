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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for pool-paramsType complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pool-paramsType", propOrder =
      { "matchConnectionsSupported", "useFirstAvailable" })
public class PoolParamsType extends ConnectionPoolParamsType
{

   @XmlElement(name = "match-connections-supported")
   private Boolean matchConnectionsSupported;

   @XmlElement(name = "use-first-available")
   private Boolean useFirstAvailable;

   /**
    * Gets the value of the matchConnectionsSupported property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getMatchConnectionsSupported()
   {
      return matchConnectionsSupported;
   }

   /**
    * Sets the value of the matchConnectionsSupported property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setMatchConnectionsSupported(Boolean value)
   {
      this.matchConnectionsSupported = value;
   }

   /**
    * Gets the value of the useFirstAvailable property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean getUseFirstAvailable()
   {
      return useFirstAvailable;
   }

   /**
    * Sets the value of the useFirstAvailable property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setUseFirstAvailable(Boolean value)
   {
      this.useFirstAvailable = value;
   }

}
