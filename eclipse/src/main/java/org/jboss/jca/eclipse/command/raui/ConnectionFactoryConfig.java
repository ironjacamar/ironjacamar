/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.eclipse.command.raui;

import org.jboss.jca.codegenerator.ConfigPropType;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * ConnectionFactoryConfig is used to configure Connection Factory.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class ConnectionFactoryConfig implements Cloneable
{
   
   private boolean active;
   
   // general
   private String mcfClsName;
   private String mcfJndiName;
   private String mcfPoolName;
   private Boolean mcfEnabled;
   private Boolean mcfUseJavaCtx;
   private Boolean mcfUseCCM;
   
   // added for 1.1
   private Boolean sharable;
   private Boolean enlistment;
   
   private List<ConfigPropType> mcfConfigProps = new ArrayList<ConfigPropType>();
   
   // pool
   private PoolConfig poolConifg = new PoolConfig();
   
   // security
   private SecurityConfig securityConfig = new SecurityConfig();
   
   // timeout
   private TimeoutConfig timeoutConfig = new TimeoutConfig();
   
   // validation
   private ValidationConfig validationConfig = new ValidationConfig();
   
   // recovery
   private RecoveryConfig recoveryConfig = new RecoveryConfig();
   
   /**
    * The constructor
    */
   public ConnectionFactoryConfig()
   {
      super();
   }
   
   @Override
   public ConnectionFactoryConfig clone()
   {
      ConnectionFactoryConfig clone = new ConnectionFactoryConfig();
      clone.active = this.active;
      clone.enlistment = this.enlistment;
      clone.mcfClsName = this.mcfClsName;
      clone.mcfConfigProps = AdminObjectConfig.cloneConfigPropTypeList(mcfConfigProps);
      clone.mcfEnabled = this.mcfEnabled;
      clone.mcfJndiName = this.mcfJndiName;
      clone.mcfPoolName = this.mcfPoolName;
      clone.mcfUseCCM = this.mcfUseCCM;
      clone.mcfUseJavaCtx = this.mcfUseJavaCtx;
      clone.poolConifg = this.poolConifg.clone();
      clone.recoveryConfig = this.recoveryConfig.clone();
      clone.securityConfig = this.securityConfig.clone();
      clone.sharable = this.sharable;
      clone.timeoutConfig = this.timeoutConfig.clone();
      clone.validationConfig = this.validationConfig.clone();
      return clone;
   }
   
   /**
    * @param mcfConfigProps the mcfConfigProps to set
    */
   public void setMcfConfigProps(List<ConfigPropType> mcfConfigProps)
   {
      this.mcfConfigProps = mcfConfigProps;
   }

   /**
    * @param poolConifg the poolConifg to set
    */
   public void setPoolConifg(PoolConfig poolConifg)
   {
      this.poolConifg = poolConifg;
   }

   /**
    * @param securityConfig the securityConfig to set
    */
   public void setSecurityConfig(SecurityConfig securityConfig)
   {
      this.securityConfig = securityConfig;
   }

   /**
    * @param timeoutConfig the timeoutConfig to set
    */
   public void setTimeoutConfig(TimeoutConfig timeoutConfig)
   {
      this.timeoutConfig = timeoutConfig;
   }

   /**
    * @param validationConfig the validationConfig to set
    */
   public void setValidationConfig(ValidationConfig validationConfig)
   {
      this.validationConfig = validationConfig;
   }

   /**
    * @param recoveryConfig the recoveryConfig to set
    */
   public void setRecoveryConfig(RecoveryConfig recoveryConfig)
   {
      this.recoveryConfig = recoveryConfig;
   }

   /**
    * @return the active
    */
   public boolean isActive()
   {
      return active;
   }


   /**
    * @param active the active to set
    */
   public void setActive(boolean active)
   {
      this.active = active;
   }



   /**
    * @return the sharable
    */
   public Boolean getSharable()
   {
      return sharable;
   }

   /**
    * @param sharable the sharable to set
    */
   public void setSharable(Boolean sharable)
   {
      this.sharable = sharable;
   }


   /**
    * @return the enlistment
    */
   public Boolean getEnlistment()
   {
      return enlistment;
   }



   /**
    * @param enlistment the enlistment to set
    */
   public void setEnlistment(Boolean enlistment)
   {
      this.enlistment = enlistment;
   }



   /**
    * Get mcfClsName
    * @return The mcfClsName
    */
   public String getMcfClsName()
   {
      return mcfClsName;
   }

   /**
    * Set mcfClsName
    * @param mcfClsName The value to set
    */
   public void setMcfClsName(String mcfClsName)
   {
      this.mcfClsName = mcfClsName;
   }

   /**
    * Get mcfJndiName
    * @return The mcfJndiName
    */
   public String getMcfJndiName()
   {
      return mcfJndiName;
   }

   /**
    * Set mcfJndiName
    * @param mcfJndiName The value to set
    */
   public void setMcfJndiName(String mcfJndiName)
   {
      this.mcfJndiName = mcfJndiName;
   }

   /**
    * Get mcfPoolName
    * @return The mcfPoolName
    */
   public String getMcfPoolName()
   {
      return mcfPoolName;
   }

   /**
    * Set mcfPoolName
    * @param mcfPoolName The value to set
    */
   public void setMcfPoolName(String mcfPoolName)
   {
      this.mcfPoolName = mcfPoolName;
   }

   /**
    * Get mcfEnabled
    * @return The mcfEnabled
    */
   public Boolean getMcfEnabled()
   {
      return mcfEnabled;
   }

   /**
    * Set mcfEnabled
    * @param mcfEnabled The value to set
    */
   public void setMcfEnabled(Boolean mcfEnabled)
   {
      this.mcfEnabled = mcfEnabled;
   }

   /**
    * Get mcfUseJavaCtx
    * @return The mcfUseJavaCtx
    */
   public Boolean getMcfUseJavaCtx()
   {
      return mcfUseJavaCtx;
   }

   /**
    * Set mcfUseJavaCtx
    * @param mcfUseJavaCtx The value to set
    */
   public void setMcfUseJavaCtx(Boolean mcfUseJavaCtx)
   {
      this.mcfUseJavaCtx = mcfUseJavaCtx;
   }

   /**
    * Get mcfUseCCM
    * @return The mcfUseCCM
    */
   public Boolean getMcfUseCCM()
   {
      return mcfUseCCM;
   }

   /**
    * Set mcfUseCCM
    * @param mcfUseCCM The value to set
    */
   public void setMcfUseCCM(Boolean mcfUseCCM)
   {
      this.mcfUseCCM = mcfUseCCM;
   }

   /**
    * Get mcfConfigProps
    * @return The mcfConfigProps
    */
   public List<ConfigPropType> getMcfConfigProps()
   {
      return mcfConfigProps;
   }

   /**
    * Get poolConifg
    * @return The poolConifg
    */
   public PoolConfig getPoolConifg()
   {
      return poolConifg;
   }

   /**
    * Get securityConfig
    * @return The securityConfig
    */
   public SecurityConfig getSecurityConfig()
   {
      return securityConfig;
   }

   /**
    * Get timeoutConfig
    * @return The timeoutConfig
    */
   public TimeoutConfig getTimeoutConfig()
   {
      return timeoutConfig;
   }

   /**
    * Get validationConfig
    * @return The validationConfig
    */
   public ValidationConfig getValidationConfig()
   {
      return validationConfig;
   }


   /**
    * Get recoveryConfig
    * @return The recoveryConfig
    */
   public RecoveryConfig getRecoveryConfig()
   {
      return recoveryConfig;
   }

   /**
    * PoolConfig is used to configure Pool
    */
   public static class PoolConfig implements Cloneable
   {
      private Integer minPoolSize;
      private Integer initialPoolSize;
      private Integer maxPoolSize;
      private Boolean prefill;
      private Boolean useStrictMin;
      private FlushStrategy flushStrategy;
      private CapacityConfig capacityConfig = new CapacityConfig();
      
      private boolean defineXA;
      
      // XA related
      private Boolean overrideIsSameRM;
      private Boolean interleaving;
      private Boolean noTxSeparatePool;
      private Boolean padXid;
      private Boolean wrapXaResource;
      
      
      @Override
      public PoolConfig clone()
      {
         PoolConfig clone = new PoolConfig();
         clone.capacityConfig = this.capacityConfig.clone();
         clone.defineXA = this.defineXA;
         clone.flushStrategy = this.flushStrategy;
         clone.initialPoolSize = this.initialPoolSize;
         clone.interleaving = this.interleaving;
         clone.maxPoolSize = this.maxPoolSize;
         clone.minPoolSize = this.minPoolSize;
         clone.noTxSeparatePool = this.noTxSeparatePool;
         clone.overrideIsSameRM = this.overrideIsSameRM;
         clone.padXid = this.padXid;
         clone.prefill = this.prefill;
         clone.useStrictMin = this.useStrictMin;
         clone.wrapXaResource = this.wrapXaResource;
         return clone;
      }
      
      /**
       * @param capacityConfig the capacityConfig to set
       */
      public void setCapacityConfig(CapacityConfig capacityConfig)
      {
         this.capacityConfig = capacityConfig;
      }

      /**
       * @return the capacityConfig
       */
      public CapacityConfig getCapacityConfig()
      {
         return capacityConfig;
      }
      /**
       * @return the initialPoolSize
       */
      public Integer getInitialPoolSize()
      {
         return initialPoolSize;
      }
      /**
       * @param initialPoolSize the initialPoolSize to set
       */
      public void setInitialPoolSize(Integer initialPoolSize)
      {
         this.initialPoolSize = initialPoolSize;
      }
      /**
       * Get minPoolSize
       * @return The minPoolSize
       */
      public Integer getMinPoolSize()
      {
         return minPoolSize;
      }
      /**
       * Set minPoolSize
       * @param minPoolSize The value to set
       */
      public void setMinPoolSize(Integer minPoolSize)
      {
         this.minPoolSize = minPoolSize;
      }
      /**
       * Get maxPoolSize
       * @return The maxPoolSize
       */
      public Integer getMaxPoolSize()
      {
         return maxPoolSize;
      }
      /**
       * Set maxPoolSize
       * @param maxPoolSize The value to set
       */
      public void setMaxPoolSize(Integer maxPoolSize)
      {
         this.maxPoolSize = maxPoolSize;
      }
      /**
       * Get prefill
       * @return The prefill
       */
      public Boolean isPrefill()
      {
         return prefill;
      }
      /**
       * Set prefill
       * @param prefill The value to set
       */
      public void setPrefill(Boolean prefill)
      {
         this.prefill = prefill;
      }
      /**
       * Get useStrictMin
       * @return The useStrictMin
       */
      public Boolean isUseStrictMin()
      {
         return useStrictMin;
      }
      /**
       * Set useStrictMin
       * @param useStrictMin The value to set
       */
      public void setUseStrictMin(Boolean useStrictMin)
      {
         this.useStrictMin = useStrictMin;
      }
      /**
       * Get flushStrategy
       * @return The flushStrategy
       */
      public FlushStrategy getFlushStrategy()
      {
         return flushStrategy;
      }
      /**
       * Set flushStrategy
       * @param flushStrategy The value to set
       */
      public void setFlushStrategy(FlushStrategy flushStrategy)
      {
         this.flushStrategy = flushStrategy;
      }
      /**
       * Get defineXA
       * @return The defineXA
       */
      public boolean getDefineXA()
      {
         return defineXA;
      }
      /**
       * Set defineXA
       * @param defineXA The value to set
       */
      public void setDefineXA(boolean defineXA)
      {
         this.defineXA = defineXA;
      }
      /**
       * Get overrideIsSameRM
       * @return The overrideIsSameRM
       */
      public Boolean isOverrideIsSameRM()
      {
         return overrideIsSameRM;
      }
      /**
       * Set overrideIsSameRM
       * @param overrideIsSameRM The value to set
       */
      public void setOverrideIsSameRM(Boolean overrideIsSameRM)
      {
         this.overrideIsSameRM = overrideIsSameRM;
      }
      /**
       * Get interleaving
       * @return The interleaving
       */
      public Boolean isInterleaving()
      {
         return interleaving;
      }
      /**
       * Set interleaving
       * @param interleaving The value to set
       */
      public void setInterleaving(Boolean interleaving)
      {
         this.interleaving = interleaving;
      }
      /**
       * Get createSubPool
       * @return The createSubPool
       */
      public Boolean isNoTxSeparatePool()
      {
         return noTxSeparatePool;
      }
      /**
       * Set createSubPool
       * @param noTxSeparatePool The value to set
       */
      public void setNoTxSeparatePool(Boolean noTxSeparatePool)
      {
         this.noTxSeparatePool = noTxSeparatePool;
      }
      /**
       * Get xidPad
       * @return The xidPad
       */
      public Boolean isPadXid()
      {
         return padXid;
      }
      /**
       * Set xidPad
       * @param padXid The value to set
       */
      public void setPadXid(Boolean padXid)
      {
         this.padXid = padXid;
      }
      /**
       * Get wrapXARes
       * @return The wrapXARes
       */
      public Boolean isWrapXaResource()
      {
         return wrapXaResource;
      }
      /**
       * Set wrapXARes
       * @param wrapXaResource The value to set
       */
      public void setWrapXaResource(Boolean wrapXaResource)
      {
         this.wrapXaResource = wrapXaResource;
      }
      
   }

   /**
    * SecurityConfig is used to configure security
    */
   public static class SecurityConfig implements Cloneable
   {
      private Boolean application;
      private String securityDomain;
      private String securityDomainAndApp;
      
      @Override
      public SecurityConfig clone()
      {
         SecurityConfig clone = new SecurityConfig();
         clone.application = this.application;
         clone.securityDomain = this.securityDomain;
         clone.securityDomainAndApp = this.securityDomainAndApp;
         return clone;
      }
      
      /**
       * Get application
       * @return The application
       */
      public Boolean getApplication()
      {
         return application;
      }
      /**
       * Set application
       * @param application The value to set
       */
      public void setApplication(Boolean application)
      {
         this.application = application;
      }
      /**
       * Get securityDomain
       * @return The securityDomain
       */
      public String getSecurityDomain()
      {
         return securityDomain;
      }
      /**
       * Set securityDomain
       * @param securityDomain The value to set
       */
      public void setSecurityDomain(String securityDomain)
      {
         this.securityDomain = securityDomain;
      }
      /**
       * Get securityDomainAndApp
       * @return The securityDomainAndApp
       */
      public String getSecurityDomainAndApp()
      {
         return securityDomainAndApp;
      }
      /**
       * Set securityDomainAndApp
       * @param securityDomainAndApp The value to set
       */
      public void setSecurityDomainAndApp(String securityDomainAndApp)
      {
         this.securityDomainAndApp = securityDomainAndApp;
      }
      
      
   }
   
   /**
    * TimeoutConfig is used to configure time out
    */
   public static class TimeoutConfig implements Cloneable
   {
      private Long blockingTimeoutMillis;
      private Long idleTimeoutMinutes;
      private Integer allocateRetry;
      private Long allocateRetryWait;
      private Integer xaResourceTimeout;
      
      @Override
      public TimeoutConfig clone()
      {
         TimeoutConfig clone = new TimeoutConfig();
         clone.allocateRetry = this.allocateRetry;
         clone.allocateRetryWait = this.allocateRetryWait;
         clone.blockingTimeoutMillis = this.blockingTimeoutMillis;
         clone.idleTimeoutMinutes = this.idleTimeoutMinutes;
         clone.xaResourceTimeout = this.xaResourceTimeout;
         return clone;
      }
      
      /**
       * Get blockingTimeoutMillis
       * @return The blockingTimeoutMillis
       */
      public Long getBlockingTimeoutMillis()
      {
         return blockingTimeoutMillis;
      }
      /**
       * Set blockingTimeoutMillis
       * @param blockingTimeoutMillis The value to set
       */
      public void setBlockingTimeoutMillis(Long blockingTimeoutMillis)
      {
         this.blockingTimeoutMillis = blockingTimeoutMillis;
      }
      /**
       * Get idleTimeoutMinutes
       * @return The idleTimeoutMinutes
       */
      public Long getIdleTimeoutMinutes()
      {
         return idleTimeoutMinutes;
      }
      /**
       * Set idleTimeoutMinutes
       * @param idleTimeoutMinutes The value to set
       */
      public void setIdleTimeoutMinutes(Long idleTimeoutMinutes)
      {
         this.idleTimeoutMinutes = idleTimeoutMinutes;
      }
      /**
       * Get allocateRetry
       * @return The allocateRetry
       */
      public Integer getAllocateRetry()
      {
         return allocateRetry;
      }
      /**
       * Set allocateRetry
       * @param allocateRetry The value to set
       */
      public void setAllocateRetry(Integer allocateRetry)
      {
         this.allocateRetry = allocateRetry;
      }
      /**
       * Get allocateRetryWait
       * @return The allocateRetryWait
       */
      public Long getAllocateRetryWait()
      {
         return allocateRetryWait;
      }
      /**
       * Set allocateRetryWait
       * @param allocateRetryWait The value to set
       */
      public void setAllocateRetryWait(Long allocateRetryWait)
      {
         this.allocateRetryWait = allocateRetryWait;
      }
      /**
       * Get xaResourceTimeout
       * @return The xaResourceTimeout
       */
      public Integer getXaResourceTimeout()
      {
         return xaResourceTimeout;
      }
      /**
       * Set xaResourceTimeout
       * @param xaResourceTimeout The value to set
       */
      public void setXaResourceTimeout(Integer xaResourceTimeout)
      {
         this.xaResourceTimeout = xaResourceTimeout;
      }

   }
   
   /**
    * ValidationConifg is used to configure validation
    */
   public static class ValidationConfig implements Cloneable
   {
      private Boolean backgroundValidation;
      private Long backgroundValidationMillis;
      private Boolean useFastFail;
      
      @Override
      public ValidationConfig clone()
      {
         ValidationConfig clone = new ValidationConfig();
         clone.backgroundValidation = this.backgroundValidation;
         clone.backgroundValidationMillis = this.backgroundValidationMillis;
         clone.useFastFail = this.useFastFail;
         return clone;
      }
      
      /**
       * Get backgroundValidation
       * @return The backgroundValidation
       */
      public Boolean getBackgroundValidation()
      {
         return backgroundValidation;
      }
      /**
       * Set backgroundValidation
       * @param backgroundValidation The value to set
       */
      public void setBackgroundValidation(Boolean backgroundValidation)
      {
         this.backgroundValidation = backgroundValidation;
      }
      /**
       * Get backgroundValidationMillis
       * @return The backgroundValidationMillis
       */
      public Long getBackgroundValidationMillis()
      {
         return backgroundValidationMillis;
      }
      /**
       * Set backgroundValidationMillis
       * @param backgroundValidationMillis The value to set
       */
      public void setBackgroundValidationMillis(Long backgroundValidationMillis)
      {
         this.backgroundValidationMillis = backgroundValidationMillis;
      }
      /**
       * Get useFastFail
       * @return The useFastFail
       */
      public Boolean getUseFastFail()
      {
         return useFastFail;
      }
      /**
       * Set useFastFail
       * @param useFastFail The value to set
       */
      public void setUseFastFail(Boolean useFastFail)
      {
         this.useFastFail = useFastFail;
      }
      
   }
   
   /**
    * RecoveryConfig is used to configure recovery
    */
   public static class RecoveryConfig implements Cloneable
   {
      private Boolean noRecovery;
      
      // credential
      private Credential credential = new Credential();
      
      // extension
      private Extension extension = new Extension();
      
      @Override
      public RecoveryConfig clone()
      {
         RecoveryConfig clone = new RecoveryConfig();
         clone.credential = this.credential.clone();
         clone.extension = this.extension.clone();
         clone.noRecovery = this.noRecovery;
         return clone;
      }
      
      /**
       * @param credential the credential to set
       */
      public void setCredential(Credential credential)
      {
         this.credential = credential;
      }

      /**
       * @param extension the extension to set
       */
      public void setExtension(Extension extension)
      {
         this.extension = extension;
      }



      /**
       * Get noRecovery
       * @return The noRecovery
       */
      public Boolean getNoRecovery()
      {
         return noRecovery;
      }

      /**
       * Set noRecovery
       * @param noRecovery The value to set
       */
      public void setNoRecovery(Boolean noRecovery)
      {
         this.noRecovery = noRecovery;
      }

      /**
       * Get credential
       * @return The credential
       */
      public Credential getCredential()
      {
         return credential;
      }

      /**
       * Get extension
       * @return The extension
       */
      public Extension getExtension()
      {
         return extension;
      }

   }
   
   /**
    * Capacity configuration for pool
    *
    */
   public static class CapacityConfig implements Cloneable
   {
      private Extension incrementer = new Extension();
      private Extension decrementer = new Extension();
      
      @Override
      public CapacityConfig clone()
      {
         CapacityConfig clone = new CapacityConfig();
         clone.decrementer = this.decrementer.clone();
         clone.incrementer = this.incrementer.clone();
         return clone;
      }
      
      /**
       * @param incrementer the incrementer to set
       */
      public void setIncrementer(Extension incrementer)
      {
         this.incrementer = incrementer;
      }

      /**
       * @param decrementer the decrementer to set
       */
      public void setDecrementer(Extension decrementer)
      {
         this.decrementer = decrementer;
      }

      /**
       * @return the incrementer
       */
      public Extension getIncrementer()
      {
         return incrementer;
      }
      /**
       * @return the decrementer
       */
      public Extension getDecrementer()
      {
         return decrementer;
      }
      
   }
   
   /**
    * Credential is used on the recovery
    */
   public static class Credential implements Cloneable
   {
      private String username;
      private String password;
      private String securityDomain;
      
      @Override
      public Credential clone()
      {
         Credential clone = new Credential();
         clone.username = this.username;
         clone.password = this.password;
         clone.securityDomain = this.securityDomain;
         return clone;
      }
      
      /**
       * Get username
       * @return The username
       */
      public String getUsername()
      {
         return username;
      }
      /**
       * Set username
       * @param username The value to set
       */
      public void setUsername(String username)
      {
         this.username = username;
      }
      /**
       * Get password
       * @return The password
       */
      public String getPassword()
      {
         return password;
      }
      /**
       * Set password
       * @param password The value to set
       */
      public void setPassword(String password)
      {
         this.password = password;
      }
      /**
       * Get securityDomain
       * @return The securityDomain
       */
      public String getSecurityDomain()
      {
         return securityDomain;
      }
      /**
       * Set securityDomain
       * @param securityDomain The value to set
       */
      public void setSecurityDomain(String securityDomain)
      {
         this.securityDomain = securityDomain;
      }
      
   }
   
   /**
    * Extension is used on the recovery
    */
   public static class Extension implements Cloneable
   {
      private String className;
      private List<ConfigPropType> configProperties = new ArrayList<ConfigPropType>();
      
      @Override
      public Extension clone()
      {
         Extension clone = new Extension();
         clone.className = this.className;
         clone.configProperties = AdminObjectConfig.cloneConfigPropTypeList(configProperties);
         return clone;
      }
      
      /**
       * Get className
       * @return The className
       */
      public String getClassName()
      {
         return className;
      }
      /**
       * Set className
       * @param className The value to set
       */
      public void setClassName(String className)
      {
         this.className = className;
      }
      /**
       * Get configProperties
       * @return The configProperties
       */
      public List<ConfigPropType> getConfigProperties()
      {
         return configProperties;
      }

      /**
       * @param configProperties the configProperties to set
       */
      public void setConfigProperties(List<ConfigPropType> configProperties)
      {
         this.configProperties = configProperties;
      }
      
   }
   
}
