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
package org.jboss.jca.as.converters.wls.metadata;

import org.jboss.jca.as.converters.wls.api.metadata.AdminObjects;
import org.jboss.jca.as.converters.wls.api.metadata.ConfigProperties;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectorWorkManager;
import org.jboss.jca.as.converters.wls.api.metadata.OutboundResourceAdapter;
import org.jboss.jca.as.converters.wls.api.metadata.ResourceAdapterSecurity;
import org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector;
import org.jboss.jca.as.converters.wls.api.metadata.WorkManager;

/**
*
* An abstract WeblogicConnector Impl.
*
* @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
*
*/
public abstract class WeblogicConnectorAbstractImpl implements WeblogicConnector
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2054156739973627322L;
   
   /**
    * nativeLibdir
    */
   protected final String nativeLibdir;
   
   /**
    * jndiName
    */
   protected final String jndiName;
   
   /** ResourceAdapterSecurity */
   protected final ResourceAdapterSecurity ras; 
   
   /** ConfigProperties */
   protected final ConfigProperties props;
   
   /** AdminObjects */
   protected final AdminObjects aos;
   
   /** OutboundResourceAdapter */
   protected final OutboundResourceAdapter ora;
   
   /**
    * constructor
    * 
    * @param nativeLibdir nativeLibdir
    * @param jndiName jndiName
    * @param ras ResourceAdapterSecurity
    * @param props ConfigProperties
    * @param aos AdminObjects
    * @param ora OutboundResourceAdapter
    */
   public WeblogicConnectorAbstractImpl(String nativeLibdir, String jndiName, ResourceAdapterSecurity ras, 
      ConfigProperties props, AdminObjects aos, OutboundResourceAdapter ora)
   {
      this.nativeLibdir = nativeLibdir;
      this.jndiName = jndiName;
      this.ras = ras;
      this.props = props;
      this.aos = aos;
      this.ora = ora;
   }
   
   /**
    * Get NativeLibdir
    * 
    * @return native-libdir string
    */
   @Override
   public String getNativeLibdir()
   {
      return nativeLibdir;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector#getJndiName()
    */
   @Override
   public String getJndiName()
   {
      return jndiName;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector#isEnableAccessOutsideApp()
    */
   @Override
   public Boolean isEnableAccessOutsideApp()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector#isEnableGlobalAccessToClasses()
    */
   @Override
   public Boolean isEnableGlobalAccessToClasses()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector#getWorkManager()
    */
   @Override
   public WorkManager getWorkManager()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector#getConnectorWorkManager()
    */
   @Override
   public ConnectorWorkManager getConnectorWorkManager()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector#getSecurity()
    */
   @Override
   public ResourceAdapterSecurity getSecurity()
   {
      return ras;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector#getProperties()
    */
   @Override
   public ConfigProperties getProperties()
   {
      return props;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector#getAdminObjects()
    */
   @Override
   public AdminObjects getAdminObjects()
   {
      return aos;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector#getOutboundResourceAdapter()
    */
   @Override
   public OutboundResourceAdapter getOutboundResourceAdapter()
   {
      return ora;
   }

}
