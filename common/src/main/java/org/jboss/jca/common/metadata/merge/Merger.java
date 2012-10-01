/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.metadata.merge;

import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonIronJacamar;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ds.CommonDataSource;
import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.Icon;
import org.jboss.jca.common.api.metadata.ra.InboundResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.LicenseType;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.OutboundResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.SecurityPermission;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16;
import org.jboss.jca.common.metadata.ra.common.AbstractResourceAdapetrImpl;
import org.jboss.jca.common.metadata.ra.common.AdminObjectImpl;
import org.jboss.jca.common.metadata.ra.common.ConfigPropertyImpl;
import org.jboss.jca.common.metadata.ra.common.ConnectionDefinitionImpl;
import org.jboss.jca.common.metadata.ra.common.OutboundResourceAdapterImpl;
import org.jboss.jca.common.metadata.ra.common.ResourceAdapter1516Impl;
import org.jboss.jca.common.metadata.ra.ra10.Connector10Impl;
import org.jboss.jca.common.metadata.ra.ra10.ResourceAdapter10Impl;
import org.jboss.jca.common.metadata.ra.ra15.Connector15Impl;
import org.jboss.jca.common.metadata.ra.ra16.ConfigProperty16Impl;
import org.jboss.jca.common.metadata.ra.ra16.Connector16Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * A Merger.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class Merger
{
   /**
   *
   * Merge ironJacamar's properties with connector's one returning a List of COnnector's properties
   *
   * @param ijProperties ironjacamar's extension style properties
   * @param original standard connector's properties
   * @return merged standard connector's properties (ironjacamar's setting overwrite the standard's one.
   *   No new property is added)
   */
   public List<ConfigProperty> mergeConfigProperties(Map<String, String> ijProperties,
      List<? extends ConfigProperty> original)
   {
      List<ConfigProperty> mergedProperties = new ArrayList<ConfigProperty>(original.size());
      for (ConfigProperty c : original)
      {
         if (ijProperties != null && ijProperties.containsKey(c.getConfigPropertyName().getValue()))
         {
            if (c instanceof ConfigProperty16)
            {
               ConfigProperty16 c16 = (ConfigProperty16) c;
               XsdString newValue = new XsdString(ijProperties.get(c.getConfigPropertyName().getValue()), c
                  .getConfigPropertyValue().getId(), c.getConfigPropertyValue().getTag());
               ConfigProperty16 newProp = new ConfigProperty16Impl(c.getDescriptions(), c.getConfigPropertyName(),
                                                                   c.getConfigPropertyType(), newValue,
                                                                   c16.getConfigPropertyIgnore(),
                                                                   c16.getConfigPropertySupportsDynamicUpdates(),
                                                                   c16.getConfigPropertyConfidential(), c.getId());
               mergedProperties.add(newProp);
            }
            else
            {
               XsdString newValue = new XsdString(ijProperties.get(c.getConfigPropertyName().getValue()), c
                  .getConfigPropertyValue().getId(), c.getConfigPropertyValue().getTag());
               ConfigProperty newProp = new ConfigPropertyImpl(c.getDescriptions(), c.getConfigPropertyName(),
                                                               c.getConfigPropertyType(), newValue, c.getId());
               mergedProperties.add(newProp);
            }

         }
         else
         {
            mergedProperties.add(c);
         }
      }
      return mergedProperties;
   }

   /**
    *
    * Merge a {@link Connector} and a {@link CommonIronJacamar}
    *
    * @param ij the {@link CommonIronJacamar} object
    * @param conn {@link Connector} object
    * @return The merged {@link Connector}
    */
   public Connector mergeConnectorWithCommonIronJacamar(CommonIronJacamar ij, Connector conn)
   {
      return this.mergeConnectorWithCommonIronJacamar(ij, conn, null, null);
   }

   /**
   *
   * Merge a {@link Connector} and a {@link CommonIronJacamar} passing also Matcher to identify {@link AdminObject}
   * and {@link ConnectionDefinition} to merge inside the passed objects
   *
   * @param ij the {@link CommonIronJacamar} object
   * @param conn {@link Connector} object
    * @param adminMatcher the matcher for {@link AdminObject} and {@link CommonAdminObject}
    * if null {@link DefaultAdminObjectMatcher} is used
    * @param connDefMatcher the matcher for {@link ConnectionDefinition} andf {@link CommonConnDef}.
    * if null {@link DefaultConnectionDefinitionMatcher} is used
   * @return The merged {@link Connector}
   */
   /**
    * FIXME Comment this
    *
    * @param ij
    * @param conn
    * @param adminMatcher
    * @param connDefMatcher
    * @return
    */
   public Connector mergeConnectorWithCommonIronJacamar(CommonIronJacamar ij, Connector conn,
      ExtensionMatcher<AdminObject, CommonAdminObject> adminMatcher,
      ExtensionMatcher<ConnectionDefinition, CommonConnDef> connDefMatcher)
   {

      if (ij == null)
         return conn;

      if (adminMatcher == null)
         adminMatcher = new DefaultAdminObjectMatcher();

      if (connDefMatcher == null)
         connDefMatcher = new DefaultConnectionDefinitionMatcher();

      //merge transactionSupport;
      mergeTransactionSupport(ij, conn);

      // merge RA onfigProperties;
      List<? extends ConfigProperty> original = conn.getResourceadapter().getConfigProperties();
      List<? extends ConfigProperty> newProperties = this.mergeConfigProperties(ij.getConfigProperties(), original);

      ((AbstractResourceAdapetrImpl) conn.getResourceadapter()).forceNewConfigPropertiesContent(newProperties);

      if (conn.getVersion() != Version.V_10)
      {
         //merge adminObjects;

         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) conn.getResourceadapter();
         if (ra1516 != null && ra1516.getAdminObjects() != null)
         {
            List<AdminObject> newAdminObjects = new ArrayList<AdminObject>(ra1516.getAdminObjects().size());
            for (AdminObject adminObj : ra1516.getAdminObjects())
            {

               AdminObject newAdminObj = adminObj;

               newAdminObjects.add(newAdminObj);

            }
            ((ResourceAdapter1516Impl) ra1516).forceAdminObjectsContent(newAdminObjects);
         }
         //merge connectionDefinitions;
         if (ra1516 != null && ra1516.getOutboundResourceadapter() != null &&
             ra1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            List<ConnectionDefinition> newConDefs = new ArrayList<ConnectionDefinition>(ra1516
               .getOutboundResourceadapter().getConnectionDefinitions().size());
            for (ConnectionDefinition conDef : ra1516.getOutboundResourceadapter().getConnectionDefinitions())
            {

               ConnectionDefinition newConDef = conDef;

               newConDefs.add(newConDef);

            }
            ((OutboundResourceAdapterImpl) ra1516.getOutboundResourceadapter())
               .forceConnectionDefinitionsContent(newConDefs);
         }

      }

      return conn;
   }

   private ConnectionDefinition mergeConDef(CommonConnDef commonConDef, ConnectionDefinition conDef)
   {
      // merge ConnectionDefinition onfigProperties;
      List<? extends ConfigProperty> original = conDef.getConfigProperties();
      List<ConfigProperty> newProperties = this.mergeConfigProperties(commonConDef.getConfigProperties(), original);


      ((ConnectionDefinitionImpl) conDef).forceNewConfigPropertiesContent(newProperties);

      return conDef;
   }

   private AdminObject mergeAdminObject(CommonAdminObject commonAdminObj, AdminObject adminObj)
   {
      // merge AdminObject onfigProperties;
      List<? extends ConfigProperty> original = adminObj.getConfigProperties();
      List<? extends ConfigProperty> newProperties = this.mergeConfigProperties(commonAdminObj.getConfigProperties(),
         original);

      ((AdminObjectImpl) adminObj).forceNewConfigPropertiesContent(newProperties);
      return adminObj;

   }

   private void mergeTransactionSupport(CommonIronJacamar ij, Connector conn)
   {
      if (ij.getTransactionSupport() != null)
      {
         if (conn.getVersion() == Version.V_10 && conn.getResourceadapter() != null)
         {
            ((ResourceAdapter10Impl) conn.getResourceadapter()).forceNewTrasactionSupport(ij.getTransactionSupport());
         }
         else
         {
            if (conn.getResourceadapter() != null)
            {
               ResourceAdapter1516 ra1516 = (ResourceAdapter1516) conn.getResourceadapter();
               if (ra1516.getOutboundResourceadapter() != null)
               {
                  ((OutboundResourceAdapterImpl) ra1516.getOutboundResourceadapter()).forceNewTrasactionSupport(ij
                     .getTransactionSupport());
               }
            }
         }
      }

   }

   /**
   *
   * Merge a connector with a DataSource metadata
   *
   * @param cds the datasource it is one of interface extending {@link CommonDataSource}.
   *   IOW it can be both {@link DataSource} or {@link XaDataSource}
   * @param connector the connector to merge
   * @return the connector with mapped properties taken forn ds
   * @throws IllegalArgumentException if version is't 1.0, 1.5 or 1.6
   * @throws Exception in case of error
   */
   public Connector mergeConnectorAndDs(CommonDataSource cds, Connector connector) throws IllegalArgumentException,
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
      String moduleName = null;
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
         ResourceAdapter resourceadapter = new ResourceAdapter10Impl(managedconnectionfactoryClass,
                                                                     connectionfactoryInterface,
                                                                     connectionfactoryImplClass, connectionInterface,
                                                                     connectionImplClass, transactionSupport,
                                                                     authenticationMechanism, connectioDefProperties,
                                                                     reauthenticationSupport, securityPermissions, id);

         Connector newConnector = new Connector10Impl(vendorName, eisType, resourceadapterVersion,
                                                      license, resourceadapter, description, displayNames, icons, id);

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
                                                                                           id);
         String resourceadapterClass = null;
         InboundResourceAdapter inboundResourceadapter = null;
         ResourceAdapter1516 resourceadapter = new ResourceAdapter1516Impl(resourceadapterClass, raConfigProperties,
                                                                           outboundResourceadapter,
                                                                           inboundResourceadapter, adminobjects,
                                                                           securityPermissions, id);

         if (connector.getVersion() == Version.V_16)
         {
            List<String> requiredWorkContexts = null;
            boolean metadataComplete = false;

            Connector newConnector = new Connector16Impl(moduleName, vendorName, eisType, resourceadapterVersion,
                                                         license, resourceadapter, requiredWorkContexts,
                                                         metadataComplete, description, displayNames, icons, id);

            return newConnector.merge(connector);
         }
         else if (connector.getVersion() == Version.V_15)
         {
            Connector newConnector = new Connector15Impl(vendorName, eisType, resourceadapterVersion, license,
                                                         resourceadapter, description, displayNames, icons, id);

            return newConnector.merge(connector);
         }
         else
            throw new IllegalArgumentException("version= " + connector.getVersion().name());
      }

   }

   private List<? extends ConfigProperty> extractProperties(Connector connector)
   {
      List<? extends ConfigProperty> originalProperties = null;
      if (connector.getVersion() == Version.V_10)
      {
         originalProperties = connector.getResourceadapter().getConfigProperties();
      }
      else
      {

         if (connector.getResourceadapter() != null && connector.getResourceadapter() instanceof ResourceAdapter1516)
         {
            ResourceAdapter1516 ra1516 = ((ResourceAdapter1516) connector.getResourceadapter());
            if (ra1516.getOutboundResourceadapter() != null &&
                ra1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
            {
               originalProperties = ra1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
                  .getConfigProperties();
            }
         }
      }
      return originalProperties;
   }

   private static List<ConfigProperty> createConfigProperties(CommonDataSource cds,
      List<? extends ConfigProperty> originalProperties)
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
               case USERNAME : {
                  if (ds != null)
                  {
                     Credential security = ds.getSecurity();
                     if (security != null && security.getUserName() != null &&
                         !security.getUserName().trim().equals(""))
                     {
                        configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                           security.getUserName()));
                     }
                  }
                  break;
               }

               case PASSWORD : {
                  if (ds != null)
                  {
                     Credential security = ds.getSecurity();
                     if (security != null && security.getPassword() != null &&
                         !security.getPassword().trim().equals(""))
                     {
                        configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                           security.getPassword()));
                     }
                  }

                  break;
               }

               case XADATASOURCEPROPERTIES : {
                  if (xads != null && xads.getXaDataSourceProperty() != null)
                  {
                     StringBuffer valueBuf = new StringBuffer();
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
                     StringBuffer valueBuf = new StringBuffer();
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
                  if (ds != null && ds.getValidation() != null && ds.getValidation().getExceptionSorter() != null &&
                      ds.getValidation().getExceptionSorter().getConfigPropertiesMap() != null)
                  {
                     StringBuffer valueBuf = new StringBuffer();
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
                      ds.getValidation().getStaleConnectionChecker() != null &&
                      ds.getValidation().getStaleConnectionChecker().getConfigPropertiesMap() != null)
                  {
                     StringBuffer valueBuf = new StringBuffer();
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

               case VALIDATEONMATCH : {
                  if (ds != null && ds.getValidation() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getValidation()
                        .isValidateOnMatch()));
                  }

                  break;
               }

               case TRANSACTIONQUERYTIMEOUT : {
                  if (ds != null && ds.getTimeOut() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getTimeOut()
                        .isSetTxQueryTimeout()));
                  }

                  break;
               }

               case QUERYTIMEOUT : {
                  if (ds != null && ds.getTimeOut() != null && ds.getTimeOut().getQueryTimeout() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getTimeOut()
                        .getQueryTimeout()));
                  }

                  break;
               }

               case USETRYLOCK : {
                  if (ds != null && ds.getTimeOut() != null && ds.getTimeOut().getUseTryLock() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getTimeOut()
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
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(
                        prototype, ds.getDataSourceClass()));
                  }
                  break;
               }
               case URLPROPERTY :
               case CONNECTIONPROPERTIES : {
                  if (ds != null && ds.getConnectionProperties() != null)
                  {
                     StringBuffer valueBuf = new StringBuffer();
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
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
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
                                       prototype.getLocalType(), new XsdString(value, null), null);
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
                                       prototype.getLocalType(), new XsdString(String.valueOf(value), null), null);
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
                                       prototype.getLocalType(), new XsdString(String.valueOf(value), null), null);
      }

      /**
       *
       * A Prototype.
       *
       * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
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
            "The fully qualified name of a class implementing org.jboss.jca.adapters.jdbc.ValidConnectionChecker"
               + " that can determine for a particular vender db when a connection is valid."),
         /** VALIDCONNECTIONCHECKERPROPERTIES **/
         VALIDCONNECTIONCHECKERPROPERTIES("ValidConnectionCheckerProperties", "java.lang.String",
            "The properties to inect into class implementing org.jboss.jca.adapters.jdbc.ValidConnectionChecker"
               + " that can determine for a particular vender db when a connection is valid."),
         /** EXCEPTIONSORTERCLASSNAME **/
         EXCEPTIONSORTERCLASSNAME("ExceptionSorterClassName", "java.lang.String",
            "The fully qualified name of a class implementing org.jboss.jca.adapters.jdbc.ExceptionSorter that"
               + " can determine for a particular vender db which exceptions are "
               + "fatal and mean a connection should be discarded."),
         /** EXCEPTIONSORTERPROPERTIES **/
         EXCEPTIONSORTERPROPERTIES("ExceptionSorterProperties", "java.lang.String",
            "The properties to inect into  class implementing org.jboss.jca.adapters.jdbc.ExceptionSorter that"
               + " can determine for a particular vender db which exceptions are "
               + "fatal and mean a connection should be discarded."),
         /** STALECONNECTIONCHECKERCLASSNAME **/
         STALECONNECTIONCHECKERCLASSNAME("StaleConnectionCheckerClassName", "java.lang.String",
            "The fully qualified name of a class implementing org.jboss.jca.adapters.jdbc.StaleConnectionChecker"
               + " that can determine for a particular vender db when a connection is stale."),
         /** STALECONNECTIONCHECKERPROPERTIES **/
         STALECONNECTIONCHECKERPROPERTIES("StaleConnectionCheckerProperties", "java.lang.String",
            "The properties to inect into  class implementing org.jboss.jca.adapters.jdbc.StaleConnectionChecker"
               + " that can determine for a particular vender db when a connection is stale."),
         /** TRACKSTATEMENTS **/
         TRACKSTATEMENTS("TrackStatements", "java.lang.String",
            "Whether to track unclosed statements - false/true/nowarn"),
         /** VALIDATEONMATCH **/
         VALIDATEONMATCH("ValidateOnMatch", "java.lang.Boolean",
            "Whether to validate the connection on the ManagedConnectionFactory.matchManagedConnection method"),
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
