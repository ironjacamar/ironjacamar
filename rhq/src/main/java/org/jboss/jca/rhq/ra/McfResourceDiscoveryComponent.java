/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.rhq.ra;

import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagedConnectionFactory;
import org.jboss.jca.core.api.management.ManagementRepository;

import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.util.ManagementRepositoryHelper;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

/**
 * Discovery McfResourceDiscoveryComponent resources from JCA container.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 */
public class McfResourceDiscoveryComponent
   implements ResourceDiscoveryComponent<McfResourceComponent>
{
   /**
    * discoverResources
    * 
    * @param context ResourceDiscoveryContext<AdminObjectResourceComponent>
    * @return Set<DiscoveredResourceDetails> set of DiscoveredResourceDetails
    * @throws InvalidPluginConfigurationException invalidPluginConfigurationException
    * @throws Exception exception
    */
   @Override
   public Set<DiscoveredResourceDetails> discoverResources(
      ResourceDiscoveryContext<McfResourceComponent> context)
      throws InvalidPluginConfigurationException, Exception
   {
      Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();

      // the uniqueId is the key of parent component.
      String rarUniqueId = context.getParentResourceContext().getResourceKey();

      ManagementRepository mr = ManagementRepositoryManager.getManagementRepository();
      Connector connector = ManagementRepositoryHelper.getConnectorByUniqueId(mr, rarUniqueId);
      
      for (ManagedConnectionFactory mcf : connector.getManagedConnectionFactories())
      {
         javax.resource.spi.ManagedConnectionFactory jcaMcf = mcf.getManagedConnectionFactory();
         // FIXME: jcaMcf is null due to weakreference.

         Class<?> mcfCls = jcaMcf.getClass();
         String key = rarUniqueId + "#" + mcfCls.getName(); //IMPORTANT: make the key uniqueId#class name
         String name = mcfCls.getSimpleName();
         DiscoveredResourceDetails mcfRes = new DiscoveredResourceDetails(context.getResourceType(), key, name, null,
            "Managed Connection Factories", context.getDefaultPluginConfiguration(), null);
         result.add(mcfRes);
      }
      return result;
   }

}
