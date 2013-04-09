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

import org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionInstance;

/**
*
* A generic ConnectionDefinition.
*
* @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
*
*/
public class ConnectionInstanceImpl implements ConnectionInstance
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2654156739923657322L;

   private final String description;
   private final String jndiName;
   private final ConnectionDefinitionProperties cdProps;
   
   /**
    * constructor
    * 
    * @param description description
    * @param jndiName jndiName
    * @param cdProps ConnectionDefinitionProperties
    */
   public ConnectionInstanceImpl(String description, String jndiName,
      ConnectionDefinitionProperties cdProps)
   {
      this.description = description;
      this.jndiName = jndiName;
      this.cdProps = cdProps;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionInstance#getDescription()
    */
   @Override
   public String getDescription()
   {
      return description;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionInstance#getJndiName()
    */
   @Override
   public String getJndiName()
   {
      return jndiName;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionInstance#getConnectionProperties()
    */
   @Override
   public ConnectionDefinitionProperties getConnectionProperties()
   {
      return cdProps;
   }

}
