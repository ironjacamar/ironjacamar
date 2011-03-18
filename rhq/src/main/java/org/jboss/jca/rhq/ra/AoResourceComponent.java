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
package org.jboss.jca.rhq.ra;

import org.jboss.jca.core.api.management.AdminObject;
import org.jboss.jca.core.api.management.ConfigProperty;
import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagementRepository;
import org.jboss.jca.rhq.core.AbstractResourceComponent;
import org.jboss.jca.rhq.core.ManagementRepositoryManager;
import org.jboss.jca.rhq.util.ManagementRepositoryHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.resource.Referenceable;
import javax.resource.spi.AdministeredObject;
import javax.resource.spi.ResourceAdapterAssociation;

import org.jboss.logging.Logger;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;

/**
 * Represent Admin Object in JCA container.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 */
public class AoResourceComponent extends AbstractResourceComponent
{
   /** log */
   private static final Logger logger = Logger.getLogger(AoResourceComponent.class);
   
   /**
    * Gets associated AdminObject.
    * 
    * @return AdminObject
    */
   private AdminObject getAdminObject()
   {
      ManagementRepository mr = ManagementRepositoryManager.getManagementRepository();
      Connector connector = ManagementRepositoryHelper.getConnectorByUniqueId(mr, getRarUniqueId());
      String jcaClsName = getJCAClassName();

      for (AdminObject ao : connector.getAdminObjects())
      {
         Object obj = ao.getAdminObject();
         Class<?> aoCls = obj.getClass();
         if (aoCls.getName().equals(jcaClsName))
         {
            logger.debug("Class Name is: " + jcaClsName);
            return ao;
         }
      }
      throw new IllegalStateException("Can not find associated AdminObject.");
   }

   /**
    * loadResourceConfiguration
    * 
    * @return Configuration configuration
    * @throws Exception exception
    */
   @Override
   public Configuration loadResourceConfiguration() throws Exception
   {

      Configuration config = new Configuration();
      String jcaClsName = getJCAClassName();

      AdminObject ao = getAdminObject();
      Object obj = ao.getAdminObject();
      
      // jndi name
      PropertySimple jndiNameProp = new PropertySimple("jndi-name", ao.getJndiName());
      config.put(jndiNameProp);
      
      // class-name
      PropertySimple intfClsNameProp = new PropertySimple("class-name", jcaClsName);
      config.put(intfClsNameProp);
      
      // interface-class-name
      String aoIntfClsName = getAdminObjectInterfaceClassName(obj.getClass());
      PropertySimple aoIntfClsNameProp = new PropertySimple("interface-class-name", aoIntfClsName);
      config.put(aoIntfClsNameProp);
      
      // use-ra-association
      boolean useRaAsso = ResourceAdapterAssociation.class.isAssignableFrom(obj.getClass());
      PropertySimple useRaAssoProp = new PropertySimple("use-ra-association", Boolean.valueOf(useRaAsso));
      config.put(useRaAssoProp);
      
      // config properties
      List<ConfigProperty> aoConfigProps = ao.getConfigProperties();
      PropertyList configList = getConfigPropertiesList(obj, aoConfigProps);
      config.put(configList);
      
      return config;
   }
   
   /**
    * Gets AdminObject interfaces class name string.
    * 
    * @param aoCls AdminObject class
    * @return string represents all interfaces class names, connected by ', '.
    */
   private String getAdminObjectInterfaceClassName(Class<? extends Object> aoCls)
   {
      List<Class<?>> intfClsList = new ArrayList<Class<?>>();
      AdministeredObject aoAnnotation = aoCls.getAnnotation(AdministeredObject.class);
      if (aoAnnotation != null)
      {
         Class<?>[] intfCls = aoAnnotation.adminObjectInterfaces();
         if (null != intfCls)
         {
            for (Class<?> cls : intfCls)
            {
               intfClsList.add(cls);
            }
         }
      }
      Class<?>[] intfs = aoCls.getInterfaces();
      for (Class<?> intf : intfs)
      {
         if (intf.equals(Serializable.class) || intf.equals(Referenceable.class))
         {
            continue;
         }
         else if (intf.getName().startsWith("javax.resource.spi"))
         {
            continue;
         }
         else if (intf.getName().startsWith("javax.naming"))
         {
            continue;
         }
         if (!intfClsList.contains(intf))
         {
            intfClsList.add(intf);
         }
      }
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i< intfClsList.size(); i ++)
      {
         Class<?> cls = intfClsList.get(i);
         sb.append(cls.getName());
         if (i < intfClsList.size() - 1)
         {
            sb.append(", ");
         }
      }
      return sb.toString();
   }

   @Override
   public void updateResourceConfiguration(ConfigurationUpdateReport updateResourceConfiguration)
   {
      super.updateResourceConfiguration(updateResourceConfiguration);
      Configuration config = updateResourceConfiguration.getConfiguration();
      AdminObject ao = getAdminObject();
      List<ConfigProperty> configProperties = ao.getConfigProperties();
      PropertyList configPropertiesList = config.getList("config-property");
      updatePropertyList(ao.getAdminObject(), configPropertiesList, configProperties);
      
      updateResourceConfiguration.setStatus(ConfigurationUpdateStatus.SUCCESS);
      
   }
   
}
