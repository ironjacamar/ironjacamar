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
package org.jboss.jca.as.converters;

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.metadata.common.CommonConnDefImpl;
import org.jboss.jca.common.metadata.common.CommonPoolImpl;
import org.jboss.jca.common.metadata.common.CommonSecurityImpl;
import org.jboss.jca.common.metadata.common.CommonTimeOutImpl;
import org.jboss.jca.common.metadata.common.CommonValidationImpl;
import org.jboss.jca.common.metadata.resourceadapter.ResourceAdapterImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A LegacyConnectionFactoryImp impl.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class LegacyConnectionFactoryImp implements TxConnectionFactory
{
   private ResourceAdapterImpl raImpl = null;

   private TransactionSupportEnum transactionSupport;
   private List<CommonConnDef> connectionDefinitions;
   //private List<CommonAdminObject> adminObjects;
   //private Map<String, String> configProperties;
   //private List<String> beanValidationGroups;
   //private String bootstrapContext;


   private CommonTimeOutImpl timeOut = null;
   
   private CommonSecurityImpl security = null;
   
   private CommonValidationImpl validation = null;

   private CommonPool pool = null;

   private String jndiName;
   private String rarName;
   private String poolName;
   private String connectionDefinition;
   private Map<String, String> configProperty;
   
   private Boolean noTxSeparatePool;
   private Boolean interleaving;

   /**
    * create a LegacyConnectionFactoryImp
    * 
    * @param jndiName jndiName
    * @param rarName rarName
    * @param poolName poolName
    * @param connectionDefinition connectionDefinition
    * @param configProperty configProperty
    * @param transactionSupport transactionSupport
    */
   public LegacyConnectionFactoryImp(String jndiName, String rarName, String poolName,
         String connectionDefinition, Map<String, String> configProperty, TransactionSupportEnum transactionSupport)
   {
      this.jndiName = jndiName;
      this.rarName = rarName;
      this.poolName = poolName;
      if (configProperty != null)
      {
         this.configProperty = new HashMap<String, String>(configProperty.size());
         this.configProperty.putAll(configProperty);
      }
      else
      {
         this.configProperty = new HashMap<String, String>(0);
      }
      this.connectionDefinition = connectionDefinition;
      this.transactionSupport = transactionSupport;
   }
   
   /**
    * buildResourceAdapterImpl
    * @throws Exception exception
    */
   public void buildResourceAdapterImpl()  throws Exception
   {
      CommonConnDefImpl connDef = new CommonConnDefImpl(configProperty, connectionDefinition, jndiName, poolName,
                                                        Defaults.ENABLED, Defaults.USE_JAVA_CONTEXT, Defaults.USE_CCM,
                                                        Defaults.SHARABLE, pool, timeOut, validation, security, null);
      connectionDefinitions = new ArrayList<CommonConnDef>();
      connectionDefinitions.add(connDef);
      raImpl = new ResourceAdapterImpl(null, rarName, transactionSupport, connectionDefinitions, null,
            null, null, null);
   }
   
   @Override
   public String toString()
   {
      String out = raImpl.toString();
      return out;
   }
   
   /**
    * build timeout part
    * 
    * @param blockingTimeoutMillis blockingTimeoutMillis
    * @param idleTimeoutMinutes idleTimeoutMinutes
    * @param allocationRetry allocationRetry
    * @param allocationRetryWaitMillis allocationRetryWaitMillis
    * @param xaResourceTimeout xaResourceTimeout
    * @return this 
    * @throws Exception exception
    */
   public LegacyConnectionFactoryImp buildTimeOut(Long blockingTimeoutMillis, Long idleTimeoutMinutes, 
         Integer allocationRetry, Long allocationRetryWaitMillis, Integer xaResourceTimeout) throws Exception
   {
      timeOut = new CommonTimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
            allocationRetryWaitMillis, xaResourceTimeout);
      return this;
   }
   
   /**
    * build validation part
    * 
    * @param backgroundValidation backgroundValidation
    * @param backgroundValidationMillis backgroundValidationMillis
    * @param useFastFail useFastFail
    * @return this
    * @throws Exception exception
    */
   public LegacyConnectionFactoryImp buildValidation(Boolean backgroundValidation, Long backgroundValidationMillis, 
         Boolean useFastFail) throws Exception
   {
      validation = new CommonValidationImpl(backgroundValidation, backgroundValidationMillis, useFastFail);
      return this;
   }

   /**
    * build pool part
    * 
    * @param minPoolSize minPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param noTxSeparatePool noTxSeparatePool
    * @param interleaving interleaving
    * @return this
    * @throws Exception exception
    */
   public LegacyConnectionFactoryImp buildCommonPool(Integer minPoolSize, Integer maxPoolSize, 
         Boolean prefill, Boolean noTxSeparatePool, Boolean interleaving) throws Exception
   {
      pool = new CommonPoolImpl(minPoolSize, maxPoolSize, prefill, Defaults.USE_STRICT_MIN, Defaults.FLUSH_STRATEGY);
      this.noTxSeparatePool = noTxSeparatePool;
      this.setInterleaving(interleaving);
      return this;
   }
   
   /** 
    * build security part
    * 
    * @param securityDomainManaged securityDomainManaged
    * @param securityDomainAndApplicationManaged securityDomainAndApplicationManaged
    * @param applicationManaged applicationManagedS
    * @return this
    * @throws Exception exception
    */
   public LegacyConnectionFactoryImp buildSecurity(String securityDomainManaged,
         String securityDomainAndApplicationManaged, boolean applicationManaged) throws Exception
   {
      security = new CommonSecurityImpl(securityDomainManaged, securityDomainAndApplicationManaged, applicationManaged);
      return this;
   }
   
   /**
    * build other properties
    * 
    * @return this
    */
   public LegacyConnectionFactoryImp buildOther()
   {
      return this;
   }
   

   @Override
   public String getJndiName()
   {
      return this.jndiName;
   }

   @Override
   public String getSecurityDomain()
   {
      return null;
   }

   @Override
   public Integer getMinPoolSize()
   {
      return pool.getMinPoolSize();
   }

   @Override
   public Integer getMaxPoolSize()
   {
      return pool.getMaxPoolSize();
   }

   @Override
   public Long getBlockingTimeoutMillis()
   {
      return this.timeOut.getBlockingTimeoutMillis();
   }

   @Override
   public Boolean isBackgroundValidation()
   {
      return this.validation.isBackgroundValidation();
   }

   @Override
   public Long getBackgroundValidationMillis()
   {
      return this.validation.getBackgroundValidationMillis();
   }

   @Override
   public Long getIdleTimeoutMinutes()
   {
      return this.timeOut.getIdleTimeoutMinutes();
   }

   @Override
   public Integer getAllocationRetry()
   {
      return this.timeOut.getAllocationRetry();
   }

   @Override
   public Long getAllocationRetryWaitMillis()
   {
      return this.timeOut.getAllocationRetryWaitMillis();
   }
   @Override
   public Boolean isPrefill()
   {
      return this.pool.isPrefill();
   }

   @Override
   public Boolean isUseFastFail()
   {
      return this.validation.isUseFastFail();
   }

   @Override
   public Boolean isNoTxSeparatePools()
   {
      return this.noTxSeparatePool;
   }

   
   @Override
   public Boolean isTrackConnectionByTx()
   {
      return false;
   }

   @Override
   public Integer getXaResourceTimeout()
   {
      return this.timeOut.getXaResourceTimeout();
   }

   @Override
   public String getRarName()
   {
      return rarName;
   }

   @Override
   public String getConnectionDefinition()
   {
      return connectionDefinition;
   }

   @Override
   public Map<String, String> getConfigProperties()
   {
      return configProperty;
   }

   @Override
   public TransactionSupportEnum getTransactionSupport()
   {
      return null;
   }
   
   /**
    * setInterleaving
    * @param interleaving interleaving
    */
   public void setInterleaving(Boolean interleaving)
   {
      this.interleaving = interleaving;
   }

   /**
    * getInterleaving
    * @return Boolean interleaving
    */
   public Boolean getInterleaving()
   {
      return interleaving;
   }
}
