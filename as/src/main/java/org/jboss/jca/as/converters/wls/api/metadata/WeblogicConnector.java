/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.as.converters.wls.api.metadata;

/**
 *
 * A generic WeblogicConnector.
 *
 * @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
 *
 */
public interface WeblogicConnector extends WlsMetadata
{

   /**
    * Get NativeLibdir
    * 
    * @return native-libdir string
    */
   public String getNativeLibdir();
   
   /**
    * Get JndiName
    * 
    * @return the jndi name
    */
   public String getJndiName();
   
   /**
    * isEnableAccessOutsideApp
    * 
    * @return bool EnableAccessOutsideApp
    */
   public Boolean isEnableAccessOutsideApp();
   
   /**
    * isEnableGlobalAccessToClasses
    * 
    * @return bool EnableGlobalAccessToClasses
    */
   public Boolean isEnableGlobalAccessToClasses();
   
   /**
    * getWorkManager
    * 
    * @return the WorkManager
    */
   public WorkManager getWorkManager();
   
   /**
    * getConnectorWorkManager
    * 
    * @return the ConnectorWorkManager
    */
   public ConnectorWorkManager getConnectorWorkManager();
   
   
   /**
    * getSecurity
    * 
    * @return the ResourceAdapterSecurity
    */
   public ResourceAdapterSecurity getSecurity();
   
   /**
    * getProperties
    *           
    * @return the ConfigProperties
    */
   public ConfigProperties getProperties();
   
   /**
    * getAdminObjects
    * 
    * @return the AdminObjects
    */
   public AdminObjects getAdminObjects();
   
   /**
    * getOutboundResourceAdapter
    * 
    * @return the OutboundResourceAdapter
    */
   public OutboundResourceAdapter getOutboundResourceAdapter();
}
