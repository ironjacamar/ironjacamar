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

import org.jboss.jca.core.api.connectionmanager.pool.Pool;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.api.management.ConnectionFactory;
import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagementRepository;

import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.core.PoolResourceComponent;
import org.jboss.jca.rhq.util.ManagementRepositoryHelper;

import org.jboss.logging.Logger;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.PropertySimple;

import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;

/**
 * CfResourceComponent represent the ManagedConnectionFactory in JCA container.
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 */
public class CfResourceComponent extends PoolResourceComponent
{
   /** log */
   private static final Logger logger = Logger.getLogger(CfResourceComponent.class);
   
   /**
    * Get associated ConnectionFactory
    * 
    * @return ConnectionFactory
    */
   public ConnectionFactory getConnectionFactory()
   {
      ManagementRepository mr = ManagementRepositoryManager.getManagementRepository();
      Connector connector = ManagementRepositoryHelper.getConnectorByUniqueId(mr, getRarUniqueId());
      Configuration plugConfig = getPluginConfiguration();
      String jndiName = plugConfig.getSimpleValue("jndi-name", null);
      if (jndiName == null || jndiName.length() == 0)
      {
         throw new IllegalStateException("ConnectionFactory jndi name is null.");
      }

      for (ConnectionFactory cf : connector.getConnectionFactories())
      {
         if (cf.getJndiName().equals(jndiName))
         {
            logger.debug("Class Name is: " + jndiName);
            return cf;
         }
      }
      return null;
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
      
      ConnectionFactory cf = getConnectionFactory();
      if (cf == null)
         throw new IllegalStateException("Can not find ConnectionFactory.");
      
      // jndi name
      PropertySimple jndiNameProp = new PropertySimple("jndi-name", cf.getJndiName());
      config.put(jndiNameProp);
      
      // conn-pool
      PoolConfiguration poolConfig = cf.getPoolConfiguration();
      putPoolConfigToResourceConfiguration(cf.getPool(), poolConfig, config);
      
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
      
      ConnectionFactory cf = getConnectionFactory();
      if (cf == null)
         throw new IllegalStateException("Can not find ConnectionFactory.");
      
      // update jndi-name
      String jndiName = config.getSimpleValue("jndi-name", null);
      if (null != jndiName && jndiName.length() > 0)
      {
         cf.setJndiName(jndiName);
      }
      
      // update conn-pool configurations
      PoolConfiguration poolConfig = cf.getPoolConfiguration();
      updatePoolConfiguration(poolConfig, config);
      
      updateResourceConfiguration.setStatus(ConfigurationUpdateStatus.SUCCESS);
      
   }

   /**
    * Gets Pool.
    * 
    * @return Pool the pool.
    */
   @Override
   protected Pool getPool()
   {
      ConnectionFactory cf = getConnectionFactory();
      return cf.getPool();
   }
}
