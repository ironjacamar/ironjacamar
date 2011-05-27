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
package org.jboss.jca.rhq.core;

import org.jboss.jca.core.api.management.ConfigProperty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

/**
 * AbstractJCAResourceComponent
 * 
 * @author <a href="mailto:yy.young@gmail.com">Yong Yang</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 */
public abstract class AbstractResourceComponent extends BaseResourceComponent
   implements ResourceComponent, ConfigurationFacet, OperationFacet, MeasurementFacet
{
   /** log */
   private static final Logger logger = Logger.getLogger(AbstractResourceComponent.class);

   /** resourceContext */
   private ResourceContext resourceContext;

   /** resourceDescription */
   private String resourceDescription;

   /**
    * start lifecycle
    * 
    * @param resourceContext ResourceContext
    * @throws Exception Exception
    */
   @Override
   public void start(ResourceContext resourceContext) throws Exception
   {
      this.resourceContext = resourceContext;
      this.resourceDescription = this.resourceContext.getResourceType() + " Resource with key ["
            + this.resourceContext.getResourceKey() + "]";
   }

   /**
    * getResourceContext
    * 
    * @return resourceContext ResourceContext
    */
   public ResourceContext getResourceContext()
   {
      return this.resourceContext;
   }

   /**
    * It is used to get unique information of the component.
    * 
    * @return current plugin configuration of this component.
    */
   protected String getResourceDescription()
   {
      return this.resourceDescription;
   }


   /**
    * updateResourceConfiguration
    * 
    * @param updateResourceConfiguration ConfigurationUpdateReport
    */
   @Override
   public void updateResourceConfiguration(ConfigurationUpdateReport updateResourceConfiguration)
   {
   }

   /**
    * getValues
    * 
    * @param measurementReport MeasurementReport
    * @param measurementScheduleRequests Set<MeasurementScheduleRequest> 
    * @throws Exception exception
    */
   @Override
   public void getValues(MeasurementReport measurementReport,
      Set<MeasurementScheduleRequest> measurementScheduleRequests) throws Exception
   {
   }

   /**
    * invokeOperation
    * 
    * @param s String
    * @param configuration Configuration
    * @return OperationResult OperationResult
    * @throws InterruptedException interruptedException
    * @throws Exception exception
    */
   @Override
   public OperationResult invokeOperation(String s, Configuration configuration) throws InterruptedException, Exception
   {
      return null;
   }
   
   /**
    * The RAR unique id is the identifier of each RAR resource connector.
    * If the component is under the connector, the resource key is: uniqueID#Resource_Class_Name
    * for example of the sample ManagedConnectionFactory component, the key is:
    * <p>
    * localtransaction.rar#org.jboss.jca.samples.localtransaction16b.LocalTransactionManagedConnectionFactory
    * </p>
    * 
    * @return The associated RAR unique ID
    */
   protected String getRarUniqueId()
   {
      String resKey = getResourceContext().getResourceKey();
      int lastPos = resKey.lastIndexOf("#");
      if (lastPos == -1)
      {
         return resKey;
      }
      return resKey.substring(0, lastPos);
   }

   /**
    * getJCAClassName
    * @return JCA Class Name.
    */
   protected String getJCAClassName()
   {
      String resKey = getResourceContext().getResourceKey();
      int lastPos = resKey.lastIndexOf("#");
      if (lastPos == -1)
      {
         return null;
      }
      return resKey.substring(lastPos + 1);
   }
   
   /**
    * getJndiName
    * @return binding jndi name
    */
   protected String getJndiName()
   {
      String resKey = getResourceContext().getResourceKey();
      int lastPos = resKey.lastIndexOf("#");
      if (lastPos == -1)
      {
         return null;
      }
      return resKey.substring(lastPos + 1);
   }

   /**
    * getConfigPropertiesList
    * 
    * @param jcaObject  JCA Object which has some ConfigProperty defined.
    *        It can be either Connector or children of the Connector in the tree.
    * @param configProperties  ConfigProperty instances belongs to the JCA Object.
    * 
    * @return PropertyList which matches the ConfigProperty defined in the JCA descriptor.
    * 
    * @throws InvocationTargetException 
    * @throws IllegalAccessException 
    * @throws NoSuchMethodException 
    */
   protected PropertyList getConfigPropertiesList(Object jcaObject, List<ConfigProperty> configProperties)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
   {
      String configListName = "config-property";
      String configMapName = configListName;
      PropertyList configList = new PropertyList(configListName);

      for (ConfigProperty configProp : configProperties)
      {
         PropertyMap configMap = new PropertyMap(configMapName);
         String configName = configProp.getName();

         Object value = getProperty(jcaObject, configName);

         PropertySimple nameProp = new PropertySimple("name", configName);
         configMap.put(nameProp);

         PropertySimple typeProp = new PropertySimple("type", value.getClass().getName());
         configMap.put(typeProp);

         PropertySimple valueProp = new PropertySimple("value", value);
         configMap.put(valueProp);

         configList.add(configMap);
      }
      return configList;
   }

   /**
    * getProperty
    * 
    * @param obj object
    * @param propName  property name
    * 
    * @return object 
    * @throws InvocationTargetException 
    * @throws IllegalAccessException 
    * @throws NoSuchMethodException
    */
   private Object getProperty(Object obj, String propName) throws IllegalAccessException, 
      InvocationTargetException, NoSuchMethodException
   {
      String methodNameBase = propName.substring(0, 1).toUpperCase() + propName.substring(1);
      String methodName = "get" + methodNameBase;
      Method method = null;
      try
      {
         method = obj.getClass().getDeclaredMethod(methodName, new Class<?>[0]);
      }
      catch (NoSuchMethodException e)
      {
         logger.info("No Method: " + methodName + " found. Try method name: is" + methodNameBase);
         method = obj.getClass().getDeclaredMethod("is" + methodNameBase, new Class<?>[0]);
      }

      return method.invoke(obj, new Object[0]);
   }

   /**
    * It is used to get unique information of the component.
    * 
    * @return current plugin configuration of this component.
    */
   protected Configuration getPluginConfiguration()
   {
      if (resourceContext != null)
         return getResourceContext().getPluginConfiguration();
      else
         return null;
   }
   
   /**
    * Updates the ConfigProperty from value of ProperyList to the jcaObj.
    * 
    * @param jcaObj the JCA object which the value will be update to
    * @param configPropList the PropertyList where the value comes from
    * @param configProperties The ConfigProperty list
    */
   protected void updatePropertyList(Object jcaObj, PropertyList configPropList, List<ConfigProperty> configProperties)
   {
      for (Property prop : configPropList.getList())
      {
         PropertyMap propMap = (PropertyMap)prop;
         String configName = propMap.getSimpleValue("name", null);
         String type = propMap.getSimpleValue("type", null);
         String valueString = propMap.getSimpleValue("value", null);
         ConfigProperty configProp = getConfigPropertyByName(configName, configProperties);
         if (null == configProp)
         {
            throw new IllegalStateException("Can not find ConfigProperty with name: " + configName);
         }
         if (!configProp.isDynamic())
         {
            logger.warn("ConfigProperty with name: " + configName + " is not updatable, ignore it.");
            continue;
         }
         try
         {
            setPropertyValue(jcaObj, configName, type, valueString);
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Can not update ConfigProperty value.", e);
         }
      }
   }
   
   private void setPropertyValue(Object jcaObj, String name, String type, String valueString) throws Exception
   {
      Class<?> typeCls = Class.forName(type);
      Object value = convertObjValue(typeCls, valueString);
      String methodNameBase = name.substring(0, 1).toUpperCase() + name.substring(1);
      String methodName = "set" + methodNameBase;
      Method setMtd = jcaObj.getClass().getMethod(methodName, typeCls);
      setMtd.invoke(jcaObj, value);
   }

   /**
    * convertObjValue
    * 
    * @param typeCls Class.
    *        the Class is one of the <code>config-property-typeType</code> defined in <code>connector_1.6.xsd</code>
    * @param valueString value string
    * @return value in typeCls
    */
   private Object convertObjValue(Class<?> typeCls, String valueString)
   {
      if (typeCls.equals(String.class))
      {
         return valueString;
      }
      else if (typeCls.equals(Character.class))
      {
         return Character.valueOf(valueString.charAt(0));
      }
      else if (typeCls.equals(Integer.class))
      {
         return Integer.valueOf(valueString);
      }
      else if (typeCls.equals(Boolean.class))
      {
         return Boolean.valueOf(valueString);
      }
      else if (typeCls.equals(Double.class))
      {
         return Double.valueOf(valueString);
      }
      else if (typeCls.equals(Byte.class))
      {
         return Byte.valueOf(valueString);
      }
      else if (typeCls.equals(Short.class))
      {
         return Short.valueOf(valueString);
      }
      else if (typeCls.equals(Long.class))
      {
         return Long.valueOf(valueString);
      }
      else if (typeCls.equals(Float.class))
      {
         return Float.valueOf(valueString);
      }
      else
      {
         throw new IllegalStateException("Unknown class: " + typeCls);
      }
   }

   /**
    * getConfigPropertyByName
    * 
    * @param configName ConfigProperty name
    * @param configProperties ConfigProperty list
    * @return ConfigProperty with name configName, otherwise null.
    */
   protected ConfigProperty getConfigPropertyByName(String configName, List<ConfigProperty> configProperties)
   {
      for (ConfigProperty configProperty : configProperties)
      {
         if (configName.equals(configProperty.getName()))
         {
            return configProperty;
         }
      }
      return null;
   }
   
   /**
    * Gets Uploaded Dir.
    * 
    * @return directory where the RAR or -ds.xml will be uploaded to.
    */
   protected String getUploadedDir()
   {
      String uploadedDir = System.getProperty("jboss.server.temp.dir");
      if (null == uploadedDir)
      {
         uploadedDir = System.getProperty("java.io.tmpdir");
      }
      if (null == uploadedDir)
      {
         uploadedDir = "/tmp";
      }
      return uploadedDir;
   }

}
