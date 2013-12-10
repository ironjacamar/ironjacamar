/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
import org.jboss.jca.common.annotations.Annotations;
import org.jboss.jca.common.api.metadata.common.Capacity;
import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.common.v11.ConnDefPool;
import org.jboss.jca.common.api.metadata.common.v11.ConnDefXaPool;
import org.jboss.jca.common.api.metadata.common.v11.WorkManager;
import org.jboss.jca.common.api.metadata.common.v11.WorkManagerSecurity;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.metadata.MetadataFactory;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.common.metadata.ra.common.ConnectionDefinitionImpl;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScanner;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScannerFactory;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.CapacityConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.Credential;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.Extension;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.PoolConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.RecoveryConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.SecurityConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.TimeoutConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.ValidationConfig;
import org.jboss.jca.eclipse.command.raui.ResourceAdapterConfig.VERSION;
import org.jboss.jca.eclipse.command.raui.ResourceAdapterConfig.WorkManagerConfig;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.fungal.api.util.FileUtil;
import com.github.fungal.api.util.JarFilter;

/**
 * @author Lin Gao <lin.gao@ironjacamar.org>
 *
 */
public class ResourceAdapterHelper
{

   /**
    * Gets all jar files URL array.
    * This is used to make the ClassLoader to parse connector from the RAR file.
    * 
    * @param directory directory which jar files are located.
    * @return URL array
    * @throws Exception the exception
    */
   private URL[] getURLs(File directory) throws Exception
   {
      List<URL> list = new LinkedList<URL>();

      if (directory.exists() && directory.isDirectory())
      {
         // Add directory
         list.add(directory.toURI().toURL());

         // Add the contents of the directory too
         File[] jars = directory.listFiles(new JarFilter());

         if (jars != null)
         {
            for (int j = 0; j < jars.length; j++)
            {
               list.add(jars[j].getCanonicalFile().toURI().toURL());
            }
         }
      }
      return list.toArray(new URL[list.size()]);
   }
   
   /**
    * Parse ConnectorHolder from the RAR file.
    * 
    * @param rarFile the RAR file
    * @throws Exception the exception
    * @return a ResourceAdapterConfig
    */
   public ResourceAdapterConfig parseResourceAdapterConfig(File rarFile) throws Exception
   {
      if (rarFile == null)
      {
         throw new IllegalArgumentException("Rar file must be specified.");
      }
      ResourceAdapterConfig raConfig = new ResourceAdapterConfig();
      String fileName = rarFile.getName();
      FileUtil fileUtil = new FileUtil();
      File root = fileUtil.extract(rarFile, new File(System.getProperty("java.io.tmpdir")));
      Connector cmd = null;
      IronJacamar ijmd = null;
      try
      {
         MetadataFactory metadataFactory = new MetadataFactory();
         cmd = metadataFactory.getStandardMetaData(root);
         ijmd = metadataFactory.getIronJacamarMetaData(root);

         URLClassLoader cl = new URLClassLoader(getURLs(root), Thread.currentThread().getContextClassLoader());
         Annotations annotator = new Annotations();
         AnnotationScanner scanner = AnnotationScannerFactory.getAnnotationScanner();
         AnnotationRepository repository = scanner.scan(cl.getURLs(), cl);

         cmd = annotator.merge(cmd, repository, cl);
         cmd.validate();
         cmd = (new Merger()).mergeConnectorWithCommonIronJacamar(ijmd, cmd);
      }
      finally
      {
         fileUtil.delete(root);
      }
      
      raConfig.setBootstrapContext(getBootStrapContext(ijmd));
      raConfig.setVersion(VERSION.VERSION_1_1); // default to 1.1
      raConfig.setBeanValidationGroups(getBeanValidationGrp(ijmd));
      raConfig.setAdminObjectConfigs(getAdminObjectConfigs(cmd, ijmd));
      raConfig.setArchive(fileName);
      raConfig.setConfigProperties(getConfigProperties(cmd));
      raConfig.setConnectionDefinitions(getConnectionDefinitions(cmd, ijmd));
      raConfig.setId(fileName); // default to the file name
      raConfig.setTransactionSupport(getTransactionSupportEnum(cmd));
      raConfig.setWorkManagerConfig(getWorkManagerConfig(ijmd));
      
      return raConfig;
   }
   
