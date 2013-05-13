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
package org.jboss.jca.as.converters.wls.metadata;

import org.jboss.jca.as.converters.wls.api.metadata.AdminObjectGroup;
import org.jboss.jca.as.converters.wls.api.metadata.AdminObjectInstance;
import org.jboss.jca.as.converters.wls.api.metadata.AdminObjects;
import org.jboss.jca.as.converters.wls.api.metadata.AnonPrincipal;
import org.jboss.jca.as.converters.wls.api.metadata.AnonPrincipalCaller;
import org.jboss.jca.as.converters.wls.api.metadata.ConfigProperties;
import org.jboss.jca.as.converters.wls.api.metadata.ConfigProperty;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinition;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionInstance;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectorWorkManager;
import org.jboss.jca.as.converters.wls.api.metadata.InboundCallerPrincipalMapping;
import org.jboss.jca.as.converters.wls.api.metadata.InboundGroupPrincipalMapping;
import org.jboss.jca.as.converters.wls.api.metadata.Logging;
import org.jboss.jca.as.converters.wls.api.metadata.OutboundResourceAdapter;
import org.jboss.jca.as.converters.wls.api.metadata.PoolParams;
import org.jboss.jca.as.converters.wls.api.metadata.ResourceAdapterSecurity;
import org.jboss.jca.as.converters.wls.api.metadata.SecurityWorkContext;
import org.jboss.jca.as.converters.wls.api.metadata.TransactionSupport;
import org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector;
import org.jboss.jca.as.converters.wls.api.metadata.WorkManager;
import org.jboss.jca.as.converters.wls.api.metadata.v13.WeblogicConnector13;
import org.jboss.jca.as.converters.wls.metadata.v13.WeblogicConnector13Impl;
import org.jboss.jca.common.CommonBundle;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.jboss.logging.Messages;

/**
 *
 * A WeblogicRaPasrer
 *
 * @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
 *
 */
