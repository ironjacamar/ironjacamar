/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.common.metadata;

import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ds.CommonDataSource;
import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.metadata.jbossra.JbossRa;
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
import org.jboss.jca.common.metadata.jbossra.JbossRaParser;
import org.jboss.jca.common.metadata.ra.RaParser;
import org.jboss.jca.common.metadata.ra.common.ConfigPropertyImpl;
import org.jboss.jca.common.metadata.ra.common.ConnectionDefinitionImpl;
import org.jboss.jca.common.metadata.ra.common.OutboundResourceAdapterImpl;
import org.jboss.jca.common.metadata.ra.common.ResourceAdapter1516Impl;
import org.jboss.jca.common.metadata.ra.ra10.Connector10Impl;
import org.jboss.jca.common.metadata.ra.ra10.ResourceAdapter10Impl;
import org.jboss.jca.common.metadata.ra.ra15.Connector15Impl;
import org.jboss.jca.common.metadata.ra.ra16.Connector16Impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.logging.Logger;

/**
 *
 * A MetadataFactory.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class MetadataFactory
{
   private static Logger log = Logger.getLogger(MetadataFactory.class);

   /**
    * Constructor
    */
   public MetadataFactory()
   {
   }

   /**
    * Get the JCA standard metadata
    * @param root The root of the deployment
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   public Connector getStandardMetaData(File root) throws Exception
   {
      Connector result = null;
      File metadataFile = new File(root, "/META-INF/ra.xml");

      if (metadataFile.exists())
      {
         InputStream input = null;
         String url = metadataFile.getAbsolutePath();
         try
         {
            long start = System.currentTimeMillis();
            input = new FileInputStream(metadataFile);

            result = (new RaParser()).parse(input);

            log.debugf("Total parse for %s took %d ms", url, (System.currentTimeMillis() - start));

            //log.tracef("successufully deployed $s", result.toString());

         }
         catch (Exception e)
         {
            log.errorf(e, "Error during parsing: %s", url);
            throw e;
         }
         finally
         {
            if (input != null)
               input.close();
         }

      }
      else
      {
         log.tracef("metadata file %s does not exist", metadataFile.toString());
      }

      return result;
   }

   /**
    * Get the JBoss specific metadata
    * @param root The root of the deployment
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   public JbossRa getJBossMetaData(File root) throws Exception
   {
      JbossRa result = null;

      File metadataFile = new File(root, "/META-INF/jboss-ra.xml");

      if (metadataFile.exists())
      {
         InputStream input = null;
         String url = metadataFile.getAbsolutePath();
         try
         {
            long start = System.currentTimeMillis();

            input = new FileInputStream(metadataFile);
            result = (new JbossRaParser()).parse(input);

            log.debugf("Total parse for %s took %d ms", url, (System.currentTimeMillis() - start));

            log.tracef("successufully deployed %s", result.toString());
         }
         catch (Exception e)
         {
            log.error("Error during parsing: " + url, e);
            throw e;
         }
         finally
         {
            if (input != null)
               input.close();
         }
      }

      return result;
   }

   /**
    *
    * Create a connector from a DataSource metadata
    *
    * @param cds the datasource it is one of interface extending {@link CommonDataSource}.
    *   IOW it can be both {@link DataSource} or {@link XaDataSource}
    * @param connector the connector to merge
    * @return the connector with mapped properties taken forn ds
    * @throws IllegalArgumentException if version is't 1.0, 1.5 or 1.6
    * @throws Exception in case of error
    */
   public Connector mergeConnectorAndDs(CommonDataSource cds, Connector connector)
      throws IllegalArgumentException, Exception
   {
      if (cds == null)
      {
         return null;
      }
      else
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

            List<ConfigProperty> configProperties = createConfigProperties(cds, connector.getResourceadapter()
                  .getConfigProperties());

            ResourceAdapter resourceadapter = new ResourceAdapter10Impl(managedconnectionfactoryClass,
                  connectionfactoryInterface, connectionfactoryImplClass, connectionInterface,
                  connectionImplClass, transactionSupport, authenticationMechanism, configProperties,
                  reauthenticationSupport, securityPermissions, id);

            Connector newConnector = new Connector10Impl(moduleName, vendorName, eisType, resourceadapterVersion,
                  license, resourceadapter, description, displayNames, icons, id);

            return newConnector.merge(connector);
         }
         else
         {
            List<? extends ConfigProperty> originalProperties = null;
            if (connector.getResourceadapter() != null &&
                  connector.getResourceadapter() instanceof ResourceAdapter1516)
            {
               ResourceAdapter1516 ra1516 = ((ResourceAdapter1516) connector.getResourceadapter());
               if (ra1516.getOutboundResourceadapter() != null &&
                     ra1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
               {
                  originalProperties = ra1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
                        .getConfigProperties();
               }
            }

            List<ConfigProperty> configProperties = createConfigProperties(cds, originalProperties);

            List<ConnectionDefinition> connectionDefinitions = new ArrayList<ConnectionDefinition>(1);
            ConnectionDefinition connectionDefinition = new ConnectionDefinitionImpl(
                  managedconnectionfactoryClass, configProperties, connectionfactoryInterface,
                  connectionfactoryImplClass, connectionInterface, connectionImplClass, id);
            connectionDefinitions.add(connectionDefinition);
            OutboundResourceAdapter outboundResourceadapter = new OutboundResourceAdapterImpl(
                  connectionDefinitions, transactionSupport, authenticationMechanism, reauthenticationSupport, id);
            String resourceadapterClass = null;
            List<? extends ConfigProperty> raConfigProperties = null;
            InboundResourceAdapter inboundResourceadapter = null;
            ResourceAdapter1516 resourceadapter = new ResourceAdapter1516Impl(resourceadapterClass,
                  raConfigProperties, outboundResourceadapter, inboundResourceadapter, adminobjects,
                  securityPermissions, id);

            if (connector.getVersion() == Version.V_16)
            {
               List<String> requiredWorkContexts = null;
               boolean metadataComplete = false;

               Connector newConnector = new Connector16Impl(moduleName, vendorName, eisType,
                     resourceadapterVersion, license, resourceadapter, requiredWorkContexts, metadataComplete,
                     description, displayNames, icons, id);

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
                  if (ds != null && ds.getUserName() != null && !ds.getUserName().trim().equals(""))
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getUserName()));
                  }

                  break;
               }

               case PASSWORD : {
                  if (ds != null && ds.getPassword() != null && !ds.getPassword().trim().equals(""))
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getPassword()));
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
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                           valueBuf.toString()));

                  }

                  break;
               }

               case URLDELIMITER : {
                  if (ds != null && ds.getUrlDelimiter() != null && !ds.getUrlDelimiter().trim().equals(""))
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                           ds.getUrlDelimiter()));
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
                        ds.getValidation().getCheckValidConnectionSql() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getValidation()
                           .getCheckValidConnectionSql()));
                  }

                  break;
               }

               case EXCEPTIONSORTERCLASSNAME : {
                  if (ds != null && ds.getValidation() != null &&
                        ds.getValidation().getExceptionSorterClassName() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getValidation()
                           .getExceptionSorterClassName()));
                  }

                  break;
               }

               case STALECONNECTIONCHECKERCLASSNAME : {
                  if (ds != null && ds.getValidation() != null &&
                        ds.getValidation().getStaleConnectionCheckerClassName() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype, ds.getValidation()
                           .getStaleConnectionCheckerClassName()));
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
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                           ds.getDriverClass()));
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
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                           valueBuf.toString()));

                  }
                  break;
               }
               case CONNECTIONURL : {
                  if (ds != null && ds.getConnectionUrl() != null)
                  {
                     configProperties.add(ConfigPropertyFactory.createConfigProperty(prototype,
                           ds.getConnectionUrl()));
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
               ConfigPropertyFactory.Prototype prototype = ConfigPropertyFactory.Prototype
                     .forName(connectionProperty.getKey());
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
         /** CONNECTIONURL **/
         CONNECTIONURL("ConnectionURL", "java.lang.String", "The jdbc connection url class."),
         /** CONNECTIONPROPERTIES **/
         CONNECTIONPROPERTIES("ConnectionProperties", "java.lang.String",
               "Connection properties for the database."),

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
         /** EXCEPTIONSORTERCLASSNAME **/
         EXCEPTIONSORTERCLASSNAME(
               "ExceptionSorterClassName",
               "java.lang.String",
               "The fully qualified name of a class implementing org.jboss.jca.adapters.jdbc.ExceptionSorter that"
                     + " can determine for a particular vender db which exceptions are "
                     + "fatal and mean a connection should be discarded."),
         /** STALECONNECTIONCHECKERCLASSNAME **/
         STALECONNECTIONCHECKERCLASSNAME("StaleConnectionCheckerClassName", "java.lang.String",
               "The fully qualified name of a class implementing org.jboss.jca.adapters.jdbc.StaleConnectionChecker"
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