   private WorkManagerConfig getWorkManagerConfig(IronJacamar ijmd)
   {
      WorkManagerConfig workManagerConfig = new WorkManagerConfig();
      if (ijmd instanceof org.jboss.jca.common.api.metadata.ironjacamar.v11.IronJacamar)
      {
         org.jboss.jca.common.api.metadata.ironjacamar.v11.IronJacamar ij11 = 
               (org.jboss.jca.common.api.metadata.ironjacamar.v11.IronJacamar)ijmd;
         WorkManager workManager = ij11.getWorkManager();
         if (workManager != null)
         {
            WorkManagerSecurity workSec = workManager.getSecurity();
            if (workSec != null)
            {
               workManagerConfig.setDefaultPricipal(workSec.getDefaultPrincipal());
               workManagerConfig.setDomain(workSec.getDomain());
               workManagerConfig.setMappingRequired(workSec.isMappingRequired());
               workManagerConfig.setDefaultGroups(new ArrayList<>(workSec.getDefaultGroups()));
               workManagerConfig.setGroupMap(new HashMap<String, String>(workSec.getGroupMappings()));
               workManagerConfig.setUserMap(new HashMap<String, String>(workSec.getUserMappings()));
            }
         }
      }
      return workManagerConfig;
   }

   private List<ConnectionFactoryConfig> getConnectionDefinitions(Connector cmd, IronJacamar ijmd)
   {
      List<ConnectionFactoryConfig> connConfigs = new ArrayList<ConnectionFactoryConfig>();
      List<ConnectionDefinition> mcfs = null;
      ResourceAdapter ra = cmd.getResourceadapter();
      if (ra instanceof ResourceAdapter1516)
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) ra;
         if (null != ra1516.getOutboundResourceadapter())
         {
            mcfs = ra1516.getOutboundResourceadapter().getConnectionDefinitions();
         }
      }
      else if (ra instanceof ResourceAdapter10)
      {
         ResourceAdapter10 ra10 = (ResourceAdapter10) ra;
         mcfs = new ArrayList<ConnectionDefinition>();
         mcfs.add(new ConnectionDefinitionImpl(ra10.getManagedConnectionFactoryClass(), ra10.getConfigProperties(),
               ra10.getConnectionFactoryInterface(), ra10.getConnectionFactoryImplClass(), ra10
                     .getConnectionInterface(), ra10.getConnectionImplClass(), ra10.getId()));
      }
      if (mcfs != null)
      {
         for (ConnectionDefinition connDef: mcfs)
         {
            ConnectionFactoryConfig connConfig = new ConnectionFactoryConfig();
            CommonConnDef commonConnDef = getCommonConnDef(ijmd, connDef);
            if (commonConnDef != null)
            {
               connConfig.setActive(true);
               if (commonConnDef instanceof org.jboss.jca.common.api.metadata.common.v11.CommonConnDef)
               {
                  org.jboss.jca.common.api.metadata.common.v11.CommonConnDef connDef11 = 
                        (org.jboss.jca.common.api.metadata.common.v11.CommonConnDef)commonConnDef;
                  connConfig.setEnlistment(connDef11.isEnlistment());
                  connConfig.setSharable(connDef11.isSharable());
               }
               
               connConfig.setMcfClsName(commonConnDef.getClassName());
               connConfig.setMcfConfigProps(getConfigPropTypes(connDef.getConfigProperties(), 
                     commonConnDef.getConfigProperties()));
               connConfig.setMcfEnabled(commonConnDef.isEnabled());
               connConfig.setMcfJndiName(commonConnDef.getJndiName());
               connConfig.setMcfPoolName(commonConnDef.getPoolName());
               connConfig.setMcfUseCCM(commonConnDef.isUseCcm());
               connConfig.setMcfUseJavaCtx(commonConnDef.isUseJavaContext());
               connConfig.setPoolConifg(getPoolConfig(connDef, commonConnDef));
               connConfig.setRecoveryConfig(getRecoveryConfig(connDef, commonConnDef));
               connConfig.setSecurityConfig(getSecurityConfig(connDef, commonConnDef));
               connConfig.setTimeoutConfig(getTimeoutConfig(connDef, commonConnDef));
               connConfig.setValidationConfig(getValidationConfig(connDef, commonConnDef));
            }
            connConfigs.add(connConfig);
         }
      }
      
      return connConfigs;
   }
   
   private ValidationConfig getValidationConfig(ConnectionDefinition connDef, CommonConnDef commonConnDef)
   {
      ValidationConfig validationConfig = new ValidationConfig();
      CommonValidation validation = commonConnDef.getValidation();
      if (validation != null)
      {
         validationConfig.setBackgroundValidation(validation.isBackgroundValidation());
         validationConfig.setBackgroundValidationMillis(validation.getBackgroundValidationMillis());
         validationConfig.setUseFastFail(validation.isUseFastFail());
      }
      return validationConfig;
   }

   private TimeoutConfig getTimeoutConfig(ConnectionDefinition connDef, CommonConnDef commonConnDef)
   {
      TimeoutConfig timeOutConfig = new TimeoutConfig();
      CommonTimeOut timeout = commonConnDef.getTimeOut();
      if (timeout != null)
      {
         timeOutConfig.setAllocateRetry(timeout.getAllocationRetry());
         timeOutConfig.setAllocateRetryWait(timeout.getAllocationRetryWaitMillis());
         timeOutConfig.setBlockingTimeoutMillis(timeout.getBlockingTimeoutMillis());
         timeOutConfig.setIdleTimeoutMinutes(timeout.getIdleTimeoutMinutes());
         timeOutConfig.setXaResourceTimeout(timeout.getXaResourceTimeout());
      }
      return timeOutConfig;
   }

   private SecurityConfig getSecurityConfig(ConnectionDefinition connDef, CommonConnDef commonConnDef)
   {
      SecurityConfig secConfig = new SecurityConfig();
      CommonSecurity security = commonConnDef.getSecurity();
      if (security != null)
      {
         secConfig.setApplication(secConfig.getApplication());
         secConfig.setSecurityDomain(security.getSecurityDomain());
         secConfig.setSecurityDomainAndApp(security.getSecurityDomainAndApplication());
      }
      return secConfig;
   }

   private RecoveryConfig getRecoveryConfig(ConnectionDefinition connDef, CommonConnDef commonConnDef)
   {
      RecoveryConfig recoveryConfig = new RecoveryConfig();
      Recovery recovery = commonConnDef.getRecovery();
      if (recovery != null)
      {
         recoveryConfig.setNoRecovery(recovery.getNoRecovery());
         recoveryConfig.setExtension(getExtensionConfig(recovery.getRecoverPlugin()));
         recoveryConfig.setCredential(getCredentialConfig(recovery.getCredential()));
      }
      return recoveryConfig;
   }
   

   private Credential getCredentialConfig(org.jboss.jca.common.api.metadata.common.Credential credential)
   {
      Credential credentialConfig = new Credential();
      if (credential != null)
      {
         credentialConfig.setPassword(credential.getPassword());
         credentialConfig.setSecurityDomain(credential.getSecurityDomain());
         credentialConfig.setUsername(credential.getUserName());
      }
      return credentialConfig;
   }

   private Extension getExtensionConfig(org.jboss.jca.common.api.metadata.common.Extension recoverPlugin)
   {
      Extension extConfig = new Extension();
      if (recoverPlugin != null)
      {
         extConfig.setClassName(recoverPlugin.getClassName());
         extConfig.setConfigProperties(getStringConfigPropTypes(recoverPlugin.getConfigPropertiesMap()));
      }
      return extConfig;
   }

   private PoolConfig getPoolConfig(ConnectionDefinition connDef, CommonConnDef commonConnDef)
   {
      PoolConfig poolConfig = new PoolConfig();
      CommonPool commonPool = commonConnDef.getPool();
      if (commonPool != null)
      {
         poolConfig.setFlushStrategy(commonPool.getFlushStrategy());
         poolConfig.setMinPoolSize(commonPool.getMinPoolSize());
         poolConfig.setMaxPoolSize(commonPool.getMaxPoolSize());
         poolConfig.setPrefill(commonPool.isPrefill());
         poolConfig.setUseStrictMin(commonPool.isUseStrictMin());
         if (commonPool instanceof CommonXaPool)
         {
            CommonXaPool xaPool = (CommonXaPool)commonPool;
            poolConfig.setDefineXA(true);
            poolConfig.setInterleaving(xaPool.isInterleaving());
            poolConfig.setNoTxSeparatePool(xaPool.isNoTxSeparatePool());
            poolConfig.setOverrideIsSameRM(xaPool.isSameRmOverride());
            poolConfig.setPadXid(xaPool.isPadXid());
            poolConfig.setWrapXaResource(xaPool.isWrapXaResource());
         }
         if (commonPool instanceof ConnDefPool)
         {
            ConnDefPool connDefPool = (ConnDefPool)commonPool;
            poolConfig.setInitialPoolSize(connDefPool.getInitialPoolSize());
            poolConfig.setCapacityConfig(getCapacityConfig(connDefPool.getCapacity()));
         }
         if (commonPool instanceof ConnDefXaPool)
         {
            ConnDefXaPool connDefPool = (ConnDefXaPool)commonPool;
            poolConfig.setInitialPoolSize(connDefPool.getInitialPoolSize());
            poolConfig.setCapacityConfig(getCapacityConfig(connDefPool.getCapacity()));
         }
      }
      return poolConfig;
   }

   private CapacityConfig getCapacityConfig(Capacity capacity)
   {
      CapacityConfig capacityConfig = new CapacityConfig();
      if (capacity != null)
      {
         capacityConfig.setDecrementer(getExtensionConfig(capacity.getDecrementer()));
         capacityConfig.setIncrementer(getExtensionConfig(capacity.getIncrementer()));
      }
      return capacityConfig;
   }

   private CommonConnDef getCommonConnDef(IronJacamar ijmd, ConnectionDefinition connDef)
   {
      if (null != ijmd)
      {
         for (CommonConnDef commonConnDef : ijmd.getConnectionDefinitions())
         {
            if (connDef.getManagedConnectionFactoryClass().getValue().equals(commonConnDef.getClassName()))
            {
               return commonConnDef;
            }
         }
      }
      return null;
   }

   private List<AdminObjectConfig> getAdminObjectConfigs(Connector cmd, IronJacamar ijmd)
   {
      List<AdminObjectConfig> aoConfigs = new ArrayList<AdminObjectConfig>();
      ResourceAdapter ra = cmd.getResourceadapter();
      if (ra instanceof ResourceAdapter1516)
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) ra;
         for (AdminObject ao: ra1516.getAdminObjects())
         {
            AdminObjectConfig aoConfig = new AdminObjectConfig();
            aoConfig.setClssName(ao.getAdminobjectClass().getValue());
            CommonAdminObject commonAO = getCommonAdminObject(ao, ijmd);
            if (commonAO != null)
            {
               aoConfig.setActive(true);
               aoConfig.setEnabled(commonAO.isEnabled());
               aoConfig.setJndiName(commonAO.getJndiName());
               aoConfig.setPoolName(commonAO.getPoolName());
               aoConfig.setUseJavaCtx(commonAO.isUseJavaContext());
            }
            aoConfigs.add(aoConfig);
         }
      }
      return aoConfigs;
   }
   
   private CommonAdminObject getCommonAdminObject(AdminObject ao, IronJacamar ijmd)
   {
      if (ijmd != null)
      {
         for (CommonAdminObject commonAO : ijmd.getAdminObjects())
         {
            if (commonAO.getClassName().equals(ao.getAdminobjectClass().getValue()))
            {
               return commonAO;
            }
         }
      }
      return null;
   }

   private List<ConfigPropType> getConfigProperties(Connector cmd)
   {
      List<ConfigPropType> result = new ArrayList<ConfigPropType>();
      List<? extends ConfigProperty> configProperties = null;
      ResourceAdapter ra = cmd.getResourceadapter();
      if (ra instanceof ResourceAdapter1516)
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) ra;
         configProperties = ra1516.getConfigProperties();
      }
      else if (ra instanceof ResourceAdapter10)
      {
         ResourceAdapter10 ra10 = (ResourceAdapter10) ra;
         configProperties = ra10.getConfigProperties();
      }
      if (configProperties != null)
      {
         for (ConfigProperty configProp : configProperties)
         {
            String name = configProp.getConfigPropertyName().getValue();
            String type = configProp.getConfigPropertyType().getValue();
            String value = configProp.getConfigPropertyValue().getValue();
            ConfigPropType configPropType = new ConfigPropType(name, type, value, configProp.isMandatory());
            result.add(configPropType);
         }
      }
      return result;
   }
   
   /**
    * Gets ConfigPropType list for string type.
    * @param map the map
    * @return the ConfigPropType list
    */
   private List<ConfigPropType> getStringConfigPropTypes(Map<String, String> map)
   {
      List<ConfigPropType> result = new ArrayList<>();
      if (map != null)
      {
         for (Map.Entry<String, String> entry: map.entrySet())
         {
            ConfigPropType type = new ConfigPropType();
            type.setName(entry.getKey());
            type.setValue(entry.getValue());
            type.setType(String.class.getName());
            result.add(type);
         }
      }
      return result;
   }

   private TransactionSupportEnum getTransactionSupportEnum(Connector cmd)
   {
      ResourceAdapter ra = cmd.getResourceadapter();
      if (ra instanceof ResourceAdapter1516)
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) ra;
         return ra1516.getOutboundResourceadapter() == null ? TransactionSupportEnum.NoTransaction : ra1516
               .getOutboundResourceadapter().getTransactionSupport();
      }
      else if (ra instanceof ResourceAdapter10)
      {
         ResourceAdapter10 ra10 = (ResourceAdapter10) ra;
         return ra10.getTransactionSupport();
      }
      return null;
   }

   private List<String> getBeanValidationGrp(IronJacamar ijmd)
   {
      if (ijmd != null)
      {
         return ijmd.getBeanValidationGroups();
      }
      return null;
   }

   private String getBootStrapContext(IronJacamar ijmd)
   {
      if (ijmd != null)
      {
         return ijmd.getBootstrapContext();
      }
      return null;
   }
   
   /**
    * Gets List<ConfigPropType> from the List<? extends ConfigProperty>.
    * 
    * @param configProperties the List<? extends ConfigProperty>
    * @param valueMap the map which contains the value specified
    * @return the ConfigPropType list
    */
   public static List<ConfigPropType> getConfigPropTypes(List<? extends ConfigProperty> configProperties, 
         Map<String, String> valueMap)
   {
      List<ConfigPropType> result = new ArrayList<ConfigPropType>();
      if (configProperties != null)
      {
         for (ConfigProperty configProp : configProperties)
         {
            String name = configProp.getConfigPropertyName().getValue();
            String type = configProp.getConfigPropertyType().getValue();
            String value = configProp.getConfigPropertyValue().getValue();
            if (valueMap != null)
            {
               String valueInMap = valueMap.get(name);
               if (valueInMap != null)
               {
                  value = valueInMap;
               }
            }
            ConfigPropType configPropType = new ConfigPropType(name, type, value, configProp.isMandatory());
            result.add(configPropType);
         }
      }
      return result;
   }
}
