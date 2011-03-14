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
import org.jboss.jca.core.api.management.ConfigProperty;
import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagedConnectionFactory;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.rhq.core.AbstractResourceComponent;

import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.util.ManagementRepositoryHelper;

import java.util.List;

import javax.resource.spi.ResourceAdapterAssociation;

import org.jboss.logging.Logger;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertySimple;

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

      ManagementRepository mr = ManagementRepositoryManager.getManagementRepository();
      Connector connector = ManagementRepositoryHelper.getConnectorByUniqueId(mr, getRarUniqueId());

      String jcaClsName = getJCAClassName();
      ManagedConnectionFactory mcf = null;
      for (ManagedConnectionFactory connectorMcf : connector.getManagedConnectionFactories())
      {
         Class<?> mcfCls = connectorMcf.getManagedConnectionFactory().getClass();
         if (mcfCls.getName().equals(jcaClsName))
         {
            logger.debug("Class Name is: " + jcaClsName);
            mcf = connectorMcf;
            break;
         }
      }
      if (null == mcf)
      {
         throw new IllegalStateException("Can not find the associated ManagedConnectionFactory in the Connector.");
      }
      
      javax.resource.spi.ManagedConnectionFactory jcaMcf = mcf.getManagedConnectionFactory();
      // jndi name
      
      // mcf-class-name
      PropertySimple clsNameProp = new PropertySimple("mcf-class-name", jcaClsName);
      config.put(clsNameProp);
      
      // cf-interface-name
      
      // cf-impl-name
      
      // connection-interface-name
      
      // connection-impl-name
      
      // use-ra-association
      boolean useRaAsso = ResourceAdapterAssociation.class.isAssignableFrom(jcaMcf.getClass());
      PropertySimple useRaAssoProp = new PropertySimple("use-ra-association", Boolean.valueOf(useRaAsso));
      config.put(useRaAssoProp);
      
      // conn_pool
      PoolConfiguration poolConfig = mcf.getPoolConfiguration();
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
      
      Integer blTimeout = Integer.valueOf((int)poolConfig.getBlockingTimeout());
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
      
      // config properties
      List<ConfigProperty> mcfConfProps = mcf.getConfigProperties();
      PropertyList configList = getConfigPropertiesList(jcaMcf, mcfConfProps);
      config.put(configList);
      
      return config;
   }
}
