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

import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.Credential;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.Extension;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test on ResourceAdapterConfig
 * 
 * @author lgao lin.gao@ironjacamar.org
 *
 */
public class ResourceAdapterConfigTest
{

   /**
    * Tests on ResourceAdapterConfig instance, 
    * list or map instance variables will be initialized.
    * sub configure will be initialized also.
    */
   @Test
   public void testResourceAdapterConfigInstance()
   {
      ResourceAdapterConfig config = new ResourceAdapterConfig();
      Assert.assertNotNull(config.getWorkManagerConfig());
      Assert.assertNotNull(config.getAdminObjectConfigs());
      Assert.assertTrue(config.getAdminObjectConfigs().isEmpty());
      
      Assert.assertNotNull(config.getBeanValidationGroups());
      Assert.assertTrue(config.getBeanValidationGroups().isEmpty());
      
      Assert.assertNotNull(config.getConfigProperties());
      Assert.assertTrue(config.getConfigProperties().isEmpty());
      
      Assert.assertNotNull(config.getConnectionDefinitions());
      Assert.assertTrue(config.getConnectionDefinitions().isEmpty());
      
      ResourceAdapterConfig.WorkManagerConfig workManagerConfig = config.getWorkManagerConfig();
      Assert.assertNotNull(workManagerConfig.getDefaultGroups());
      
      Assert.assertNotNull(workManagerConfig.getGroupMap());
      Assert.assertTrue(workManagerConfig.getGroupMap().isEmpty());
      
      Assert.assertNotNull(workManagerConfig.getUserMap());
      Assert.assertTrue(workManagerConfig.getUserMap().isEmpty());
   }
   
   /**
    * Tests on ConnectionFactoryConfig instance, 
    * list or map instance variables will be initialized.
    * sub configure will be initialized also.
    */
   @Test
   public void testConnectionFactoryConfigInstance()
   {
      ConnectionFactoryConfig connFactoryConfig = new ConnectionFactoryConfig();
      Assert.assertNotNull(connFactoryConfig.getMcfConfigProps()); 
      Assert.assertTrue(connFactoryConfig.getMcfConfigProps().isEmpty());

      ConnectionFactoryConfig.PoolConfig poolConfig = connFactoryConfig.getPoolConifg();
      Assert.assertNotNull(poolConfig);
      
      ConnectionFactoryConfig.RecoveryConfig reCoveryConfig = connFactoryConfig.getRecoveryConfig();
      Assert.assertNotNull(reCoveryConfig);
      Credential credential = reCoveryConfig.getCredential();
      Assert.assertNotNull(credential);
      Extension extension = reCoveryConfig.getExtension();
      Assert.assertNotNull(extension);
      
      ConnectionFactoryConfig.SecurityConfig secConfig = connFactoryConfig.getSecurityConfig();
      Assert.assertNotNull(secConfig);
      
      ConnectionFactoryConfig.TimeoutConfig timeOutConfig = connFactoryConfig.getTimeoutConfig();
      Assert.assertNotNull(timeOutConfig);
      
      ConnectionFactoryConfig.ValidationConfig validationConfig = connFactoryConfig.getValidationConfig();
      Assert.assertNotNull(validationConfig);
      
      ConnectionFactoryConfig.CapacityConfig capacityConfig = poolConfig.getCapacityConfig();
      Assert.assertNotNull(capacityConfig);
      Assert.assertNotNull(capacityConfig.getIncrementer());
      Assert.assertNotNull(capacityConfig.getIncrementer().getConfigProperties());
      Assert.assertNotNull(capacityConfig.getDecrementer());
      Assert.assertNotNull(capacityConfig.getDecrementer().getConfigProperties());
      
   }
   
}
