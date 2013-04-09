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
package org.jboss.jca.as.converters.wls.metadata.v13;

import org.jboss.jca.as.converters.wls.api.metadata.AdminObjects;
import org.jboss.jca.as.converters.wls.api.metadata.ConfigProperties;
import org.jboss.jca.as.converters.wls.api.metadata.OutboundResourceAdapter;
import org.jboss.jca.as.converters.wls.api.metadata.ResourceAdapterSecurity;
import org.jboss.jca.as.converters.wls.api.metadata.v13.WeblogicConnector13;
import org.jboss.jca.as.converters.wls.metadata.WeblogicConnectorAbstractImpl;

/**
 *
 * A WeblogicConnector v1.3.
 *
 * @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
 *
 */
public class WeblogicConnector13Impl extends WeblogicConnectorAbstractImpl implements WeblogicConnector13
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 631372312218060928L;

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
   public WeblogicConnector13Impl(String nativeLibdir, String jndiName, ResourceAdapterSecurity ras, 
      ConfigProperties props, AdminObjects aos, OutboundResourceAdapter ora)
   {
      super(nativeLibdir, jndiName, ras, props, aos, ora);
   }
}