public class WeblogicRaPasrer extends AbstractParser implements MetadataParser<WeblogicConnector>
{
   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);
   
   @Override
   public WeblogicConnector parse(InputStream xmlInputStream) throws Exception
   {
      XMLStreamReader reader = null;

      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
      reader = inputFactory.createXMLStreamReader(xmlInputStream);
      return parse(reader);
   }

   @Override
   public WeblogicConnector parse(XMLStreamReader reader) throws Exception
   {
      WeblogicConnector connector = null;

      try
      {
         //iterate over tags
         int iterate;
         try
         {
            iterate = reader.nextTag();
         }
         catch (XMLStreamException e)
         {
            //founding a non tag..go on Normally non-tag found at beginning are comments or DTD declaration
            iterate = reader.nextTag();
         }
         switch (iterate)
         {
            case END_ELEMENT : {
               // should mean we're done, so ignore it.
               break;
            }
            case START_ELEMENT : {
               if ("1.0".equals(reader.getAttributeValue(null, "version")))
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case CONNECTOR : {
                        connector = parseWlsConnector10(reader);
                        break;
                     }
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }

               }
               else if ("1.2".equals(reader.getAttributeValue(null, "version")))
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case CONNECTOR : {
                        connector = parseWlsConnector12(reader);
                        break;
                     }
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }
               }
               else
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case CONNECTOR : {
                        connector = parseWlsConnector13(reader);
                        break;
                     }
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }
               }

               break;
            }
            default :
               throw new IllegalStateException();
         }
      }
      finally
      {
         if (reader != null)
            reader.close();
      }

      return connector;
   }

   private WeblogicConnector parseWlsConnector13(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      String nativeLibdir = null;
      String jndiName = null;
      //Boolean eaoa = false;
      //Boolean egac = false;
      //WorkManager wm = null;
      //ConnectorWorkManager cwm = null;
      ResourceAdapterSecurity ras = null;
      ConfigProperties props = null;
      AdminObjects aos = null;
      OutboundResourceAdapter ora = null;
      
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {
                  return new WeblogicConnector13Impl(nativeLibdir, jndiName, ras, props, aos, ora);
               }
               else
               {
                  if (WeblogicConnector13.Tag.forName(reader.getLocalName()) == WeblogicConnector13.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (WeblogicConnector13.Tag.forName(reader.getLocalName()))
               {
                  case NATIVE_LIBDIR : {
                     nativeLibdir = elementAsString(reader);
                     break;
                  }
                  case JNDI_NAME : {
                     jndiName = elementAsString(reader);
                     break;
                  }
                  case ENABLE_ACCESS_OUTSIDE_APP :
                  case ENABLE_GLOBAL_ACCESS_TO_CLASSES : {
                     elementAsBoolean(reader);
                     break;
                  }
                  case WORK_MANAGER : {
                     parseWorkManager(reader);
                     break;
                  }
                  case CONNECTOR_WORK_MANAGER : {
                     parseConnectorWorkManager(reader);
                     break;
                  }
                  case RESOURCE_ADAPTER_SECURITY : {
                     ras = parseResourceAdapaterSecurity(reader);
                     break;
                  }
                  case PROPERTIES : {
                     props = parseProperties(reader);
                     break;
                  }
                  case ADMIN_OBJECTS : {
                     aos = parseAdminObjects(reader);
                     break;
                  }
                  case OUTBOUND_RESOURCE_ADAPTER : {
                     ora = parseOutBoundResourceAdapetr(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   private ConfigProperties parseProperties(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<ConfigProperty> props = new ArrayList<ConfigProperty>();
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (WeblogicConnector13.Tag.forName(reader.getLocalName()) == WeblogicConnector13.Tag.PROPERTIES ||
                  AdminObjects.Tag.forName(reader.getLocalName()) == AdminObjects.Tag.DEFAULT_PROPERTIES ||
                  AdminObjectGroup.Tag.forName(reader.getLocalName()) == AdminObjectGroup.Tag.DEFAULT_PROPERTIES ||
                  AdminObjectInstance.Tag.forName(reader.getLocalName()) == AdminObjectInstance.Tag.PROPERTIES ||
                  ConnectionDefinitionProperties.Tag.forName(reader.getLocalName()) == 
                  ConnectionDefinitionProperties.Tag.PROPERTIES)
               {
                  return new ConfigPropertiesImpl(props);
               }
               else
               {
                  if (ConfigProperties.Tag.forName(reader.getLocalName()) == ConfigProperties.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ConfigProperties.Tag.forName(reader.getLocalName()))
               {
                  case PROPERTY : {
                     props.add(parseProperty(reader));
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private ConfigProperty parseProperty(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      String name = null;
      String value = null;
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (ConfigProperties.Tag.forName(reader.getLocalName()) == ConfigProperties.Tag.PROPERTY)
               {
                  return new ConfigPropertyImpl(name, value);
               }
               else
               {
                  if (ConfigProperty.Tag.forName(reader.getLocalName()) == ConfigProperty.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ConfigProperty.Tag.forName(reader.getLocalName()))
               {
                  case NAME : {
                     name = elementAsString(reader);
                     break;
                  }
                  case VALUE : {
                     value = elementAsString(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private AdminObjects parseAdminObjects(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ConfigProperties props = null;
      ArrayList<AdminObjectGroup> aogs = new ArrayList<AdminObjectGroup>();
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (WeblogicConnector13.Tag.forName(reader.getLocalName()) == WeblogicConnector13.Tag.ADMIN_OBJECTS)
               {
                  return new AdminObjectsImpl(props, aogs);
               }
               else
               {
                  if (AdminObjects.Tag.forName(reader.getLocalName()) == AdminObjects.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (AdminObjects.Tag.forName(reader.getLocalName()))
               {
                  case DEFAULT_PROPERTIES : {
                     props = parseProperties(reader);
                     break;
                  }
                  case ADMIN_OBJECT_GROUP : {
                     aogs.add(parseAdminObejctGroup(reader));
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private AdminObjectGroup parseAdminObejctGroup(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      String aoInterface = null;
      String aoClass = null;
      ConfigProperties props = null;
      ArrayList<AdminObjectInstance> aois = new ArrayList<AdminObjectInstance>();
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (AdminObjects.Tag.forName(reader.getLocalName()) == AdminObjects.Tag.ADMIN_OBJECT_GROUP)
               {
                  return new AdminObjectGroupImpl(aoInterface, aoClass, props, aois);
               }
               else
               {
                  if (AdminObjectGroup.Tag.forName(reader.getLocalName()) == AdminObjectGroup.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (AdminObjectGroup.Tag.forName(reader.getLocalName()))
               {
                  case ADMIN_OBJECT_INTERFACE : {
                     aoInterface = elementAsString(reader);
                     break;
                  }
                  case ADMIN_OBJECT_CLASS : {
                     aoClass = elementAsString(reader);
                     break;
                  }
                  case DEFAULT_PROPERTIES : {
                     props = parseProperties(reader);
                     break;
                  }
                  case ADMIN_OBJECT_INSTANCE : {
                     aois.add(parseAdminObejctInstance(reader));
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private AdminObjectInstance parseAdminObejctInstance(XMLStreamReader reader) 
      throws XMLStreamException, ParserException
   {
      String jndiName = null;
      ConfigProperties props = null;
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (AdminObjectGroup.Tag.forName(reader.getLocalName()) == AdminObjectGroup.Tag.ADMIN_OBJECT_INSTANCE)
               {
                  return new AdminObjectInstanceImpl(jndiName, props);
               }
               else
               {
                  if (AdminObjectInstance.Tag.forName(reader.getLocalName()) == AdminObjectInstance.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (AdminObjectInstance.Tag.forName(reader.getLocalName()))
               {
                  case JNDI_NAME : {
                     jndiName = elementAsString(reader);
                     break;
                  }
                  case PROPERTIES : {
                     props = parseProperties(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private OutboundResourceAdapter parseOutBoundResourceAdapetr(XMLStreamReader reader) 
      throws XMLStreamException, ParserException
   {
      ConnectionDefinitionProperties cdProps = null;
      ArrayList<ConnectionDefinition> cds = new ArrayList<ConnectionDefinition>();
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (WeblogicConnector13.Tag.forName(reader.getLocalName()) == 
                  WeblogicConnector13.Tag.OUTBOUND_RESOURCE_ADAPTER)
               {
                  return new OutboundResourceAdapterImpl(cdProps, cds);
               }
               else
               {
                  if (OutboundResourceAdapter.Tag.forName(reader.getLocalName()) == 
                     OutboundResourceAdapter.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (OutboundResourceAdapter.Tag.forName(reader.getLocalName()))
               {
                  case DEFAULT_CONNECTION_PROPERTIES : {
                     cdProps = parseConnectionDefinitionProperties(reader);
                     break;
                  }
                  case CONNECTION_DEFINITION_GROUP : {
                     cds.add(parseConnectionDefinition(reader));
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private ConnectionDefinition parseConnectionDefinition(XMLStreamReader reader)
      throws XMLStreamException, ParserException
   {
      String cdi = null;
      ConnectionDefinitionProperties cdProps = null;
      ArrayList<ConnectionInstance> cis = new ArrayList<ConnectionInstance>();
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (OutboundResourceAdapter.Tag.forName(reader.getLocalName()) == 
                  OutboundResourceAdapter.Tag.CONNECTION_DEFINITION_GROUP)
               {
                  return new ConnectionDefinitionImpl(cdi, cdProps, cis);
               }
               else
               {
                  if (ConnectionDefinition.Tag.forName(reader.getLocalName()) == ConnectionDefinition.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ConnectionDefinition.Tag.forName(reader.getLocalName()))
               {
                  case CONNECTION_FACTORY_INTERFACE : {
                     cdi = elementAsString(reader);
                     break;
                  }
                  case DEFAULT_CONNECTION_PROPERTIES : {
                     cdProps = parseConnectionDefinitionProperties(reader);
                     break;
                  }
                  case CONNECTION_INSTANCE : {
                     cis.add(parseConnectionInstance(reader));
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private ConnectionInstance parseConnectionInstance(XMLStreamReader reader)
      throws XMLStreamException, ParserException
   {
      String description = null;
      String jndiName = null;
      ConnectionDefinitionProperties cdProps = null;

      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (ConnectionDefinition.Tag.forName(reader.getLocalName()) == 
                     ConnectionDefinition.Tag.CONNECTION_INSTANCE)
               {
                  return new ConnectionInstanceImpl(description, jndiName, cdProps);
               }
               else
               {
                  if (ConnectionInstance.Tag.forName(reader.getLocalName()) == ConnectionInstance.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ConnectionInstance.Tag.forName(reader.getLocalName()))
               {
                  case DESCRIPTION : {
                     description = elementAsString(reader);
                     break;
                  }
                  case JNDI_NAME : {
                     jndiName = elementAsString(reader);
                     break;
                  }
                  case CONNECTION_PROPERTIES : {
                     cdProps = parseConnectionDefinitionProperties(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private ConnectionDefinitionProperties parseConnectionDefinitionProperties(XMLStreamReader reader)
      throws XMLStreamException, ParserException
   {
      PoolParams poolParams = null;
      Logging logging = null;
      TransactionSupport trans = null;
      String authMech = null;
      Boolean reAuthSupport = null;
      ConfigProperties props = null;
      String resAuth = null;
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (OutboundResourceAdapter.Tag.forName(reader.getLocalName()) == 
                  OutboundResourceAdapter.Tag.DEFAULT_CONNECTION_PROPERTIES ||
                  ConnectionDefinition.Tag.forName(reader.getLocalName()) == 
                  ConnectionDefinition.Tag.DEFAULT_CONNECTION_PROPERTIES ||
                  ConnectionInstance.Tag.forName(reader.getLocalName()) == 
                  ConnectionInstance.Tag.CONNECTION_PROPERTIES)
               {
                  return new ConnectionDefinitionPropertiesImpl(poolParams, logging, trans, 
                     authMech, reAuthSupport, props, resAuth);
               }
               else
               {
                  if (ConnectionDefinitionProperties.Tag.forName(reader.getLocalName()) == 
                     ConnectionDefinitionProperties.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ConnectionDefinitionProperties.Tag.forName(reader.getLocalName()))
               {
                  case POOL_PARAMS : {
                     poolParams = parsePoolParams(reader);
                     break;
                  }
                  case LOGGING : {
                     logging = parseLogging(reader);
                     break;
                  }
                  case TRANSATION_SUPPORT : {
                     trans = parseTransactionSupport(reader);
                     break;
                  }
                  case AUTHENTICATION_MECHANISM : {
                     authMech = elementAsString(reader);
                     break;
                  }
                  case REAUTHENTICATION_SUPPORT : {
                     reAuthSupport = elementAsBoolean(reader);
                     break;
                  }
                  case PROPERTIES : {
                     props = parseProperties(reader);
                     break;
                  }
                  case RES_AUTH : {
                     resAuth = elementAsString(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private TransactionSupport parseTransactionSupport(XMLStreamReader reader) 
      throws XMLStreamException, ParserException
   {
      String trans = elementAsString(reader);
      if (trans.equals("XATransaction"))
      {
         return TransactionSupport.XATransaction;
      }
      else if (trans.equals("LocalTransaction"))
      {
         return TransactionSupport.LocalTransaction;
      }
      else if (trans.equals("NoTransaction"))
      {
         return TransactionSupport.NoTransaction;
      }
      else
      {
         return TransactionSupport.NotDefined;
      }
   }

   private Logging parseLogging(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (ConnectionDefinitionProperties.Tag.forName(reader.getLocalName()) == 
                  ConnectionDefinitionProperties.Tag.LOGGING)
               {
                  return null;
               }
            }
         }
      }
      return null;
   }

   private PoolParams parsePoolParams(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      //String noMapped = null;

      Integer initialCapacity = null;
      Integer maxCapacity = null;
      Integer shrinkFrequencySeconds = null;
      Integer connectionCreationRetryFrequencySeconds = null;
      Integer connectionReserveTimeoutSeconds = null;
      Integer testFrequencySeconds = null;
      Integer capacityIncrement = null;
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (ConnectionDefinitionProperties.Tag.forName(reader.getLocalName()) == 
                  ConnectionDefinitionProperties.Tag.POOL_PARAMS)
               {
                  return new PoolParamsImpl(initialCapacity, maxCapacity, capacityIncrement, shrinkFrequencySeconds, 
                     connectionCreationRetryFrequencySeconds, connectionReserveTimeoutSeconds, testFrequencySeconds);
               }
               else
               {
                  if (PoolParams.Tag.forName(reader.getLocalName()) == PoolParams.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (PoolParams.Tag.forName(reader.getLocalName()))
               {
                  case INITIAL_CAPACITY : {
                     initialCapacity = elementAsInteger(reader);
                     break;
                  }
                  case MAX_CAPACITY : {
                     maxCapacity = elementAsInteger(reader);
                     break;
                  }
                  case SHRINK_FREQUENCY_SECONDS : {
                     shrinkFrequencySeconds = elementAsInteger(reader);
                     break;
                  }
                  case CONNECTION_CREATION_RETRY_FREQUENCY_SECONDS : {
                     connectionCreationRetryFrequencySeconds = elementAsInteger(reader);
                     break;
                  }
                  case CONNECTION_RESERVE_TIMWOUT_SECONDS : {
                     connectionReserveTimeoutSeconds = elementAsInteger(reader);
                     break;
                  }
                  case TEST_FREQUENCY_SECONDS : {
                     testFrequencySeconds = elementAsInteger(reader);
                     break;
                  }
                  case CAPACITY_INCREMENT: {
                     capacityIncrement = elementAsInteger(reader);
                     break;
                  }
                  case SHRINKING_ENABLED : 
                  case HIGHEST_NUM_WAITERS :
                  case HIGHEST_NUM_UNAVILABE :
                  case TEST_CONNECTION_ON_CREATE :
                  case TEST_CONNECTION_ON_RELEASE :
                  case TEST_CONNECTION_ON_RESERVE : 
                  case PROFILE_HARVEST_FREQUENCY_SECONDS :
                  case IGNORE_IN_USE_CONNECTION_ENABLED : 
                  case MATCH_CONNECTIONS_SUPPORTED : 
                  case USE_FIRST_AVAILABLE : {
                     //noMapped = elementAsString(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private ResourceAdapterSecurity parseResourceAdapaterSecurity(XMLStreamReader reader)
      throws XMLStreamException, ParserException
   {
      //AnonPrincipal dpn = null;
      //AnonPrincipal mapn = null;
      //AnonPrincipalCaller rapn = null;
      //AnonPrincipalCaller rwapn = null;
      SecurityWorkContext swc = null;
            
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (WeblogicConnector13.Tag.forName(reader.getLocalName()) == 
                  WeblogicConnector13.Tag.RESOURCE_ADAPTER_SECURITY)
               {
                  return new ResourceAdapterSecurityImpl(swc);

               }
               else
               {
                  if (ResourceAdapterSecurity.Tag.forName(reader.getLocalName()) == 
                     ResourceAdapterSecurity.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ResourceAdapterSecurity.Tag.forName(reader.getLocalName()))
               {
                  case DEFAULT_PRINCIPAL_NAME : 
                  case MANAGE_AS_PRINCIPAL_NAME : {
                     parseAnonPrincipal(reader);
                     break;
                  }
                  case RUN_AS_PRINCIPAL_NAME : 
                  case RUN_WORK_AS_PRINCIPAL_NAME : {
                     parseAnonPrincipalCaller(reader);
                     break;
                  }
                  case SECURITY_WORK_CONTEXT : {
                     swc = parseSecurityWorkContext(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private SecurityWorkContext parseSecurityWorkContext(XMLStreamReader reader) 
      throws XMLStreamException, ParserException
   {
      Boolean emr = null;
      AnonPrincipal cpdm = null;
      ArrayList<InboundCallerPrincipalMapping> cpms = new ArrayList<InboundCallerPrincipalMapping>();
      String gpdm = null;
      ArrayList<InboundGroupPrincipalMapping> gpms = new ArrayList<InboundGroupPrincipalMapping>();
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (ResourceAdapterSecurity.Tag.forName(reader.getLocalName()) == 
                  ResourceAdapterSecurity.Tag.SECURITY_WORK_CONTEXT)
               {
                  return new SecurityWorkContextImpl(emr, cpdm, cpms, gpdm, gpms);
               }
               else
               {
                  if (SecurityWorkContext.Tag.forName(reader.getLocalName()) == SecurityWorkContext.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (SecurityWorkContext.Tag.forName(reader.getLocalName()))
               {
                  case INBOUND_MAPPING_REQUIRED : {
                     emr = elementAsBoolean(reader);
                     break;
                  }
                  case CALLER_PRINCIPAL_DEFAULT_MAPPED : {
                     cpdm = parseAnonPrincipal(reader);
                     break;
                  }
                  case CALLER_PRINCIPAL_MAPPING : {
                     cpms.add(parseCallerPrincipalMapping(reader));
                     break;
                  }
                  case GROUP_PRINCIPAL_DEFAULT_MAPPED : {
                     gpdm = elementAsString(reader);
                     break;
                  }
                  case GROUP_PRINCIPAL_MAPPING : {
                     gpms.add(parseGroupPrincipalMapping(reader));
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private InboundGroupPrincipalMapping parseGroupPrincipalMapping(XMLStreamReader reader) 
      throws XMLStreamException, ParserException
   {
      String eisGroup = null;
      String mappedGroup = null;
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (SecurityWorkContext.Tag.forName(reader.getLocalName()) == 
                  SecurityWorkContext.Tag.GROUP_PRINCIPAL_MAPPING)
               {
                  return new InboundGroupPrincipalMappingImpl(eisGroup, mappedGroup);
               }
               else
               {
                  if (InboundGroupPrincipalMapping.Tag.forName(reader.getLocalName()) == 
                     InboundGroupPrincipalMapping.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (InboundGroupPrincipalMapping.Tag.forName(reader.getLocalName()))
               {
                  case EIS_GROUP_PRINCIPAL : {
                     eisGroup = elementAsString(reader);
                     break;
                  }
                  case MAPPED_GROUP_PRINCIPAL : {
                     mappedGroup = elementAsString(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private InboundCallerPrincipalMapping parseCallerPrincipalMapping(XMLStreamReader reader) 
      throws XMLStreamException, ParserException
   {
      String eisCaller = null;
      AnonPrincipal mcp = null;
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (SecurityWorkContext.Tag.forName(reader.getLocalName()) == 
                  SecurityWorkContext.Tag.CALLER_PRINCIPAL_MAPPING)
               {
                  return new InboundCallerPrincipalMappingImpl(eisCaller, mcp);
               }
               else
               {
                  if (InboundCallerPrincipalMapping.Tag.forName(reader.getLocalName()) == 
                     InboundCallerPrincipalMapping.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (InboundCallerPrincipalMapping.Tag.forName(reader.getLocalName()))
               {
                  case EIS_CALLER_PRINCIPAL : {
                     eisCaller = elementAsString(reader);
                     break;
                  }
                  case MAPPED_CALLER_PRINCIPAL : {
                     mcp = parseAnonPrincipal(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }

   private AnonPrincipal parseAnonPrincipal(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Boolean anonIdent = false;
      String principleName = null;
      
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (ResourceAdapterSecurity.Tag.forName(reader.getLocalName()) == 
                  ResourceAdapterSecurity.Tag.DEFAULT_PRINCIPAL_NAME ||
                  ResourceAdapterSecurity.Tag.forName(reader.getLocalName()) == 
                  ResourceAdapterSecurity.Tag.MANAGE_AS_PRINCIPAL_NAME ||
                  SecurityWorkContext.Tag.forName(reader.getLocalName()) == 
                  SecurityWorkContext.Tag.CALLER_PRINCIPAL_DEFAULT_MAPPED ||
                  InboundCallerPrincipalMapping.Tag.forName(reader.getLocalName()) == 
                  InboundCallerPrincipalMapping.Tag.MAPPED_CALLER_PRINCIPAL)
               {
                  return new AnonPrincipalImpl(anonIdent, principleName);
               }
               else
               {
                  if (AnonPrincipal.Tag.forName(reader.getLocalName()) == AnonPrincipal.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (AnonPrincipal.Tag.forName(reader.getLocalName()))
               {
                  case USE_ANONYMOUS_IDENTITY : {
                     anonIdent = elementAsBoolean(reader);
                     break;
                  }
                  case PRINCIPAL_NAME : {
                     principleName = elementAsString(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      return null;
   }
   private AnonPrincipalCaller parseAnonPrincipalCaller(XMLStreamReader reader) 
      throws XMLStreamException, ParserException
   {
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (ResourceAdapterSecurity.Tag.forName(reader.getLocalName()) == 
                  ResourceAdapterSecurity.Tag.RUN_AS_PRINCIPAL_NAME)
               {
                  return null;
               }
               if (ResourceAdapterSecurity.Tag.forName(reader.getLocalName()) == 
                  ResourceAdapterSecurity.Tag.RUN_WORK_AS_PRINCIPAL_NAME)
               {
                  return null;
               }
            }
         }
      }
      return null;
   }
   private ConnectorWorkManager parseConnectorWorkManager(XMLStreamReader reader) 
      throws XMLStreamException, ParserException
   {
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (WeblogicConnector13.Tag.forName(reader.getLocalName()) == 
                  WeblogicConnector13.Tag.CONNECTOR_WORK_MANAGER)
               {
                  return null;

               }
            }
         }
      }
      return null;
   }

   private WorkManager parseWorkManager(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      while (reader.hasNext())
      {
         switch (reader.next())
         {
            case END_ELEMENT : {
               if (WeblogicConnector13.Tag.forName(reader.getLocalName()) == WeblogicConnector13.Tag.WORK_MANAGER)
               {
                  return null;

               }
            }
         }
      }
      return null;
   }

   private WeblogicConnector parseWlsConnector12(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      return null;
   }

   private WeblogicConnector parseWlsConnector10(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      return null;
   }

   /**
   *
   * A Tag.
   */
   public enum Tag 
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /** connector tag name
       *
       */
      CONNECTOR("weblogic-connector");

      private String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Tag(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }

      private static final Map<String, Tag> MAP;

      static
      {
         final Map<String, Tag> map = new HashMap<String, Tag>();
         for (Tag element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAP = map;
      }

      /**
       * Set the value
       * @param v The name
       * @return The value
       */
      Tag value(String v)
      {
         name = v;
         return this;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

}
