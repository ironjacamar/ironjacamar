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
package org.jboss.jca.as.converters.wls.metadata;

import org.jboss.jca.as.converters.wls.api.metadata.AdminObjectGroup;
import org.jboss.jca.as.converters.wls.api.metadata.AdminObjectInstance;
import org.jboss.jca.as.converters.wls.api.metadata.ConfigProperties;

import java.util.List;

/**
*
* A generic AdminObjectGroup.
*
* @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
*
*/
public class AdminObjectGroupImpl implements AdminObjectGroup
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2654156739973647323L;

   private final String aoInterface;
   private final String aoClass;
   private final ConfigProperties props;
   private final List<AdminObjectInstance> aois;
   
   /**
    * AdminObjectGroupImpl constructor 
    * 
    * @param aoInterface admin object interface
    * @param aoClass admin object class
    * @param props ConfigProperties
    * @param aois List<AdminObjectInstance>
    * 
    */
   public AdminObjectGroupImpl(String aoInterface, String aoClass, ConfigProperties props, 
      List<AdminObjectInstance> aois)
   {
      this.aoInterface = aoInterface;
      this.aoClass = aoClass;
      this.props = props;
      this.aois = aois;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.AdminObjectGroup#getAdminObjectInterface()
    */
   @Override
   public String getAdminObjectInterface()
   {
      return aoInterface;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.AdminObjectGroup#getAdminObjectClass()
    */
   @Override
   public String getAdminObjectClass()
   {
      return aoClass;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.AdminObjectGroup#getDefaultProperties()
    */
   @Override
   public ConfigProperties getDefaultProperties()
   {
      return props;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.AdminObjectGroup#getAdminObjectInstance()
    */
   @Override
   public List<AdminObjectInstance> getAdminObjectInstance()
   {
      return aois;
   }

}
