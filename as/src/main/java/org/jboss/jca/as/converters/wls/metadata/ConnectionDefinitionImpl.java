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

import org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinition;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionInstance;

import java.util.List;

/**
*
* A generic ConnectionDefinition.
*
* @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
*
*/
public class ConnectionDefinitionImpl implements ConnectionDefinition
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2654156439973657322L;

   private final String cdi;
   private final ConnectionDefinitionProperties cdProps;
   private final List<ConnectionInstance> cis;
   
   /**
    * constructor
    * 
    * @param cdi ConnectionFactoryInterface
    * @param cdProps ConnectionDefinitionProperties
    * @param cis List<ConnectionInstance>
    */
   public ConnectionDefinitionImpl(String cdi, ConnectionDefinitionProperties cdProps,
      List<ConnectionInstance> cis)
   {
      this.cdi = cdi;
      this.cdProps = cdProps;
      this.cis = cis;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinition#getConnectionFactoryInterface()
    */
   @Override
   public String getConnectionFactoryInterface()
   {
      return cdi;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinition#getDefaultConnectionProperties()
    */
   @Override
   public ConnectionDefinitionProperties getDefaultConnectionProperties()
   {
      return cdProps;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinition#getConnectionInstance()
    */
   @Override
   public List<ConnectionInstance> getConnectionInstance()
   {
      return cis;
   }

}
