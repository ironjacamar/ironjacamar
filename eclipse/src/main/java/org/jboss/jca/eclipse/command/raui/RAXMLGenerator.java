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
import org.jboss.jca.common.api.metadata.common.Capacity;
import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.common.v10.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.v11.WorkManager;
import org.jboss.jca.common.api.metadata.common.v11.WorkManagerSecurity;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapters;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.common.CommonAdminObjectImpl;
import org.jboss.jca.common.metadata.common.CommonPoolImpl;
import org.jboss.jca.common.metadata.common.CommonSecurityImpl;
import org.jboss.jca.common.metadata.common.CommonTimeOutImpl;
import org.jboss.jca.common.metadata.common.CommonValidationImpl;
import org.jboss.jca.common.metadata.common.CommonXaPoolImpl;
import org.jboss.jca.common.metadata.common.CredentialImpl;
import org.jboss.jca.common.metadata.common.v10.CommonConnDefImpl;
import org.jboss.jca.common.metadata.common.v11.ConnDefPoolImpl;
import org.jboss.jca.common.metadata.common.v11.ConnDefXaPoolImpl;
import org.jboss.jca.common.metadata.common.v11.WorkManagerImpl;
import org.jboss.jca.common.metadata.common.v11.WorkManagerSecurityImpl;
import org.jboss.jca.common.metadata.resourceadapter.ResourceAdaptersImpl;
import org.jboss.jca.common.metadata.resourceadapter.v10.ResourceAdapterImpl;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.CapacityConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.PoolConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.RecoveryConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.SecurityConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.TimeoutConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.ValidationConfig;
import org.jboss.jca.eclipse.command.raui.ResourceAdapterConfig.VERSION;
import org.jboss.jca.eclipse.command.raui.ResourceAdapterConfig.WorkManagerConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * RAXMLGenerator is used to generate the *-ra.xml file.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class RAXMLGenerator
{
   /**
    * The constructor.
    */
   public RAXMLGenerator()
   {
      super();
   }
   
   /**
    * Generates the *-ra.xml file.
    * 
    * @param raConfig the ResourceAdapterConfig
    * @param initialConfig the initial ResourceAdapterConfig
    * @param outputFile the output file
    * @throws Exception any Exception
    */
   public void generateRAXML(ResourceAdapterConfig initialConfig, ResourceAdapterConfig raConfig, File outputFile)
      throws Exception
   {
      if (raConfig == null)
      {
         throw new IllegalArgumentException("ResourceAdapterConfig can not be null.");
      }
      if (outputFile == null || outputFile.isDirectory())
      {
         throw new IllegalArgumentException("OutputFile can not be null, and it must not be a directory.");
      }
      List<ResourceAdapter> resourceAdapters = new ArrayList<ResourceAdapter>();
      
      if (raConfig.getVersion().equals(ResourceAdapterConfig.VERSION.VERSION_1_0))
      {
         // resource adapter 1.0
         ResourceAdapter ra10Impl = getResourceAdapter10(raConfig);
         resourceAdapters.add(ra10Impl);
      }
      else if (raConfig.getVersion().equals(ResourceAdapterConfig.VERSION.VERSION_1_1))
      {
         ResourceAdapter ra11Impl = getResourceAdapter11(raConfig);
         resourceAdapters.add(ra11Impl);
      }
      else
      {
         throw new IllegalStateException("Unkown Version: " + raConfig.getVersion());
      }
      
      ResourceAdapters ras = new ResourceAdaptersImpl(resourceAdapters);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new InputSource(new StringReader(ras.toString())));

      TransformerFactory tfactory = TransformerFactory.newInstance();
      Transformer serializer = tfactory.newTransformer();
      //Setup indenting to "pretty print"
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
      serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      serializer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(outputFile)));
   }

   private ResourceAdapter getResourceAdapter10(ResourceAdapterConfig raConfig)
   {
      String archive = raConfig.getArchive();
      TransactionSupportEnum transactionSupport = raConfig.getTransactionSupport();
      List<CommonConnDef> connectionDefinitions10 = getRaCommonConnDef10(raConfig.getConnectionDefinitions());
      List<CommonAdminObject> adminObjects = getAdminObjects(raConfig.getAdminObjectConfigs());
      
      ResourceAdapterImpl ra10Impl = new ResourceAdapterImpl(archive, transactionSupport, 
            connectionDefinitions10, adminObjects, getConfigProperties(raConfig.getConfigProperties()), 
            raConfig.getBeanValidationGroups(), raConfig.getBootstrapContext());
      
      return ra10Impl;
   }
   
   private ResourceAdapter getResourceAdapter11(ResourceAdapterConfig raConfig)
   {
      String archive = raConfig.getArchive();
      String id = raConfig.getId();
      TransactionSupportEnum transactionSupport = raConfig.getTransactionSupport();
      List<org.jboss.jca.common.api.metadata.common.v11.CommonConnDef> connectionDefinitions11 = 
            getRaCommonConnDef11(raConfig.getConnectionDefinitions());
      List<CommonAdminObject> adminObjects = getAdminObjects(raConfig.getAdminObjectConfigs());
      WorkManager workManager = getWorkManager(raConfig);
      org.jboss.jca.common.metadata.resourceadapter.v11.ResourceAdapterImpl ra11Impl = 
            new org.jboss.jca.common.metadata.resourceadapter.v11.ResourceAdapterImpl(id, archive, 
                  transactionSupport, connectionDefinitions11, adminObjects, 
                  getConfigProperties(raConfig.getConfigProperties()), raConfig.getBeanValidationGroups(), 
                  raConfig.getBootstrapContext(), workManager);
      return ra11Impl;
   }

   private List<org.jboss.jca.common.api.metadata.common.v11.CommonConnDef> getRaCommonConnDef11(
         List<ConnectionFactoryConfig> connectionDefinitions)
   {
      List<org.jboss.jca.common.api.metadata.common.v11.CommonConnDef> result = 
            new ArrayList<org.jboss.jca.common.api.metadata.common.v11.CommonConnDef>();
      for (ConnectionFactoryConfig connConfig : connectionDefinitions)
      {
         if (!connConfig.isActive())
         {
            continue;
         }
         Map<String, String> configProperties = getConfigProperties(connConfig.getMcfConfigProps());
         String className = connConfig.getMcfClsName();
         String jndiName = connConfig.getMcfJndiName();
         String poolName = connConfig.getMcfPoolName();
         Boolean enabled = connConfig.getMcfEnabled();
         Boolean useJavaContext = connConfig.getMcfUseJavaCtx();
         Boolean useCcm = connConfig.getMcfUseCCM();
         Boolean sharable = connConfig.getSharable();
         Boolean enlistment = connConfig.getEnlistment();
         CommonPool pool = getCommonPool(connConfig.getPoolConifg(), VERSION.VERSION_1_1);
         boolean isXA = connConfig.getPoolConifg().getDefineXA();
         
         CommonTimeOut timeOut = getCommonTimeOut(connConfig.getTimeoutConfig());
         CommonValidation validation = getCommonValidation(connConfig.getValidationConfig());
         CommonSecurity security = getCommonSecurity(connConfig.getSecurityConfig());
         Recovery recovery = getRecovery(connConfig.getRecoveryConfig());
         if (className != null || jndiName != null || poolName != null || enabled != null || useJavaContext != null 
               || useCcm != null || pool != null || timeOut != null || validation != null || security != null 
                     || recovery != null)
         {
            org.jboss.jca.common.api.metadata.common.v11.CommonConnDef commonConn = 
                  new org.jboss.jca.common.metadata.common.v11.CommonConnDefImpl(configProperties, className, jndiName,
                        poolName, enabled, useJavaContext, useCcm, sharable, enlistment, pool, timeOut, validation, 
                        security, recovery, isXA);
            result.add(commonConn);
         }
      }
      return result.isEmpty() ? null : result;
   }

   private WorkManager getWorkManager(ResourceAdapterConfig raConfig)
   {
      WorkManagerConfig workManagerConfig = raConfig.getWorkManagerConfig();
      Boolean mappingRequired = workManagerConfig.isMappingRequired();
      String domain = workManagerConfig.getDomain();
      String defaultPrincipal = workManagerConfig.getDefaultPricipal();
      List<String> defaultGroups = workManagerConfig.getDefaultGroups();
      Map<String, String> userMappings = workManagerConfig.getUserMap();
      Map<String, String> groupMappings = workManagerConfig.getGroupMap();
      WorkManagerSecurity security = new WorkManagerSecurityImpl(mappingRequired, domain, defaultPrincipal, 
            defaultGroups, userMappings, groupMappings);
      WorkManagerImpl workManager = new WorkManagerImpl(security);
      return workManager;
   }

   private List<CommonConnDef> getRaCommonConnDef10(List<ConnectionFactoryConfig> connectionFactoryConfigs)
   {
      List<CommonConnDef> result = new ArrayList<CommonConnDef>();
      for (ConnectionFactoryConfig connConfig : connectionFactoryConfigs)
      {
         if (!connConfig.isActive())
         {
            continue;
         }
         Map<String, String> configProperties = getConfigProperties(connConfig.getMcfConfigProps());
         String className = connConfig.getMcfClsName();
         String jndiName = connConfig.getMcfJndiName();
         String poolName = connConfig.getMcfPoolName();
         Boolean enabled = connConfig.getMcfEnabled();
         Boolean useJavaContext = connConfig.getMcfUseJavaCtx();
         Boolean useCcm = connConfig.getMcfUseCCM();
         CommonPool pool = getCommonPool(connConfig.getPoolConifg(), VERSION.VERSION_1_0);
         boolean isXA = connConfig.getPoolConifg().getDefineXA();
         CommonTimeOut timeOut = getCommonTimeOut(connConfig.getTimeoutConfig());
         CommonValidation validation = getCommonValidation(connConfig.getValidationConfig());
         CommonSecurity security = getCommonSecurity(connConfig.getSecurityConfig());
         Recovery recovery = getRecovery(connConfig.getRecoveryConfig());
         if (className != null || jndiName != null || poolName != null || enabled != null || useJavaContext != null 
               || useCcm != null || pool != null || timeOut != null || validation != null || security != null 
                     || recovery != null)
         {
            CommonConnDefImpl commonConn = new CommonConnDefImpl(configProperties, className, jndiName, poolName, 
               enabled, useJavaContext, useCcm, pool, timeOut, validation, security, recovery, isXA);
            result.add(commonConn);
         }
      }
      return result.isEmpty() ? null : result;
   }

   private Recovery getRecovery(RecoveryConfig recoveryConfig)
   {
      if (recoveryConfig == null)
      {
         return null;
      }
      if (recoveryConfig.getNoRecovery() == null)
      {
         ConnectionFactoryConfig.Credential credential = recoveryConfig.getCredential();
         ConnectionFactoryConfig.Extension extension = recoveryConfig.getExtension();
         if (credential == null && extension == null)
         {
            return null;
         }
         else if (credential != null && extension == null)
         {
            if (credential.getUsername() == null && credential.getPassword() == null 
                  && credential.getSecurityDomain() == null)
            {
               return null;
            }
         }
         else if (credential == null && extension != null)
         {
            if (extension.getClassName() == null && extension.getConfigProperties().isEmpty())
            {
               return null;
            }
         }
         else if (credential != null && extension != null)
         {
            if (credential.getUsername() == null && credential.getPassword() == null 
                  && credential.getSecurityDomain() == null && extension.getClassName() == null 
                  && extension.getConfigProperties().isEmpty())
            {
               return null;
            }
         }
      }
      Boolean noRecovery = recoveryConfig.getNoRecovery();
      ConnectionFactoryConfig.Credential credentialConfig = recoveryConfig.getCredential();
      String userName = null;
      String password = null;
      String securityDomain = null;
      if (credentialConfig != null)
      {
         userName = credentialConfig.getUsername();
         password = credentialConfig.getPassword();
         securityDomain = credentialConfig.getSecurityDomain();
      }
      try
      {
         Credential credential = null;
         // userName and securityDomain can not be not-null together.
         if (credentialConfig != null)
         {
            credential = new CredentialImpl(userName, password, securityDomain);
         }
         String className = null;
         Map<String, String> configPropertiesMap = null;
         ConnectionFactoryConfig.Extension extensionConfig = recoveryConfig.getExtension();
         if (extensionConfig != null)
         {
            className = extensionConfig.getClassName();
            configPropertiesMap = getConfigProperties(extensionConfig.getConfigProperties());
         }
         Extension extension = null;
         
         // className is required.
         if (className != null)
         {
            extension = new Extension(className, configPropertiesMap);
         }
         return new Recovery(credential, extension, noRecovery);
      }
      catch (ValidateException e)
      {
         throw new RuntimeException("Can't create Recovery.", e);
      }
   }

   private CommonSecurity getCommonSecurity(SecurityConfig securityConfig)
   {
      if (securityConfig == null)
      {
         return null;
      }
      if ((securityConfig.getApplication() == null) && (securityConfig.getSecurityDomain() == null) 
            && (securityConfig.getSecurityDomainAndApp() == null))
      {
         return null;
      }
      String securityDomainManaged = securityConfig.getSecurityDomain();
      String securityDomainAndApplicationManaged = securityConfig.getSecurityDomainAndApp();
      Boolean applicationManaged = securityConfig.getApplication();
      if (applicationManaged == null)
      {
         applicationManaged = Boolean.valueOf(false);
      }
      try
      {
         return new CommonSecurityImpl(securityDomainManaged, securityDomainAndApplicationManaged, applicationManaged);
      }
      catch (ValidateException e)
      {
         throw new RuntimeException("Can't create CommonSecurity.", e);
      }
   }

   private CommonValidation getCommonValidation(ValidationConfig validationConfig)
   {
      if (validationConfig == null)
      {
         return null;
      }
      if (validationConfig.getBackgroundValidation() == null 
            && validationConfig.getBackgroundValidationMillis() == null && validationConfig.getUseFastFail() == null)
      {
         return null;
      }
      Boolean backgroundValidation = validationConfig.getBackgroundValidation();
      Long backgroundValidationMillis = validationConfig.getBackgroundValidationMillis();
      Boolean useFastFail = validationConfig.getUseFastFail();
      try
      {
         return new CommonValidationImpl(backgroundValidation, backgroundValidationMillis, useFastFail);
      }
      catch (ValidateException e)
      {
         throw new RuntimeException("Can't create Validation.", e);
      }
   }

   private CommonTimeOut getCommonTimeOut(TimeoutConfig timeoutConfig)
   {
      if (timeoutConfig == null)
      {
         return null;
      }
      if (timeoutConfig.getAllocateRetry() == null && timeoutConfig.getAllocateRetryWait() == null 
            && timeoutConfig.getBlockingTimeoutMillis() == null && timeoutConfig.getIdleTimeoutMinutes() == null 
            && timeoutConfig.getXaResourceTimeout() == null)
      {
         return null;
      }
      Long blockingTimeoutMillis = timeoutConfig.getBlockingTimeoutMillis();
      Long idleTimeoutMinutes = timeoutConfig.getIdleTimeoutMinutes();
      Integer allocationRetry = timeoutConfig.getAllocateRetry();
      Long allocationRetryWaitMillis = timeoutConfig.getAllocateRetryWait();
      Integer xaResourceTimeout = timeoutConfig.getXaResourceTimeout();
      try
      {
         CommonTimeOut timeout = new CommonTimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, 
               allocationRetry, allocationRetryWaitMillis, xaResourceTimeout);
         return timeout;
      }
      catch (ValidateException e)
      {
         throw new RuntimeException("Can't create Timeout.", e);
      }
   }

   private CommonPool getCommonPool(PoolConfig poolConfig, VERSION version)
   {
      if (poolConfig == null)
      {
         return null;
      }
      if (poolConfig.isInterleaving() == null && poolConfig.isNoTxSeparatePool() == null 
            && poolConfig.isOverrideIsSameRM() == null && poolConfig.isPadXid() == null 
            && poolConfig.isPrefill() == null && poolConfig.isUseStrictMin() == null 
            && poolConfig.getFlushStrategy() == null && poolConfig.getMaxPoolSize() == null 
            && poolConfig.getMinPoolSize() == null && poolConfig.getInitialPoolSize() == null 
            && poolConfig.getCapacityConfig().getIncrementer().getClassName() == null 
            && poolConfig.getCapacityConfig().getDecrementer().getClassName() == null)
      {
         return null;
      }
      CommonPool pool = null;
      Integer minPoolSize = poolConfig.getMinPoolSize();
      Integer maxPoolSize = poolConfig.getMaxPoolSize();
      Integer initialPoolSize = poolConfig.getInitialPoolSize();
      Boolean prefill = poolConfig.isPrefill();
      Boolean useStrictMin = poolConfig.isUseStrictMin();
      FlushStrategy flushStrategy = poolConfig.getFlushStrategy();
      CapacityConfig capacityConfig = poolConfig.getCapacityConfig();
      try
      {
         Capacity capacity = null;
         if (VERSION.VERSION_1_1.equals(version))
         {
            String increMenterClassName = capacityConfig.getIncrementer().getClassName();
            Map<String, String> increMenterConfigPropsMap = 
                  getConfigProperties(capacityConfig.getIncrementer().getConfigProperties());
            Extension incrementer = null;
            if (increMenterClassName != null && increMenterClassName.length() > 0)
            {
               incrementer = new Extension(increMenterClassName, increMenterConfigPropsMap);
            }
            
            String decreMenterClassName = capacityConfig.getDecrementer().getClassName();
            Map<String, String> decreMenterConfigPropsMap = 
                  getConfigProperties(capacityConfig.getDecrementer().getConfigProperties());
            Extension decrementer = null;
            if (decreMenterClassName != null && decreMenterClassName.length() > 0)
            {
               decrementer = new Extension(decreMenterClassName, decreMenterConfigPropsMap);
            }
            if (incrementer != null || decrementer != null)
            {
               capacity = new Capacity(incrementer, decrementer);
            }
         }
         if (poolConfig.getDefineXA())
         {
            // xa
            Boolean isSameRmOverride = poolConfig.isOverrideIsSameRM();
            Boolean interleaving = poolConfig.isInterleaving();
            Boolean padXid = poolConfig.isPadXid();
            Boolean wrapXaResource = poolConfig.isWrapXaResource();
            Boolean noTxSeparatePool = poolConfig.isNoTxSeparatePool();
            if (VERSION.VERSION_1_0.equals(version))
            {
               pool = new CommonXaPoolImpl(minPoolSize, maxPoolSize, prefill, useStrictMin, 
                     flushStrategy, isSameRmOverride, interleaving, padXid, wrapXaResource, noTxSeparatePool);
            }
            else if (VERSION.VERSION_1_1.equals(version))
            {
               pool = new ConnDefXaPoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin, 
                     flushStrategy, capacity, isSameRmOverride, interleaving, padXid, wrapXaResource, 
                     noTxSeparatePool);
            }
         }
         else
         {
            if (VERSION.VERSION_1_0.equals(version))
            {
               pool = new CommonPoolImpl(minPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy);
            }
            else if (VERSION.VERSION_1_1.equals(version))
            {
               pool = new ConnDefPoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin, 
                     flushStrategy, capacity);
            }
         }
      }
      catch (ValidateException e)
      {
         throw new RuntimeException("Can't create CommonPool.", e);
      }
      return pool;
   }

   private List<CommonAdminObject> getAdminObjects(List<AdminObjectConfig> adminObjectConfigs)
   {
      List<CommonAdminObject> result = new ArrayList<CommonAdminObject>();
      for (AdminObjectConfig config : adminObjectConfigs)
      {
         if (config.isActive())
         {
            Map<String, String> configProps = getConfigProperties(config.getConfigProps());
            CommonAdminObject commonAO = new CommonAdminObjectImpl(configProps, config.getClssName(),  
                  config.getJndiName(), config.getPoolName(), config.isEnabled(), config.isUseJavaCtx());
            result.add(commonAO);
         }
      }
      return result;
   }
   
   /**
    * Gets the ConfigPropery maps.
    * 
    * @param configPropTypes the ConfigPropType list
    * @return the ConfigProperty map
    */
   public Map<String, String> getConfigProperties(List<ConfigPropType> configPropTypes)
   {
      Map<String, String> map = new HashMap<String, String>();
      if (configPropTypes != null)
      {
         for (ConfigPropType configPropType : configPropTypes)
         {
            map.put(configPropType.getName(), configPropType.getValue());
         }
      }
      return map;
   }

}
