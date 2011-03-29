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
package org.jboss.jca.rhq.ds;

import org.jboss.jca.core.api.management.DataSource;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.ra.RarResourceComponent;

import java.util.HashSet;
import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

/**
 * Discovery<code>DatasourceResourceDiscoveryComponent</code> from JCA container.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 */
public class DsResourceDiscoveryComponent
   implements ResourceDiscoveryComponent<RarResourceComponent>
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
      ResourceDiscoveryContext<RarResourceComponent> context) 
      throws InvalidPluginConfigurationException, Exception
   {
      Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();

      ManagementRepository mr = ManagementRepositoryManager.getManagementRepository();
      for (DataSource ds : mr.getDataSources())
      {
         String dsJndiName = ds.getJndiName();
         DiscoveredResourceDetails dsRes = new DiscoveredResourceDetails(context.getResourceType(),
               dsJndiName, dsJndiName, null, "Datasource of " + dsJndiName, context.getDefaultPluginConfiguration(),
               null);
         Configuration configuration = dsRes.getPluginConfiguration();
         configuration.put(new PropertySimple("jndi-name", dsJndiName));
         result.add(dsRes);
      }
      return result;
   }

}
