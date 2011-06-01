/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.api.connectionmanager.pool.Pool;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.api.management.DataSource;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.core.spi.statistics.StatisticsPlugin;

import org.jboss.jca.rhq.core.IronJacamarResourceComponent;
import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.core.PoolResourceComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.DataType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.DeleteResourceFacet;

/**
 * Represent <b>XA Datasource</b> in JCA container
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 */
public class DsResourceComponent extends PoolResourceComponent implements DeleteResourceFacet
{
   
   /** log */
   private static final Logger logger = Logger.getLogger(DsResourceComponent.class);
   
   /**
    * getDataSource
    * 
    * @return DataSource associated with this ResourceComponent
    */
   private DataSource getDataSource()
   {
      ManagementRepository mr = ManagementRepositoryManager.getManagementRepository();
      Configuration plugConfig = getPluginConfiguration();
      String dsJndiName = plugConfig.getSimpleValue("jndi-name", null);
      if (dsJndiName == null || dsJndiName.length() == 0)
      {
         throw new IllegalStateException("DataSource jndi name is null.");
      }
      for (DataSource ds : mr.getDataSources())
      {
         if (dsJndiName.equals(ds.getJndiName()))
         {
            return ds;
         }
      }
      throw new IllegalStateException("Can not find the DataSource");
   }

   
   /**
    * loadResourceConfiguration
    * @return The resource Configuration
    * @throws Exception exception
    */
   @Override
   public Configuration loadResourceConfiguration() throws Exception
   {
      Configuration config = new Configuration();
      DataSource ds = getDataSource();
      
      // jndi name
      PropertySimple jndiNameProp = new PropertySimple("jndi-name", ds.getJndiName());
      config.put(jndiNameProp);
      
      // is xa
      PropertySimple isXAProp = new PropertySimple("xa", Boolean.valueOf(ds.isXA()));
      config.put(isXAProp);
      
      // conn-pool
      PoolConfiguration poolConfig = ds.getPoolConfiguration();
      putPoolConfigToResourceConfiguration(ds.getPool(), poolConfig, config);
      
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
      DataSource ds = getDataSource();
      
      // update jndi-name
      String jndiName = config.getSimpleValue("jndi-name", null);
      if (null != jndiName && jndiName.length() > 0)
      {
         ds.setJndiName(jndiName);
      }
      
      // update conn-pool configurations
      PoolConfiguration poolConfig = ds.getPoolConfiguration();
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
      DataSource ds = getDataSource();
      return ds.getPool();
   }
   
   /**
    * Available DataSource Statistics names
    */
   private static final List<String> dsStatisticsNames = new ArrayList<String>();
   static
   {
      dsStatisticsNames.add("PreparedStatementCacheAccessCount");
      dsStatisticsNames.add("PreparedStatementCacheAddCount");
      dsStatisticsNames.add("PreparedStatementCacheCurrentSize");
      dsStatisticsNames.add("PreparedStatementCacheDeleteCount");
      dsStatisticsNames.add("PreparedStatementCacheHitCount");
      dsStatisticsNames.add("PreparedStatementCacheMissCount");
   }
   
   
   /**
    * Gets values for MeasurementReport
    * 
    * @param measurementReport the MeasurementReport
    * @param measurementScheduleRequests the requests
    * @throws Exception the exception
    */
   @Override
   public void getValues(MeasurementReport measurementReport,
         Set<MeasurementScheduleRequest> measurementScheduleRequests) throws Exception
   {
      // super for PoolStatistics
      super.getValues(measurementReport, measurementScheduleRequests);
      DataSource ds = getDataSource();
      StatisticsPlugin statistics = ds.getStatistics();
      for (MeasurementScheduleRequest request : measurementScheduleRequests)
      {
         String reqName = request.getName();
         if (request.getDataType().equals(DataType.MEASUREMENT) && dsStatisticsNames.contains(reqName))
         {
            Double value = Double.valueOf(statistics.getValue(reqName).toString());
            MeasurementDataNumeric dsStatisMetrics = new MeasurementDataNumeric(request, value);
            measurementReport.addData(dsStatisMetrics);
         }
      }
   }


   /**
    * {@inheritDoc}}
    */
   @Override
   public void deleteResource() throws Exception
   {
      DataSource ds = getDataSource();
      String jndiName = ds.getJndiName();
      IronJacamarResourceComponent ironJacamarResCompo = 
         (IronJacamarResourceComponent)this.getResourceContext().getParentResourceComponent();
      try
      {
         if (!ironJacamarResCompo.unDeployDataSource(jndiName))
         {
            throw new IllegalStateException("Can not delete the DataSource with JndiName: " + jndiName);
         }
         logger.debug("Finished undeploy DataSource: " + jndiName);
      }
      catch (Throwable e)
      {
         throw new Exception(e);
      }
   }
   
}
