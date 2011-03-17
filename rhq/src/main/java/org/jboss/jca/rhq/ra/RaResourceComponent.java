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

import org.jboss.jca.core.api.management.ConfigProperty;
import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.core.api.management.ResourceAdapter;
import org.jboss.jca.rhq.core.AbstractResourceComponent;
import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.util.ManagementRepositoryHelper;

import java.util.List;

import org.jboss.logging.Logger;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;


/**
 * Represent Resource Adpater in JCA container.
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 */
public class RaResourceComponent extends AbstractResourceComponent
{
   /** log */
   private static final Logger logger = Logger.getLogger(RaResourceComponent.class);
   
   /**
    * Gets associated ResourceAdapter.
    * 
    * @return ResourceAdapter null if there is no ResourceAdapter in the connector.
    */
   private ResourceAdapter getResourceAdapter()
   {
      ManagementRepository mr = ManagementRepositoryManager.getManagementRepository();
      Connector connector = ManagementRepositoryHelper.getConnectorByUniqueId(mr, getRarUniqueId());
      return connector.getResourceAdapter();
   }

   /**
    * loadResourceConfiguration
    * 
    * @return Configuration configuration
    * @throws Exception exception
    */
   @Override
   public Configuration loadResourceConfiguration() throws Exception
   {

      Configuration config = new Configuration();
      ResourceAdapter ra = getResourceAdapter();
      if (ra != null)
      {
         String raClsName = ra.getResourceAdapter().getClass().getName();
         
         // class-name
         PropertySimple clsNameProp = new PropertySimple("class-name", raClsName);
         config.put(clsNameProp);
         
         List<ConfigProperty> configProps = ra.getConfigProperties();
         PropertyList configList = getConfigPropertiesList(ra.getResourceAdapter(), configProps);
         config.put(configList);
      }

      return config;
   }
   
   @Override
   public void updateResourceConfiguration(ConfigurationUpdateReport updateResourceConfiguration)
   {
      super.updateResourceConfiguration(updateResourceConfiguration);
      ResourceAdapter ra = getResourceAdapter();
      if (ra != null)
      {
         Configuration config = updateResourceConfiguration.getConfiguration();
         PropertyList configPropList = config.getList("config-property");
         List<ConfigProperty> configProps = ra.getConfigProperties();
         updatePropertyList(ra.getResourceAdapter(), configPropList, configProps);
      }
      updateResourceConfiguration.setStatus(ConfigurationUpdateStatus.SUCCESS);
   }
}
