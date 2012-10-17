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
public class ResourceAdapterConfig
{

   /**
    * The Version enum
    *
    */
   public enum VERSION
   {
      /** Version 1.0 */
      VERSION_1_0,
      
      /** Version 1.1 */
      VERSION_1_1
   }
   
   private VERSION version = VERSION.VERSION_1_0;
   
   private String archive;
   private TransactionSupportEnum transactionSupport;
   private Map<String, String> configProperties = new HashMap<String, String>();
   private List<String> beanValidationGroups = new ArrayList<String>();
   private String bootstrapContext = null;
   
   private List<AdminObjectConfig> adminObjectConfigs = new ArrayList<AdminObjectConfig>();
   
   private List<ConnectionFactoryConfig> connectionDefinitions = new ArrayList<ConnectionFactoryConfig>();
   
   /**
    * The constructor.
    */
   public ResourceAdapterConfig()
   {
      super();
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
   public Map<String, String> getConfigProperties()
   {
      return configProperties;
   }

   /**
    * Set configProperties
    * @param configProperties The value to set
    */
   public void setConfigProperties(Map<String, String> configProperties)
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
      this.version = version;
   }

}
