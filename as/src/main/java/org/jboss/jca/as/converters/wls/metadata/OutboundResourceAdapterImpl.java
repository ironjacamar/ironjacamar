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
import org.jboss.jca.as.converters.wls.api.metadata.OutboundResourceAdapter;

import java.util.List;

/**
*
* A generic OutboundResourceAdapter.
*
* @author <a href="jeff.zhang@ironjacamar.org">Jeff Zhang</a>
*
*/
public class OutboundResourceAdapterImpl implements OutboundResourceAdapter
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2654156739946657322L;

   private final ConnectionDefinitionProperties cdProps;
   private final List<ConnectionDefinition> cds;
   
   /**
    * constructor
    * 
    * @param cdProps ConnectionDefinitionProperties
    * @param cds List<ConnectionDefinition>
    */
   public OutboundResourceAdapterImpl(ConnectionDefinitionProperties cdProps, List<ConnectionDefinition> cds)
   {
      this.cdProps = cdProps;
      this.cds = cds;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.OutboundResourceAdapter#getDefaultConnectionProperties()
    */
   @Override
   public ConnectionDefinitionProperties getDefaultConnectionProperties()
   {
      return cdProps;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.OutboundResourceAdapter#getConnectionDefinitionGroup()
    */
   @Override
   public List<ConnectionDefinition> getConnectionDefinitionGroup()
   {
      return cds;
   }

}
