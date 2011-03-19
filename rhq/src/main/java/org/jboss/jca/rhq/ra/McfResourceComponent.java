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
import org.jboss.jca.core.api.management.ManagedConnectionFactory;
import org.jboss.jca.rhq.core.AbstractResourceComponent;

import java.util.List;

import javax.resource.spi.ResourceAdapterAssociation;

import org.jboss.logging.Logger;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;

/**
 * McfResourceComponent represent the ManagedConnectionFactory in JCA container.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 */
public class McfResourceComponent extends AbstractResourceComponent
{
   /** log */
   private static final Logger logger = Logger.getLogger(McfResourceComponent.class);
   
   /**
    * Get associated ManagedConnectionFactory
    * 
    * @return ManagedConnectionFactory
    */
   private ManagedConnectionFactory getManagedConnectionFactory()
   {
      CfResourceComponent parentRes = (CfResourceComponent)getResourceContext().getParentResourceComponent();
      return parentRes.getConnectionFactory().getMcf();
   }
   
   /**
    * loadResourceConfiguration
    * 
    * 
    * @return Configuration Configuration
    * @throws Exception exception
    */
   @Override
   public Configuration loadResourceConfiguration() throws Exception
   {
      Configuration config = new Configuration();
      
      ManagedConnectionFactory mcf = getManagedConnectionFactory();
      javax.resource.spi.ManagedConnectionFactory jcaMcf = mcf.getManagedConnectionFactory();
      
      // mcf-class-name
      Class<?> mcfCls = jcaMcf.getClass();
      PropertySimple clsNameProp = new PropertySimple("mcf-class-name", mcfCls.getName());
      config.put(clsNameProp);
      
      // cf-interface-name
      
      // cf-impl-name
      
      // connection-interface-name
      
      // connection-impl-name
      
      // use-ra-association
      boolean useRaAsso = ResourceAdapterAssociation.class.isAssignableFrom(jcaMcf.getClass());
      PropertySimple useRaAssoProp = new PropertySimple("use-ra-association", Boolean.valueOf(useRaAsso));
      config.put(useRaAssoProp);
      
      // config properties
      List<ConfigProperty> mcfConfProps = mcf.getConfigProperties();
      PropertyList configList = getConfigPropertiesList(jcaMcf, mcfConfProps);
      config.put(configList);
      
      return config;
   }
   
   
   /**
    * updateResourceConfiguration
    * 
    * @param updateResourceConfiguration the ConfigurationUpdateReport
    */
   @Override
   public void updateResourceConfiguration(ConfigurationUpdateReport updateResourceConfiguration)
   {
      super.updateResourceConfiguration(updateResourceConfiguration);
      Configuration config = updateResourceConfiguration.getConfiguration();
      
      ManagedConnectionFactory mcf = getManagedConnectionFactory();
      
      // config-properties
      PropertyList configPropertiesList = config.getList("config-property");
      javax.resource.spi.ManagedConnectionFactory jcaMcf = mcf.getManagedConnectionFactory();
      List<ConfigProperty> configProperties = mcf.getConfigProperties();
      updatePropertyList(jcaMcf, configPropertiesList, configProperties);
      
      updateResourceConfiguration.setStatus(ConfigurationUpdateStatus.SUCCESS);
      
   }

}
