/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.metadata.merge;

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.ds.CommonDataSource;
import org.ironjacamar.common.api.metadata.ds.DataSource;
import org.ironjacamar.common.api.metadata.ds.XaDataSource;
import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.spec.AdminObject;
import org.ironjacamar.common.api.metadata.spec.AuthenticationMechanism;
import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.common.api.metadata.spec.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.api.metadata.spec.Connector.Version;
import org.ironjacamar.common.api.metadata.spec.Icon;
import org.ironjacamar.common.api.metadata.spec.InboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.LicenseType;
import org.ironjacamar.common.api.metadata.spec.LocalizedXsdString;
import org.ironjacamar.common.api.metadata.spec.OutboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.ResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.SecurityPermission;
import org.ironjacamar.common.api.metadata.spec.XsdString;
import org.ironjacamar.common.metadata.spec.ConfigPropertyImpl;
import org.ironjacamar.common.metadata.spec.ConnectionDefinitionImpl;
import org.ironjacamar.common.metadata.spec.ConnectorImpl;
import org.ironjacamar.common.metadata.spec.OutboundResourceAdapterImpl;
import org.ironjacamar.common.metadata.spec.ResourceAdapterImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.logging.Messages;

/**
 *
 * A Merger.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class Merger
{
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
    *
    * Merge ironJacamar's properties with connector's one returning a List of COnnector's properties
    *
    * @param ijProperties ironjacamar's extension style properties
    * @param original standard connector's properties
    * @return merged standard connector's properties (ironjacamar's setting overwrite the standard's one.
    *   No new property is added)
    */
   private List<ConfigProperty> mergeConfigProperties(Map<String, String> ijProperties,
      List<ConfigProperty> original)
   {
      List<ConfigProperty> mergedProperties = new ArrayList<ConfigProperty>(original.size());
      for (ConfigProperty c : original)
      {
         if (ijProperties != null && ijProperties.containsKey(c.getConfigPropertyName().getValue()))
         {
            XsdString newValue = new XsdString(ijProperties.get(c.getConfigPropertyName().getValue()), c
                                               .getConfigPropertyValue().getId(), c.getConfigPropertyValue().getTag());
            ConfigProperty newProp = new ConfigPropertyImpl(c.getDescriptions(), c.getConfigPropertyName(),
                                                            c.getConfigPropertyType(), newValue,
                                                            c.getConfigPropertyIgnore(),
                                                            c.getConfigPropertySupportsDynamicUpdates(),
                                                            c.getConfigPropertyConfidential(), c.getId(),
                                                            c.isMandatory(),
                                                            c.getAttachedClassName(), c.getConfigPropertyIgnoreId(),
                                                            c.getConfigPropertySupportsDynamicUpdatesId(),
                                                            c.getConfigPropertyConfidentialId());

            mergedProperties.add(newProp);
         }
         else
         {
            mergedProperties.add(c);
         }
      }
      return mergedProperties;
   }

   /**
    * Merge a {@link Connector} and a {@link Activation} to form the resulting spec metadata
    * @param conn {@link Connector} object
    * @param ij The {@link Activation} object
    * @return The merged {@link Connector}
    */
   public Connector merge(Connector conn, Activation ij)
   {
      if (ij == null)
         return conn;

      //merge transactionSupport;
      mergeTransactionSupport(ij, conn);

      // merge RA onfigProperties;
      List<ConfigProperty> original = conn.getResourceadapter().getConfigProperties();
      List<ConfigProperty> newProperties = this.mergeConfigProperties(ij.getConfigProperties(), original);

      ((ResourceAdapterImpl) conn.getResourceadapter()).forceConfigProperties(newProperties);

      if (conn.getVersion() != Version.V_10)
      {
         //merge adminObjects;

         ResourceAdapter ra = conn.getResourceadapter();
         if (ra != null && ra.getAdminObjects() != null)
         {
            List<AdminObject> newAdminObjects = new ArrayList<AdminObject>(ra.getAdminObjects().size());
            for (AdminObject adminObj : ra.getAdminObjects())
            {
               AdminObject newAdminObj = adminObj;
               newAdminObjects.add(newAdminObj);
            }
            ((ResourceAdapterImpl) ra).forceAdminObjects(newAdminObjects);
         }
         //merge connectionDefinitions;
         if (ra != null && ra.getOutboundResourceadapter() != null &&
             ra.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            List<ConnectionDefinition> newConDefs = new ArrayList<ConnectionDefinition>(ra
               .getOutboundResourceadapter().getConnectionDefinitions().size());
            for (ConnectionDefinition conDef : ra.getOutboundResourceadapter().getConnectionDefinitions())
            {
               ConnectionDefinition newConDef = conDef;
               newConDefs.add(newConDef);
            }
            ((OutboundResourceAdapterImpl) ra.getOutboundResourceadapter())
               .forceConnectionDefinitions(newConDefs);
         }
      }

      return conn;
   }

   private void mergeTransactionSupport(Activation ij, Connector conn)
   {
      if (ij.getTransactionSupport() != null && conn.getResourceadapter() != null)
      {
         ResourceAdapter ra = conn.getResourceadapter();
         if (ra.getOutboundResourceadapter() != null)
         {
            ((OutboundResourceAdapterImpl) ra.getOutboundResourceadapter()).
               forceTransactionSupport(ij.getTransactionSupport());
         }
      }
   }

   /**
    *
    * Merge a connector with a DataSource metadata
    *
    * @param connector the connector to merge
    * @param cds the datasource it is one of interface extending {@link CommonDataSource}.
    *   IOW it can be both {@link DataSource} or {@link XaDataSource}
    * @return the connector with mapped properties taken forn ds
    * @throws IllegalArgumentException if version is't 1.0, 1.5 or 1.6
    * @throws Exception in case of error
    */
   public Connector merge(Connector connector, CommonDataSource cds) throws IllegalArgumentException,
                                                                            Exception
   {
      if (cds == null)
      {
         return null;
      }
      else
      {
         return mergeConnectorWithProperties(connector, createConfigProperties(cds, extractProperties(connector)),
            null);
      }
   }

   private Connector mergeConnectorWithProperties(Connector connector, List<ConfigProperty> connectioDefProperties,
      List<ConfigProperty> raConfigProperties) throws IllegalArgumentException, Exception
   {

      XsdString managedconnectionfactoryClass = null;

      String id = null;

      XsdString connectionfactoryImplClass = null;
      XsdString connectionfactoryInterface = null;
      XsdString connectionImplClass = null;
      XsdString connectionInterface = null;
      List<AuthenticationMechanism> authenticationMechanism = null;
      boolean reauthenticationSupport = false;
      List<SecurityPermission> securityPermissions = null;

      XsdString vendorName = null;
      List<LocalizedXsdString> description = null;
      XsdString resourceadapterVersion = null;
      XsdString moduleName = null;
      XsdString eisType = null;
      LicenseType license = null;
      List<LocalizedXsdString> displayNames = null;
      List<Icon> icons = null;
      List<AdminObject> adminobjects = null;
      TransactionSupportEnum transactionSupport = null;

      if (connector.getVersion() == Version.V_10)
      {
         if (raConfigProperties != null)
         {
            if (connectioDefProperties == null)
            {
               connectioDefProperties = raConfigProperties;
            }
            else
            {
               connectioDefProperties.addAll(raConfigProperties);
            }
         }

         List<ConnectionDefinition> cds = new ArrayList<ConnectionDefinition>(1);
         ConnectionDefinition cd = new ConnectionDefinitionImpl(managedconnectionfactoryClass,
                                                                connectioDefProperties,
                                                                connectionfactoryInterface,
                                                                connectionfactoryImplClass,
                                                                connectionInterface, 
                                                                connectionImplClass, id);
         cds.add(cd);

         OutboundResourceAdapter ora = new OutboundResourceAdapterImpl(cds,
                                                                       transactionSupport,
                                                                       authenticationMechanism,
                                                                       reauthenticationSupport,
                                                                       id, null, null);

         //building and returning object
         ResourceAdapter resourceadapter = new ResourceAdapterImpl(null, null, ora, null, null,
                                                                   securityPermissions, id);

         Connector newConnector = new ConnectorImpl(Version.V_10, null, vendorName, eisType, resourceadapterVersion,
                                                    license, resourceadapter, null, true,
                                                    description, displayNames, icons, id);

         return newConnector.merge(connector);
      }
      else
      {
         List<ConnectionDefinition> connectionDefinitions = new ArrayList<ConnectionDefinition>(1);
         ConnectionDefinition connectionDefinition = new ConnectionDefinitionImpl(managedconnectionfactoryClass,
                                                                                  connectioDefProperties,
                                                                                  connectionfactoryInterface,
                                                                                  connectionfactoryImplClass,
                                                                                  connectionInterface,
                                                                                  connectionImplClass, id);
         connectionDefinitions.add(connectionDefinition);
         OutboundResourceAdapter outboundResourceadapter = new OutboundResourceAdapterImpl(connectionDefinitions,
                                                                                           transactionSupport,
                                                                                           authenticationMechanism,
                                                                                           reauthenticationSupport,
                                                                                           id, null, null);

         String resourceadapterClass = null;
         InboundResourceAdapter inboundResourceadapter = null;
         ResourceAdapter resourceadapter = new ResourceAdapterImpl(new XsdString(resourceadapterClass, null),
                                                                   raConfigProperties,
                                                                   outboundResourceadapter,
                                                                   inboundResourceadapter, adminobjects,
                                                                   securityPermissions, id);

         if (connector.getVersion() == Version.V_20)
         {
            Connector newConnector = new ConnectorImpl(Version.V_20, moduleName, vendorName, eisType,
                    resourceadapterVersion,
                    license, resourceadapter, null,
                    false, description, displayNames, icons, id);

            return newConnector.merge(connector);
         }
         if (connector.getVersion() == Version.V_17)
         {
            Connector newConnector = new ConnectorImpl(Version.V_17, moduleName, vendorName, eisType,
                                                       resourceadapterVersion,
                                                       license, resourceadapter, null,
                                                       false, description, displayNames, icons, id);

            return newConnector.merge(connector);
         }
         else if (connector.getVersion() == Version.V_16)
         {
            Connector newConnector = new ConnectorImpl(Version.V_16, moduleName, vendorName, eisType,
                                                       resourceadapterVersion,
                                                       license, resourceadapter, null,
                                                       false, description, displayNames, icons, id);

            return newConnector.merge(connector);
         }
         else if (connector.getVersion() == Version.V_15)
         {
            Connector newConnector = new ConnectorImpl(Version.V_15, null, vendorName, eisType, resourceadapterVersion,
                                                       license, resourceadapter, null, true, description, displayNames,
                                                       icons, id);

            return newConnector.merge(connector);
         }
         else
            throw new IllegalArgumentException(bundle.wrongVersion(connector.getVersion().name()));
      }

   }

   private List<ConfigProperty> extractProperties(Connector connector)
   {
      List<ConfigProperty> originalProperties = null;
      if (connector.getVersion() == Version.V_10)
      {
         originalProperties = connector.getResourceadapter().getConfigProperties();
      }
      else
      {
         if (connector.getResourceadapter() != null)
         {
            ResourceAdapter ra = connector.getResourceadapter();
            if (ra.getOutboundResourceadapter() != null &&
                ra.getOutboundResourceadapter().getConnectionDefinitions() != null)
            {
               originalProperties = ra.getOutboundResourceadapter().getConnectionDefinitions().get(0)
                  .getConfigProperties();
            }
         }
      }
      return originalProperties;
   }

   private static List<ConfigProperty> createConfigProperties(CommonDataSource cds,
      List<ConfigProperty> originalProperties)
   {
      DataSource ds = null;
      XaDataSource xads = null;
      if (cds instanceof DataSource)
      {
         ds = (DataSource) cds;
      }
      if (cds instanceof XaDataSource)
      {
         xads = (XaDataSource) cds;
      }
      if (originalProperties != null)
      {
         List<ConfigProperty> configProperties = new ArrayList<ConfigProperty>(originalProperties.size());
         for (ConfigProperty property : originalProperties)
         {

            ConfigPropertyFactory.Prototype prototype = ConfigPropertyFactory.Prototype.forName(property
               .getConfigPropertyName().getValue());
            switch (prototype)
            {
               case XADATASOURCEPROPERTIES : {
                  if (xads != null && xads.getXaDataSourceProperty() != null)
                  {
                     StringBuilder valueBuf = new StringBuilder();
                     for (Entry<String, String> xaConfigProperty : xads.getXaDataSourceProperty().entrySet())
                     {
                        valueBuf.append(xaConfigProperty.getKey());
                        valueBuf.append("=");
                        valueBuf.append(xaConfigProperty.getValue());
                        valueBuf.append(";");
                     }
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, valueBuf.toString()));

                  }

                  break;
               }

               case URLDELIMITER : {
                  if (ds != null && ds.getUrlDelimiter() != null && !ds.getUrlDelimiter().trim().equals(""))
                  {
                     configProperties
                        .add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getUrlDelimiter()));
                  }

                  break;
               }

               case URLSELECTORSTRATEGYCLASSNAME : {
                  if (ds != null && ds.getUrlSelectorStrategyClassName() != null &&
                      !ds.getUrlSelectorStrategyClassName().trim().equals(""))
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                        ds.getUrlSelectorStrategyClassName()));
                  }

                  break;
               }

               case XADATASOURCECLASS : {
                  if (xads != null && xads.getXaDataSourceClass() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                        xads.getXaDataSourceClass()));
                  }

                  break;
               }

               case TRANSACTIONISOLATION : {
                  if (ds != null && ds.getTransactionIsolation() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds
                        .getTransactionIsolation().name()));
                  }

                  break;
               }

               case PREPAREDSTATEMENTCACHESIZE : {
                  if (ds != null && ds.getStatement() != null &&
                      ds.getStatement().getPreparedStatementsCacheSize() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getStatement()
                        .getPreparedStatementsCacheSize()));
                  }

                  break;
               }

               case SHAREPREPAREDSTATEMENTS : {
                  if (ds != null && ds.getStatement() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                        ds.getStatement() != null && ds.getStatement().isSharePreparedStatements()));
                  }

                  break;
               }

               case NEWCONNECTIONSQL : {
                  if (ds != null && ds.getNewConnectionSql() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                        ds.getNewConnectionSql()));
                  }

                  break;
               }

               case CHECKVALIDCONNECTIONSQL : {
                  if (ds != null && ds.getValidation() != null &&
                      ds.getValidation().getCheckValidConnectionSql() != null &&
                      !ds.getValidation().getCheckValidConnectionSql().trim().equals(""))
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getValidation()
                        .getCheckValidConnectionSql()));
                  }

                  break;
               }

               case VALIDCONNECTIONCHECKERCLASSNAME : {
                  if (ds != null && ds.getValidation() != null &&
                      ds.getValidation().getValidConnectionChecker() != null &&
                      ds.getValidation().getValidConnectionChecker().getClassName() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getValidation()
                        .getValidConnectionChecker().getClassName()));
                  }

                  break;
               }
               case VALIDCONNECTIONCHECKERPROPERTIES : {
                  if (ds != null && ds.getValidation() != null &&
                      ds.getValidation().getValidConnectionChecker() != null &&
                      ds.getValidation().getValidConnectionChecker().getClassName() != null)
                  {
                     StringBuilder valueBuf = new StringBuilder();
                     for (Entry<String, String> connProperty : ds.getValidation().getValidConnectionChecker()
                        .getConfigPropertiesMap().entrySet())
                     {
                        valueBuf.append(connProperty.getKey());
                        valueBuf.append("=");
                        valueBuf.append(connProperty.getValue());
                        valueBuf.append(";");
                     }
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, valueBuf.toString()));

                  }
                  break;
               }

               case EXCEPTIONSORTERCLASSNAME : {
                  if (ds != null && ds.getValidation() != null && ds.getValidation().getExceptionSorter() != null &&
                      ds.getValidation().getExceptionSorter().getClassName() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getValidation()
                        .getExceptionSorter().getClassName()));
                  }

                  break;
               }
               case EXCEPTIONSORTERPROPERTIES : {
                  if (ds != null && ds.getValidation() != null && ds.getValidation().getExceptionSorter() != null)
                  {
                     StringBuilder valueBuf = new StringBuilder();
                     for (Entry<String, String> connProperty : ds.getValidation().getExceptionSorter()
                        .getConfigPropertiesMap().entrySet())
                     {
                        valueBuf.append(connProperty.getKey());
                        valueBuf.append("=");
                        valueBuf.append(connProperty.getValue());
                        valueBuf.append(";");
                     }
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, valueBuf.toString()));

                  }
                  break;
               }

               case STALECONNECTIONCHECKERCLASSNAME : {
                  if (ds != null && ds.getValidation() != null &&
                      ds.getValidation().getStaleConnectionChecker() != null &&
                      ds.getValidation().getStaleConnectionChecker().getClassName() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getValidation()
                        .getStaleConnectionChecker().getClassName()));
                  }

                  break;
               }
               case STALECONNECTIONCHECKERPROPERTIES : {
                  if (ds != null && ds.getValidation() != null &&
                      ds.getValidation().getStaleConnectionChecker() != null)
                  {
                     StringBuilder valueBuf = new StringBuilder();
                     for (Entry<String, String> connProperty : ds.getValidation().getStaleConnectionChecker()
                        .getConfigPropertiesMap().entrySet())
                     {
                        valueBuf.append(connProperty.getKey());
                        valueBuf.append("=");
                        valueBuf.append(connProperty.getValue());
                        valueBuf.append(";");
                     }
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, valueBuf.toString()));

                  }
                  break;
               }

               case TRACKSTATEMENTS : {
                  if (ds != null && ds.getStatement() != null && ds.getStatement().getTrackStatements() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getStatement()
                        .getTrackStatements().name()));
                  }

                  break;
               }

               case TRANSACTIONQUERYTIMEOUT : {
                  if (ds != null && ds.getTimeout() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getTimeout()
                        .isSetTxQueryTimeout()));
                  }

                  break;
               }

               case QUERYTIMEOUT : {
                  if (ds != null && ds.getTimeout() != null && ds.getTimeout().getQueryTimeout() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getTimeout()
                        .getQueryTimeout()));
                  }

                  break;
               }

               case USETRYLOCK : {
                  if (ds != null && ds.getTimeout() != null && ds.getTimeout().getUseTryLock() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getTimeout()
                        .getUseTryLock()));
                  }

                  break;
               }
               case DRIVERCLASS : {
                  if (ds != null && ds.getDriverClass() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getDriverClass()));
                  }
                  break;
               }
               case DATASOURCECLASS : {
                  if (ds != null && ds.getDataSourceClass() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                                                                                     ds.getDataSourceClass()));
                  }
                  break;
               }
               case URLPROPERTY :
                  if (xads != null && xads.getUrlProperty() != null && !xads.getUrlProperty().trim().equals(""))
                  {
                     configProperties
                        .add(ConfigPropertyFactory.createConfigProperty(prototype, xads.getUrlProperty()));
                  }

                  break;

               case CONNECTIONPROPERTIES : {
                  if (ds != null && ds.getConnectionProperties() != null)
                  {
                     StringBuilder valueBuf = new StringBuilder();
                     for (Entry<String, String> connProperty : ds.getConnectionProperties().entrySet())
                     {
                        valueBuf.append(connProperty.getKey());
                        valueBuf.append("=");
                        valueBuf.append(connProperty.getValue());
                        valueBuf.append(";");
                     }
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, valueBuf.toString()));

                  }
                  break;
               }
               case CONNECTIONURL : {
                  if (ds != null && ds.getConnectionUrl() != null)
                  {
                     configProperties
                        .add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getConnectionUrl()));
                  }
                  break;
               }

               default :
                  break;
            }
         }
         if (ds != null)
         {
            for (Entry<String, String> connectionProperty : ds.getConnectionProperties().entrySet())
            {
               ConfigPropertyFactory.Prototype prototype = ConfigPropertyFactory.Prototype.forName(connectionProperty
                  .getKey());
               if (prototype != ConfigPropertyFactory.Prototype.UNKNOWN)
               {
                  configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                     connectionProperty.getValue()));
               }
            }
         }
         return configProperties;
      }
      else
      {
         return null;
      }
   }

   /**
    *
    * A ConfigPropertyFactory.
    *
    * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
    *
    */
   protected static class ConfigPropertyFactory
   {
      /**
       *
       * create a config property from a prototype
       *
       * @param prototype prototype
       * @param value value
       * @return the property created
       */
      public static ConfigProperty createConfigProperty(Prototype prototype, String value)
      {

         return new ConfigPropertyImpl(prototype.getDescription(), prototype.getLocalName(),
                                       prototype.getLocalType(), new XsdString(value, null), Boolean.FALSE,
                                       Boolean.FALSE, Boolean.FALSE, null, false, null, null, null, null);
      }

      /**
      *
      * create a config property from a prototype
      *
      * @param prototype prototype
      * @param value value
      * @return the property created
      */
      public static ConfigProperty createConfigProperty(Prototype prototype, boolean value)
      {

         return new ConfigPropertyImpl(prototype.getDescription(), prototype.getLocalName(),
                                       prototype.getLocalType(), new XsdString(String.valueOf(value), null),
                                       Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null, false,
                                       null, null, null, null);

      }

      /**
      *
      * create a config property from a prototype
      *
      * @param prototype prototype
      * @param value value
      * @return the property created
      */
      public static ConfigProperty createConfigProperty(Prototype prototype, Number value)
      {

         return new ConfigPropertyImpl(prototype.getDescription(), prototype.getLocalName(),
                                       prototype.getLocalType(), new XsdString(String.valueOf(value), null),
                                       Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null, false,
                                       null, null, null, null);
      }

      /**
       *
       * A Prototype.
       *
       * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
       *
       */
      enum Prototype
      {
         /** UNKNOWN **/
         UNKNOWN(null, null, null),
         /** DRIVERCLASS **/
         DRIVERCLASS("DriverClass", "java.lang.String", "The jdbc driver class."),
         /** DATASOURCECLASS **/
         DATASOURCECLASS("DataSourceClass", "java.lang.String", "The jdbc datasource class."),
         /** CONNECTIONURL **/
         CONNECTIONURL("ConnectionURL", "java.lang.String", "The jdbc connection url class."),
         /** CONNECTIONPROPERTIES **/
         CONNECTIONPROPERTIES("ConnectionProperties", "java.lang.String", "Connection properties for the database."),

         /** USERNAME **/
         USERNAME("UserName", "java.lang.String", "The default user name used to create JDBC connections."),
         /** PASSWORD **/
         PASSWORD("Password", "java.lang.String", "The default password used to create JDBC connections."),
         /** XADATASOURCEPROPERTIES **/
         XADATASOURCEPROPERTIES("XADataSourceProperties", "java.lang.String",
            "The properties to set up the XA driver. These properties must be in the form "
               + "name1=value1;name2=value2;...namen=valuen"),
         /** URLDELIMITER **/
         URLDELIMITER("URLDelimiter", "java.lang.String", "The jdbc connection url delimeter."),
         /** URLPROPERTY **/
         URLPROPERTY("URLProperty", "java.lang.String", "The property that contains the list of URLs."),
         /** URLSELECTORSTRATEGYCLASSNAME **/
         URLSELECTORSTRATEGYCLASSNAME("UrlSelectorStrategyClassName", "java.lang.String",
            "The configurable URLSelectorStrategy class name."),
         /** XADATASOURCECLASS **/
         XADATASOURCECLASS("XADataSourceClass", "java.lang.String",
            "The class name of the JDBC XA driver that handlesthis JDBC URL."),
         /** TRANSACTIONISOLATION **/
         TRANSACTIONISOLATION("TransactionIsolation", "java.lang.String",
            "The transaction isolation for new connections. Not necessary: the driver default will be used "
               + "if ommitted."),
         /** PREPAREDSTATEMENTCACHESIZE **/
         PREPAREDSTATEMENTCACHESIZE("PreparedStatementCacheSize", "java.lang.Integer",
            "The number of cached prepared statements per connection."),
         /** SHAREPREPAREDSTATEMENTS **/
         SHAREPREPAREDSTATEMENTS("SharePreparedStatements", "java.lang.Boolean",
            "Whether to share prepared statements."),
         /** NEWCONNECTIONSQL **/
         NEWCONNECTIONSQL("NewConnectionSQL", "java.lang.String",
            "An SQL statement to be executed when a new connection is created as auxillary setup."),
         /** CHECKVALIDCONNECTIONSQL **/
         CHECKVALIDCONNECTIONSQL("CheckValidConnectionSQL", "java.lang.String",
            "An SQL statement that may be executed when a managed connection is taken out of the pool and is "
               + "about to be given to a client: the purpose is to verify that the connection still works."),
         /** VALIDCONNECTIONCHECKERCLASSNAME **/
         VALIDCONNECTIONCHECKERCLASSNAME("ValidConnectionCheckerClassName", "java.lang.String",
            "The fully qualified name of a class implementing org.ironjacamar.adapters.jdbc.ValidConnectionChecker"
               + " that can determine for a particular vender db when a connection is valid."),
         /** VALIDCONNECTIONCHECKERPROPERTIES **/
         VALIDCONNECTIONCHECKERPROPERTIES("ValidConnectionCheckerProperties", "java.lang.String",
            "The properties to inect into class implementing org.ironjacamar.adapters.jdbc.ValidConnectionChecker"
               + " that can determine for a particular vender db when a connection is valid."),
         /** EXCEPTIONSORTERCLASSNAME **/
         EXCEPTIONSORTERCLASSNAME("ExceptionSorterClassName", "java.lang.String",
            "The fully qualified name of a class implementing org.ironjacamar.adapters.jdbc.ExceptionSorter that"
               + " can determine for a particular vender db which exceptions are "
               + "fatal and mean a connection should be discarded."),
         /** EXCEPTIONSORTERPROPERTIES **/
         EXCEPTIONSORTERPROPERTIES("ExceptionSorterProperties", "java.lang.String",
            "The properties to inect into  class implementing org.ironjacamar.adapters.jdbc.ExceptionSorter that"
               + " can determine for a particular vender db which exceptions are "
               + "fatal and mean a connection should be discarded."),
         /** STALECONNECTIONCHECKERCLASSNAME **/
         STALECONNECTIONCHECKERCLASSNAME("StaleConnectionCheckerClassName", "java.lang.String",
            "The fully qualified name of a class implementing org.ironjacamar.adapters.jdbc.StaleConnectionChecker"
               + " that can determine for a particular vender db when a connection is stale."),
         /** STALECONNECTIONCHECKERPROPERTIES **/
         STALECONNECTIONCHECKERPROPERTIES("StaleConnectionCheckerProperties", "java.lang.String",
            "The properties to inect into  class implementing org.ironjacamar.adapters.jdbc.StaleConnectionChecker"
               + " that can determine for a particular vender db when a connection is stale."),
         /** TRACKSTATEMENTS **/
         TRACKSTATEMENTS("TrackStatements", "java.lang.String",
            "Whether to track unclosed statements - false/true/nowarn"),
         /** TRANSACTIONQUERYTIMEOUT **/
         TRANSACTIONQUERYTIMEOUT("TransactionQueryTimeout", "java.lang.Boolean",
            "Whether to set the query timeout based on the transaction timeout"),
         /** QUERYTIMEOUT **/
         QUERYTIMEOUT("QueryTimeout", "java.lang.Integer", "A configured query timeout"),
         /** USETRYLOCK **/
         USETRYLOCK("UseTryLock", "java.lang.Integer", "Maximum wait for a lock");

         private final XsdString localName;

         private final XsdString localType;

         private final ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>(1);

         /**
          * Create a new Prototype.
          *
          * @param name name
          * @param type type
          * @param description description
          */
         private Prototype(String name, String type, String description)
         {
            this.localName = new XsdString(name, null);
            this.localType = new XsdString(type, null);;
            this.description.add(new LocalizedXsdString(description, null));
         }

         /**
           * Get the name.
           *
           * @return the name.
           */
         public final XsdString getLocalName()
         {
            return localName;
         }

         /**
          * Get the type.
          *
          * @return the type.
          */
         public final XsdString getLocalType()
         {
            return localType;
         }

         /**
          * Get the description.
          *
          * @return the description.
          */
         public final List<LocalizedXsdString> getDescription()
         {
            return description;
         }

         private static final Map<String, Prototype> MAP;

         static
         {
            final Map<String, Prototype> map = new HashMap<String, Prototype>();
            for (Prototype element : values())
            {
               final String name = element.getLocalName().getValue();
               if (name != null)
                  map.put(name, element);
            }
            MAP = map;
         }

         /**
         *
         * Static method to get enum instance given localName XsdString
         *
         * @param localName a XsdString used as localname (typically tag name as defined in xsd)
         * @return the enum instance
         */
         public static Prototype forName(String localName)
         {
            final Prototype element = MAP.get(localName);
            return element == null ? UNKNOWN : element;
         }
      }

   }
}
