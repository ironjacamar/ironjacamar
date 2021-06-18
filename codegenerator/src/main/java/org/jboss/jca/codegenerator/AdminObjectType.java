/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.codegenerator;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * An AdminObjectType.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AdminObjectType
{
   /** define admin object interface */
   @XmlElement(name = "AdminObjectInterface") 
   private String adminObjectInterface;

   /** define admin object class */
   @XmlElement(name = "AdminObjectClass") 
   private String adminObjectClass;

   /** admin object configuration properties */
   @XmlElement(name = "AoConfigProp") 
   private List<ConfigPropType> aoConfigProps;
   
   /**
    * Set the adminObjectInterface.
    * 
    * @param adminObjectInterface The adminObjectInterface to set.
    */
   public void setAdminObjectInterface(String adminObjectInterface)
   {
      this.adminObjectInterface = adminObjectInterface;
   }

   /**
    * Get the adminObjectInterface.
    * 
    * @return the adminObjectInterface.
    */
   public String getAdminObjectInterface()
   {
      return adminObjectInterface;
   }

   /**
    * Set the adminObjectClass.
    * 
    * @param adminObjectClass The adminObjectClass to set.
    */
   public void setAdminObjectClass(String adminObjectClass)
   {
      this.adminObjectClass = adminObjectClass;
   }

   /**
    * Get the adminObjectClass.
    * 
    * @return the adminObjectClass.
    */
   public String getAdminObjectClass()
   {
      return adminObjectClass;
   }
   
   /**
    * Set the aoConfigProps.
    * 
    * @param aoConfigProps The aoConfigProps to set.
    */
   public void setAoConfigProps(List<ConfigPropType> aoConfigProps)
   {
      this.aoConfigProps = aoConfigProps;
   }

   /**
    * Get the aoConfigProps.
    * 
    * @return the aoConfigProps.
    */
   public List<ConfigPropType> getAoConfigProps()
   {
      return aoConfigProps;
   }

}
