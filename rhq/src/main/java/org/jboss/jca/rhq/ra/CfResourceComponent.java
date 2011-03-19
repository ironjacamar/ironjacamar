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

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.api.management.ConnectionFactory;
import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagementRepository;

import org.jboss.jca.rhq.core.AbstractResourceComponent;
import org.jboss.jca.rhq.core.ManagementRepositoryManager;
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
public class CfResourceComponent extends AbstractResourceComponent
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
      String jndiName = getJndiName();

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
      
      PropertySimple poolNameProp = new PropertySimple("pool-name", cf.getPool().getName());
      config.put(poolNameProp);
      
      PropertySimple minSizeProp = new PropertySimple("min-pool-size", Integer.valueOf(poolConfig.getMinSize()));
      config.put(minSizeProp);
      
      PropertySimple maxSizeProp = new PropertySimple("max-pool-size", Integer.valueOf(poolConfig.getMaxSize()));
      config.put(maxSizeProp);
      
      Boolean doBackGroundValidation = Boolean.valueOf(poolConfig.isBackgroundValidation());
      PropertySimple isBackGroundValidateProp = new PropertySimple("background-validation", doBackGroundValidation);
      config.put(isBackGroundValidateProp);
      
      Long bvInterval = Long.valueOf(poolConfig.getBackgroundValidationInterval());
      PropertySimple backGroundValidateIntervalProp = new PropertySimple("background-validation-millis", bvInterval);
      config.put(backGroundValidateIntervalProp);
      
      Integer bvMinutes = Integer.valueOf(poolConfig.getBackgroundValidationMinutes());
      PropertySimple backGroundValidateMintuesProp = new PropertySimple("background-validation-minutes", bvMinutes);
      config.put(backGroundValidateMintuesProp);
      
      Long blTimeout = Long.valueOf(poolConfig.getBlockingTimeout());
      PropertySimple blockingTimeoutProp = new PropertySimple("blocking-timeout-millis", blTimeout);
      config.put(blockingTimeoutProp);
      
      Long idleTimeoutMills = poolConfig.getIdleTimeout();
      
      Integer idleTimeout = (int)(idleTimeoutMills / (1000 * 60)); // convert to minutes
      PropertySimple idleTimeoutProp = new PropertySimple("idle-timeout-minutes", idleTimeout);
      config.put(idleTimeoutProp);
      
      PropertySimple prefillProp = new PropertySimple("prefill", Boolean.valueOf(poolConfig.isPrefill()));
      config.put(prefillProp);
      
      PropertySimple useStictMinProp = new PropertySimple("use-strict-min", Boolean.valueOf(poolConfig.isStrictMin()));
      config.put(useStictMinProp);
      
      PropertySimple useFasFailProp = new PropertySimple("use-fast-fail", Boolean.valueOf(poolConfig.isUseFastFail()));
      config.put(useFasFailProp);
      
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
      Integer minPoolSize = Integer.valueOf(config.getSimpleValue("min-pool-size", "0"));
      poolConfig.setMinSize(minPoolSize.intValue());
      
      Integer maxPoolSize = Integer.valueOf(config.getSimpleValue("max-pool-size", "20"));
      poolConfig.setMaxSize(maxPoolSize.intValue());
      
      Boolean backGroundValid = Boolean.valueOf(config.getSimpleValue("background-validation", "false"));
      poolConfig.setBackgroundValidation(backGroundValid.booleanValue());
      
      // background-validation-millis
      
      // background-validation-minutes
      Integer backGroundValidMinutes = Integer.valueOf(config.getSimpleValue("background-validation-minutes", "0"));
      poolConfig.setBackgroundValidationMinutes(backGroundValidMinutes.intValue());

      // blocking-timeout-millis
      Long blockTimeoutMillis = Long.valueOf(config.getSimpleValue("blocking-timeout-millis", "30000"));
      poolConfig.setBlockingTimeout(blockTimeoutMillis.longValue());
      
      // idle-timeout-minutes
      Integer idleTimeoutMinutes = Integer.valueOf(config.getSimpleValue("idle-timeout-minutes", "30"));
      Long idleTimeoutMillis = Long.valueOf(idleTimeoutMinutes * 60 * 1000);
      poolConfig.setIdleTimeout(idleTimeoutMillis.longValue());
      
      // prefill
      Boolean preFill = Boolean.valueOf(config.getSimpleValue("prefill", "true"));
      poolConfig.setPrefill(preFill);
      
      // use-strict-min
      Boolean useStrictMin = Boolean.valueOf(config.getSimpleValue("use-strict-min", "false"));
      poolConfig.setStrictMin(useStrictMin);
      
      // use-fast-fail
      Boolean useFastFail = Boolean.valueOf(config.getSimpleValue("use-fast-fail", "false"));
      poolConfig.setUseFastFail(useFastFail);
      
      updateResourceConfiguration.setStatus(ConfigurationUpdateStatus.SUCCESS);
      
   }
}
