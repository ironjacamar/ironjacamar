/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
import org.jboss.jca.common.api.metadata.common.Capacity;
import org.jboss.jca.common.api.metadata.common.Pool;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.resourceadapter.WorkManager;
import org.jboss.jca.common.metadata.common.CredentialImpl;
import org.jboss.jca.common.metadata.common.PoolImpl;
import org.jboss.jca.common.metadata.common.SecurityImpl;
import org.jboss.jca.common.metadata.common.TimeOutImpl;
import org.jboss.jca.common.metadata.common.ValidationImpl;
import org.jboss.jca.common.metadata.common.XaPoolImpl;
import org.jboss.jca.common.metadata.resourceadapter.ActivationImpl;
import org.jboss.jca.common.metadata.resourceadapter.AdminObjectImpl;
import org.jboss.jca.common.metadata.resourceadapter.ConnectionDefinitionImpl;

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
   private ActivationImpl raImpl = null;

   private TransactionSupportEnum transactionSupport;
   private List<ConnectionDefinition> connectionDefinitions;
   private List<AdminObject> adminObjects;
   //private Map<String, String> configProperties;
   //private List<String> beanValidationGroups;
   //private String bootstrapContext;


   private TimeOutImpl timeOut = null;
   
   private SecurityImpl security = null;
   
   private ValidationImpl validation = null;

   private Pool pool = null;
   
   private WorkManager workmanager = null;

   private String jndiName;
   private String rarName;
   private String poolName;
   private String connectionDefinition;
   private Map<String, String> rarConfigProperty;
   private Map<String, String> connConfigProperty;
   private Boolean noTxSeparatePool;
   private Boolean interleaving;

   /**
    * create a LegacyConnectionFactoryImp
    * 
    * @param jndiName jndiName
    * @param rarName rarName
    * @param rarConfigProperty rarConfigProperty
    * @param poolName poolName
    * @param connectionDefinition connectionDefinition
    * @param connConfigProperty connConfigProperty
    * @param transactionSupport transactionSupport
    */
   public LegacyConnectionFactoryImp(String jndiName, String rarName, Map<String, String> rarConfigProperty, 
         String poolName, String connectionDefinition, Map<String, String> connConfigProperty, 
         TransactionSupportEnum transactionSupport)
   {
      this.jndiName = jndiName;
      this.rarName = rarName;
      this.poolName = poolName;
      if (rarConfigProperty != null)
      {
         this.rarConfigProperty = new HashMap<String, String>(rarConfigProperty.size());
         this.rarConfigProperty.putAll(rarConfigProperty);
      }
      else
      {
         this.rarConfigProperty = new HashMap<String, String>(0);
      }
      if (connConfigProperty != null)
      {
         this.connConfigProperty = new HashMap<String, String>(connConfigProperty.size());
         this.connConfigProperty.putAll(connConfigProperty);
      }
      else
      {
         this.connConfigProperty = new HashMap<String, String>(0);
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
      Boolean isXA = Boolean.FALSE;
      Recovery recovery = null;
      if (transactionSupport.equals(TransactionSupportEnum.XATransaction))
      {
         isXA = Boolean.TRUE;
         recovery = new Recovery(new CredentialImpl("user", "password", null), null, false);
      }
      ConnectionDefinitionImpl connDef =
         new ConnectionDefinitionImpl(connConfigProperty, "FIXME", jndiName, poolName,
                                      Defaults.ENABLED, Defaults.USE_JAVA_CONTEXT, Defaults.USE_CCM,
                                      Defaults.SHARABLE, Defaults.ENLISTMENT, Defaults.CONNECTABLE, Defaults.TRACKING,
                                      Defaults.MCP, Defaults.ENLISTMENT_TRACE,
                                      pool, timeOut, validation, security, recovery, isXA);
      
      connectionDefinitions = new ArrayList<ConnectionDefinition>();
      connectionDefinitions.add(connDef);
      raImpl = new ActivationImpl("ID", rarName, transactionSupport, connectionDefinitions, adminObjects,
                                  rarConfigProperty, null, null, workmanager);
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
      timeOut = new TimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
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
      validation = new ValidationImpl(Boolean.FALSE, backgroundValidation, backgroundValidationMillis, useFastFail);
      return this;
   }

   /**
    * build pool part
    * 
    * @param minPoolSize minPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param capacity capacity
    * @param noTxSeparatePool noTxSeparatePool
    * @param interleaving interleaving
    * @return this
    * @throws Exception exception
    */
   public LegacyConnectionFactoryImp buildCommonPool(Integer minPoolSize, Integer maxPoolSize, 
         Boolean prefill, Capacity capacity, Boolean noTxSeparatePool, Boolean interleaving) throws Exception
   {
      if (transactionSupport == TransactionSupportEnum.XATransaction)
         pool = new XaPoolImpl(minPoolSize, null, maxPoolSize, prefill, Defaults.USE_STRICT_MIN, 
            Defaults.FLUSH_STRATEGY, capacity, Defaults.FAIR, Defaults.IS_SAME_RM_OVERRIDE, interleaving, Defaults.PAD_XID,
            Defaults.WRAP_XA_RESOURCE, noTxSeparatePool);
      else
         pool = new PoolImpl(minPoolSize, null, maxPoolSize, prefill, Defaults.USE_STRICT_MIN,
                             Defaults.FLUSH_STRATEGY, capacity, Defaults.FAIR);
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
      security = new SecurityImpl(securityDomainManaged, securityDomainAndApplicationManaged, applicationManaged);
      return this;
   }
   
   /**
    * build admin object
    * 
    * @param className className
    * @param jndiName jndiName
    * @param poolName poolName
    * @param configProperties configProperties
    * @param enabled enabled
    * @param useJavaContext useJavaContext
    * @return this
    * @throws Exception exception
    */
   public LegacyConnectionFactoryImp buildAdminObejcts(String className, String jndiName, String poolName,
         Map<String, String> configProperties, boolean enabled, boolean useJavaContext)  throws Exception
   {
      if (adminObjects == null)
      {
         adminObjects = new ArrayList<AdminObject>();
      }
      adminObjects.add(
            new AdminObjectImpl(configProperties, className, jndiName, poolName, enabled, useJavaContext));
      return this;
   }
   
   
   /**
    * build workmanager
    * 
    * @param workmanager workmanager
    * @return this
    * @throws Exception exception
    */
   public LegacyConnectionFactoryImp buildWorkManager(WorkManager workmanager) throws Exception
   {
      this.workmanager = workmanager;
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
      return rarConfigProperty;
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
