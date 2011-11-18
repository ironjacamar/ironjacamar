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


import org.jboss.jca.core.api.connectionmanager.pool.Pool;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.api.connectionmanager.pool.PoolStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.DataType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.operation.OperationResult;

/**
 * PoolResourceComponent
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public abstract class PoolResourceComponent extends AbstractResourceComponent
{

   /**
    * Puts configurations in the connection pool to the resource configuration
    * 
    * @param pool Pool
    * @param poolConfig PoolConfiguration
    * @param config Configuration
    */
   protected void putPoolConfigToResourceConfiguration(Pool pool, PoolConfiguration poolConfig, Configuration config)
   {
      PropertySimple poolNameProp = new PropertySimple("pool-name", pool.getName());
      config.put(poolNameProp);
      
      PropertySimple minSizeProp = new PropertySimple("min-pool-size", Integer.valueOf(poolConfig.getMinSize()));
      config.put(minSizeProp);
      
      PropertySimple maxSizeProp = new PropertySimple("max-pool-size", Integer.valueOf(poolConfig.getMaxSize()));
      config.put(maxSizeProp);
      
      Boolean doBackGroundValidation = Boolean.valueOf(poolConfig.isBackgroundValidation());
      PropertySimple isBackGroundValidateProp = new PropertySimple("background-validation", doBackGroundValidation);
      config.put(isBackGroundValidateProp);
      
      Long bvMillis = Long.valueOf(poolConfig.getBackgroundValidationMillis());
      PropertySimple backGroundValidateMillisProp = new PropertySimple("background-validation-millis", bvMillis);
      config.put(backGroundValidateMillisProp);
      
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
   }
   
   /**
    * Updates PoolConfiguration
    * 
    * @param poolConfig PoolConfiguration
    * @param config Configuration
    */
   protected void updatePoolConfiguration(PoolConfiguration poolConfig, Configuration config)
   {
      Integer minPoolSize = Integer.valueOf(config.getSimpleValue("min-pool-size", "0"));
      poolConfig.setMinSize(minPoolSize.intValue());
      
      Integer maxPoolSize = Integer.valueOf(config.getSimpleValue("max-pool-size", "20"));
      poolConfig.setMaxSize(maxPoolSize.intValue());
      
      Boolean backGroundValid = Boolean.valueOf(config.getSimpleValue("background-validation", "false"));
      poolConfig.setBackgroundValidation(backGroundValid.booleanValue());
      
      // background-validation-millis
      
      // background-validation-millis
      Long backGroundValidMillis = Long.valueOf(config.getSimpleValue("background-validation-millis", "0"));
      poolConfig.setBackgroundValidationMillis(backGroundValidMillis.intValue());

      // blocking-timeout-millis
      Long blockTimeoutMillis = Long.valueOf(config.getSimpleValue("blocking-timeout-millis", "30000"));
      poolConfig.setBlockingTimeout(blockTimeoutMillis.longValue());
      
      // idle-timeout-minutes
      Integer idleTimeoutMinutes = Integer.valueOf(config.getSimpleValue("idle-timeout-minutes", "30"));
      poolConfig.setIdleTimeoutMinutes(idleTimeoutMinutes.intValue());
      
      // prefill
      Boolean preFill = Boolean.valueOf(config.getSimpleValue("prefill", "true"));
      poolConfig.setPrefill(preFill);
      
      // use-strict-min
      Boolean useStrictMin = Boolean.valueOf(config.getSimpleValue("use-strict-min", "false"));
      poolConfig.setStrictMin(useStrictMin);
      
      // use-fast-fail
      Boolean useFastFail = Boolean.valueOf(config.getSimpleValue("use-fast-fail", "false"));
      poolConfig.setUseFastFail(useFastFail);
   }
   
   
   /**
    * Invokes Operations.
    * @param s operation name
    * @param configuration Configuration of the operation parameters
    * @return OperationResult result of the Operation
    * @throws InterruptedException when canceled.
    * @throws Exception the exception
    */
   @Override
   public OperationResult invokeOperation(String s, Configuration configuration) throws InterruptedException, Exception
   {
      OperationResult result = new OperationResult();
      Pool pool = getPool();
      if ("flush".equals(s))
      {
         pool.flush();
      }
      else if ("resetConnectionPool".equals(s))
      {
         pool.flush(true);
      }
      else if ("testConnection".equals(s))
      {
         boolean testConn = pool.testConnection();
         Configuration config = result.getComplexResults();
         config.put(new PropertySimple("result", testConn));
      }
      else if ("listFormattedSubPoolStatistics".equals(s))
      {
         result.setErrorMessage("Not Implemented yet!");
      }
      else if ("listStatistics".equals(s))
      {
         result.setErrorMessage("Not Implemented yet!");
      }
      else
      {
         throw new IllegalStateException("Wrong operation: " + s);
      }
      return result;
   }

   /**
    * Gets Pool.
    * 
    * @return Pool
    */
   protected abstract Pool getPool();
   
   /**
    * Available PoolStatistics names
    */
   private static final List<String> poolStatisticsNames = new ArrayList<String>();
   static
   {
      poolStatisticsNames.add("ActiveCount");
      poolStatisticsNames.add("AvailableCount");
      poolStatisticsNames.add("AverageBlockingTime");
      poolStatisticsNames.add("AverageCreationTime");
      poolStatisticsNames.add("CreatedCount");
      poolStatisticsNames.add("DestroyedCount");
      poolStatisticsNames.add("MaxCreationTime");
      poolStatisticsNames.add("MaxUsedCount");
      poolStatisticsNames.add("MaxWaitCount");
      poolStatisticsNames.add("MaxWaitTime");
      poolStatisticsNames.add("TimedOut");
      poolStatisticsNames.add("TotalBlockingTime");
      poolStatisticsNames.add("TotalCreationTime");
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
      Pool pool = getPool();
      PoolStatistics poolStatistics = pool.getStatistics();
      for (MeasurementScheduleRequest request : measurementScheduleRequests)
      {
         String reqName = request.getName();
         if (request.getDataType().equals(DataType.MEASUREMENT) && poolStatisticsNames.contains(reqName))
         {
            Double value = Double.valueOf(poolStatistics.getValue(reqName).toString());
            MeasurementDataNumeric poolStatisMetrics = new MeasurementDataNumeric(request, value);
            measurementReport.addData(poolStatisMetrics);
         }
      }
   }
   
}
