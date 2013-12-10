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
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ResourceAdapterConfig.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class ResourceAdapterConfig implements Cloneable
{

   /**
    * The Version enum
    *
    */
   public enum VERSION
   {
      /** Version 1.0 */
      VERSION_1_0("1.0"),
      
      /** Version 1.1 */
      VERSION_1_1("1.1");
      
      private String version;
      
      /**
       * Constructor with a version string
       * @param version the version string
       */
      VERSION(String version)
      {
         this.version = version;
      }
      
      /**
       * Gets the version string
       * @return the version string
       */
      public String getVersion()
      {
         return this.version;
      }
      
      
      /**
       * Gets VERSION using the version string
       * @param version the version string
       * @return the VERSION_1_0 or VERSION_1_1
       */
      public static VERSION forName(String version)
      {
         if ("1.0".equals(version))
         {
            return VERSION_1_0;
         }
         else if ("1.1".equals(version))
         {
            return VERSION_1_1;
         }
         throw new IllegalArgumentException("Unkown version: " + version);
      }
      
   }
   
   private VERSION version = VERSION.VERSION_1_1;
   
   private List<VersionChangeListener> versionListeners = new ArrayList<VersionChangeListener>();
   
   private String archive;
   private String id;
   private TransactionSupportEnum transactionSupport;
   private List<ConfigPropType> configProperties = new ArrayList<ConfigPropType>();
   private List<String> beanValidationGroups = new ArrayList<String>();
   private String bootstrapContext = null;
   
   private List<AdminObjectConfig> adminObjectConfigs = new ArrayList<AdminObjectConfig>();
   
   private List<ConnectionFactoryConfig> connectionDefinitions = new ArrayList<ConnectionFactoryConfig>();
   
   private WorkManagerConfig workManagerConfig = new WorkManagerConfig();
   
   /**
    * The constructor.
    */
   public ResourceAdapterConfig()
   {
      super();
   }
   
   @Override
   protected ResourceAdapterConfig clone()
   {
      ResourceAdapterConfig clone = new ResourceAdapterConfig();
      clone.setArchive(this.archive);
      if (this.beanValidationGroups != null)
      {
         clone.setBeanValidationGroups(new ArrayList<String>(this.beanValidationGroups));
      }
      clone.setBootstrapContext(bootstrapContext);
      clone.setConfigProperties(AdminObjectConfig.cloneConfigPropTypeList(configProperties));
      clone.setId(id);
      clone.setTransactionSupport(transactionSupport);
      clone.setVersion(version);
      
      List<ConnectionFactoryConfig> connConfigs = new ArrayList<ConnectionFactoryConfig>();
      for (ConnectionFactoryConfig connConfig: this.connectionDefinitions)
      {
         connConfigs.add(connConfig.clone());
      }
      clone.setConnectionDefinitions(connConfigs);
      
      List<AdminObjectConfig> aoConfigs = new ArrayList<AdminObjectConfig>();
      for (AdminObjectConfig aoConfig: this.adminObjectConfigs)
      {
         aoConfigs.add(aoConfig.clone());
      }
      clone.setAdminObjectConfigs(aoConfigs);
      clone.setWorkManagerConfig(workManagerConfig.clone());
      return clone;
   }
   
   
   /**
    * @param workManagerConfig the workManagerConfig to set
    */
   public void setWorkManagerConfig(WorkManagerConfig workManagerConfig)
   {
      this.workManagerConfig = workManagerConfig;
   }

   /**
    * Adds a VersionChangeListener
    * @param listener the VersionChangeListener
    */
   public void addVersionChangeListener(VersionChangeListener listener)
   {
      this.versionListeners.add(listener);
   }
   
   /**
    * Removes a VersionChangeListener
    * @param listener the VersionChangeListener
    */
   public void removeVersionChangeListener(VersionChangeListener listener)
   {
      this.versionListeners.remove(listener);
   }

   /**
    * Get archive
    * @return The archive
    */
   public String getArchive()
   {
      return archive;
   }

   /**
    * Set archive
    * @param archive The value to set
    */
   public void setArchive(String archive)
   {
      this.archive = archive;
   }

   /**
    * @return the id
    */
   public String getId()
   {
      return id;
   }

   /**
    * @param id the id to set
    */
   public void setId(String id)
   {
      this.id = id;
   }

   /**
    * Get transactionSupport
    * @return The transactionSupport
    */
   public TransactionSupportEnum getTransactionSupport()
   {
      return transactionSupport;
   }

   /**
    * Set transactionSupport
    * @param transactionSupport The value to set
    */
   public void setTransactionSupport(TransactionSupportEnum transactionSupport)
   {
      this.transactionSupport = transactionSupport;
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

   /**
    * Get beanValidationGroups
    * @return The beanValidationGroups
    */
   public List<String> getBeanValidationGroups()
   {
      return beanValidationGroups;
   }

   /**
    * Set beanValidationGroups
    * @param beanValidationGroups The value to set
    */
   public void setBeanValidationGroups(List<String> beanValidationGroups)
   {
      this.beanValidationGroups = beanValidationGroups;
   }

   /**
    * Get bootstrapContext
    * @return The bootstrapContext
    */
   public String getBootstrapContext()
   {
      return bootstrapContext;
   }

   /**
    * Set bootstrapContext
    * @param bootstrapContext The value to set
    */
   public void setBootstrapContext(String bootstrapContext)
   {
      this.bootstrapContext = bootstrapContext;
   }

   /**
    * Get adminObjectConfigs
    * @return The adminObjectConfigs
    */
   public List<AdminObjectConfig> getAdminObjectConfigs()
   {
      return adminObjectConfigs;
   }

   /**
    * Set adminObjectConfigs
    * @param adminObjectConfigs The value to set
    */
   public void setAdminObjectConfigs(List<AdminObjectConfig> adminObjectConfigs)
   {
      this.adminObjectConfigs = adminObjectConfigs;
   }

   /**
    * Get connectionDefinitions
    * @return The connectionDefinitions
    */
   public List<ConnectionFactoryConfig> getConnectionDefinitions()
   {
      return connectionDefinitions;
   }

   /**
    * Set connectionDefinitions
    * @param connectionDefinitions The value to set
    */
   public void setConnectionDefinitions(List<ConnectionFactoryConfig> connectionDefinitions)
   {
      this.connectionDefinitions = connectionDefinitions;
   }
   
   /**
    * @return the workManagerConfig
    */
   public WorkManagerConfig getWorkManagerConfig()
   {
      return workManagerConfig;
   }

   /**
    * Get version
    * @return The version
    */
   public VERSION getVersion()
   {
      return version;
   }

   /**
    * Set version
    * @param version The value to set
    */
   public void setVersion(VERSION version)
   {
      if (!this.version.equals(version))
      {
         this.version = version;
         for (VersionChangeListener l: this.versionListeners)
         {
            l.versionChanged(version);
         }
      }
   }
   
   /**
    * The VersionChangeListener to monitor version change event.    *
    */
   public static interface VersionChangeListener
   {
      /**
       * When version is changed
       * @param version the version
       */
      void versionChanged(VERSION version);
   }
   
   /**
    * WorkManager security related configuration.
    * 
    */
   public static class WorkManagerConfig implements Cloneable
   {
      private boolean mappingRequired;
      private String domain;
      private String defaultPricipal;
      private List<String> defaultGroups = new ArrayList<String>();
      private Map<String, String> userMap = new HashMap<String, String>();
      private Map<String, String> groupMap = new HashMap<String, String>();
      
      @Override
      public WorkManagerConfig clone()
      {
         WorkManagerConfig clone = new WorkManagerConfig();
         clone.defaultGroups = new ArrayList<String>(this.defaultGroups);
         clone.defaultPricipal = this.defaultPricipal;
         clone.domain = this.domain;
         clone.groupMap = new HashMap<>(this.groupMap);
         clone.mappingRequired = this.mappingRequired;
         clone.userMap = new HashMap<>(this.userMap);
         return clone;
      }
      
      /**
       * @param defaultGroups the defaultGroups to set
       */
      public void setDefaultGroups(List<String> defaultGroups)
      {
         this.defaultGroups = defaultGroups;
      }



      /**
       * @param userMap the userMap to set
       */
      public void setUserMap(Map<String, String> userMap)
      {
         this.userMap = userMap;
      }



      /**
       * @param groupMap the groupMap to set
       */
      public void setGroupMap(Map<String, String> groupMap)
      {
         this.groupMap = groupMap;
      }



      /**
       * @return the mappingRequired
       */
      public boolean isMappingRequired()
      {
         return mappingRequired;
      }
      /**
       * @param mappingRequired the mappingRequired to set
       */
      public void setMappingRequired(boolean mappingRequired)
      {
         this.mappingRequired = mappingRequired;
      }
      /**
       * @return the domain
       */
      public String getDomain()
      {
         return domain;
      }
      /**
       * @param domain the domain to set
       */
      public void setDomain(String domain)
      {
         this.domain = domain;
      }
      /**
       * @return the defaultPricipal
       */
      public String getDefaultPricipal()
      {
         return defaultPricipal;
      }
      /**
       * @param defaultPricipal the defaultPricipal to set
       */
      public void setDefaultPricipal(String defaultPricipal)
      {
         this.defaultPricipal = defaultPricipal;
      }
      /**
       * @return the defaultGroups
       */
      public List<String> getDefaultGroups()
      {
         return defaultGroups;
      }
      /**
       * @return the userMap
       */
      public Map<String, String> getUserMap()
      {
         return userMap;
      }
      /**
       * @return the groupMap
       */
      public Map<String, String> getGroupMap()
      {
         return groupMap;
      }
      
   }

}
