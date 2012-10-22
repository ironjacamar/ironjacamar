/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
public class ConnectionFactoryConfig
{
   // general
   private String mcfClsName;
   private String mcfJndiName;
   private String mcfPoolName;
   private Boolean mcfEnabled;
   private Boolean mcfUseJavaCtx;
   private Boolean mcfUseCCM;
   private List<ConfigPropType> mcfConfigProps = new ArrayList<ConfigPropType>();
   
   // pool
   private PoolConfig poolConifg;
   
   // security
   private SecurityConfig securityConfig;
   
   // timeout
   private TimeoutConfig timeoutConfig;
   
   // validation
   private ValidationConfig validationConfig;
   
   // recovery
   private RecoveryConfig recoveryConfig;
   
   /**
    * The constructor
    */
   public ConnectionFactoryConfig()
   {
      super();
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
    * Set mcfConfigProps
    * @param mcfConfigProps The value to set
    */
   public void setMcfConfigProps(List<ConfigPropType> mcfConfigProps)
   {
      this.mcfConfigProps = mcfConfigProps;
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
    * Set poolConifg
    * @param poolConifg The value to set
    */
   public void setPoolConifg(PoolConfig poolConifg)
   {
      this.poolConifg = poolConifg;
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
    * Set securityConfig
    * @param securityConfig The value to set
    */
   public void setSecurityConfig(SecurityConfig securityConfig)
   {
      this.securityConfig = securityConfig;
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
    * Set timeoutConfig
    * @param timeoutConfig The value to set
    */
   public void setTimeoutConfig(TimeoutConfig timeoutConfig)
   {
      this.timeoutConfig = timeoutConfig;
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
    * Set validationConfig
    * @param validationConfig The value to set
    */
   public void setValidationConfig(ValidationConfig validationConfig)
   {
      this.validationConfig = validationConfig;
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
    * Set recoveryConfig
    * @param recoveryConfig The value to set
    */
   public void setRecoveryConfig(RecoveryConfig recoveryConfig)
   {
      this.recoveryConfig = recoveryConfig;
   }

   /**
    * PoolConfig is used to configure Pool
    */
   public static class PoolConfig
   {
      private Integer minPoolSize;
      private Integer maxPoolSize;
      private Boolean prefill;
      private Boolean useStrictMin;
      private FlushStrategy flushStrategy;
      
      private Boolean defineXA;
      
      // XA related
      private Boolean overrideIsSameRM;
      private Boolean interleaving;
      private Boolean noTxSeparatePool;
      private Boolean padXid;
      private Boolean wrapXaResource;
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
      public Boolean getDefineXA()
      {
         return defineXA;
      }
      /**
       * Set defineXA
       * @param defineXA The value to set
       */
      public void setDefineXA(Boolean defineXA)
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
   public static class SecurityConfig
   {
      private Boolean application;
      private String securityDomain;
      private String securityDomainAndApp;
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
   public static class TimeoutConfig
   {
      private Long blockingTimeoutMillis;
      private Long idleTimeoutMinutes;
      private Integer allocateRetry;
      private Long allocateRetryWait;
      private Integer xaResourceTimeout;
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
   public static class ValidationConfig
   {
      private Boolean backgroundValidation;
      private Long backgroundValidationMillis;
      private Boolean useFastFail;
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
   public static class RecoveryConfig
   {
      private Boolean noRecovery;
      
      // credential
      private Credential credential;
      
      // extension
      private Extension extension;
      
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
       * Set credential
       * @param credential The value to set
       */
      public void setCredential(Credential credential)
      {
         this.credential = credential;
      }

      /**
       * Get extension
       * @return The extension
       */
      public Extension getExtension()
      {
         return extension;
      }

      /**
       * Set extension
       * @param extension The value to set
       */
      public void setExtension(Extension extension)
      {
         this.extension = extension;
      }

      /**
       * Credential is used on the recovery
       */
      public static class Credential
      {
         private String username;
         private String password;
         private String securityDomain;
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
      public static class Extension
      {
         private String className;
         private List<ConfigPropType> configProperties = new ArrayList<ConfigPropType>();
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
          * Set configProperties
          * @param configProperties The value to set
          */
         public void setConfigProperties(List<ConfigPropType> configProperties)
         {
            this.configProperties = configProperties;
         }
      }
   }
   
}
