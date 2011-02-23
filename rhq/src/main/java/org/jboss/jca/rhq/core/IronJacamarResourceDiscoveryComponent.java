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
package org.jboss.jca.rhq.core;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

/**
 * Discovery the root element of this plug-in.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 */
public class IronJacamarResourceDiscoveryComponent implements ResourceDiscoveryComponent<IronJacamarResourceComponent>
{
   /**
    * discoverResources
    * 
    * @param context ResourceDiscoveryContext<AdminObjectResourceComponent>
    * @return Set<DiscoveredResourceDetails> set of DiscoveredResourceDetails
    * @throws InvalidPluginConfigurationException InvalidPluginConfigurationException
    * @throws Exception Exception
    */
   public Set<DiscoveredResourceDetails> discoverResources(
      ResourceDiscoveryContext<IronJacamarResourceComponent> context) 
      throws InvalidPluginConfigurationException, Exception
   {

      Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
      String resourceKey = "IronJacamar_AS7";
      String resourceName = "IronJacamar_AS7";
      String resourceVersion = "1.0.0";
      String resourceDescription = "JCA Management Resources";

      DiscoveredResourceDetails resource = new DiscoveredResourceDetails(context.getResourceType(), resourceKey,
         resourceName, resourceVersion, resourceDescription, context.getDefaultPluginConfiguration(), null);
      result.add(resource);
      return result;
   }

}
